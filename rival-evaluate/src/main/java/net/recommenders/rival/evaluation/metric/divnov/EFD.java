package net.recommenders.rival.evaluation.metric.divnov;

import static java.lang.Math.log;
import static java.lang.Math.min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.core.DataModel;

/**
 * Expected Free Discovery novelty metric
 *
 * See Vargas and Castells @ RecSys 2011
 *
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public class EFD extends AbstractNoveltyMetric {

    private final Map<Long, Double> itemNovelty;
    private final double maxNov;

    public EFD(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats, DataModel<Long, Long> training) {
        super(predictions, test, ats);

        int norm = 0;
        double maxNov_ = Double.POSITIVE_INFINITY;
        for (Long i : training.getItems()) {
            int n = training.getItemUserPreferences().get(i).size();
            norm += n;
            maxNov_ = min(maxNov_, n);
        }
        maxNov = -log(maxNov_ / norm) / log(2);
        itemNovelty = new HashMap<Long, Double>();
        for (Long i : training.getItems()) {
            int n = training.getItemUserPreferences().get(i).size();
            itemNovelty.put(i, -log(n / (double) norm) / log(2));
        }
    }

    @Override
    protected double itemNovelty(Long i, Long user, List<Long> recList, int rank) {
        if (itemNovelty.containsKey(i)) {
            return itemNovelty.get(i);
        } else {
            return maxNov;
        }
    }

    @Override
    protected double norm(Long user, int at) {
        return at;
    }

}
