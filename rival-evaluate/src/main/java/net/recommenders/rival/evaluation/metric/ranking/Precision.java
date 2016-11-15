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
package net.recommenders.rival.evaluation.metric.ranking;

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.evaluation.Pair;

/**
 * Precision of a ranked list of items.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 *
 * @param <U> - type associated to users' ids
 * @param <I> - type associated to items' ids
 */
public class Precision<U, I> extends AbstractRankingMetric<U, I> implements EvaluationMetric<U> {

    /**
     * Precision values per user at each cutoff level.
     */
    private Map<Integer, Map<U, Double>> userPrecAtCutoff;

    /**
     * Default constructor with predictions and groundtruth information.
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public Precision(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test) {
        this(predictions, test, 1.0);
    }

    /**
     * Constructor where the relevance threshold can be initialized.
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     */
    public Precision(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test, final double relThreshold) {
        this(predictions, test, relThreshold, new int[]{});
    }

    /**
     * Constructor where the cutoff levels can be initialized.
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     * @param ats cutoffs
     */
    public Precision(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test, final double relThreshold, final int[] ats) {
        super(predictions, test, relThreshold, ats);
    }

    /**
     * Computes the global precision by first summing the precision for each
     * user and then averaging by the number of users.
     */
    @Override
    public void compute() {
        if (!Double.isNaN(getValue())) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        iniCompute();

        Map<U, List<Pair<I, Double>>> data = processDataAsRankedTestRelevance();
        userPrecAtCutoff = new HashMap<Integer, Map<U, Double>>();

        int nUsers = 0;
        for (Map.Entry<U, List<Pair<I, Double>>> e : data.entrySet()) {
            U user = e.getKey();
            List<Pair<I, Double>> sortedList = e.getValue();
            double uprec = 0.0;
            int rank = 0;
            for (Pair<I, Double> pair : sortedList) {
                double rel = pair.getSecond();
                rank++;
                uprec += computeBinaryPrecision(rel);
                // compute at a particular cutoff
                for (int at : getCutoffs()) {
                    if (rank == at) {
                        Map<U, Double> m = userPrecAtCutoff.get(at);
                        if (m == null) {
                            m = new HashMap<U, Double>();
                            userPrecAtCutoff.put(at, m);
                        }
                        m.put(user, uprec / rank);
                    }
                }
            }
            // DO NOT assign the precision of the whole list to those cutoffs larger than the list's size
            // instead, we fill with not relevant items until such cutoff
            for (int at : getCutoffs()) {
                if (rank <= at) {
                    Map<U, Double> m = userPrecAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<U, Double>();
                        userPrecAtCutoff.put(at, m);
                    }
                    m.put(user, uprec / at);
                }
            }
            // normalize by list size
            uprec /= rank;
            if (!Double.isNaN(uprec)) {
                setValue(getValue() + uprec);
                getMetricPerUser().put(user, uprec);
                nUsers++;
            }
        }
        setValue(getValue() / nUsers);
    }

    /**
     * Method to return the precision value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the precision corresponding to the requested cutoff level
     */
    @Override
    public double getValueAt(final int at) {
        if (userPrecAtCutoff.containsKey(at)) {
            int n = 0;
            double prec = 0.0;
            for (U u : userPrecAtCutoff.get(at).keySet()) {
                double uprec = getValueAt(u, at);
                if (!Double.isNaN(uprec)) {
                    prec += uprec;
                    n++;
                }
            }
            if (n == 0) {
                prec = 0.0;
            } else {
                prec = prec / n;
            }
            return prec;
        }
        return Double.NaN;
    }

    /**
     * Method to return the precision value at a particular cutoff level for a
     * given user.
     *
     * @param user the user
     * @param at cutoff level
     * @return the precision corresponding to the requested user at the cutoff
     * level
     */
    @Override
    public double getValueAt(final U user, final int at) {
        if (userPrecAtCutoff.containsKey(at) && userPrecAtCutoff.get(at).containsKey(user)) {
            return userPrecAtCutoff.get(at).get(user);
        }
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Precision_" + getRelevanceThreshold();
    }
}
