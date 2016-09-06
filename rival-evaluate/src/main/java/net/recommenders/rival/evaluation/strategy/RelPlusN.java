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
package net.recommenders.rival.evaluation.strategy;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.evaluation.Pair;

/**
 * Implementation of the Relevant + N Evaluation Strategy as described by
 * Cremonesi et al. [http://dx.doi.org/10.1145/1864708.1864721]
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class RelPlusN extends AbstractStrategy {

    /**
     * The number of additional non-relevant items to be considered as
     * candidates.
     */
    private int n;
    /**
     * An instance of the Random class.
     */
    private Random rnd;

    /**
     * Default constructor for the strategy.
     *
     * @param training The training data model.
     * @param test The test data model.
     * @param theN The N (as described by Cremonesi et al.)
     * @param threshold The relevance threshold.
     * @param seed Randomization seed.
     */
    public RelPlusN(final DataModelIF<Long, Long> training, final DataModelIF<Long, Long> test, final int theN, final double threshold, final long seed) {
        super(training, test, threshold);
        this.n = theN;

        rnd = new Random(seed);
    }

    /**
     * Gets the number of additional non-relevant items to be considered.
     *
     * @return the number of additional non-relevant items
     */
    protected int getN() {
        return n;
    }

    /**
     * Gets the random class.
     *
     * @return the random class
     */
    protected Random getRnd() {
        return rnd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Long> getCandidateItemsToRank(final Long user) {
        final Set<Long> allItems = getModelTrainingDifference(getTraining(), user);
        allItems.addAll(getModelTrainingDifference(getTest(), user));
        // return only N not relevant items
        List<Long> shuffledItems = new ArrayList<Long>(allItems);
        Collections.shuffle(shuffledItems, rnd);
        shuffledItems = shuffledItems.subList(0, Math.min(shuffledItems.size(), n));
        final Set<Long> items = new HashSet<Long>(shuffledItems);
        // add relevant ones
        for (Long i : getTest().getUserItems(user)) {
            if (getTest().getUserItemPreference(user, i) >= getThreshold()) {
                items.add(i);
            }
        }
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printRanking(final Long user, final List<Pair<Long, Double>> scoredItems, final PrintStream out, final OUTPUT_FORMAT format) {
        final Set<Long> relItems = new HashSet<Long>();
        for (Long i : getTest().getUserItems(user)) {
            if (getTest().getUserItemPreference(user, i) >= getThreshold()) {
                relItems.add(i);
            }
        }
        final Map<Long, Double> relScores = new HashMap<Long, Double>();
        final Map<Long, Double> notRelScores = new HashMap<Long, Double>();
        for (Pair<Long, Double> p : scoredItems) {
            if (relItems.contains(p.getFirst())) {
                relScores.put(p.getFirst(), p.getSecond());
            } else {
                notRelScores.put(p.getFirst(), p.getSecond());
            }
        }
        for (Entry<Long, Double> e : relScores.entrySet()) {
            Long r = e.getKey();
            Map<Long, Double> m = new HashMap<Long, Double>(notRelScores);
            m.put(r, e.getValue());
            printRanking(user + "_" + r, m, out, format);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printGroundtruth(final Long user, final PrintStream out, final OUTPUT_FORMAT format) {
        for (Long i : getTest().getUserItems(user)) {
            Double d = getTest().getUserItemPreference(user, i);
            if (d >= getThreshold()) {
                final Map<Long, Double> tmp = new HashMap<Long, Double>();
                tmp.put(i, d);
                printGroundtruth(user + "_" + i, tmp, out, format);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "RelPlusN_" + n + "_" + getThreshold();
    }
}
