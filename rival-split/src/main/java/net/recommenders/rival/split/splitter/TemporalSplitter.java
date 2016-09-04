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
import java.util.Map.Entry;
import java.util.Set;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Splitter that takes into account the timestamps in the data (older
 * interactions are kept only in the training set).
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class TemporalSplitter implements Splitter<Long, Long> {

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
    public DataModelIF<Long, Long>[] split(final DataModelIF<Long, Long> data) {
        throw new IllegalArgumentException("Method not available");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalDataModelIF<Long, Long>[] split(final TemporalDataModelIF<Long, Long> data) {
        @SuppressWarnings("unchecked")
        final TemporalDataModelIF<Long, Long>[] splits = new TemporalDataModel[2];
        splits[0] = new TemporalDataModel<>(); // training
        splits[1] = new TemporalDataModel<>(); // test
        if (perUser) {
            for (Long user : data.getUsers()) {
                if (!data.getUserItemTimestamps().containsKey(user)) {
                    continue;
                }
                Set<Long> userTimestamps = new HashSet<>();
                for (Set<Long> timestamps : data.getUserItemTimestamps().get(user).values()) {
                    userTimestamps.addAll(timestamps);
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
                    for (Entry<Long, Set<Long>> e : data.getUserItemTimestamps().get(user).entrySet()) {
                        Long item = e.getKey();
                        Double pref = data.getUserItemPreferences().get(user).get(item);
                        boolean inTest = false;
                        for (Long time : e.getValue()) {
                            if (testTimestamps.contains(time)) {
                                inTest = true;
                                break;
                            }
                        }
                        TemporalDataModelIF<Long, Long> datamodel = splits[0]; // training
                        if (inTest) {
                            datamodel = splits[1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        for (Long time : e.getValue()) {
                            datamodel.addTimestamp(user, item, time);
                        }
                    }
                } else {
                    if (!data.getUserItemTimestamps().containsKey(user)) {
                        continue;
                    }
                    for (Entry<Long, Set<Long>> e : data.getUserItemTimestamps().get(user).entrySet()) {
                        Long item = e.getKey();
                        Double pref = data.getUserItemPreferences().get(user).get(item);
                        for (Long time : e.getValue()) {
                            TemporalDataModelIF<Long, Long> datamodel = splits[0]; // training
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
            for (Long user : data.getUserItemTimestamps().keySet()) {
                for (Set<Long> timestamps : data.getUserItemTimestamps().get(user).values()) {
                    allTimestamps.addAll(timestamps);
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
            for (Long user : data.getUsers()) {
                if (!data.getUserItemTimestamps().containsKey(user)) {
                    continue;
                }
                for (Long item : data.getUserItemPreferences().get(user).keySet()) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    Set<Long> time = data.getUserItemTimestamps().get(user).get(item);
                    if (doSplitPerItems) {
                        boolean inTest = false;
                        for (Long t : time) {
                            if (testTimestamps.contains(t)) {
                                inTest = true;
                                break;
                            }
                        }
                        TemporalDataModelIF<Long, Long> datamodel = splits[0]; // training
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
                            TemporalDataModelIF<Long, Long> datamodel = splits[0]; // training
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
