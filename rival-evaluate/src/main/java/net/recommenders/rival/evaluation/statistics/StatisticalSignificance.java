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
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

/**
 *
 * Class used to compute statistical significance methods, such as t's Student
 * (paired or not) and Wilcoxon.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class StatisticalSignificance {

    /**
     * Baseline metric for each dimension (users).
     */
    private Map<?, Double> baselineMetricPerDimension;
    /**
     * Test metric for each dimension (users).
     */
    private Map<?, Double> testMetricPerDimension;
    /**
     * Users.
     */
    private Set<?> dimensions;

    /**
     * Default constructor.
     *
     * @param theBaselineMetricPerDimension map for the baseline method, one
     * value for each user (dimension)
     * @param theTestMetricPerDimension map for the test method, one value for
     * each user (dimension)
     */
    public StatisticalSignificance(final Map<?, Double> theBaselineMetricPerDimension, final Map<?, Double> theTestMetricPerDimension) {
        this.baselineMetricPerDimension = theBaselineMetricPerDimension;
        this.testMetricPerDimension = theTestMetricPerDimension;
        this.dimensions = new HashSet<>(baselineMetricPerDimension.keySet());
        this.dimensions.retainAll(testMetricPerDimension.keySet());
    }

    /**
     * Gets the p-value according to the requested method.
     *
     * @param method one of "t", "pairedT", "wilcoxon"
     * @return the p-value according to the requested method
     */
    public double getPValue(final String method) {
        double p = Double.NaN;

        if ("t".equals(method)) {
            double[] baselineValues = new double[baselineMetricPerDimension.values().size()];
            int i = 0;
            for (Double d : baselineMetricPerDimension.values()) {
                baselineValues[i] = d;
                i++;
            }

            double[] testValues = new double[testMetricPerDimension.values().size()];
            i = 0;
            for (Double d : testMetricPerDimension.values()) {
                testValues[i] = d;
                i++;
            }

            p = TestUtils.tTest(baselineValues, testValues);
        } else {
            double[] baselineValues = new double[dimensions.size()];
            int i = 0;
            for (Object d : dimensions) {
                baselineValues[i] = baselineMetricPerDimension.get(d);
                i++;
            }

            double[] testValues = new double[dimensions.size()];
            i = 0;
            for (Object d : dimensions) {
                testValues[i] = testMetricPerDimension.get(d);
                i++;
            }
            if ("pairedT".equals(method)) {
                p = TestUtils.pairedTTest(baselineValues, testValues);
            } else if ("wilcoxon".equals(method)) {
                p = new WilcoxonSignedRankTest().wilcoxonSignedRankTest(baselineValues, testValues, false);
            }
        }
        return p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "StatisticalSignificance";
    }
}
