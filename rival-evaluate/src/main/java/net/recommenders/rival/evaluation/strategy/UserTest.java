package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * An evaluation strategy where only the items in the user's test are used as
 * candidates.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class UserTest extends AbstractStrategy {

    /**
     * @see
     * AbstractStrategy#AbstractStrategy(net.recommenders.rival.core.DataModel,
     * net.recommenders.rival.core.DataModel, double)
     *
     * @param training The training set.
     * @param test The test set.
     * @param threshold The relevance threshold.
     */
    public UserTest(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<Long> getCandidateItemsToRank(Long user) {
        return test.getUserItemPreferences().get(user).keySet();
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return "UserTest_" + threshold;
    }
}
