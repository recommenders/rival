package net.recommenders.evaluation.splitter;

import net.recommenders.evaluation.core.DataModel;

/**
 *
 * @author Alejandro
 */
public interface Splitter<U, I> {

    public DataModel<U, I>[] split(DataModel<U, I> data);
}
