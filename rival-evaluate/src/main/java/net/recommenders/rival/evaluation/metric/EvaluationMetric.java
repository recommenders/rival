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
package net.recommenders.rival.evaluation.metric;

import java.util.Map;

/**
 * An evaluation metric expressing the quality of the evaluated system.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <V> generic type for users
 */
public interface EvaluationMetric<V> {

    /**
     * Get the overall value of the metric.
     *
     * @return The overall value of the metric.
     */
    double getValue();

    /**
     * Get the value of the metric on a per-user basis.
     *
     * @return Map containing the values per user.
     */
    Map<V, Double> getValuePerUser();

    /**
     * Get the value of the metric for a specific user.
     *
     * @param u user whose metric value will be computed
     *
     * @return a value for user u.
     */
    double getValue(V u);

    /**
     * Computes the evaluation metric. This method should be called
     * <b>before</b> asking for the values of the metric.
     */
    void compute();
}
