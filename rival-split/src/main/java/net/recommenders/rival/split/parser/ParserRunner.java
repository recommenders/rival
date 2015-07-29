package net.recommenders.rival.split.parser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.Parser;

/**
 * Runner for the parser classes.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class ParserRunner {

    /**
     * Variables that represent the name of several properties in the file.
     */
    public static final String DATASET_FILE = "dataset.file";
    public static final String DATASET_PARSER = "dataset.parser";
    public static final String LASTFM_IDS_PREFIX = "dataset.parser.lastfm.idsprefix";
    public static final String LASTFM_USEARTISTS = "dataset.parser.lastfm.useartists";

    /**
     * Run the parser based on given properties.
     *
     * @param properties The properties
     * @return The data model parsed by the parser.
     * @throws ClassNotFoundException when no class found
     * @throws IllegalAccessException when
     * @throws IllegalArgumentException when argument is illegal
     * @throws InstantiationException when
     * @throws InvocationTargetException when
     * @throws NoSuchMethodException when
     * @throws SecurityException when
     * @throws IOException when
     */
    public static DataModel<Long, Long> run(Properties properties) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
        System.out.println("Parsing started");
        DataModel<Long, Long> model = null;
        File file = new File(properties.getProperty(DATASET_FILE));
        String parserClassName = properties.getProperty(DATASET_PARSER);
        Class<?> parserClass = Class.forName(parserClassName);
        Parser parser = instantiateParser(properties);
        if (parserClassName.contains("LastfmCelma")) {
            String mapIdsPrefix = properties.getProperty(LASTFM_IDS_PREFIX);
            Object modelObj = parserClass.getMethod("parseData", File.class, String.class).invoke(parser, file, mapIdsPrefix);
            if (modelObj instanceof DataModel) {
                @SuppressWarnings("unchecked")
                DataModel<Long, Long> modelTemp = (DataModel<Long, Long>) modelObj;
                model = modelTemp;
            }
        } else {
            model = parser.parseData(file);
        }
        System.out.println("Parsing finished");
        return model;
    }

    /**
     *
     * Instantiates a parser based on the properties.
     *
     * @param properties the properties to be used.
     * @return a parser according to the provided properties.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Parser instantiateParser(Properties properties) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        String parserClassName = properties.getProperty(DATASET_PARSER);
        Class<?> parserClass = Class.forName(parserClassName);
        Parser parser = null;
        if (parserClassName.contains("LastfmCelma")) {
            Boolean useArtists = Boolean.parseBoolean(properties.getProperty(LASTFM_USEARTISTS));
            parser = (Parser) parserClass.getConstructor(boolean.class).newInstance(useArtists);
        } else {
            parser = (Parser) parserClass.getConstructor().newInstance();
        }
        return parser;
    }
}
