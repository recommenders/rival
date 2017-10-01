/*
 * Copyright 2017 recommenders.net.
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
package net.recommenders.rival.recommend.frameworks.ranksys;

import es.uam.eps.ir.ranksys.core.Recommendation;
import es.uam.eps.ir.ranksys.core.preference.ConcatPreferenceData;
import es.uam.eps.ir.ranksys.core.preference.PreferenceData;
import es.uam.eps.ir.ranksys.core.preference.SimplePreferenceData;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastUserIndex;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.fast.preference.SimpleFastPreferenceData;
import es.uam.eps.ir.ranksys.mf.Factorization;
import es.uam.eps.ir.ranksys.mf.Factorizer;
import es.uam.eps.ir.ranksys.nn.item.neighborhood.ItemNeighborhood;
import es.uam.eps.ir.ranksys.nn.item.neighborhood.TopKItemNeighborhood;
import es.uam.eps.ir.ranksys.nn.item.sim.ItemSimilarity;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.TopKUserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.UserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.sim.UserSimilarity;
import es.uam.eps.ir.ranksys.rec.Recommender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleUnaryOperator;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;
import net.recommenders.rival.recommend.frameworks.AbstractRunner;
import net.recommenders.rival.recommend.frameworks.RecommendationRunner;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.index.ItemsReader;
import org.ranksys.formats.index.UsersReader;
import org.ranksys.formats.parsing.Parsers;
import org.ranksys.formats.preference.SimpleRatingPreferencesReader;

/**
 * A runner for RankSys-based recommenders.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class RanksysRecommenderRunner extends AbstractRunner<Long, Long> {

    public static final double DEFAULT_ALPHA = 1.0;
    public static final double DEFAULT_LAMBDA = 0.1;
    public static final int DEFAULT_Q = 1;

    /**
     * Default constructor.
     *
     * @param props the properties.
     */
    public RanksysRecommenderRunner(final Properties props) {
        super(props);
    }

    @Override
    public TemporalDataModelIF<Long, Long> run(final RUN_OPTIONS opts) throws RecommenderException, IOException {
        if (isAlreadyRecommended()) {
            return null;
        }

        String trainDataPath = getProperties().getProperty(RecommendationRunner.TRAINING_SET);
        String testDataPath = getProperties().getProperty(RecommendationRunner.TEST_SET);
        String userPath = getProperties().getProperty(RecommendationRunner.USER_INDEX);
        String itemPath = getProperties().getProperty(RecommendationRunner.ITEM_INDEX);
		
		PreferenceData<Long, Long> dataTotal = null;
		if ((userPath == null) ||  (itemPath == null)){
			PreferenceData<Long, Long> dataTrain = SimplePreferenceData.load(SimpleRatingPreferencesReader.get().read(trainDataPath, Parsers.lp, Parsers.lp));
			PreferenceData<Long, Long> dataTest = SimplePreferenceData.load(SimpleRatingPreferencesReader.get().read(testDataPath, Parsers.lp, Parsers.lp));
			dataTotal = new ConcatPreferenceData<>(dataTrain, dataTest);
		}

        FastUserIndex<Long> userIndex = null;
		if (userPath != null){
			userIndex = SimpleFastUserIndex.load(UsersReader.read(userPath, Parsers.lp));
		}else{
			userIndex = SimpleFastUserIndex.load(dataTotal.getAllUsers());
		}
        FastItemIndex<Long> itemIndex = null;
		if (itemPath != null){
			itemIndex = SimpleFastItemIndex.load(ItemsReader.read(itemPath, Parsers.lp));
		}else{
			itemIndex = SimpleFastItemIndex.load(dataTotal.getAllItems());
		}
        FastPreferenceData<Long, Long> trainData = SimpleFastPreferenceData.load(SimpleRatingPreferencesReader.get().read(trainDataPath, Parsers.lp, Parsers.lp), userIndex, itemIndex);
        FastPreferenceData<Long, Long> testData = SimpleFastPreferenceData.load(SimpleRatingPreferencesReader.get().read(testDataPath, Parsers.lp, Parsers.lp), userIndex, itemIndex);

        return runRanksysRecommender(opts, userIndex, itemIndex, trainData, testData);
    }

    @Override
    public TemporalDataModelIF<Long, Long> run(final RUN_OPTIONS opts,
            final net.recommenders.rival.core.TemporalDataModelIF<Long, Long> trainingModel,
            final net.recommenders.rival.core.TemporalDataModelIF<Long, Long> testModel)
            throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }
        // transform from core's DataModels to RankSys's PreferenceData
        FastUserIndex<Long> userIndex = new UserIndexWrapper(trainingModel, testModel);
        FastItemIndex<Long> itemIndex = new ItemIndexWrapper(trainingModel, testModel);

        FastPreferenceData<Long, Long> trainingModelRanksys = new PreferenceDataWrapper(trainingModel, userIndex, itemIndex);
        FastPreferenceData<Long, Long> testModelRanksys = new PreferenceDataWrapper(testModel, userIndex, itemIndex);

        return runRanksysRecommender(opts, userIndex, itemIndex, trainingModelRanksys, testModelRanksys);
    }

    public TemporalDataModelIF<Long, Long> runRanksysRecommender(final RUN_OPTIONS opts, final FastUserIndex<Long> userIndex, final FastItemIndex<Long> itemIndex, final FastPreferenceData<Long, Long> trainingModel, final FastPreferenceData<Long, Long> testModel)
            throws RecommenderException {
        if (isAlreadyRecommended()) {
            return null;
        }

        final int nItems = trainingModel.numItems();

        if (getProperties().containsKey(RecommendationRunner.NEIGHBORHOOD) && getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD).equals("-1")) {
            getProperties().setProperty(RecommendationRunner.NEIGHBORHOOD, Math.round(Math.sqrt(nItems)) + "");
        }
        if (getProperties().containsKey(RecommendationRunner.FACTORS) && getProperties().getProperty(RecommendationRunner.FACTORS).equals("-1")) {
            getProperties().setProperty(RecommendationRunner.FACTORS, Math.round(Math.sqrt(nItems)) + "");
        }

        Recommender<Long, Long> rec = null;
        if (getProperties().getProperty(RecommendationRunner.FACTORS) == null) {
            // knn, itempop, ...
            Class<?> recClass = null;
            try {
                recClass = Class.forName(getProperties().getProperty(RecommendationRunner.RECOMMENDER));
                if (recClass.getCanonicalName().contains("Neighborhood")) {
                    if (recClass.getCanonicalName().contains("User")) {
                        Class<?> sClass = Class.forName(getProperties().getProperty(RecommendationRunner.SIMILARITY));
                        UserSimilarity<Long> sim = (UserSimilarity<Long>) sClass.getConstructor(FastPreferenceData.class, double.class, boolean.class).newInstance(trainingModel, DEFAULT_ALPHA, true);
                        UserNeighborhood<Long> neigh = new TopKUserNeighborhood<>(sim, Integer.parseInt(getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD)));
                        rec = (Recommender) recClass.getConstructor(FastPreferenceData.class, UserNeighborhood.class, int.class).newInstance(trainingModel, neigh, DEFAULT_Q);
                    } else {
                        Class<?> sClass = Class.forName(getProperties().getProperty(RecommendationRunner.SIMILARITY));
                        ItemSimilarity<Long> sim = (ItemSimilarity<Long>) sClass.getConstructor(FastPreferenceData.class, double.class, boolean.class).newInstance(trainingModel, DEFAULT_ALPHA, true);
                        ItemNeighborhood<Long> neigh = new TopKItemNeighborhood<>(sim, Integer.parseInt(getProperties().getProperty(RecommendationRunner.NEIGHBORHOOD)));
                        rec = (Recommender) recClass.getConstructor(FastPreferenceData.class, ItemNeighborhood.class, int.class).newInstance(trainingModel, neigh, DEFAULT_Q);
                    }
                } else {
                    rec = (Recommender) recClass.getConstructor(FastPreferenceData.class).newInstance(trainingModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (getProperties().getProperty(RecommendationRunner.FACTORS) != null) {
            // mf
            Class<?> mfClass = null;
            try {
                mfClass = Class.forName(getProperties().getProperty(RecommendationRunner.RECOMMENDER));
                Class<?> fClass = Class.forName(getProperties().getProperty(RecommendationRunner.FACTORIZER));
                DoubleUnaryOperator confidence = x -> 1 + DEFAULT_ALPHA * x;
                Factorizer<Long, Long> f = (Factorizer) fClass.getConstructor(double.class, DoubleUnaryOperator.class, int.class).newInstance(DEFAULT_LAMBDA, confidence, Integer.parseInt(getProperties().getProperty(RecommendationRunner.ITERATIONS), DEFAULT_ITERATIONS));
                Factorization<Long, Long> factorizer = f.factorize(Integer.parseInt(getProperties().getProperty(RecommendationRunner.FACTORS)), trainingModel);
                rec = (Recommender) mfClass.getConstructor(FastUserIndex.class, FastItemIndex.class, Factorization.class).newInstance(userIndex, itemIndex, factorizer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TemporalDataModelIF<Long, Long> m = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case RETURN_RECS:
                m = new TemporalDataModel<>();
                break;
            default:
                m = null;
        }
        String n = null;
        switch (opts) {
            case RETURN_AND_OUTPUT_RECS:
            case OUTPUT_RECS:
                n = getFileName();
                break;
            default:
                n = null;
        }
        final AtomicBoolean createFile = new AtomicBoolean(true);
        final Recommender<Long, Long> recommender = rec;
        final String name = n;
        final TemporalDataModelIF<Long, Long> model = m;
        testModel.getUsersWithPreferences().forEach(u -> {
            Recommendation<Long, Long> items = recommender.getRecommendation(u, nItems);
            //
            List<RecommenderIO.Preference<Long, Long>> prefs = new ArrayList<>();
            for (Tuple2od<Long> i : items.getItems()) {
                prefs.add(new RecommenderIO.Preference<>(u, i.v1, i.v2));
            }
            //
            RecommenderIO.writeData(u, prefs, getPath(), name, !createFile.get(), model);
            createFile.set(false);
        }
        );
        return model;
    }
}
