package net.recommenders.rival.evaluation.metric.divnov.dist;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
@RunWith(JUnit4.class)
public class JaccardGenreItemDistanceTest {
    
    @Test
    public void test() {
        Map<Long, Set<String>> itemGenresMap = new HashMap<Long, Set<String>>();
        itemGenresMap.put(0L, new HashSet<String>(Arrays.asList("comedy", "action", "scifi")));
        itemGenresMap.put(1L, new HashSet<String>(Arrays.asList("drama", "crime", "romance")));
        itemGenresMap.put(2L, new HashSet<String>(Arrays.asList("comedy", "crime", "scifi")));
        
        JaccardGenreItemDistance<Long, String> dist = new JaccardGenreItemDistance<Long, String>(itemGenresMap);
        
        Assert.assertEquals(1.0000, dist.dist(0L, 1L), 0.0001);
        Assert.assertEquals(0.8000, dist.dist(1L, 2L), 0.0001);
        Assert.assertEquals(0.5000, dist.dist(2L, 0L), 0.0001);
    }
}
