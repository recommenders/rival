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
     * @param delimiter field delimiter
     * @param <U> type of users
     * @param <I> type of items
     * @throws FileNotFoundException when outfile cannot be used.
     * @throws UnsupportedEncodingException when the requested encoding (UTF-8)
     * is not available.
     */
    public static <U, I> void saveDataModel(final DataModelIF<U, I> dm, final String outfile, final boolean overwrite, final String delimiter)
            throws FileNotFoundException, UnsupportedEncodingException {
        if (new File(outfile).exists() && !overwrite) {
            System.out.println("Ignoring " + outfile);
        } else {
            PrintStream out = new PrintStream(outfile, "UTF-8");
            for (U user : dm.getUsers()) {
                for (I item : dm.getUserItems(user)) {
                    Double pref = dm.getUserItemPreference(user, item);
                    out.println(user + delimiter + item + delimiter + pref);
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
     * @param delimiter field delimiter
     * @param <U> type of users
     * @param <I> type of items
     * @throws FileNotFoundException when outfile cannot be used.
     * @throws UnsupportedEncodingException when the requested encoding (UTF-8)
     * is not available.
     */
    public static <U, I> void saveDataModel(final TemporalDataModelIF<U, I> dm, final String outfile, final boolean overwrite, String delimiter)
            throws FileNotFoundException, UnsupportedEncodingException {
        if (new File(outfile).exists() && !overwrite) {
            System.out.println("Ignoring " + outfile);
        } else {
            PrintStream out = new PrintStream(outfile, "UTF-8");
            for (U user : dm.getUsers()) {
                for (I item : dm.getUserItems(user)) {
                    Double pref = dm.getUserItemPreference(user, item);
                    Iterable<Long> time = dm.getUserItemTimestamps(user, item);
                    if (time == null) {
                        out.println(user + delimiter + item + delimiter + pref + delimiter + "-1");
                    } else {
                        for (Long t : time) {
                            out.println(user + delimiter + item + delimiter + pref + delimiter + t);
                        }
                    }
                }
            }
            out.close();
        }
    }
}
