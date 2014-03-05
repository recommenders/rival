package net.recommenders.rival.core;

import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link net.recommenders.rival.core.DataModel}.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
@RunWith(JUnit4.class)
public class DataModelTest<U, I> {

    DataModel dm = new DataModel();
    static final int USERS = 3;
    static final int ITEMS = 3;

    @Before
    public void initialize() {

        for (int u = 1; u <= USERS; u++) {
            for (int i = 1; i <= ITEMS; i++) {
                dm.addPreference((double) u, (double) i, (double) u * i);
            }
        }
    }

    @Test
    public void testGetUserPreferences() {
        Map<Double, Map<Double, Double>> storedPrefs = dm.getUserItemPreferences();
        for (int u = 1; u <= USERS; u++) {
            Map<Double, Double> iprefs = storedPrefs.get((double) u);
            for (int i = 1; i <= ITEMS; i++) {
                assertEquals((double) u * i, iprefs.get((double) i), 0.0);
            }
        }
    }

    @Test
    public void testGetNumItems() {
        assertEquals(ITEMS, dm.getNumItems());
    }

    @Test
    public void testGetNumUsers() {
        assertEquals(USERS, dm.getNumUsers());
    }
}