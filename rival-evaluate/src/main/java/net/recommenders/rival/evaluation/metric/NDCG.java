package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModel;

import java.util.*;

/**
 * Normalized <a href="http://recsyswiki.com/wiki/Discounted_Cumulative_Gain" target="_blank">discounted cumulative gain</a> (NDCG) of a ranked list of items.
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class NDCG extends AbstractMetric {

    /**
     * @inheritDoc
     */
    public NDCG(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int at){ // at should be an array if ndcg at sevaral at's is to be found
        super(predictions, test);
    }


    /**
     * Computes the global NDCG by first summing the NDCG for each user and then averaging by the number of users.
     *
     * @return NDCG
     */
    public double computeNDCG(){
        Set<Long> predictedUsers = predictions.getUsers();
        double ndcg = 0.0;
        for (long user : predictedUsers){
            double dcg = 0.0;
            List<Long> sortedList =  rankUser(user);
            Map<Long, Double> testItems = test.getUserItemPreferences().get(user);
            int rank = 0;
            for (long item : sortedList){
                if (testItems.containsKey(item))
                    dcg += Math.log(2) / Math.log(rank + 1);
                rank ++;
            }
            ndcg += dcg / computeIDCG(testItems.size());
        }
        return ndcg / test.getUsers().size();
    }

    /**
     * Ranks the set of items by predicted rating.
     *
     * @param user
     * @return the ranked list
     */
    private List<Long> rankUser(long user){
        Map<Long, Double> predictedItems = predictions.getItemUserPreferences().get(user);
        Map<Double, Set<Long>> itemsByRank  = new HashMap<Double, Set<Long>>();
        for (Map.Entry<Long, Double> e : predictedItems.entrySet()){
            long item = e.getKey();
            double pref = e.getValue();
            if (Double.isNaN(pref))
                continue;
            Set<Long> items = itemsByRank.get(pref);
            if (items == null)
                itemsByRank.put(pref, new HashSet<Long>());
            items.add(item);
        }
        List<Double> sortedScores = new ArrayList<Double>(itemsByRank.keySet());
        Collections.sort(sortedScores, Collections.reverseOrder());
        List<Long> sortedItems = new ArrayList<Long>();
        int num = 0;
        for (double pref : sortedScores){
            for (long itemID : itemsByRank.get(pref)){
                if (at > 0 || num < at) {
                    sortedItems.add(itemID);
                    num ++;
                }
                else
                    break;
            }
        }
        return sortedItems;
    }

    /**
     * Computes the ideal <a href="http://recsyswiki.com/wiki/Discounted_Cumulative_Gain" target="_blank">discounted cumulative gain</a> (IDCG) given the number of items in the test set (correct items).
     *
     * @param testItems the number of correct items.
     * @return  the IDCG
     */
    static double computeIDCG(int testItems){
        double idcg = 0.0;
        for (int i = 0; i < testItems; i++)
            idcg += Math.log(2) / Math.log(i + 2);
        return idcg;
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
