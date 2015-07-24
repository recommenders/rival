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

import java.util.Map;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

/**
 *
 * @author Alejandro
 */
public class StatisticalSignificance {

    private Map<?, Double> baselineMetricPerDimension;
    private Map<?, Double> testMetricPerDimension;

    public StatisticalSignificance(Map<?, Double> baselineMetricPerDimension, Map<?, Double> testMetricPerDimension) {
        this.baselineMetricPerDimension = baselineMetricPerDimension;
        this.testMetricPerDimension = testMetricPerDimension;
    }

    /**
     * @param method one of "t", "pairedT", "wilcoxon"
     */
    public double getPValue(String method) {
        double p = Double.NaN;

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

        if ("t".equals(method)) {
            p = TestUtils.tTest(baselineValues, testValues);
        } else if ("pairedT".equals(method)) {
            p = TestUtils.pairedTTest(baselineValues, testValues);
        } else if ("wilcoxon".equals(method)) {
            p = new WilcoxonSignedRankTest().wilcoxonSignedRankTest(baselineValues, testValues, true);
        }

        return p;
    }
}
