package net.recommenders.rival.core;

import java.io.File;
import java.io.IOException;

/**
 * Data model parser interface.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public interface Parser {

    /**
     * Parse data file.
     *
     * @param f The file to be parsed.
     * @return A dataset created from the file.
     * @throws IOException if the file cannot be read.
     */
    public DataModel<Long, Long> parseData(File f) throws IOException;
}
