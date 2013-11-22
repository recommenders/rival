package net.recommenders.evaluation.frameworks.mahout.exceptions;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alan
 * Date: 2013-11-22
 * Time: 11:34
 */
public class AbstractRunner {
    private final static Logger logger = LoggerFactory.getLogger(AbstractRunner.class);

    public void writeData(String path, long user, List<RecommendedItem> recommendations) {
        try {
            File dir = new File(path);
           if (!dir.isDirectory())
               dir.mkdir();
            BufferedWriter out = new BufferedWriter(new FileWriter(path+"/recommendations.tsv", true));
            for(RecommendedItem ri : recommendations){
                out.write(user +"\t"+ ri.getItemID() + "\t" + ri.getValue() +"\n");
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.error(e.getMessage());
        }
    }
}
