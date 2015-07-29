package net.recommenders.rival.evaluation.metric.error;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

import java.util.List;
import java.util.Map;

/**
 * <a href="http://recsyswiki.com/wiki/RMSE" target="_blank">Root mean square
 * error</a> (RMSE) of a list of predicted ratings.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class RMSE<U, I> extends AbstractErrorMetric<U, I> implements EvaluationMetric<U> {

    /**
     * Default constructor with predictions and groundtruth information
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public RMSE(DataModel<U, I> predictions, DataModel<U, I> test) {
        super(predictions, test);
    }

    /**
     * Constructor where the error strategy can be initialized
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     * @param errorStrategy the error strategy
     */
    public RMSE(DataModel<U, I> predictions, DataModel<U, I> test, ErrorStrategy errorStrategy) {
        super(predictions, test, errorStrategy);
    }

    /**
     * Instantiates and computes the RMSE value. Prior to running this, there is
     * no valid value.
     *
     */
    @Override
    public void compute() {
        if (!Double.isNaN(value)) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        Map<U, List<Double>> data = processDataAsPredictedDifferencesToTest();
        value = 0.0;
        int testItems = 0;
        for (U testUser : test.getUsers()) {
            int userItems = 0;
            double umse = 0.0;

            if (data.containsKey(testUser)) {
                for (double difference : data.get(testUser)) {
                    umse += difference * difference;
                    userItems++;
                }
            }

            testItems += userItems;
            value += umse;
            umse = (userItems == 0) ? Double.NaN : Math.sqrt(umse / userItems);
            metricPerUser.put(testUser, umse);
        }
        value = (testItems == 0) ? Double.NaN : Math.sqrt(value / testItems);
    }

    @Override
    public String toString() {
        return "RMSE_" + strategy;
    }
}
