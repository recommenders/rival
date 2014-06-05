package net.recommenders.rival.evaluation.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class StrategyIO {

    /**
     * Read a file from the recommended items file.
     *
     * @param line The line.
     * @param mapUserRecommendations The recommendations for the users.
     */
    public static void readLine(String line, Map<Long, List<EvaluationStrategy.Pair<Long, Double>>> mapUserRecommendations) {
        String[] toks = line.split("\t");
        // mymedialite format: user \t [item:score,item:score,...]
        if (line.contains(":") && line.contains(",")) {
            Long user = Long.parseLong(toks[0]);
            String items = toks[1].replace("[", "").replace("]", "");
            for (String pair : items.split(",")) {
                String[] pairToks = pair.split(":");
                Long item = Long.parseLong(pairToks[0]);
                Double score = Double.parseDouble(pairToks[1]);
                List<EvaluationStrategy.Pair<Long, Double>> userRec = mapUserRecommendations.get(user);
                if (userRec == null) {
                    userRec = new ArrayList<EvaluationStrategy.Pair<Long, Double>>();
                    mapUserRecommendations.put(user, userRec);
                }
                userRec.add(new EvaluationStrategy.Pair<Long, Double>(item, score));
            }
        } else {
            Long user = Long.parseLong(toks[0]);
            Long item = Long.parseLong(toks[1]);
            Double score = Double.parseDouble(toks[2]);
            List<EvaluationStrategy.Pair<Long, Double>> userRec = mapUserRecommendations.get(user);
            if (userRec == null) {
                userRec = new ArrayList<EvaluationStrategy.Pair<Long, Double>>();
                mapUserRecommendations.put(user, userRec);
            }
            userRec.add(new EvaluationStrategy.Pair<Long, Double>(item, score));
        }
    }
}
