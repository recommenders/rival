package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.evaluation.metric.ranking.NDCG;
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
public class NDCGTest<U, V> {

    /**
     * NDCG
     */
    private NDCG ndcg;

    @Before
    public void initialize() {
        DataModel predictions = new DataModel();
        DataModel test = new DataModel();
        for (int i = 1; i < 20; i++) {
            for (int j = 1; j < 15; j++) {
                test.addPreference((long) i, (long) j, (double) i * j % 5 + 1.0);
                predictions.addPreference((long) i, (long) j, (double) i * j % 5 + 1.0);
            }
        }
        ndcg = new NDCG(predictions, test, new int[]{5, 10, 20});
    }

    @Test
    public void testComputeNDCG() {
        ndcg.compute();
        assertEquals(1.0, ndcg.getValue(), 0.0);
    }

    @Test
    public void testComputeNDCGAt() {
        ndcg.compute();
        assertEquals(1.0, ndcg.getValueAt(5), 0.0);
        assertEquals(1.0, ndcg.getValueAt(10), 0.0);
    }

    @Test
    public void testGetValuePerUser() {
        ndcg.compute();
        Map<Long, Double> ndcgPerUser = ndcg.getValuePerUser();
        for (Map.Entry<Long, Double> e : ndcgPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(1.0, value, 0.0);
        }
    }
}
