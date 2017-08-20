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
package net.recommenders.rival.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.Parser;
import net.recommenders.rival.core.TemporalDataModelIF;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;
import net.recommenders.rival.evaluation.metric.MultipleEvaluationMetricRunner;
import net.recommenders.rival.evaluation.metric.ranking.AbstractRankingMetric;
import net.recommenders.rival.evaluation.statistics.StatisticsRunner;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;
import net.recommenders.rival.evaluation.strategy.MultipleStrategyRunner;
import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.MultipleRecommendationRunner;
import net.recommenders.rival.split.parser.ParserRunner;
import net.recommenders.rival.split.splitter.Splitter;
import net.recommenders.rival.split.splitter.SplitterRunner;

/**
 * Class that demonstrates the complete pipeline (at the moment) in RiVal,
 * starting from a file read from disk and without printing anything else to
 * disk (the whole process is hence done in memory).
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public final class CompletePipelineInMemory {

    /**
     * Utility classes should not have a public or default constructor.
     */
    private CompletePipelineInMemory() {
    }

    /**
     * Fills a property mapping with default values.
     *
     * @param props mapping where the default properties will be set.
     */
    private static void fillDefaultProperties(final Properties props) {
        System.out.println("Setting default properties...");
        // parser
        props.put(ParserRunner.DATASET_FILE, "./data/ml-100k/ml-100k/u.data");
        props.put(ParserRunner.DATASET_PARSER, "net.recommenders.rival.split.parser.MovielensParser");
        // splits
        props.put(SplitterRunner.DATASET_SPLITTER, "net.recommenders.rival.split.splitter.RandomSplitter");
        props.put(SplitterRunner.SPLIT_CV_NFOLDS, "");
        props.put(SplitterRunner.SPLIT_PERITEMS, "false");
        props.put(SplitterRunner.SPLIT_PERUSER, "false");
        props.put(SplitterRunner.SPLIT_RANDOM_PERCENTAGE, "0.8");
        props.put(SplitterRunner.SPLIT_SEED, "2015");
        // recommender
        props.put(MultipleRecommendationRunner.LENSKIT_ITEMBASED_RECS, "org.grouplens.lenskit.knn.item.ItemItemScorer");
        props.put(MultipleRecommendationRunner.LENSKIT_SIMILARITIES,
                "org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity,org.grouplens.lenskit.vectors.similarity.PearsonCorrelation");
//        props.put(MultipleRecommendationRunner.LENSKIT_SVD_RECS, "org.grouplens.lenskit.mf.funksvd.FunkSVDItemScorer");
        props.put(MultipleRecommendationRunner.LENSKIT_SVD_RECS, "");
        props.put(MultipleRecommendationRunner.LENSKIT_USERBASED_RECS, "org.grouplens.lenskit.knn.user.UserUserItemScorer");
//        props.put(MultipleRecommendationRunner.LENSKIT_USERBASED_RECS, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_ITEMBASED_RECS, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_SIMILARITIES, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_SVD_FACTORIZER, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_SVD_RECS, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_USERBASED_RECS, "");
        props.put(MultipleRecommendationRunner.N, "-1,10,50");
        props.put(MultipleRecommendationRunner.SVD_ITER, "50");
        // strategy
        props.put(MultipleStrategyRunner.STRATEGIES,
                "net.recommenders.rival.evaluation.strategy.RelPlusN,net.recommenders.rival.evaluation.strategy.TestItems,"
                + "net.recommenders.rival.evaluation.strategy.AllItems,net.recommenders.rival.evaluation.strategy.TrainItems,"
                + "net.recommenders.rival.evaluation.strategy.UserTest");
        props.put(MultipleStrategyRunner.RELEVANCE_THRESHOLDS, "5");
        props.put(MultipleStrategyRunner.RELPLUSN_N, "100");
        props.put(MultipleStrategyRunner.RELPLUSN_SEED, "2015");
        // evaluation
        props.put(MultipleEvaluationMetricRunner.METRICS,
                "net.recommenders.rival.evaluation.metric.error.MAE,"
                + "net.recommenders.rival.evaluation.metric.error.RMSE,"
                + "net.recommenders.rival.evaluation.metric.ranking.MAP,"
                + "net.recommenders.rival.evaluation.metric.ranking.NDCG,"
                + "net.recommenders.rival.evaluation.metric.ranking.Precision,"
                + "net.recommenders.rival.evaluation.metric.ranking.Recall");
        props.put(MultipleEvaluationMetricRunner.RELEVANCE_THRESHOLD, "5");
        props.put(MultipleEvaluationMetricRunner.RANKING_CUTOFFS, "1,5,10,50");
        props.put(MultipleEvaluationMetricRunner.NDCG_TYPE, "exp");
        props.put(MultipleEvaluationMetricRunner.ERROR_STRATEGY, "NOT_CONSIDER_NAN");
        // statistics
        props.put(StatisticsRunner.ALPHA, "0.05");
        props.put(StatisticsRunner.AVOID_USERS, "all");
        props.put(StatisticsRunner.STATISTICS,
                "confidence_interval,"
                + "effect_size_d,"
                + "effect_size_dLS,"
                + "effect_size_pairedT,"
                + "standard_error,"
                + "statistical_significance_t,"
                + "statistical_significance_pairedT,"
                + "statistical_significance_wilcoxon");
        props.put(StatisticsRunner.INPUT_FORMAT, "default");
        //              we use simple names instead of files
        props.put(StatisticsRunner.BASELINE_FILE, "/..lenskit.ItemItemScorer.CosineVectorSimilarity.tsv.stats");
        //
        System.out.println("Properties: " + props);
    }

    /**
     *
     * Main method: it will use the property file passed as a System property
     * (pointed by 'propertyFile') or one created in memory with default values
     * (see {@link #fillDefaultProperties(Properties) fillDefaultProperties}).
     *
     * @param args not used.
     */
    public static void main(final String[] args) {
        String propertyFile = System.getProperty("propertyFile");

        final Properties properties = new Properties();
        try {
            if (propertyFile == null) {
                fillDefaultProperties(properties);
            } else {
                properties.load(new FileInputStream(propertyFile));
            }
        } catch (IOException ie) {
            fillDefaultProperties(properties);
            ie.printStackTrace();
        }

        runExampleInMemory(properties);
    }

    /**
     *
     * Runs each of the steps of the pipeline, only reading the dataset file
     * from disk, performing the rest of the tasks in memory. The final
     * evaluation results and statistical analysis will be printed through the
     * standard output.
     *
     * @param properties properties that will be used for this execution. It
     * should contain properties for all steps: parsing, splitting,
     * recommendation, strategies, evaluation, statistics.
     */
    public static void runExampleInMemory(final Properties properties) {
        try {
            TemporalDataModelIF<Long, Long>[] splits = prepareSplitsInMemory(properties);
            for (int i = 0; i < splits.length / 2; i++) {
                System.out.println(">>> Processing split " + i / 2);
                TemporalDataModelIF<Long, Long> training = splits[2 * i];
                TemporalDataModelIF<Long, Long> test = splits[2 * i + 1];
                Map<String, DataModelIF<Long, Long>> recModels = getRecommenderModels(properties, training, test);
                Map<String, Map<String, Map<String, Map<String, Double>>>> mapStrategyRecommenderMetricUserValue = new HashMap<>();
                for (Entry<String, DataModelIF<Long, Long>> e : recModels.entrySet()) {
                    String rec = e.getKey();
                    DataModelIF<Long, Long> recModel = e.getValue();
                    Map<String, DataModelIF<Long, Long>> evalModels = applyStrategiesToRecommender(properties, training, test, recModel);
                    for (Entry<String, DataModelIF<Long, Long>> e2 : evalModels.entrySet()) {
                        String strat = e2.getKey();
                        DataModelIF<Long, Long> evalModel = e2.getValue();
                        Map<String, Map<String, Double>> results = evaluateStrategy(properties, test, evalModel);
                        // assign these results to a rec+strategy, at the end, compute statistics for all recs and one strategy
                        Map<String, Map<String, Map<String, Double>>> stratResults = mapStrategyRecommenderMetricUserValue.get(strat);
                        if (stratResults == null) {
                            stratResults = new HashMap<>();
                            mapStrategyRecommenderMetricUserValue.put(strat, stratResults);
                        }
                        stratResults.put(rec, results);
                        // print results
                        for (Entry<String, Map<String, Double>> e3 : results.entrySet()) {
                            String metric = e3.getKey();
                            Map<String, Double> metricResults = e3.getValue();
                            System.out.println(rec + "\t" + strat + "\t" + metric + "\t" + metricResults.get("all"));
                            // remove this value to not be considered in subsequent statistical analysis
                            metricResults.remove("all");
                        }
                    }
                }
                // compute statistics for a given strategy
                for (Entry<String, Map<String, Map<String, Map<String, Double>>>> e : mapStrategyRecommenderMetricUserValue.entrySet()) {
                    String strat = e.getKey();
                    Map<String, Map<String, Map<String, Double>>> strategyResults = e.getValue();
                    System.out.println("----> Statistics for strategy " + strat);
                    computeStatistics(properties, strategyResults, System.out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Parses and splits a given dataset file as provided in the properties
     * mapping.
     *
     * @param properties properties to be used.
     * @return the splits generated according to the properties passed.
     * @throws IOException see {@link Parser#parseData(java.io.File)}
     * @throws ClassNotFoundException see
     * {@link ParserRunner#instantiateParser(java.util.Properties)}
     * @throws IllegalAccessException see
     * {@link ParserRunner#instantiateParser(java.util.Properties)}
     * @throws InstantiationException see
     * {@link ParserRunner#instantiateParser(java.util.Properties)}
     * @throws InvocationTargetException see
     * {@link ParserRunner#instantiateParser(java.util.Properties)}
     * @throws NoSuchMethodException see
     * {@link ParserRunner#instantiateParser(java.util.Properties)}
     */
    public static TemporalDataModelIF<Long, Long>[] prepareSplitsInMemory(final Properties properties)
            throws IOException, ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException {
        // get parameters
        String inFile = properties.getProperty(ParserRunner.DATASET_FILE);
        // parse dataset
        Parser<Long, Long> parser = ParserRunner.instantiateParser(properties);
        TemporalDataModelIF<Long, Long> data = parser.parseTemporalData(new File(inFile));
        // prepare splits
        Splitter<Long, Long> splitter = SplitterRunner.instantiateSplitter(properties);
        return splitter.split(data);
    }

    /**
     *
     * Generates recommender models according to the properties mapping and the
     * provided training and test models.
     *
     * @param properties properties to be used.
     * @param trainingModel model to train the recommenders.
     * @param testModel model to test the recommenders.
     * @return a DataModel for every recommender generated assigned to its
     * corresponding name.
     * @throws Exception see
     * {@link AbstractRunner#run(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS, net.recommenders.rival.core.TemporalDataModelIF, net.recommenders.rival.core.TemporalDataModelIF)}
     */
    public static Map<String, DataModelIF<Long, Long>> getRecommenderModels(final Properties properties, final TemporalDataModelIF<Long, Long> trainingModel, final TemporalDataModelIF<Long, Long> testModel)
            throws Exception {
        AbstractRunner<Long, Long>[] mahoutRecs = MultipleRecommendationRunner.instantiateMahoutRecommenders(new HashSet<String>() {

            {
                add(".");
            }
        }, properties);
        AbstractRunner<Long, Long>[] lenskitRecs = MultipleRecommendationRunner.instantiateLenskitRecommenders(new HashSet<String>() {

            {
                add(".");
            }
        }, properties);

        Map<String, DataModelIF<Long, Long>> recommenderModels = new HashMap<>();

        for (AbstractRunner<Long, Long> mahoutRec : mahoutRecs) {
            recommenderModels.put(mahoutRec.getCanonicalFileName(), mahoutRec.run(AbstractRunner.RUN_OPTIONS.RETURN_RECS, trainingModel, testModel));
        }
        for (AbstractRunner<Long, Long> lensKit : lenskitRecs) {
            recommenderModels.put(lensKit.getCanonicalFileName(), lensKit.run(AbstractRunner.RUN_OPTIONS.RETURN_RECS, trainingModel, testModel));
        }

        return recommenderModels;
    }

    /**
     *
     * Generates the subset of the recommendations generated by a recommender to
     * be used in a given strategy.
     *
     * @param properties properties to be used.
     * @param trainingModel tranining model used to generate strategies.
     * @param testModel test model used to generate strategies.
     * @param recModel a recommender model containing recommendations for users.
     * @return a datamodel for each strategy and its corresponding name.
     * @throws ClassNotFoundException see
     * {@link MultipleStrategyRunner#instantiateStrategies(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws IllegalAccessException see
     * {@link MultipleStrategyRunner#instantiateStrategies(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InstantiationException see
     * {@link MultipleStrategyRunner#instantiateStrategies(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InvocationTargetException see
     * {@link MultipleStrategyRunner#instantiateStrategies(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws NoSuchMethodException see
     * {@link MultipleStrategyRunner#instantiateStrategies(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     */
    public static Map<String, DataModelIF<Long, Long>> applyStrategiesToRecommender(final Properties properties,
            final DataModelIF<Long, Long> trainingModel, final DataModelIF<Long, Long> testModel, final DataModelIF<Long, Long> recModel)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException {
        // apply all strategies
        Map<String, DataModelIF<Long, Long>> modelToEvals = new HashMap<>();
        for (EvaluationStrategy<Long, Long> strategy : MultipleStrategyRunner.instantiateStrategies(properties, trainingModel, testModel)) {
            // apply strategy
            DataModelIF<Long, Long> modelToEval = DataModelFactory.getDefaultModel();
            for (Long user : recModel.getUsers()) {
                for (Long item : strategy.getCandidateItemsToRank(user)) {
                    if (!Double.isNaN(recModel.getUserItemPreference(user, item))) {
                        modelToEval.addPreference(user, item, recModel.getUserItemPreference(user, item));
                    }
                }
            }
            modelToEvals.put(strategy.toString(), modelToEval);
        }
        return modelToEvals;
    }

    /**
     *
     * Computes evaluation results for a combination of recommender and
     * strategy.
     *
     * @param properties properties to be used.
     * @param test test model (groundtruth).
     * @param modelToEvaluate model containing the recommendations (filtered by
     * the strategies) to be evaluated.
     * @return the evaluation results for every metric included in the
     * properties mapping.
     * @throws ClassNotFoundException see
     * {@link MultipleEvaluationMetricRunner#instantiateEvaluationMetrics(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws IllegalAccessException see
     * {@link MultipleEvaluationMetricRunner#instantiateEvaluationMetrics(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InstantiationException see
     * {@link MultipleEvaluationMetricRunner#instantiateEvaluationMetrics(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws InvocationTargetException see
     * {@link MultipleEvaluationMetricRunner#instantiateEvaluationMetrics(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     * @throws NoSuchMethodException see
     * {@link MultipleEvaluationMetricRunner#instantiateEvaluationMetrics(java.util.Properties, net.recommenders.rival.core.DataModelIF, net.recommenders.rival.core.DataModelIF)}
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Double>> evaluateStrategy(final Properties properties, final DataModelIF<Long, Long> test, final DataModelIF<Long, Long> modelToEvaluate)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Map<String, Map<String, Double>> mapMetricResults = new HashMap<>();
        for (EvaluationMetric<Long> metric : MultipleEvaluationMetricRunner.instantiateEvaluationMetrics(properties, modelToEvaluate, test)) {
            metric.compute();
            Double all = metric.getValue();
            Map<String, Double> results = new HashMap<>();
            mapMetricResults.put(metric.toString(), results);
            results.put("all", all);
            Map<Long, Double> perUser = metric.getValuePerUser();
            for (Entry<Long, Double> e : perUser.entrySet()) {
                Long u = e.getKey();
                results.put(u.toString(), e.getValue());
            }
            // is this a ranking metric?
            if (metric instanceof AbstractRankingMetric) {
                AbstractRankingMetric<Long, Long> rankingMetric = (AbstractRankingMetric<Long, Long>) metric;
                for (int n : rankingMetric.getCutoffs()) {
                    all = rankingMetric.getValueAt(n);
                    results = new HashMap<>();
                    mapMetricResults.put(metric.toString() + "@" + n, results);
                    results.put("all", all);
                    perUser = rankingMetric.getValuePerUser();
                    for (Long u : perUser.keySet()) {
                        results.put(u.toString(), rankingMetric.getValueAt(u, n));
                    }
                }
            }
        }
        return mapMetricResults;
    }

    /**
     *
     * Computes statistical analysis for a set of results, and considering which
     * recommender is the baseline (according to the properties mapping).
     *
     * @param properties properties to be used.
     * @param strategyResults mapping of evaluation results for every
     * recommender (one map for every metric).
     * @param out where the statistical analysis should be printed.
     */
    private static void computeStatistics(final Properties properties, final Map<String, Map<String, Map<String, Double>>> strategyResults, final PrintStream out) {
        // extract baseline from map
        String baselineName = properties.getProperty(StatisticsRunner.BASELINE_FILE);
        if (baselineName == null) {
            System.err.println("Name of baseline method not found in properties!");
            return;
        }
        Map<String, Map<String, Double>> baselineResults = null;
        Map<String, Map<String, Map<String, Double>>> methodsResults = new HashMap<>();
        for (Entry<String, Map<String, Map<String, Double>>> e : strategyResults.entrySet()) {
            String n = e.getKey();
            if (n.equals(baselineName)) {
                baselineResults = e.getValue();
            } else {
                methodsResults.put(n, e.getValue());
            }
        }
        if (baselineResults == null) {
            System.err.println("Baseline method not found (required for statistic functions)!");
            return;
        }
        // run statistical methods
        properties.put(StatisticsRunner.TEST_METHODS_FILES, methodsResults.keySet());
        StatisticsRunner.run(properties, out, baselineName, baselineResults, methodsResults);
    }
}
