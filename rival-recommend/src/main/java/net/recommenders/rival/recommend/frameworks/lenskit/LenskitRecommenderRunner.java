/*
 * Copyright 2015 recommenders.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.recommenders.rival.recommend.frameworks.lenskit;

import java.io.File;
import java.util.List;
import java.util.Properties;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModelIF;
import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.UserMeanItemScorer;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.PrefetchingItemDAO;
import org.grouplens.lenskit.data.dao.PrefetchingUserDAO;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.text.Formats;
import org.grouplens.lenskit.data.text.TextEventDAO;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.IterationCountStoppingCondition;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.knn.user.NeighborFinder;
import org.grouplens.lenskit.knn.user.LiveNeighborFinder;
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
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LenskitRecommenderRunner.class);

    /**
     * Default constructor.
     *
     * @param props properties
     */
    public LenskitRecommenderRunner(final Properties props) {
        super(props);
    }

    /**
     * Runs the recommender.
     *
     * @param opts see
     * {@link net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS}
     * @return see
     * {@link #run(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS, net.recommenders.rival.core.TemporalDataModelIF, net.recommenders.rival.core.TemporalDataModelIF)}
     * @throws RecommenderException when the recommender is instantiated
     * incorrectly or breaks otherwise.
     */
    @Override
    @SuppressWarnings("unchecked")
    public DataModelIF<Long, Long> run(final RUN_OPTIONS opts) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }

        File trainingFile = new File(getProperties().getProperty(RecommendationRunner.TRAINING_SET));
        File testFile = new File(getProperties().getProperty(RecommendationRunner.TEST_SET));
        EventDAO base = new TextEventDAO(trainingFile, Formats.delimitedRatings("\t"));
        EventDAO test = new TextEventDAO(testFile, Formats.delimitedRatings("\t"));
        return runLenskitRecommender(opts, base, test);
    }

    /**
     * Runs the recommender using the provided datamodels.
     *
     * @param opts see
     * {@link net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS}
     * @param trainingModel model to be used to train the recommender.
     * @param testModel model to be used to test the recommender.
     * @return see
     * {@link #runLenskitRecommender(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS, org.grouplens.lenskit.data.dao.EventDAO, org.grouplens.lenskit.data.dao.EventDAO)}
     * @throws RecommenderException see
     * {@link #runLenskitRecommender(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS, org.grouplens.lenskit.data.dao.EventDAO, org.grouplens.lenskit.data.dao.EventDAO)}
     */
    @Override
    public DataModelIF<Long, Long> run(final RUN_OPTIONS opts, final TemporalDataModelIF<Long, Long> trainingModel, final TemporalDataModelIF<Long, Long> testModel) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }
        // transform from core's DataModels to Lenskit's EventDAO
        EventDAO trainingModelLensKit = new EventDAOWrapper(trainingModel);
        EventDAO testModelLensKit = new EventDAOWrapper(testModel);

        return runLenskitRecommender(opts, trainingModelLensKit, testModelLensKit);
    }

    /**
     * Runs a Lenskit recommender using the provided datamodels and the
     * previously provided properties.
     *
     * @param opts see
     * {@link net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS}
     * @param trainingModel model to be used to train the recommender.
     * @param testModel model to be used to test the recommender.
     * @return nothing when opts is
     * {@link net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS#OUTPUT_RECS},
     * otherwise, when opts is
     * {@link net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS#RETURN_RECS}
     * or
     * {@link net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS#RETURN_AND_OUTPUT_RECS}
     * it returns the predictions
     * @throws RecommenderException when recommender cannot be instantiated
     * properly
     */
    @SuppressWarnings("unchecked")
    public DataModelIF<Long, Long> runLenskitRecommender(final RUN_OPTIONS opts, final EventDAO trainingModel, final EventDAO testModel) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }
        EventDAO dao = EventCollectionDAO.create(Cursors.makeList(trainingModel.streamEvents()));
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);

        try {
            config.bind(ItemScorer.class).to((Class<? extends ItemScorer>) Class.forName(getProperties().getProperty(RecommendationRunner.RECOMMENDER)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RecommenderException("Problem with ItemScorer: " + e.getMessage());
        }
        if (getProperties().getProperty(RecommendationRunner.RECOMMENDER).contains(".user.")) {
            config.bind(NeighborFinder.class).to(LiveNeighborFinder.class);
            if (getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD).equals("-1")) {
                getProperties().setProperty(RecommendationRunner.NEIGHBORHOOD, Math.round(Math.sqrt(new PrefetchingItemDAO(trainingModel).getItemIds().size())) + "");
            }
            config.set(NeighborhoodSize.class).to(Integer.parseInt(getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD)));
        }
        if (getProperties().containsKey(RecommendationRunner.SIMILARITY)) {
            try {
                config.within(ItemSimilarity.class).
                        bind(VectorSimilarity.class).
                        to((Class<? extends VectorSimilarity>) Class.forName(getProperties().getProperty(RecommendationRunner.SIMILARITY)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RecommenderException("Problem with ItemSimilarity: " + e.getMessage());
            }
        }
        if (getProperties().containsKey(RecommendationRunner.FACTORS)) {
            config.bind(BaselineScorer.class, ItemScorer.class).to(UserMeanItemScorer.class);
            config.bind(StoppingCondition.class).to(IterationCountStoppingCondition.class);
            config.set(IterationCount.class).to(DEFAULT_ITERATIONS);
            if (getProperties().getProperty(RecommendationRunner.FACTORS).equals("-1")) {
                getProperties().setProperty(RecommendationRunner.FACTORS, Math.round(Math.sqrt(new PrefetchingItemDAO(trainingModel).getItemIds().size())) + "");
            }
            config.set(FeatureCount.class).to(Integer.parseInt(getProperties().getProperty(RecommendationRunner.FACTORS)));
        }

        UserDAO test = new PrefetchingUserDAO(testModel);
        Recommender rec = null;
        try {
            LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);
            rec = engine.createRecommender();
        } catch (RecommenderBuildException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            throw new RecommenderException("Problem with LenskitRecommenderEngine: " + e.getMessage());
        }
        ItemRecommender irec = null;
        if (rec != null) {
            irec = rec.getItemRecommender();
        }
        assert irec != null;

        DataModelIF<Long, Long> model = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case RETURN_RECS:
                model = DataModelFactory.getDefaultModel();
                break;
            default:
                model = null;
        }
        String name = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case OUTPUT_RECS:
                name = getFileName();
                break;
            default:
                name = null;
        }
        boolean createFile = true;
        for (long user : test.getUserIds()) {
            List<ScoredId> recs = irec.recommend(user);
            RecommenderIO.writeData(user, recs, getPath(), name, !createFile, model);
            createFile = false;
        }
        return model;
    }
}
