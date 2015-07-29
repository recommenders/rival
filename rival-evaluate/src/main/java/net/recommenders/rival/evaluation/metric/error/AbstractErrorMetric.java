package net.recommenders.rival.evaluation.metric.error;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.AbstractMetric;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <U> - type associated to users' ids
 * @param <I> - type associated to items' ids
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public abstract class AbstractErrorMetric<U, I> extends AbstractMetric<U, I> implements EvaluationMetric<U> {

    /**
     * Type of error strategy: what to do when there is no predicted rating but
     * there is groundtruth information
     */
    public static enum ErrorStrategy {

        CONSIDER_EVERYTHING,
        NOT_CONSIDER_NAN,
        CONSIDER_NAN_AS_0,
        CONSIDER_NAN_AS_1,
        CONSIDER_NAN_AS_3;
    }
    /**
     * Global value
     */
    protected double value;
    /**
     * For coverage
     */
    protected int emptyUsers;
    /**
     * For coverage
     */
    protected int emptyItems;
    /**
     * Strategy to decide what to do when there is no predicted value for a user
     * and item contained in the test set
     */
    protected ErrorStrategy strategy;

    /**
     * Default constructor with predictions and groundtruth information
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public AbstractErrorMetric(DataModel<U, I> predictions, DataModel<U, I> test) {
        this(predictions, test, ErrorStrategy.NOT_CONSIDER_NAN);
    }

    /**
     * Constructor where the error strategy can be initialized
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     * @param strategy the error strategy
     */
    public AbstractErrorMetric(DataModel<U, I> predictions, DataModel<U, I> test, ErrorStrategy strategy) {
        super(predictions, test);

        this.value = Double.NaN;
        this.strategy = strategy;
    }

    /**
     * Method that transforms the user data from pairs of (item, score) into
     * lists of differences, by using groundtruth information.
     *
     * @return a map with the transformed data, one list per user
     */
    public Map<U, List<Double>> processDataAsPredictedDifferencesToTest() {
        Map<U, List<Double>> data = new HashMap<U, List<Double>>();
        Map<U, Map<I, Double>> actualRatings = test.getUserItemPreferences();
        Map<U, Map<I, Double>> predictedRatings = predictions.getUserItemPreferences();

        emptyItems = 0;
        emptyUsers = 0;

        for (U testUser : test.getUsers()) {
            Map<I, Double> ratings = actualRatings.get(testUser);
            List<Double> userData = data.get(testUser);
            if (userData == null) {
                userData = new ArrayList<Double>();
                data.put(testUser, userData);
            }
            for (I testItem : ratings.keySet()) {
                double realRating = ratings.get(testItem);
                double predictedRating = Double.NaN; // NaN as default value
                if (predictedRatings.containsKey(testUser)) {
                    if (predictedRatings.get(testUser).containsKey(testItem)) {
                        predictedRating = predictedRatings.get(testUser).get(testItem);
                    } else {
                        emptyItems++;
                    }
                } else {
                    emptyUsers++;
                }
                // get estimated preference depending on the ErrorStrategy
                predictedRating = considerEstimatedPreference(strategy, predictedRating);
                // if returned value is NaN, then we ignore the predicted rating
                if (!Double.isNaN(predictedRating)) {
                    double difference = realRating - predictedRating;
                    userData.add(difference);
                }
            }
        }
        return data;
    }

    /**
     * Method that returns an estimated preference according to a given value
     * and an error strategy
     *
     * @param strategy the error strategy
     * @param recValue the predicted value by the recommender
     * @return an estimated preference according to the provided strategy
     */
    public static double considerEstimatedPreference(ErrorStrategy strategy, double recValue) {
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
        return value;
    }
}
