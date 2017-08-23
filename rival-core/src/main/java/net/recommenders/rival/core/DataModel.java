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

import java.util.Collections;
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
public class DataModel<U, I> implements DataModelIF<U, I> {

    /**
     * Preference map between users and items.
     */
    protected Map<U, Map<I, Double>> userItemPreferences;
    /**
     * Set containing all the items.
     */
    protected Set<I> items;
    /**
     * Flag to indicate if duplicate preferences should be ignored or not.
     * Default: false.
     */
    protected boolean ignoreDuplicatePreferences;

    /**
     * Default constructor.
     */
    DataModel() {
        this(false);
    }

    /**
     * Constructor with parameters.
     *
     * @param ignoreDupPreferences The flag to indicate whether preferences
     * should be ignored.
     */
    protected DataModel(final boolean ignoreDupPreferences) {
        this(ignoreDupPreferences, new HashMap<U, Map<I, Double>>(), new HashSet<I>());
    }

    /**
     * Constructor with parameters.
     *
     * @param ignoreDupPreferences The flag to indicate whether preferences
     * should be ignored.
     * @param userItemPreference The preference map between users and items.
     * @param itemSet The items.
     */
    protected DataModel(final boolean ignoreDupPreferences, final Map<U, Map<I, Double>> userItemPreference, final Set<I> itemSet) {
        this.ignoreDuplicatePreferences = ignoreDupPreferences;
        this.userItemPreferences = userItemPreference;
        this.items = itemSet;
    }

    /**
     * Method that returns the preference between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @return the preference between a user and an item or NaN.
     */
    @Override
    public Double getUserItemPreference(U u, I i) {
        if (userItemPreferences.containsKey(u) && userItemPreferences.get(u).containsKey(i)) {
            return userItemPreferences.get(u).get(i);
        }
        return Double.NaN;
    }

    /**
     * Method that returns the items of a user.
     *
     * @param u the user.
     * @return the items of a user.
     */
    @Override
    public Iterable<I> getUserItems(U u) {
        if (userItemPreferences.containsKey(u)) {
            return userItemPreferences.get(u).keySet();
        }
        return Collections.emptySet();
    }

    /**
     * Method that adds a preference to the model between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @param d the preference.
     */
    @Override
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
        }
        // update items
        items.add(i);
    }

    /**
     * Method that returns the items in the model.
     *
     * @return the items in the model.
     */
    @Override
    public Iterable<I> getItems() {
        return items;
    }

    /**
     * Method that returns the users in the model.
     *
     * @return the users in the model.
     */
    @Override
    public Iterable<U> getUsers() {
        return userItemPreferences.keySet();
    }

    /**
     * Method that returns the number of items in the model.
     *
     * @return the number of items in the model.
     */
    @Override
    public int getNumItems() {
        return items.size();
    }

    /**
     * Method that returns the number of users in the model.
     *
     * @return the number of users in the model.
     */
    @Override
    public int getNumUsers() {
        return userItemPreferences.keySet().size();
    }

    /**
     * Method that clears all the maps contained in the model.
     */
    @Override
    public void clear() {
        userItemPreferences.clear();
        items.clear();
    }
}
