/*
 * Copyright 2015 recommenders.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.evaluation.metric.ranking.Recall;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;
import net.recommenders.rival.core.DataModelFactory;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link Recall}.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
@RunWith(JUnit4.class)
public class RecallTest {

    @Test
    public void testSameGroundtruthAsPredictions() {
        DataModelIF<Long, Long> predictions = DataModelFactory.getDefaultModel();
        DataModelIF<Long, Long> test = DataModelFactory.getDefaultModel();
        int nUsers = 20;
        int nItems = 15;
        for (long i = 1L; i < nUsers + 1; i++) {
            for (long j = 1L; j < nItems + 1; j++) {
                test.addPreference(i, j, i * j % 5 + 1.0);
                predictions.addPreference(i, j, i * j % 5 + 1.0);
            }
        }
        Recall<Long, Long> recall = new Recall<Long, Long>(predictions, test, 1.0, new int[]{5, 10, 20});

        recall.compute();

        assertEquals(1.0, recall.getValue(), 0.0);
        assertEquals(5.0 / nItems, recall.getValueAt(5), 0.0001);
        assertEquals(10.0 / nItems, recall.getValueAt(10), 0.0001);

        Map<Long, Double> recallPerUser = recall.getValuePerUser();
        for (Map.Entry<Long, Double> e : recallPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(1.0, value, 0.0);
        }
    }

    @Test
    public void testOneUserTrecevalStrategySingleRelevance() {
        // groundtruth: ? 0 1 1
        // predictions: 3 4 5 1
        DataModelIF<Long, Long> test = DataModelFactory.getDefaultModel();
        DataModelIF<Long, Long> predictions = DataModelFactory.getDefaultModel();
        test.addPreference(1L, 2L, 0.0);
        test.addPreference(1L, 3L, 1.0);
        test.addPreference(1L, 4L, 1.0);
        predictions.addPreference(1L, 1L, 3.0);
        predictions.addPreference(1L, 2L, 4.0);
        predictions.addPreference(1L, 3L, 5.0);
        predictions.addPreference(1L, 4L, 1.0);

        Recall<Long, Long> recall = new Recall<Long, Long>(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5});

        recall.compute();

        assertEquals(1.0, recall.getValue(), 0.001);
        assertEquals(0.5, recall.getValueAt(1), 0.001);
        assertEquals(0.5, recall.getValueAt(2), 0.001);
        assertEquals(0.5, recall.getValueAt(3), 0.001);
        assertEquals(1.0, recall.getValueAt(4), 0.001);
        assertEquals(1.0, recall.getValueAt(5), 0.001);

        Map<Long, Double> recallPerUser = recall.getValuePerUser();
        for (Map.Entry<Long, Double> e : recallPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(1.0, value, 0.001);
        }
    }

    @Test
    public void testOneUserTrecevalStrategyMultipleRelevance() {
        // groundtruth: ? 0 1 2
        // predictions: 3 4 5 1
        DataModelIF<Long, Long> test = DataModelFactory.getDefaultModel();
        DataModelIF<Long, Long> predictions = DataModelFactory.getDefaultModel();
        test.addPreference(1L, 2L, 0.0);
        test.addPreference(1L, 3L, 1.0);
        test.addPreference(1L, 4L, 2.0);
        predictions.addPreference(1L, 1L, 3.0);
        predictions.addPreference(1L, 2L, 4.0);
        predictions.addPreference(1L, 3L, 5.0);
        predictions.addPreference(1L, 4L, 1.0);

        Recall<Long, Long> recall = new Recall<Long, Long>(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5});

        recall.compute();

        assertEquals(1.0, recall.getValue(), 0.001);
        assertEquals(0.5, recall.getValueAt(1), 0.001);
        assertEquals(0.5, recall.getValueAt(2), 0.001);
        assertEquals(0.5, recall.getValueAt(3), 0.001);
        assertEquals(1.0, recall.getValueAt(4), 0.001);
        assertEquals(1.0, recall.getValueAt(5), 0.001);

        Map<Long, Double> recallPerUser = recall.getValuePerUser();
        for (Map.Entry<Long, Double> e : recallPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(1.0, value, 0.001);
        }
    }
}
