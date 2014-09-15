package net.recommenders.rival.core;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public abstract class AbstractParser implements Parser {
    public static void setUSER_TOK(int USER_TOK) {
        AbstractParser.USER_TOK = USER_TOK;
    }

    public static void setITEM_TOK(int ITEM_TOK) {
        AbstractParser.ITEM_TOK = ITEM_TOK;
    }

    public static void setPREFERENCE_TOK(int PREFERENCE_TOK) {
        AbstractParser.PREFERENCE_TOK = PREFERENCE_TOK;
    }

    public static void setTIME_TOK(int TIME_TOK) {
        AbstractParser.TIME_TOK = TIME_TOK;
    }

    public static void setHAS_HEADER(boolean HAS_HEADER) {
        AbstractParser.HAS_HEADER = HAS_HEADER;
    }

    public static void setDELIMITER(char DELIMITER) {
        AbstractParser.DELIMITER = DELIMITER;
    }

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
    public static char DELIMITER = ',';




}
