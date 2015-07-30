package net.recommenders.rival.evaluation.metric.ranking;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.AbstractMetric;
import net.recommenders.rival.evaluation.metric.EvaluationMetric;
import net.recommenders.rival.evaluation.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which represents all the basic elements of a ranking based
 * metric.
 *
 * @param <U> - type associated to users' ids
 * @param <I> - type associated to items' ids
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
public abstract class AbstractRankingMetric<U, I> extends AbstractMetric<U, I> implements EvaluationMetric<U> {

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
    public AbstractRankingMetric(DataModel<U, I> predictions, DataModel<U, I> test) {
        this(predictions, test, 1.0);
    }

    /**
     * Constructor where the relevance threshold can be initialized
     *
     * @param predictions predicted ratings
     * @param test groundtruth ratings
     * @param relThreshold relevance threshold
     */
    public AbstractRankingMetric(DataModel<U, I> predictions, DataModel<U, I> test, double relThreshold) {
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
    public AbstractRankingMetric(DataModel<U, I> predictions, DataModel<U, I> test, double relThreshold, int[] ats) {
        super(predictions, test);
        this.value = Double.NaN;
        this.ats = Arrays.copyOf(ats, ats.length);
        this.relevanceThreshold = relThreshold;
    }

    /**
     * Method that transforms the user data from pairs of (item, score) into
     * ranked lists of relevance values, by using ground truth information.
     *
     * @return a map with the transformed data, one list per user
     */
    public Map<U, List<Pair<I, Double>>> processDataAsRankedTestRelevance() {
        Map<U, List<Pair<I, Double>>> data = new HashMap<U, List<Pair<I, Double>>>();

        Map<U, Map<I, Double>> predictedRatings = predictions.getUserItemPreferences();
        for (U testUser : test.getUsers()) {
            Map<I, Double> userPredictedRatings = predictedRatings.get(testUser);
            Map<I, Double> userRelevance = test.getUserItemPreferences().get(testUser);
            if (userPredictedRatings != null) {
                List<Pair<I, Double>> rankedTestRel = new ArrayList<Pair<I, Double>>();
                for (I item : rankItems(userPredictedRatings)) {
                    double rel = 0.0;
                    if (userRelevance.containsKey(item)) {
                        rel = userRelevance.get(item);
                    }
                    rankedTestRel.add(new Pair<I, Double>(item, rel));
                }
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
    protected double getNumberOfRelevantItems(U user) {
        int n = 0;
        if (test.getUserItemPreferences().containsKey(user)) {
            for (Map.Entry<I, Double> e : test.getUserItemPreferences().get(user).entrySet()) {
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
     * Returns the array of cutoff levels where this metric has computed values
     * at.
     */
    public int[] getCutoffs() {
        return ats;
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
    public abstract double getValueAt(U user, int at);
}
