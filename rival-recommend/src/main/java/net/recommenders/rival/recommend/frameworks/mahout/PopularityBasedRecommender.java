/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.rival.recommend.frameworks.mahout;

import java.util.Collection;
import java.util.List;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 *
 * @author alejandr
 */
public class PopularityBasedRecommender extends AbstractRecommender implements Recommender {

    public PopularityBasedRecommender(DataModel dataModel, CandidateItemsStrategy candidateItemsStrategy) {
        super(dataModel, candidateItemsStrategy);
    }

    public PopularityBasedRecommender(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public float estimatePreference(long u, long i) throws TasteException {
        return new Float(getDataModel().getPreferencesForItem(i).length());
    }

    public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(Collection<Refreshable> clctn) {
    }
}
