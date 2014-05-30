package net.recommenders.rival.evaluation.metric.ranking;

import java.util.*;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.AbstractMetric;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;

/**
 * Normalized <a href="http://recsyswiki.com/wiki/Discounted_Cumulative_Gain"
 * target="_blank">discounted cumulative gain</a> (NDCG) of a ranked list of
 * items.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public abstract class AbstractRankingMetric extends AbstractMetric implements EvaluationMetric<Long> {

    /**
     * Global metric value
     */
    protected double value;
    /**
     * Array of cutoff levels
     */
    protected int[] ats;
    /**
     * Relevance threshold
     */
    protected double relevanceThreshold;

    /**
     * Default constructor with predictions and groundtruth information
     *
     * @param predictions predicted scores for users and items
     * @param test groundtruth information for users and items
     */
    public AbstractRankingMetric(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        this(predictions, test, 1.0);
    }

    /**
     * Constructor where the relevance threshold can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     */
    public AbstractRankingMetric(DataModel<Long, Long> predictions, DataModel<Long, Long> test, double relThreshold) {
        this(predictions, test, relThreshold, new int[]{});
    }

    /**
     * Constructor where the cutoff levels can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param ats cutoffs
     * @param relThreshold relevance threshold
     */
    public AbstractRankingMetric(DataModel<Long, Long> predictions, DataModel<Long, Long> test, double relThreshold, int[] ats) {
        super(predictions, test);
        this.value = Double.NaN;
        this.ats = ats;
        this.relevanceThreshold = relThreshold;
    }

    /**
     * Method that transforms the user data from pairs of <item, score> into
     * ranked lists of relevance values, by using groundtruth information.
     *
     * @return a map with the transformed data, one list per user
     */
    public Map<Long, List<Double>> processDataAsRankedTestRelevance() {
        Map<Long, List<Double>> data = new HashMap<Long, List<Double>>();

        Map<Long, Map<Long, Double>> predictedRatings = predictions.getUserItemPreferences();
        for (long testUser : test.getUsers()) {
            Map<Long, Double> userPredictedRatings = predictedRatings.get(testUser);
            if (userPredictedRatings != null) {
                List<Double> rankedTestRel = rankScores(userPredictedRatings);
                data.put(testUser, rankedTestRel);
            }
        }
        return data;
    }

    /**
     * Method that computes the number of relevant items in the test set for a
     * user
     *
     * @param user a user
     * @return the number of relevant items the user has in the test set
     */
    protected double getNumberOfRelevantItems(long user) {
        int n = 0;
        if (test.getUserItemPreferences().containsKey(user)) {
            for (Map.Entry<Long, Double> e : test.getUserItemPreferences().get(user).entrySet()) {
                if (e.getValue() >= relevanceThreshold) {
                    n++;
                }
            }
        }
        return n * 1.0;
    }

    /**
     * Method that computes the binary precision of a specific item, taking into
     * account its relevance value.
     *
     * @param rel the item's relevance
     * @return the binary precision of the item
     */
    protected double computeBinaryPrecision(double rel) {
        double prec = 0.0;
        if (rel >= relevanceThreshold) {
            prec = 1.0;
        }
        return prec;
    }

    /**
     * @inheritDoc
     */
    @Override
    public double getValue() {
        return value;
    }

    /**
     * Method to return the metric value at a particular cutoff level.
     *
     * @param at cutoff level
     * @return the metric corresponding to the requested cutoff level
     */
    public abstract double getValueAt(int at);

    /**
     * Method to return the metric value at a particular cutoff level for a
     * given user.
     *
     * @param user the user
     * @param at cutoff level
     * @return the metric corresponding to the requested user at the cutoff
     * level
     */
    public abstract double getValueAt(long user, int at);
}
