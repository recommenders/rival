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
package net.recommenders.rival.core;

import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link net.recommenders.rival.core.DataModel}.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
@RunWith(JUnit4.class)
public class DataModelTest<U, I> {

    /**
     * The data model.
     */
    private DataModel<Long, Long> dm = new DataModel<>();
    /**
     * The number of users in the data model.
     */
    private static final int USERS = 3;
    /**
     * The number of items in the data model.
     */
    private static final int ITEMS = 3;

    @Before
    public void initialize() {

        for (long u = 1L; u <= USERS; u++) {
            for (long i = 1L; i <= ITEMS; i++) {
                dm.addPreference(u, i, 1.0 * u * i);
            }
        }
    }

    @Test
    public void testGetUserPreferences() {
        Map<Long, Map<Long, Double>> storedPrefs = dm.getUserItemPreferences();
        for (long u = 1L; u <= USERS; u++) {
            Map<Long, Double> iprefs = storedPrefs.get(u);
            for (long i = 1L; i <= ITEMS; i++) {
                assertEquals(1.0 * u * i, iprefs.get(i), 0.0);
            }
        }
    }

    @Test
    public void testGetNumItems() {
        assertEquals(ITEMS, dm.getNumItems());
    }

    @Test
    public void testGetNumUsers() {
        assertEquals(USERS, dm.getNumUsers());
    }
}