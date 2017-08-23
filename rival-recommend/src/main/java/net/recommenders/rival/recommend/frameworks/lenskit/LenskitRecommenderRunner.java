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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;
import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.IterationCountStoppingCondition;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.api.Result;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.baseline.UserMeanItemScorer;
import org.lenskit.bias.BiasModel;
import org.lenskit.bias.UserItemBiasModel;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.dao.file.TextEntitySource;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.data.ratings.StandardRatingVectorPDAO;
import org.lenskit.knn.NeighborhoodSize;
import org.lenskit.knn.user.LiveNeighborFinder;
import org.lenskit.knn.user.NeighborFinder;
import org.lenskit.mf.funksvd.FeatureCount;
import org.lenskit.similarity.VectorSimilarity;
import org.lenskit.util.IdBox;
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
    public TemporalDataModelIF<Long, Long> run(final RUN_OPTIONS opts) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }

        File trainingFile = new File(getProperties().getProperty(RecommendationRunner.TRAINING_SET));
        File testFile = new File(getProperties().getProperty(RecommendationRunner.TEST_SET));
//        EventDAO base = new TextEventDAO(trainingFile, Formats.delimitedRatings("\t"));
        TextEntitySource tesTraining = new TextEntitySource();
        tesTraining.setFile(trainingFile.toPath());
        tesTraining.setFormat(org.lenskit.data.dao.file.Formats.delimitedRatings("\t"));
        StaticDataSource sourceTraining = new StaticDataSource("training");
        sourceTraining.addSource(tesTraining);
        DataAccessObject base = sourceTraining.get();
//        EventDAO test = new TextEventDAO(testFile, Formats.delimitedRatings("\t"));
        TextEntitySource tesTest = new TextEntitySource();
        tesTest.setFile(testFile.toPath());
        tesTest.setFormat(org.lenskit.data.dao.file.Formats.delimitedRatings("\t"));
        StaticDataSource sourceTest = new StaticDataSource("test");
        sourceTest.addSource(tesTest);
        DataAccessObject test = sourceTest.get();
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
    public TemporalDataModelIF<Long, Long> run(final RUN_OPTIONS opts, final TemporalDataModelIF<Long, Long> trainingModel, final TemporalDataModelIF<Long, Long> testModel) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }
        // transform from core's DataModels to Lenskit's EventDAO
        DataAccessObject trainingModelLensKit = new EventDAOWrapper(trainingModel);
        DataAccessObject testModelLensKit = new EventDAOWrapper(testModel);

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
    public TemporalDataModelIF<Long, Long> runLenskitRecommender(final RUN_OPTIONS opts, final DataAccessObject trainingModel, final DataAccessObject testModel) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }
        LenskitConfiguration config = new LenskitConfiguration();
//        int nItems = new PrefetchingItemDAO(trainingModel).getItemIds().size();
        LongSet items = RatingSummary.create(trainingModel).getItems();
        int nItems = items.size();

        try {
            config.bind(ItemScorer.class).to((Class<? extends ItemScorer>) Class.forName(getProperties().getProperty(RecommendationRunner.RECOMMENDER)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RecommenderException("Problem with ItemScorer: " + e.getMessage());
        }
        if (getProperties().getProperty(RecommendationRunner.RECOMMENDER).contains(".user.")) {
            config.bind(NeighborFinder.class).to(LiveNeighborFinder.class);
            if (getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD).equals("-1")) {
                getProperties().setProperty(RecommendationRunner.NEIGHBORHOOD, Math.round(Math.sqrt(nItems)) + "");
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
            config.bind(BiasModel.class).to(UserItemBiasModel.class);
            config.set(IterationCount.class).to(DEFAULT_ITERATIONS);
            if (getProperties().getProperty(RecommendationRunner.FACTORS).equals("-1")) {
                getProperties().setProperty(RecommendationRunner.FACTORS, Math.round(Math.sqrt(nItems)) + "");
            }
            config.set(FeatureCount.class).to(Integer.parseInt(getProperties().getProperty(RecommendationRunner.FACTORS)));
        }

        RatingVectorPDAO test = new StandardRatingVectorPDAO(testModel);
        LenskitRecommender rec = null;
        try {
            LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, trainingModel);
            rec = engine.createRecommender(trainingModel);
        } catch (RecommenderBuildException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            throw new RecommenderException("Problem with LenskitRecommenderEngine: " + e.getMessage());
        }
        ItemRecommender irec = null;
        ItemScorer iscore = null;
        if (rec != null) {
            irec = rec.getItemRecommender();
            iscore = rec.getItemScorer();
        }
        assert irec != null;
        assert iscore != null;

        TemporalDataModelIF<Long, Long> model = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case RETURN_RECS:
                model = new TemporalDataModel<>();
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
        for (IdBox<Long2DoubleMap> u : test.streamUsers()) {
            long user = u.getId();
            // The following does not return anything
            // List<Long> recItems = irec.recommend(user, nItems);
            //
            List<RecommenderIO.Preference<Long, Long>> prefs = new ArrayList<>();
            Map<Long, Double> results = iscore.score(user, items);
            for (Long i : items) {
//                Result r = iscore.score(user, i);
//                if (r != null) {
                if (results.containsKey(i)) {
//                    Double s = r.getScore();
                    Double s = results.get(i);
                    prefs.add(new RecommenderIO.Preference<>(user, i, s));
                }
            }
            //
            RecommenderIO.writeData(user, prefs, getPath(), name, !createFile, model);
            createFile = false;
        }
        rec.close();
        return model;
    }
}
