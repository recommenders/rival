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
 * Data model used throughout the toolkit. Able to store users, items,
 * preferences, timestamps.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>, <a
 * href="http://github.com/alansaid">Alan</a>
 *
 * @param <U> generic type for users
 * @param <I> generic type for items
 */
public class DataModel<U, I> {

    /**
     * Preference map between users and items.
     */
    private Map<U, Map<I, Double>> userItemPreferences;
    /**
     * Preference map between items and users.
     */
    private Map<I, Map<U, Double>> itemUserPreferences;
    /**
     * The map with the timestamps between users and items.
     */
    private Map<U, Map<I, Set<Long>>> userItemTimestamps;
    /**
     * Flag to indicate if duplicate preferences should be ignored or not.
     * Default: false.
     */
    private boolean ignoreDuplicatePreferences;

    /**
     * Default constructor.
     */
    public DataModel() {
        this(false);
    }

    /**
     * Constructor with parameters.
     *
     * @param ignoreDupPreferences The flag to indicate whether preferences
     * should be ignored.
     */
    public DataModel(final boolean ignoreDupPreferences) {
        this(ignoreDupPreferences, new HashMap<U, Map<I, Double>>(), new HashMap<I, Map<U, Double>>(), new HashMap<U, Map<I, Set<Long>>>());
    }

    /**
     * Constructor with parameters.
     *
     * @param ignoreDupPreferences The flag to indicate whether preferences
     * should be ignored.
     * @param userItemPreference The preference map between users and items.
     * @param itemUserPreference The preference map between items and users.
     * @param userItemTimestamp The map with the timestamps between users and
     * items
     */
    public DataModel(final boolean ignoreDupPreferences, final Map<U, Map<I, Double>> userItemPreference, final Map<I, Map<U, Double>> itemUserPreference,
            final Map<U, Map<I, Set<Long>>> userItemTimestamp) {
        this.ignoreDuplicatePreferences = ignoreDupPreferences;
        this.userItemPreferences = userItemPreference;
        this.itemUserPreferences = itemUserPreference;
        this.userItemTimestamps = userItemTimestamp;
    }

    /**
     * Method that returns the preference map between items and users.
     *
     * @return the preference map between items and users.
     */
    public Map<I, Map<U, Double>> getItemUserPreferences() {
        return itemUserPreferences;
    }

    /**
     * Method that returns the preference map between users and items.
     *
     * @return the preference map between users and items.
     */
    public Map<U, Map<I, Double>> getUserItemPreferences() {
        return userItemPreferences;
    }

    /**
     * Method that returns the map with the timestamps between users and items.
     *
     * @return the map with the timestamps between users and items.
     */
    public Map<U, Map<I, Set<Long>>> getUserItemTimestamps() {
        return userItemTimestamps;
    }

    /**
     * Method that adds a preference to the model between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @param d the preference.
     */
    public void addPreference(final U u, final I i, final Double d) {
        // update direct map
        Map<I, Double> userPreferences = userItemPreferences.get(u);
        if (userPreferences == null) {
            userPreferences = new HashMap<>();
            userItemPreferences.put(u, userPreferences);
        }
        Double preference = userPreferences.get(i);
        if (preference == null) {
            preference = 0.0;
        } else if (ignoreDuplicatePreferences) {
            // if duplicate preferences should be ignored, then we do not take into account the new value
            preference = null;
        }
        if (preference != null) {
            preference += d;
            userPreferences.put(i, preference);
            // update inverse map
            Map<U, Double> itemPreferences = itemUserPreferences.get(i);
            if (itemPreferences == null) {
                itemPreferences = new HashMap<>();
                itemUserPreferences.put(i, itemPreferences);
            }
            itemPreferences.put(u, preference);
        }
    }

    /**
     * Method that adds a timestamp to the model between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @param t the timestamp.
     */
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
     * Method that returns the items in the model.
     *
     * @return the items in the model.
     */
    public Set<I> getItems() {
        return getItemUserPreferences().keySet();
    }

    /**
     * Method that returns the users in the model.
     *
     * @return the users in the model.
     */
    public Set<U> getUsers() {
        return getUserItemPreferences().keySet();
    }

    /**
     * Method that returns the number of items in the model.
     *
     * @return the number of items in the model.
     */
    public int getNumItems() {
        return getItems().size();
    }

    /**
     * Method that returns the number of users in the model.
     *
     * @return the number of users in the model.
     */
    public int getNumUsers() {
        return getUsers().size();
    }

    /**
     * Method that clears all the maps contained in the model.
     */
    public void clear() {
        userItemPreferences.clear();
        userItemTimestamps.clear();
        itemUserPreferences.clear();
    }
}
