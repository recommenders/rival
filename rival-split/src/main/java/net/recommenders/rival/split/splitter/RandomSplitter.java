package net.recommenders.rival.split.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class RandomSplitter implements Splitter<Long, Long> {

    private float percentageTraining;
    private boolean perUser;
    private Random rnd;
    private boolean doSplitPerItems;

    public RandomSplitter(float percentageTraining, boolean perUser, long seed, boolean doSplitPerItems) {
        this.percentageTraining = percentageTraining;
        this.perUser = perUser;
        this.doSplitPerItems = doSplitPerItems;

        rnd = new Random(seed);
    }

    public DataModel<Long, Long>[] split(DataModel<Long, Long> data) {
        final DataModel<Long, Long>[] splits = new DataModel[2];
        splits[0] = new DataModel<Long, Long>(); // training
        splits[1] = new DataModel<Long, Long>(); // test
        if (perUser) {
            for (Long user : data.getUsers()) {
                if (doSplitPerItems) {
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
                } else {
                    if (!data.getUserItemTimestamps().containsKey(user)) {
                        continue;
                    }
                    List<String> itemsTime = new ArrayList<String>();
                    for (Entry<Long, Set<Long>> e : data.getUserItemTimestamps().get(user).entrySet()) {
                        long i = e.getKey();
                        for (Long t : e.getValue()) {
                            itemsTime.add(i + "_" + t);
                        }
                    }
                    Collections.shuffle(itemsTime, rnd);
                    int splitPoint = Math.round(percentageTraining * itemsTime.size());
                    for (int i = 0; i < itemsTime.size(); i++) {
                        String it = itemsTime.get(i);
                        Long item = Long.parseLong(it.split("_")[0]);
                        Long time = Long.parseLong(it.split("_")[1]);
                        Double pref = data.getUserItemPreferences().get(user).get(item);
                        DataModel<Long, Long> datamodel = splits[0]; // training
                        if (i > splitPoint) {
                            datamodel = splits[1]; // test
                        }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        if (time != null) {
                            datamodel.addTimestamp(user, item, time);
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
                    if (doSplitPerItems) {
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
                    } else {
                        if (time != null) {
                            for (Long t : time) {
                                DataModel<Long, Long> datamodel = splits[0]; // training
                                if (rnd.nextDouble() > percentageTraining) {
                                    datamodel = splits[1]; // test
                                }
                                if (pref != null) {
                                    datamodel.addPreference(user, item, pref);
                                }
                                datamodel.addTimestamp(user, item, t);
                            }
                        } else {
                            DataModel<Long, Long> datamodel = splits[0]; // training
                            if (rnd.nextDouble() > percentageTraining) {
                                datamodel = splits[1]; // test
                            }
                            if (pref != null) {
                                datamodel.addPreference(user, item, pref);
                            }
                        }
                    }
                }
            }
        }
        return splits;
    }
}
