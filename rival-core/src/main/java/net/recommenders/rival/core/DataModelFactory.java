/*
 * Copyright 2016 recommenders.net.
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

import net.recommenders.rival.core.ext.MahoutDataModel;
import net.recommenders.rival.core.ext.RankSysDataModel;

/**
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class DataModelFactory {

    public static <U, I> DataModelIF<U, I> getDefaultModel() {
        return getSimpleModel();
    }

    public static <U, I> TemporalDataModelIF<U, I> getDefaultTemporalModel() {
        return getSimpleTemporalModel();
    }

    public static <U, I> DataModelIF<U, I> getSimpleModel() {
        return new DataModel<>();
    }

    public static <U, I> TemporalDataModelIF<U, I> getSimpleTemporalModel() {
        return new TemporalDataModel<>();
    }

    public static TemporalDataModelIF<Long, Long> getMahoutTemporalModel() {
        return new MahoutDataModel();
    }

    public static <U, I> DataModelIF<U, I> getRankSysModel() {
        return new RankSysDataModel<>();
    }
}
