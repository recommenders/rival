package net.recommenders.rival.split.splitter;

import java.util.Map;
import java.util.Map.Entry;
import net.recommenders.rival.core.DataModel;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Main class that parses a data set and splits it according to a property file.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class SplitTest {

    /**
     * The number of users in the data model.
     */
    private static final int USERS = 10;
    /**
     * The number of items in the data model.
     */
    private static final int ITEMS = 10;

    @Test
    public void testCrossValidation() {
        DataModel<Long, Long> dm = new DataModel<Long, Long>();
        for (long u = 1L; u <= USERS; u++) {
            for (long i = 1L; i <= ITEMS; i++) {
                dm.addPreference(u, i, 1.0 * u * i);
            }
        }

        int nFolds = 5;

        DataModel<Long, Long>[] splits = null;
        splits = new CrossValidationSplitter(nFolds, false, 1L).split(dm);

        assertTrue(splits.length == 2 * nFolds);

        // Let's take one (user, item) pair from one test
        long userTest = -1;
        long itemTest = -1;
        for (Entry<Long, Map<Long, Double>> e : splits[1].getUserItemPreferences().entrySet()) {
            userTest = e.getKey();
            for (long i : e.getValue().keySet()) {
                itemTest = i;
                break;
            }
            break;
        }

        // Let's check this pair is not in its corresponding training split
        assertTrue(!splits[0].getUserItemPreferences().containsKey(userTest) || !splits[0].getUserItemPreferences().get(userTest).containsKey(itemTest));

        for (int i = 1; i < splits.length / 2; i++) {
            DataModel<Long, Long> training = splits[2 * i];
            DataModel<Long, Long> test = splits[2 * i + 1];
            // Let's check this pair is not in any other test split
            assertTrue(!test.getUserItemPreferences().containsKey(userTest) || !test.getUserItemPreferences().get(userTest).containsKey(itemTest));
            // Let's check this pair is in every other training split
            assertTrue(training.getUserItemPreferences().get(userTest).containsKey(itemTest));
        }
    }

    @Test
    public void testRandom() {
        DataModel<Long, Long> dm = new DataModel<Long, Long>();
        for (long u = 1L; u <= USERS; u++) {
            for (long i = 1L; i <= ITEMS; i++) {
                dm.addPreference(u, i, 1.0 * u * i);
            }
        }

        DataModel<Long, Long>[] splits = null;
        splits = new RandomSplitter(0.8f, false, 1L, false).split(dm);

        assertTrue(splits.length == 2);

        // Let's take one (user, item) pair from test
        long userTest = -1;
        long itemTest = -1;
        for (Entry<Long, Map<Long, Double>> e : splits[1].getUserItemPreferences().entrySet()) {
            userTest = e.getKey();
            for (long i : e.getValue().keySet()) {
                itemTest = i;
                break;
            }
            break;
        }

        // Let's check this pair is not in its corresponding training split
        assertTrue(!splits[0].getUserItemPreferences().containsKey(userTest) || !splits[0].getUserItemPreferences().get(userTest).containsKey(itemTest));
    }
}
