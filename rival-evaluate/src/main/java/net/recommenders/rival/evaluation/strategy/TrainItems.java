package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * @inheritDoc
 * @author Alejandro
 */
public class TrainItems extends AbstractStrategy {

    public TrainItems(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    public Set<Long> getCandidateItemsToRank(Long user) {
        return getModelTrainingDifference(training, user);
    }
}
