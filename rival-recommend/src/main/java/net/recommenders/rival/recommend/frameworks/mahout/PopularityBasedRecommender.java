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
 * Basic popularity-based recommender.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class PopularityBasedRecommender extends AbstractRecommender implements Recommender {

    /**
     * Constructor when a canidate item strategy is to be used.
     *
     * @param dataModel the data model
     * @param candidateItemsStrategy the strategy
     */
    public PopularityBasedRecommender(final DataModel dataModel, final CandidateItemsStrategy candidateItemsStrategy) {
        super(dataModel, candidateItemsStrategy);
    }

    /**
     * Default constructor.
     *
     * @param dataModel the data model.
     */
    public PopularityBasedRecommender(final DataModel dataModel) {
        super(dataModel);
    }

    /**
     * Estimate the preference of @u of @i.
     *
     * @param u the user
     * @param i the item
     * @return the preference
     * @throws TasteException when the recommender cannot estimate the
     * preference.
     */
    @Override
    public float estimatePreference(final long u, final long i) throws TasteException {
        return 1.0f * getDataModel().getPreferencesForItem(i).length();
    }

    /**
     * Recommend items to a user.
     *
     * @param userID the user
     * @param howMany how many items to recommend
     * @param rescorer what rescorer to use
     * @return the list of recommendations
     * @throws TasteException if something in the recommender breaks.
     */
    public List<RecommendedItem> recommend(final long userID, final int howMany, final IDRescorer rescorer) throws TasteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Refresh the recommender.
     *
     * @param clctn the data.
     */
    @Override
    public void refresh(final Collection<Refreshable> clctn) {
    }
}
