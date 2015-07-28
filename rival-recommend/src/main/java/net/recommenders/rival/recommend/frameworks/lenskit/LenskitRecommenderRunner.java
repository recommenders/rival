package net.recommenders.rival.recommend.frameworks.lenskit;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.UserMeanItemScorer;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.IterationCountStoppingCondition;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.knn.user.NeighborhoodFinder;
import org.grouplens.lenskit.knn.user.SimpleNeighborhoodFinder;
import org.grouplens.lenskit.mf.funksvd.FeatureCount;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A runner for LensKit-based recommenders.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
public class LenskitRecommenderRunner extends AbstractRunner<Long, Long> {

    /**
     * Logger
     */
    private final static Logger logger = LoggerFactory.getLogger(LenskitRecommenderRunner.class);

    /**
     * Default constructor
     *
     * @param _properties properties
     */
    public LenskitRecommenderRunner(Properties _properties) {
        super(_properties);
    }

    /**
     * Runs the recommender.
     *
     * @throws IOException when the recommender is instantiated incorrectly or
     * breaks otherwise.
     */
    @Override
    @SuppressWarnings("unchecked")
    public DataModel<Long, Long> run(RUN_OPTIONS opts) throws IOException {
        if (alreadyRecommended) {
            return null;
        }

        File trainingFile = new File(properties.getProperty(RecommendationRunner.trainingSet));
        File testFile = new File(properties.getProperty(RecommendationRunner.testSet));
        EventDAO base = new SimpleFileRatingDAO(trainingFile, "\t");
        EventDAO test = new SimpleFileRatingDAO(testFile, "\t");
        return runLenskitRecommender(opts, base, test);
    }

    @Override
    public DataModel<Long, Long> run(RUN_OPTIONS opts, DataModel<Long, Long> trainingModel, DataModel<Long, Long> testModel) throws IOException {
        if (alreadyRecommended) {
            return null;
        }
        // transform from core's DataModels to Lenskit's EventDAO
        EventDAO trainingModelLensKit = new EventDAOWrapper(trainingModel);
        EventDAO testModelLensKit = new EventDAOWrapper(testModel);

        return runLenskitRecommender(opts, trainingModelLensKit, testModelLensKit);
    }

    public DataModel<Long, Long> runLenskitRecommender(RUN_OPTIONS opts, EventDAO trainingModel, EventDAO testModel) throws IOException {
        if (alreadyRecommended) {
            return null;
        }
        EventDAO dao = new EventCollectionDAO(Cursors.makeList(trainingModel.streamEvents()));
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);


        try {
            config.bind(ItemScorer.class).to((Class<? extends ItemScorer>) Class.forName(properties.getProperty(RecommendationRunner.recommender)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (properties.getProperty(RecommendationRunner.recommender).contains(".user.")) {
            config.bind(NeighborhoodFinder.class).to(SimpleNeighborhoodFinder.class);
            if (properties.getProperty(RecommendationRunner.neighborhood).equals("-1")) {
                properties.setProperty(RecommendationRunner.neighborhood, Math.round(Math.sqrt(new PrefetchingItemDAO(trainingModel).getItemIds().size())) + "");
            }
            config.set(NeighborhoodSize.class).to(Integer.parseInt(properties.getProperty(RecommendationRunner.neighborhood)));
        }
        if (properties.containsKey(RecommendationRunner.similarity)) {
            try {
                config.within(ItemSimilarity.class).bind(VectorSimilarity.class).to((Class<? extends VectorSimilarity>) Class.forName(properties.getProperty(RecommendationRunner.similarity)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (properties.containsKey(RecommendationRunner.factors)) {
            config.bind(BaselineScorer.class, ItemScorer.class).to(UserMeanItemScorer.class);
            config.bind(StoppingCondition.class).to(IterationCountStoppingCondition.class);
            config.set(IterationCount.class).to(DEFAULT_ITERATIONS);
            if (properties.getProperty(RecommendationRunner.factors).equals("-1")) {
                properties.setProperty(RecommendationRunner.factors, Math.round(Math.sqrt(new PrefetchingItemDAO(trainingModel).getItemIds().size())) + "");
            }
            config.set(FeatureCount.class).to(Integer.parseInt(properties.getProperty(RecommendationRunner.factors)));
        }

        UserDAO test = new PrefetchingUserDAO(testModel);
        Recommender rec = null;
        try {
            LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);
            rec = engine.createRecommender();
        } catch (RecommenderBuildException e) {
            logger.error(e.getMessage());
            System.out.println("RecommenderBuildException thrown");
            e.printStackTrace();
        }
        ItemRecommender irec = null;
        if (rec != null) {
            irec = rec.getItemRecommender();
        }
        assert irec != null;

        DataModel<Long, Long> model = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case RETURN_RECS:
                model = new DataModel();
        }
        String name = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case OUTPUT_RECS:
                name = fileName;
        }
        boolean createFile = true;
        for (long user : test.getUserIds()) {
            List<ScoredId> recs = irec.recommend(user);
            RecommenderIO.writeData(user, recs, path, name, !createFile, model);
            createFile = false;
        }
        return model;
    }
}
