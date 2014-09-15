package net.recommenders.rival.core;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Data parser for tab-separated data files.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class SimpleParser implements Parser {

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
     * @inheritDoc
     */
    @Override
    public DataModel<Long, Long> parseData(File f) throws IOException {
        return parseData(f, "\t");
    }

    /**
     * Parse a data file with a specific separator between fields.
     *
     * @param f The file to be parsed.
     * @param token The separator to be used.
     * @return A dataset created from the file.
     * @throws IOException if the file cannot be read.
     */
    public DataModel<Long, Long> parseData(File f, String token) throws IOException {
        DataModel<Long, Long> dataset = new DataModel<Long, Long>();

        BufferedReader br = SimpleParser.getBufferedReader(f);
        String line = br.readLine();
        if (!line.matches(".*[a-zA-Z].*")) {
            parseLine(line, dataset, token);
        }
        while ((line = br.readLine()) != null) {
            parseLine(line, dataset, token);
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
     * @throws FileNotFoundException when the file does not exist.
     * @throws IOException when the file cannot be read.
     * @see BufferedReader
     */
    public static BufferedReader getBufferedReader(File f) throws FileNotFoundException, IOException {
        BufferedReader br = null;
        if ((f == null) || (!f.isFile())) {
            return br;
        }
        if (f.getName().endsWith(".gz") || f.getName().endsWith(".zip") || f.getName().endsWith(".tgz")) {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
        } else {
            br = new BufferedReader(new FileReader(f));
        }
        return br;
    }

    /**
     * Parse line from data file.
     *
     * @param line The line to be parsed.
     * @param dataset The dataset to add data from line to.
     */
    private void parseLine(String line, DataModel<Long, Long> dataset, String token) {
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
