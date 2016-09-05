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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Class that splits a dataset using a cross validation technique (every
 * interaction in the data only appears once in each test split).
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <U> type of users
 * @param <I> type of items
 */
public class CrossValidationSplitter<U, I> implements Splitter<U, I> {

    /**
     * The number of folds that the data will be split into.
     */
    private int nFolds;
    /**
     * The flag that indicates if the split should be done in a per user basis.
     */
    private boolean perUser;
    /**
     * An instance of a Random class.
     */
    private Random rnd;

    /**
     * Constructor.
     *
     * @param nFold number of folds that the data will be split into
     * @param perUsers flag to do the split in a per user basis
     * @param seed value to initialize a Random class
     */
    public CrossValidationSplitter(final int nFold, final boolean perUsers, final long seed) {
        this.nFolds = nFold;
        this.perUser = perUsers;

        rnd = new Random(seed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<U, I>[] split(final DataModelIF<U, I> data) {
        @SuppressWarnings("unchecked")
        final DataModelIF<U, I>[] splits = new DataModelIF[2 * nFolds];
        for (int i = 0; i < nFolds; i++) {
            splits[2 * i] = DataModelFactory.getDefaultModel(); // training
            splits[2 * i + 1] = DataModelFactory.getDefaultModel(); // test
        }
        if (perUser) {
            int n = 0;
            for (U user : data.getUsers()) {
                List<I> items = new ArrayList<>(data.getUserItemPreferences().get(user).keySet());
                Collections.shuffle(items, rnd);
                for (I item : items) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    int curFold = n % nFolds;
                    for (int i = 0; i < nFolds; i++) {
                        DataModelIF<U, I> datamodel = splits[2 * i]; // training
                        if (i == curFold) {
                            datamodel = splits[2 * i + 1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                    }
                    n++;
                }
            }
        } else {
            List<U> users = new ArrayList<>(data.getUsers());
            Collections.shuffle(users, rnd);
            int n = 0;
            for (U user : users) {
                List<I> items = new ArrayList<>(data.getUserItemPreferences().get(user).keySet());
                Collections.shuffle(items, rnd);
                for (I item : items) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    int curFold = n % nFolds;
                    for (int i = 0; i < nFolds; i++) {
                        DataModelIF<U, I> datamodel = splits[2 * i]; // training
                        if (i == curFold) {
                            datamodel = splits[2 * i + 1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                    }
                    n++;
                }
            }
        }
        return splits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalDataModelIF<U, I>[] split(final TemporalDataModelIF<U, I> data) {
        @SuppressWarnings("unchecked")
        final TemporalDataModelIF<U, I>[] splits = new TemporalDataModelIF[2 * nFolds];
        for (int i = 0; i < nFolds; i++) {
            splits[2 * i] = new TemporalDataModel<>(); // training
            splits[2 * i + 1] = new TemporalDataModel<>(); // test
        }
        if (perUser) {
            int n = 0;
            for (U user : data.getUsers()) {
                List<I> items = new ArrayList<>(data.getUserItemPreferences().get(user).keySet());
                Collections.shuffle(items, rnd);
                for (I item : items) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    Set<Long> time = null;
                    if (data.getUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
                        time = data.getUserItemTimestamps().get(user).get(item);
                    }
                    int curFold = n % nFolds;
                    for (int i = 0; i < nFolds; i++) {
                        TemporalDataModelIF<U, I> datamodel = splits[2 * i]; // training
                        if (i == curFold) {
                            datamodel = splits[2 * i + 1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        if (time != null) {
                            for (Long t : time) {
                                datamodel.addTimestamp(user, item, t);
                            }
                        }
                    }
                    n++;
                }
            }
        } else {
            List<U> users = new ArrayList<>(data.getUsers());
            Collections.shuffle(users, rnd);
            int n = 0;
            for (U user : users) {
                List<I> items = new ArrayList<>(data.getUserItemPreferences().get(user).keySet());
                Collections.shuffle(items, rnd);
                for (I item : items) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    Set<Long> time = null;
                    if (data.getUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
                        time = data.getUserItemTimestamps().get(user).get(item);
                    }
                    int curFold = n % nFolds;
                    for (int i = 0; i < nFolds; i++) {
                        TemporalDataModelIF<U, I> datamodel = splits[2 * i]; // training
                        if (i == curFold) {
                            datamodel = splits[2 * i + 1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        if (time != null) {
                            for (Long t : time) {
                                datamodel.addTimestamp(user, item, t);
                            }
                        }
                    }
                    n++;
                }
            }
        }
        return splits;
    }
}
