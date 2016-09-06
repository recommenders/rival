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
import java.util.Set;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.evaluation.Pair;

/**
 * A basic evaluation strategy.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public abstract class AbstractStrategy implements EvaluationStrategy<Long, Long> {

    /**
     * The training set.
     */
    private DataModelIF<Long, Long> training;
    /**
     * The test set.
     */
    private DataModelIF<Long, Long> test;
    /**
     * The relevance threshold.
     */
    private double threshold;

    /**
     * Default constructor for the evaluation strategy.
     *
     * @param theTraining The training set.
     * @param theTest The test set.
     * @param theThreshold The relevance threshold.
     */
    public AbstractStrategy(final DataModelIF<Long, Long> theTraining, final DataModelIF<Long, Long> theTest, final double theThreshold) {
        this.training = theTraining;
        this.test = theTest;
        this.threshold = theThreshold;
    }

    /**
     * Gets the training set.
     *
     * @return the training set
     */
    protected DataModelIF<Long, Long> getTraining() {
        return training;
    }

    /**
     * Gets the test set.
     *
     * @return the test set
     */
    protected DataModelIF<Long, Long> getTest() {
        return test;
    }

    /**
     * Gets the relevance threshold.
     *
     * @return the relevance threshold
     */
    protected double getThreshold() {
        return threshold;
    }

    /**
     * Get the items appearing in the training set and not in the data model.
     *
     * @param model The data model.
     * @param user The user.
     * @return The items not appearing in the training set.
     */
    protected Set<Long> getModelTrainingDifference(final DataModelIF<Long, Long> model, final Long user) {
        final Set<Long> items = new HashSet<Long>();
        if (training.getUserItems(user) != null) {
            final Set<Long> trainingItems = new HashSet<>();
            for (Long i : training.getUserItems(user)) {
                trainingItems.add(i);
            }
            for (Long item : model.getItems()) {
                if (!trainingItems.contains(item)) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printRanking(final Long user, final List<Pair<Long, Double>> scoredItems, final PrintStream out, final OUTPUT_FORMAT format) {
        final Map<Long, Double> scores = new HashMap<Long, Double>();
        for (Pair<Long, Double> p : scoredItems) {
            scores.put(p.getFirst(), p.getSecond());
        }
        printRanking("" + user, scores, out, format);
    }

    /**
     * Print the item ranking and scores for a specific user.
     *
     * @param user The user (as a String).
     * @param scoredItems The item to print rankings for.
     * @param out Where to direct the print.
     * @param format The format of the printer.
     */
    protected void printRanking(final String user, final Map<Long, Double> scoredItems, final PrintStream out, final OUTPUT_FORMAT format) {
        final Map<Double, Set<Long>> preferenceMap = new HashMap<Double, Set<Long>>();
        for (Map.Entry<Long, Double> e : scoredItems.entrySet()) {
            long item = e.getKey();
            double pref = e.getValue();
            // ignore NaN's
            if (Double.isNaN(pref)) {
                continue;
            }
            Set<Long> items = preferenceMap.get(pref);
            if (items == null) {
                items = new HashSet<Long>();
                preferenceMap.put(pref, items);
            }
            items.add(item);
        }
        final List<Double> sortedScores = new ArrayList<Double>(preferenceMap.keySet());
        Collections.sort(sortedScores, Collections.reverseOrder());
        // Write estimated preferences
        int pos = 1;
        for (double pref : sortedScores) {
            for (long itemID : preferenceMap.get(pref)) {
                switch (format) {
                    case TRECEVAL:
                        out.println(user + "\tQ0\t" + itemID + "\t" + pos + "\t" + pref + "\t" + "r");
                        break;
                    default:
                    case SIMPLE:
                        out.println(user + "\t" + itemID + "\t" + pref);
                        break;
                }
                pos++;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printGroundtruth(final Long user, final PrintStream out, final OUTPUT_FORMAT format) {
        final Map<Long, Double> relItems = new HashMap<Long, Double>();
        for (Long i : test.getUserItems(user)) {
            Double d = test.getUserItemPreference(user, i);
            if (d >= threshold) {
                relItems.put(i, d);
            }
        }
        printGroundtruth("" + user, relItems, out, format);
    }

    /**
     * Internal function to print the ground truth (the test set).
     *
     * @param user The user (as a String).
     * @param groundtruthItems The ground truth items for the user.
     * @param out Where to print.
     * @param format The format of the printer.
     */
    protected void printGroundtruth(final String user, final Map<Long, Double> groundtruthItems, final PrintStream out, final OUTPUT_FORMAT format) {
        for (Entry<Long, Double> e : groundtruthItems.entrySet()) {
            switch (format) {
                case TRECEVAL:
                    out.println(user + "\tQ0\t" + e.getKey() + "\t" + e.getValue());
                    break;
                default:
                case SIMPLE:
                    out.println(user + "\t" + e.getKey() + "\t" + e.getValue());
                    break;
            }
        }
    }
}
