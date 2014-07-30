package net.recommenders.rival.evaluation.metric.divnov;

import net.recommenders.rival.core.DataModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
@RunWith(JUnit4.class)
public class GiniIndexTest {
    
    @Test
    public void test() {
        int nItems = 10;
        DataModel<Long, Long> system1 = new DataModel<Long, Long>();
        DataModel<Long, Long> system2 = new DataModel<Long, Long>();
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        for (long u = 0L; u < 5L; u++) {
            system1.addPreference(u, 1L, 1.0);
            system1.addPreference(u, 2L, 1.0);
            system2.addPreference(u, u + 1L, 1.0);
            system2.addPreference(u, u + 6L, 1.0);
            test.addPreference(u, 0L, 1.0);
        }
        
        GiniIndex giniIndex1 = new GiniIndex(system1, test, new int[]{2}, nItems);
        giniIndex1.compute();
        
        Assert.assertEquals(0.1111, giniIndex1.getValueAt(2), 0.0001);

        GiniIndex giniIndex2 = new GiniIndex(system2, test, new int[]{2}, nItems);
        giniIndex2.compute();
        
        Assert.assertEquals(1.0000, giniIndex2.getValueAt(2), 0.0001);
    }
}
