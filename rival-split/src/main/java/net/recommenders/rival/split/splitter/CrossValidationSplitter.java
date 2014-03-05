package net.recommenders.rival.split.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.recommenders.rival.core.DataModel;

/**
 * Class that splits a dataset using a cross validation technique (every
 * interaction in the data only appears once in each test split).
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class CrossValidationSplitter implements Splitter<Long, Long> {

    /**
     * The number of folds that the data will be split into.
     */
    private int nFolds;
    /**
     * The flag that indicates if the split should be done in a per user basis.
     */
    private boolean perUser;
    /**
     * An instance of a Random class.
     */
    private Random rnd;

    /**
     * Constructor
     *
     * @param nFolds number of folds that the data will be split into
     * @param perUser flag to do the split in a per user basis
     * @param seed value to initialize a Random class
     */
    public CrossValidationSplitter(int nFolds, boolean perUser, long seed) {
        this.nFolds = nFolds;
        this.perUser = perUser;

        rnd = new Random(seed);
    }

    /**
     * @inheritDoc
     */
    @Override
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
