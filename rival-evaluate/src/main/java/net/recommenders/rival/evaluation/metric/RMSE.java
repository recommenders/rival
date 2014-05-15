package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public class RMSE extends AbstractMetric {
    public RMSE(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        super(predictions, test);
    }

    private double rmse;
    private Map<Long, Double> perUserRMSE = new HashMap<Long, Double>();
    public double computeRMSE(){
        Map<Long, Map<Long, Double>> actualRatings = test.getUserItemPreferences();
        Map<Long, Map<Long, Double>> predictedRatings = predictions.getUserItemPreferences();
        int testItems = 0;
         rmse = 0.0;
        int emptyUsers = 0; // for coverage
        int emptyItems = 0; // for coverage

        for(long testUser : test.getUsers()){
            double difference = 0.0;
            Map<Long, Double> ratings = actualRatings.get(testUser);
            testItems += ratings.size();
            for (long testItem : ratings.keySet()){
                double realRating = ratings.get(testItem);
                double predictedRating = 0.0;
                if(actualRatings.containsKey(testUser)){
                    if(actualRatings.get(testUser).containsKey(testItem))
                        predictedRating = predictedRatings.get(testUser).get(testItem);
                    else {
                        emptyItems++;
                        continue;
                    }
                }
                else {
                    emptyUsers++;
                    continue;
                }
                difference = realRating - predictedRating;
                rmse += difference * difference;
                perUserRMSE.put(testUser, Math.sqrt((difference * difference) / ratings.size()));

            }
        }
        rmse = Math.sqrt(rmse / testItems);
        return rmse;
    }

    @Override
    public double getValue() {
        return rmse;
    }

    @Override
    public Map getValuePerUser() {
        return perUserRMSE;
    }
}
