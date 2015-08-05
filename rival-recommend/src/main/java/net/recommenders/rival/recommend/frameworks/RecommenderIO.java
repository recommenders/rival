package net.recommenders.rival.recommend.frameworks;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.grouplens.lenskit.scored.ScoredId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import net.recommenders.rival.core.DataModel;

/**
 * Recommender-related IO operations.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class RecommenderIO {

    /**
     * Write recommendations to file.
     *
     * @param user the user
     * @param recommendations the recommendations
     * @param path directory where fileName will be written (if not null)
     * @param fileName name of the file, if null recommendations will not be
     * printed
     * @param append flag to decide if recommendations should be appended to
     * file
     * @param model if not null, recommendations will be saved here
     * @param <T> type of recommendations
     */
    public static <T> void writeData(long user, List<T> recommendations, String path, String fileName, boolean append, DataModel<Long, Long> model) {
        BufferedWriter out = null;
        try {
            File dir = null;
            if (path != null) {
                dir = new File(path);
                if (!dir.isDirectory()) {
                    if (!dir.mkdir() && (fileName != null)) {
                        System.out.println("Directory " + path + " could not be created");
                        return;
                    }
                }
            }
            if ((path != null) && (fileName != null)) {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "/" + fileName, append), "UTF-8"));
            }
            for (Object ri : recommendations) {
                if (ri instanceof RecommendedItem) {
                    RecommendedItem recItem = (RecommendedItem) ri;
                    if (out != null) {
                        out.write(user + "\t" + recItem.getItemID() + "\t" + recItem.getValue() + "\n");
                    }
                    if (model != null) {
                        model.addPreference(user, recItem.getItemID(), 1.0 * recItem.getValue());
                    }
                }
                if (ri instanceof ScoredId) {
                    ScoredId recItem = (ScoredId) ri;
                    if (out != null) {
                        out.write(user + "\t" + recItem.getId() + "\t" + recItem.getScore() + "\n");
                    }
                    if (model != null) {
                        model.addPreference(user, recItem.getId(), recItem.getScore());
                    }
                }
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
//            logger.error(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
