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
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class PopularityBasedRecommender extends AbstractRecommender implements Recommender {

    /**
     * Constructor when a canidate item strategy is to be used.
     * @param dataModel the data model
     * @param candidateItemsStrategy    the strategy
     */
    public PopularityBasedRecommender(DataModel dataModel, CandidateItemsStrategy candidateItemsStrategy) {
        super(dataModel, candidateItemsStrategy);
    }

    /**
     * Default constructor.
     * @param dataModel the data model.
     */
    public PopularityBasedRecommender(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public float estimatePreference(long u, long i) throws TasteException {
        return new Float(getDataModel().getPreferencesForItem(i).length());
    }

    /**
     * Recommend items to a user.
     * @param userID    the user
     * @param howMany   how many items to recommend
     * @param rescorer  what rescorer to use
     * @return  the list of recommendations
     * @throws TasteException   if something in the recommender breaks.
     */
    public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(Collection<Refreshable> clctn) {
    }
}
