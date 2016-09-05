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
import net.recommenders.rival.evaluation.metric.ranking.Precision;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.evaluation.metric.ranking.Recall;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link Precision}.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
@RunWith(JUnit4.class)
public class PrecisionTest {

    @Test
    public void testSameGroundtruthAsPredictions() {
        DataModelIF<Long, Long> predictions = DataModelFactory.getDefaultModel();
        DataModelIF<Long, Long> test = DataModelFactory.getDefaultModel();
        for (long i = 1L; i < 20; i++) {
            for (long j = 1L; j < 15; j++) {
                test.addPreference(i, j, i * j % 5 + 1.0);
                predictions.addPreference(i, j, i * j % 5 + 1.0);
            }
        }
        Precision<Long, Long> precision = new Precision<Long, Long>(predictions, test, 1.0, new int[]{5, 10, 20});

        precision.compute();

        assertEquals(1.0, precision.getValue(), 0.0);
        assertEquals(1.0, precision.getValueAt(5), 0.0);
        assertEquals(1.0, precision.getValueAt(10), 0.0);

        Map<Long, Double> precisionPerUser = precision.getValuePerUser();
        for (Map.Entry<Long, Double> e : precisionPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(1.0, value, 0.0);
        }
    }

    @Test
    public void testSameGroundtruthAsPredictionsStringIDs() {
        DataModelIF<String, String> predictions = DataModelFactory.getDefaultModel();
        DataModelIF<String, String> test = DataModelFactory.getDefaultModel();
        for (long i = 1L; i < 20; i++) {
            for (long j = 1L; j < 15; j++) {
                test.addPreference(String.valueOf("u" + i), String.valueOf("i" + j), i * j % 5 + 1.0);
                predictions.addPreference(String.valueOf("u" + i), String.valueOf("i" + j), i * j % 5 + 1.0);
            }
        }
        Precision<String, String> precision = new Precision<String, String>(predictions, test, 1.0, new int[]{5, 10, 20});

        precision.compute();

        assertEquals(1.0, precision.getValue(), 0.0);
        assertEquals(1.0, precision.getValueAt(5), 0.0);
        assertEquals(1.0, precision.getValueAt(10), 0.0);

        Map<String, Double> precisionPerUser = precision.getValuePerUser();
        for (Map.Entry<String, Double> e : precisionPerUser.entrySet()) {
            String user = e.getKey();
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

        Precision<Long, Long> precision = new Precision<Long, Long>(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5});

        precision.compute();

        assertEquals(0.5, precision.getValue(), 0.001);
        assertEquals(1.0, precision.getValueAt(1), 0.001);
        assertEquals(0.5, precision.getValueAt(2), 0.001);
        assertEquals(0.3333, precision.getValueAt(3), 0.001);
        assertEquals(0.5, precision.getValueAt(4), 0.001);
        assertEquals(0.4, precision.getValueAt(5), 0.001);

        Map<Long, Double> precisionPerUser = precision.getValuePerUser();
        for (Map.Entry<Long, Double> e : precisionPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(0.5, value, 0.001);
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

        Precision<Long, Long> precision = new Precision<Long, Long>(predictions, test, 1.0, new int[]{1, 2, 3, 4, 5});

        precision.compute();

        assertEquals(0.5, precision.getValue(), 0.001);
        assertEquals(1.0, precision.getValueAt(1), 0.001);
        assertEquals(0.5, precision.getValueAt(2), 0.001);
        assertEquals(0.3333, precision.getValueAt(3), 0.001);
        assertEquals(0.5, precision.getValueAt(4), 0.001);
        assertEquals(0.4, precision.getValueAt(5), 0.001);

        Map<Long, Double> precisionPerUser = precision.getValuePerUser();
        for (Map.Entry<Long, Double> e : precisionPerUser.entrySet()) {
            long user = e.getKey();
            double value = e.getValue();
            assertEquals(0.5, value, 0.001);
        }
    }

    @Test
    public void testIssue113() {
        boolean useBinaryScores = false; // if true, first configuration, otherwise, second configuration
        DataModelIF<Long, Long> predictions = DataModelFactory.getDefaultModel();
        DataModelIF<Long, Long> test = DataModelFactory.getDefaultModel();

        predictions.addPreference(2L, 13L, useBinaryScores ? 1.0 : 0.5328112138740229);
        test.addPreference(2L, 13L, 1.0);
        predictions.addPreference(2L, 19L, useBinaryScores ? 1.0 : 0.8095414650648188);
        test.addPreference(2L, 19L, 0.0);
        predictions.addPreference(2L, 50L, useBinaryScores ? 1.0 : 0.9888651730555137);
        test.addPreference(2L, 50L, 1.0);
        predictions.addPreference(2L, 251L, useBinaryScores ? 1.0 : 0.8796398906715018);
        test.addPreference(2L, 251L, 1.0);
        predictions.addPreference(2L, 257L, useBinaryScores ? 1.0 : 0.8994154288885712);
        test.addPreference(2L, 257L, 1.0);
        predictions.addPreference(2L, 279L, useBinaryScores ? 0.0 : 0.2031462463186414);
        test.addPreference(2L, 279L, 1.0);
        predictions.addPreference(2L, 280L, useBinaryScores ? 0.0 : 0.137729028181314);
        test.addPreference(2L, 280L, 0.0);
        predictions.addPreference(2L, 281L, useBinaryScores ? 0.0 : 0.12677275323108753);
        test.addPreference(2L, 281L, 0.0);
        predictions.addPreference(2L, 290L, useBinaryScores ? 0.0 : 0.12267777345041282);
        test.addPreference(2L, 290L, 0.0);
        predictions.addPreference(2L, 292L, useBinaryScores ? 1.0 : 0.6708907647085832);
        test.addPreference(2L, 292L, 1.0);
        predictions.addPreference(2L, 297L, useBinaryScores ? 1.0 : 0.769468001849272);
        test.addPreference(2L, 297L, 1.0);
        predictions.addPreference(2L, 298L, useBinaryScores ? 1.0 : 0.878495414483143);
        test.addPreference(2L, 298L, 0.0);
        predictions.addPreference(2L, 299L, useBinaryScores ? 0.0 : 0.09460751859396674);
        test.addPreference(2L, 299L, 1.0);
        predictions.addPreference(2L, 301L, useBinaryScores ? 1.0 : 0.5983346178722854);
        test.addPreference(2L, 301L, 1.0);
        predictions.addPreference(2L, 303L, useBinaryScores ? 1.0 : 0.8094825203837621);
        test.addPreference(2L, 303L, 1.0);
        predictions.addPreference(2L, 307L, useBinaryScores ? 1.0 : 0.7223299632360657);
        test.addPreference(2L, 307L, 0.0);
        predictions.addPreference(2L, 308L, useBinaryScores ? 0.0 : 0.11311986767160687);
        test.addPreference(2L, 308L, 0.0);
        predictions.addPreference(2L, 312L, useBinaryScores ? 0.0 : 0.1279286169284483);
        test.addPreference(2L, 312L, 0.0);
        predictions.addPreference(2L, 313L, useBinaryScores ? 1.0 : 0.9889272842775845);
        test.addPreference(2L, 313L, 1.0);
        predictions.addPreference(2L, 314L, useBinaryScores ? 0.0 : 0.0026441894503800604);
        test.addPreference(2L, 314L, 0.0);
        predictions.addPreference(2L, 315L, useBinaryScores ? 1.0 : 0.955807550363639);
        test.addPreference(2L, 315L, 0.0);
        predictions.addPreference(2L, 316L, useBinaryScores ? 1.0 : 0.965191057772059);
        test.addPreference(2L, 316L, 1.0);

        Precision<Long, Long> precision = new Precision<Long, Long>(predictions, test);
        precision.compute();
        assertEquals(0.545, precision.getValue(), 0.001);

        Recall<Long, Long> recall = new Recall<Long, Long>(predictions, test);
        recall.compute();
        assertEquals(1.0, recall.getValue(), 0.);
    }
}
