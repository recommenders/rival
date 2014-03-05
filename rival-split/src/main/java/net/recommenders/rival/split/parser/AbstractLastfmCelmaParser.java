package net.recommenders.rival.split.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Parser for the Last.fm dataset by Celma.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class AbstractLastfmCelmaParser {

    protected boolean useArtists;

    /**
     * Default constructor.
     *
     * @param useArtists Flag to consider artists as the items (instead of
     * tracks).
     */
    public AbstractLastfmCelmaParser(boolean useArtists) {
        this.useArtists = useArtists;
    }

    /**
     * Read a user/item mapping (user/item original value, user/item internal
     * id) from a file and return the maximum index number in that file.
     *
     * @param in The file with id mapping.
     * @param map The user/item mapping
     * @return The largest id number.
     * @throws IOException if file does not exist.
     */
    public static long getIndexMap(File in, Map<String, Long> map) throws IOException {
        long id = 0;
        if (in.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(in));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] toks = line.split("\t");
                long i = Long.parseLong(toks[1]);
                map.put(toks[0], i);
                id = Math.max(i, id);
            }
            br.close();
        }
        return id + 1;
    }
}
