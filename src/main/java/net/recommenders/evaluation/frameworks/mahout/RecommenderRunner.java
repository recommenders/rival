/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.frameworks.mahout;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 *
 * @author alejandr
 */
public class RecommenderRunner {

    private String inputPath;
    private String outputPath;
    private Map<String, String> parameters;

    public void run() throws IOException, TasteException {
        Recommender recommender = null;
        DataModel model = null;
        // instantiate everything
        model = new FileDataModel(new File(inputPath));
        // 
        LongPrimitiveIterator users = model.getUserIDs();
        while (users.hasNext()) {
            long u = users.nextLong();
            recommender.recommend(u, model.getNumItems());
            // write to disk
        }
    }
}
