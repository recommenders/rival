package net.recommenders.rival.evaluation.metric;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;

import net.recommenders.rival.core.DataModel;

import java.util.Map;
import net.recommenders.rival.evaluation.metric.ranking.MAP;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
@RunWith(JUnit4.class)
public class MAPTest {

    @Test
    public void testSameGroundtruthAsPredictions() {
        DataModel<Long, Long> predictions = new DataModel<Long, Long>();
        DataModel<Long, Long> test = new DataModel<Long, Long>();
        int nUsers = 20;
        int nItems = 15;
        for (long i = 1L; i < nUsers + 1; i++) {
            for (long j = 1L; j < nItems + 1; j++) {
                test.addPreference(i, j, i * j % 5 + 1.0);
                predictions.addPreference(i, j, i * j % 5 + 1.0);
            }
        }
        MAP map = new MAP(predictions, test, 1.0, new int[]{1, 5, 10, 20});

        map.compute();

        assertEquals(1.0, map.getValue(), 0.0);
        assertEquals(1.0 / nItems, map.getValueAt(1), 0.0001);
        assertEquals(1.0 / nItems * 5, map.getValueAt(5), 0.0001);
        assertEquals(1.0 / nItems * 10, map.getValueAt(10), 0.0001);

        Map<Long, Double> mapPerUser = map.getValuePerUser();
        for (Map.Entry<Long, Double> e : mapPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(1.0, value, 0.0);
        }
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

        MAP map = new MAP(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5});

        map.compute();

        assertEquals(0.75, map.getValue(), 0.001);
        assertEquals(0.5, map.getValueAt(1), 0.001);
        assertEquals(0.5, map.getValueAt(2), 0.001);
        assertEquals(0.5, map.getValueAt(3), 0.001);
        assertEquals(0.75, map.getValueAt(4), 0.001);
        assertEquals(0.75, map.getValueAt(5), 0.001);

        Map<Long, Double> mapPerUser = map.getValuePerUser();
        for (Map.Entry<Long, Double> e : mapPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(0.75, value, 0.001);
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

        MAP map = new MAP(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5});

        map.compute();

        assertEquals(0.75, map.getValue(), 0.001);
        assertEquals(0.5, map.getValueAt(1), 0.001);
        assertEquals(0.5, map.getValueAt(2), 0.001);
        assertEquals(0.5, map.getValueAt(3), 0.001);
        assertEquals(0.75, map.getValueAt(4), 0.001);
        assertEquals(0.75, map.getValueAt(5), 0.001);

        Map<Long, Double> mapPerUser = map.getValuePerUser();
        for (Map.Entry<Long, Double> e : mapPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(0.75, value, 0.001);
        }
    }
}
