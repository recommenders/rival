package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModel;

import java.util.*;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class NDCG extends AbstractMetric {

    /**
     * @inheritDoc
     */
    public NDCG(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int at){ // at should be an array
        super(predictions, test);
    }

    /**
     * Prepares (sorts and does at cutoff at "at") the predictions after they are read from file.
     *
     */
    public void preparePredictions(){
        Map<Long, Map<Long, Double>> recommendations = predictions.getUserItemPreferences();

        for (long user : predictions.getUsers()) {
            final Map<Double, Set<Long>> preferenceMap = new HashMap<Double, Set<Long>>();
            for (Map.Entry<Long, Double> e : recommendations.get(user).entrySet()) {
                long item = e.getKey();
                double pref = e.getValue();
                // ignore NaN's
                if (Double.isNaN(pref)) {
                    continue;
                }
                Set<Long> items = preferenceMap.get(pref);
                if (items == null) {
                    items = new HashSet<Long>();
                    preferenceMap.put(pref, items);
                }
                items.add(item);
            }
            final List<Double> sortedScores = new ArrayList<Double>(preferenceMap.keySet());
            Collections.sort(sortedScores, Collections.reverseOrder());
            // Write estimated preferences
            int pos = 1;


/**
            for (double pref : sortedScores) {
                for (long itemID : preferenceMap.get(pref)) {
                    switch (format) {
                        case TRECEVAL:
                            out.println(user + "\tQ0\t" + itemID + "\t" + pos + "\t" + pref + "\t" + "r");
                            break;
                        case SIMPLE:
                            out.println(user + "\t" + itemID + "\t" + pref);
                            break;
                    }
                    pos++;
                }
            }
 */
        }


/**

        Map<Long, Map<Long, Double>> recommendations = predictions.getUserItemPreferences();
        for (long user : predictions.getUsers()){
            Map<Long, Double> sortedRatings = MapUtil.sortByValue(recommendations.get(user));

            for(Map.Entry<Long, Double> entry : sortedRatings.entrySet()){

            }

        }
 */

    }


    @Override
    public double getValue() {
        return 0;
    }

    @Override
    public Map getValuePerUser() {
        return null;
    }
}
