package net.recommenders.rival.split.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 *
 * @author Alejandro
 */
public class AllItems extends AbstractStrategy {

    public AllItems(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

    public Set<Long> getCandidateItemsToRank(Long user) {
        final Set<Long> items = getModelTrainingDifference(training, user);
        items.addAll(getModelTrainingDifference(test, user));
        return items;
    }
}
