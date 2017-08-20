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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Class replicates CrossValidationSplitter but each generated fold is written
 * directly to a file instead of keeping N+1 times (original dataset + N folds)
 * all at the same time in memory.
 *
 * @author <a href="https://github.com/afcarvalho1991">André Carvalho</a>
 *
 * @param <U> type of users
 * @param <I> type of items
 */
public class IterativeCrossValidationSplitter<U, I> extends CrossValidationSplitter<U, I> {

    /**
     * Folder where the generated splits are written to
     */
    private String outPath;

    /**
     * Constructor.
     *
     * @param nFold number of folds that the data will be split into
     * @param perUsers flag to do the split in a per user basis
     * @param seed value to initialize a Random class
     * @param outPath is folder to where each split (train and test) is going to
     * be written to
     */
    public IterativeCrossValidationSplitter(int nFold, boolean perUsers, long seed, String outPath) {
        super(nFold, perUsers, seed);
        this.outPath = outPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<U, I>[] split(final DataModelIF<U, I> data) {
        try {
            File dir = new File(outPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            final FileWriter[] splits = new FileWriter[2 * nFolds];
            for (int i = 0; i < nFolds; i++) {
                String trainingFile = outPath + "train_" + i + ".csv",
                        testFile = outPath + "test_" + i + ".csv";
                splits[2 * i] = new FileWriter(trainingFile);
                splits[2 * i + 1] = new FileWriter(testFile);

            }

            if (perUser) {
                int n = 0;
                for (U user : data.getUsers()) {
                    List<I> items = new ArrayList<>();
                    data.getUserItems(user).forEach(i -> items.add(i));
                    Collections.shuffle(items, rnd);
                    for (I item : items) {
                        Double pref = data.getUserItemPreference(user, item);
                        int curFold = n % nFolds;
                        for (int i = 0; i < nFolds; i++) {
                            FileWriter f_writer = splits[2 * i]; // training
                            if (i == curFold) {
                                f_writer = splits[2 * i + 1]; // test
                            }

                            if (f_writer == null) {
                                continue; // not a "valid" fold already computed
                            }
                            if (pref != null) {
                                f_writer.write(user + "\t" + item + "\t" + pref);
                            }
                            f_writer.write("\n");
                            f_writer.flush();
                        }
                        n++;
                    }
                }
            } else {
                List<U> users = new ArrayList<>();
                data.getUsers().forEach(u -> users.add(u));
                Collections.shuffle(users, rnd);
                int n = 0;
                for (U user : users) {
                    List<I> items = new ArrayList<>();
                    data.getUserItems(user).forEach(i -> items.add(i));
                    Collections.shuffle(items, rnd);
                    for (I item : items) {
                        Double pref = data.getUserItemPreference(user, item);
                        int curFold = n % nFolds;
                        for (int i = 0; i < nFolds; i++) {
                            FileWriter f_writer = splits[2 * i]; // training
                            if (i == curFold) {
                                f_writer = splits[2 * i + 1]; // test
                            }
                            if (f_writer == null) {
                                continue; // not a "valid" fold already computed
                            }

                            if (pref != null) {
                                f_writer.write(user + "\t" + item + "\t" + pref);
                            }
                            f_writer.write("\n");
                            f_writer.flush();
                        }
                        n++;
                    }
                }
            }

            // Close files
            for (int i = 0; i < nFolds; i++) {
                splits[2 * i].close();
                splits[2 * i + 1].close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @returns
     */
    @Override
    public TemporalDataModelIF<U, I>[] split(final TemporalDataModelIF<U, I> data) {
        try {
            File dir = new File(outPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            final FileWriter[] splits = new FileWriter[2 * nFolds];
            for (int i = 0; i < nFolds; i++) {
                String trainingFile = outPath + "train_" + i + ".csv",
                        testFile = outPath + "test_" + i + ".csv";
                splits[2 * i] = new FileWriter(trainingFile);
                splits[2 * i + 1] = new FileWriter(testFile);

            }

            if (perUser) {
                int n = 0;
                for (U user : data.getUsers()) {
                    List<I> items = new ArrayList<>();
                    data.getUserItems(user).forEach(i -> items.add(i));
                    Collections.shuffle(items, rnd);
                    for (I item : items) {
                        Double pref = data.getUserItemPreference(user, item);
                        Iterable<Long> time = data.getUserItemTimestamps(user, item);
                        int curFold = n % nFolds;
                        for (int i = 0; i < nFolds; i++) {
                            FileWriter f_writer = splits[2 * i]; // training
                            if (i == curFold) {
                                f_writer = splits[2 * i + 1]; // test
                            }

                            if (f_writer == null) {
                                continue; // not a "valid" fold already computed
                            }
                            if (pref != null) {
                                f_writer.write(user + "\t" + item + "\t" + pref);
                            }
                            if (time != null) {
                                for (Long t : time) {
                                    f_writer.write("\t" + t);
                                }
                            }
                            f_writer.write("\n");
                            f_writer.flush();
                        }
                        n++;
                    }
                }
            } else {
                List<U> users = new ArrayList<>();
                data.getUsers().forEach(u -> users.add(u));
                Collections.shuffle(users, rnd);
                int n = 0;
                for (U user : users) {
                    List<I> items = new ArrayList<>();
                    data.getUserItems(user).forEach(i -> items.add(i));
                    Collections.shuffle(items, rnd);
                    for (I item : items) {
                        Double pref = data.getUserItemPreference(user, item);
                        Iterable<Long> time = data.getUserItemTimestamps(user, item);
                        int curFold = n % nFolds;
                        for (int i = 0; i < nFolds; i++) {
                            FileWriter f_writer = splits[2 * i]; // training
                            if (i == curFold) {
                                f_writer = splits[2 * i + 1]; // test
                            }
                            if (f_writer == null) {
                                continue; // not a "valid" fold already computed
                            }

                            if (pref != null) {
                                f_writer.write(user + "\t" + item + "\t" + pref);
                            }
                            if (time != null) {
                                for (Long t : time) {
                                    f_writer.write("\t" + t);
                                }
                            }
                            f_writer.write("\n");
                            f_writer.flush();
                        }
                        n++;
                    }
                }
            }

            // Close files
            for (int i = 0; i < nFolds; i++) {
                splits[2 * i].close();
                splits[2 * i + 1].close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
