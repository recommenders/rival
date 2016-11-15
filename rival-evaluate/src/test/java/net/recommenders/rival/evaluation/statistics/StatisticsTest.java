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
package net.recommenders.rival.evaluation.statistics;

import java.util.HashMap;
import java.util.Map;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link StatisticalSignificance}, {@link ConfidenceInterval}, {@link EffectSize},
 * and {@link StandardError}. Examples taken from: Tetsuya Sakai. 2014.
 * Statistical reform in information retrieval?. SIGIR Forum 48, 1 (June 2014),
 * 3-12. DOI=10.1145/2641383.2641385 http://doi.acm.org/10.1145/2641383.2641385
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
@RunWith(JUnit4.class)
public class StatisticsTest {

    @Test
    public void testPValue() {
        double[] systemX = new double[]{0.39, 0.28, 0.31, 0.21, 0.19, 0.64, 0.75, 0.36, 0.66, 0.54};
        double[] systemY = new double[]{0.27, 0.04, 0.18, 0.08, 0.19, 0.54, 0.57, 0.29, 0.20, 0.40};
        Map<Integer, Double> x = new HashMap<>();
        Map<Integer, Double> y = new HashMap<>();
        for (int i = 1; i <= systemX.length; i++) {
            x.put(i, systemX[i - 1]);
            y.put(i, systemY[i - 1]);
        }

        StatisticalSignificance ss = new StatisticalSignificance(x, y);
        assertEquals(0.003, ss.getPValue("pairedT"), 0.001);
    }

    @Test
    public void testConfidenceInterval() {
        double[] systemX = new double[]{0.39, 0.28, 0.31, 0.21, 0.19, 0.64, 0.75, 0.36, 0.66, 0.54};
        double[] systemY = new double[]{0.27, 0.04, 0.18, 0.08, 0.19, 0.54, 0.57, 0.29, 0.20, 0.40};
        Map<Integer, Double> x = new HashMap<>();
        Map<Integer, Double> y = new HashMap<>();
        for (int i = 1; i <= systemX.length; i++) {
            x.put(i, systemX[i - 1]);
            y.put(i, systemY[i - 1]);
        }

        double[] interval = new ConfidenceInterval().getConfidenceInterval(0.05, x, y, true);
        assertEquals(0.07, interval[0], 0.01);
        assertEquals(0.25, interval[1], 0.01);

        // from http://www.unt.edu/rss/class/Jon/ISSS_SC/Module008/isss_m8_introttests/node4.html
        double[] x1 = new double[]{6, 6, 9, 8, 4, 6, 7, 8};
        double[] x2 = new double[]{5, 4, 3, 1, 5, 6, 3, 4};
        x = new HashMap<>();
        y = new HashMap<>();
        for (int i = 1; i <= x1.length; i++) {
            x.put(i, x1[i - 1]);
            y.put(i, x2[i - 1]);
        }
        interval = new ConfidenceInterval().getConfidenceInterval(0.05, x, y, false);
        assertEquals(1.5, interval[0], 0.01);
        assertEquals(4.25, interval[1], 0.01);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConfidenceInterval2() {
        double[] systemX = new double[]{0.4, 0.44, 0.42, 0.4, 0.39};
        double[] systemY = new double[]{0.35, 0.4, 0.4, 0.39, 0.4};
        double[] systemZ = new double[]{0.35, 0.4, 0.37, 0.38, 0.39};
        Map<Integer, Double> x = new HashMap<>();
        Map<Integer, Double> y = new HashMap<>();
        Map<Integer, Double> z = new HashMap<>();
        for (int i = 1; i <= systemX.length; i++) {
            x.put(i, systemX[i - 1]);
            y.put(i, systemY[i - 1]);
            z.put(i, systemZ[i - 1]);
        }

        double[][] intervals = new ConfidenceInterval().getConfidenceInterval(0.05, new Map[]{x, y, z});
        assertEquals(0.41 - 0.015, intervals[0][0], 0.01);
        assertEquals(0.41 + 0.015, intervals[0][1], 0.01);
        assertEquals(0.39 - 0.015, intervals[1][0], 0.01);
        assertEquals(0.39 + 0.015, intervals[1][1], 0.01);
        assertEquals(0.38 - 0.015, intervals[2][0], 0.01);
        assertEquals(0.38 + 0.015, intervals[2][1], 0.01);
    }

    @Test
    public void testEffectSize() {
        double[] systemX = new double[]{0.39, 0.28, 0.31, 0.21, 0.19, 0.64, 0.75, 0.36, 0.66, 0.54};
        double[] systemY = new double[]{0.27, 0.04, 0.18, 0.08, 0.19, 0.54, 0.57, 0.29, 0.20, 0.40};
        Map<Integer, Double> x = new HashMap<>();
        Map<Integer, Double> y = new HashMap<>();
        for (int i = 1; i <= systemX.length; i++) {
            x.put(i, systemX[i - 1]);
            y.put(i, systemY[i - 1]);
        }

        EffectSize<Integer> es = new EffectSize<>(x, y);
        // high delta due to rounding numbers in referenced paper
        assertEquals(1.3, es.getEffectSize("pairedT"), 0.1);

        // from http://cep932.wikispaces.com/Effect+Size
        assertEquals(1.11, EffectSize.getCohenD(30, 9.55, 2.9, 30, 12.22, 1.86), 0.1);
        // from https://researchrundowns.wordpress.com/quantitative-methods/effect-size/
        assertEquals(0.62, EffectSize.getCohenD(336, 818.92, 16.11, 336, 828.28, 14.09), 0.01);
//        // from https://en.wikipedia.org/wiki/Effect_size#Cohen.27s_d
//        assertEquals(1.72, EffectSize.getCohenD(2436, 1750.0, 89.93, 3311, 1612.0, 69.05), 0.01);
//        assertEquals(1.72, EffectSize.getCohenDLeastSquares(2436, 1750.0, 89.93, 3311, 1612.0, 69.05), 0.01);

        // from http://www.unt.edu/rss/class/Jon/ISSS_SC/Module008/isss_m8_introttests/node4.html
        double[] x1 = new double[]{6, 6, 9, 8, 4, 6, 7, 8};
        double[] x2 = new double[]{5, 4, 3, 1, 5, 6, 3, 4};
        x = new HashMap<>();
        y = new HashMap<>();
        for (int i = 1; i <= x1.length; i++) {
            x.put(i, x1[i - 1]);
            y.put(i, x2[i - 1]);
        }
        es = new EffectSize<>(x, y);
        assertEquals(1.83, es.getEffectSize("dLS"), 0.1);
    }

    @Test
    public void testStandardError() {
        // Example taken from Table 8.1 in "Elementary Statistics: A Problem Solving Approach 4th Edition", Andrew L. Comrey, Howard B. Lee
        double[] drugA = new double[]{6, 7, 9, 6, 3, 4, 7, 2, 1, 8};
        double[] drugB = new double[]{4, 6, 7, 7, 4, 2, 5, 1, 1, 5};
        Map<Integer, Double> x = new HashMap<>();
        Map<Integer, Double> y = new HashMap<>();
        for (int i = 1; i <= drugA.length; i++) {
            x.put(i, drugA[i - 1]);
            y.put(i, drugB[i - 1]);
        }
        assertEquals(0.433, new StandardError<>(x, y).getStandardError(), 0.001);
    }
}
