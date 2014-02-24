package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * @inheritDoc
 * @author Alejandro
 */
public class AllItems extends AbstractStrategy {

    /**
     * Default constructor.
     * @param training  The training data.
     * @param test  The test data.
     * @param threshold The relevance threshold.
     */
    public AllItems(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    /**
     * Get the candidate items to perform ranking for.
     * @param user  The user.
     * @return  The items to rank.
     */
    public Set<Long> getCandidateItemsToRank(Long user) {
        final Set<Long> items = getModelTrainingDifference(training, user);
        items.addAll(getModelTrainingDifference(test, user));
        return items;
    }
}
