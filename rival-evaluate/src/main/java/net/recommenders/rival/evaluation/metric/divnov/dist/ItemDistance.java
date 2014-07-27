package net.recommenders.rival.evaluation.metric.divnov.dist;

/**
 * Item distance
 * 
 * Symmetrical and left-bounded at 0 (identical items).
 *
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public interface ItemDistance<I> {
    
    public double dist(I i, I j);
}