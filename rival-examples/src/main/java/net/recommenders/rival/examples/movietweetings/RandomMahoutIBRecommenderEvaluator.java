package net.recommenders.rival.examples.movietweetings;

import net.recommenders.rival.core.*;
import net.recommenders.rival.evaluation.metric.error.RMSE;
import net.recommenders.rival.evaluation.metric.ranking.NDCG;
import net.recommenders.rival.evaluation.metric.ranking.Precision;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;
import net.recommenders.rival.examples.DataDownloader;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.mahout.GenericRecommenderBuilder;
import net.recommenders.rival.recommend.frameworks.mahout.exceptions.RecommenderException;
import net.recommenders.rival.split.splitter.RandomSplitter;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class RandomMahoutIBRecommenderEvaluator {

    public static void main(String[] args) {
        String url = "https://raw.githubusercontent.com/sidooms/MovieTweetings/master/snapshots/10K/ratings.dat";
        String modelPath = "data/movietweeting/model/";
        String recPath = "data/movietweeting/recommendations/";
        String folder = "data/movietweeting";
        int nFolds = 1;
        prepareSplits(url, nFolds, "data/movietweeting/ratings.dat", folder, modelPath);
        recommend(nFolds, modelPath, recPath);
        prepareStrategy(nFolds, modelPath, recPath, modelPath);
        evaluate(nFolds, modelPath, recPath);
    }

    public static void prepareSplits(String url, int nFolds, String inFile, String folder, String outPath) {
        DataDownloader dd = new DataDownloader(url, folder);
        dd.download();

        boolean perUser = true;
        long seed = 20;
        AbstractParser parser = new UIPParser();

        parser.setDelimiter(':');
        parser.setUserTok(0);
        parser.setItemTok(2);
        parser.setPrefTok(4);
        parser.setTimeTok(6);

        DataModel<Long, Long> data = null;
        try {
            data = parser.parseData(new File(inFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataModel<Long, Long>[] splits = new RandomSplitter(0.2f, perUser, seed, false).split(data);
        File dir = new File(outPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (int i = 0; i < splits.length / 2; i++) {
            DataModel<Long, Long> training = splits[2 * i];
            DataModel<Long, Long> test = splits[2 * i + 1];
            String trainingFile = outPath + "train_" + i + ".csv";
            String testFile = outPath + "test_" + i + ".csv";
            System.out.println("train: " + trainingFile);
            System.out.println("test: " + testFile);
            boolean overwrite = true;
            try {
                DataModelUtils.saveDataModel(training, trainingFile, overwrite);
                DataModelUtils.saveDataModel(test, testFile, overwrite);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void recommend(int nFolds, String inPath, String outPath) {
        for (int i = 0; i < nFolds; i++) {
            org.apache.mahout.cf.taste.model.DataModel trainModel = null;
            org.apache.mahout.cf.taste.model.DataModel testModel = null;
            try {
                trainModel = new FileDataModel(new File(inPath + "train_" + i + ".csv"));
                testModel = new FileDataModel(new File(inPath + "test_" + i + ".csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            GenericRecommenderBuilder grb = new GenericRecommenderBuilder();
            String recommenderClass = "org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender";
            String similarityClass = "org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity";
            Recommender recommender = null;
            try {
                recommender = grb.buildRecommender(trainModel, recommenderClass, similarityClass);
            } catch (TasteException e) {
                e.printStackTrace();
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
                    RecommenderIO.writeData(u, items, outPath, fileName, !createFile);
                    createFile = false;
                }
            } catch (TasteException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void prepareStrategy(int nFolds, String splitPath, String recPath, String outPath) {
        for (int i = 0; i < nFolds; i++) {
            File trainingFile = new File(splitPath + "train_" + i + ".csv");
            File testFile = new File(splitPath + "test_" + i + ".csv");
            File recFile = new File(recPath + "recs_" + i + ".csv");
            DataModel<Long, Long> trainingModel = null;
            DataModel<Long, Long> testModel = null;
            DataModel<Long, Long> recModel = null;
            try {
                trainingModel = new SimpleParser().parseData(trainingFile);
                testModel = new SimpleParser().parseData(testFile);
                recModel = new SimpleParser().parseData(recFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Double threshold = 2.0;
            String strategyClassName = "net.recommenders.rival.evaluation.strategy.UserTest";
            EvaluationStrategy<Long, Long> strategy = null;
            try {
                strategy = (EvaluationStrategy<Long, Long>) (Class.forName(strategyClassName)).getConstructor(DataModel.class, DataModel.class, double.class).newInstance(trainingModel, testModel, threshold);
                // Alternatively
                // strategy = new UserTest(trainingModel,testModel,threshold);
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

            DataModel<Long, Long> modelToEval = new DataModel<Long, Long>();

            for (Long user : recModel.getUsers()) {
                for (Long item : strategy.getCandidateItemsToRank(user)) {
                    if (recModel.getUserItemPreferences().get(user).containsKey(item)) {
                        modelToEval.addPreference(user, item, recModel.getUserItemPreferences().get(user).get(item));
                    }
                }
            }
            try {
                DataModelUtils.saveDataModel(modelToEval, outPath + "strategymodel_" + i + ".csv", true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void evaluate(int nFolds, String splitPath, String recPath) {
        double ndcgRes = 0.0;
        double precisionRes = 0.0;
        double rmseRes = 0.0;
        for (int i = 0; i < nFolds; i++) {
            File testFile = new File(splitPath + "test_" + i + ".csv");
            File recFile = new File(recPath + "recs_" + i + ".csv");
            DataModel<Long, Long> testModel = null;
            DataModel<Long, Long> recModel = null;
            try {
                testModel = new SimpleParser().parseData(testFile);
                recModel = new SimpleParser().parseData(recFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            NDCG<Long, Long> ndcg = new NDCG<Long, Long>(recModel, testModel, new int[]{10});
            ndcg.compute();
            ndcgRes += ndcg.getValueAt(10);

            RMSE<Long, Long> rmse = new RMSE<Long, Long>(recModel, testModel);
            rmse.compute();
            rmseRes += rmse.getValue();

            Precision<Long, Long> precision = new Precision<Long, Long>(recModel, testModel, 3.0, new int[]{10});
            precision.compute();
            precisionRes += precision.getValueAt(10);
        }
        System.out.println("NDCG@10: " + ndcgRes / nFolds);
        System.out.println("RMSE: " + rmseRes / nFolds);
        System.out.println("P@10: " + precisionRes / nFolds);

    }
}
