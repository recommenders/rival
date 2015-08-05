package net.recommenders.rival.evaluation.metric.error;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

import java.util.List;
import java.util.Map;

/**
 * <a href="http://recsyswiki.com/wiki/RMSE" target="_blank">Mean absolute
 * error</a> (MAE) of a list of predicted ratings.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 *
 * @param <U> - type associated to users' ids
 * @param <I> - type associated to items' ids
 */
public class MAE<U, I> extends AbstractErrorMetric<U, I> implements EvaluationMetric<U> {

    /**
     * Default constructor with predictions and groundtruth information
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public MAE(DataModel<U, I> predictions, DataModel<U, I> test) {
        super(predictions, test);
    }

    /**
     * Constructor where the error strategy can be initialized
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     * @param errorStrategy the error strategy
     */
    public MAE(DataModel<U, I> predictions, DataModel<U, I> test, ErrorStrategy errorStrategy) {
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
        Map<U, List<Double>> data = processDataAsPredictedDifferencesToTest();
        value = 0.0;
        int testItems = 0;
        for (U testUser : test.getUsers()) {
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

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return "MAE_" + strategy;
    }
}
