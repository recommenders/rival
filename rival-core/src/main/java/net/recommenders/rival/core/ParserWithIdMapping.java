package net.recommenders.rival.core;

import java.io.File;
import java.io.IOException;

/**
 * Parser of ?
 * @author Alejandro
 */
public interface ParserWithIdMapping {

    /**
     * Parse data file
     * @param f The file to parse.
     * @param mapIdsPrefix Data separator?
     * @return  The data model created from the file.
     * @throws IOException if the file cannot be read.
     */
    public DataModel<Long, Long> parseData(File f, String mapIdsPrefix) throws IOException;
}
