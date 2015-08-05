package net.recommenders.rival.core;

/**
 * Abstract class for datamodel parsers.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public abstract class AbstractParser {

    /**
     * The column index for the user id in the file.
     */
    public static final int USER_TOK = 0;
    /**
     * The column index for the item id in the file.
     */
    public static final int ITEM_TOK = 1;
    /**
     * The column index for the interaction value (e.g. rating) in the file.
     */
    public static final int PREFERENCE_TOK = 2;
    /**
     * The column index for the timestamp in the file.
     */
    public static final int TIME_TOK = 3;
    /**
     * Whether the file contains a column header.
     */
    public static final boolean HAS_HEADER = false;
    /**
     * The column delimiter.
     */
    public static final char DELIMITER = ',';
    /**
     * Protected variables
     */
    protected int userTok;
    protected int itemTok;
    protected int prefTok;
    protected int timeTok;
    protected boolean hasHeader;
    protected char delimiter;

    /**
     * Default constructor
     */
    public AbstractParser() {
        this.userTok = USER_TOK;
        this.itemTok = ITEM_TOK;
        this.prefTok = PREFERENCE_TOK;
        this.timeTok = TIME_TOK;
        this.hasHeader = HAS_HEADER;
        this.delimiter = DELIMITER;
    }

    /**
     * Gets the delimiter
     *
     * @return the delimiter
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Checks if this parser has a header
     *
     * @return true if it has a header
     */
    public boolean isHasHeader() {
        return hasHeader;
    }

    /**
     * Gets the column index for items
     *
     * @return the column index for items
     */
    public int getItemTok() {
        return itemTok;
    }

    /**
     * Gets the column index for preferences
     *
     * @return the column index for preferences
     */
    public int getPrefTok() {
        return prefTok;
    }

    /**
     * Gets the column index for time
     *
     * @return the column index for time
     */
    public int getTimeTok() {
        return timeTok;
    }

    /**
     * Gets the column index for users
     *
     * @return the column index for users
     */
    public int getUserTok() {
        return userTok;
    }

    /**
     * Sets the field delimiter
     *
     * @param delimiter the delimiter between fields
     */
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Sets the flag indicating whether the file has a header
     *
     * @param hasHeader flag indicating whether the file has a header
     */
    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    /**
     * Sets the column index for items
     *
     * @param itemTok the column index for items
     */
    public void setItemTok(int itemTok) {
        this.itemTok = itemTok;
    }

    /**
     * Sets the column index for preferences
     *
     * @param prefTok the column index for preferences
     */
    public void setPrefTok(int prefTok) {
        this.prefTok = prefTok;
    }

    /**
     * Sets the column index for time
     *
     * @param timeTok the column index for time
     */
    public void setTimeTok(int timeTok) {
        this.timeTok = timeTok;
    }

    /**
     * Sets the column index for users
     *
     * @param userTok the column index for users
     */
    public void setUserTok(int userTok) {
        this.userTok = userTok;
    }
}
