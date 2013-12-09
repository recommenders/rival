package net.recommenders.rival.split.splitter;

import net.recommenders.rival.core.DataModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Alejandro
 */
public class TemporalSplitter implements Splitter<Long, Long> {

    private float percentageTraining;
    private boolean perUser;
    private boolean doSplitPerItems;

    public TemporalSplitter(float percentageTraining, boolean perUser, boolean doSplitPerItems) {
        this.percentageTraining = percentageTraining;
        this.perUser = perUser;
        this.doSplitPerItems = doSplitPerItems;
    }

    public DataModel<Long, Long>[] split(DataModel<Long, Long> data) {
        final DataModel<Long, Long>[] splits = new DataModel[2];
        splits[0] = new DataModel<Long, Long>(); // training
        splits[1] = new DataModel<Long, Long>(); // test
        if (perUser) {
            for (Long user : data.getUsers()) {
                if (!data.getUserItemTimestamps().containsKey(user)) {
                    continue;
                }
                Set<Long> userTimestamps = new HashSet<Long>();
                for (Set<Long> timestamps : data.getUserItemTimestamps().get(user).values()) {
                    userTimestamps.addAll(timestamps);
                }
                List<Long> listTimestamps = new ArrayList<Long>(userTimestamps);
                Collections.sort(listTimestamps);
                int splitPoint = Math.round(percentageTraining * listTimestamps.size());
                Set<Long> testTimestamps = new HashSet<Long>();
                int n = 0;
                for (Long t : listTimestamps) {
                    if (n > splitPoint) {
                        testTimestamps.add(t);
                    }
                    n++;
                }
                if (doSplitPerItems) {
                    for (Entry<Long, Set<Long>> e : data.getUserItemTimestamps().get(user).entrySet()) {
                        Long item = e.getKey();
                        Double pref = data.getUserItemPreferences().get(user).get(item);
                        boolean inTest = false;
                        for (Long time : e.getValue()) {
                            if (testTimestamps.contains(time)) {
                                inTest = true;
                                break;
                            }
                        }
                        DataModel<Long, Long> datamodel = splits[0]; // training
                        if (inTest) {
                            datamodel = splits[1]; // test
                            }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        for (Long time : e.getValue()) {
                            datamodel.addTimestamp(user, item, time);
                        }
                    }
                } else {
                    if (!data.getUserItemTimestamps().containsKey(user)) {
                        continue;
                    }
                    for (Entry<Long, Set<Long>> e : data.getUserItemTimestamps().get(user).entrySet()) {
                        Long item = e.getKey();
                        Double pref = data.getUserItemPreferences().get(user).get(item);
                        for (Long time : e.getValue()) {
                            DataModel<Long, Long> datamodel = splits[0]; // training
                            if (testTimestamps.contains(time)) {
                                datamodel = splits[1]; // test
                            }
                            if (pref != null) {
                                datamodel.addPreference(user, item, pref);
                            }
                            datamodel.addTimestamp(user, item, time);
                        }
                    }
                }
            }
        } else {
            // global temporal splitting
            Set<Long> allTimestamps = new HashSet<Long>();
            for (Long user : data.getUserItemTimestamps().keySet()) {
                for (Set<Long> timestamps : data.getUserItemTimestamps().get(user).values()) {
                    allTimestamps.addAll(timestamps);
                }
            }
            List<Long> listTimestamps = new ArrayList<Long>(allTimestamps);
            Collections.sort(listTimestamps);
            int splitPoint = Math.round(percentageTraining * listTimestamps.size());
            Set<Long> testTimestamps = new HashSet<Long>();
            int n = 0;
            for (Long t : listTimestamps) {
                if (n > splitPoint) {
                    testTimestamps.add(t);
                }
                n++;
            }
            for (Long user : data.getUsers()) {
                if (!data.getUserItemTimestamps().containsKey(user)) {
                    continue;
                }
                for (Long item : data.getUserItemPreferences().get(user).keySet()) {
                    Double pref = data.getUserItemPreferences().get(user).get(item);
                    Set<Long> time = data.getUserItemTimestamps().get(user).get(item);
                    if (doSplitPerItems) {
                        boolean inTest = false;
                        for (Long t : time) {
                            if (testTimestamps.contains(t)) {
                                inTest = true;
                                break;
                            }
                        }
                        DataModel<Long, Long> datamodel = splits[0]; // training
                        if (inTest) {
                            datamodel = splits[1]; // test
                            }
                        if (pref != null) {
                            datamodel.addPreference(user, item, pref);
                        }
                        for (Long t : time) {
                            datamodel.addTimestamp(user, item, t);
                        }
                    } else {
                        for (Long t : time) {
                            DataModel<Long, Long> datamodel = splits[0]; // training
                            if (testTimestamps.contains(t)) {
                                datamodel = splits[1]; // test
                            }
                            if (pref != null) {
                                datamodel.addPreference(user, item, pref);
                            }
                            datamodel.addTimestamp(user, item, t);
                        }
                    }
                }
            }
        }
        return splits;
    }
}
