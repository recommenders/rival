package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * Representaton of test items.
 * @author Alejandro
 */
public class TestItems extends AbstractStrategy {

    /**
     * Default constructor.
     * @param training  The training data model.
     * @param test  The test data model.
     * @param threshold The relevance threshold.
     */
    public TestItems(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    /**
     * Get the condidate items to rank.
     * @param user  The user.
     * @return  The candidate items.
     */
    public Set<Long> getCandidateItemsToRank(Long user) {
        return getModelTrainingDifference(test, user);
    }
}
