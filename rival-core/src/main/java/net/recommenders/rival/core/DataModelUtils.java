package net.recommenders.rival.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class DataModelUtils {

    /**
     * Method that saves a data model to a file.
     *
     * @param dm the data model
     * @param outfile file where the model will be saved
     * @param overwrite flag that indicates if the file should be overwritten
     * @param <U> user
     * @param <I> item
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static <U, I> void saveDataModel(DataModel<U, I> dm, String outfile, boolean overwrite) throws FileNotFoundException, UnsupportedEncodingException {
        if (new File(outfile).exists() && !overwrite) {
            System.out.println("Ignoring " + outfile);
        } else {
            PrintStream out = new PrintStream(outfile, "UTF-8");
            for (U user : dm.getUsers()) {
                Map<I, Double> userPrefModel = dm.getUserItemPreferences().get(user);
                Map<I, Set<Long>> userTimeModel = dm.getUserItemTimestamps().get(user);
                for (Entry<I, Double> e : userPrefModel.entrySet()) {
                    I item = e.getKey();
                    Double pref = userPrefModel.get(item);
                    Set<Long> time = userTimeModel != null ? userTimeModel.get(item) : null;
                    if (time == null) {
                        out.println(user + "\t" + item + "\t" + pref + "\t-1");
                    } else {
                        for (Long t : time) {
                            out.println(user + "\t" + item + "\t" + pref + "\t" + t);
                        }
                    }
                }
            }
            out.close();
        }
    }
}
