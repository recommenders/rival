package net.recommenders.rival.evaluation.metric.ranking;

import net.recommenders.rival.evaluation.metric.*;
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
public abstract class AbstractRankingMetric extends AbstractMetric implements EvaluationMetric<Long> {

    /**
     * Global metric value
     */
    protected double value;
    /**
     * Array of cutoff levels
     */
    protected int[] ats;
    protected double relevanceThreshold;

    /**
     * @inheritDoc
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
     * @inheritDoc
     */
    @Override
    public double getValue() {
        return value;
    }
}
