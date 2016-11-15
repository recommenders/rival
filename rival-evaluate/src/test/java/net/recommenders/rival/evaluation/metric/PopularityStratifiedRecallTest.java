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

import java.util.HashMap;
import net.recommenders.rival.core.DataModelIF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.evaluation.metric.ranking.PopularityStratifiedRecall;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link PopularityStratifiedRecall}.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>.
 */
@RunWith(JUnit4.class)
public class PopularityStratifiedRecallTest {

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
        Map<Long, Integer> observedItemRelevance = new HashMap<Long, Integer>();
        for (long j = 1L; j < nItems + 1; j++) {
            observedItemRelevance.put(j, Long.valueOf(j).intValue());
        }

        double gamma = 0.0;
        PopularityStratifiedRecall<Long, Long> recall = new PopularityStratifiedRecall<Long, Long>(predictions, test, 1.0, new int[]{5, 10, 20}, gamma, observedItemRelevance);

        recall.compute();

        assertEquals(1.0, recall.getValue(), 0.0);
//        assertEquals(, recall.getValueAt(5), 0.0001);
//        assertEquals(, recall.getValueAt(10), 0.0001);

        Map<Long, Double> recallPerUser = recall.getValuePerUser();
        for (Map.Entry<Long, Double> e : recallPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(1.0, value, 0.0);
        }
    }
}
