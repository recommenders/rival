package net.recommenders.rival.core;

import java.io.File;
import java.io.IOException;

/**
 * Parser of files where users or items are not represented as integer ids
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public interface ParserWithIdMapping {

    /**
     * Parse data file
     *
     * @param f The file to parse.
     * @param mapIdsPrefix The prefix of the file where the id mapping will be
     * stored (and will be read from).
     * @return The data model created from the file.
     * @throws IOException if the file cannot be read.
     */
    public DataModel<Long, Long> parseData(File f, String mapIdsPrefix) throws IOException;
}
