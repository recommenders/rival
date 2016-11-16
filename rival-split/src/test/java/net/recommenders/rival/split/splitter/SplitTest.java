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
package net.recommenders.rival.split.splitter;

import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import org.junit.Test;

import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertTrue;

/**
 * Main class that parses a data set and splits it according to a property file.
 * It tests implementations of {@link net.recommenders.rival.split.splitter.Splitter}.
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
        DataModelIF<Long, Long> dm = DataModelFactory.getDefaultModel();
        for (long u = 1L; u <= USERS; u++) {
            for (long i = 1L; i <= ITEMS; i++) {
                dm.addPreference(u, i, 1.0 * u * i);
            }
        }

        int nFolds = 5;

        DataModelIF<Long, Long>[] splits = null;
        splits = new CrossValidationSplitter<Long, Long>(nFolds, false, 1L).split(dm);

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
            DataModelIF<Long, Long> training = splits[2 * i];
            DataModelIF<Long, Long> test = splits[2 * i + 1];
            // Let's check this pair is not in any other test split
            assertTrue(!test.getUserItemPreferences().containsKey(userTest) || !test.getUserItemPreferences().get(userTest).containsKey(itemTest));
            // Let's check this pair is in every other training split
            assertTrue(training.getUserItemPreferences().get(userTest).containsKey(itemTest));
        }
    }

    @Test
    public void testRandom() {
        DataModelIF<Long, Long> dm = DataModelFactory.getDefaultModel();
        for (long u = 1L; u <= USERS; u++) {
            for (long i = 1L; i <= ITEMS; i++) {
                dm.addPreference(u, i, 1.0 * u * i);
            }
        }

        DataModelIF<Long, Long>[] splits = null;
        splits = new RandomSplitter<Long, Long>(0.8f, false, 1L, false).split(dm);

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

    @Test
    public void testValidation() {
        DataModelIF<Long, Long> dm = DataModelFactory.getDefaultModel();
        for (long u = 1L; u <= USERS; u++) {
            for (long i = 1L; i <= ITEMS; i++) {
                dm.addPreference(u, i, 1.0 * u * i);
            }
        }

        DataModelIF<Long, Long>[] splits =
                new ValidationSplitter<>(new RandomSplitter<Long, Long>(0.8f, false, 1L, false)).split(dm);

        assertTrue(splits.length == 3);

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

    @Test
    public void testValidationCV() {
        DataModelIF<Long, Long> dm = DataModelFactory.getDefaultModel();
        for (long u = 1L; u <= USERS; u++) {
            for (long i = 1L; i <= ITEMS; i++) {
                dm.addPreference(u, i, 1.0 * u * i);
            }
        }

        int nFolds = 5;

        DataModelIF<Long, Long>[] splits =
                new ValidationSplitter<>(new CrossValidationSplitter<Long, Long>(nFolds, false, 1L)).split(dm);

        assertTrue(splits.length == 3 * nFolds);

        // Let's take one (user, item) pair from one test
        long userTest = -1;
        long itemTest = -1;
        for (Entry<Long, Map<Long, Double>> e : splits[2].getUserItemPreferences().entrySet()) {
            userTest = e.getKey();
            for (long i : e.getValue().keySet()) {
                itemTest = i;
                break;
            }
            break;
        }

        // Let's check this pair is not in its corresponding training split
        assertTrue(!splits[0].getUserItemPreferences().containsKey(userTest) || !splits[0].getUserItemPreferences().get(userTest).containsKey(itemTest));
        // Let's check this pair is not in its corresponding validation split
        assertTrue(!splits[1].getUserItemPreferences().containsKey(userTest) || !splits[1].getUserItemPreferences().get(userTest).containsKey(itemTest));

        // Let's take one (user, item) pair from one validation
        long userValid = -1;
        long itemValid = -1;
        for (Entry<Long, Map<Long, Double>> e : splits[1].getUserItemPreferences().entrySet()) {
            userValid = e.getKey();
            for (long i : e.getValue().keySet()) {
                itemValid = i;
                break;
            }
            break;
        }

        // Let's check this pair is not in its corresponding training split
        assertTrue(!splits[0].getUserItemPreferences().containsKey(userValid) || !splits[0].getUserItemPreferences().get(userValid).containsKey(itemValid));
        // Let's check this pair is not in its corresponding test split
        assertTrue(!splits[2].getUserItemPreferences().containsKey(userValid) || !splits[2].getUserItemPreferences().get(userValid).containsKey(itemValid));
        
    }


}
