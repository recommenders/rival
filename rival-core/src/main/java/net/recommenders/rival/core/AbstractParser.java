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

/**
 * Abstract class for datamodel parsers.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public abstract class AbstractParser {

    /**
     * Default for the column index for the user id in the file.
     */
    public static final int USER_TOK = 0;
    /**
     * Default for the column index for the item id in the file.
     */
    public static final int ITEM_TOK = 1;
    /**
     * Default for the column index for the interaction value (e.g. rating) in
     * the file.
     */
    public static final int PREFERENCE_TOK = 2;
    /**
     * Default for the column index for the timestamp in the file.
     */
    public static final int TIME_TOK = 3;
    /**
     * Default for whether the file contains a column header.
     */
    public static final boolean HAS_HEADER = false;
    /**
     * Default for the column delimiter.
     */
    public static final char DELIMITER = ',';
    /**
     * The column index for the user id in the file.
     */
    private int userTok;
    /**
     * The column index for the item id in the file.
     */
    private int itemTok;
    /**
     * The column index for the interaction value (e.g. rating) in the file.
     */
    private int prefTok;
    /**
     * The column index for the timestamp in the file.
     */
    private int timeTok;
    /**
     * Whether the file contains a column header.
     */
    private boolean hasHeader;
    /**
     * The column delimiter.
     */
    private char delimiter;

    /**
     * Default constructor.
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
     * Gets the delimiter.
     *
     * @return the delimiter
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Checks if this parser has a header.
     *
     * @return true if it has a header
     */
    public boolean isHasHeader() {
        return hasHeader;
    }

    /**
     * Gets the column index for items.
     *
     * @return the column index for items
     */
    public int getItemTok() {
        return itemTok;
    }

    /**
     * Gets the column index for preferences.
     *
     * @return the column index for preferences
     */
    public int getPrefTok() {
        return prefTok;
    }

    /**
     * Gets the column index for time.
     *
     * @return the column index for time
     */
    public int getTimeTok() {
        return timeTok;
    }

    /**
     * Gets the column index for users.
     *
     * @return the column index for users
     */
    public int getUserTok() {
        return userTok;
    }

    /**
     * Sets the field delimiter.
     *
     * @param del the delimiter between fields
     */
    public void setDelimiter(final char del) {
        this.delimiter = del;
    }

    /**
     * Sets the flag indicating whether the file has a header.
     *
     * @param header flag indicating whether the file has a header
     */
    public void setHasHeader(final boolean header) {
        this.hasHeader = header;
    }

    /**
     * Sets the column index for items.
     *
     * @param itemToken the column index for items
     */
    public void setItemTok(final int itemToken) {
        this.itemTok = itemToken;
    }

    /**
     * Sets the column index for preferences.
     *
     * @param prefToken the column index for preferences
     */
    public void setPrefTok(final int prefToken) {
        this.prefTok = prefToken;
    }

    /**
     * Sets the column index for time.
     *
     * @param timeToken the column index for time
     */
    public void setTimeTok(final int timeToken) {
        this.timeTok = timeToken;
    }

    /**
     * Sets the column index for users.
     *
     * @param userToken the column index for users
     */
    public void setUserTok(final int userToken) {
        this.userTok = userToken;
    }
}
