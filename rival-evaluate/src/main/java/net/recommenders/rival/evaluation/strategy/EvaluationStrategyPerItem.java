package net.recommenders.rival.evaluation.strategy;

import java.util.Set;

/**
 * An interface for the per-item evaluation strategy.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <U> generic type for users
 * @param <I> generic type for items
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
