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

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import net.recommenders.rival.evaluation.Pair;

/**
 * An interface for evaluation strategies.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <U> generic type for users
 * @param <I> generic type for items
 */
public interface EvaluationStrategy<U, I> {

    /**
     * Enumeration that defines two output formats: a simple one (tab-separated)
     * and another compatible with the one used by the treceval program.
     */
    public enum OUTPUT_FORMAT {

        /**
         * Tab-separated format.
         */
        SIMPLE,
        /**
         * Format as followed by the trec_eval program.
         */
        TRECEVAL;
    }

    /**
     * Get the items to rank.
     *
     * @param user The user.
     * @return The items to rank.
     */
    Set<I> getCandidateItemsToRank(U user);

    /**
     * Print rankings for a user.
     *
     * @param user The user.
     * @param scoredItems The scored items to print.
     * @param out Where to print.
     * @param format The format of the printer (see {@link OUTPUT_FORMAT}).
     */
    void printRanking(U user, List<Pair<I, Double>> scoredItems, PrintStream out, OUTPUT_FORMAT format);

    /**
     * Print the ground truth.
     *
     * @param user The user.
     * @param out Where to print.
     * @param format The format of the printer (see {@link OUTPUT_FORMAT}).
     */
    void printGroundtruth(U user, PrintStream out, OUTPUT_FORMAT format);
}
