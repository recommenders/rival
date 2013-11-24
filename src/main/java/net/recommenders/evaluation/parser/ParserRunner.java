/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.parser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import net.recommenders.evaluation.core.DataModel;

/**
 *
 * @author nets
 */
public class ParserRunner {

    public static final String DATASET_FILE = "dataset.file";
    public static final String DATASET_PARSER = "dataset.parser";
    public static final String LASTFM_IDS_PREFIX = "dataset.parser.lastfm.idsprefix";
    public static final String LASTFM_USEARTISTS = "dataset.parser.lastfm.useartists";

    public static DataModel<Long, Long> run(Properties properties) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
        DataModel<Long, Long> model = null;
        File file = new File(properties.getProperty(DATASET_FILE));
        String parserClassName = properties.getProperty(DATASET_PARSER);
        Class<?> parserClass = Class.forName(parserClassName);
        if (parserClassName.contains("Movielens")) {
            MovielensParser parser = (MovielensParser) parserClass.getConstructor().newInstance();
            model = parser.parseData(file);
        } else if (parserClassName.contains("LastfmCelma")) {
            String mapIdsPrefix = properties.getProperty(LASTFM_IDS_PREFIX);
            Boolean useArtists = Boolean.parseBoolean(properties.getProperty(LASTFM_USEARTISTS));
            Object parser = parserClass.getConstructor(boolean.class).newInstance(useArtists);
            model = (DataModel<Long, Long>) parserClass.getMethod("parseData", File.class, String.class).invoke(parser, file, mapIdsPrefix);
        }
        return model;
    }
}
