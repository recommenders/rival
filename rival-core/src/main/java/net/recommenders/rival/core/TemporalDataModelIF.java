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
 * Interface for a temporal data model. It is able to store users, items,
 * preferences, and timestamps.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>, <a
 * href="http://github.com/alansaid">Alan</a>
 *
 * @param <U> generic type for users
 * @param <I> generic type for items
 */
public interface TemporalDataModelIF<U, I> extends DataModelIF<U, I> {

    /**
     * Method that returns the timestamps between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @return the timestamps between a user and an item.
     */
    public Iterable<Long> getUserItemTimestamps(U u, I i);

    /**
     * Method that adds a timestamp to the model between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @param t the timestamp.
     */
    public void addTimestamp(final U u, final I i, final Long t);
}
