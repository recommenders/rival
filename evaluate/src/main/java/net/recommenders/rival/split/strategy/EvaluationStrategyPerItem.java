package net.recommenders.rival.split.strategy;

import java.util.Set;

/**
 *
 * @author Alejandro
 */
public interface EvaluationStrategyPerItem<U, I> {

    public Set<I> getCandidateItemsToRank(U user, I item);
}
