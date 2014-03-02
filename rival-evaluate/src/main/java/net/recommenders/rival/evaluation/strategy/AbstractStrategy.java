package net.recommenders.rival.evaluation.strategy;

import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;
import net.recommenders.rival.core.DataModel;

/**
 * A basic evaluation strategy.
 *
 * @author Alejandro
 */
public abstract class AbstractStrategy implements EvaluationStrategy<Long, Long> {

    protected DataModel<Long, Long> training;
    protected DataModel<Long, Long> test;
    protected double threshold;

    /**
     * Default constructor for the evaluation strategy.
     *
     * @param training The training set.
     * @param test The test set.
     * @param threshold The relevance threshold.
     */
    public AbstractStrategy(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        this.training = training;
        this.test = test;
        this.threshold = threshold;
    }

    /**
     * Get the items appearing in the training set and not in the data model.
     *
     * @param model The data model.
     * @param user The user.
     * @return The items not appearing in the training set.
     */
    protected Set<Long> getModelTrainingDifference(DataModel<Long, Long> model, Long user) {
        final Set<Long> items = new HashSet<Long>();
        if (training.getUserItemPreferences().containsKey(user)) {
            final Set<Long> trainingItems = training.getUserItemPreferences().get(user).keySet();
            for (Long item : model.getItems()) {
                if (!trainingItems.contains(item)) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void printRanking(Long user, List<Pair<Long, Double>> scoredItems, PrintStream out, OUTPUT_FORMAT format) {
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
    protected void printRanking(String user, Map<Long, Double> scoredItems, PrintStream out, OUTPUT_FORMAT format) {
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
                    case SIMPLE:
                        out.println(user + "\t" + itemID + "\t" + pref);
                        break;
                }
                pos++;
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void printGroundtruth(Long user, PrintStream out, OUTPUT_FORMAT format) {
        final Map<Long, Double> relItems = new HashMap<Long, Double>();
        for (Entry<Long, Double> e : test.getUserItemPreferences().get(user).entrySet()) {
            if (e.getValue() >= threshold) {
                relItems.put(e.getKey(), e.getValue());
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
    protected void printGroundtruth(String user, Map<Long, Double> groundtruthItems, PrintStream out, OUTPUT_FORMAT format) {
        for (Entry<Long, Double> e : groundtruthItems.entrySet()) {
            switch (format) {
                case TRECEVAL:
                    out.println(user + "\tQ0\t" + e.getKey() + "\t" + e.getValue());
                    break;
                case SIMPLE:
                    out.println(user + "\t" + e.getKey() + "\t" + e.getValue());
                    break;
            }
        }
    }
}
