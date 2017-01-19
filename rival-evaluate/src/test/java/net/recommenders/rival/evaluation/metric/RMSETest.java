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
import net.recommenders.rival.evaluation.metric.error.AbstractErrorMetric;
import net.recommenders.rival.evaluation.metric.error.RMSE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;
import net.recommenders.rival.core.DataModelFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link RMSE}.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
@RunWith(JUnit4.class)
public class RMSETest<U, I> {


    @Test
    public void testSameGroundtruthAsPredictions() {
        DataModelIF<Long, Long> predictions = DataModelFactory.getDefaultModel();
        DataModelIF<Long, Long> test = DataModelFactory.getDefaultModel();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                test.addPreference((long) i, (long) j, (double) i * j);
                predictions.addPreference((long) i, (long) j, (double) i * j);
            }
        }
        RMSE<Long, Long> rmse = new RMSE<Long, Long>(predictions, test);

        RMSE<Long, Long> rmseStrat = new RMSE<Long, Long>(predictions, test, AbstractErrorMetric.ErrorStrategy.CONSIDER_EVERYTHING);
        assertNotNull(rmseStrat);

        assertEquals("RMSE_CONSIDER_EVERYTHING", rmseStrat.toString());

        rmse.compute();

        assertEquals(0.0, rmse.getValue(), 0.0);

        Map<Long, Double> rmsePerUser = rmse.getValuePerUser();
        for (Map.Entry<Long, Double> e : rmsePerUser.entrySet()) {
            double value = e.getValue();
            assertEquals(0.0, value, 0.0);
        }
    }
}