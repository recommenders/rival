package net.recommenders.rival.core;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.IOException;

/**
 * User-Item-Preference (rating) Parser.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class UIPParser extends AbstractParser implements Parser{

    public UIPParser(){
        super();
    }

    @Override
    public DataModel<Long, Long> parseData(File f) throws IOException {
        DataModel<Long, Long> dataset = new DataModel<Long, Long>();
        Reader in = new FileReader(f);

        Iterable<CSVRecord> records;
        if(HAS_HEADER) {
            records = CSVFormat.EXCEL.withDelimiter(DELIMITER).withHeader().parse(in);
        }
        else {
            records = CSVFormat.EXCEL.withDelimiter(DELIMITER).parse(in);
        }
        for (CSVRecord record : records) {
            long userID = Long.parseLong(record.get(USER_TOK));
            long itemID = Long.parseLong(record.get(ITEM_TOK));
            long timestamp = Long.parseLong(record.get(TIME_TOK));
            double preference = Double.parseDouble(record.get(PREFERENCE_TOK));
            dataset.addPreference(userID, itemID, preference);
            dataset.addTimestamp(userID, itemID, timestamp);
        }
        return dataset;
    }


}
