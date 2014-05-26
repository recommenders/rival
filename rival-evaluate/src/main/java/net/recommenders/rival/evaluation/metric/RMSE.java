package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModel;

import java.util.Map;

/**
 * <a href="http://recsyswiki.com/wiki/RMSE" target="_blank">Root mean square
 * error</a> (RMSE) of a list of predicted ratings.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class RMSE extends AbstractMetric implements EvaluationMetric<Long> {

    public static enum RMSEStrategy {

        CONSIDER_EVERYTHING,
        NOT_CONSIDER_NAN,
        CONSIDER_NAN_AS_0,
        CONSIDER_NAN_AS_1,
        CONSIDER_NAN_AS_3;
    }
    /**
     * Global RMSE
     */
    private double rmse;
    /**
     * Strategy to decide what to do when there is no predicted value for a 
     * user and item contained in the test set
     */
    private RMSEStrategy rmseStrategy;

    /**
     * @inheritDoc
     */
    public RMSE(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        this(predictions, test, RMSEStrategy.NOT_CONSIDER_NAN);
    }

    /**
     *
     * @param predictions
     * @param test
     * @param rmseStrategy
     */
    public RMSE(DataModel<Long, Long> predictions, DataModel<Long, Long> test, RMSEStrategy rmseStrategy) {
        super(predictions, test);

        this.rmse = Double.NaN;
        this.rmseStrategy = rmseStrategy;
    }

    /**
     * Instantiates and computes the RMSE value. Prior to running this, there is
     * no valid RMSE value.
     *
     * @return The global RMSE
     */
    public void compute() {
        Map<Long, Map<Long, Double>> actualRatings = test.getUserItemPreferences();
        Map<Long, Map<Long, Double>> predictedRatings = predictions.getUserItemPreferences();
        int testItems = 0;
        rmse = 0.0;
        int emptyUsers = 0; // for coverage
        int emptyItems = 0; // for coverage

        for (long testUser : test.getUsers()) {
            Map<Long, Double> ratings = actualRatings.get(testUser);
            int userItems = 0;
            double umse = 0.0;
            for (long testItem : ratings.keySet()) {
                double difference = 0.0;
                double realRating = ratings.get(testItem);
                double predictedRating = Double.NaN; // NaN as default value
                if (actualRatings.containsKey(testUser)) {
                    if (actualRatings.get(testUser).containsKey(testItem)) {
                        predictedRating = predictedRatings.get(testUser).get(testItem);
                    } else {
                        emptyItems++;
//                        continue; // we can delete this because the method considerEstimatedPreference already deals with the other cases
                    }
                } else {
                    emptyUsers++;
//                        continue; // we can delete this because the method considerEstimatedPreference already deals with the other cases
                }
                // get estimated preference depending on the RMSEstrategy
                predictedRating = considerEstimatedPreference(rmseStrategy, predictedRating);
                // if returned value is NaN, then we ignore the predicted rating
                if (!Double.isNaN(predictedRating)) {
                    difference = realRating - predictedRating;
                    umse += difference * difference;
                    userItems++;
                }
            }
            testItems += userItems;
            rmse += umse;
            umse = (userItems == 0) ? Double.NaN : Math.sqrt(umse / userItems);
            metricPerUser.put(testUser, umse);
        }
        rmse = (testItems == 0) ? Double.NaN : Math.sqrt(rmse / testItems);
    }

    public static double considerEstimatedPreference(RMSEStrategy strategy, double recValue) {
        boolean consider = true;
        switch (strategy) {
            case CONSIDER_EVERYTHING:
                break;
            case NOT_CONSIDER_NAN:
                consider = !Double.isNaN(recValue);
                break;
            case CONSIDER_NAN_AS_0:
                if (Double.isNaN(recValue)) {
                    recValue = 0.0;
                }
                break;
            case CONSIDER_NAN_AS_1:
                if (Double.isNaN(recValue)) {
                    recValue = 1.0;
                }
                break;
            case CONSIDER_NAN_AS_3:
                if (Double.isNaN(recValue)) {
                    recValue = 3.0;
                }
                break;
        }
        if (consider) {
            return recValue;
        } else {
            return Double.NaN;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public double getValue() {
        return rmse;
    }
}
