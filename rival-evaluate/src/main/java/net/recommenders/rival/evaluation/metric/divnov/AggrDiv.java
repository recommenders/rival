package net.recommenders.rival.evaluation.metric.divnov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;
import net.recommenders.rival.evaluation.metric.ranking.AbstractRankingMetric;

/**
 * Aggregate Diversity
 *
 * See Adomavicius and Kwon @ TKDE Vol 24
 *
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public class AggrDiv extends AbstractRankingMetric implements EvaluationMetric<Long> {

    /**
     * Aggregate Diversity values each cutoff level
     */
    private Map<Integer, Double> aggrDivAtCutoff;

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
    public AggrDiv(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats, int nItems) {
        super(predictions, test, Double.NaN, ats);
        this.nItems = nItems;
    }

    /**
     * Computes the Aggregate Diversity
     */
    @Override
    public void compute() {
        if (!Double.isNaN(value)) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        value = 0.0;
        aggrDivAtCutoff = new HashMap<Integer, Double>();
        Map<Integer, Set<Long>> uniqueItemsAtCutoff = new HashMap<Integer, Set<Long>>();
        for (int at : ats) {
            uniqueItemsAtCutoff.put(at, new HashSet<Long>());
        }
        Set<Long> uniqueItems = new HashSet<Long>();

        for (long user : test.getUsers()) {
            List<Long> recList;
            if (predictions.getUsers().contains(user)) {
                recList = rankItems(predictions.getUserItemPreferences().get(user));
            } else {
                recList = new ArrayList<Long>();
            }

            for (int at : ats) {
                if (recList.size() > at) {
                    uniqueItemsAtCutoff.get(at).addAll(recList.subList(0, at));
                } else {
                    uniqueItemsAtCutoff.get(at).addAll(recList);
                }
            }
            uniqueItems.addAll(recList);
        }
        for (int at : ats) {
            aggrDivAtCutoff.put(at, uniqueItemsAtCutoff.get(at).size() / (double) nItems);
        }
        value = uniqueItems.size() / (double) nItems;
    }

    /**
     * Method to return the Aggregate Diversity value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the Aggregate Diversity value corresponding to the requested cutoff level
     */
    @Override
    public double getValueAt(int at) {
        if (aggrDivAtCutoff.containsKey(at)) {
            return aggrDivAtCutoff.get(at);
        }
        return Double.NaN;
    }

    @Override
    public double getValueAt(long user, int at) {
        throw new UnsupportedOperationException("system metric");
    }

}
