package net.recommenders.rival.core;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public abstract class AbstractParser {
    /**
     * The column index for the user id in the file.
     */
    public static int USER_TOK = 0;
    /**
     * The column index for the item id in the file.
     */
    public static int ITEM_TOK = 1;
    /**
     * The column index for the interaction value (e.g. rating) in the file.
     */
    public static int PREFERENCE_TOK = 2;
    /**
     * The column index for the timestamp in the file.
     */
    public static int TIME_TOK = 3;
    /**
     * Whether the file contains a column header.
     */
    public static boolean HAS_HEADER = false;

    /**
     * The column delimiter.
     */
    public static String DELIMITER = ",";
}
