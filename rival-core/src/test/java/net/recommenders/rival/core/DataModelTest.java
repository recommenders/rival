package net.recommenders.rival.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link net.recommenders.rival.core.DataModel}.
 *
 * @author Alan
 */
@RunWith(JUnit4.class)
public class DataModelTest<U,I> {

    DataModel dm = new DataModel();
    static final int USERS = 3;
    static final int ITEMS = 3;

    @Before
    public void initialize(){

        for(int u = 1; u <= USERS; u++){
            for(int i = 1; i <= ITEMS; i++){
                dm.addPreference((double)u, (double)i, (double)u*i);
            }
        }
    }
    @Test
    public void testGetUserPreferences() {
        Map<Double, Map<Double, Double>> storedPrefs = dm.getUserItemPreferences();
        for(int u = 1; u < 4; u++){
            Map<Double, Double> iprefs = storedPrefs.get((double) u);
            for(int i = 1; i < 4; i++){
                assertEquals((double)u*i, iprefs.get((double)i), 0.0);
            }
        }
    }
    @Test
    public void testGetNumItems(){
        assertEquals(ITEMS, dm.getNumItems());
    }
    @Test
    public void testGetNumUsers(){
        assertEquals(USERS, dm.getNumUsers());
    }

}