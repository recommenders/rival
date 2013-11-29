package net.recommenders.rival.split.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Alejandro
 */
public class CrossValidationSplitter implements Splitter<Long, Long> {

    private int nFolds;
    private boolean perUser;
    private Random rnd;

    public CrossValidationSplitter(int nFolds, boolean perUser, long seed) {
        this.nFolds = nFolds;
        this.perUser = perUser;

        rnd = new Random(seed);
    }

    public DataModel<Long, Long>[] split(DataModel<Long, Long> data) {
        final DataModel<Long, Long>[] splits = new DataModel[2 * nFolds];
        for (int i = 0; i < nFolds; i++) {
            splits[2 * i] = new DataModel<Long, Long>(); // training
            splits[2 * i + 1] = new DataModel<Long, Long>(); // test
        }
        if (perUser) {
            int n = 0;
            for (Long user : data.getUsers()) {
                List<Long> items = new ArrayList<Long>(data.getUserItemPreferences().get(user).keySet());
                Collections.shuffle(items, rnd);
                for (Long item : items) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    Set<Long> time = null;
                    if (data.getUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
                        time = data.getUserItemTimestamps().get(user).get(item);
                    }
                    int curFold = n % nFolds;
                    for (int i = 0; i < nFolds; i++) {
                        DataModel<Long, Long> datamodel = splits[2 * i]; // training
                        if (i == curFold) {
                            datamodel = splits[2 * i + 1]; // test
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
                    n++;
                }
            }
        } else {
            List<Long> users = new ArrayList<Long>(data.getUsers());
            Collections.shuffle(users, rnd);
            int n = 0;
            for (Long user : users) {
                List<Long> items = new ArrayList<Long>(data.getUserItemPreferences().get(user).keySet());
                Collections.shuffle(items, rnd);
                for (Long item : items) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    Set<Long> time = null;
                    if (data.getUserItemTimestamps().containsKey(user) && data.getUserItemTimestamps().get(user).containsKey(item)) {
                        time = data.getUserItemTimestamps().get(user).get(item);
                    }
                    int curFold = n % nFolds;
                    for (int i = 0; i < nFolds; i++) {
                        DataModel<Long, Long> datamodel = splits[2 * i]; // training
                        if (i == curFold) {
                            datamodel = splits[2 * i + 1]; // test
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
                    n++;
                }
            }
        }
        return splits;
    }
}
