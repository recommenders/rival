package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * An evaluation strategy where all the items are used as candidates.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class AllItems extends AbstractStrategy {


    public AllItems(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        super(training, test, threshold);
    }

   @Override
    public Set<Long> getCandidateItemsToRank(Long user) {
        final Set<Long> items = getModelTrainingDifference(training, user);
        items.addAll(getModelTrainingDifference(test, user));
        return items;
    }
}
