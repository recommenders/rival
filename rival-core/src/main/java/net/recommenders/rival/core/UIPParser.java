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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * User-Item-Preference (rating) Parser.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class UIPParser extends AbstractParser implements Parser<Long, Long> {

    /**
     * Default constructor.
     */
    public UIPParser() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalDataModelIF<Long, Long> parseTemporalData(final File f) throws IOException {
        TemporalDataModelIF<Long, Long> dataset = DataModelFactory.getDefaultTemporalModel();
        Reader in = new InputStreamReader(new FileInputStream(f), "UTF-8");

        Iterable<CSVRecord> records;
        if (isHasHeader()) {
            records = CSVFormat.EXCEL.withDelimiter(getDelimiter()).withHeader().parse(in);
        } else {
            records = CSVFormat.EXCEL.withDelimiter(getDelimiter()).parse(in);
        }
        for (CSVRecord record : records) {
            long userID = Long.parseLong(record.get(getUserTok()));
            long itemID = Long.parseLong(record.get(getItemTok()));
            long timestamp = -1L;
            if (getTimeTok() != -1) {
                timestamp = Long.parseLong(record.get(getTimeTok()));
            }
            double preference = Double.parseDouble(record.get(getPrefTok()));
            dataset.addPreference(userID, itemID, preference);
            dataset.addTimestamp(userID, itemID, timestamp);
        }
        in.close();
        return dataset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<Long, Long> parseData(final File f) throws IOException {
        DataModelIF<Long, Long> dataset = new DataModel<>();
        Reader in = new InputStreamReader(new FileInputStream(f), "UTF-8");

        Iterable<CSVRecord> records;
        if (isHasHeader()) {
            records = CSVFormat.EXCEL.withDelimiter(getDelimiter()).withHeader().parse(in);
        } else {
            records = CSVFormat.EXCEL.withDelimiter(getDelimiter()).parse(in);
        }
        for (CSVRecord record : records) {
            long userID = Long.parseLong(record.get(getUserTok()));
            long itemID = Long.parseLong(record.get(getItemTok()));
            double preference = Double.parseDouble(record.get(getPrefTok()));
            dataset.addPreference(userID, itemID, preference);
        }
        in.close();
        return dataset;
    }
}
