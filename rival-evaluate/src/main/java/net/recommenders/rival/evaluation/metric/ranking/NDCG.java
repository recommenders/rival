package net.recommenders.rival.evaluation.metric.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

/**
 * Normalized <a href="http://recsyswiki.com/wiki/Discounted_Cumulative_Gain"
 * target="_blank">discounted cumulative gain</a> (NDCG) of a ranked list of
 * items.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public class NDCG extends AbstractRankingMetric implements EvaluationMetric<Long> {

    /**
     * Type of nDCG computation (linear or exponential)
     */
    public static enum TYPE {

        LIN,
        EXP;
    }
    /**
     * Type of nDCG computation (linear or exponential)
     */
    private TYPE type;
    private Map<Integer, Map<Long, Double>> userDcgAtCutoff;
    private Map<Integer, Map<Long, Double>> userIdcgAtCutoff;


    public NDCG(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        this(predictions, test, new int[]{});
    }

    /**
     * Constructor where the cutoff levels can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param ats cutoffs
     */
    public NDCG(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats) {
        this(predictions, test, ats, TYPE.EXP, 1.0);
    }

    /**
     * Constructor where the cutoff levels and the type of NDCG computation can
     * be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param ats cutoffs
     * @param type type of NDCG computation
     */
    public NDCG(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats, TYPE type, double relThreshold) {
        super(predictions, test, relThreshold, ats);
        this.type = type;
    }

    /**
     * Computes the global NDCG by first summing the NDCG for each user and then
     * averaging by the number of users.
     */
    @Override
    public void compute() {
        value = 0.0;
        Map<Long, List<Double>> data = processDataAsRankedTestRelevance();
        userDcgAtCutoff = new HashMap<Integer, Map<Long, Double>>();
        userIdcgAtCutoff = new HashMap<Integer, Map<Long, Double>>();
        metricPerUser = new HashMap<Long, Double>();

        int nUsers = 0;
        for (long user : data.keySet()) {
            List<Double> sortedList = data.get(user);
            double dcg = 0.0;
            int rank = 1;
            for (double rel : sortedList) {
                dcg += computeDCG(rel, rank);
                // compute at a particular cutoff
                for (int at : ats) {
                    if (rank == at) {
                        Map<Long, Double> m = userDcgAtCutoff.get(at);
                        if (m == null) {
                            m = new HashMap<Long, Double>();
                            userDcgAtCutoff.put(at, m);
                        }
                        m.put(user, dcg);
                    }
                }
                rank++;
            }
            // assign the ndcg of the whole list to those cutoffs larger than the list's size
            for (int at : ats) {
                if (rank <= at) {
                    Map<Long, Double> m = userDcgAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<Long, Double>();
                        userDcgAtCutoff.put(at, m);
                    }
                    m.put(user, dcg);
                }
            }
            double idcg = computeIDCG(user, test.getUserItemPreferences().get(user));
            double undcg = dcg / idcg;
            if (!Double.isNaN(undcg)) {
                value += undcg;
                metricPerUser.put(user, undcg);
                nUsers++;
            }
        }
        value = value / nUsers;
    }

    /**
     * Method that computes the discounted cumulative gain of a specific item,
     * taking into account its ranking in a user's list and its relevance value.
     *
     * @param rel the item's relevance
     * @param rank the item's rank in a user's list (sorted by predicted rating)
     * @return the dcg of the item
     */
    protected double computeDCG(double rel, int rank) {
        double dcg = 0.0;
        if (rel >= relevanceThreshold) {
            switch (type) {
                case EXP: {
                    dcg = (Math.pow(2.0, rel) - 1.0) / (Math.log(rank + 1) / Math.log(2));
                }
                break;
                case LIN: {
                    dcg = rel;
                    if (rank > 1) {
                        dcg /= (Math.log(rank) / Math.log(2));
                    }
                }
                break;
            }
        }
        return dcg;
    }

    /**
     * Computes the ideal <a
     * href="http://recsyswiki.com/wiki/Discounted_Cumulative_Gain"
     * target="_blank">discounted cumulative gain</a> (IDCG) given the test set
     * (groundtruth items) for a user.
     *
     * @user the user
     *
     * @param userTestItems the groundtruth items of a user.
     * @return the IDCG
     */
    private double computeIDCG(long user, Map<Long, Double> userTestItems) {
        double idcg = 0.0;
        // sort the items according to their relevance level
        List<Double> sortedList = rankScores(userTestItems);
        int rank = 1;
        for (double itemRel : sortedList) {
            idcg += computeDCG(itemRel, rank);
            // compute at a particular cutoff
            for (int at : ats) {
                if (rank == at) {
                    Map<Long, Double> m = userIdcgAtCutoff.get(at);
                    if (m == null) {
                        m = new HashMap<Long, Double>();
                        userIdcgAtCutoff.put(at, m);
                    }
                    m.put(user, idcg);
                }
            }
            rank++;
        }
        // assign the ndcg of the whole list to those cutoffs larger than the list's size
        for (int at : ats) {
            if (rank <= at) {
                Map<Long, Double> m = userIdcgAtCutoff.get(at);
                if (m == null) {
                    m = new HashMap<Long, Double>();
                    userIdcgAtCutoff.put(at, m);
                }
                m.put(user, idcg);
            }
        }
        return idcg;
    }

    /**
     * Method to return the NDCG value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the NDCG corresponding to the requested cutoff level
     */
    public double getValueAt(int at) {
        if (userDcgAtCutoff.containsKey(at) && userIdcgAtCutoff.containsKey(at)) {
            int n = 0;
            double ndcg = 0.0;
            for (long u : userIdcgAtCutoff.get(at).keySet()) {
                double udcg = getValueAt(u, at);
                if (!Double.isNaN(udcg)) {
                    ndcg += udcg;
                    n++;
                }
            }
            ndcg = (n == 0) ? 0.0 : ndcg / n;
            return ndcg;
        }
        return Double.NaN;
    }

    public double getValueAt(long user, int at) {
        if (userDcgAtCutoff.containsKey(at) && userDcgAtCutoff.get(at).containsKey(user)
                && userIdcgAtCutoff.containsKey(at) && userIdcgAtCutoff.get(at).containsKey(user)) {
            double idcg = userIdcgAtCutoff.get(at).get(user);
            double dcg = userDcgAtCutoff.get(at).get(user);
            return dcg / idcg;
        }
        return Double.NaN;
    }
}
