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
import net.recommenders.rival.evaluation.core.DataModel;

/**
 *
 * @author Alejandro
 */
public abstract class AbstractStrategy implements EvaluationStrategy<Long, Long> {

    protected DataModel<Long, Long> training;
    protected DataModel<Long, Long> test;
    protected double threshold;

    public AbstractStrategy(DataModel<Long, Long> training, DataModel<Long, Long> test, double threshold) {
        this.training = training;
        this.test = test;
        this.threshold = threshold;
    }

    protected Set<Long> getModelTrainingDifference(DataModel<Long, Long> model, Long user) {
        final Set<Long> items = new HashSet<Long>();
        final Set<Long> trainingItems = training.getUserItemPreferences().get(user).keySet();
        for (Long item : model.getItems()) {
            if (!trainingItems.contains(item)) {
                items.add(item);
            }
        }
        return items;
    }

    public void printRanking(Long user, List<Pair<Long, Double>> scoredItems, PrintStream out, OUTPUT_FORMAT format) {
        final Map<Long, Double> scores = new HashMap<Long, Double>();
        for (Pair<Long, Double> p : scoredItems) {
            scores.put(p.getFirst(), p.getSecond());
        }
        printRanking("" + user, scores, out, format);
    }

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

    public void printGroundtruth(Long user, PrintStream out, OUTPUT_FORMAT format) {
        final Map<Long, Double> relItems = new HashMap<Long, Double>();
        for (Entry<Long, Double> e : test.getUserItemPreferences().get(user).entrySet()) {
            if (e.getValue() >= threshold) {
                relItems.put(e.getKey(), e.getValue());
            }
        }
        printGroundtruth("" + user, relItems, out, format);
    }

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
