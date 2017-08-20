/*
 * Copyright 2016 recommenders.net.
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
package net.recommenders.rival.recommend.frameworks.librec;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.conf.Configured;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.item.RecommendedItem;
import net.librec.similarity.RecommenderSimilarity;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;
import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class LibrecRecommenderRunner extends AbstractRunner<Long, Long> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LibrecRecommenderRunner.class);

    /**
     * Default constructor.
     *
     * @param props properties
     */
    public LibrecRecommenderRunner(final Properties props) {
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

        try {
            Configuration confTraining = new Configuration();
            confTraining.set(Configured.CONF_DATA_INPUT_PATH, trainingFile.getAbsolutePath());
            confTraining.set(Configured.CONF_DATA_COLUMN_FORMAT, "UIR");
            confTraining.set("data.model.splitter", "ratio");
            confTraining.set("data.splitter.trainset.ratio", "0.999");
            confTraining.set("data.splitter.ratio", "rating");
            DataModel training = new TextDataModel(confTraining);
            training.buildDataModel();

            Configuration confTest = new Configuration();
            confTest.set(Configured.CONF_DATA_INPUT_PATH, testFile.getAbsolutePath());
            confTest.set(Configured.CONF_DATA_COLUMN_FORMAT, "UIR");
            confTest.set("data.model.splitter", "ratio");
            confTest.set("data.splitter.trainset.ratio", "0.999");
            confTest.set("data.splitter.ratio", "rating");
            DataModel test = new TextDataModel(confTest);
            test.buildDataModel();

            return runLibrecRecommender(opts, training, test);
        } catch (LibrecException e) {
            e.printStackTrace();
            throw new RecommenderException(e.getMessage());
        }
    }

    /**
     * Runs the recommender using the provided datamodels.
     *
     * @param opts see
     * {@link net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS}
     * @param trainingModel model to be used to train the recommender.
     * @param testModel model to be used to test the recommender.
     * @return see
     * {@link #runLibrecRecommender(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS, org.grouplens.lenskit.data.dao.EventDAO, org.grouplens.lenskit.data.dao.EventDAO)}
     * @throws RecommenderException see
     * {@link #runLibrecRecommender(net.recommenders.rival.recommend.frameworks.AbstractRunner.RUN_OPTIONS, org.grouplens.lenskit.data.dao.EventDAO, org.grouplens.lenskit.data.dao.EventDAO)}
     */
    @Override
    public TemporalDataModelIF<Long, Long> run(RUN_OPTIONS opts, TemporalDataModelIF<Long, Long> trainingModel, TemporalDataModelIF<Long, Long> testModel) throws Exception {
        if (isAlreadyRecommended()) {
            return null;
        }
        // transform from core's DataModels to LibRec's DataModel
        DataModel trainingModelLibrec = new DataDAOWrapper(trainingModel);
        DataModel testModelLibrec = new DataDAOWrapper(testModel);

        return runLibrecRecommender(opts, trainingModelLibrec, testModelLibrec);
    }

    /**
     * Runs a Librec recommender using the provided datamodels and the
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
    public TemporalDataModelIF<Long, Long> runLibrecRecommender(final RUN_OPTIONS opts, final DataModel trainingModel, final DataModel testModel) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }

        Configuration conf = new Configuration();
        RecommenderContext rc = new RecommenderContext(conf, trainingModel);

        int nItems = trainingModel.getItemMappingData().size();
        String recommenderProperty = getProperties().getProperty(RecommendationRunner.RECOMMENDER);
        if (getProperties().containsKey(RecommendationRunner.NEIGHBORHOOD) && getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD).equals("-1")) {
            getProperties().setProperty(RecommendationRunner.NEIGHBORHOOD, Math.round(Math.sqrt(nItems)) + "");
        }
        if (getProperties().containsKey(RecommendationRunner.FACTORS) && getProperties().getProperty(RecommendationRunner.FACTORS).equals("-1")) {
            getProperties().setProperty(RecommendationRunner.FACTORS, Math.round(Math.sqrt(nItems)) + "");
        }
        if (getProperties().containsKey(RecommendationRunner.SIMILARITY)) {
            String similarityType = getProperties().getProperty(RecommendationRunner.SIMILARITY);
            Class<?> similarityClass = null;
            try {
                similarityClass = Class.forName(similarityType);
                RecommenderSimilarity similarity = (RecommenderSimilarity) similarityClass.getConstructor().newInstance();
                similarity.buildSimilarityMatrix(trainingModel);
                rc.setSimilarity(similarity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            conf.set("rec.neighbors.knn.number", getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD));
        }
        // TODO: more recs

        Recommender rec = null;
        Class<?> recClass = null;
        try {
            recClass = Class.forName(recommenderProperty);
            rec = (Recommender) recClass.getConstructor().newInstance();
            rec.setContext(rc);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RecommenderException("Could not create Similarity class " + e.getMessage());
        }

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

        if (rec == null) {
            return model;
        } else {
            try {
                // train the model
                rec.recommend(rc);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
        }

        List<RecommendedItem> recommendedItemList = rec.getRecommendedList();
        boolean createFile = true;
        // rows: users
        // columns: items
        for (Map.Entry<String, Integer> e : testModel.getUserMappingData().entrySet()) {
            Long user = Long.parseLong(e.getKey());
            int u = e.getValue();
            Set<String> notInTrainingItems = new HashSet<>();
            for (Map.Entry<String, Integer> e2 : trainingModel.getItemMappingData().entrySet()) {
                String item = e2.getKey();
                int i = e2.getValue();
                // ignore if item belongs to the training profile of user
                if (notInTrainingItems.contains(item)) {
                    continue;
                }
                if (!trainingModel.getDataSplitter().getTrainData().contains(u, i)) {
                    notInTrainingItems.add(item);
                }
            }
            // 
            List<String> userIdList = new ArrayList<>();
            userIdList.add(e.getKey());
            List<String> itemIdList = new ArrayList<>(notInTrainingItems);
            // filter the recommended result
            //   The GenericRecommendedFilter instance returns the recommendedList 
            //   that contains the recommendedItem with only the specific userId or itemId
            GenericRecommendedFilter filter = new GenericRecommendedFilter();
            filter.setUserIdList(userIdList);
            filter.setItemIdList(itemIdList);
            List<RecommendedItem> recs = filter.filter(recommendedItemList);

            // end of ranking creation
            List<RecommenderIO.Preference<Long, Long>> prefs = new ArrayList<>();
            for (RecommendedItem recommendedItem : recs) {
                prefs.add(new RecommenderIO.Preference<>(user, Long.parseLong(recommendedItem.getItemId()), recommendedItem.getValue()));
            }
            //
            RecommenderIO.writeData(user, prefs, getPath(), name, !createFile, model);
            createFile = false;
        }
        return model;
    }
}
