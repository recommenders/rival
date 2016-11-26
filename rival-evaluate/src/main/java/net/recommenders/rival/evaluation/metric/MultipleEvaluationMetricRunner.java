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
package net.recommenders.rival.evaluation.metric;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.parser.TrecEvalParser;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;

/**
 * Runner for multiple evaluation metrics.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public final class MultipleEvaluationMetricRunner {

    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String PREDICTION_FOLDER = "evaluation.pred.folder";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String PREDICTION_PREFIX = "evaluation.pred.prefix";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String PREDICTION_FILE_FORMAT = "evaluation.pred.format";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String TEST_FILE = "evaluation.test.file";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String OUTPUT_OVERWRITE = "evaluation.output.overwrite";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String OUTPUT_APPEND = "evaluation.output.append";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String OUTPUT_FOLDER = "evaluation.output.folder";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String METRICS = "evaluation.classes";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String RELEVANCE_THRESHOLD = "evaluation.relevance.threshold";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String RANKING_CUTOFFS = "evaluation.ranking.cutoffs";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String NDCG_TYPE = "evaluation.ndcg.type";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String ERROR_STRATEGY = "evaluation.error.strategy";
    /**
     * Variable that represents the name of a property in the file.
     */
    public static final String METRIC_PER_USER = "evaluation.peruser";

    /**
     * Utility classes should not have a public or default constructor.
     */
    private MultipleEvaluationMetricRunner() {
    }

    /**
     * Main method for running multiple evaluation metrics.
     *
     * @param args Arguments.
     * @throws Exception If file not found.
     */
    public static void main(final String[] args) throws Exception {
        String propertyFile = System.getProperty("propertyFile");

        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        run(properties);
    }

    /**
     * Runs multiple evaluation metrics.
     *
     * @param properties The properties of the strategy.
     * @throws IOException if test file or prediction file are not found or
     * output cannot be generated (see {@link net.recommenders.rival.core.Parser#parseData(java.io.File)}
     * and {@link EvaluationMetricRunner#generateOutput(net.recommenders.rival.core.DataModelIF, int[],
     * net.recommenders.rival.evaluation.metric.EvaluationMetric, java.lang.String, java.lang.Boolean, java.io.File, java.lang.Boolean, java.lang.Boolean)}).
     * @throws ClassNotFoundException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws IllegalAccessException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InstantiationException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InvocationTargetException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws NoSuchMethodException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     */
    @SuppressWarnings("unchecked")
    public static void run(final Properties properties)
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        EvaluationStrategy.OUTPUT_FORMAT recFormat;
        if (properties.getProperty(PREDICTION_FILE_FORMAT).equals(EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL.toString())) {
            recFormat = EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL;
        } else {
            recFormat = EvaluationStrategy.OUTPUT_FORMAT.SIMPLE;
        }

        System.out.println("Parsing started: test file");
        File testFile = new File(properties.getProperty(TEST_FILE));
        DataModelIF<Long, Long> testModel = new SimpleParser().parseData(testFile);
        System.out.println("Parsing finished: test file");

        File predictionsFolder = new File(properties.getProperty(PREDICTION_FOLDER));
        String predictionsPrefix = properties.getProperty(PREDICTION_PREFIX);
        Set<String> predictionFiles = new HashSet<>();
        getAllPredictionFiles(predictionFiles, predictionsFolder, predictionsPrefix);

        // read other parameters
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(OUTPUT_OVERWRITE, "false"));
        Boolean doAppend = Boolean.parseBoolean(properties.getProperty(OUTPUT_APPEND, "true"));
        Boolean perUser = Boolean.parseBoolean(properties.getProperty(METRIC_PER_USER, "false"));
        int[] rankingCutoffs = EvaluationMetricRunner.getRankingCutoffs(properties);
        // process info for each result file
        File resultsFolder = new File(properties.getProperty(OUTPUT_FOLDER));
        for (String file : predictionFiles) {
            File predictionFile = new File(predictionsPrefix + file);
            System.out.println("Parsing started: recommendation file");
            DataModelIF<Long, Long> predictions;
            switch (recFormat) {
                case SIMPLE:
                    predictions = new SimpleParser().parseData(predictionFile);
                    break;
                case TRECEVAL:
                    predictions = new TrecEvalParser().parseData(predictionFile);
                    break;
                default:
                    throw new AssertionError();
            }
            System.out.println("Parsing finished: recommendation file");
            File resultsFile = new File(resultsFolder, "eval" + "__" + predictionFile.getName());

            // get metrics
            for (EvaluationMetric<Long> metric : instantiateEvaluationMetrics(properties, predictions, testModel)) {
                // generate output
                EvaluationMetricRunner.generateOutput(testModel, rankingCutoffs, metric, metric.getClass().getSimpleName(), perUser, resultsFile, overwrite, doAppend);
            }
        }
    }

    /**
     *
     * Instantiates multiple evaluation metrics.
     *
     * @param properties the properties to be used.
     * @param predictions datamodel containing the predictions of a recommender.
     * @param testModel datamodel containing the test split.
     * @return a set of evaluation metrics.
     * @throws ClassNotFoundException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws IllegalAccessException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InstantiationException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InvocationTargetException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws NoSuchMethodException see
     * {@link EvaluationMetricRunner#instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     */
    public static EvaluationMetric<Long>[] instantiateEvaluationMetrics(final Properties properties, final DataModelIF<Long, Long> predictions, final DataModelIF<Long, Long> testModel)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        List<EvaluationMetric<Long>> metricList = new ArrayList<>();
        String[] metricClassNames = properties.getProperty(METRICS).split(",");
        for (String metricClassName : metricClassNames) {
            // get metric
            properties.put(EvaluationMetricRunner.METRIC, metricClassName);
            EvaluationMetric<Long> metric = EvaluationMetricRunner.instantiateEvaluationMetric(properties, predictions, testModel);
            metricList.add(metric);
            properties.remove(EvaluationMetricRunner.METRIC);
        }

    @SuppressWarnings("unchecked")
        EvaluationMetric<Long>[] metrics = metricList.toArray(new EvaluationMetric[0]);
        return metrics;
    }

    /**
     * Gets all prediction files.
     *
     * @param predictionFiles The prediction files.
     * @param path The path where the splits are.
     * @param predictionPrefix The prefix of the prediction files.
     */
    public static void getAllPredictionFiles(final Set<String> predictionFiles, final File path, final String predictionPrefix) {
        if (path == null) {
            return;
        }
        File[] files = path.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                getAllPredictionFiles(predictionFiles, file, predictionPrefix);
            } else if (file.getName().startsWith(predictionPrefix)) {
                predictionFiles.add(file.getAbsolutePath().replaceAll(predictionPrefix, ""));
            }
        }
    }
}
