package net.recommenders.rival.evaluation.metric.error;

import java.util.List;
import net.recommenders.rival.core.DataModel;

import java.util.Map;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

/**
 * <a href="http://recsyswiki.com/wiki/RMSE" target="_blank">Root mean square
 * error</a> (RMSE) of a list of predicted ratings.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class RMSE extends AbstractErrorMetric implements EvaluationMetric<Long> {

    /**
     * @inheritDoc
     */
    public RMSE(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        super(predictions, test);
    }

    /**
     *
     * @inheritDoc
     */
    public RMSE(DataModel<Long, Long> predictions, DataModel<Long, Long> test, ErrorStrategy rmseStrategy) {
        super(predictions, test, rmseStrategy);
    }

    /**
     * Instantiates and computes the RMSE value. Prior to running this, there is
     * no valid value.
     *
     * @return The global RMSE
     */
    public void compute() {
        Map<Long, List<Double>> data = processDataAsPredictedDifferencesToTest();
        value = 0.0;
        int testItems = 0;
        for (long testUser : test.getUsers()) {
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
}
