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
package net.recommenders.rival.evaluation.statistics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 * Class used to compute the standard error of an algorithm with respect to the
 * baseline.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class StandardError<V> {

    private Map<V, Double> baselineMetricPerDimension;
    private Map<V, Double> testMetricPerDimension;

    /**
     * Default constructor
     *
     * @param baselineMetricPerDimension map for the baseline method, one value
     * for each user (dimension)
     * @param testMetricPerDimension map for the test method, one value for each
     * user (dimension)
     */
    public StandardError(Map<V, Double> baselineMetricPerDimension, Map<V, Double> testMetricPerDimension) {
        this.baselineMetricPerDimension = baselineMetricPerDimension;
        this.testMetricPerDimension = testMetricPerDimension;
    }

    /**
     * Implements equation (8.13) from "Elementary Statistics: A Problem Solving
     * Approach 4th Edition", Andrew L. Comrey, Howard B. Lee
     *
     * @return the standard error as the ratio of the standard deviation divided
     * by the sqrt(number of users) of the distribution of difference scores.
     */
    public double getStandardError() {
        Set<V> overlap = new HashSet<V>(baselineMetricPerDimension.keySet());
        overlap.retainAll(testMetricPerDimension.keySet());

        // paired or matched samples --> analyse distribution of difference scores
        SummaryStatistics differences = new SummaryStatistics();
        for (V key : overlap) {
            double diff = baselineMetricPerDimension.get(key) - testMetricPerDimension.get(key);
            differences.addValue(diff);
        }

        double e = differences.getStandardDeviation() / Math.sqrt(differences.getN());
        return e;
    }

    @Override
    public String toString() {
        return "StandardError";
    }
}
