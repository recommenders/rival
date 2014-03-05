package net.recommenders.rival.evaluation.strategy;

import java.util.Set;

/**
 * An interface for the per-item evaluation strategy.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public interface EvaluationStrategyPerItem<U, I> {

    /**
     * Get the items to rank.
     *
     * @param user The user.
     * @param item The item.
     * @return The items to rank.
     */
    public Set<I> getCandidateItemsToRank(U user, I item);
}
