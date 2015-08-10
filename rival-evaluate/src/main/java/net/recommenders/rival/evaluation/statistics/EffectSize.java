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
 * Class used to compute the effect size of an algorithm with respect to the
 * baseline.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <V> generic type for users
 */
public class EffectSize<V> {

    /**
     * Baseline metric for each dimension (users).
     */
    private Map<V, Double> baselineMetricPerDimension;
    /**
     * Test metric for each dimension (users).
     */
    private Map<V, Double> testMetricPerDimension;

    /**
     * Default constructor.
     *
     * @param theBaselineMetricPerDimension map for the baseline method, one
     * value for each user (dimension)
     * @param theTestMetricPerDimension map for the test method, one value for
     * each user (dimension)
     */
    public EffectSize(final Map<V, Double> theBaselineMetricPerDimension, final Map<V, Double> theTestMetricPerDimension) {
        this.baselineMetricPerDimension = theBaselineMetricPerDimension;
        this.testMetricPerDimension = theTestMetricPerDimension;
    }

    /**
     * Computes the effect size according to different methods: d and dLS are
     * adequate for not paired samples, whereas pairedT is better for paired
     * samples.
     *
     * @param method one of "d", "dLS", "pairedT"
     * @return the effect size
     */
    public double getEffectSize(final String method) {
        if ("d".equals(method)) {
            return getCohenD(baselineMetricPerDimension, testMetricPerDimension, false);
        } else if ("dLS".equals(method)) {
            return getCohenD(baselineMetricPerDimension, testMetricPerDimension, true);
        } else if ("pairedT".equals(method)) {
            return getEffectSizePairedT(baselineMetricPerDimension, testMetricPerDimension);
        }
        return Double.NaN;
    }

    /**
     * Computes Cohen's d, either the classical formulation (dividing the pooled
     * standard deviation by the sum of the number of samples) or using the
     * least squares estimation (substracting 2 to the sum of the number of
     * samples when normalizing the pooled standard deviation).
     *
     * @param <V> type of the keys of each map.
     * @param baselineMetricPerDimension map for the baseline method, one value
     * for each user (dimension)
     * @param testMetricPerDimension map for the test method, one value for each
     * user (dimension)
     * @param doLeastSquares flag to use one formulation or the other (see
     * description above)
     * @return the computed Cohen's d as estimation of the effect size..
     */
    public static <V> double getCohenD(final Map<V, Double> baselineMetricPerDimension, final Map<V, Double> testMetricPerDimension, final boolean doLeastSquares) {
        SummaryStatistics statsBaseline = new SummaryStatistics();
        for (double d : baselineMetricPerDimension.values()) {
            statsBaseline.addValue(d);
        }
        SummaryStatistics statsTest = new SummaryStatistics();
        for (double d : testMetricPerDimension.values()) {
            statsTest.addValue(d);
        }
        if (doLeastSquares) {
            return getCohenDLeastSquares(
                    (int) statsBaseline.getN(), statsBaseline.getMean(), statsBaseline.getStandardDeviation(),
                    (int) statsTest.getN(), statsTest.getMean(), statsTest.getStandardDeviation());
        }
        return getCohenD(
                (int) statsBaseline.getN(), statsBaseline.getMean(), statsBaseline.getStandardDeviation(),
                (int) statsTest.getN(), statsTest.getMean(), statsTest.getStandardDeviation());
    }

    /**
     * Original Cohen's d formulation, as in Cohen (1988), Statistical power
     * analysis for the behavioral sciences.
     *
     * @param <V> type of the keys of each map.
     * @param baselineN number of samples of baseline method.
     * @param baselineMean mean of baseline method.
     * @param baselineStd standard deviation of baseline method.
     * @param testN number of samples of test method.
     * @param testMean mean of test method.
     * @param testStd standard deviation of test method.
     * @return Cohen's d without least squares estimation.
     */
    public static <V> double getCohenD(final int baselineN, final double baselineMean, final double baselineStd, final int testN, final double testMean, final double testStd) {
        double pooledStd = Math.sqrt(((testN - 1) * Math.pow(testStd, 2) + (baselineN - 1) * Math.pow(baselineStd, 2)) / (baselineN + testN));

        double d = Math.abs(testMean - baselineMean) / pooledStd;
        return d;
    }

    /**
     *
     * Least squares estimator of Cohen's d as described in McGrath and Meyer,
     * When effect sizes disagree: the case of r and d. 2006. Psychological
     * Methods, 11 (4)
     *
     * @param <V> type of the keys of each map.
     * @param baselineN number of samples of baseline method.
     * @param baselineMean mean of baseline method.
     * @param baselineStd standard deviation of baseline method.
     * @param testN number of samples of test method.
     * @param testMean mean of test method.
     * @param testStd standard deviation of test method.
     * @return Cohen's d with least squares estimation.
     */
    public static <V> double getCohenDLeastSquares(final int baselineN, final double baselineMean, final double baselineStd, final int testN, final double testMean, final double testStd) {
        double pooledStd = Math.sqrt(((testN - 1) * Math.pow(testStd, 2) + (baselineN - 1) * Math.pow(baselineStd, 2)) / (baselineN + testN - 2));

        double d = Math.abs(testMean - baselineMean) / pooledStd;
        return d;
    }

    /**
     *
     * Estimation of effect size based on the distribution of score differences
     * (from paired samples).
     *
     * @param <V> type of the keys of each map.
     * @param baselineMetricPerDimension map for the baseline method, one value
     * for each user (dimension)
     * @param testMetricPerDimension map for the test method, one value for each
     * user (dimension)
     * @return the effect size.
     */
    public static <V> double getEffectSizePairedT(final Map<V, Double> baselineMetricPerDimension, final Map<V, Double> testMetricPerDimension) {
        Set<V> overlap = new HashSet<V>(baselineMetricPerDimension.keySet());
        overlap.retainAll(testMetricPerDimension.keySet());

        SummaryStatistics differences = new SummaryStatistics();
        for (V key : overlap) {
            double diff = testMetricPerDimension.get(key) - baselineMetricPerDimension.get(key);
            differences.addValue(diff);
        }

        return getEffectSizePairedT(differences.getMean(), Math.sqrt(differences.getVariance()));
    }

    /**
     *
     * Returns the ratio between the mean and the standard deviation, assuming
     * these values come from a distribution of differences of scores.
     *
     * @param meanOfDifferences the mean of the distribution.
     * @param stdOfDifferences the standard deviation of the distribution.
     * @return the ratio between these values (absolute value of the mean is
     * considered).
     */
    public static double getEffectSizePairedT(final double meanOfDifferences, final double stdOfDifferences) {
        double e = Math.abs(meanOfDifferences) / stdOfDifferences;
        return e;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "EffectSize";
    }
}
