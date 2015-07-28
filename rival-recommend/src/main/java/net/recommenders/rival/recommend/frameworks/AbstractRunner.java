package net.recommenders.rival.recommend.frameworks;

import java.io.File;
import java.util.Properties;
import net.recommenders.rival.core.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract recommender runner.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
public abstract class AbstractRunner<U, I> {

    public enum RUN_OPTIONS {

        RETURN_RECS,
        OUTPUT_RECS,
        RETURN_AND_OUTPUT_RECS;
    }
    /**
     * Logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(AbstractRunner.class);
    /**
     * Default number of iterations.
     */
    public static final int DEFAULT_ITERATIONS = 50;
    /**
     * The properties.
     */
    public Properties properties;
    /**
     * The file name where the properties live.
     */
    public String fileName;
    /**
     * The path where output is written to.
     */
    public String path;
    /**
     * True if this recommender has already been issued and output files exist.
     */
    protected boolean alreadyRecommended;

    /**
     * Default constructor.
     *
     * @param properties The properties.
     */
    public AbstractRunner(Properties properties) {
        this.properties = properties;
        this.setFileName();
        String filePath = properties.getProperty(RecommendationRunner.output, "") + "/" + fileName;
        alreadyRecommended = new File(filePath).exists();
        if (alreadyRecommended) {
            System.out.println("File exists: " + filePath);
        }
        path = properties.getProperty(RecommendationRunner.output, "");
    }

    /**
     * Create the file name of the output file.
     */
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

    /**
     * Get file name with canonical path.
     *
     * @return the file name and path.
     */
    public String getCanonicalFileName() {
        return path + "/" + fileName + ".stats";
    }

    /**
     * Sets the properties.
     *
     * @param properties the properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Check if there already exist recommendations for this recommender.
     *
     * @return true if recommendations exist.
     */
    public boolean getAlreadyRecommended() {
        return alreadyRecommended;
    }

    /**
     * Runs the recommender. Training and test models will be read from file.
     *
     * @param opts options to run this recommender. See {@link RUN_OPTIONS}
     * enum.
     * @throws Exception when the recommender cannot be run. See implementations
     * for more information on possible exceptions.
     */
    public abstract DataModel<U, I> run(RUN_OPTIONS opts) throws Exception;

    /**
     * Runs the recommender using the provided training and test models.
     *
     * @param opts options to run this recommender. See {@link RUN_OPTIONS}
     * enum.
     * @param  trainingModel Model to train the recommender.
     * @param testModel Model from where users to generate recommendations to will be considered.
     * @throws Exception when the recommender cannot be run. See implementations
     * for more information on possible exceptions.
     */
    public abstract DataModel<U, I> run(RUN_OPTIONS opts, DataModel<U, I> trainingModel, DataModel<U, I> testModel) throws Exception;
}
