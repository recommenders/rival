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
package net.recommenders.rival.split.splitter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.recommenders.rival.core.TemporalDataModelIF;
import net.recommenders.rival.split.parser.ParserRunner;

/**
 * Main class that parses a data set and splits it according to a property file.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public final class Split {

    /**
     * Utility classes should not have a public or default constructor.
     */
    private Split() {
    }

    /**
     * Main method that loads properties from a file and runs a SplitterRunner.
     *
     * @param args program arguments (not used)
     * @throws Exception see {@link net.recommenders.rival.split.splitter.SplitterRunner#run(Properties, TemporalDataModelIF, boolean)}
     * @see net.recommenders.rival.split.splitter.SplitterRunner
     */
    public static void main(final String[] args) throws Exception {
        String propertyFile = System.getProperty("propertyFile");

        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        SplitterRunner.run(properties, ParserRunner.run(properties), true);
    }
}
