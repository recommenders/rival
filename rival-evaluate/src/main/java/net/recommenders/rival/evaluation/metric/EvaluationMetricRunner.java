package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.metric.error.AbstractErrorMetric;
import net.recommenders.rival.evaluation.metric.ranking.AbstractRankingMetric;
import net.recommenders.rival.evaluation.metric.ranking.NDCG;
import net.recommenders.rival.evaluation.parser.TrecEvalParser;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Runner for a single evaluation metric.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class EvaluationMetricRunner {

    /**
     * Variables that represent the name of several properties in the file.
     */
    public static final String PREDICTION_FILE = "evaluation.pred.file";
    public static final String PREDICTION_FILE_FORMAT = "evaluation.pred.format";
    public static final String TEST_FILE = "evaluation.test.file";
    public static final String OUTPUT_OVERWRITE = "evaluation.output.overwrite";
    public static final String OUTPUT_APPEND = "evaluation.output.append";
    public static final String OUTPUT_FILE = "evaluation.output.file";
    public static final String METRIC = "evaluation.class";
    public static final String RELEVANCE_THRESHOLD = "evaluation.relevance.threshold";
    public static final String RANKING_CUTOFFS = "evaluation.ranking.cutoffs";
    public static final String NDCG_TYPE = "evaluation.ndcg.type";
    public static final String ERROR_STRATEGY = "evaluation.error.strategy";
    public static final String METRIC_PER_USER = "evaluation.peruser";

    /**
     * Main method for running a single evaluation metric.
     *
     * @param args Arguments.
     * @throws Exception If file not found.
     */
    public static void main(String[] args) throws Exception {
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
     * Run a single evaluation metric.
     *
     * @param properties The properties of the strategy.
     * @throws IOException if file not found.
     * @throws ClassNotFoundException when
     * @throws IllegalAccessException when
     * @throws IllegalArgumentException when
     * @throws InstantiationException when
     * @throws InvocationTargetException when
     * @throws NoSuchMethodException when
     * @throws SecurityException when
     */
    @SuppressWarnings("unchecked")
    public static void run(Properties properties) throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        System.out.println("Parsing started: recommendation file");
        File recommendationFile = new File(properties.getProperty(PREDICTION_FILE));
        DataModel<Long, Long> predictions;// = null;
        EvaluationStrategy.OUTPUT_FORMAT recFormat = properties.getProperty(PREDICTION_FILE_FORMAT).equals(EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL.toString()) ? EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL : EvaluationStrategy.OUTPUT_FORMAT.SIMPLE;
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
        DataModel<Long, Long> testModel = new SimpleParser().parseData(testFile);
        System.out.println("Parsing finished: test file");
        // read other parameters
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(OUTPUT_OVERWRITE, "false"));
        Boolean doAppend = Boolean.parseBoolean(properties.getProperty(OUTPUT_APPEND, "true"));
        Boolean perUser = Boolean.parseBoolean(properties.getProperty(METRIC_PER_USER, "false"));
        File resultsFile = new File(properties.getProperty(OUTPUT_FILE));
        Double threshold = Double.parseDouble(properties.getProperty(RELEVANCE_THRESHOLD));
        int[] rankingCutoffs;// = null;
        // get metric
        String metricClassName = properties.getProperty(METRIC);
        Class<?> metricClass = Class.forName(metricClassName);
        EvaluationMetric<Long> metric;// = null;
        if (metricClassName.contains(".ranking.")) {
            String[] cutoffs = properties.getProperty(RANKING_CUTOFFS).split(",");
            rankingCutoffs = new int[cutoffs.length];
            for (int i = 0; i < rankingCutoffs.length; i++) {
                rankingCutoffs[i] = Integer.parseInt(cutoffs[i]);
            }
            if (metricClassName.endsWith("NDCG")) {
                String ndcgType = properties.getProperty(NDCG_TYPE, "exp");
                NDCG.TYPE nt = ndcgType.equalsIgnoreCase(NDCG.TYPE.EXP.toString()) ? NDCG.TYPE.EXP : NDCG.TYPE.LIN;
                metric = (EvaluationMetric<Long>) metricClass.getConstructor(DataModel.class, DataModel.class, double.class, int[].class, NDCG.TYPE.class).newInstance(predictions, testModel, threshold.doubleValue(), rankingCutoffs, nt);
            } else {
                metric = (EvaluationMetric<Long>) metricClass.getConstructor(DataModel.class, DataModel.class, double.class, int[].class).newInstance(predictions, testModel, threshold.doubleValue(), rankingCutoffs);
            }
        } else {
            rankingCutoffs = new int[0];
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
                return;
            }
            metric = (EvaluationMetric<Long>) metricClass.getConstructor(DataModel.class, DataModel.class, AbstractErrorMetric.ErrorStrategy.class).newInstance(predictions, testModel, es);
        }
        // generate output
        generateOutput(testModel, rankingCutoffs, metric, metricClass.getSimpleName(), perUser, resultsFile, overwrite, doAppend);
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
     * @throws FileNotFoundException If file not found.
     */
    @SuppressWarnings("unchecked")
    public static <U, I> void generateOutput(final DataModel<U, I> testModel, final int[] rankingCutoffs, EvaluationMetric<U> metric, String metricName, Boolean perUser, File resultsFile, Boolean overwrite, Boolean append) throws FileNotFoundException {
        PrintStream out = null;
        if (overwrite && append) {
            System.out.println("Incompatible arguments: overwrite && append!!!");
            return;
        }
        if (resultsFile.exists() && !overwrite && !append) {
            System.out.println("Ignoring " + resultsFile);
            return;
        } else {
            out = new PrintStream(new FileOutputStream(resultsFile, append));
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
        if (out != null) {
            out.close();
        }
    }
}
