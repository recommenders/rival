package net.recommenders.evaluation.strategy;

import java.util.Set;
import net.recommenders.evaluation.core.DataModel;

/**
 *
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
