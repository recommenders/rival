package net.recommenders.rival.split.splitter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import net.recommenders.rival.split.parser.ParserRunner;

/**
 * Main class that parses a data set and splits it according to a property file.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class Split {

    /**
     * Main method that loads properties from a file and runs a SplitterRunner
     * @param args program arguments (not used)
     * @throws Exception 
     * @see net.recommenders.rival.split.splitter.SplitterRunner
     */
    public static void main(String[] args) throws Exception {
        String propertyFile = System.getProperty("propertyFile");

        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        SplitterRunner.run(properties, ParserRunner.run(properties), true);
    }
}
