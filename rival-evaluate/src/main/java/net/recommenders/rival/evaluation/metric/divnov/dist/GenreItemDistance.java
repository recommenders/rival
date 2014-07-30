package net.recommenders.rival.evaluation.metric.divnov.dist;

import java.util.Map;
import java.util.Set;

/**
 * Abstract genre-based item distance
 * 
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public abstract class GenreItemDistance<I, G> implements ItemDistance<I> {

    private final Map<I, Set<G>> itemGenresMap;

    public GenreItemDistance(Map<I, Set<G>> itemGenresMap) {
        this.itemGenresMap = itemGenresMap;
    }

    @Override
    public double dist(I i, I j) {
        return dist(itemGenresMap.get(i), itemGenresMap.get(j));
    }

    protected abstract double dist(Set<G> genresI, Set<G> genresJ);
}
