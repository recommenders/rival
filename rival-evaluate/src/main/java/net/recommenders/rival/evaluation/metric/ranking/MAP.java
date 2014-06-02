package net.recommenders.rival.evaluation.metric.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

/**
 * Mean Average Precision of a ranked list of items.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public class MAP extends AbstractRankingMetric implements EvaluationMetric<Long> {

    /**
     * AP (average precision) values per user at each cutoff level
     */
    private Map<Integer, Map<Long, Double>> userMAPAtCutoff;

    /**
     * Default constructor with predictions and groundtruth information
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public MAP(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        this(predictions, test, 1.0);
    }

    /**
     * Constructor where the relevance threshold can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     */
    public MAP(DataModel<Long, Long> predictions, DataModel<Long, Long> test, double relThreshold) {
        this(predictions, test, relThreshold, new int[]{});
    }

    /**
     * Constructor where the cutoff levels can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold the relevance threshold
     * @param ats cutoffs
     */
    public MAP(DataModel<Long, Long> predictions, DataModel<Long, Long> test, double relThreshold, int[] ats) {
        super(predictions, test, relThreshold, ats);
    }

    /**
     * Computes the global MAP by first summing the AP (average precision) for
     * each user and then averaging by the number of users.
     */
    @Override
    public void compute() {
        if (!Double.isNaN(value)) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        value = 0.0;
        Map<Long, List<Double>> data = processDataAsRankedTestRelevance();
        userMAPAtCutoff = new HashMap<Integer, Map<Long, Double>>();
        metricPerUser = new HashMap<Long, Double>();

        int nUsers = 0;
        for (long user : data.keySet()) {
            List<Double> sortedList = data.get(user);
            // number of relevant items for this user
            double uRel = getNumberOfRelevantItems(user);
            double uMAP = 0.0;
            double uPrecision = 0.0;
            int rank = 0;
            for (double rel : sortedList) {
                rank++;
                double itemPrecision = computeBinaryPrecision(rel);
                uPrecision += itemPrecision;
                if (itemPrecision > 0) {
                    uMAP += uPrecision / rank;
                }
                // compute at a particular cutoff
                for (int at : ats) {
                    if (rank == at) {
                        Map<Long, Double> m = userMAPAtCutoff.get(at);
                        if (m == null) {
                            m = new HashMap<Long, Double>();
                            userMAPAtCutoff.put(at, m);
                        }
                        m.put(user, uMAP / uRel);
                    }
                }
            }
            // normalize by number of relevant items
            uMAP /= uRel;
            // assign the MAP of the whole list to those cutoffs larger than the list's size
            for (int at : ats) {
                if (rank <= at) {
                    Map<Long, Double> m = userMAPAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<Long, Double>();
                        userMAPAtCutoff.put(at, m);
                    }
                    m.put(user, uMAP);
                }
            }
            if (!Double.isNaN(uMAP)) {
                value += uMAP;
                metricPerUser.put(user, uMAP);
                nUsers++;
            }
        }
        value = value / nUsers;
    }

    /**
     * Method to return the MAP value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the MAP corresponding to the requested cutoff level
     */
    @Override
    public double getValueAt(int at) {
        if (userMAPAtCutoff.containsKey(at)) {
            int n = 0;
            double map = 0.0;
            for (long u : userMAPAtCutoff.get(at).keySet()) {
                double uMAP = getValueAt(u, at);
                if (!Double.isNaN(uMAP)) {
                    map += uMAP;
                    n++;
                }
            }
            map = (n == 0) ? 0.0 : map / n;
            return map;
        }
        return Double.NaN;
    }

    /**
     * Method to return the AP (average precision) value at a particular cutoff
     * level for a given user.
     *
     * @param user the user
     * @param at cutoff level
     * @return the AP (average precision) corresponding to the requested user at
     * the cutoff level
     */
    @Override
    public double getValueAt(long user, int at) {
        if (userMAPAtCutoff.containsKey(at) && userMAPAtCutoff.get(at).containsKey(user)) {
            double map = userMAPAtCutoff.get(at).get(user);
            return map;
        }
        return Double.NaN;
    }
}
