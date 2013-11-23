/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.recommenders.evaluation.core.DataModel;

/**
 *
 * @author alejandr
 */
public class RandomSplitter implements Splitter<Long, Long> {

    private float percentageTraining;
    private boolean perUser;
    private Random rnd;

    public RandomSplitter(float percentageTraining, boolean perUser, long seed) {
        this.percentageTraining = percentageTraining;
        this.perUser = perUser;

        rnd = new Random(seed);
    }

    public DataModel<Long, Long>[] split(DataModel<Long, Long> data) {
        final DataModel<Long, Long>[] splits = new DataModel[2];
        splits[0] = new DataModel<Long, Long>(); // training
        splits[1] = new DataModel<Long, Long>(); // test
        if (perUser) {
            for (Long user : data.getUsers()) {
                List<Long> items = new ArrayList<Long>(data.getUserItemPreferences().get(user).keySet());
                Collections.shuffle(items, rnd);
                int splitPoint = Math.round(percentageTraining * items.size());
                for (int i = 0; i < items.size(); i++) {
                    Long item = items.get(i);
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    Set<Long> time = null;
                    if (data.getUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
                        time = data.getUserItemTimestamps().get(user).get(item);
                    }
                    DataModel<Long, Long> datamodel = splits[0]; // training
                    if (i > splitPoint) {
                        datamodel = splits[1]; // test
                    }
                    if (pref != null) {
                        datamodel.addPreference(user, item, pref);
                    }
                    if (time != null) {
                        for (Long t : time) {
                            datamodel.addTimestamp(user, item, t);
                        }
                    }
                }
            }
        } else {
            for (Long user : data.getUsers()) {
                for (Long item : data.getUserItemPreferences().get(user).keySet()) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    Set<Long> time = null;
                    if (data.getUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
                        time = data.getUserItemTimestamps().get(user).get(item);
                    }
                    DataModel<Long, Long> datamodel = splits[0]; // training
                    if (rnd.nextDouble() > percentageTraining) {
                        datamodel = splits[1]; // test
                    }
                    if (pref != null) {
                        datamodel.addPreference(user, item, pref);
                    }
                    if (time != null) {
                        for (Long t : time) {
                            datamodel.addTimestamp(user, item, t);
                        }
                    }
                }
            }
        }
        return splits;
    }
}
