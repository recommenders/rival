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
package net.recommenders.rival.examples.movielens100k;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.Parser;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;
import net.recommenders.rival.evaluation.metric.MultipleEvaluationMetricRunner;
import net.recommenders.rival.evaluation.statistics.StatisticsRunner;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;
import net.recommenders.rival.evaluation.strategy.MultipleStrategyRunner;
import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.MultipleRecommendationRunner;
import net.recommenders.rival.split.parser.ParserRunner;
import net.recommenders.rival.split.splitter.Splitter;
import net.recommenders.rival.split.splitter.SplitterRunner;

/**
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class CompleteExampleInMemory {

    private static void fillDefaultProperties(Properties props) {
        System.out.println("Setting default properties...");
        // parser
        props.put(ParserRunner.DATASET_FILE, "./data/ml-100k/u.data");
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
        props.put(MultipleRecommendationRunner.LENSKIT_SIMILARITIES, "org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity,org.grouplens.lenskit.vectors.similarity.PearsonCorrelation");
//        props.put(MultipleRecommendationRunner.LENSKIT_SVD_RECS, "org.grouplens.lenskit.mf.funksvd.FunkSVDItemScorer");
        props.put(MultipleRecommendationRunner.LENSKIT_SVD_RECS, "");
//        props.put(MultipleRecommendationRunner.LENSKIT_USERBASED_RECS, "org.grouplens.lenskit.knn.user.UserUserItemScorer");
        props.put(MultipleRecommendationRunner.LENSKIT_USERBASED_RECS, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_ITEMBASED_RECS, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_SIMILARITIES, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_SVD_FACTORIZER, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_SVD_RECS, "");
//        props.put(MultipleRecommendationRunner.MAHOUT_USERBASED_RECS, "");
        props.put(MultipleRecommendationRunner.N, "-1,10,50");
        props.put(MultipleRecommendationRunner.SVD_ITER, "50");
        // strategy
        props.put(MultipleStrategyRunner.STRATEGIES, "net.recommenders.rival.evaluation.strategy.RelPlusN,net.recommenders.rival.evaluation.strategy.TestItems,net.recommenders.rival.evaluation.strategy.AllItems,net.recommenders.rival.evaluation.strategy.TrainItems,net.recommenders.rival.evaluation.strategy.UserTest");
        props.put(MultipleStrategyRunner.RELEVANCE_THRESHOLDS, "5");
        props.put(MultipleStrategyRunner.RELPLUSN_N, "100");
        props.put(MultipleStrategyRunner.RELPLUSN_SEED, "2015");
        // evaluation
        props.put(MultipleEvaluationMetricRunner.METRICS, "net.recommenders.rival.evaluation.metric.error.MAE,net.recommenders.rival.evaluation.metric.error.RMSE,net.recommenders.rival.evaluation.metric.ranking.MAP,net.recommenders.rival.evaluation.metric.ranking.NDCG,net.recommenders.rival.evaluation.metric.ranking.Precision,net.recommenders.rival.evaluation.metric.ranking.Recall");
        props.put(MultipleEvaluationMetricRunner.RELEVANCE_THRESHOLD, "5");
        props.put(MultipleEvaluationMetricRunner.RANKING_CUTOFFS, "1,5,10,50");
        props.put(MultipleEvaluationMetricRunner.NDCG_TYPE, "exp");
        props.put(MultipleEvaluationMetricRunner.ERROR_STRATEGY, "NOT_CONSIDER_NAN");
        // statistics
        props.put(StatisticsRunner.ALPHA, "0.05");
        props.put(StatisticsRunner.AVOID_USERS, "all");
        props.put(StatisticsRunner.STATISTICS, "confidence_interval,effect_size_d,effect_size_dLS,effect_size_pairedT,standard_error,statistical_significance_t,statistical_significance_pairedT,statistical_significance_wilcoxon");
        props.put(StatisticsRunner.INPUT_FORMAT, "default");
        //              we use simple names instead of files
        props.put(StatisticsRunner.BASELINE_FILE, "");
        props.put(StatisticsRunner.TEST_METHODS_FILES, "");
        //
        System.out.println("Properties: " + props);
    }

    public static void main(String[] args) {
        String propertyFile = System.getProperty("propertyFile");

        final Properties properties = new Properties();
        try {
            if (propertyFile == null) {
                fillDefaultProperties(properties);
            } else {
                properties.load(new FileInputStream(propertyFile));
            }
        } catch (FileNotFoundException e) {
            fillDefaultProperties(properties);
            e.printStackTrace();
        } catch (IOException ie) {
            fillDefaultProperties(properties);
            ie.printStackTrace();
        }

        runExampleInMemory(properties);
    }

    public static void runExampleInMemory(Properties properties) {
        try {
            DataModel<Long, Long>[] splits = prepareSplitsInMemory(properties);
            for (int i = 0; i < splits.length / 2; i++) {
                System.out.println(">>> Processing split " + i / 2);
                DataModel<Long, Long> training = splits[2 * i];
                DataModel<Long, Long> test = splits[2 * i + 1];
                Map<String, DataModel<Long, Long>> recModels = getRecommenderModels(properties, training, test);
                Map<String, Map<String, Map<String, Map<String, Double>>>> mapStrategyRecommenderMetricUserValue = new HashMap();
                for (String rec : recModels.keySet()) {
                    DataModel<Long, Long> recModel = recModels.get(rec);
                    Map<String, DataModel<Long, Long>> evalModels = applyStrategiesToRecommender(properties, training, test, recModel);
                    for (String strat : evalModels.keySet()) {
                        DataModel<Long, Long> evalModel = evalModels.get(strat);
                        Map<String, Map<String, Double>> results = evaluateStrategy(properties, test, evalModel);
                        // assign these results to a rec+strategy, at the end, compute statistics for all recs and one strategy
                        Map<String, Map<String, Map<String, Double>>> stratResults = mapStrategyRecommenderMetricUserValue.get(strat);
                        if (stratResults == null) {
                            stratResults = new HashMap();
                            mapStrategyRecommenderMetricUserValue.put(strat, stratResults);
                        }
                        stratResults.put(rec, results);
                        // print results
                        for (String metric : results.keySet()) {
                            Map<String, Double> metricResults = results.get(metric);
                            System.out.println(rec + "\t" + strat + "\t" + metric + "\t" + metricResults.get("all"));
                            // remove this value to not be considered in subsequent statistical analysis
                            metricResults.remove("all");
                        }
                    }
                }
                // compute statistics for a given strategy
                for (String strat : mapStrategyRecommenderMetricUserValue.keySet()) {
                    Map<String, Map<String, Map<String, Double>>> strategyResults = mapStrategyRecommenderMetricUserValue.get(strat);
                    System.out.println("----> Statistics for strategy " + strat);
                    computeStatistics(properties, strategyResults, System.out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DataModel<Long, Long>[] prepareSplitsInMemory(Properties properties) throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // get parameters
        String inFile = properties.getProperty(ParserRunner.DATASET_FILE);
        // parse dataset
        Parser parser = ParserRunner.instantiateParser(properties);
        DataModel<Long, Long> data = parser.parseData(new File(inFile));
        // prepare splits
        Splitter<Long, Long> splitter = SplitterRunner.instantiateSplitter(properties);
        DataModel<Long, Long>[] splits = splitter.split(data);
        return splits;
    }

    public static Map<String, DataModel<Long, Long>> getRecommenderModels(Properties properties, DataModel<Long, Long> trainingModel, DataModel<Long, Long> testModel) throws Exception {
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

        Map<String, DataModel<Long, Long>> recommenderModels = new HashMap();

        for (AbstractRunner<Long, Long> mahoutRec : mahoutRecs) {
            recommenderModels.put(mahoutRec.getCanonicalFileName(), mahoutRec.run(AbstractRunner.RUN_OPTIONS.RETURN_RECS, trainingModel, testModel));
        }
        for (AbstractRunner<Long, Long> lensKit : lenskitRecs) {
            recommenderModels.put(lensKit.getCanonicalFileName(), lensKit.run(AbstractRunner.RUN_OPTIONS.RETURN_RECS, trainingModel, testModel));
        }

        return recommenderModels;
    }

    public static Map<String, DataModel<Long, Long>> applyStrategiesToRecommender(Properties properties, DataModel<Long, Long> trainingModel, DataModel<Long, Long> testModel, DataModel<Long, Long> recModel) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // apply all strategies
        Map<String, DataModel<Long, Long>> modelToEvals = new HashMap();
        for (EvaluationStrategy<Long, Long> strategy : MultipleStrategyRunner.instantiateStrategies(properties, trainingModel, testModel)) {
            // apply strategy
            DataModel<Long, Long> modelToEval = new DataModel<Long, Long>();
            for (Long user : recModel.getUsers()) {
                for (Long item : strategy.getCandidateItemsToRank(user)) {
                    if (recModel.getUserItemPreferences().get(user).containsKey(item)) {
                        modelToEval.addPreference(user, item, recModel.getUserItemPreferences().get(user).get(item));
                    }
                }
            }
            modelToEvals.put(strategy.toString(), modelToEval);
        }
        return modelToEvals;
    }

    private static Map<String, Map<String, Double>> evaluateStrategy(Properties properties, DataModel<Long, Long> test, DataModel<Long, Long> evalModel) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Map<String, Map<String, Double>> mapMetricResults = new HashMap();
        for (EvaluationMetric<Long> metric : MultipleEvaluationMetricRunner.instantiateEvaluationMetrics(properties, evalModel, test)) {
            metric.compute();
            Map<Long, Double> perUser = metric.getValuePerUser();
            Double all = metric.getValue();
            Map<String, Double> results = new HashMap();
            mapMetricResults.put(metric.toString(), results);
            results.put("all", all);
            for (Long u : perUser.keySet()) {
                results.put(u.toString(), perUser.get(u));
            }
        }
        return mapMetricResults;
    }

    private static void computeStatistics(Properties properties, Map<String, Map<String, Map<String, Double>>> strategyResults, PrintStream out) {
        // extract baseline from map
        String baselineName = properties.getProperty(StatisticsRunner.BASELINE_FILE);
        Map<String, Map<String, Double>> baselineResults = null;
        Map<String, Map<String, Map<String, Double>>> methodsResults = null;
        for (String n : strategyResults.keySet()) {
            if (n.equals(baselineName)) {
                baselineResults = strategyResults.get(n);
            } else {
                methodsResults.put(n, strategyResults.get(n));
            }
        }
        if (baselineResults == null) {
            System.err.println("Baseline method not found (required for statistic functions)!");
            return;
        }
        // run statistical methods
        StatisticsRunner.run(properties, out, baselineName, baselineResults, methodsResults);
    }
}
