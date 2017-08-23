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
package net.recommenders.rival.recommend.frameworks.mahout;

import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;

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
     * Default constructor.
     *
     * @param props the properties.
     */
    public MahoutRecommenderRunner(final Properties props) {
        super(props);
    }

    /**
     * Runs the recommender using models from file.
     *
     * @param opts see
     * {@link net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS}
     * @return see
     * {@link #runMahoutRecommender(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS, org.apache.mahout.cf.taste.model.DataModel, org.apache.mahout.cf.taste.model.DataModel)}
     *
     * @throws RecommenderException when the recommender is instantiated
     * incorrectly.
     * @throws IOException when paths in property object are incorrect..
     * @throws TasteException when the recommender is instantiated incorrectly
     * or breaks otherwise.
     */
    @Override
    public TemporalDataModelIF<Long, Long> run(final RUN_OPTIONS opts) throws RecommenderException, TasteException, IOException {
        if (isAlreadyRecommended()) {
            return null;
        }
        DataModel trainingModel = new FileDataModel(new File(getProperties().getProperty(RecommendationRunner.TRAINING_SET)));
        DataModel testModel = new FileDataModel(new File(getProperties().getProperty(RecommendationRunner.TEST_SET)));
        return runMahoutRecommender(opts, trainingModel, testModel);
    }

    /**
     * Runs the recommender using the provided datamodels.
     *
     * @param opts see
     * {@link net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS}
     * @param trainingModel model to be used to train the recommender.
     * @param testModel model to be used to test the recommender.
     * @return see
     * {@link #runMahoutRecommender(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS, org.apache.mahout.cf.taste.model.TemporalDataModelIF, org.apache.mahout.cf.taste.model.TemporalDataModelIF)}
     * @throws RecommenderException see null     {@link #runMahoutRecommender(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS,
     * org.apache.mahout.cf.taste.model.DataModel, org.apache.mahout.cf.taste.model.DataModel)}
     * @throws TasteException see null     {@link #runMahoutRecommender(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS,
     * org.apache.mahout.cf.taste.model.DataModel, org.apache.mahout.cf.taste.model.DataModel)}
     */
    @Override
    public TemporalDataModelIF<Long, Long> run(final RUN_OPTIONS opts,
            final net.recommenders.rival.core.TemporalDataModelIF<Long, Long> trainingModel,
            final net.recommenders.rival.core.TemporalDataModelIF<Long, Long> testModel)
            throws RecommenderException, TasteException {
        if (isAlreadyRecommended()) {
            return null;
        }
        // transform from core's DataModels to Mahout's DataModels
        DataModel trainingModelMahout = new DataModelWrapper(trainingModel);
        DataModel testModelMahout = new DataModelWrapper(testModel);

        return runMahoutRecommender(opts, trainingModelMahout, testModelMahout);
    }

    /**
     * Runs a Mahout recommender using the provided datamodels and the
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
     * @throws TasteException when there is a problem with the Mahout
     * recommender
     * @throws RecommenderException when recommender cannot be instantiated
     * properly
     */
    public TemporalDataModelIF<Long, Long> runMahoutRecommender(final RUN_OPTIONS opts, final DataModel trainingModel, final DataModel testModel)
            throws RecommenderException, TasteException {
        if (isAlreadyRecommended()) {
            return null;
        }

        GenericRecommenderBuilder grb = new GenericRecommenderBuilder();

        if (getProperties().containsKey(RecommendationRunner.NEIGHBORHOOD) && getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD).equals("-1")) {
            getProperties().setProperty(RecommendationRunner.NEIGHBORHOOD, Math.round(Math.sqrt(trainingModel.getNumItems())) + "");
        }
        if (getProperties().containsKey(RecommendationRunner.FACTORS) && getProperties().getProperty(RecommendationRunner.FACTORS).equals("-1")) {
            getProperties().setProperty(RecommendationRunner.FACTORS, Math.round(Math.sqrt(trainingModel.getNumItems())) + "");
        }

        Recommender recommender = null;
        if (getProperties().getProperty(RecommendationRunner.FACTORS) == null) {
            recommender = grb.buildRecommender(
                    trainingModel,
                    getProperties().getProperty(RecommendationRunner.RECOMMENDER),
                    getProperties().getProperty(RecommendationRunner.SIMILARITY),
                    Integer.parseInt(getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD)));
        }
        if (getProperties().getProperty(RecommendationRunner.FACTORS) != null) {
            recommender = grb.buildRecommender(
                    trainingModel,
                    getProperties().getProperty(RecommendationRunner.RECOMMENDER),
                    getProperties().getProperty(RecommendationRunner.FACTORIZER),
                    DEFAULT_ITERATIONS,
                    Integer.parseInt(getProperties().getProperty(RecommendationRunner.FACTORS)));
        }

        LongPrimitiveIterator users = testModel.getUserIDs();

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
        while (users.hasNext()) {
            long u = users.nextLong();
            try {
                List<RecommendedItem> items = recommender.recommend(u, trainingModel.getNumItems());
                //
                List<RecommenderIO.Preference<Long, Long>> prefs = new ArrayList<>();
                for (RecommendedItem i : items) {
                    prefs.add(new RecommenderIO.Preference<>(u, i.getItemID(), i.getValue()));
                }
                //
                RecommenderIO.writeData(u, prefs, getPath(), name, !createFile, model);
                createFile = false;
            } catch (TasteException e) {
                e.printStackTrace();
            }
        }
        return model;
    }
}
