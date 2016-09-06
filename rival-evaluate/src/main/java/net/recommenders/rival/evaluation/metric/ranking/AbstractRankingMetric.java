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
import net.recommenders.rival.evaluation.metric.AbstractMetric;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;
import net.recommenders.rival.evaluation.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which represents all the basic elements of a ranking based
 * metric.
 *
 * @param <U> - type associated to users' ids
 * @param <I> - type associated to items' ids
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public abstract class AbstractRankingMetric<U, I> extends AbstractMetric<U, I> implements EvaluationMetric<U> {

    /**
     * Array of cutoff levels.
     */
    private int[] ats;
    /**
     * Relevance threshold.
     */
    private double relevanceThreshold;

    /**
     * Default constructor with predictions and groundtruth information.
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public AbstractRankingMetric(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test) {
        this(predictions, test, 1.0);
    }

    /**
     * Constructor where the relevance threshold can be initialized.
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     */
    public AbstractRankingMetric(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test, final double relThreshold) {
        this(predictions, test, relThreshold, new int[]{});
    }

    /**
     * Constructor where the cutoff levels can be initialized.
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param cutoffLevels cutoffs
     * @param relThreshold relevance threshold
     */
    public AbstractRankingMetric(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test, final double relThreshold, final int[] cutoffLevels) {
        super(predictions, test);
        setValue(Double.NaN);
        this.ats = Arrays.copyOf(cutoffLevels, cutoffLevels.length);
        this.relevanceThreshold = relThreshold;
    }

    /**
     * Gets the relevance threshold.
     *
     * @return the relevance threshold
     */
    protected double getRelevanceThreshold() {
        return relevanceThreshold;
    }

    /**
     * Method that transforms the user data from pairs of (item, score) into
     * ranked lists of relevance values, by using ground truth information.
     *
     * @return a map with the transformed data, one list per user
     */
    public Map<U, List<Pair<I, Double>>> processDataAsRankedTestRelevance() {
        Map<U, List<Pair<I, Double>>> data = new HashMap<U, List<Pair<I, Double>>>();

        for (U testUser : getTest().getUsers()) {
            Map<I, Double> userPredictedRatings = new HashMap<>();
            for (I i : getPredictions().getUserItems(testUser)) {
                userPredictedRatings.put(i, getPredictions().getUserItemPreference(testUser, i));
            }
            if (!userPredictedRatings.isEmpty()) {
                List<Pair<I, Double>> rankedTestRel = new ArrayList<Pair<I, Double>>();
                for (I item : rankItems(userPredictedRatings)) {
                    double rel = getTest().getUserItemPreference(testUser, item);
                    if (Double.isNaN(rel)) {
                        rel = 0.0;
                    }
                    rankedTestRel.add(new Pair<I, Double>(item, rel));
                }
                data.put(testUser, rankedTestRel);
            }
        }
        return data;
    }

    /**
     * Method that computes the number of relevant items in the test set for a
     * user.
     *
     * @param user a user
     * @return the number of relevant items the user has in the test set
     */
    protected double getNumberOfRelevantItems(final U user) {
        int n = 0;
        if (getTest().getUserItems(user) != null) {
            for (I i : getTest().getUserItems(user)) {
                if (getTest().getUserItemPreference(user, i) >= relevanceThreshold) {
                    n++;
                }
            }
        }
        return n * 1.0;
    }

    /**
     * Method that computes the binary precision of a specific item, taking into
     * account its relevance value.
     *
     * @param rel the item's relevance
     * @return the binary precision of the item
     */
    protected double computeBinaryPrecision(final double rel) {
        double prec = 0.0;
        if (rel >= relevanceThreshold) {
            prec = 1.0;
        }
        return prec;
    }

    /**
     * Returns the array of cutoff levels where this metric has computed values
     * at.
     *
     * @return the array of cutoff levels
     */
    public int[] getCutoffs() {
        return ats;
    }

    /**
     * Method to return the metric value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the metric corresponding to the requested cutoff level
     */
    public abstract double getValueAt(final int at);

    /**
     * Method to return the metric value at a particular cutoff level for a
     * given user.
     *
     * @param user the user
     * @param at cutoff level
     * @return the metric corresponding to the requested user at the cutoff
     * level
     */
    public abstract double getValueAt(final U user, final int at);
}
