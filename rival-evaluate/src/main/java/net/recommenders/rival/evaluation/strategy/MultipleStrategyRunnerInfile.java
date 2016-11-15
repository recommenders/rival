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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.SimpleParser;

/**
 * Runner of multiple evaluation strategies using StrategyRunnerInfile.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public final class MultipleStrategyRunnerInfile {

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
    private MultipleStrategyRunnerInfile() {
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
     * Method that runs several strategies (depending on the properties) where
     * the information is not completely stored in memory.
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
            for (String inputFileString : recommendationFiles) {
                System.out.println("Recommendation file: " + inputFileString);
                File inputFile = new File(inputFileString);
                String inputFileName = inputFile.getName();
                // generate output for each strategy
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
                                    generateOutput(testModel, inputFile, strategy, format, rankingFolder, groundtruthFolder,
                                            inputFileName, strategyClass.getSimpleName(), threshold, "__" + number + "__" + seed, overwrite);
                                }
                            }
                        } else {
                            Object strategyObj = strategyClass.getConstructor(DataModelIF.class, DataModelIF.class, double.class).newInstance(trainingModel, testModel, Double.parseDouble(threshold));
                            if (strategyObj instanceof EvaluationStrategy) {
                                @SuppressWarnings("unchecked")
                                EvaluationStrategy<Long, Long> strategy = (EvaluationStrategy<Long, Long>) strategyObj;
                                generateOutput(testModel, inputFile, strategy, format, rankingFolder, groundtruthFolder, inputFileName, strategyClass.getSimpleName(), threshold, "", overwrite);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Runs multiple strategies on some data and outputs the result into a file.
     *
     * @param testModel The test datamodel.
     * @param userRecommendationFile The file that contains the recommendations
     * for users.
     * @param strategy The strategy to use.
     * @param format The format of the printer
     * @param rankingFolder Where to write output.
     * @param groundtruthFolder Where to read test set.
     * @param inputFileName The file names to read.
     * @param strategyClassSimpleName The class name of the strategy.
     * @param threshold The relevance threshold.
     * @param suffix The file suffix.
     * @param overwrite Whether or not to overwrite the results file.
     * @throws IOException when a file cannot be parsed.
     */
    public static void generateOutput(final DataModelIF<Long, Long> testModel, final File userRecommendationFile,
            final EvaluationStrategy<Long, Long> strategy, final EvaluationStrategy.OUTPUT_FORMAT format,
            final File rankingFolder, final File groundtruthFolder, final String inputFileName,
            final String strategyClassSimpleName, final String threshold, final String suffix, final Boolean overwrite)
            throws IOException {
        File outRanking = new File(rankingFolder, "out" + "__" + inputFileName + "__" + strategyClassSimpleName + "__" + threshold + suffix);
        File outGroundtruth = new File(groundtruthFolder, "gr" + "__" + inputFileName + "__" + strategyClassSimpleName + "__" + threshold + suffix);
        StrategyRunnerInfile.generateOutput(testModel, userRecommendationFile, strategy, format, outRanking, outGroundtruth, overwrite);
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
     * @param recommendationFiles The recommendation files (what is this?)
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
