package net.recommenders.rival.split.splitter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import net.recommenders.rival.split.parser.ParserRunner;

/**
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class Split {

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
