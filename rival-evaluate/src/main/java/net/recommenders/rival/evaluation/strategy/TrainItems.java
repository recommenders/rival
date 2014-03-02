package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * An evaluation strategy where only the items in training are used as candidates.
 *
 * @author Alejandro
 */
public class TrainItems extends AbstractStrategy {

    public TrainItems(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<Long> getCandidateItemsToRank(Long user) {
        return getModelTrainingDifference(training, user);
    }
}
