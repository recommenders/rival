package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.evaluation.core.DataModel;

/**
 *
 * @author Alejandro
 */
public class TestItems extends AbstractStrategy {

    public TestItems(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    public Set<Long> getCandidateItemsToRank(Long user) {
        return getModelTrainingDifference(test, user);
    }
}
