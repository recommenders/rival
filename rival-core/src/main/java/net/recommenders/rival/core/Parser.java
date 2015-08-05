package net.recommenders.rival.core;

import java.io.File;
import java.io.IOException;

/**
 * Data model parser interface.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <U> generic type of users
 * @param <I> generic type of items
 */
public interface Parser<U, I> {

    /**
     * Parse data file.
     *
     * @param f The file to be parsed.
     * @return A dataset created from the file.
     * @throws IOException if the file cannot be read.
     */
    public DataModel<U, I> parseData(File f) throws IOException;
}
