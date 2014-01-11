package net.recommenders.rival.core;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Alejandro
 */
public interface Parser {

    public DataModel<Long, Long> parseData(File f) throws IOException;
}
