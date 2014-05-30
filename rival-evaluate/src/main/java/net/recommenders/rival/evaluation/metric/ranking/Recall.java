package net.recommenders.rival.evaluation.metric.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

/**
 * Recall of a ranked list of items.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public class Recall extends AbstractRankingMetric implements EvaluationMetric<Long> {

    /**
     * Recall values per user at each cutoff level
     */
    private Map<Integer, Map<Long, Double>> userRecallAtCutoff;

    /**
     * Default constructor with predictions and groundtruth information
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public Recall(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        this(predictions, test, 1.0);
    }

    /**
     * Constructor where the relevance threshold can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     */
    public Recall(DataModel<Long, Long> predictions, DataModel<Long, Long> test, double relThreshold) {
        this(predictions, test, relThreshold, new int[]{});
    }

    /**
     * Constructor where the cutoff levels can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param ats cutoffs
     */
    public Recall(DataModel<Long, Long> predictions, DataModel<Long, Long> test, double relThreshold, int[] ats) {
        super(predictions, test, relThreshold, ats);
    }

    /**
     * Computes the global recall by first summing the recall for each user and
     * then averaging by the number of users.
     */
    @Override
    public void compute() {
        if (!Double.isNaN(value)) {
            // since the data cannot change, avoid re-doing the calculations
            return;
        }
        value = 0.0;
        Map<Long, List<Double>> data = processDataAsRankedTestRelevance();
        userRecallAtCutoff = new HashMap<Integer, Map<Long, Double>>();
        metricPerUser = new HashMap<Long, Double>();

        int nUsers = 0;
        for (long user : data.keySet()) {
            List<Double> sortedList = data.get(user);
            // number of relevant items for this user
            double uRel = getNumberOfRelevantItems(user);
            double urec = 0.0;
            int rank = 0;
            for (double rel : sortedList) {
                rank++;
                urec += computeBinaryPrecision(rel);
                // compute at a particular cutoff
                for (int at : ats) {
                    if (rank == at) {
                        Map<Long, Double> m = userRecallAtCutoff.get(at);
                        if (m == null) {
                            m = new HashMap<Long, Double>();
                            userRecallAtCutoff.put(at, m);
                        }
                        m.put(user, urec / uRel);
                    }
                }
            }
            // normalize by number of relevant items
            urec /= uRel;
            // assign the recall of the whole list to those cutoffs larger than the list's size
            for (int at : ats) {
                if (rank <= at) {
                    Map<Long, Double> m = userRecallAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<Long, Double>();
                        userRecallAtCutoff.put(at, m);
                    }
                    m.put(user, urec);
                }
            }
            if (!Double.isNaN(urec)) {
                value += urec;
                metricPerUser.put(user, urec);
                nUsers++;
            }
        }
        value = value / nUsers;
    }

    /**
     * Method to return the recall value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the recall corresponding to the requested cutoff level
     */
    @Override
    public double getValueAt(int at) {
        if (userRecallAtCutoff.containsKey(at)) {
            int n = 0;
            double rec = 0.0;
            for (long u : userRecallAtCutoff.get(at).keySet()) {
                double urec = getValueAt(u, at);
                if (!Double.isNaN(urec)) {
                    rec += urec;
                    n++;
                }
            }
            rec = (n == 0) ? 0.0 : rec / n;
            return rec;
        }
        return Double.NaN;
    }

    /**
     * Method to return the recall value at a particular cutoff level for a
     * given user.
     *
     * @param user the user
     * @param at cutoff level
     * @return the recall corresponding to the requested user at the cutoff
     * level
     */
    @Override
    public double getValueAt(long user, int at) {
        if (userRecallAtCutoff.containsKey(at) && userRecallAtCutoff.get(at).containsKey(user)) {
            double rec = userRecallAtCutoff.get(at).get(user);
            return rec;
        }
        return Double.NaN;
    }
}
