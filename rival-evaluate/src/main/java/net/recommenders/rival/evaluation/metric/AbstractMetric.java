package net.recommenders.rival.evaluation.metric;

import java.util.*;
import net.recommenders.rival.core.DataModel;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public abstract class AbstractMetric implements EvaluationMetric<Long> {

    /**
     * The predictions.
     */
    protected DataModel<Long, Long> predictions;
    /**
     * The test set.
     */
    protected DataModel<Long, Long> test;
    /**
     * Metric per user
     */
    protected Map<Long, Double> metricPerUser;

    /**
     * Default constructor for the metric.
     *
     * @param predictions The predictions.
     * @param test The test set.
     */
    public AbstractMetric(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        this.predictions = predictions;
        this.test = test;

        this.metricPerUser = new HashMap<Long, Double>();
    }

    /**
     * @inheritDoc
     */
    @Override
    public Map<Long, Double> getValuePerUser() {
        return metricPerUser;
    }

    @Override
    public double getValue(Long u) {
        if (metricPerUser.containsKey(u)) {
            return metricPerUser.get(u);
        }
        return Double.NaN;
    }

    /**
     * Ranks the set of items by predicted rating.
     *
     * @param user
     * @return the ranked list
     */
    protected List<Long> rankUserTest(long user) {
        return rankUserMap(user, test.getUserItemPreferences().get(user));
    }

    /**
     * Ranks the set of items by predicted rating.
     *
     * @param user
     * @return the ranked list
     */
    protected List<Long> rankUserPredictions(long user) {
        return rankUserMap(user, predictions.getUserItemPreferences().get(user));
    }

    /**
     * Ranks the set of items by predicted rating.
     *
     * @param user
     * @return the ranked list
     */
    private List<Long> rankUserMap(long user, Map<Long, Double> userItems) {
        List<Long> sortedItems = new ArrayList<Long>();
        if (userItems == null) {
            return sortedItems;
        }
        Map<Double, Set<Long>> itemsByRank = new HashMap<Double, Set<Long>>();
        for (Map.Entry<Long, Double> e : userItems.entrySet()) {
            long item = e.getKey();
            double pref = e.getValue();
            if (Double.isNaN(pref)) {
                // we ignore any preference assigned as NaN
                continue;
            }
            Set<Long> items = itemsByRank.get(pref);
            if (items == null) {
                items = new HashSet<Long>();
                itemsByRank.put(pref, items);
            }
            items.add(item);
        }
        List<Double> sortedScores = new ArrayList<Double>(itemsByRank.keySet());
        Collections.sort(sortedScores, Collections.reverseOrder());
        for (double pref : sortedScores) {
            for (long itemID : itemsByRank.get(pref)) {
                sortedItems.add(itemID);
            }
        }
        return sortedItems;
    }
}
