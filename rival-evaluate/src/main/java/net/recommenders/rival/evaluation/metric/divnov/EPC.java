package net.recommenders.rival.evaluation.metric.divnov;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.core.DataModel;

/**
 * Expected Popularity Complement novelty metric
 *
 * See Vargas and Castells @ RecSys 2011
 *
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public class EPC extends AbstractNoveltyMetric {

    private final Map<Long, Double> itemNovelty;

    public EPC(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int[] ats, DataModel<Long, Long> training) {
        super(predictions, test, ats);

        itemNovelty = new HashMap<Long, Double>();
        int numUsers = training.getNumUsers();
        for (Long i : training.getItems()) {
            int n = training.getItemUserPreferences().get(i).size();
            itemNovelty.put(i, 1 - n / (double) numUsers);
        }
    }

    @Override
    protected double itemNovelty(Long i, Long user, List<Long> recList, int rank) {
        if (itemNovelty.containsKey(i)) {
            return itemNovelty.get(i);
        } else {
            return 1.0;
        }
    }

    @Override
    protected double norm(Long user, int at) {
        return at;
    }

}
