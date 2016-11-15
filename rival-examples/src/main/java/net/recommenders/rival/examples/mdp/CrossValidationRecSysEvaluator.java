package net.recommenders.rival.examples.mdp;

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.DataModelUtils;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.metric.error.MAE;
import net.recommenders.rival.evaluation.metric.error.RMSE;
import net.recommenders.rival.evaluation.metric.ranking.NDCG;
import net.recommenders.rival.evaluation.metric.ranking.Precision;
import net.recommenders.rival.evaluation.metric.ranking.Recall;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;
import net.recommenders.rival.evaluation.strategy.UserTest;
import net.recommenders.rival.split.splitter.CrossValidationSplitter;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import net.recommenders.rival.core.DataModelFactory;

/**
 * RiVal-based evaluation of recommender systems,
 * using the k-fold cross validation and UserTest strategy.
 * <p>
 * Input: a csv dataset containing users, items and ratings.
 * Output: evaluation metrics results.
 *
 * @author <a href="http://github.com/mdip">Marco Di Pietro</a>
 */

public abstract class CrossValidationRecSysEvaluator {

   Recommender recommender;
   private String delimiter;
   private double relevanceThreshold;
   private int[] cutoffs;
   private int numFolds;
   private static final String FILE_EXT = ".tsv";
   private Logger log = LoggerFactory.getLogger("");

   CrossValidationRecSysEvaluator(int numFolds, double relevanceThreshold) {
      this.numFolds = numFolds;
      this.relevanceThreshold = relevanceThreshold;
      this.delimiter = ",";
      this.cutoffs = new int[]{5, 10};
   }

   /**
    * Load a dataset and stores the splits generated from it.
    *
    * @param inFile    input dataset
    * @param outPath   path where the splits will be stored
    * @param perUser   flag for enable or disable splitting by user
    * @param seed      seed for creating random split
    * @param delimiter dataset delimiter
    */
   public void split(final String inFile, final String outPath, boolean perUser, long seed, String delimiter, boolean isTemporalData) {

      try {

         if (delimiter == null)
            delimiter = this.delimiter;

         DataModelIF<Long, Long>[] splits = new CrossValidationSplitter<Long, Long>(this.numFolds, perUser, seed).split(
                 new SimpleParser().parseData(new File(inFile), delimiter, isTemporalData));

         File dir = new File(outPath);
         if (!dir.exists()) {
            if (!dir.mkdirs()) {
               log.error("Directory {} could not be created", dir);
               return;
            }
         }
         for (int i = 0; i < splits.length / 2; i++) {
            DataModelIF<Long, Long> training = splits[2 * i];
            DataModelIF<Long, Long> test = splits[2 * i + 1];
            String trainingFile = Paths.get(outPath, "train_" + i + FILE_EXT).toString();
            String testFile = Paths.get(outPath, "test_" + i + FILE_EXT).toString();
            log.info("train model fold {}: {}", (i + 1), trainingFile);
            log.info("test: model fold {}: {}", (i + 1), testFile);
            try {
               DataModelUtils.saveDataModel(training, trainingFile, true);
               DataModelUtils.saveDataModel(test, testFile, true);
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
               e.printStackTrace();
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   protected abstract Recommender buildRecommender(org.apache.mahout.cf.taste.model.DataModel model) throws TasteException;

   /**
    * Make predictions.
    *
    * @param inPath
    * @param outPath
    * @throws IOException
    * @throws TasteException
    */
   public void recommend(final String inPath, final String outPath) throws IOException, TasteException {

      for (int i = 0; i < this.numFolds; i++) {

         org.apache.mahout.cf.taste.model.DataModel trainModel;
         org.apache.mahout.cf.taste.model.DataModel testModel;

         trainModel = new FileDataModel(new File(Paths.get(inPath, "train_" + i + FILE_EXT).toString()));
         testModel = new FileDataModel(new File(Paths.get(inPath, "test_" + i + FILE_EXT).toString()));

         File dir = new File(outPath);
         if (!dir.exists()) {
            if (!dir.mkdirs()) {
               log.error("Directory {} could not be created", dir);
               throw new IOException("Directory " + dir.toString() + " could not be created");
            }
         }
         recommender = buildRecommender(trainModel);

         log.info("Predicting ratings...");

         String predictionsFileName = "recs_" + i + FILE_EXT;
         File predictionsFile = new File(Paths.get(outPath, predictionsFileName).toString());
         BufferedWriter bw = new BufferedWriter(new FileWriter(predictionsFile));
         PrintWriter outFile = new PrintWriter(bw, true);

         int numUsers = testModel.getNumUsers();
         int progress = 0;
         int counter = 0;

         LongPrimitiveIterator users = testModel.getUserIDs();
         while (users.hasNext()) {
            long user = users.nextLong();
            try {
               for (RecommendedItem item : recommender.recommend(user, trainModel.getNumItems())) {
                  String s = user + "\t" + item.getItemID() + "\t" + item.getValue();
                  outFile.println(s);
               }
            } catch (NoSuchUserException e) {
               log.debug("No such user exception. Skipping recommendations for user {}", e.getMessage());
            } finally {
               counter++;
               if (counter >= numUsers / 10 || !users.hasNext()) {
                  progress += counter;
                  counter = 0;
                  log.info("Predictions for {} users done...", progress);
               }
            }
         }
         outFile.close();
      }
   }

   /**
    * Prepare the strategy models using prediction files.
    *
    * @param splitPath       path where splits have been stored
    * @param predictionsPath path where prediction files have been stored
    * @param outPath         path where the filtered recommendations will be stored
    */
   public void buildEvaluationModels(final String splitPath, final String predictionsPath, final String outPath) {
      for (int i = 0; i < this.numFolds; i++) {
         File trainingFile = new File(Paths.get(splitPath, "train_" + i + FILE_EXT).toString());
         File testFile = new File(Paths.get(splitPath, "test_" + i + FILE_EXT).toString());
         File predictionsFile = new File(Paths.get(predictionsPath, "recs_" + i + FILE_EXT).toString());

         DataModelIF<Long, Long> trainingModel;
         DataModelIF<Long, Long> testModel;
         org.apache.mahout.cf.taste.model.DataModel recModel;

         try {
            trainingModel = new SimpleParser().parseData(trainingFile);
            testModel = new SimpleParser().parseData(testFile);
            recModel = new FileDataModel(predictionsFile);
         } catch (IOException e) {
            e.printStackTrace();
            return;
         }

         File dir = new File(outPath);
         if (!dir.exists()) {
            if (!dir.mkdirs()) {
               log.error("Directory " + dir + " could not be created");
               try {
                  throw new FileSystemException("Directory " + dir + " could not be created");
               } catch (FileSystemException e) {
                  e.printStackTrace();
               }
            }
         }

         EvaluationStrategy<Long, Long> strategy = new UserTest(trainingModel, testModel, this.relevanceThreshold);
         DataModelIF<Long, Long> evaluationModel = DataModelFactory.getDefaultModel();
         try {
            DataModelUtils.saveDataModel(evaluationModel, Paths.get(outPath, "strategymodel_" + i + FILE_EXT).toString(), true);
         } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
         }

         try {
            LongPrimitiveIterator userIDs = recModel.getUserIDs();
            while (userIDs.hasNext()) {
               Long user = userIDs.nextLong();
               for (Long item : strategy.getCandidateItemsToRank(user)) {
                  Float rating = recModel.getPreferenceValue(user, item);
                  if (rating != null)
                     evaluationModel.addPreference(user, item, rating.doubleValue());
               }
            }
         } catch (TasteException e) {
            e.printStackTrace();
         }

         try {
            DataModelUtils.saveDataModel(evaluationModel, Paths.get(outPath, "strategymodel_" + i + FILE_EXT).toString(), true);
         } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * Evaluate predictions using an evaluation model against the test set.
    *
    * @param splitPath         path where splits have been stored
    * @param strategyModelPath path where strategy model files have been stored
    * @return evaluation metrics
    */
   public EvaluationMetrics evaluate(final String splitPath, final String strategyModelPath) throws IOException {

      EvaluationMetrics results = new EvaluationMetrics();
      double rmseResult = 0.0;
      double maeResult = 0.0;

      for (int cutoff : this.cutoffs) {
         double ndcgRes = 0.0;
         double precisionRes = 0.0;
         double recallRes = 0.0;

         for (int i = 0; i < this.numFolds; i++) {
            File testFile = new File(Paths.get(splitPath, "test_" + i + FILE_EXT).toString());
            File strategyFile = new File(Paths.get(strategyModelPath, "strategymodel_" + i + FILE_EXT).toString());
            DataModelIF<Long, Long> testModel = new SimpleParser().parseData(testFile);
            DataModelIF<Long, Long> strategyModel = new SimpleParser().parseData(strategyFile);

            // Error metrics calculated only once per fold, using all predictions
            if (cutoff == this.cutoffs[0]) {
               RMSE<Long, Long> rmse = new RMSE<>(strategyModel, testModel);
               rmse.compute();
               rmseResult += rmse.getValue();

               MAE<Long, Long> mae = new MAE<>(strategyModel, testModel);
               mae.compute();
               maeResult += mae.getValue();
            }

            // Ranking metrics
            NDCG<Long, Long> ndcg = new NDCG<>(strategyModel, testModel, new int[]{cutoff});
            ndcg.compute();
            ndcgRes += ndcg.getValueAt(cutoff);

            Precision<Long, Long> precision = new Precision<>(strategyModel, testModel, this.relevanceThreshold, new int[]{cutoff});
            precision.compute();
            precisionRes += precision.getValueAt(cutoff);

            Recall<Long, Long> recall = new Recall<>(strategyModel, testModel, this.relevanceThreshold, new int[]{cutoff});
            recall.compute();
            recallRes += recall.getValueAt(cutoff);
         }

         results.setPrecisionAtK(cutoff, precisionRes / this.numFolds);
         results.setRecallAtK(cutoff, recallRes / this.numFolds);
         results.setNDCGAtK(cutoff, ndcgRes / this.numFolds);

         log.info("Ranking metrics at {} computed", cutoff);
         log.info("NDCG@" + cutoff + ": " + ndcgRes / this.numFolds + " / P@" + cutoff + ": " + precisionRes / this.numFolds
                 + " / R@" + cutoff + ": " + recallRes / this.numFolds);

      }

      results.setMAE(maeResult / this.numFolds);
      results.setRMSE(rmseResult / this.numFolds);
      log.info("Error metrics computed");
      log.info("RMSE: " + rmseResult / this.numFolds + " / MAE: " + maeResult / this.numFolds);

      return results;
   }
}
