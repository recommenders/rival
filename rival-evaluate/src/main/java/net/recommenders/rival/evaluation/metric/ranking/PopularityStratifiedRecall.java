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
 * Popularity-stratified recall as defined in "A generalized probabilistic
 * framework and its variants for training top-k recommender systems", Harald
 * Steck and Yu Xin. Compatible with definitions in "Item popularity and
 * recommendation accuracy", Harald Steck and "Ranking with non-random missing
 * ratings: influence of popularity and positivity on evaluation metrics", Bruno
 * Padel, Nicolas Usunier, Patrick Gallinari.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 *
 * @param <U> - type associated to users' ids
 * @param <I> - type associated to items' ids
 */
public class PopularityStratifiedRecall<U, I> extends AbstractRankingMetric<U, I> implements EvaluationMetric<U> {

    /**
     * Recall values per user at each cutoff level.
     */
    private Map<Integer, Map<U, Double>> userRecallAtCutoff;
    /**
     * Recall values per user.
     */
    private Map<U, Double> userTotalRecall;
    /**
     * Relevant ratings observed for each item (assuming the probability of
     * observing a relevant rating depends on the popularity of items).
     */
    private Map<I, Integer> observedItemRelevance;
    /**
     * Smoothing parameter for the observation probability with respect to item
     * popularity.
     */
    private double gamma;

    /**
     * Default constructor with predictions, groundtruth information, gamma
     * parameter and item relevance.
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     * @param theGamma smoothing parameter
     * @param theObservedItemRelevance item relevance (popularity)
     */
    public PopularityStratifiedRecall(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test, final double theGamma, final Map<I, Integer> theObservedItemRelevance) {
        this(predictions, test, 1.0, theGamma, theObservedItemRelevance);
    }

    /**
     * Constructor where the relevance threshold can be initialized.
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     * @param theGamma smoothing parameter
     * @param theObservedItemRelevance item relevance (popularity)
     */
    public PopularityStratifiedRecall(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test, final double relThreshold, final double theGamma, final Map<I, Integer> theObservedItemRelevance) {
        this(predictions, test, relThreshold, new int[]{}, theGamma, theObservedItemRelevance);
    }

    /**
     * Constructor where the cutoff levels can be initialized.
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     * @param ats cutoffs
     * @param theGamma smoothing parameter
     * @param theObservedItemRelevance item relevance (popularity)
     */
    public PopularityStratifiedRecall(final DataModelIF<U, I> predictions, final DataModelIF<U, I> test,
            final double relThreshold, final int[] ats,
            final double theGamma, final Map<I, Integer> theObservedItemRelevance) {
        super(predictions, test, relThreshold, ats);
        this.gamma = theGamma;
        this.observedItemRelevance = theObservedItemRelevance;
    }

    /**
     * Computes the global popularity-stratified recall by applying the
     * normalized user weights w^u as defined in the paper.
     */
    @Override
    public void compute() {
        if (!Double.isNaN(getValue())) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        iniCompute();

        Map<U, List<Pair<I, Double>>> data = processDataAsRankedTestRelevance();
        userRecallAtCutoff = new HashMap<Integer, Map<U, Double>>();
        userTotalRecall = new HashMap<U, Double>();


        double sum = 0.0;
        for (Map.Entry<U, List<Pair<I, Double>>> e : data.entrySet()) {
            U user = e.getKey();
            List<Pair<I, Double>> sortedList = e.getValue();
            double urec = 0.0;
            int rank = 0;
            for (Pair<I, Double> pair : sortedList) {
                I item = pair.getFirst();
                double rel = pair.getSecond();
                rank++;
                if (computeBinaryPrecision(rel) > 0) {
                    urec += getPopularityStratificationWeight(item);
                }
                // compute at a particular cutoff
                for (int at : getCutoffs()) {
                    if (rank == at) {
                        Map<U, Double> m = userRecallAtCutoff.get(at);
                        if (m == null) {
                            m = new HashMap<U, Double>();
                            userRecallAtCutoff.put(at, m);
                        }
                        m.put(user, urec);
                    }
                }
            }
            // assign the recall of the whole list to those cutoffs larger than the list's size
            for (int at : getCutoffs()) {
                if (rank <= at) {
                    Map<U, Double> m = userRecallAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<U, Double>();
                        userRecallAtCutoff.put(at, m);
                    }
                    m.put(user, urec);
                }
            }
            if (!Double.isNaN(urec)) {
                // these values are not meaningful, since this metric only makes sense at top-k
                setValue(getValue() + urec);
                getMetricPerUser().put(user, urec / urec);
                userTotalRecall.put(user, urec);
                sum += urec;
            }
        }
        setValue(getValue() / sum);
    }

    /**
     *
     * Returns s_i = 1/p_obs(i), assuming Eq 13 from the paper. If this weight
     * is reduced to 1.0 for all items, this metric would be equivalent to
     * recall.
     *
     * @param item the item for which the weight has to be computed.
     * @return the popularity-stratification weight
     */
    private double getPopularityStratificationWeight(final I item) {
        if (observedItemRelevance.containsKey(item)) {
            int observedRelevance = observedItemRelevance.get(item);
            double si = Math.pow(1.0 * observedRelevance, -1.0 * gamma / (gamma + 1));
            return si;
        }
        return 0.0;
    }

    /**
     * Method to return the recall value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the recall corresponding to the requested cutoff level
     */
    @Override
    public double getValueAt(final int at) {
        if (userRecallAtCutoff.containsKey(at)) {
            double sum = 0.0;
            double rec = 0.0;
            for (U u : userRecallAtCutoff.get(at).keySet()) {
                double urec = getValueAt(u, at);
                double utotal = userTotalRecall.get(u);
                if (!Double.isNaN(urec)) {
                    rec += urec * utotal;
                    sum += utotal;
                }
            }
            if (sum == 0.0) {
                rec = 0.0;
            } else {
                rec = rec / sum;
            }
            return rec;
        }
        return Double.NaN;
    }

    /**
     * Method to return the recall value at a particular cutoff level for a
     * given user.
     *
     * @param user the user
     * @param at cutoff level
     * @return the recall corresponding to the requested user at the cutoff
     * level
     */
    @Override
    public double getValueAt(final U user, final int at) {
        if (userRecallAtCutoff.containsKey(at) && userRecallAtCutoff.get(at).containsKey(user)) {
            return userRecallAtCutoff.get(at).get(user) / userTotalRecall.get(user);
        }
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PopularityStratifiedRecall_" + gamma + "_" + getRelevanceThreshold();
    }
}
