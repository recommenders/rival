package net.recommenders.rival.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
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

    /**
     * Preference map between users and items.
     */
    private Map<U, Map<I, Double>> userItemPreferences;
    /**
     * Preference map between items and users.
     */
    private Map<I, Map<U, Double>> itemUserPreferences;
    /**
     * The map with the timestamps between users and items.
     */
    private Map<U, Map<I, Set<Long>>> userItemTimestamps;

    /**
     * Default constructor
     */
    public DataModel() {
        this(new HashMap<U, Map<I, Double>>(), new HashMap<I, Map<U, Double>>(), new HashMap<U, Map<I, Set<Long>>>());
    }

    /**
     * Constructor with parameters.
     *
     * @param userItemPreferences The preference map between users and items.
     * @param itemUserPreferences The preference map between items and users.
     * @param userItemTimestamps The map with the timestamps between users and
     * items
     */
    public DataModel(Map<U, Map<I, Double>> userItemPreferences, Map<I, Map<U, Double>> itemUserPreferences, Map<U, Map<I, Set<Long>>> userItemTimestamps) {
        this.userItemPreferences = userItemPreferences;
        this.itemUserPreferences = itemUserPreferences;
        this.userItemTimestamps = userItemTimestamps;
    }

    /**
     * Method that returns the preference map between items and users.
     *
     * @return the preference map between items and users.
     */
    public Map<I, Map<U, Double>> getItemUserPreferences() {
        return itemUserPreferences;
    }

    /**
     * Method that returns the preference map between users and items.
     *
     * @return the preference map between users and items.
     */
    public Map<U, Map<I, Double>> getUserItemPreferences() {
        return userItemPreferences;
    }

    /**
     * Method that returns the map with the timestamps between users and items.
     *
     * @return the map with the timestamps between users and items.
     */
    public Map<U, Map<I, Set<Long>>> getUserItemTimestamps() {
        return userItemTimestamps;
    }

    /**
     * Method that adds a preference to the model between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @param d the preference.
     */
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

    /**
     * Method that adds a timestamp to the model between a user and an item.
     *
     * @param u the user.
     * @param i the item.
     * @param t the timestamp.
     */
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

    /**
     * Method that returns the items in the model.
     *
     * @return the items in the model.
     */
    public Set<I> getItems() {
        return getItemUserPreferences().keySet();
    }

    /**
     * Method that returns the users in the model.
     *
     * @return the users in the model.
     */
    public Set<U> getUsers() {
        return getUserItemPreferences().keySet();
    }

    /**
     * Method that returns the number of items in the model.
     *
     * @return the number of items in the model.
     */
    public int getNumItems() {
        return getItems().size();
    }

    /**
     * Method that returns the number of users in the model.
     *
     * @return the number of users in the model.
     */
    public int getNumUsers() {
        return getUsers().size();
    }

    /**
     * Method that clears all the maps contained in the model.
     */
    public void clear() {
        userItemPreferences.clear();
        userItemTimestamps.clear();
        itemUserPreferences.clear();
    }

    /**
     * Method that saves a data model to a file.
     * Moved to @DataModelUtils
     *
     * @param outfile file where the model will be saved
     * @param overwrite flag that indicates if the file should be overwritten
     * @throws java.io.FileNotFoundException when
     */
    /**
    public void saveDataModel(String outfile, boolean overwrite) throws FileNotFoundException {
        if (new File(outfile).exists() && !overwrite) {
            System.out.println("Ignoring " + outfile);
        } else {
            PrintStream out = new PrintStream(outfile);
            for (U user : getUsers()) {
                Map<I, Double> userPrefModel = getUserItemPreferences().get(user);
                Map<I, Set<Long>> userTimeModel = getUserItemTimestamps().get(user);
                for (I item : userPrefModel.keySet()) {
                    Double pref = userPrefModel.get(item);
                    Set<Long> time = userTimeModel != null ? userTimeModel.get(item) : null;
                    if (time == null) {
                        out.println(user + "\t" + item + "\t" + pref + "\t-1");
                    } else {
                        for (Long t : time) {
                            out.println(user + "\t" + item + "\t" + pref + "\t" + t);
                        }
                    }
                }
            }
            out.close();
        }
    }
     */
}
