package net.recommenders.rival.evaluation.metric.divnov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;
import net.recommenders.rival.evaluation.metric.ranking.AbstractRankingMetric;

/**
 * Gini Index
 *
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public class GiniIndex extends AbstractRankingMetric implements EvaluationMetric<Long> {

    /**
     * Gini Index values each cutoff level
     */
    private Map<Integer, Double> giniIndexAtCutoff;

    /**
     * Number of recommendable items
     */
    private final int nItems;

    /**
     * Constructor where the cutoff levels can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param ats cutoffs
     */
    public GiniIndex(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats, int nItems) {
        super(predictions, test, Double.NaN, ats);
        this.nItems = nItems;
    }

    /**
     * Computes the Gini Index
     */
    @Override
    public void compute() {
        if (!Double.isNaN(value)) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        value = 0.0;
        giniIndexAtCutoff = new HashMap<Integer, Double>();
        Map<Integer, Map<Long, Integer>> itemCountAtCutoff = new HashMap<Integer, Map<Long, Integer>>();
        for (int at : ats) {
            itemCountAtCutoff.put(at, new HashMap<Long, Integer>());
        }
        Map<Long, Integer> itemCount = new HashMap<Long, Integer>();

        for (long user : test.getUsers()) {
            List<Long> recList;
            if (predictions.getUsers().contains(user)) {
                recList = rankItems(predictions.getUserItemPreferences().get(user));
            } else {
                recList = new ArrayList<Long>();
            }

            for (int at : ats) {
                if (recList.size() > at) {
                    updateItemCount(itemCountAtCutoff.get(at), recList.subList(0, at));
                } else {
                    updateItemCount(itemCountAtCutoff.get(at), recList);
                }
            }
            updateItemCount(itemCount, recList);
        }
        for (int at : ats) {
            giniIndexAtCutoff.put(at, calculateGiniIndex(itemCountAtCutoff.get(at)));
        }
        value = calculateGiniIndex(itemCount);
    }

    private void updateItemCount(Map<Long, Integer> itemCount, List<Long> recList) {
        for (Long item : recList) {
            Integer c = itemCount.get(item);
            if (c == null) {
                itemCount.put(item, 1);
            } else {
                itemCount.put(item, c + 1);
            }
        }
    }

    private double calculateGiniIndex(Map<Long, Integer> itemCount) {
        double gi = 0;
        List<Integer> cs = new ArrayList<Integer>(itemCount.values());
        int N = 0;
        for (Integer c : cs) {
            N += c;
        }
        Collections.sort(cs);
        for (int k = 0; k < cs.size(); k++) {
            gi += (2 * (k + (nItems - cs.size()) + 1) - nItems - 1) * (cs.get(k) / (double) N);
        }
        gi /= (nItems - 1);
        gi = 1 - gi;

        return gi;
    }

    /**
     * Method to return the Gini Index value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the Gini Index value corresponding to the requested cutoff level
     */
    @Override
    public double getValueAt(int at) {
        if (giniIndexAtCutoff.containsKey(at)) {
            return giniIndexAtCutoff.get(at);
        }
        return Double.NaN;
    }

    @Override
    public double getValueAt(long user, int at) {
        throw new UnsupportedOperationException("system metric");
    }

}
