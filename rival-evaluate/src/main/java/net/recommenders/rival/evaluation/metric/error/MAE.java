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
     * Default constructor with predictions and groundtruth information.
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public MAE(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test) {
        super(predictions, test);
    }

    /**
     * Constructor where the error strategy can be initialized.
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     * @param errorStrategy the error strategy
     */
    public MAE(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test, final ErrorStrategy errorStrategy) {
        super(predictions, test, errorStrategy);
    }

    /**
     * Instantiates and computes the MAE value. Prior to running this, there is
     * no valid value.
     *
     */
    @Override
    public void compute() {
        if (!Double.isNaN(getValue())) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        iniCompute();

        Map<U, List<Double>> data = processDataAsPredictedDifferencesToTest();

        int testItems = 0;
        for (U testUser : getTest().getUsers()) {
            int userItems = 0;
            double ume = 0.0;

            if (data.containsKey(testUser)) {
                for (double difference : data.get(testUser)) {
                    ume += Math.abs(difference);
                    userItems++;
                }
            }

            testItems += userItems;
            setValue(getValue() + ume);
            if (userItems == 0) {
                ume = Double.NaN;
            } else {
                ume = ume / userItems;
            }
            getMetricPerUser().put(testUser, ume);
        }
        if (testItems == 0) {
            setValue(Double.NaN);
        } else {
            setValue(getValue() / testItems);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MAE_" + getStrategy();
    }
}
