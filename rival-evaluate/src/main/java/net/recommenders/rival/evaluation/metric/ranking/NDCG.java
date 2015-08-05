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

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.evaluation.Pair;

/**
 * Normalized <a href="http://recsyswiki.com/wiki/Discounted_Cumulative_Gain"
 * target="_blank">discounted cumulative gain</a> (NDCG) of a ranked list of
 * items.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 *
 * @param <U> - type associated to users' ids
 * @param <I> - type associated to items' ids
 */
public class NDCG<U, I> extends AbstractRankingMetric<U, I> implements EvaluationMetric<U> {

    /**
     * Type of nDCG computation (linear or exponential)
     */
    public static enum TYPE {

        LIN,
        EXP,
        TREC_EVAL;
    }
    /**
     * Type of nDCG computation (linear or exponential)
     */
    private TYPE type;
    /**
     * DCG values per user at each cutoff level
     */
    private Map<Integer, Map<U, Double>> userDcgAtCutoff;
    /**
     * Ideal DCG values per user at each cutoff level
     */
    private Map<Integer, Map<U, Double>> userIdcgAtCutoff;

    /**
     * Default constructor with predictions and groundtruth information
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public NDCG(DataModel<U, I> predictions, DataModel<U, I> test) {
        this(predictions, test, new int[]{});
    }

    /**
     * Constructor where the cutoff levels can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param ats cutoffs
     */
    public NDCG(DataModel<U, I> predictions, DataModel<U, I> test, int[] ats) {
        this(predictions, test, 1.0, ats, TYPE.EXP);
    }

    /**
     * Constructor where the cutoff levels and the type of NDCG computation can
     * be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold the relevance threshold
     * @param ats cutoffs
     * @param type type of NDCG computation
     */
    public NDCG(DataModel<U, I> predictions, DataModel<U, I> test, double relThreshold, int[] ats, TYPE type) {
        super(predictions, test, relThreshold, ats);
        this.type = type;
    }

    /**
     * Computes the global NDCG by first summing the NDCG for each user and then
     * averaging by the number of users.
     */
    @Override
    public void compute() {
        if (!Double.isNaN(value)) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        value = 0.0;
        Map<U, List<Pair<I, Double>>> data = processDataAsRankedTestRelevance();
        userDcgAtCutoff = new HashMap<Integer, Map<U, Double>>();
        userIdcgAtCutoff = new HashMap<Integer, Map<U, Double>>();
        metricPerUser = new HashMap<U, Double>();

        int nUsers = 0;
        for (Map.Entry<U, List<Pair<I, Double>>> e : data.entrySet()) {
            U user = e.getKey();
            List<Pair<I, Double>> sortedList = e.getValue();
            double dcg = 0.0;
            int rank = 0;
            for (Pair<I, Double> pair : sortedList) {
                double rel = pair.getSecond();
                rank++;
                dcg += computeDCG(rel, rank);
                // compute at a particular cutoff
                for (int at : ats) {
                    if (rank == at) {
                        Map<U, Double> m = userDcgAtCutoff.get(at);
                        if (m == null) {
                            m = new HashMap<U, Double>();
                            userDcgAtCutoff.put(at, m);
                        }
                        m.put(user, dcg);
                    }
                }
            }
            // assign the ndcg of the whole list to those cutoffs larger than the list's size
            for (int at : ats) {
                if (rank <= at) {
                    Map<U, Double> m = userDcgAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<U, Double>();
                        userDcgAtCutoff.put(at, m);
                    }
                    m.put(user, dcg);
                }
            }
            double idcg = computeIDCG(user, test.getUserItemPreferences().get(user));
            double undcg = dcg / idcg;
            if (!Double.isNaN(undcg)) {
                value += undcg;
                metricPerUser.put(user, undcg);
                nUsers++;
            }
        }
        value = value / nUsers;
    }

    /**
     * Method that computes the discounted cumulative gain of a specific item,
     * taking into account its ranking in a user's list and its relevance value.
     *
     * @param rel the item's relevance
     * @param rank the item's rank in a user's list (sorted by predicted rating)
     * @return the dcg of the item
     */
    protected double computeDCG(double rel, int rank) {
        double dcg = 0.0;
        if (rel >= relevanceThreshold) {
            switch (type) {
                case EXP: {
                    dcg = (Math.pow(2.0, rel) - 1.0) / (Math.log(rank + 1) / Math.log(2));
                }
                break;
                case LIN: {
                    dcg = rel;
                    if (rank > 1) {
                        dcg /= (Math.log(rank) / Math.log(2));
                    }
                }
                break;
                case TREC_EVAL: {
                    dcg = rel / (Math.log(rank + 1) / Math.log(2));
                }
                break;
            }
        }
        return dcg;
    }

    /**
     * Computes the ideal <a
     * href="http://recsyswiki.com/wiki/Discounted_Cumulative_Gain"
     * target="_blank">discounted cumulative gain</a> (IDCG) given the test set
     * (groundtruth items) for a user.
     *
     * @param user the user
     * @param userTestItems the groundtruth items of a user.
     * @return the IDCG
     */
    private double computeIDCG(U user, Map<I, Double> userTestItems) {
        double idcg = 0.0;
        // sort the items according to their relevance level
        List<Double> sortedList = rankScores(userTestItems);
        int rank = 1;
        for (double itemRel : sortedList) {
            idcg += computeDCG(itemRel, rank);
            // compute at a particular cutoff
            for (int at : ats) {
                if (rank == at) {
                    Map<U, Double> m = userIdcgAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<U, Double>();
                        userIdcgAtCutoff.put(at, m);
                    }
                    m.put(user, idcg);
                }
            }
            rank++;
        }
        // assign the ndcg of the whole list to those cutoffs larger than the list's size
        for (int at : ats) {
            if (rank <= at) {
                Map<U, Double> m = userIdcgAtCutoff.get(at);
                if (m == null) {
                    m = new HashMap<U, Double>();
                    userIdcgAtCutoff.put(at, m);
                }
                m.put(user, idcg);
            }
        }
        return idcg;
    }

    /**
     * Method to return the NDCG value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the NDCG corresponding to the requested cutoff level
     */
    @Override
    public double getValueAt(int at) {
        if (userDcgAtCutoff.containsKey(at) && userIdcgAtCutoff.containsKey(at)) {
            int n = 0;
            double ndcg = 0.0;
            for (U u : userIdcgAtCutoff.get(at).keySet()) {
                double udcg = getValueAt(u, at);
                if (!Double.isNaN(udcg)) {
                    ndcg += udcg;
                    n++;
                }
            }
            ndcg = (n == 0) ? 0.0 : ndcg / n;
            return ndcg;
        }
        return Double.NaN;
    }

    /**
     * Method to return the NDCG value at a particular cutoff level for a given
     * user.
     *
     * @param user the user
     * @param at cutoff level
     * @return the NDCG corresponding to the requested user at the cutoff level
     */
    @Override
    public double getValueAt(U user, int at) {
        if (userDcgAtCutoff.containsKey(at) && userDcgAtCutoff.get(at).containsKey(user)
                && userIdcgAtCutoff.containsKey(at) && userIdcgAtCutoff.get(at).containsKey(user)) {
            double idcg = userIdcgAtCutoff.get(at).get(user);
            double dcg = userDcgAtCutoff.get(at).get(user);
            return dcg / idcg;
        }
        return Double.NaN;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return "NDCG" + type + "_" + relevanceThreshold;
    }
}
