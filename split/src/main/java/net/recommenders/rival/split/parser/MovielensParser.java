package net.recommenders.rival.split.parser;

import net.recommenders.rival.core.DataModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import net.recommenders.rival.core.Parser;

/**
 *
 * @author Alejandro
 */
public class MovielensParser implements Parser {

    public static final int USER_TOK = 0;
    public static final int ITEM_TOK = 1;
    public static final int RATING_TOK = 2;
    public static final int TIME_TOK = 3;

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

    private void parseLine(String line, DataModel<Long, Long> dataset) {
        String[] toks = line.contains("::") ? line.split("::") : line.split("\t");
        // user
        long userId = Long.parseLong(toks[USER_TOK]);
//            long userId = TypeFormat.parseLong(toks[userTok], 10, new Cursor());
        // item
        long itemId = Long.parseLong(toks[ITEM_TOK]);
//            long itemId = TypeFormat.parseLong(toks[itemTok], 10, new Cursor());
        // timestamp
        long timestamp = Long.parseLong(toks[TIME_TOK]);
//            long timestamp = TypeFormat.parseLong(toks[timeTok], 10, new Cursor());
        // preference
        double preference = Double.parseDouble(toks[RATING_TOK]);
//            double preference = TypeFormat.parseDouble(toks[ratingTok], new Cursor());
        //////
        // update information
        //////
        dataset.addPreference(userId, itemId, preference);
        dataset.addTimestamp(userId, itemId, timestamp);
    }
}
