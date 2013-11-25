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
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: alan
 * Date: 2013-11-22
 * Time: 11:34
 */
public class AbstractRunner {
    private final static Logger logger = LoggerFactory.getLogger(AbstractRunner.class);
    public Properties properties;
    private String fileName;


    public AbstractRunner(Properties properties){
        this.properties = properties;
        this.setFileName();
    }

    public <T> void writeData(String path, long user, List<T> recommendations) {
        try {
            File dir = new File(path);
            if (!dir.isDirectory())
                dir.mkdir();
            BufferedWriter out = new BufferedWriter(new FileWriter(path + "/" + fileName, true));
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

    public void setFileName(){
        String type = "";
        // lenskit does not provide a factorizer class. This check is to actually see if it's a Mahout or Lenskit SVD.
        if(properties.containsKey(Recommend.factorizer) || properties.containsKey(Recommend.similarity)){
            type = (properties.containsKey(Recommend.factorizer) ?
                    properties.getProperty(Recommend.factorizer) :
                    properties.getProperty(Recommend.similarity));
            type = type.substring(type.lastIndexOf(".") + 1)+".";
        }
        String num = "";
        if (properties.containsKey(Recommend.factors) || properties.containsKey(Recommend.neighborhood))
            num = (properties.containsKey(Recommend.factors) ?
                    properties.getProperty(Recommend.factors) :
                    properties.getProperty(Recommend.neighborhood)) + ".";

        String trainingSet =  properties.getProperty(Recommend.trainingSet);
        trainingSet = trainingSet.substring(trainingSet.lastIndexOf("/") + 1, trainingSet.lastIndexOf("."));

        fileName = trainingSet + "." +
                properties.getProperty(Recommend.framework) + "." +
                properties.getProperty(Recommend.recommender).substring(properties.getProperty(Recommend.recommender).lastIndexOf(".") + 1) + "." +
                type +
                num +
                "tsv";

        System.out.println(fileName);
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
