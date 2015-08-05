package net.recommenders.rival.recommend.frameworks.mahout;

import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.RandomRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class GenericRecommenderBuilderTest {

    @Test
    public void testBuildDefaultRecommender() {

        RecommenderBuilder rb = new GenericRecommenderBuilder();
        FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>();
        userData.put(1, new GenericUserPreferenceArray(Arrays.asList(new GenericPreference(1, 1, 1),
                new GenericPreference(1, 2, 1), new GenericPreference(1, 3, 1))));
        userData.put(2, new GenericUserPreferenceArray(Arrays.asList(new GenericPreference(2, 1, 1),
                new GenericPreference(2, 2, 1), new GenericPreference(2, 4, 1))));
        DataModel dm = new GenericDataModel(userData);

        Recommender rec = null;
        try {
            rec = rb.buildRecommender(dm);
        } catch (TasteException e) {
            e.printStackTrace();
        }
        assertTrue(rec instanceof RandomRecommender);
    }

    @Test
    public void testBuildKNNRecommender() {
        GenericRecommenderBuilder rb = new GenericRecommenderBuilder();
        FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>();
        userData.put(1, new GenericUserPreferenceArray(Arrays.asList(new GenericPreference(1, 1, 1),
                new GenericPreference(1, 2, 1), new GenericPreference(1, 3, 1))));
        userData.put(2, new GenericUserPreferenceArray(Arrays.asList(new GenericPreference(2, 1, 1),
                new GenericPreference(2, 2, 1), new GenericPreference(2, 4, 1))));
        DataModel dm = new GenericDataModel(userData);

        Recommender rec = null;
        String recommenderType = "org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender";
        String similarityType = "org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity";
        try {
            rec = rb.buildRecommender(dm, recommenderType, similarityType);
        } catch (RecommenderException e) {
            e.printStackTrace();
        }
        assertTrue(rec instanceof GenericUserBasedRecommender);
    }
}
