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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.recommenders.rival.evaluation.Pair;

/**
 * Methods related to input/output of strategies.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public final class StrategyIO {

    /**
     * Utility classes should not have a public or default constructor.
     */
    private StrategyIO() {
    }

    /**
     * Read a file from the recommended items file.
     *
     * @param line The line.
     * @param mapUserRecommendations The recommendations for the users where
     * information will be stored into.
     */
    public static void readLine(final String line, final Map<Long, List<Pair<Long, Double>>> mapUserRecommendations) {
        String[] toks = line.split("\t");
        // mymedialite format: user \t [item:score,item:score,...]
        if (line.contains(":") && line.contains(",")) {
            Long user = Long.parseLong(toks[0]);
            String items = toks[1].replace("[", "").replace("]", "");
            for (String pair : items.split(",")) {
                String[] pairToks = pair.split(":");
                Long item = Long.parseLong(pairToks[0]);
                Double score = Double.parseDouble(pairToks[1]);
                List<Pair<Long, Double>> userRec = mapUserRecommendations.get(user);
                if (userRec == null) {
                    userRec = new ArrayList<Pair<Long, Double>>();
                    mapUserRecommendations.put(user, userRec);
                }
                userRec.add(new Pair<Long, Double>(item, score));
            }
        } else {
            Long user = Long.parseLong(toks[0]);
            Long item = Long.parseLong(toks[1]);
            Double score = Double.parseDouble(toks[2]);
            List<Pair<Long, Double>> userRec = mapUserRecommendations.get(user);
            if (userRec == null) {
                userRec = new ArrayList<Pair<Long, Double>>();
                mapUserRecommendations.put(user, userRec);
            }
            userRec.add(new Pair<Long, Double>(item, score));
        }
    }
}
