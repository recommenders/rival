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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Splitter that takes into account the timestamps in the data (older
 * interactions are kept only in the training set).
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class TemporalSplitter<U, I> implements Splitter<U, I> {

    /**
     * The percentage of training to be used by the splitter.
     */
    private float percentageTraining;
    /**
     * The flag that indicates if the split should be done in a per user basis.
     */
    private boolean perUser;
    /**
     * The flag that indicates if the split should consider all the items
     * independently.
     */
    private boolean doSplitPerItems;

    /**
     * Constructor.
     *
     * @param percentageTrainingRatio percentage of training data to be split
     * @param perUserFlag flag to do the split in a per user basis
     * @param doSplitPerItemsFlag if true, every interaction between a user and
     * an item will be kept in the test set if at least one interaction belongs
     * to the corresponding timestamp (according to the rest of the parameters)
     */
    public TemporalSplitter(final float percentageTrainingRatio, final boolean perUserFlag, final boolean doSplitPerItemsFlag) {
        this.percentageTraining = percentageTrainingRatio;
        this.perUser = perUserFlag;
        this.doSplitPerItems = doSplitPerItemsFlag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<U, I>[] split(final DataModelIF<U, I> data) {
        throw new IllegalArgumentException("Method not available");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalDataModelIF<U, I>[] split(final TemporalDataModelIF<U, I> data) {
        @SuppressWarnings("unchecked")
        final TemporalDataModelIF<U, I>[] splits = new TemporalDataModel[2];
        splits[0] = DataModelFactory.getDefaultTemporalModel(); // training
        splits[1] = DataModelFactory.getDefaultTemporalModel(); // test
        if (perUser) {
            for (U user : data.getUsers()) {
                Set<Long> userTimestamps = new HashSet<>();
                for (I i : data.getUserItems(user)) {
                    for (Long t : data.getUserItemTimestamps(user, i)) {
                        userTimestamps.add(t);
                    }
                }
                if (userTimestamps.isEmpty()) {
                    continue;
                }
                List<Long> listTimestamps = new ArrayList<>(userTimestamps);
                Collections.sort(listTimestamps);
                int splitPoint = Math.round(percentageTraining * listTimestamps.size());
                Set<Long> testTimestamps = new HashSet<>();
                int n = 0;
                for (Long t : listTimestamps) {
                    if (n > splitPoint) {
                        testTimestamps.add(t);
                    }
                    n++;
                }
                if (doSplitPerItems) {
                    for (I item : data.getUserItems(user)) {
                        Double pref = data.getUserItemPreference(user, item);
                        boolean inTest = false;
                        for (Long time : data.getUserItemTimestamps(user, item)) {
                            if (testTimestamps.contains(time)) {
                                inTest = true;
                                break;
                            }
                        }
                        TemporalDataModelIF<U, I> datamodel = splits[0]; // training
                        if (inTest) {
                            datamodel = splits[1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        for (Long time : data.getUserItemTimestamps(user, item)) {
                            datamodel.addTimestamp(user, item, time);
                        }
                    }
                } else {
                    for (I item : data.getUserItems(user)) {
                        Double pref = data.getUserItemPreference(user, item);
                        for (Long time : data.getUserItemTimestamps(user, item)) {
                            TemporalDataModelIF<U, I> datamodel = splits[0]; // training
                            if (testTimestamps.contains(time)) {
                                datamodel = splits[1]; // test
                            }
                            if (pref != null) {
                                datamodel.addPreference(user, item, pref);
                            }
                            datamodel.addTimestamp(user, item, time);
                        }
                    }
                }
            }
        } else {
            // global temporal splitting
            Set<Long> allTimestamps = new HashSet<>();
            for (U user : data.getUsers()) {
                for (I i : data.getUserItems(user)) {
                    for (Long t : data.getUserItemTimestamps(user, i)) {
                        allTimestamps.add(t);
                    }
                }
            }
            List<Long> listTimestamps = new ArrayList<>(allTimestamps);
            Collections.sort(listTimestamps);
            int splitPoint = Math.round(percentageTraining * listTimestamps.size());
            Set<Long> testTimestamps = new HashSet<>();
            int n = 0;
            for (Long t : listTimestamps) {
                if (n > splitPoint) {
                    testTimestamps.add(t);
                }
                n++;
            }
            for (U user : data.getUsers()) {
                for (I item : data.getUserItems(user)) {
                    Double pref = data.getUserItemPreference(user, item);
                    Iterable<Long> time = data.getUserItemTimestamps(user, item);
                    if (time == null) {
                        continue;
                    }
                    if (doSplitPerItems) {
                        boolean inTest = false;
                        for (Long t : time) {
                            if (testTimestamps.contains(t)) {
                                inTest = true;
                                break;
                            }
                        }
                        TemporalDataModelIF<U, I> datamodel = splits[0]; // training
                        if (inTest) {
                            datamodel = splits[1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        for (Long t : time) {
                            datamodel.addTimestamp(user, item, t);
                        }
                    } else {
                        for (Long t : time) {
                            TemporalDataModelIF<U, I> datamodel = splits[0]; // training
                            if (testTimestamps.contains(t)) {
                                datamodel = splits[1]; // test
                            }
                            if (pref != null) {
                                datamodel.addPreference(user, item, pref);
                            }
                            datamodel.addTimestamp(user, item, t);
                        }
                    }
                }
            }
        }
        return splits;
    }
}
