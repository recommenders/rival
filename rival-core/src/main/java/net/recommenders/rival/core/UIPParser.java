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
public class UIPParser extends AbstractParser implements Parser {

    public UIPParser() {
        super();
    }

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
