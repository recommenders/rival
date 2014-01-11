package net.recommenders.rival.core;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Alejandro
 */
public interface ParserWithIdMapping {

    public DataModel<Long, Long> parseData(File f, String mapIdsPrefix) throws IOException;
}
