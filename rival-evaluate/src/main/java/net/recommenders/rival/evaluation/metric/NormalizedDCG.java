package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModel;

import java.util.*;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class NormalizedDCG extends AbstractMetric {

    /**
     * @inheritDoc
     */
    public NormalizedDCG(Map<Long, Double> predictions, DataModel<Long, Long> test, int at){ // at should be an array
        super(predictions, test);
    }


    public double calculateNDCGValue(){

        final Set<Long> testUsers = test.getUsers();
        for (long user : testUsers) {
            final Map<Double, Set<Long>> preferenceMap = new HashMap<Double, Set<Long>>();
            for (Map.Entry<Long, Double> e : predictions.entrySet()) {
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


            //if (predictions.getUserItemPreferences().containsKey(user)){
            //    final Set<Long> predictedItems = predictions.getUserItemPreferences().get(user).keySet();
            //}
        }
        return 0.0;
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
