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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utilities for datamodels.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public final class DataModelUtils {

    /**
     * Utility classes should not have a public constructor.
     */
    private DataModelUtils() {
    }

    /**
     * Method that saves a data model to a file.
     *
     * @param dm the data model
     * @param outfile file where the model will be saved
     * @param overwrite flag that indicates if the file should be overwritten
     * @param <U> type of users
     * @param <I> type of items
     * @throws FileNotFoundException when outfile cannot be used.
     * @throws UnsupportedEncodingException when the requested encoding (UTF-8)
     * is not available.
     */
    public static <U, I> void saveDataModel(final DataModelIF<U, I> dm, final String outfile, final boolean overwrite)
            throws FileNotFoundException, UnsupportedEncodingException {
        if (new File(outfile).exists() && !overwrite) {
            System.out.println("Ignoring " + outfile);
        } else {
            PrintStream out = new PrintStream(outfile, "UTF-8");
            for (U user : dm.getUsers()) {
                Map<I, Double> userPrefModel = dm.getUserItemPreferences().get(user);
                for (Entry<I, Double> e : userPrefModel.entrySet()) {
                    I item = e.getKey();
                    Double pref = userPrefModel.get(item);
                    out.println(user + "\t" + item + "\t" + pref + "\t-1");
                }
            }
            out.close();
        }
    }

    /**
     * Method that saves a temporal data model to a file.
     *
     * @param dm the data model
     * @param outfile file where the model will be saved
     * @param overwrite flag that indicates if the file should be overwritten
     * @param <U> type of users
     * @param <I> type of items
     * @throws FileNotFoundException when outfile cannot be used.
     * @throws UnsupportedEncodingException when the requested encoding (UTF-8)
     * is not available.
     */
    public static <U, I> void saveDataModel(final TemporalDataModelIF<U, I> dm, final String outfile, final boolean overwrite)
            throws FileNotFoundException, UnsupportedEncodingException {
        if (new File(outfile).exists() && !overwrite) {
            System.out.println("Ignoring " + outfile);
        } else {
            PrintStream out = new PrintStream(outfile, "UTF-8");
            for (U user : dm.getUsers()) {
                Map<I, Double> userPrefModel = dm.getUserItemPreferences().get(user);
                Map<I, Set<Long>> userTimeModel = dm.getUserItemTimestamps().get(user);
                for (Entry<I, Double> e : userPrefModel.entrySet()) {
                    I item = e.getKey();
                    Double pref = userPrefModel.get(item);
                    Set<Long> time = null;
                    if (userTimeModel != null) {
                        time = userTimeModel.get(item);
                    }
                    if (time == null) {
                        out.println(user + "\t" + item + "\t" + pref + "\t-1");
                    } else {
                        for (Long t : time) {
                            out.println(user + "\t" + item + "\t" + pref + "\t" + t);
                        }
                    }
                }
            }
            out.close();
        }
    }
}
