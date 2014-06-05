package net.recommenders.rival.recommend.frameworks;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.grouplens.lenskit.scored.ScoredId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class RecommenderIO {


        /**
         * Write recommendations to file.
         * @param user  the user
         * @param recommendations   the recommendations
         * @param <T> list
         */
    public static <T> void writeData(long user, List<T> recommendations, String path, String fileName) {
        try {
            File dir = new File(path);
            if (!dir.isDirectory()) {
                dir.mkdir();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(path + "/" + fileName, true));
            for (Object ri : recommendations) {
                if (ri instanceof RecommendedItem) {
                    out.write(user + "\t" + ((RecommendedItem) ri).getItemID() + "\t" + ((RecommendedItem) ri).getValue() + "\n");
                }
                if (ri instanceof ScoredId) {
                    out.write(user + "\t" + ((ScoredId) ri).getId() + "\t" + ((ScoredId) ri).getScore() + "\n");
                }
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
//            logger.error(e.getMessage());
        }
    }



}
