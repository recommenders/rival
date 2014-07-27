package net.recommenders.rival.evaluation.metric.divnov;

import java.util.List;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.divnov.dist.ItemDistance;

/**
 * Expected Profile Distance novelty metric
 *
 * See Vargas and Castells @ RecSys 2011
 *
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public class EPD extends AbstractNoveltyMetric {

    private final DataModel<Long, Long> training;
    private final ItemDistance<Long> dist;

    public EPD(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats, DataModel<Long, Long> training, ItemDistance<Long> dist) {
        super(predictions, test, ats);
        this.training = training;
        this.dist = dist;
    }

    @Override
    protected double itemNovelty(Long i, Long user, List<Long> recList, int rank) {
        double nov = 0.0;
        for (Long j : training.getUserItemPreferences().get(user).keySet()) {
            nov += dist.dist(i, j);
        }

        return nov;
    }

    @Override
    protected double norm(Long user, int at) {
        return at * training.getUserItemPreferences().get(user).size();
    }

}
