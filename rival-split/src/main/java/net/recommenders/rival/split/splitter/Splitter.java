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

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Interface for the data splitter.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <U> generic type for users
 * @param <I> generic type for items
 */
public interface Splitter<U, I> {

    /**
     * Splits the data.
     *
     * @param data The data.
     * @return The split data model.
     */
    DataModelIF<U, I>[] split(DataModelIF<U, I> data);

    /**
     * Splits temporal data.
     *
     * @param data The data.
     * @return The split data model.
     */
    TemporalDataModelIF<U, I>[] split(TemporalDataModelIF<U, I> data);
}
