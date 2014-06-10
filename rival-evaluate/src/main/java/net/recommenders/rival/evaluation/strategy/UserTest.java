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
     * @inheritDoc
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
}
