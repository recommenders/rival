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
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.ParserWithIdMapping;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Parser for the 360K Last.fm dataset by O Celma. More information here
 * http://www.dtic.upf.edu/~ocelma/MusicRecommendationDataset/lastfm-360K.html
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class LastfmCelma360KParser extends AbstractLastfmCelmaParser implements ParserWithIdMapping<Long, Long> {

    /**
     * The column index for the user id in the file.
     */
    public static final int USER_TOK = 0;
    /**
     * The column index for the artist id in the file.
     */
    public static final int ARTIST_TOK = 1;
    /**
     * The column index for the track id in the file.
     */
    public static final int TRACK_TOK = 2;
    /**
     * The column index for the preference number in the file.
     */
    public static final int PREF_TOK = 3;

    /**
     * Constructor.
     *
     * @param useArtists Flag to consider artists as the items (instead of
     * tracks).
     */
    public LastfmCelma360KParser(final boolean useArtists) {
        super(useArtists);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<Long, Long> parseData(final File f) throws IOException {
        return parseTemporalData(f);
    }

    @Override
    public TemporalDataModelIF<Long, Long> parseTemporalData(File f) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataModelIF<Long, Long> parseData(final File f, final String mapIdsPrefix) throws IOException {
        return parseTemporalData(f, mapIdsPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalDataModelIF<Long, Long> parseTemporalData(final File f, final String mapIdsPrefix) throws IOException {
        TemporalDataModelIF<Long, Long> dataset = DataModelFactory.getDefaultTemporalModel();

        Map<String, Long> mapUserIds = new HashMap<>();
        Map<String, Long> mapItemIds = new HashMap<>();

        long curUser = getIndexMap(new File(mapIdsPrefix + "_userId.txt"), mapUserIds);
        long curItem = getIndexMap(new File(mapIdsPrefix + "_itemId.txt"), mapItemIds);

        BufferedReader br = SimpleParser.getBufferedReader(f);
        String line;
        while ((line = br.readLine()) != null) {
            String[] toks = line.split("\t");
            // user
            String user = toks[USER_TOK];
            if (!mapUserIds.containsKey(user)) {
                mapUserIds.put(user, curUser);
                curUser++;
            }
            long userId = mapUserIds.get(user);
            // item
            String artist = toks[ARTIST_TOK];
            String track = toks[TRACK_TOK];
            String item;
            if (isUseArtists()) {
                item = artist;
            } else {
                item = artist + "_" + track;
            }
            if (!mapItemIds.containsKey(item)) {
                mapItemIds.put(item, curItem);
                curItem++;
            }
            long itemId = mapItemIds.get(item);
            // preference
            double preference = Double.parseDouble(toks[PREF_TOK]);
            //////
            // update information
            //////
            dataset.addPreference(userId, itemId, preference);
        }
        br.close();

        // save map ids?
        if (mapIdsPrefix != null) {
            // save user map
            PrintStream outUser = new PrintStream(mapIdsPrefix + "_userId.txt", "UTF-8");
            for (Entry<String, Long> e : mapUserIds.entrySet()) {
                outUser.println(e.getKey() + "\t" + e.getValue());
            }
            outUser.close();
            // save item map
            PrintStream outItem = new PrintStream(mapIdsPrefix + "_itemId.txt", "UTF-8");
            for (Entry<String, Long> e : mapItemIds.entrySet()) {
                outItem.println(e.getKey() + "\t" + e.getValue());
            }
            outItem.close();
        }

        return dataset;
    }
}
