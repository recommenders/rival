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
import net.recommenders.rival.core.DataModel;

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
    protected int N;
    /**
     * An instance of the Random class.
     */
    protected Random rnd;

    /**
     * Default constructor for the strategy.
     *
     * @param training The training data model.
     * @param test The test data model.
     * @param N The N (as described by Cremonesi et al.)
     * @param threshold The relevance threshold.
     * @param seed Randomization seed.
     */
    public RelPlusN(DataModel<Long, Long> training, DataModel<Long, Long> test, int N, double threshold, long seed) {
        super(training, test, threshold);
        this.N = N;

        rnd = new Random(seed);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<Long> getCandidateItemsToRank(Long user) {
        final Set<Long> allItems = getModelTrainingDifference(training, user);
        allItems.addAll(getModelTrainingDifference(test, user));
        // return only N not relevant items
        List<Long> shuffledItems = new ArrayList<Long>(allItems);
        Collections.shuffle(shuffledItems, rnd);
        shuffledItems = shuffledItems.subList(0, Math.min(shuffledItems.size(), N));
        final Set<Long> items = new HashSet<Long>(shuffledItems);
        // add relevant ones
        for (Entry<Long, Double> e : test.getUserItemPreferences().get(user).entrySet()) {
            if (e.getValue() >= threshold) {
                items.add(e.getKey());
            }
        }
        return items;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void printRanking(Long user, List<Pair<Long, Double>> scoredItems, PrintStream out, OUTPUT_FORMAT format) {
        final Set<Long> relItems = new HashSet<Long>();
        for (Entry<Long, Double> e : test.getUserItemPreferences().get(user).entrySet()) {
            if (e.getValue() >= threshold) {
                relItems.add(e.getKey());
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
        for (Long r : relScores.keySet()) {
            Map<Long, Double> m = new HashMap<Long, Double>(notRelScores);
            m.put(r, relScores.get(r));
            printRanking(user + "_" + r, m, out, format);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void printGroundtruth(Long user, PrintStream out, OUTPUT_FORMAT format) {
        for (Entry<Long, Double> e : test.getUserItemPreferences().get(user).entrySet()) {
            if (e.getValue() >= threshold) {
                final Map<Long, Double> tmp = new HashMap<Long, Double>();
                tmp.put(e.getKey(), e.getValue());
                printGroundtruth(user + "_" + e.getKey(), tmp, out, format);
            }
        }
    }
}
