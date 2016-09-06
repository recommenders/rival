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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Data parser for tab-separated data files.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class SimpleParser implements Parser<Long, Long> {

    /**
     * The column index for the user id in the file.
     */
    public static final int USER_TOK = 0;
    /**
     * The column index for the item id in the file.
     */
    public static final int ITEM_TOK = 1;
    /**
     * The column index for the rating in the file.
     */
    public static final int RATING_TOK = 2;
    /**
     * The column index for the time in the file.
     */
    public static final int TIME_TOK = 3;

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<Long, Long> parseData(final File f) throws IOException {
        return parseData(f, "\t", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalDataModelIF<Long, Long> parseTemporalData(final File f) throws IOException {
        return parseData(f, "\t", true);
    }

    /**
     * Parses a data file with a specific separator between fields.
     *
     * @param f The file to be parsed.
     * @param token The separator to be used.
     * @param isTemporal A flag indicating if the file contains temporal
     * information.
     * @return A dataset created from the file.
     * @throws IOException if the file cannot be read.
     */
    public TemporalDataModelIF<Long, Long> parseData(final File f, final String token, final boolean isTemporal) throws IOException {
        TemporalDataModelIF<Long, Long> dataset = DataModelFactory.getDefaultTemporalModel();

        BufferedReader br = SimpleParser.getBufferedReader(f);
        String line = br.readLine();
        if ((line != null) && (!line.matches(".*[a-zA-Z].*"))) {
            parseLine(line, dataset, token, isTemporal);
        }
        while ((line = br.readLine()) != null) {
            parseLine(line, dataset, token, isTemporal);
        }
        br.close();

        return dataset;
    }

    /**
     * Obtains an instance of BufferedReader depending on the file extension: if
     * it ends with gz, zip, or tgz then a compressed reader is used instead of
     * the standard one.
     *
     * @param f The file to be opened.
     * @return An instance of BufferedReader or null if there is a problem
     * @throws IOException when the file cannot be read.
     * @see BufferedReader
     */
    public static BufferedReader getBufferedReader(final File f) throws IOException {
        BufferedReader br = null;
        if ((f == null) || (!f.isFile())) {
            return br;
        }
        if (f.getName().endsWith(".gz") || f.getName().endsWith(".zip") || f.getName().endsWith(".tgz")) {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f)), "UTF-8"));
        } else {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        }
        return br;
    }

    /**
     * Parses line from data file.
     *
     * @param line The line to be parsed.
     * @param dataset The dataset to add data from line to.
     * @param token the token to split on.
     * @param isTemporal A flag indicating if the line contains temporal
     * information.
     */
    private void parseLine(final String line, final TemporalDataModelIF<Long, Long> dataset, final String token, final boolean isTemporal) {
        if (line == null) {
            return;
        }
        String[] toks = line.split(token);
        // user
        long userId = Long.parseLong(toks[USER_TOK]);
        // item
        long itemId = Long.parseLong(toks[ITEM_TOK]);
        // preference
        double preference = Double.parseDouble(toks[RATING_TOK]);
        // timestamp
        long timestamp = -1;
        // allow no timestamp information
        if (isTemporal && toks.length > TIME_TOK) {
            timestamp = Long.parseLong(toks[TIME_TOK]);
        }
        //////
        // update information
        //////
        dataset.addPreference(userId, itemId, preference);
        if (timestamp != -1) {
            dataset.addTimestamp(userId, itemId, timestamp);
        }
    }
}
