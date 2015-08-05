package net.recommenders.rival.evaluation.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.recommenders.rival.core.DataModel;

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
    protected DataModel<U, I> predictions;
    /**
     * The test set.
     */
    protected DataModel<U, I> test;
    /**
     * Metric per user
     */
    protected Map<U, Double> metricPerUser;

    /**
     * Default constructor with predictions and groundtruth information
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public AbstractMetric(DataModel<U, I> predictions, DataModel<U, I> test) {
        this.predictions = predictions;
        this.test = test;

        this.metricPerUser = new HashMap<U, Double>();
    }

    /**
     * @inheritDoc
     */
    @Override
    public Map<U, Double> getValuePerUser() {
        return metricPerUser;
    }

    /**
     * @inheritDoc
     */
    @Override
    public double getValue(U u) {
        if (metricPerUser.containsKey(u)) {
            return metricPerUser.get(u);
        }
        return Double.NaN;
    }

    /**
     * Ranks the set of items by associated score.
     *
     * @param userItems map with scores for each item
     * @return the ranked list
     */
    protected List<I> rankItems(Map<I, Double> userItems) {
        List<I> sortedItems = new ArrayList<I>();
        if (userItems == null) {
            return sortedItems;
        }
        Map<Double, Set<I>> itemsByRank = new HashMap<Double, Set<I>>();
        for (Map.Entry<I, Double> e : userItems.entrySet()) {
            I item = e.getKey();
            double pref = e.getValue();
            if (Double.isNaN(pref)) {
                // we ignore any preference assigned as NaN
                continue;
            }
            Set<I> items = itemsByRank.get(pref);
            if (items == null) {
                items = new HashSet<I>();
                itemsByRank.put(pref, items);
            }
            items.add(item);
        }
        List<Double> sortedScores = new ArrayList<Double>(itemsByRank.keySet());
        Collections.sort(sortedScores, Collections.reverseOrder());
        for (double pref : sortedScores) {
            List<I> sortedPrefItems = new ArrayList<I>(itemsByRank.get(pref));
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
    protected List<Double> rankScores(Map<I, Double> userItems) {
        List<Double> sortedScores = new ArrayList<Double>();
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
