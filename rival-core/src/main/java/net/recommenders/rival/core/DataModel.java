package net.recommenders.rival.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Data model used throughout the toolkit. Able to store users, items,
 * preferences, timestamps.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>, <a
 * href="http://github.com/alansaid">Alan</a>
 */
public class DataModel<U, I> {

    private Map<U, Map<I, Double>> userItemPreferences;
    private Map<I, Map<U, Double>> itemUserPreferences;
    private Map<U, Map<I, Set<Long>>> userItemTimestamps;

    public DataModel() {
        this(new HashMap<U, Map<I, Double>>(), new HashMap<I, Map<U, Double>>(), new HashMap<U, Map<I, Set<Long>>>());
    }

    public DataModel(Map<U, Map<I, Double>> userItemPreferences, Map<I, Map<U, Double>> itemUserPreferences, Map<U, Map<I, Set<Long>>> userItemTimestamps) {
        this.userItemPreferences = userItemPreferences;
        this.itemUserPreferences = itemUserPreferences;
        this.userItemTimestamps = userItemTimestamps;
    }

    public Map<I, Map<U, Double>> getItemUserPreferences() {
        return itemUserPreferences;
    }

    public Map<U, Map<I, Double>> getUserItemPreferences() {
        return userItemPreferences;
    }

    public Map<U, Map<I, Set<Long>>> getUserItemTimestamps() {
        return userItemTimestamps;
    }

    public void addPreference(U u, I i, Double d) {
        // update direct map
        Map<I, Double> userPreferences = userItemPreferences.get(u);
        if (userPreferences == null) {
            userPreferences = new HashMap<I, Double>();
            userItemPreferences.put(u, userPreferences);
        }
        Double preference = userPreferences.get(i);
        if (preference == null) {
            preference = 0.0;
        }
        preference += d;
        userPreferences.put(i, preference);
        // update inverse map
        Map<U, Double> itemPreferences = itemUserPreferences.get(i);
        if (itemPreferences == null) {
            itemPreferences = new HashMap<U, Double>();
            itemUserPreferences.put(i, itemPreferences);
        }
        itemPreferences.put(u, preference);
    }

    public void addTimestamp(U u, I i, Long t) {
        Map<I, Set<Long>> userTimestamps = userItemTimestamps.get(u);
        if (userTimestamps == null) {
            userTimestamps = new HashMap<I, Set<Long>>();
            userItemTimestamps.put(u, userTimestamps);
        }
        Set<Long> timestamps = userTimestamps.get(i);
        if (timestamps == null) {
            timestamps = new HashSet<Long>();
            userTimestamps.put(i, timestamps);
        }
        timestamps.add(t);
    }

    public Set<I> getItems() {
        return getItemUserPreferences().keySet();
    }

    public Set<U> getUsers() {
        return getUserItemPreferences().keySet();
    }

    public int getNumItems() {
        return getItems().size();
    }

    public int getNumUsers() {
        return getUsers().size();
    }

    public void clear() {
        userItemPreferences.clear();
        userItemTimestamps.clear();
        itemUserPreferences.clear();
    }
}
