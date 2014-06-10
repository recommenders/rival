package net.recommenders.rival.evaluation.metric.error;

import java.util.List;
import java.util.Map;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

/**
 * <a href="http://recsyswiki.com/wiki/RMSE" target="_blank">Mean absolute
 * error</a> (MAE) of a list of predicted ratings.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public class MAE extends AbstractErrorMetric implements EvaluationMetric<Long> {

    /**
     * Default constructor with predictions and groundtruth information
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public MAE(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        super(predictions, test);
    }

    /**
     * Constructor where the error strategy can be initialized
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     * @param errorStrategy the error strategy
     */
    public MAE(DataModel<Long, Long> predictions, DataModel<Long, Long> test, ErrorStrategy errorStrategy) {
        super(predictions, test, errorStrategy);
    }

    /**
     * Instantiates and computes the MAE value. Prior to running this, there is
     * no valid value.
     *
     */
    @Override
    public void compute() {
        if (!Double.isNaN(value)) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        Map<Long, List<Double>> data = processDataAsPredictedDifferencesToTest();
        value = 0.0;
        int testItems = 0;
        for (long testUser : test.getUsers()) {
            int userItems = 0;
            double ume = 0.0;

            if (data.containsKey(testUser)) {
                for (double difference : data.get(testUser)) {
                    ume += Math.abs(difference);
                    userItems++;
                }
            }

            testItems += userItems;
            value += ume;
            ume = (userItems == 0) ? Double.NaN : ume / userItems;
            metricPerUser.put(testUser, ume);
        }
        value = (testItems == 0) ? Double.NaN : value / testItems;
    }
}
