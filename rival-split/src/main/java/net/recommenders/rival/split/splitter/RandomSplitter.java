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
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * Class that splits a dataset randomly.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class RandomSplitter implements Splitter<Long, Long> {

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
     * An instance of a Random class.
     */
    private Random rnd;

    /**
     * Constructor.
     *
     * @param percentageTrainingRatio percentage of training data to be split
     * @param perUserFlag flag to do the split in a per user basis
     * @param seed value to initialize a Random class
     * @param doSplitPerItemsFlag if true, every interaction between a user and
     * a specific item is considered as one, and hence all of them will be
     * either on the training or on the test split
     */
    public RandomSplitter(final float percentageTrainingRatio, final boolean perUserFlag, final long seed, final boolean doSplitPerItemsFlag) {
        this.percentageTraining = percentageTrainingRatio;
        this.perUser = perUserFlag;
        this.doSplitPerItems = doSplitPerItemsFlag;

        rnd = new Random(seed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModel<Long, Long>[] split(final DataModel<Long, Long> data) {
        @SuppressWarnings("unchecked")
        final DataModel<Long, Long>[] splits = new DataModel[2];
        splits[0] = new DataModel<>(); // training
        splits[1] = new DataModel<>(); // test
        if (perUser) {
            for (Long user : data.getUsers()) {
                if (doSplitPerItems) {
                    List<Long> items = new ArrayList<>(data.getUserItemPreferences().get(user).keySet());
                    Collections.shuffle(items, rnd);
                    int splitPoint = Math.round(percentageTraining * items.size());
                    for (int i = 0; i < items.size(); i++) {
                        Long item = items.get(i);
                        Double pref = data.getUserItemPreferences().get(user).get(item);
                        Set<Long> time = null;
                        if (data.getUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
                            time = data.getUserItemTimestamps().get(user).get(item);
                        }
                        DataModel<Long, Long> datamodel = splits[0]; // training
                        if (i > splitPoint) {
                            datamodel = splits[1]; // test
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
                } else {
                    if (!data.getUserItemTimestamps().containsKey(user)) {
                        continue;
                    }
                    List<String> itemsTime = new ArrayList<>();
                    for (Entry<Long, Set<Long>> e : data.getUserItemTimestamps().get(user).entrySet()) {
                        long i = e.getKey();
                        for (Long t : e.getValue()) {
                            itemsTime.add(i + "_" + t);
                        }
                    }
                    Collections.shuffle(itemsTime, rnd);
                    int splitPoint = Math.round(percentageTraining * itemsTime.size());
                    for (int i = 0; i < itemsTime.size(); i++) {
                        String it = itemsTime.get(i);
                        Long item = Long.parseLong(it.split("_")[0]);
                        Long time = Long.parseLong(it.split("_")[1]);
                        Double pref = data.getUserItemPreferences().get(user).get(item);
                        DataModel<Long, Long> datamodel = splits[0]; // training
                        if (i > splitPoint) {
                            datamodel = splits[1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        if (time != null) {
                            datamodel.addTimestamp(user, item, time);
                        }
                    }
                }
            }
        } else {
            for (Long user : data.getUsers()) {
                for (Long item : data.getUserItemPreferences().get(user).keySet()) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    Set<Long> time = null;
                    if (data.getUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
                        time = data.getUserItemTimestamps().get(user).get(item);
                    }
                    if (doSplitPerItems) {
                        DataModel<Long, Long> datamodel = splits[0]; // training
                        if (rnd.nextDouble() > percentageTraining) {
                            datamodel = splits[1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        if (time != null) {
                            for (Long t : time) {
                                datamodel.addTimestamp(user, item, t);
                            }
                        }
                    } else {
                        if (time != null) {
                            for (Long t : time) {
                                DataModel<Long, Long> datamodel = splits[0]; // training
                                if (rnd.nextDouble() > percentageTraining) {
                                    datamodel = splits[1]; // test
                                }
                                if (pref != null) {
                                    datamodel.addPreference(user, item, pref);
                                }
                                datamodel.addTimestamp(user, item, t);
                            }
                        } else {
                            DataModel<Long, Long> datamodel = splits[0]; // training
                            if (rnd.nextDouble() > percentageTraining) {
                                datamodel = splits[1]; // test
                            }
                            if (pref != null) {
                                datamodel.addPreference(user, item, pref);
                            }
                        }
                    }
                }
            }
        }
        return splits;
    }
}
