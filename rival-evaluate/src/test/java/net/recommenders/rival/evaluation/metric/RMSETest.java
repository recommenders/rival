package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.error.RMSE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
@RunWith(JUnit4.class)
public class RMSETest<U, I> {

    @Test
    public void testSameGroundtruthAsPredictions() {
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                test.addPreference((long) i, (long) j, (double) i * j);
                predictions.addPreference((long) i, (long) j, (double) i * j);
            }
        }
        RMSE<Long, Long> rmse = new RMSE<Long, Long>(predictions, test);

        rmse.compute();

        assertEquals(0.0, rmse.getValue(), 0.0);

        Map<Long, Double> rmsePerUser = rmse.getValuePerUser();
        for (Map.Entry<Long, Double> e : rmsePerUser.entrySet()) {
            double value = e.getValue();
            assertEquals(0.0, value, 0.0);
        }
    }
}