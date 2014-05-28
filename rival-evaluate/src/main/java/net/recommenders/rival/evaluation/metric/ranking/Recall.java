package net.recommenders.rival.evaluation.metric.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

/**
 * Recall of a ranked list of items.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public class Recall extends AbstractRankingMetric implements EvaluationMetric<Long> {

    private Map<Integer, Map<Long, Double>> userRecallAtCutoff;

    /**
     * @inheritDoc
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
            int rank = 1;
            for (double rel : sortedList) {
                urec += computeRecall(rel);
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
                rank++;
            }
            // normalize by number of relevant items
            urec /= uRel;
            // assign the ndcg of the whole list to those cutoffs larger than the list's size
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
     * Method that computes the recall of a specific item, taking into account
     * its relevance value.
     *
     * @param rel the item's relevance
     * @return the recall of the item
     */
    private double computeRecall(double rel) {
        double prec = 0.0;
        if (rel >= relevanceThreshold) {
            prec = 1.0;
        }
        return prec;
    }

    /**
     * Method that computes the number of relevant items in the test set for a
     * user
     *
     * @param user a user
     * @return the number of relevant items the user has in the test set
     */
    private double getNumberOfRelevantItems(long user) {
        int n = 0;
        if (test.getUserItemPreferences().containsKey(user)) {
            for (Entry<Long, Double> e : test.getUserItemPreferences().get(user).entrySet()) {
                if (e.getValue() >= relevanceThreshold) {
                    n++;
                }
            }
        }
        return n * 1.0;
    }

    /**
     * Method to return the recall value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the recall corresponding to the requested cutoff level
     */
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

    public double getValueAt(long user, int at) {
        if (userRecallAtCutoff.containsKey(at) && userRecallAtCutoff.get(at).containsKey(user)) {
            double rec = userRecallAtCutoff.get(at).get(user);
            return rec;
        }
        return Double.NaN;
    }
}
