package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * An evaluation strategy where all the items are used as candidates.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class AllItems extends AbstractStrategy {

    /**
     * @see
     * AbstractStrategy#AbstractStrategy(net.recommenders.rival.core.DataModel,
     * net.recommenders.rival.core.DataModel, double)
     *
     * @param training The training set.
     * @param test The test set.
     * @param threshold The relevance threshold.
     */
    public AllItems(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<Long> getCandidateItemsToRank(Long user) {
        final Set<Long> items = getModelTrainingDifference(training, user);
        items.addAll(getModelTrainingDifference(test, user));
        return items;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return "AllItems_" + threshold;
    }
}
