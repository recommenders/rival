package net.recommenders.rival.evaluation.metric;

import java.util.*;
import net.recommenders.rival.core.DataModel;

/**
 * Normalized <a href="http://recsyswiki.com/wiki/Discounted_Cumulative_Gain"
 * target="_blank">discounted cumulative gain</a> (NDCG) of a ranked list of
 * items.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public class NDCG extends AbstractMetric implements EvaluationMetric<Long> {

    /**
     * Type of nDCG computation (linear or exponential)
     */
    public static enum TYPE {

        LIN,
        EXP;
    }
    /**
     * Global NDCG
     */
    private double ndcg;
    /**
     * Array of cutoff levels
     */
    private int[] ats;
    /**
     * Type of nDCG computation (linear or exponential)
     */
    private TYPE type;
    private Map<Integer, Map<Long, Double>> userDcgAtCutoff;
    private Map<Integer, Map<Long, Double>> userIdcgAtCutoff;

    /**
     * @inheritDoc
     */
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
        this(predictions, test, ats, TYPE.EXP);
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
    public NDCG(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats, TYPE type) {
        super(predictions, test);
        this.ndcg = Double.NaN;
        this.ats = ats;
        this.type = type;
    }

    /**
     * Computes the global NDCG by first summing the NDCG for each user and then
     * averaging by the number of users.
     */
    @Override
    public void compute() {
        ndcg = 0.0;
        userDcgAtCutoff = new HashMap<Integer, Map<Long, Double>>();
        userIdcgAtCutoff = new HashMap<Integer, Map<Long, Double>>();
        metricPerUser = new HashMap<Long, Double>();

        int nUsers = 0;
        Set<Long> predictedUsers = predictions.getUsers();
        for (long user : predictedUsers) {
            Map<Long, Double> userTestItems = test.getUserItemPreferences().get(user);
            List<Long> sortedList = rankUserPredictions(user);
            double dcg = 0.0;
            int rank = 1;
            for (long item : sortedList) {
                dcg += computeDCG(item, rank, userTestItems);
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
            double idcg = computeIDCG(user, userTestItems);
            double undcg = dcg / idcg;
            ndcg += undcg;
            metricPerUser.put(user, undcg);
            nUsers++;
        }
        ndcg = ndcg / nUsers;
    }

    /**
     * Method that computes the discounted cumulative gain of a specific item,
     * taking into account its ranking in a user's list and its relevance.
     *
     * @param item the item
     * @param rank the item's rank in a user's list (sorted by predicted rating)
     * @param userTestItems groundtruth information for a specific user
     * @return the dcg of the item
     */
    protected double computeDCG(long item, int rank, Map<Long, Double> userTestItems) {
        double dcg = 0.0;
        if (userTestItems.containsKey(item)) {
            double rel = userTestItems.get(item);
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
        List<Long> sortedList = rankUserTest(user);
        int rank = 1;
        for (long item : sortedList) {
            idcg += computeDCG(item, rank, userTestItems);
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
        return idcg;
    }

    /**
     * @inheritDoc
     */
    @Override
    public double getValue() {
        return ndcg;
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
            ndcg /= n;
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
