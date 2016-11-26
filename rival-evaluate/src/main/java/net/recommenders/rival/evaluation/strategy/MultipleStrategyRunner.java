/*
 * Copyright 2015 recommenders.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.recommenders.rival.evaluation.strategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.Pair;

/**
 * Runner of multiple evaluation strategies.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public final class MultipleStrategyRunner {

    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String SPLITS_FOLDER = "split.folder";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String TRAINING_SUFFIX = "split.training.suffix";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String TEST_SUFFIX = "split.test.suffix";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String RECOMMENDATION_FOLDER = "recommendation.folder";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String RECOMMENDATION_SUFFIX = "recommendation.suffix";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String OUTPUT_FORMAT = "output.format";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String OUTPUT_FOLDER = "output.ranking.folder";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String GROUNDTRUTH_FOLDER = "output.groundtruth.folder";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String STRATEGIES = "strategy.classes";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String RELEVANCE_THRESHOLDS = "strategy.relevance.thresholds";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String RELPLUSN_N = "strategy.relplusn.N";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String RELPLUSN_SEED = "strategy.relplusn.seed";

    /**
     * Utility classes should not have a public or default constructor.
     */
    private MultipleStrategyRunner() {
    }

    /**
     * Main method. It receives the property file using a system property
     * ('propertyFile')
     *
     * @param args Input arguments (not used).
     * @throws Exception if no properties can be read.
     */
    public static void main(final String[] args) throws Exception {
        String propertyFile = System.getProperty("propertyFile");

        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        for (String pr : properties.stringPropertyNames()) {
            System.out.println(pr + " : " + properties.getProperty(pr));
        }

        run(properties);
    }

    /**
     * Method that runs several strategies (depending on the properties).
     *
     * @param properties The properties of the strategies to run.
     * @throws IOException when a file cannot be parsed
     * @throws ClassNotFoundException when {@link Class#forName(java.lang.String)}
     * fails
     * @throws IllegalAccessException when {@link java.lang.reflect.Constructor#newInstance(java.lang.Object[])}
     * fails
     * @throws InstantiationException when {@link java.lang.reflect.Constructor#newInstance(java.lang.Object[])}
     * fails
     * @throws InvocationTargetException when {@link java.lang.reflect.Constructor#newInstance(java.lang.Object[])}
     * fails
     * @throws NoSuchMethodException when {@link Class#getConstructor(java.lang.Class[])}
     * fails
     */
    public static void run(final Properties properties)
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        // get splits
        File splitsFolder = new File(properties.getProperty(SPLITS_FOLDER));
        String trainingSuffix = properties.getProperty(TRAINING_SUFFIX);
        String testSuffix = properties.getProperty(TEST_SUFFIX);
        Set<String> splits = new HashSet<String>();
        getAllSplits(splits, splitsFolder, trainingSuffix, testSuffix);
        // read more parameters
        File inputFolder = new File(properties.getProperty(RECOMMENDATION_FOLDER));
        String inputSuffix = properties.getProperty(RECOMMENDATION_SUFFIX);
        File rankingFolder = new File(properties.getProperty(OUTPUT_FOLDER));
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(StrategyRunner.OUTPUT_OVERWRITE, "true"));
        File groundtruthFolder = new File(properties.getProperty(GROUNDTRUTH_FOLDER));
        EvaluationStrategy.OUTPUT_FORMAT format = null;
        if (properties.getProperty(OUTPUT_FORMAT).equals(EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL.toString())) {
            format = EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL;
        } else {
            format = EvaluationStrategy.OUTPUT_FORMAT.SIMPLE;
        }
        String[] thresholds = properties.getProperty(RELEVANCE_THRESHOLDS).split(",");
        String[] strategyClassNames = properties.getProperty(STRATEGIES).split(",");
        // process info for each split
        for (String split : splits) {
            File trainingFile = new File(split + trainingSuffix);
            System.out.println("Parsing started: training file" + trainingFile);
            DataModelIF<Long, Long> trainingModel = new SimpleParser().parseData(trainingFile);
            System.out.println("Parsing finished: training file ");
            File testFile = new File(split + testSuffix);
            System.out.println("Parsing started: test file" + testFile);
            DataModelIF<Long, Long> testModel = new SimpleParser().parseData(testFile);
            System.out.println("Parsing finished: test file");
            Set<String> recommendationFiles = new HashSet<String>();
            getAllRecommendationFiles(recommendationFiles, inputFolder, new File(split).getName(), inputSuffix);
            for (String inputFile : recommendationFiles) {
                System.out.println("Recommendation file: " + inputFile);
                String inputFileName = new File(inputFile).getName();
                // read recommendations: user \t item \t score
                final Map<Long, List<Pair<Long, Double>>> mapUserRecommendations = new HashMap<Long, List<Pair<Long, Double>>>();
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
                try {
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        StrategyIO.readLine(line, mapUserRecommendations);
                    }
                } finally {
                    in.close();
                }
                // generate output for each strategy
                // improvement: call to instantiateStrategies instead of having duplicated code. Current problem: how to obtain nice suffix text and threshold information in a generic way
                for (String strategyClassName : strategyClassNames) {
                    Class<?> strategyClass = Class.forName(strategyClassName);
                    for (String threshold : thresholds) {
                        System.out.println("Generating " + strategyClassName + " with threshold " + threshold);
                        // get strategy and generate output
                        if (strategyClassName.contains("RelPlusN")) {
                            String[] numbers = properties.getProperty(RELPLUSN_N).split(",");
                            String[] seeds = properties.getProperty(RELPLUSN_SEED).split(",");
                            for (String number : numbers) {
                                for (String seed : seeds) {
                                    EvaluationStrategy<Long, Long> strategy = new RelPlusN(trainingModel, testModel, Integer.parseInt(number), Double.parseDouble(threshold), Long.parseLong(seed));
                                    generateOutput(testModel, mapUserRecommendations, strategy, format, rankingFolder, groundtruthFolder,
                                            inputFileName, strategyClass.getSimpleName(), threshold, "__" + number + "__" + seed, overwrite);
                                }
                            }
                        } else {
                            Object strategyObj = strategyClass.getConstructor(DataModelIF.class, DataModelIF.class, double.class).newInstance(trainingModel, testModel, Double.parseDouble(threshold));
                            if (strategyObj instanceof EvaluationStrategy) {
                                @SuppressWarnings("unchecked")
                                EvaluationStrategy<Long, Long> strategy = (EvaluationStrategy<Long, Long>) strategyObj;
                                generateOutput(testModel, mapUserRecommendations, strategy, format, rankingFolder, groundtruthFolder,
                                        inputFileName, strategyClass.getSimpleName(), threshold, "", overwrite);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * Instantiates several strategies, according to the provided properties
     * mapping.
     *
     * @param properties the properties to be used.
     * @param trainingModel datamodel containing the training interactions to be
     * considered when generating the strategies.
     * @param testModel datamodel containing the interactions in the test split
     * to be considered when generating the strategies.
     * @return an array of strategies, generated according to the provided
     * properties.
     * @throws ClassNotFoundException when {@link Class#forName(java.lang.String)}
     * fails
     * @throws IllegalAccessException when {@link java.lang.reflect.Constructor#newInstance(java.lang.Object[])}
     * fails
     * @throws InstantiationException when {@link java.lang.reflect.Constructor#newInstance(java.lang.Object[])}
     * fails
     * @throws InvocationTargetException when {@link java.lang.reflect.Constructor#newInstance(java.lang.Object[])}
     * fails
     * @throws NoSuchMethodException when {@link Class#getConstructor(java.lang.Class[])}
     * fails
     */
    @SuppressWarnings("unchecked")
    public static EvaluationStrategy<Long, Long>[] instantiateStrategies(final Properties properties, final DataModelIF<Long, Long> trainingModel, final DataModelIF<Long, Long> testModel)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        List<EvaluationStrategy<Long, Long>> stratList = new ArrayList<EvaluationStrategy<Long, Long>>();

        String[] thresholds = properties.getProperty(RELEVANCE_THRESHOLDS).split(",");
        String[] strategyClassNames = properties.getProperty(STRATEGIES).split(",");
        for (String strategyClassName : strategyClassNames) {
            Class<?> strategyClass = Class.forName(strategyClassName);
            for (String threshold : thresholds) {
                // get strategy and generate output
                if (strategyClassName.contains("RelPlusN")) {
                    String[] numbers = properties.getProperty(RELPLUSN_N).split(",");
                    String[] seeds = properties.getProperty(RELPLUSN_SEED).split(",");
                    for (String number : numbers) {
                        for (String seed : seeds) {
                            EvaluationStrategy<Long, Long> strategy = new RelPlusN(trainingModel, testModel, Integer.parseInt(number), Double.parseDouble(threshold), Long.parseLong(seed));
                            stratList.add(strategy);
                        }
                    }
                } else {
                    Object strategyObj = strategyClass.getConstructor(DataModelIF.class, DataModelIF.class, double.class).newInstance(trainingModel, testModel, Double.parseDouble(threshold));
                    if (strategyObj instanceof EvaluationStrategy) {
                        @SuppressWarnings("unchecked")
                        EvaluationStrategy<Long, Long> strategy = (EvaluationStrategy<Long, Long>) strategyObj;
                        stratList.add(strategy);
                    }
                }
            }
        }
        EvaluationStrategy<Long, Long>[] strats = stratList.toArray(new EvaluationStrategy[0]);
        return strats;
    }

    /**
     * Runs multiple strategies on some data and outputs the result into a file.
     *
     * @param testModel The test datamodel.
     * @param mapUserRecommendations A map with the recommendations for the
     * users.
     * @param strategy The strategy to use.
     * @param format The format of the printer
     * @param rankingFolder Where to write output.
     * @param groundtruthFolder Where to read test set.
     * @param inputFileName The file names to read.
     * @param strategyClassSimpleName The class name of the strategy.
     * @param threshold The relevance threshold.
     * @param suffix The file suffix.
     * @param overwrite Whether or not to overwrite the results file.
     * @throws FileNotFoundException see
     * {@link StrategyRunner#generateOutput(net.recommenders.rival.core.DataModelIF, java.util.Map, net.recommenders.rival.evaluation.strategy.EvaluationStrategy,
     * net.recommenders.rival.evaluation.strategy.EvaluationStrategy.OUTPUT_FORMAT, java.io.File, java.io.File, java.lang.Boolean)}
     * @throws UnsupportedEncodingException see
     * {@link StrategyRunner#generateOutput(net.recommenders.rival.core.DataModelIF, java.util.Map, net.recommenders.rival.evaluation.strategy.EvaluationStrategy,
     * net.recommenders.rival.evaluation.strategy.EvaluationStrategy.OUTPUT_FORMAT, java.io.File, java.io.File, java.lang.Boolean)}
     */
    public static void generateOutput(final DataModelIF<Long, Long> testModel, final Map<Long, List<Pair<Long, Double>>> mapUserRecommendations,
            final EvaluationStrategy<Long, Long> strategy, final EvaluationStrategy.OUTPUT_FORMAT format,
            final File rankingFolder, final File groundtruthFolder, final String inputFileName,
            final String strategyClassSimpleName, final String threshold, final String suffix, final Boolean overwrite)
            throws FileNotFoundException, UnsupportedEncodingException {
        File outRanking = new File(rankingFolder, "out" + "__" + inputFileName + "__" + strategyClassSimpleName + "__" + threshold + suffix);
        File outGroundtruth = new File(groundtruthFolder, "gr" + "__" + inputFileName + "__" + strategyClassSimpleName + "__" + threshold + suffix);
        StrategyRunner.generateOutput(testModel, mapUserRecommendations, strategy, format, outRanking, outGroundtruth, overwrite);
    }

    /**
     * Get all training/test splits.
     *
     * @param splits The splits.
     * @param path The path where the splits are.
     * @param trainingSuffix The suffix of the training files.
     * @param testSuffix The suffix of the test files.
     */
    public static void getAllSplits(final Set<String> splits, final File path, final String trainingSuffix, final String testSuffix) {
        if (path == null) {
            return;
        }
        File[] files = path.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                getAllSplits(splits, file, trainingSuffix, testSuffix);
            } else if (file.getName().endsWith(trainingSuffix)) {
                splits.add(file.getAbsolutePath().replaceAll(trainingSuffix + "$", ""));
            } else if (file.getName().endsWith(testSuffix)) {
                splits.add(file.getAbsolutePath().replaceAll(testSuffix + "$", ""));
            }
        }
    }

    /**
     * Get all recommendation files.
     *
     * @param recommendationFiles The recommendation files
     * @param path The path of the recommendation files.
     * @param prefix The prefix of the recommendation files.
     * @param suffix The suffix of the recommendation files.
     */
    public static void getAllRecommendationFiles(final Set<String> recommendationFiles, final File path, final String prefix, final String suffix) {
        if (path == null) {
            return;
        }
        File[] files = path.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                getAllRecommendationFiles(recommendationFiles, file, prefix, suffix);
            } else if (file.getName().startsWith(prefix) && file.getName().endsWith(suffix)) {
                recommendationFiles.add(file.getAbsolutePath());
            }
        }
    }
}
