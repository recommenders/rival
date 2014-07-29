package net.recommenders.rival.evaluation.metric.divnov;

import net.recommenders.rival.core.DataModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * The same examples as in Vargas and Castells @ Recsys2014, section 8
 * 
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
@RunWith(JUnit4.class)
public class EPCTest {

    @Test
    public void testR1() {
        DataModel<Long, Long> training = new DataModel<Long, Long>();
        int[] pop = new int[]{1000, 1000, 500, 500, 10, 10, 10, 10, 10, 10};
        for (int i = 0; i < pop.length; i++) {
            for (int u = 0; u < pop[i]; u++) {
                training.addPreference((long) u, (long) i, 1.0);
            }
        }
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        for (int i = 0; i < pop.length; i++) {
            predictions.addPreference(0L, (long) i, 1 / (i + 1.0));
            test.addPreference(0L, (long) i, 1.0);
        }
        
        EPC epc = new EPC(predictions, test, new int[]{10}, training);
        epc.compute();
        
        Assert.assertEquals(0.6940, epc.getValueAt(10), 0.0001);
    }

    @Test
    public void testR2() {
        DataModel<Long, Long> training = new DataModel<Long, Long>();
        int[] pop = new int[]{10, 10, 10, 500, 500, 1000, 1000, 1000, 10, 10};
        for (int i = 0; i < pop.length; i++) {
            for (int u = 0; u < pop[i]; u++) {
                training.addPreference((long) u, (long) i, 1.0);
            }
        }
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        for (int i = 0; i < pop.length; i++) {
            predictions.addPreference(0L, (long) i, 1 / (i + 1.0));
            test.addPreference(0L, (long) i, 1.0);
        }
        
        EPC epc = new EPC(predictions, test, new int[]{10}, training);
        epc.compute();
        
        Assert.assertEquals(0.5950, epc.getValueAt(10), 0.0001);
    }
}
