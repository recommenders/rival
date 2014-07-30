package net.recommenders.rival.evaluation.metric.divnov;

import java.util.List;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.divnov.dist.ItemDistance;

/**
 * Expected Intra List Distance diversity metric
 *
 * See Vargas and Castells @ RecSys 2011
 *
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public class EILD extends AbstractNoveltyMetric {

    private final ItemDistance<Long> dist;

    public EILD(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats,ItemDistance<Long> dist) {
        super(predictions, test, ats);
        this.dist = dist;
    }

    @Override
    protected double itemNovelty(Long i, Long user, List<Long> recList, int rank) {
        double nov = 0;
        for (int k = 0; k < rank - 1; k++) {
            nov += dist.dist(i, recList.get(k));
        }
        
        return nov;
    }

    @Override
    protected double norm(Long user, int at) {
        return at * (at - 1) / 2.0;
    }

}
