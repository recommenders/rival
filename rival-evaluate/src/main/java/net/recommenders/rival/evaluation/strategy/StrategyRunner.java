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
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.Pair;

/**
 * Runner for a single strategy.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public final class StrategyRunner {

    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String TRAINING_FILE = "split.training.file";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String TEST_FILE = "split.test.file";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String INPUT_FILE = "recommendation.file";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String OUTPUT_FORMAT = "output.format";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String OUTPUT_OVERWRITE = "output.overwrite";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String OUTPUT_FILE = "output.file.ranking";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String GROUNDTRUTH_FILE = "output.file.groundtruth";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String STRATEGY = "strategy.class";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String RELEVANCE_THRESHOLD = "strategy.relevance.threshold";
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
    private StrategyRunner() {
    }

    /**
     * Main method for running a single evaluation strategy.
     *
     * @param args Arguments.
     * @throws Exception If file not found.
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

        run(properties);
    }

    /**
     * Runs a single evaluation strategy.
     *
     * @param properties The properties of the strategy.
     * @throws IOException when a file cannot be parsed
     * @throws ClassNotFoundException see {@link #instantiateStrategy(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws IllegalAccessException see {@link #instantiateStrategy(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InstantiationException see {@link #instantiateStrategy(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InvocationTargetException see {@link #instantiateStrategy(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws NoSuchMethodException see {@link #instantiateStrategy(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     */
    public static void run(final Properties properties)
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        // read splits
        System.out.println("Parsing started: training file");
        File trainingFile = new File(properties.getProperty(TRAINING_FILE));
        DataModelIF<Long, Long> trainingModel = new SimpleParser().parseData(trainingFile);
        System.out.println("Parsing finished: training file");
        System.out.println("Parsing started: test file");
        File testFile = new File(properties.getProperty(TEST_FILE));
        DataModelIF<Long, Long> testModel = new SimpleParser().parseData(testFile);
        System.out.println("Parsing finished: test file");
        // read other parameters
        File inputFile = new File(properties.getProperty(INPUT_FILE));
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(OUTPUT_OVERWRITE, "false"));
        File rankingFile = new File(properties.getProperty(OUTPUT_FILE));
        File groundtruthFile = new File(properties.getProperty(GROUNDTRUTH_FILE));
        EvaluationStrategy.OUTPUT_FORMAT format = null;
        if (properties.getProperty(OUTPUT_FORMAT).equals(EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL.toString())) {
            format = EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL;
        } else {
            format = EvaluationStrategy.OUTPUT_FORMAT.SIMPLE;
        }

        // get strategy
        EvaluationStrategy<Long, Long> strategy = instantiateStrategy(properties, trainingModel, testModel);

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
        // generate output
        generateOutput(testModel, mapUserRecommendations, strategy, format, rankingFile, groundtruthFile, overwrite);
    }

    /**
     * Instantiates an strategy, according to the provided properties mapping.
     *
     * @param properties the properties to be used.
     * @param trainingModel datamodel containing the training interactions to be
     * considered when generating the strategy.
     * @param testModel datamodel containing the interactions in the test split
     * to be considered when generating the strategy.
     * @return the strategy generated according to the provided properties
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
    public static EvaluationStrategy<Long, Long> instantiateStrategy(final Properties properties, final DataModelIF<Long, Long> trainingModel, final DataModelIF<Long, Long> testModel)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Double threshold = Double.parseDouble(properties.getProperty(RELEVANCE_THRESHOLD));
        String strategyClassName = properties.getProperty(STRATEGY);
        Class<?> strategyClass = Class.forName(strategyClassName);
        // get strategy
        EvaluationStrategy<Long, Long> strategy = null;
        if (strategyClassName.contains("RelPlusN")) {
            Integer number = Integer.parseInt(properties.getProperty(RELPLUSN_N));
            Long seed = Long.parseLong(properties.getProperty(RELPLUSN_SEED));
            strategy = new RelPlusN(trainingModel, testModel, number, threshold, seed);
        } else {
            Object strategyObj = strategyClass.getConstructor(DataModelIF.class, DataModelIF.class, double.class).newInstance(trainingModel, testModel, threshold);
            if (strategyObj instanceof EvaluationStrategy) {
                @SuppressWarnings("unchecked")
                EvaluationStrategy<Long, Long> strategyTemp = (EvaluationStrategy<Long, Long>) strategyObj;
                strategy = strategyTemp;
            }
        }
        return strategy;
    }

    /**
     * Generates the output of the evaluation.
     *
     * @param testModel The test model.
     * @param mapUserRecommendations The recommendations for the users.
     * @param strategy The strategy.
     * @param format The printer format.
     * @param rankingFile The ranking file.
     * @param groundtruthFile The ground truth.
     * @param overwrite Whether or not to overwrite results file.
     * @throws FileNotFoundException If file not found.
     * @throws UnsupportedEncodingException If the default encoding (UTF-8) is
     * not supported.
     */
    public static void generateOutput(final DataModelIF<Long, Long> testModel, final Map<Long, List<Pair<Long, Double>>> mapUserRecommendations,
            final EvaluationStrategy<Long, Long> strategy, final EvaluationStrategy.OUTPUT_FORMAT format,
            final File rankingFile, final File groundtruthFile, final Boolean overwrite)
            throws FileNotFoundException, UnsupportedEncodingException {
        PrintStream outRanking = null;
        if (rankingFile.exists() && !overwrite) {
            System.out.println("Ignoring " + rankingFile);
        } else {
            outRanking = new PrintStream(rankingFile, "UTF-8");
        }
        try {
            PrintStream outGroundtruth = null;
            if (groundtruthFile.exists() && !overwrite) {
                System.out.println("Ignoring " + groundtruthFile);
            } else {
                outGroundtruth = new PrintStream(groundtruthFile, "UTF-8");
            }
            try {
                for (Long user : testModel.getUsers()) {
                    if (outRanking != null) {
                        final List<Pair<Long, Double>> allScoredItems = mapUserRecommendations.get(user);
                        if (allScoredItems == null) {
                            continue;
                        }
                        final Set<Long> items = strategy.getCandidateItemsToRank(user);
                        final List<Pair<Long, Double>> scoredItems = new ArrayList<Pair<Long, Double>>();
                        for (Pair<Long, Double> scoredItem : allScoredItems) {
                            if (items.contains(scoredItem.getFirst())) {
                                scoredItems.add(scoredItem);
                            }
                        }
                        strategy.printRanking(user, scoredItems, outRanking, format);
                    }
                    if (outGroundtruth != null) {
                        strategy.printGroundtruth(user, outGroundtruth, format);
                    }
                }
            } finally {
                if (outGroundtruth != null) {
                    outGroundtruth.close();
                }
            }
        } finally {
            if (outRanking != null) {
                outRanking.close();
            }
        }
    }
}
