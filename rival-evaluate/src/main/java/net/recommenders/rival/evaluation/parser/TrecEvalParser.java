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
package net.recommenders.rival.evaluation.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.Parser;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * A parser based on the format of trec_eval output (no timestamp info).
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class TrecEvalParser implements Parser<Long, Long> {

    /**
     * The column index for the user id in the file.
     */
    public static final int USER_TOK = 0;
    /**
     * The column index for the item id in the file.
     */
    public static final int ITEM_TOK = 2;
    /**
     * The column index for the rating in the file.
     */
    public static final int RATING_TOK = 4;

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalDataModelIF<Long, Long> parseTemporalData(final File f) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<Long, Long> parseData(final File f) throws IOException {
        DataModelIF<Long, Long> dataset = DataModelFactory.getDefaultModel();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                parseLine(line, dataset);
            }
        } finally {
            br.close();
        }

        return dataset;
    }

    /**
     * A method that parses a line from the file.
     *
     * @param line the line to be parsed
     * @param dataset the dataset where the information parsed from the line
     * will be stored into.
     */
    private void parseLine(final String line, final DataModelIF<Long, Long> dataset) {
        String[] toks = line.split("\t");
        // user
        long userId = Long.parseLong(toks[USER_TOK]);
        // item
        long itemId = Long.parseLong(toks[ITEM_TOK]);
        // preference
        double preference = Double.parseDouble(toks[RATING_TOK]);
        //////
        // update information
        //////
        dataset.addPreference(userId, itemId, preference);
        // no timestamp info
    }
}
