package net.recommenders.evaluation.strategy;

import java.util.Set;
import net.recommenders.evaluation.core.DataModel;

/**
 *
 * @author Alejandro
 */
public class UserTest extends AbstractStrategy {

    public UserTest(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    public Set<Long> getCandidateItemsToRank(Long user) {
        return test.getUserItemPreferences().get(user).keySet();
    }
}
