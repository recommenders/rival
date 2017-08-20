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

import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;

import java.util.*;

/**
 * Class that splits a dataset randomly.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class RandomSplitter<U, I> implements Splitter<U, I> {

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
     * Raises an exception if perUser is "true" and doSplitPerItems is "false".
     *
     * @param percentageTrainingRatio percentage of training data to be split
     * @param perUserFlag flag to do the split in a per user basis
     * @param seed value to initialize a Random class
     * @param doSplitPerItemsFlag if true, every interaction between a user and
     * a specific item is considered as one, and hence all of them will be
     * either on the training or on the test split
     *
     * @exception IllegalArgumentException if perUser == true and
     * doSplitPerItems == false
     */
    public RandomSplitter(final float percentageTrainingRatio, final boolean perUserFlag, final long seed, final boolean doSplitPerItemsFlag) {
        if (this.perUser && !this.doSplitPerItems) {
            throw new IllegalArgumentException("Unsupported parameters combination: perUser == true and doSplitPerItems == false");
        }
        this.percentageTraining = percentageTrainingRatio;
        this.perUser = perUserFlag;
        this.doSplitPerItems = doSplitPerItemsFlag;
        rnd = new Random(seed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<U, I>[] split(final DataModelIF<U, I> data) {
        @SuppressWarnings("unchecked")
        final DataModelIF<U, I>[] splits = new DataModelIF[2];
        splits[0] = DataModelFactory.getDefaultModel(); // training
        splits[1] = DataModelFactory.getDefaultModel(); // test
        if (perUser) {
            for (U user : data.getUsers()) {
                if (doSplitPerItems) {
                    List<I> items = new ArrayList<>();
                    data.getUserItems(user).forEach(items::add);
                    Collections.shuffle(items, rnd);
                    int splitPoint = Math.round(percentageTraining * items.size());
                    for (int i = 0; i < items.size(); i++) {
                        I item = items.get(i);
                        Double pref = data.getUserItemPreference(user, item);
                        DataModelIF<U, I> datamodel = splits[0]; // training
                        if (i > splitPoint) {
                            datamodel = splits[1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                    }
                }
            }
        } else {
            for (U user : data.getUsers()) {
                for (I item : data.getUserItems(user)) {
                    Double pref = data.getUserItemPreference(user, item);
                    if (doSplitPerItems) {
                        DataModelIF<U, I> datamodel = splits[0]; // training
                        if (rnd.nextDouble() > percentageTraining) {
                            datamodel = splits[1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                    } else {
                        DataModelIF<U, I> datamodel = splits[0]; // training
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
        return splits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalDataModelIF<U, I>[] split(final TemporalDataModelIF<U, I> data) {
        @SuppressWarnings("unchecked")
        final TemporalDataModelIF<U, I>[] splits = new TemporalDataModelIF[2];
        splits[0] = new TemporalDataModel<>(); // training
        splits[1] = new TemporalDataModel<>(); // test
        if (perUser) {
            for (U user : data.getUsers()) {
                if (doSplitPerItems) {
                    List<I> items = new ArrayList<>();
                    data.getUserItems(user).forEach(items::add);
                    Collections.shuffle(items, rnd);
                    int splitPoint = Math.round(percentageTraining * items.size());
                    for (int i = 0; i < items.size(); i++) {
                        I item = items.get(i);
                        Double pref = data.getUserItemPreference(user, item);
                        Iterable<Long> time = data.getUserItemTimestamps(user, item);
                        TemporalDataModelIF<U, I> datamodel = splits[0]; // training
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
                    List<Pair<I, Long>> itemsTime = new ArrayList<>();
                    for (I i : data.getUserItems(user)) {
                        for (Long t : data.getUserItemTimestamps(user, i)) {
                            itemsTime.add(new Pair<>(i, t));
                        }
                    }
                    Collections.shuffle(itemsTime, rnd);
                    int splitPoint = Math.round(percentageTraining * itemsTime.size());
                    for (int i = 0; i < itemsTime.size(); i++) {
                        Pair<I, Long> it = itemsTime.get(i);
                        I item = it.getFirst();
                        Long time = it.getSecond();
                        Double pref = data.getUserItemPreference(user, item);
                        TemporalDataModelIF<U, I> datamodel = splits[0]; // training
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
            for (U user : data.getUsers()) {
                for (I item : data.getUserItems(user)) {
                    Double pref = data.getUserItemPreference(user, item);
                    Iterable<Long> time = data.getUserItemTimestamps(user, item);
                    if (doSplitPerItems) {
                        TemporalDataModelIF<U, I> datamodel = splits[0]; // training
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
                    } else if (time != null) {
                        for (Long t : time) {
                            TemporalDataModelIF<U, I> datamodel = splits[0]; // training
                            if (rnd.nextDouble() > percentageTraining) {
                                datamodel = splits[1]; // test
                            }
                            if (pref != null) {
                                datamodel.addPreference(user, item, pref);
                            }
                            datamodel.addTimestamp(user, item, t);
                        }
                    } else {
                        TemporalDataModelIF<U, I> datamodel = splits[0]; // training
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
        return splits;
    }

    private static class Pair<A, B> {

        private A a;
        private B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A getFirst() {
            return a;
        }

        public B getSecond() {
            return b;
        }

    }
}
