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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Temporal data model used throughout the toolkit. It is able to store users,
 * items, preferences, and timestamps.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>, <a
 * href="http://github.com/alansaid">Alan</a>
 *
 * @param <U> generic type for users
 * @param <I> generic type for items
 */
public class TemporalDataModel<U, I> extends DataModel<U, I> implements TemporalDataModelIF<U, I> {

    /**
     * The map with the timestamps between users and items.
     */
    protected Map<U, Map<I, Set<Long>>> userItemTimestamps;

    /**
     * Default constructor.
     */
    public TemporalDataModel() {
        super();
        userItemTimestamps = new HashMap<>();
    }

    /**
     * Constructor with parameters.
     *
     * @param ignoreDupPreferences The flag to indicate whether preferences
     * should be ignored.
     */
    public TemporalDataModel(final boolean ignoreDupPreferences) {
        super(ignoreDupPreferences);
        userItemTimestamps = new HashMap<>();
    }

    /**
     * Constructor with parameters.
     *
     * @param ignoreDupPreferences The flag to indicate whether preferences
     * should be ignored.
     * @param userItemPreference The preference map between users and items.
     * @param itemSet The items.
     * @param userItemTimestamp The map with the timestamps between users and
     * items
     */
    public TemporalDataModel(final boolean ignoreDupPreferences, final Map<U, Map<I, Double>> userItemPreference, final Set<I> itemSet,
            final Map<U, Map<I, Set<Long>>> userItemTimestamp) {
        super(ignoreDupPreferences, userItemPreference, itemSet);
        this.userItemTimestamps = userItemTimestamp;
    }

    /**
     * Method that returns the map with the timestamps between users and items.
     *
     * @return the map with the timestamps between users and items.
     */
    @Override
    public Iterable<Long> getUserItemTimestamps(U u, I i) {
        if (userItemTimestamps.containsKey(u) && userItemTimestamps.get(u).containsKey(i)) {
            return userItemTimestamps.get(u).get(i);
        }
        return null;
    }

    /**
     * Method that adds a timestamp to the model between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @param t the timestamp.
     */
    @Override
    public void addTimestamp(final U u, final I i, final Long t) {
        Map<I, Set<Long>> userTimestamps = userItemTimestamps.get(u);
        if (userTimestamps == null) {
            userTimestamps = new HashMap<>();
            userItemTimestamps.put(u, userTimestamps);
        }
        Set<Long> timestamps = userTimestamps.get(i);
        if (timestamps == null) {
            timestamps = new HashSet<>();
            userTimestamps.put(i, timestamps);
        }
        timestamps.add(t);
    }

    /**
     * Method that clears all the maps contained in the model.
     */
    public void clear() {
        super.clear();
        userItemTimestamps.clear();
    }
}
