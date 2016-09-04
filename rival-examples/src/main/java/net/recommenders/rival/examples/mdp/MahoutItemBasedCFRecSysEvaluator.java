package net.recommenders.rival.examples.mdp;

import org.apache.commons.lang.NotImplementedException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.AbstractItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

public class MahoutItemBasedCFRecSysEvaluator extends CrossValidationRecSysEvaluator {

   public enum Distance {PEARSON, EUCLIDEAN, TANIMOTO}

   private Distance distance;

   public MahoutItemBasedCFRecSysEvaluator(int numFolds, double relevanceThreshold, Distance distance) {
      super(numFolds, relevanceThreshold);
      this.distance = distance;
   }

   @Override
   protected Recommender buildRecommender(DataModel trainingModel) throws TasteException {

      AbstractItemSimilarity similarity;

      switch (this.distance) {
         case EUCLIDEAN:
            similarity = new EuclideanDistanceSimilarity(trainingModel);
            break;
         case TANIMOTO:
            similarity = new TanimotoCoefficientSimilarity(trainingModel);
            break;
         case PEARSON:
            similarity = new PearsonCorrelationSimilarity(trainingModel);
            break;
         default:
            throw new NotImplementedException("Unknown distance specified!");
      }

      return new GenericItemBasedRecommender(trainingModel, similarity);
   }
}
