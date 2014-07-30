package net.recommenders.rival.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class DataModelUtils {

    /**
     * Method that saves a data model to a file.
     *
     * @param outfile file where the model will be saved
     * @param overwrite flag that indicates if the file should be overwritten
     * @throws java.io.FileNotFoundException when
     **/
    public static <U,I> void saveDataModel(DataModel<U,I> dm, String outfile, boolean overwrite) throws FileNotFoundException {
        if (new File(outfile).exists() && !overwrite) {
            System.out.println("Ignoring " + outfile);
        } else {
            PrintStream out = new PrintStream(outfile);
            for (U user : dm.getUsers()) {
                Map<I, Double> userPrefModel = dm.getUserItemPreferences().get(user);
                Map<I, Set<Long>> userTimeModel = dm.getUserItemTimestamps().get(user);
                for (I item : userPrefModel.keySet()) {
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
