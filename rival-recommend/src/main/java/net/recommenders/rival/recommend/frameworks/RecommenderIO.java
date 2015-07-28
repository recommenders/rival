package net.recommenders.rival.recommend.frameworks;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.grouplens.lenskit.scored.ScoredId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
     * @param <T> list
     */
    public static <T> void writeData(long user, List<T> recommendations, String path, String fileName, boolean append, DataModel<Long, Long> model) {
        try {
            File dir = null;
            if (path != null) {
                dir = new File(path);
                if (!dir.isDirectory()) {
                    dir.mkdir();
                }
            }
            BufferedWriter out = null;
            if ((path != null) && (fileName != null)) {
                out = new BufferedWriter(new FileWriter(path + "/" + fileName, append));
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
        }
    }
}
