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
package net.recommenders.rival.split.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import net.recommenders.rival.core.SimpleParser;

/**
 * Parser for the Last.fm dataset by O Celma.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class AbstractLastfmCelmaParser {

    /**
     * A flag that indicates if the artists should be considered as the items
     * (instead of tracks).
     */
    private boolean useArtists;

    /**
     * Default constructor.
     *
     * @param useTheArtists Flag to consider artists as the items (instead of
     * tracks).
     */
    public AbstractLastfmCelmaParser(final boolean useTheArtists) {
        this.useArtists = useTheArtists;
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
    public static long getIndexMap(final File in, final Map<String, Long> map) throws IOException {
        long id = 0;
        if (in.exists()) {
            BufferedReader br = SimpleParser.getBufferedReader(in);
            String line;
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

    /**
     * Gets the value of the flag indicating if the artists should be considered
     * as items (instead of tracks).
     *
     * @return the flag
     */
    protected boolean isUseArtists() {
        return useArtists;
    }
}
