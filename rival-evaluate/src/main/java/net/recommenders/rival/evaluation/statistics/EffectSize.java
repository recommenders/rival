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
 * @author Alejandro
 */
public class EffectSize<V> {

    private Map<V, Double> baselineMetricPerDimension;
    private Map<V, Double> testMetricPerDimension;

    public EffectSize(Map<V, Double> baselineMetricPerDimension, Map<V, Double> testMetricPerDimension) {
        this.baselineMetricPerDimension = baselineMetricPerDimension;
        this.testMetricPerDimension = testMetricPerDimension;
    }

    /**
     * @param method one of "d", "pairedT"
     */
    public double getEffectSize(String method) {
        if ("d".equals(method)) {
            return getCohenD(baselineMetricPerDimension, testMetricPerDimension, false);
        } else if ("dLS".equals(method)) {
            return getCohenD(baselineMetricPerDimension, testMetricPerDimension, true);
        } else if ("pairedT".equals(method)) {
            return getEffectSizePairedT(baselineMetricPerDimension, testMetricPerDimension);
        }
        return Double.NaN;
    }

    public static <V> double getCohenD(Map<V, Double> baselineMetricPerDimension, Map<V, Double> testMetricPerDimension, boolean doLeastSquares) {
        SummaryStatistics statsBaseline = new SummaryStatistics();
        for (double d : baselineMetricPerDimension.values()) {
            statsBaseline.addValue(d);
        }
        SummaryStatistics statsTest = new SummaryStatistics();
        for (double d : testMetricPerDimension.values()) {
            statsTest.addValue(d);
        }
        if (doLeastSquares) {
            return getCohenDLeastSquares((int) statsBaseline.getN(), statsBaseline.getMean(), statsBaseline.getStandardDeviation(), (int) statsTest.getN(), statsTest.getMean(), statsTest.getStandardDeviation());
        }
        return getCohenD((int) statsBaseline.getN(), statsBaseline.getMean(), statsBaseline.getStandardDeviation(), (int) statsTest.getN(), statsTest.getMean(), statsTest.getStandardDeviation());
    }

    /**
     * Original Cohen's d formulation, as in Cohen (1988), Statistical power
     * analysis for the behavioral sciences.
     *
     * @param <V>
     * @param baselineN
     * @param baselineMean
     * @param baselineStd
     * @param testN
     * @param testMean
     * @param testStd
     * @return
     */
    public static <V> double getCohenD(int baselineN, double baselineMean, double baselineStd, int testN, double testMean, double testStd) {
        double pooledStd = Math.sqrt(((testN - 1) * Math.pow(testStd, 2) + (baselineN - 1) * Math.pow(baselineStd, 2)) / (baselineN + testN));

        double d = Math.abs(testMean - baselineMean) / pooledStd;
        return d;
    }

    /**
     *
     * Least squares estimator of Cohen's d as described in McGrath & Meyer,
     * When effect sizes disagree: the case of r and d. 2006. Psychological
     * Methods, 11 (4)
     *
     * @param <V>
     * @param baselineN
     * @param baselineMean
     * @param baselineStd
     * @param testN
     * @param testMean
     * @param testStd
     * @return
     */
    public static <V> double getCohenDLeastSquares(int baselineN, double baselineMean, double baselineStd, int testN, double testMean, double testStd) {
        double pooledStd = Math.sqrt(((testN - 1) * Math.pow(testStd, 2) + (baselineN - 1) * Math.pow(baselineStd, 2)) / (baselineN + testN - 2));

        double d = Math.abs(testMean - baselineMean) / pooledStd;
        return d;
    }

    public static <V> double getEffectSizePairedT(Map<V, Double> baselineMetricPerDimension, Map<V, Double> testMetricPerDimension) {
        Set<V> overlap = new HashSet<V>(baselineMetricPerDimension.keySet());
        overlap.retainAll(testMetricPerDimension.keySet());

        SummaryStatistics differences = new SummaryStatistics();
        for (V key : overlap) {
            double diff = testMetricPerDimension.get(key) - baselineMetricPerDimension.get(key);
            differences.addValue(diff);
        }

        return getEffectSizePairedT(differences.getMean(), Math.sqrt(differences.getVariance()));
    }

    public static <V> double getEffectSizePairedT(double meanOfDifferences, double stdOfDifferences) {
        double e = Math.abs(meanOfDifferences) / stdOfDifferences;
        return e;
    }
}
