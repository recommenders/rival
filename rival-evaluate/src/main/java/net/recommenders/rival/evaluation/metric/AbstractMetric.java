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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.recommenders.rival.core.DataModelIF;

/**
 * Abstract class for evaluation metrics.
 *
 * @param <U> - type associated to users' ids
 * @param <I> - type associated to items' ids
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public abstract class AbstractMetric<U, I> implements EvaluationMetric<U> {

    /**
     * The predictions.
     */
    private DataModelIF<U, I> predictions;
    /**
     * The test set.
     */
    private DataModelIF<U, I> test;
    /**
     * Metric per user.
     */
    private Map<U, Double> metricPerUser;
    /**
     * Global value.
     */
    private double value;

    /**
     * Default constructor with predictions and groundtruth information.
     *
     * @param thePredictions predicted scores for users and items
     * @param theTest groundtruth information for users and items
     */
    public AbstractMetric(final DataModelIF<U, I> thePredictions, final DataModelIF<U, I> theTest) {
        this.predictions = thePredictions;
        this.test = theTest;
        this.metricPerUser = null;
    }

    /**
     * Gets the predictions.
     *
     * @return the predictions
     */
    protected DataModelIF<U, I> getPredictions() {
        return predictions;
    }

    /**
     * Gets the test set.
     *
     * @return the test set
     */
    protected DataModelIF<U, I> getTest() {
        return test;
    }

    /**
     * Gets the metric value per user.
     *
     * @return the metric value per user
     */
    protected Map<U, Double> getMetricPerUser() {
        return metricPerUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<U, Double> getValuePerUser() {
        return metricPerUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getValue(final U u) {
        if (metricPerUser.containsKey(u)) {
            return metricPerUser.get(u);
        }
        return Double.NaN;
    }

    /**
     * Initialize private variables required for metric computation.
     */
    protected void iniCompute() {
        value = 0.0;
        metricPerUser = new HashMap<>();
    }

    /**
     * Updates the global value of the metric.
     *
     * @param v new value of the metric
     */
    protected void setValue(final double v) {
        this.value = v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getValue() {
        return value;
    }

    /**
     * Ranks the set of items by associated score.
     *
     * @param userItems map with scores for each item
     * @return the ranked list
     */
    protected List<I> rankItems(final Map<I, Double> userItems) {
        List<I> sortedItems = new ArrayList<>();
        if (userItems == null) {
            return sortedItems;
        }
        Map<Double, Set<I>> itemsByRank = new HashMap<>();
        for (Map.Entry<I, Double> e : userItems.entrySet()) {
            I item = e.getKey();
            double pref = e.getValue();
            if (Double.isNaN(pref)) {
                // we ignore any preference assigned as NaN
                continue;
            }
            Set<I> items = itemsByRank.get(pref);
            if (items == null) {
                items = new HashSet<>();
                itemsByRank.put(pref, items);
            }
            items.add(item);
        }
        List<Double> sortedScores = new ArrayList<>(itemsByRank.keySet());
        Collections.sort(sortedScores, Collections.reverseOrder());
        for (double pref : sortedScores) {
            List<I> sortedPrefItems = new ArrayList<>(itemsByRank.get(pref));
            // deterministic output when ties in preferences: sort by item id
            Collections.sort(sortedPrefItems, Collections.reverseOrder());
            for (I itemID : sortedPrefItems) {
                sortedItems.add(itemID);
            }
        }
        return sortedItems;
    }

    /**
     * Ranks the scores of an item-score map.
     *
     * @param userItems map with scores for each item
     * @return the ranked list
     */
    protected List<Double> rankScores(final Map<I, Double> userItems) {
        List<Double> sortedScores = new ArrayList<>();
        if (userItems == null) {
            return sortedScores;
        }
        for (Map.Entry<I, Double> e : userItems.entrySet()) {
            double pref = e.getValue();
            if (Double.isNaN(pref)) {
                // we ignore any preference assigned as NaN
                continue;
            }
            sortedScores.add(pref);
        }
        Collections.sort(sortedScores, Collections.reverseOrder());
        return sortedScores;
    }
}
