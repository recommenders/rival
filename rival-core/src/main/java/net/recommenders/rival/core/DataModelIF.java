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

/**
 * Interface for the data model used throughout the toolkit. Able to store
 * users, items, and preferences.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>, <a
 * href="http://github.com/alansaid">Alan</a>
 *
 * @param <U> generic type for users
 * @param <I> generic type for items
 */
public interface DataModelIF<U, I> {

    /**
     * Method that returns the preference between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @return the preference between a user and an item.
     */
    public Double getUserItemPreference(U u, I i);

    /**
     * Method that returns the items of a user.
     *
     * @param u the user.
     * @return the items of a user.
     */
    public Iterable<I> getUserItems(U u);

    /**
     * Method that adds a preference to the model between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @param d the preference.
     */
    public void addPreference(final U u, final I i, final Double d);

    /**
     * Method that returns the items in the model.
     *
     * @return the items in the model.
     */
    public Iterable<I> getItems();

    /**
     * Method that returns the users in the model.
     *
     * @return the users in the model.
     */
    public Iterable<U> getUsers();

    /**
     * Method that returns the number of items in the model.
     *
     * @return the number of items in the model.
     */
    public int getNumItems();

    /**
     * Method that returns the number of users in the model.
     *
     * @return the number of users in the model.
     */
    public int getNumUsers();

    /**
     * Method that clears all the maps contained in the model.
     */
    public void clear();
}
