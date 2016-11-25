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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.metric.error.AbstractErrorMetric;
import net.recommenders.rival.evaluation.metric.ranking.AbstractRankingMetric;
import net.recommenders.rival.evaluation.metric.ranking.NDCG;
import net.recommenders.rival.evaluation.parser.TrecEvalParser;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;

/**
 * Runner for a single evaluation metric.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public final class EvaluationMetricRunner {

    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String PREDICTION_FILE = "evaluation.pred.file";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String PREDICTION_FILE_FORMAT = "evaluation.pred.format";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String TEST_FILE = "evaluation.test.file";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String OUTPUT_OVERWRITE = "evaluation.output.overwrite";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String OUTPUT_APPEND = "evaluation.output.append";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String OUTPUT_FILE = "evaluation.output.file";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String METRIC = "evaluation.class";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String RELEVANCE_THRESHOLD = "evaluation.relevance.threshold";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String RANKING_CUTOFFS = "evaluation.ranking.cutoffs";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String NDCG_TYPE = "evaluation.ndcg.type";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String ERROR_STRATEGY = "evaluation.error.strategy";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String METRIC_PER_USER = "evaluation.peruser";

    /**
     * Utility classes should not have a public or default constructor.
     */
    private EvaluationMetricRunner() {
    }

    /**
     * Main method for running a single evaluation metric.
     *
     * @param args the arguments.
     * @throws Exception see {@link #run(java.util.Properties)}
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
     * Runs a single evaluation metric.
     *
     * @param properties The properties of the strategy.
     * @throws IOException if recommendation file is not found or output cannot
     * be written (see {@link #generateOutput(net.recommenders.rival.core.DataModelIF, int[],
     * net.recommenders.rival.evaluation.metric.EvaluationMetric, java.lang.String, java.lang.Boolean, java.io.File, java.lang.Boolean, java.lang.Boolean)})
     * @throws ClassNotFoundException see {@link #instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws IllegalAccessException see {@link #instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InstantiationException see {@link #instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InvocationTargetException see {@link #instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws NoSuchMethodException see {@link #instantiateEvaluationMetric(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     */
    @SuppressWarnings("unchecked")
    public static void run(final Properties properties)
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        System.out.println("Parsing started: recommendation file");
        File recommendationFile = new File(properties.getProperty(PREDICTION_FILE));
        DataModelIF<Long, Long> predictions;
        EvaluationStrategy.OUTPUT_FORMAT recFormat;
        if (properties.getProperty(PREDICTION_FILE_FORMAT).equals(EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL.toString())) {
            recFormat = EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL;
        } else {
            recFormat = EvaluationStrategy.OUTPUT_FORMAT.SIMPLE;
        }
        switch (recFormat) {
            case SIMPLE:
                predictions = new SimpleParser().parseData(recommendationFile);
                break;
            case TRECEVAL:
                predictions = new TrecEvalParser().parseData(recommendationFile);
                break;
            default:
                throw new AssertionError();
        }
        System.out.println("Parsing finished: recommendation file");
        System.out.println("Parsing started: test file");
        File testFile = new File(properties.getProperty(TEST_FILE));
        DataModelIF<Long, Long> testModel = new SimpleParser().parseData(testFile);
        System.out.println("Parsing finished: test file");
        // read other parameters
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(OUTPUT_OVERWRITE, "false"));
        Boolean doAppend = Boolean.parseBoolean(properties.getProperty(OUTPUT_APPEND, "true"));
        Boolean perUser = Boolean.parseBoolean(properties.getProperty(METRIC_PER_USER, "false"));
        File resultsFile = new File(properties.getProperty(OUTPUT_FILE));
        // get metric
        EvaluationMetric<Long> metric = instantiateEvaluationMetric(properties, predictions, testModel);
        // get ranking cutoffs
        int[] rankingCutoffs = getRankingCutoffs(properties);
        // generate output
        generateOutput(testModel, rankingCutoffs, metric, metric.getClass().getSimpleName(), perUser, resultsFile, overwrite, doAppend);
    }

    /**
     *
     * Gets the ranking cutoffs requested in a properties mapping.
     *
     * @param properties the properties mapping to be parsed.
     * @return an array with the ranking cutoffs (if available).
     */
    public static int[] getRankingCutoffs(final Properties properties) {
        int[] rankingCutoffs = new int[0];
        String metricClassName = properties.getProperty(METRIC);
        if (metricClassName.contains(".ranking.")) {
            String[] cutoffs = properties.getProperty(RANKING_CUTOFFS).split(",");
            rankingCutoffs = new int[cutoffs.length];
            for (int i = 0; i < rankingCutoffs.length; i++) {
                rankingCutoffs[i] = Integer.parseInt(cutoffs[i]);
            }
        }
        return rankingCutoffs;
    }

    /**
     *
     * Instantiates a single evaluation metric.
     *
     * @param properties the properties to be used.
     * @param predictions datamodel containing the predictions of a recommender.
     * @param testModel a datamodel containing the test split.
     * @return a single evaluation metric according to the properties provided.
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
    public static EvaluationMetric<Long> instantiateEvaluationMetric(final Properties properties, final DataModelIF<Long, Long> predictions, final DataModelIF<Long, Long> testModel)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Double threshold = Double.parseDouble(properties.getProperty(RELEVANCE_THRESHOLD));
        int[] rankingCutoffs = getRankingCutoffs(properties);
        String metricClassName = properties.getProperty(METRIC);
        Class<?> metricClass = Class.forName(metricClassName);
        EvaluationMetric<Long> metric;
        if (metricClassName.contains(".ranking.")) {
            if (metricClassName.endsWith("NDCG")) {
                String ndcgType = properties.getProperty(NDCG_TYPE, "exp");
                NDCG.TYPE nt;
                if (ndcgType.equalsIgnoreCase(NDCG.TYPE.EXP.toString())) {
                    nt = NDCG.TYPE.EXP;
                } else {
                    nt = NDCG.TYPE.LIN;
                }
                metric = (EvaluationMetric<Long>) metricClass.getConstructor(DataModelIF.class, DataModelIF.class, double.class, int[].class, NDCG.TYPE.class).
                        newInstance(predictions, testModel, threshold, rankingCutoffs, nt);
            } else {
                metric = (EvaluationMetric<Long>) metricClass.getConstructor(DataModelIF.class, DataModelIF.class, double.class, int[].class).
                        newInstance(predictions, testModel, threshold, rankingCutoffs);
            }
        } else {
            String strategy = properties.getProperty(ERROR_STRATEGY);
            AbstractErrorMetric.ErrorStrategy es = null;
            for (AbstractErrorMetric.ErrorStrategy s : AbstractErrorMetric.ErrorStrategy.values()) {
                if (strategy.equalsIgnoreCase(s.toString())) {
                    es = s;
                    break;
                }
            }
            if (es == null) {
                System.out.println("Invalid error strategy: " + strategy);
                return null;
            }
            metric = (EvaluationMetric<Long>) metricClass.getConstructor(DataModelIF.class, DataModelIF.class, AbstractErrorMetric.ErrorStrategy.class).newInstance(predictions, testModel, es);
        }
        return metric;
    }

    /**
     * Generates the output of the evaluation.
     *
     * @param testModel The test model.
     * @param rankingCutoffs The ranking cutoffs for the ranking metrics.
     * @param metric The metric to be executed.
     * @param metricName The name to be printed in the file for this metric.
     * @param perUser Whether or not to print results per user.
     * @param resultsFile The results file.
     * @param overwrite Whether or not to overwrite results file.
     * @param append Whether or not to append results in an existing file.
     * @throws FileNotFoundException If file not found or cannot be created.
     * @throws UnsupportedEncodingException If default encoding (UTF-8) is not
     * available.
     *
     * @param <U> generic type for users.
     * @param <I> generic type for items.
     */
    @SuppressWarnings("unchecked")
    public static <U, I> void generateOutput(final DataModelIF<U, I> testModel, final int[] rankingCutoffs,
            final EvaluationMetric<U> metric, final String metricName,
            final Boolean perUser, final File resultsFile, final Boolean overwrite, final Boolean append) throws FileNotFoundException, UnsupportedEncodingException {
        PrintStream out;
        if (overwrite && append) {
            System.out.println("Incompatible arguments: overwrite && append!!!");
            return;
        }
        if (resultsFile.exists() && !overwrite && !append) {
            System.out.println("Ignoring " + resultsFile);
            return;
        } else {
            out = new PrintStream(new FileOutputStream(resultsFile, append), false, "UTF-8");
        }
        metric.compute();
        out.println(metricName + "\tall\t" + metric.getValue());
        if (metric instanceof AbstractRankingMetric) {
            AbstractRankingMetric<U, I> rankingMetric = (AbstractRankingMetric<U, I>) metric;
            for (int c : rankingCutoffs) {
                out.println(metricName + "@" + c + "\tall\t" + rankingMetric.getValueAt(c));
            }
        }
        if (perUser) {
            for (U user : testModel.getUsers()) {
                out.println(metricName + "\t" + user + "\t" + metric.getValue(user));
                if (metric instanceof AbstractRankingMetric) {
                    AbstractRankingMetric<U, I> rankingMetric = (AbstractRankingMetric<U, I>) metric;
                    for (int c : rankingCutoffs) {
                        out.println(metricName + "@" + c + "\t" + user + "\t" + rankingMetric.getValueAt(user, c));
                    }
                }
            }
        }
        out.close();
    }
}
