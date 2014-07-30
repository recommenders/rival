package net.recommenders.rival.evaluation.metric.divnov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;
import net.recommenders.rival.evaluation.metric.ranking.AbstractRankingMetric;

/**
 * Abstract class for novelty Metrics
 *
 * See Vargas and Castells @ RecSys 2011
 *
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public abstract class AbstractNoveltyMetric extends AbstractRankingMetric implements EvaluationMetric<Long> {

    /**
     * Novelty values per user at each cutoff level
     */
    private Map<Integer, Map<Long, Double>> userNoveltyAtCutoff;

    /**
     * Constructor where the cutoff levels can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param ats cutoffs
     */
    public AbstractNoveltyMetric(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats) {
        super(predictions, test, Double.NaN, ats);
    }

    /**
     * Novelty of the item (w.r.t. the user or the other ranked items).
     *
     * @param item
     * @param user
     * @param recList recommendation list
     * @param rank of item in the list
     */
    protected abstract double itemNovelty(Long i, Long user, List<Long> recList, int rank);
    
    /**
     * Rank normalization
     *
     * @param user
     * @param at
     */
    protected abstract double norm(Long user, int at);
    
    /**
     * Computes the global novelty by first summing the novelty for each user and then averaging by the number of users.
     */
    @Override
    public void compute() {
        if (!Double.isNaN(value)) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        value = 0.0;
        userNoveltyAtCutoff = new HashMap<Integer, Map<Long, Double>>();
        metricPerUser = new HashMap<Long, Double>();

        int nUsers = 0;
        for (long user : test.getUsers()) {
            List<Long> recList;
            if (predictions.getUsers().contains(user)) {
                recList = rankItems(predictions.getUserItemPreferences().get(user));
            } else {
                recList = new ArrayList<Long>();
            }
            double unov = 0.0;
            int rank = 0;

            for (Long i : recList) {
                rank++;
                unov += itemNovelty(i, user, recList, rank);

                for (int at : ats) {
                    if (rank == at) {
                        Map<Long, Double> m = userNoveltyAtCutoff.get(at);
                        if (m == null) {
                            m = new HashMap<Long, Double>();
                            userNoveltyAtCutoff.put(at, m);
                        }
                        m.put(user, unov / norm(user, at));
                    }
                }
            }
            // DO NOT assign the novelty of the whole list to those cutoffs larger than the list's size
            // instead, we fill with not relevant items until such cutoff
            for (int at : ats) {
                if (rank <= at) {
                    Map<Long, Double> m = userNoveltyAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<Long, Double>();
                        userNoveltyAtCutoff.put(at, m);
                    }
                    m.put(user, unov / norm(user, at));
                }
            }
            // normalize by list size
            unov /= norm(user, rank);
            if (!Double.isNaN(unov)) {
                value += unov;
                metricPerUser.put(user, unov);
                nUsers++;
            }
        }
        value = value / nUsers;
    }

    /**
     * Method to return the novelty value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the novelty corresponding to the requested cutoff level
     */
    @Override
    public double getValueAt(int at) {
        if (userNoveltyAtCutoff.containsKey(at)) {
            int n = 0;
            double nov = 0.0;
            for (long u : userNoveltyAtCutoff.get(at).keySet()) {
                double unov = getValueAt(u, at);
                if (!Double.isNaN(unov)) {
                    nov += unov;
                    n++;
                }
            }
            nov = (n == 0) ? 0.0 : nov / n;
            return nov;
        }
        return Double.NaN;
    }

    /**
     * Method to return the novelty value at a particular cutoff level for a given user.
     *
     * @param user the user
     * @param at cutoff level
     * @return the novelty corresponding to the requested user at the cutoff level
     */
    @Override
    public double getValueAt(long user, int at) {
        if (userNoveltyAtCutoff.containsKey(at) && userNoveltyAtCutoff.get(at).containsKey(user)) {
            double nov = userNoveltyAtCutoff.get(at).get(user);
            return nov;
        }
        return Double.NaN;
    }
}
