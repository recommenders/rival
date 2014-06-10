package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * An evaluation strategy where only the test items are used as candidates.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class TestItems extends AbstractStrategy {

    /**
     * @inheritDoc
     */
    public TestItems(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<Long> getCandidateItemsToRank(Long user) {
        return getModelTrainingDifference(test, user);
    }
}
