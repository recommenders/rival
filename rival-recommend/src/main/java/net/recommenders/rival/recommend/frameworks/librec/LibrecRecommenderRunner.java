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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import librec.data.DataDAO;
import librec.data.SparseMatrix;
import librec.intf.GraphicRecommender;
import librec.intf.IterativeRecommender;
import librec.intf.Recommender;
import librec.util.FileConfiger;
import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModelIF;
import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
    public DataModelIF<Long, Long> run(final RUN_OPTIONS opts) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }

        File trainingFile = new File(getProperties().getProperty(RecommendationRunner.TRAINING_SET));
        File testFile = new File(getProperties().getProperty(RecommendationRunner.TEST_SET));

        DataDAO training = new DataDAO(trainingFile.getAbsolutePath());
        DataDAO test = new DataDAO(testFile.getAbsolutePath());
        SparseMatrix[] data = null;
        try {
            data = training.readData();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        SparseMatrix[] testData = null;
        try {
            testData = test.readData();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }

        return runLibrecRecommender(opts, training, data[0], testData[0]);
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
    public DataModelIF<Long, Long> run(RUN_OPTIONS opts, TemporalDataModelIF<Long, Long> trainingModel, TemporalDataModelIF<Long, Long> testModel) throws Exception {
        if (isAlreadyRecommended()) {
            return null;
        }
        // transform from core's DataModels to LibRec's DataDAO and SparseMatrix
        DataDAO trainingModelLibrec = new DataDAOWrapper(trainingModel);
        DataDAO testModelLibrec = new DataDAOWrapper(testModel);
        SparseMatrix[] data = null;
        try {
            data = trainingModelLibrec.readData();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        SparseMatrix[] testData = null;
        try {
            testData = testModelLibrec.readData();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }

        return runLibrecRecommender(opts, trainingModelLibrec, data[0], testData[0]);
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
    public DataModelIF<Long, Long> runLibrecRecommender(final RUN_OPTIONS opts, final DataDAO rateDAO, final SparseMatrix trainingModel, final SparseMatrix testModel) throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }

        String recommenderProperty = getProperties().getProperty(RecommendationRunner.RECOMMENDER);
        FileConfiger cf = null;
        try {
            cf = new MyFileConfiger(getProperties());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }

        // preset from LibRec.java
        Recommender.cf = cf;
        Recommender.resetStatics = true;
        IterativeRecommender.resetStatics = true;
        GraphicRecommender.resetStatics = true;
        // end
        // readData from LibRec.java
        Recommender.rateMatrix = trainingModel;
        Recommender.rateDao = rateDAO;
        // end
        Recommender rec = null;
        Class<?> recClass = null;
        try {
            recClass = Class.forName(recommenderProperty);
            rec = (Recommender) recClass.getConstructor(SparseMatrix.class, SparseMatrix.class, int.class).newInstance(trainingModel, testModel, -1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RecommenderException("Could not create Similarity class " + e.getMessage());
        }

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

        if (rec == null) {
            return model;
        } else {
            try {
                // train the model
                rec.execute();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
        }

        boolean createFile = true;
        // rows: users
        // columns: items
        for (Integer user : testModel.rows()) {
            // LibRec does not have a method to return a ranking!
            // ranking creation
            List<Pair<Integer, Double>> recs = new ArrayList<>();
            Set<Integer> trainingItems = new HashSet<>(trainingModel.getColumns(user));
            for (Integer item : trainingModel.columns()) {
                // ignore if item belongs to the training profile of user
                if (trainingItems.contains(item)) {
                    continue;
                }
                double d = Double.NaN;
                try {
                    d = rec.predict(user, item);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                }
                recs.add(new ImmutablePair<>(item, d));
            }
            // sort the list to have items with larger scores at the beginning
            Collections.sort(recs, new Comparator<Pair<Integer, Double>>() {
                @Override
                public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
                    return o2.getRight().compareTo(o1.getRight());
                }
            });
            // end of ranking creation
            List<RecommenderIO.Preference<Long, Long>> prefs = new ArrayList<>();
            for (Pair<Integer, Double> i : recs) {
                prefs.add(new RecommenderIO.Preference<>(user.longValue(), i.getKey().longValue(), i.getValue()));
            }
            //
            RecommenderIO.writeData(user, prefs, getPath(), name, !createFile, model);
            createFile = false;
        }
        return model;
    }

    private static class MyFileConfiger extends FileConfiger {

        private Properties props;

        public MyFileConfiger(Properties p) throws Exception {
            super(".");
            props = p;
        }

        @Override
        public String getString(String key, String val) {
            return props.getProperty(key, val);
        }

        @Override
        public String getString(String key) {
            return props.getProperty(key);
        }
    }
}
