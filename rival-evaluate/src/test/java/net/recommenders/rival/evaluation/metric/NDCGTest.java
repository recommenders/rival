package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.evaluation.metric.ranking.NDCG;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
@RunWith(JUnit4.class)
public class NDCGTest {

    @Test
    public void testSameGroundtruthAsPredictions() {
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        for (long i = 1L; i < 20; i++) {
            for (long j = 1L; j < 15; j++) {
                test.addPreference(i, j, i * j % 5 + 1.0);
                predictions.addPreference(i, j, i * j % 5 + 1.0);
            }
        }
        NDCG<Long, Long> ndcg = new NDCG<Long, Long>(predictions, test, new int[]{5, 10, 20});

        ndcg.compute();

        assertEquals(1.0, ndcg.getValue(), 0.0);
        assertEquals(1.0, ndcg.getValueAt(5), 0.0);
        assertEquals(1.0, ndcg.getValueAt(10), 0.0);

        Map<Long, Double> ndcgPerUser = ndcg.getValuePerUser();
        for (Map.Entry<Long, Double> e : ndcgPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(1.0, value, 0.0);
        }
    }

    @Test
    public void testOrderOneUserTrecevalStrategy() {
        // groundtruth: 0 1 1 1
        // predictions: 0 1 1 1
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        test.addPreference(1L, 1L, 0.0);
        test.addPreference(1L, 2L, 1.0);
        test.addPreference(1L, 3L, 1.0);
        test.addPreference(1L, 4L, 1.0);
        predictions.addPreference(1L, 3L, 1.0);
        predictions.addPreference(1L, 2L, 1.0);
        predictions.addPreference(1L, 4L, 1.0);
        predictions.addPreference(1L, 1L, 0.0);

        NDCG<Long, Long> ndcg = new NDCG<Long, Long>(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5}, NDCG.TYPE.TREC_EVAL);

        ndcg.compute();

        assertEquals(1.0, ndcg.getValue(), 0.001);
        
        // change the order of the predictions:
        // groundtruth: 0 0 1 1
        // predictions: 0 1 1 1
        test = new DataModel<Long, Long>();
        predictions = new DataModel<Long, Long>();
        test.addPreference(1L, 1L, 0.0);
        test.addPreference(1L, 2L, 0.0);
        test.addPreference(1L, 3L, 1.0);
        test.addPreference(1L, 4L, 1.0);
        predictions.addPreference(1L, 4L, 1.0);
        predictions.addPreference(1L, 3L, 1.0);
        predictions.addPreference(1L, 2L, 1.0);
        predictions.addPreference(1L, 1L, 0.0);

        ndcg = new NDCG<Long, Long>(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5}, NDCG.TYPE.TREC_EVAL);

        ndcg.compute();

        assertEquals(1.0, ndcg.getValue(), 0.001);
        // groundtruth: 0 1 1 0
        // predictions: 0 1 1 1
        test = new DataModel<Long, Long>();
        predictions = new DataModel<Long, Long>();
        test.addPreference(1L, 1L, 0.0);
        test.addPreference(1L, 20L, 0.0);
        test.addPreference(1L, 3L, 1.0);
        test.addPreference(1L, 4L, 1.0);
        predictions.addPreference(1L, 20L, 1.0);
        predictions.addPreference(1L, 4L, 1.0);
        predictions.addPreference(1L, 3L, 1.0);
        predictions.addPreference(1L, 1L, 0.0);

        ndcg = new NDCG<Long, Long>(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5}, NDCG.TYPE.TREC_EVAL);

        ndcg.compute();

        assertEquals(0.693, ndcg.getValue(), 0.001);
    }

    @Test
    public void testOneUserTrecevalStrategySingleRelevance() {
        // groundtruth: ? 0 1 1
        // predictions: 3 4 5 1
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        test.addPreference(1L, 2L, 0.0);
        test.addPreference(1L, 3L, 1.0);
        test.addPreference(1L, 4L, 1.0);
        predictions.addPreference(1L, 1L, 3.0);
        predictions.addPreference(1L, 2L, 4.0);
        predictions.addPreference(1L, 3L, 5.0);
        predictions.addPreference(1L, 4L, 1.0);

        NDCG<Long, Long> ndcg = new NDCG<Long, Long>(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5}, NDCG.TYPE.TREC_EVAL);

        ndcg.compute();

        assertEquals(0.8772, ndcg.getValue(), 0.001);
        assertEquals(1.0, ndcg.getValueAt(1), 0.001);
        assertEquals(0.6131, ndcg.getValueAt(2), 0.001);
        assertEquals(0.6131, ndcg.getValueAt(3), 0.001);
        assertEquals(0.8772, ndcg.getValueAt(4), 0.001);
        assertEquals(0.8772, ndcg.getValueAt(5), 0.001);

        Map<Long, Double> ndcgPerUser = ndcg.getValuePerUser();
        for (Map.Entry<Long, Double> e : ndcgPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(0.8772, value, 0.001);
        }
    }

    @Test
    public void testOneUserTrecevalStrategyMultipleRelevance() {
        // groundtruth: ? 0 1 2
        // predictions: 3 4 5 1
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        test.addPreference(1L, 2L, 0.0);
        test.addPreference(1L, 3L, 1.0);
        test.addPreference(1L, 4L, 2.0);
        predictions.addPreference(1L, 1L, 3.0);
        predictions.addPreference(1L, 2L, 4.0);
        predictions.addPreference(1L, 3L, 5.0);
        predictions.addPreference(1L, 4L, 1.0);

        NDCG<Long, Long> ndcg = new NDCG<Long, Long>(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5}, NDCG.TYPE.TREC_EVAL);

        ndcg.compute();

        assertEquals(0.7075, ndcg.getValue(), 0.001);
        assertEquals(0.5, ndcg.getValueAt(1), 0.001);
        assertEquals(0.3801, ndcg.getValueAt(2), 0.001);
        assertEquals(0.3801, ndcg.getValueAt(3), 0.001);
        assertEquals(0.7075, ndcg.getValueAt(4), 0.001);
        assertEquals(0.7075, ndcg.getValueAt(5), 0.001);

        Map<Long, Double> ndcgPerUser = ndcg.getValuePerUser();
        for (Map.Entry<Long, Double> e : ndcgPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(0.7075, value, 0.001);
        }
    }
}
