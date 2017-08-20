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

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.DataModelUtils;
import net.recommenders.rival.core.Parser;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.metric.ranking.NDCG;
import net.recommenders.rival.evaluation.metric.ranking.Precision;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;
import net.recommenders.rival.examples.DataDownloader;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.mahout.GenericRecommenderBuilder;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import net.recommenders.rival.split.parser.MovielensParser;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.evaluation.metric.error.RMSE;
import net.recommenders.rival.split.splitter.RandomSplitter;

/**
 * RiVal Movielens100k Mahout Example, using 80-20 random splitting.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
public final class RandomSplitMahoutKNNRecommenderEvaluator {

    /**
     * Default percentage.
     */
    public static final float PERCENTAGE = 0.8f;
    /**
     * Default neighbohood size.
     */
    public static final int NEIGH_SIZE = 50;
    /**
     * Default cutoff for evaluation metrics.
     */
    public static final int AT = 10;
    /**
     * Default relevance threshold.
     */
    public static final double REL_TH = 3.0;
    /**
     * Default seed.
     */
    public static final long SEED = 2048L;

    /**
     * Utility classes should not have a public or default constructor.
     */
    private RandomSplitMahoutKNNRecommenderEvaluator() {
    }

    /**
     * Main method. Parameter is not used.
     *
     * @param args the arguments (not used)
     */
    public static void main(final String[] args) {
        String url = "http://files.grouplens.org/datasets/movielens/ml-100k.zip";
        String folder = "data/ml-100k";
        String modelPath = "data/ml-100k/model/";
        String recPath = "data/ml-100k/recommendations/";
        String dataFile = "data/ml-100k/ml-100k/u.data";
        float percentage = PERCENTAGE;
        prepareSplits(url, percentage, dataFile, folder, modelPath);
        recommend(modelPath, recPath);
        // the strategy files are (currently) being ignored
        prepareStrategy(modelPath, recPath, modelPath);
        evaluate(modelPath, recPath);
    }

    /**
     * Downloads a dataset and stores the splits generated from it.
     *
     * @param url url where dataset can be downloaded from
     * @param percentage percentage of training data
     * @param inFile file to be used once the dataset has been downloaded
     * @param folder folder where dataset will be stored
     * @param outPath path where the splits will be stored
     */
    public static void prepareSplits(final String url, final float percentage, final String inFile, final String folder, final String outPath) {
        DataDownloader dd = new DataDownloader(url, folder);
        dd.downloadAndUnzip();

        boolean perUser = true;
        boolean perItem = false;
        long seed = SEED;
        Parser<Long, Long> parser = new MovielensParser();

        DataModelIF<Long, Long> data = null;
        try {
            data = parser.parseData(new File(inFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataModelIF<Long, Long>[] splits = new RandomSplitter<Long, Long>(percentage, perUser, seed, perItem).split(data);
        File dir = new File(outPath);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.err.println("Directory " + dir + " could not be created");
                return;
            }
        }
        for (int i = 0; i < splits.length / 2; i++) {
            DataModelIF<Long, Long> training = splits[2 * i];
            DataModelIF<Long, Long> test = splits[2 * i + 1];
            String trainingFile = outPath + "train_" + i + ".csv";
            String testFile = outPath + "test_" + i + ".csv";
            System.out.println("train: " + trainingFile);
            System.out.println("test: " + testFile);
            boolean overwrite = true;
            try {
                DataModelUtils.saveDataModel(training, trainingFile, overwrite, "\t");
                DataModelUtils.saveDataModel(test, testFile, overwrite, "\t");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Recommends using an UB algorithm.
     *
     * @param inPath path where training and test models have been stored
     * @param outPath path where recommendation files will be stored
     */
    public static void recommend(final String inPath, final String outPath) {
        int i = 0;
        org.apache.mahout.cf.taste.model.DataModel trainModel = null;
        org.apache.mahout.cf.taste.model.DataModel testModel = null;
        try {
            trainModel = new FileDataModel(new File(inPath + "train_" + i + ".csv"));
            testModel = new FileDataModel(new File(inPath + "test_" + i + ".csv"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        GenericRecommenderBuilder grb = new GenericRecommenderBuilder();
        String recommenderClass = "org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender";
        String similarityClass = "org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity";
        int neighborhoodSize = NEIGH_SIZE;
        Recommender recommender = null;
        try {
            recommender = grb.buildRecommender(trainModel, recommenderClass, similarityClass, neighborhoodSize);
        } catch (RecommenderException e) {
            e.printStackTrace();
        }

        String fileName = "recs_" + i + ".csv";

        LongPrimitiveIterator users = null;
        try {
            users = testModel.getUserIDs();
            boolean createFile = true;
            while (users.hasNext()) {
                long u = users.nextLong();
                List<RecommendedItem> items = recommender.recommend(u, trainModel.getNumItems());
                //
                List<RecommenderIO.Preference<Long, Long>> prefs = new ArrayList<>();
                for (RecommendedItem ri : items) {
                    prefs.add(new RecommenderIO.Preference<>(u, ri.getItemID(), ri.getValue()));
                }
                //
                RecommenderIO.writeData(u, prefs, outPath, fileName, !createFile, null);
                createFile = false;
            }
        } catch (TasteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepares the strategies to be evaluated with the recommenders already
     * generated.
     *
     * @param splitPath path where splits have been stored
     * @param recPath path where recommendation files have been stored
     * @param outPath path where the filtered recommendations will be stored
     */
    @SuppressWarnings("unchecked")
    public static void prepareStrategy(final String splitPath, final String recPath, final String outPath) {
        int i = 0;
        File trainingFile = new File(splitPath + "train_" + i + ".csv");
        File testFile = new File(splitPath + "test_" + i + ".csv");
        File recFile = new File(recPath + "recs_" + i + ".csv");
        DataModelIF<Long, Long> trainingModel = null;
        DataModelIF<Long, Long> testModel = null;
        DataModelIF<Long, Long> recModel = null;
        try {
            trainingModel = new SimpleParser().parseData(trainingFile);
            testModel = new SimpleParser().parseData(testFile);
            recModel = new SimpleParser().parseData(recFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Double threshold = REL_TH;
        String strategyClassName = "net.recommenders.rival.evaluation.strategy.UserTest";
        EvaluationStrategy<Long, Long> strategy = null;
        try {
            strategy = (EvaluationStrategy<Long, Long>) (Class.forName(strategyClassName)).getConstructor(DataModelIF.class, DataModelIF.class, double.class).
                    newInstance(trainingModel, testModel, threshold);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        DataModelIF<Long, Long> modelToEval = DataModelFactory.getDefaultModel();
        for (Long user : recModel.getUsers()) {
            for (Long item : strategy.getCandidateItemsToRank(user)) {
                if (!Double.isNaN(recModel.getUserItemPreference(user, item))) {
                    modelToEval.addPreference(user, item, recModel.getUserItemPreference(user, item));
                }
            }
        }
        try {
            DataModelUtils.saveDataModel(modelToEval, outPath + "strategymodel_" + i + ".csv", true, "\t");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Evaluates the recommendations generated in previous steps.
     *
     * @param splitPath path where splits have been stored
     * @param recPath path where recommendation files have been stored
     */
    public static void evaluate(final String splitPath, final String recPath) {
        double ndcgRes = 0.0;
        double precisionRes = 0.0;
        double rmseRes = 0.0;

        int i = 0;
        File testFile = new File(splitPath + "test_" + i + ".csv");
        File recFile = new File(recPath + "recs_" + i + ".csv");
        DataModelIF<Long, Long> testModel = null;
        DataModelIF<Long, Long> recModel = null;
        try {
            testModel = new SimpleParser().parseData(testFile);
            recModel = new SimpleParser().parseData(recFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        NDCG<Long, Long> ndcg = new NDCG<Long, Long>(recModel, testModel, new int[]{AT});
        ndcg.compute();
        ndcgRes += ndcg.getValueAt(AT);

        RMSE<Long, Long> rmse = new RMSE<Long, Long>(recModel, testModel);
        rmse.compute();
        rmseRes += rmse.getValue();

        Precision<Long, Long> precision = new Precision<Long, Long>(recModel, testModel, REL_TH, new int[]{AT});
        precision.compute();
        precisionRes += precision.getValueAt(AT);

        System.out.println("NDCG@" + AT + ": " + ndcgRes);
        System.out.println("RMSE: " + rmseRes);
        System.out.println("P@" + AT + ": " + precisionRes);
    }
}
