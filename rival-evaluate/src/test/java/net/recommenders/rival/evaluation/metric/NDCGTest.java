package net.recommenders.rival.evaluation.metric;
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
     * Dummy test set
     */
    private DataModel test = new DataModel();

    /**
     * Dummy recommendation set
     */
    private DataModel predictions = new DataModel();

    /**
     * NDCG
     */
    NDCG ndcg;
    @Before
    public void initialize(){
        predictions = new DataModel();
        test = new DataModel();
        for (int i = 1; i < 20; i ++){
            for (int j = 1; j < 15; j++){
                test.addPreference((long) i, (long) j, (double) i * j % 5);
                predictions.addPreference((long) i, (long) j, (double) i * j % 5);
            }
        }
        ndcg = new NDCG(predictions, test);
        ndcg.compute();
    }
    @Test
    public void testComputeNDCG(){
        ndcg = new NDCG(predictions, test);
        assertEquals(1.0, ndcg.compute(), 0.0);
    }

    @Test
    public void testGetValue(){
        assertEquals(1.0, ndcg.getValue(), 0.0);
    }

    @Test
    public void testGetValuePerUser(){
        Map<Long, Double> ndcgPerUser = ndcg.getValuePerUser();
        for (Map.Entry<Long, Double> e : ndcgPerUser.entrySet()){
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(1.0, value, 0.0);
        }
    }
}
