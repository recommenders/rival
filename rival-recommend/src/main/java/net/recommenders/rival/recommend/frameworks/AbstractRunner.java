package net.recommenders.rival.recommend.frameworks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.grouplens.lenskit.scored.ScoredId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
public abstract class AbstractRunner {

    private final static Logger logger = LoggerFactory.getLogger(AbstractRunner.class);
    public static final int DEFAULT_ITERATIONS = 50;
    public Properties properties;
    private String fileName;
    private String path;
    protected boolean alreadyRecommended;

    public AbstractRunner(Properties properties) {
        this.properties = properties;
        this.setFileName();
        String filePath = properties.getProperty(RecommendationRunner.output) + "/" + fileName;
        alreadyRecommended = new File(filePath).exists();
        if (alreadyRecommended) {
            System.out.println("File exists: " + filePath);
        }
        path = properties.getProperty(RecommendationRunner.output);
    }

    public <T> void writeData(long user, List<T> recommendations) {
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
            logger.error(e.getMessage());
        }
    }

    public void setFileName() {
        String type = "";
        // lenskit does not provide a factorizer class. This check is to actually see if it's a Mahout or Lenskit SVD.
        if (properties.containsKey(RecommendationRunner.factorizer) || properties.containsKey(RecommendationRunner.similarity)) {
            type = (properties.containsKey(RecommendationRunner.factorizer)
                    ? properties.getProperty(RecommendationRunner.factorizer)
                    : properties.getProperty(RecommendationRunner.similarity));
            type = type.substring(type.lastIndexOf(".") + 1) + ".";
        }
        String num = "";
        if (properties.containsKey(RecommendationRunner.factors) || properties.containsKey(RecommendationRunner.neighborhood)) {
            num = (properties.containsKey(RecommendationRunner.factors)
                    ? properties.getProperty(RecommendationRunner.factors)
                    : properties.getProperty(RecommendationRunner.neighborhood)) + ".";
        }

        String trainingSet = properties.getProperty(RecommendationRunner.trainingSet);
        trainingSet = trainingSet.substring(trainingSet.lastIndexOf("/") + 1, trainingSet.lastIndexOf("_train"));

        fileName = trainingSet + "."
                + properties.getProperty(RecommendationRunner.framework) + "."
                + properties.getProperty(RecommendationRunner.recommender).substring(properties.getProperty(RecommendationRunner.recommender).lastIndexOf(".") + 1) + "."
                + type
                + num
                + "tsv";

        System.out.println(fileName);
    }

    public String getCanonicalFileName() {
        return path + "/" + fileName + ".stats";
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public boolean getAlreadyRecommended() {
        return alreadyRecommended;
    }

    public abstract void run() throws Exception;
}
