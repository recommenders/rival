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
public class EPDTest {

    @Test
    public void test() {
        DataModel<Long, Long> training = new DataModel<Long, Long>();
        for (int i = 0; i < 5; i++) {
            training.addPreference(0L, (long) i, 1 / (i + 1.0));
            training.addPreference(1L, (long) 2 * i, 1 / (i + 1.0));
            training.addPreference(2L, (long) 2 * i, 1 / (i + 1.0));
        }
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        for (int i = 0; i < 10; i++) {
            predictions.addPreference(0L, (long) i, 1 / (i + 1.0));
            test.addPreference(0L, (long) i, 1.0);
            predictions.addPreference(1L, (long) 2 * i, 1 / (i + 1.0));
            test.addPreference(1L, (long) 2 * i, 1.0);
            predictions.addPreference(2L, (long) 2 * i + 1, 1 / (i + 1.0));
            test.addPreference(2L, (long) 2 * i + 1, 1.0);
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
        
        EPD epd = new EPD(predictions, test, new int[]{10}, training, dist);
        epd.compute();
        
        Assert.assertEquals(0.5000, epd.getValueAt(0L, 10), 0.0001);
        Assert.assertEquals(0.0000, epd.getValueAt(1L, 10), 0.0001);
        Assert.assertEquals(1.0000, epd.getValueAt(2L, 10), 0.0001);
    }
}
