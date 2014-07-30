package net.recommenders.rival.evaluation.metric.divnov.dist;

import java.util.Map;
import java.util.Set;

/**
 * Genre-based item distance using Jaccard's coefficient. Takes values between 0 and 1.
 * 
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public class JaccardGenreItemDistance<I, G> extends GenreItemDistance<I, G> {

    public JaccardGenreItemDistance(Map<I, Set<G>> itemGenresMap) {
        super(itemGenresMap);
    }

    @Override
    protected double dist(Set<G> genresI, Set<G> genresJ) {
        int isect = 0;
        for (G g : genresI) {
            isect += genresJ.contains(g) ? 1 : 0;
        }
        
        return 1 - isect / (double) (genresI.size() + genresJ.size() - isect);
    }
    
}
