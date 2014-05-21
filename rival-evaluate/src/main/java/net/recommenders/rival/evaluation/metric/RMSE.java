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

    /**
     * Global RMSE
     */
    private double rmse;

    /**
     * @inheritDoc
     */
    public RMSE(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        super(predictions, test);

        this.rmse = Double.NaN;
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
                double predictedRating = 0.0;
                if (actualRatings.containsKey(testUser)) {
                    if (actualRatings.get(testUser).containsKey(testItem)) {
                        predictedRating = predictedRatings.get(testUser).get(testItem);
                    } else {
                        emptyItems++;
                        continue;
                    }
                } else {
                    emptyUsers++;
                    continue;
                }
                difference = realRating - predictedRating;
                umse += difference * difference;
            }
            testItems += userItems;
            rmse += umse;
            metricPerUser.put(testUser, Math.sqrt(umse / userItems));
        }
        rmse = Math.sqrt(rmse / testItems);
    }

    /**
     * @inheritDoc
     */
    @Override
    public double getValue() {
        return rmse;
    }
}
