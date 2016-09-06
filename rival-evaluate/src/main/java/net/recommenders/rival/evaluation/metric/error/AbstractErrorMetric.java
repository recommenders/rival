/*
 * Copyright 2015 recommenders.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.recommenders.rival.evaluation.metric.error;

import net.recommenders.rival.core.DataModelIF;
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
     * there is groundtruth information.
     */
    public static enum ErrorStrategy {

        /**
         * Consider every rating (also those with no prediction).
         */
        CONSIDER_EVERYTHING,
        /**
         * Only consider valid predictions.
         */
        NOT_CONSIDER_NAN,
        /**
         * Take unpredicted values as 0.
         */
        CONSIDER_NAN_AS_0,
        /**
         * Take unpredicted values as 1.
         */
        CONSIDER_NAN_AS_1,
        /**
         * Take unpredicted values as 3.
         */
        CONSIDER_NAN_AS_3;
    }
    /**
     * For coverage.
     */
    private int emptyUsers;
    /**
     * For coverage.
     */
    private int emptyItems;
    /**
     * Strategy to decide what to do when there is no predicted value for a user
     * and item contained in the test set.
     */
    private ErrorStrategy strategy;

    /**
     * Default constructor with predictions and groundtruth information.
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public AbstractErrorMetric(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test) {
        this(predictions, test, ErrorStrategy.NOT_CONSIDER_NAN);
    }

    /**
     * Constructor where the error strategy can be initialized.
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     * @param errorStrategy the error strategy
     */
    public AbstractErrorMetric(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test, final ErrorStrategy errorStrategy) {
        super(predictions, test);

        setValue(Double.NaN);
        this.strategy = errorStrategy;
    }

    /**
     * Gets the error strategy. See {@link ErrorStrategy}.
     *
     * @return the error strategy
     */
    protected ErrorStrategy getStrategy() {
        return strategy;
    }

    /**
     * Method that transforms the user data from pairs of (item, score) into
     * lists of differences, by using groundtruth information.
     *
     * @return a map with the transformed data, one list per user
     */
    public Map<U, List<Double>> processDataAsPredictedDifferencesToTest() {
        Map<U, List<Double>> data = new HashMap<U, List<Double>>();

        emptyItems = 0;
        emptyUsers = 0;

        for (U testUser : getTest().getUsers()) {
            List<Double> userData = data.get(testUser);
            if (userData == null) {
                userData = new ArrayList<Double>();
                data.put(testUser, userData);
            }
            for (I testItem : getTest().getUserItems(testUser)) {
                double realRating = getTest().getUserItemPreference(testUser, testItem);
                double predictedRating = Double.NaN; // NaN as default value
                if (getPredictions().getUserItems(testUser) != null) {
                    predictedRating = getPredictions().getUserItemPreference(testUser, testItem);
                    if (Double.isNaN(predictedRating)) {
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
     * and an error strategy.
     *
     * @param errorStrategy the error strategy
     * @param recValue the predicted value by the recommender
     * @return an estimated preference according to the provided strategy
     */
    public static double considerEstimatedPreference(final ErrorStrategy errorStrategy, final double recValue) {
        boolean consider = true;
        double v = recValue;
        switch (errorStrategy) {
            default:
            case CONSIDER_EVERYTHING:
                break;
            case NOT_CONSIDER_NAN:
                consider = !Double.isNaN(recValue);
                break;
            case CONSIDER_NAN_AS_0:
                if (Double.isNaN(recValue)) {
                    v = 0.0;
                }
                break;
            case CONSIDER_NAN_AS_1:
                if (Double.isNaN(recValue)) {
                    v = 1.0;
                }
                break;
            case CONSIDER_NAN_AS_3:
                if (Double.isNaN(recValue)) {
                    v = 3.0;
                }
                break;
        }
        if (consider) {
            return v;
        } else {
            return Double.NaN;
        }
    }
}
