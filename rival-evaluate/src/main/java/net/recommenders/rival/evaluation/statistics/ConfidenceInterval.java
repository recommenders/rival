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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Class used to compute confidence intervals. Methods are implemented as
 * suggested in Tetsuya Sakai. 2014. Statistical reform in information
 * retrieval?. SIGIR Forum 48, 1 (June 2014), 3-12. DOI=10.1145/2641383.2641385
 * http://doi.acm.org/10.1145/2641383.2641385
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class ConfidenceInterval {

    /**
     * Method that takes an array of metrics as parameters. The null hypothesis
     * is that all the systems are equivalent.
     *
     * @param <V> type of keys for metrics
     * @param alpha probability of incorrectly rejecting the null hypothesis (1
     * - confidence_level)
     * @param metricValuesPerDimension array of metrics, one for each system to
     * be compared
     * @return array with the confidence intervals: for each system provided, a
     * confidence interval is computed
     */
    public <V> double[][] getConfidenceInterval(final double alpha, final Map<V, Double>[] metricValuesPerDimension) {
        Map<Integer, Double> systemMeans = new HashMap<Integer, Double>();
        Map<V, Double> dimensionSum = new HashMap<V, Double>();
        Map<V, Integer> dimensionN = new HashMap<V, Integer>();
        Set<V> dimensions = new HashSet<V>();
        double grandSum = 0.0;
        int grandN = 0;
        for (int i = 0; i < metricValuesPerDimension.length; i++) {
            Map<V, Double> m = metricValuesPerDimension[i];
            dimensions.addAll(m.keySet());
            int n = 0;
            double sum = 0.0;
            for (Entry<V, Double> e : m.entrySet()) {
                V k = e.getKey();
                double d = e.getValue();
                sum += d;
                n++;
                grandSum += d;
                grandN++;
                if (!dimensionN.containsKey(k)) {
                    dimensionN.put(k, 0);
                }
                dimensionN.put(k, dimensionN.get(k) + 1);
                if (!dimensionSum.containsKey(k)) {
                    dimensionSum.put(k, 0.0);
                }
                dimensionSum.put(k, dimensionSum.get(k) + d);
            }
            systemMeans.put(i, sum / n);
        }
        double grandMean = grandSum / grandN;

        double sT = 0.0, sA = 0.0, sB = 0.0;
        for (int i = 0; i < metricValuesPerDimension.length; i++) {
            Map<V, Double> m = metricValuesPerDimension[i];
            for (double d : m.values()) {
                double dd = d - grandMean;
                sT += dd * dd;
            }
            sA += (systemMeans.get(i) - grandMean) * (systemMeans.get(i) - grandMean);
        }
        sA *= dimensions.size();
        for (V v : dimensions) {
            double m = dimensionSum.get(v) / dimensionN.get(v);
            sB += (m - grandMean) * (m - grandMean);
        }
        sB *= systemMeans.size();
        double sE = sT - sA - sB;
        int phiA = systemMeans.size() - 1;
        int phiB = dimensions.size() - 1;
        int phiE = phiA * phiB;
        double vE = sE / phiE;

        double[][] intervals = new double[metricValuesPerDimension.length][];
        for (int i = 0; i < metricValuesPerDimension.length; i++) {
            double[] systemCI = getConfidenceInterval(alpha, phiB, phiB + 1, Math.sqrt(vE), systemMeans.get(i));
            intervals[i] = systemCI;
        }
        return intervals;
    }

    /**
     * Method that takes two metrics as parameters. It will compute the
     * differences between both (only considering the keys in the overlap)
     *
     * @param <V> type of keys for metrics
     * @param alpha probability of incorrectly rejecting the null hypothesis (1
     * - confidence_level)
     * @param baselineMetricPerDimension baseline metric, one value for each
     * dimension
     * @param testMetricPerDimension test metric, one value for each dimension
     * @param pairedSamples flag to indicate if the comparison should be made
     * for the distribution of difference scores (when true) or for the
     * distribution of differences between means
     * @return array with the confidence interval: [mean - margin of error, mean
     * + margin of error]
     */
    public <V> double[] getConfidenceInterval(final double alpha, final Map<V, Double> baselineMetricPerDimension, final Map<V, Double> testMetricPerDimension, final boolean pairedSamples) {
        if (pairedSamples) {
            Set<V> overlap = new HashSet<V>(baselineMetricPerDimension.keySet());
            overlap.retainAll(testMetricPerDimension.keySet());

            // paired or matched samples --> analyse distribution of difference scores
            SummaryStatistics differences = new SummaryStatistics();
            for (V key : overlap) {
                double diff = Math.abs(testMetricPerDimension.get(key) - baselineMetricPerDimension.get(key));
                differences.addValue(diff);
            }
            return getConfidenceInterval(alpha / 2, (int) differences.getN() - 1, (int) differences.getN(), differences.getStandardDeviation(), differences.getMean());
        } else {
            // independent samples --> analyse distribution of differences between means
            SummaryStatistics statsBaseline = new SummaryStatistics();
            for (double d : baselineMetricPerDimension.values()) {
                statsBaseline.addValue(d);
            }
            SummaryStatistics statsTest = new SummaryStatistics();
            for (double d : testMetricPerDimension.values()) {
                statsTest.addValue(d);
            }
            long dfT = statsBaseline.getN() + statsTest.getN() - 2;
            double sDif = Math.sqrt((1.0 / statsBaseline.getN() + 1.0 / statsTest.getN())
                    * (statsBaseline.getVariance()
                    * (statsBaseline.getN() - 1) + statsTest.getVariance()
                    * (statsTest.getN() - 1)));
            double mDif = Math.abs(statsTest.getMean() - statsBaseline.getMean());
            return getConfidenceInterval(alpha, (int) dfT, (int) dfT, sDif, mDif);
        }
    }

    /**
     * Method that takes only one metric as parameter. It is useful when
     * comparing more than two metrics (so that a confidence interval is
     * computed for each of them), as suggested in [Sakai, 2014]
     *
     * @param alpha probability of incorrectly rejecting the null hypothesis (1
     * - confidence_level)
     * @param metricValuesPerDimension one value of the metric for each
     * dimension
     * @return array with the confidence interval: [mean - margin of error, mean
     * + margin of error]
     */
    public double[] getConfidenceInterval(final double alpha, final Map<?, Double> metricValuesPerDimension) {
        SummaryStatistics differences = new SummaryStatistics();
        for (Double d : metricValuesPerDimension.values()) {
            differences.addValue(d);
        }
        return getConfidenceInterval(alpha, (int) differences.getN() - 1, (int) differences.getN(), differences.getStandardDeviation(), differences.getMean());
    }

    /**
     * Adapted from https://gist.github.com/gcardone/5536578.
     *
     * @param alpha probability of incorrectly rejecting the null hypothesis (1
     * - confidence_level)
     * @param df degrees of freedom
     * @param n number of observations
     * @param std standard deviation
     * @param mean mean
     * @return array with the confidence interval: [mean - margin of error, mean
     * + margin of error]
     */
    public static double[] getConfidenceInterval(final double alpha, final int df, final int n, final double std, final double mean) {
        // Create T Distribution with df degrees of freedom
        TDistribution tDist = new TDistribution(df);
        // Calculate critical value
        double critVal = tDist.inverseCumulativeProbability(1.0 - alpha);
        // Calculate confidence interval
        double ci = critVal * std / Math.sqrt(n);
        double lower = mean - ci;
        double upper = mean + ci;
        double[] interval = new double[]{lower, upper};
        return interval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ConfidenceInterval";
    }
}
