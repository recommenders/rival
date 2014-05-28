package net.recommenders.rival.evaluation.metric.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

/**
 * Precision of a ranked list of items.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public class Precision extends AbstractRankingMetric implements EvaluationMetric<Long> {

    private Map<Integer, Map<Long, Double>> userPrecAtCutoff;

    /**
     * @inheritDoc
     */
    public Precision(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        this(predictions, test, 1.0);
    }

    /**
     * Constructor where the relevance threshold can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     */
    public Precision(DataModel<Long, Long> predictions, DataModel<Long, Long> test, double relThreshold) {
        this(predictions, test, relThreshold, new int[]{});
    }

    /**
     * Constructor where the cutoff levels can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param ats cutoffs
     */
    public Precision(DataModel<Long, Long> predictions, DataModel<Long, Long> test, double relThreshold, int[] ats) {
        super(predictions, test, relThreshold, ats);
    }

    /**
     * Computes the global precision by first summing the precision for each user and then
     * averaging by the number of users.
     */
    @Override
    public void compute() {
        value = 0.0;
        Map<Long, List<Double>> data = processDataAsRankedTestRelevance();
        userPrecAtCutoff = new HashMap<Integer, Map<Long, Double>>();
        metricPerUser = new HashMap<Long, Double>();

        int nUsers = 0;
        for (long user : data.keySet()) {
            List<Double> sortedList = data.get(user);
            double uprec = 0.0;
            int rank = 1;
            for (double rel : sortedList) {
                uprec += computePrecision(rel);
                // compute at a particular cutoff
                for (int at : ats) {
                    if (rank == at) {
                        Map<Long, Double> m = userPrecAtCutoff.get(at);
                        if (m == null) {
                            m = new HashMap<Long, Double>();
                            userPrecAtCutoff.put(at, m);
                        }
                        m.put(user, uprec / rank);
                    }
                }
                rank++;
            }
            // normalize by list size
            uprec /= rank;
            // assign the ndcg of the whole list to those cutoffs larger than the list's size
            for (int at : ats) {
                if (rank <= at) {
                    Map<Long, Double> m = userPrecAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<Long, Double>();
                        userPrecAtCutoff.put(at, m);
                    }
                    m.put(user, uprec);
                }
            }
            if (!Double.isNaN(uprec)) {
                value += uprec;
                metricPerUser.put(user, uprec);
                nUsers++;
            }
        }
        value = value / nUsers;
    }

    /**
     * Method that computes the precision of a specific item, taking into
     * account its relevance value.
     *
     * @param rel the item's relevance
     * @return the precision of the item
     */
    private double computePrecision(double rel) {
        double prec = 0.0;
        if (rel >= relevanceThreshold) {
            prec = 1.0;
        }
        return prec;
    }

    /**
     * Method to return the precision value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the precision corresponding to the requested cutoff level
     */
    public double getValueAt(int at) {
        if (userPrecAtCutoff.containsKey(at)) {
            int n = 0;
            double prec = 0.0;
            for (long u : userPrecAtCutoff.get(at).keySet()) {
                double uprec = getValueAt(u, at);
                if (!Double.isNaN(uprec)) {
                    prec += uprec;
                    n++;
                }
            }
            prec = (n == 0) ? 0.0 : prec / n;
            return prec;
        }
        return Double.NaN;
    }

    public double getValueAt(long user, int at) {
        if (userPrecAtCutoff.containsKey(at) && userPrecAtCutoff.get(at).containsKey(user)) {
            double prec = userPrecAtCutoff.get(at).get(user);
            return prec;
        }
        return Double.NaN;
    }
}
