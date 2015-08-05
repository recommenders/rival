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
     * Default constructor
     */
    public UIPParser() {
        super();
    }

    /**
     * @inheritDoc
     */
    @Override
    public DataModel<Long, Long> parseData(File f) throws IOException {
        DataModel<Long, Long> dataset = new DataModel<Long, Long>();
        Reader in = new InputStreamReader(new FileInputStream(f), "UTF-8");

        Iterable<CSVRecord> records;
        if (hasHeader) {
            records = CSVFormat.EXCEL.withDelimiter(delimiter).withHeader().parse(in);
        } else {
            records = CSVFormat.EXCEL.withDelimiter(delimiter).parse(in);
        }
        for (CSVRecord record : records) {
            long userID = Long.parseLong(record.get(userTok));
            long itemID = Long.parseLong(record.get(itemTok));
            long timestamp = -1L;
            if (timeTok != -1) {
                timestamp = Long.parseLong(record.get(timeTok));
            }
            double preference = Double.parseDouble(record.get(prefTok));
            dataset.addPreference(userID, itemID, preference);
            dataset.addTimestamp(userID, itemID, timestamp);
        }
        in.close();
        return dataset;
    }
}
