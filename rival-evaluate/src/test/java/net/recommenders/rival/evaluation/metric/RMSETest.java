package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.evaluation.metric.error.RMSE;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;

import net.recommenders.rival.core.DataModel;

import java.util.Map;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
@RunWith(JUnit4.class)
public class RMSETest<U, I> {

    @Test
    public void testSameGroundtruthAsPredictions() {
        DataModel predictions = new DataModel();
        DataModel test = new DataModel();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                test.addPreference((long) i, (long) j, (double) i * j);
                predictions.addPreference((long) i, (long) j, (double) i * j);
            }
        }
        RMSE rmse = new RMSE(predictions, test);

        rmse.compute();

        assertEquals(0.0, rmse.getValue(), 0.0);

        Map<Long, Double> rmsePerUser = rmse.getValuePerUser();
        for (Map.Entry<Long, Double> e : rmsePerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(0.0, value, 0.0);
        }
    }
}