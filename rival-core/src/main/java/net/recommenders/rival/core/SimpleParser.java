package net.recommenders.rival.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Data parser for tab-separated data files.
 * @author Alejandro
 */
public class SimpleParser implements Parser {

    public static final int USER_TOK = 0;
    public static final int ITEM_TOK = 1;
    public static final int RATING_TOK = 2;
    public static final int TIME_TOK = 3;

    /**
     * @inheritDoc
     */
    @Override
    public DataModel<Long, Long> parseData(File f) throws IOException {
        DataModel<Long, Long> dataset = new DataModel<Long, Long>();

        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = null;
        while ((line = br.readLine()) != null) {
            parseLine(line, dataset);
        }
        br.close();

        return dataset;
    }

    /**
     * Parse line from data file.
     * @param line  The line to be parsed.
     * @param dataset The dataset to add data from line to.
     */
    private void parseLine(String line, DataModel<Long, Long> dataset) {
        String[] toks = line.split("\t");
        // user
        long userId = Long.parseLong(toks[USER_TOK]);
        // item
        long itemId = Long.parseLong(toks[ITEM_TOK]);
        // preference
        double preference = Double.parseDouble(toks[RATING_TOK]);
        // timestamp
        long timestamp = -1;
        // allow no timestamp information
        if (toks.length > 3) {
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
