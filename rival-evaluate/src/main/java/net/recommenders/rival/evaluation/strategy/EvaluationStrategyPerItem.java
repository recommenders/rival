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

/**
 * An interface for the per-item evaluation strategy.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <U> generic type for users
 * @param <I> generic type for items
 */
public interface EvaluationStrategyPerItem<U, I> {

    /**
     * Get the items to rank.
     *
     * @param user The user.
     * @param item The item.
     * @return The items to rank.
     */
    Set<I> getCandidateItemsToRank(U user, I item);
}
