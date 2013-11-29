package net.recommenders.rival.evaluation.splitter;

import net.recommenders.rival.evaluation.core.DataModel;

/**
 *
 * @author Alejandro
 */
public interface Splitter<U, I> {

    public DataModel<U, I>[] split(DataModel<U, I> data);
}
