package net.recommenders.rival.recommend.frameworks.mahout;

import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.mahout.exceptions.RecommenderException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * A runner for Mahout-based recommenders.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>, <a
 * href="http://github.com/alansaid">Alan</a>
 */
public class MahoutRecommenderRunner extends AbstractRunner<Long, Long> {

    /**
     * Default neighborhood size.
     */
    public static final int DEFAULT_NEIGHBORHOOD_SIZE = 50;
    /**
     * Default number of iterations.
     */
    public static final int DEFAULT_ITERATIONS = 50;

    /**
     * Default constructor.
     *
     * @param _properties the properties.
     */
    public MahoutRecommenderRunner(Properties _properties) {
        super(_properties);
    }

    /**
     * Runs the recommender.
     *
     * @throws IOException when paths in property object are incorrect..
     * @throws TasteException when the recommender is instantiated incorrectly
     * or breaks otherwise.
     */
    @Override
    public net.recommenders.rival.core.DataModel<Long, Long> run(RUN_OPTIONS opts) throws IOException, TasteException {
        if (alreadyRecommended) {
            return null;
        }
        DataModel trainingModel = new FileDataModel(new File(properties.getProperty(RecommendationRunner.trainingSet)));
        DataModel testModel = new FileDataModel(new File(properties.getProperty(RecommendationRunner.testSet)));
        return runMahoutRecommender(opts, trainingModel, testModel);
    }

    @Override
    public net.recommenders.rival.core.DataModel<Long, Long> run(RUN_OPTIONS opts, net.recommenders.rival.core.DataModel<Long, Long> trainingModel, net.recommenders.rival.core.DataModel<Long, Long> testModel) throws IOException, TasteException {
        if (alreadyRecommended) {
            return null;
        }
        // transform from core's DataModels to Mahout's DataModels
        // TODO
        DataModel trainingModelMahout = new DataModelWrapper(trainingModel);
        DataModel testModelMahout = new DataModelWrapper(testModel);

        return runMahoutRecommender(opts, trainingModelMahout, testModelMahout);
    }

    public net.recommenders.rival.core.DataModel<Long, Long> runMahoutRecommender(RUN_OPTIONS opts, DataModel trainingModel, DataModel testModel) throws IOException, TasteException {
        if (alreadyRecommended) {
            return null;
        }

        GenericRecommenderBuilder grb = new GenericRecommenderBuilder();

        if (properties.containsKey(RecommendationRunner.neighborhood) && properties.getProperty(RecommendationRunner.neighborhood).equals("-1")) {
            properties.setProperty(RecommendationRunner.neighborhood, Math.round(Math.sqrt(trainingModel.getNumItems())) + "");
        }
        if (properties.containsKey(RecommendationRunner.factors) && properties.getProperty(RecommendationRunner.factors).equals("-1")) {
            properties.setProperty(RecommendationRunner.factors, Math.round(Math.sqrt(trainingModel.getNumItems())) + "");
        }


        Recommender recommender = null;
        try {
            if (properties.getProperty(RecommendationRunner.factors) == null) {
                recommender = grb.buildRecommender(trainingModel, properties.getProperty(RecommendationRunner.recommender), properties.getProperty(RecommendationRunner.similarity), Integer.parseInt(properties.getProperty(RecommendationRunner.neighborhood)));
            }
            if (properties.getProperty(RecommendationRunner.factors) != null) //                recommender = grb.buildRecommender(trainModel, properties.getProperty(RecommendationRunner.recommender), properties.getProperty(RecommendationRunner.factorizer), Integer.parseInt(properties.getProperty(RecommendationRunner.iterations)), Integer.parseInt(properties.getProperty(RecommendationRunner.factors)));
            {
                recommender = grb.buildRecommender(trainingModel, properties.getProperty(RecommendationRunner.recommender), properties.getProperty(RecommendationRunner.factorizer), DEFAULT_ITERATIONS, Integer.parseInt(properties.getProperty(RecommendationRunner.factors)));
            }
            //(dataModel, recommender, factorizer, default iterations, factors)
        } catch (RecommenderException e) {
            e.printStackTrace();
        }

        LongPrimitiveIterator users = testModel.getUserIDs();

        net.recommenders.rival.core.DataModel<Long, Long> model = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case RETURN_RECS:
                model = new net.recommenders.rival.core.DataModel();
                break;
            default:
                model = null;
        }
        String name = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case OUTPUT_RECS:
                name = fileName;
                break;
            default:
                name = null;
        }
        boolean createFile = true;
        while (users.hasNext()) {
            long u = users.nextLong();
            try {
                List<RecommendedItem> items = recommender.recommend(u, trainingModel.getNumItems());
                RecommenderIO.writeData(u, items, path, name, !createFile, model);
                createFile = false;
            } catch (TasteException e) {
                e.printStackTrace();
            }
        }
        return model;
    }

    /**
     * Set the properties object.
     *
     * @param parameters the properties.
     */
    public void setProperties(Properties parameters) {
        this.properties = parameters;
    }
}
