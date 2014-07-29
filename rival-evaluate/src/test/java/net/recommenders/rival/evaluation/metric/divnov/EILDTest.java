package net.recommenders.rival.evaluation.metric.divnov;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.divnov.dist.ItemDistance;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
@RunWith(JUnit4.class)
public class EILDTest {

    @Test
    public void test() {
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        for (int i = 0; i < 10; i++) {
            predictions.addPreference(0L, (long) i, 1 / (i + 1.0));
            test.addPreference(0L, (long) i, 1.0);
            predictions.addPreference(1L, (long) 2 * i, 1 / (i + 1.0));
            test.addPreference(1L, (long) 2 * i, 1.0);
        }
        
        ItemDistance<Long> dist = new ItemDistance<Long>() {

            @Override
            public double dist(Long i, Long j) {
                if (i % 2 == j % 2) {
                    return 0.0;
                } else {
                    return 1.0;
                }
            }
        };
        
        EILD eild = new EILD(predictions, test, new int[]{10}, dist);
        eild.compute();
        
        Assert.assertEquals(0.5556, eild.getValueAt(0L, 10), 0.0001);
        Assert.assertEquals(0.0000, eild.getValueAt(1L, 10), 0.0001);
    }
}
