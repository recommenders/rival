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
package net.recommenders.rival.evaluation.strategy;

import java.util.Set;
import net.recommenders.rival.core.DataModelIF;

/**
 * An evaluation strategy where all the items are used as candidates.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class AllItems extends AbstractStrategy {

    /**
     * Default constructor.
     *
     * @see
     * AbstractStrategy#AbstractStrategy(net.recommenders.rival.core.DataModelIF,
     * net.recommenders.rival.core.DataModelIF, double)
     *
     * @param training The training set.
     * @param test The test set.
     * @param threshold The relevance threshold.
     */
    public AllItems(final DataModelIF<Long, Long> training, final DataModelIF<Long, Long> test, final double threshold) {
        super(training, test, threshold);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Long> getCandidateItemsToRank(final Long user) {
        final Set<Long> items = getModelTrainingDifference(getTraining(), user);
        items.addAll(getModelTrainingDifference(getTest(), user));
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AllItems_" + getThreshold();
    }
}
