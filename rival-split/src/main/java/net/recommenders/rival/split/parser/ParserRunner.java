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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.Parser;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Runner for the parser classes.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public final class ParserRunner {

    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String DATASET_FILE = "dataset.file";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String DATASET_PARSER = "dataset.parser";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String LASTFM_IDS_PREFIX = "dataset.parser.lastfm.idsprefix";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String LASTFM_USEARTISTS = "dataset.parser.lastfm.useartists";

    /**
     * Utility classes should not have a public or default constructor.
     */
    private ParserRunner() {
    }

    /**
     * Run the parser based on given properties.
     *
     * @param properties The properties
     * @return The data model parsed by the parser.
     * @throws ClassNotFoundException when {@link Class#forName(java.lang.String)}
     * fails
     * @throws IllegalAccessException when {@link java.lang.reflect.Method#invoke(java.lang.Object, java.lang.Object[])}
     * fails
     * @throws InstantiationException when {@link Parser#parseData(java.io.File)}
     * fails
     * @throws InvocationTargetException when {@link java.lang.reflect.Method#invoke(java.lang.Object, java.lang.Object[])}
     * fails
     * @throws NoSuchMethodException when {@link Class#getMethod(java.lang.String, java.lang.Class[])}
     * fails
     * @throws IOException when {@link Parser#parseData(java.io.File)} fails
     */
    public static TemporalDataModelIF<Long, Long> run(final Properties properties) throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {
        System.out.println("Parsing started");
        TemporalDataModelIF<Long, Long> model = null;
        File file = new File(properties.getProperty(DATASET_FILE));
        String parserClassName = properties.getProperty(DATASET_PARSER);
        Class<?> parserClass = Class.forName(parserClassName);
        Parser<Long, Long> parser = instantiateParser(properties);
        if (parserClassName.contains("LastfmCelma")) {
            String mapIdsPrefix = properties.getProperty(LASTFM_IDS_PREFIX);
            Object modelObj = parserClass.getMethod("parseData", File.class, String.class).invoke(parser, file, mapIdsPrefix);
            if (modelObj instanceof TemporalDataModelIF) {
                @SuppressWarnings("unchecked")
                TemporalDataModelIF<Long, Long> modelTemp = (TemporalDataModelIF<Long, Long>) modelObj;
                model = modelTemp;
            }
        } else {
            model = parser.parseTemporalData(file);
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
     * @throws ClassNotFoundException when {@link Class#forName(java.lang.String)}
     * fails
     * @throws IllegalAccessException when {@link java.lang.reflect.Method#invoke(java.lang.Object, java.lang.Object[])}
     * fails
     * @throws InstantiationException when {@link Parser#parseData(java.io.File)
     * } fails
     * @throws InvocationTargetException when {@link java.lang.reflect.Method#invoke(java.lang.Object, java.lang.Object[])}
     * fails
     * @throws NoSuchMethodException when {@link Class#getMethod(java.lang.String, java.lang.Class[])}
     * fails
     */
    @SuppressWarnings("unchecked")
    public static Parser<Long, Long> instantiateParser(final Properties properties) throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException {
        String parserClassName = properties.getProperty(DATASET_PARSER);
        Class<?> parserClass = Class.forName(parserClassName);
        Parser<Long, Long> parser;
        if (parserClassName.contains("LastfmCelma")) {
            Boolean useArtists = Boolean.parseBoolean(properties.getProperty(LASTFM_USEARTISTS));
            parser = (Parser<Long, Long>) parserClass.getConstructor(boolean.class).newInstance(useArtists);
        } else {
            parser = (Parser<Long, Long>) parserClass.getConstructor().newInstance();
        }
        return parser;
    }
}
