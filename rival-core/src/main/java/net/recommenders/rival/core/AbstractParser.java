package net.recommenders.rival.core;

/**
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

    public AbstractParser() {
        this.userTok = USER_TOK;
        this.itemTok = ITEM_TOK;
        this.prefTok = PREFERENCE_TOK;
        this.timeTok = TIME_TOK;
        this.hasHeader = HAS_HEADER;
        this.delimiter = DELIMITER;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public int getItemTok() {
        return itemTok;
    }

    public int getPrefTok() {
        return prefTok;
    }

    public int getTimeTok() {
        return timeTok;
    }

    public int getUserTok() {
        return userTok;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public void setItemTok(int itemTok) {
        this.itemTok = itemTok;
    }

    public void setPrefTok(int prefTok) {
        this.prefTok = prefTok;
    }

    public void setTimeTok(int timeTok) {
        this.timeTok = timeTok;
    }

    public void setUserTok(int userTok) {
        this.userTok = userTok;
    }
}
