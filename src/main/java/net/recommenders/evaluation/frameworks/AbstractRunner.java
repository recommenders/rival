package net.recommenders.evaluation.frameworks;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.grouplens.lenskit.scored.ScoredId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: alan
 * Date: 2013-11-22
 * Time: 11:34
 */
public class AbstractRunner {
    private final static Logger logger = LoggerFactory.getLogger(AbstractRunner.class);
    public Map<String, String> parameters;



    public <T> void writeData(String path, long user, List<T> recommendations) {
        try {
            File dir = new File(path);
           if (!dir.isDirectory())
               dir.mkdir();
            BufferedWriter out = new BufferedWriter(new FileWriter(path+"_recommendations.tsv", true));
            for(Object ri : recommendations){
                if (ri instanceof RecommendedItem)
                    out.write(user + "\t" + ((RecommendedItem)ri).getItemID() + "\t" + ((RecommendedItem)ri).getValue() + "\n");
                if (ri instanceof ScoredId)
                    out.write(user + "\t" + ((ScoredId)ri).getId() + "\t" + ((ScoredId)ri).getScore() + "\n");
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.error(e.getMessage());
        }
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
