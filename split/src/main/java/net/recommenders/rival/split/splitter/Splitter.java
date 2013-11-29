package net.recommenders.rival.split.splitter;

import net.recommenders.rival.core.DataModel;

/**
 *
 * @author Alejandro
 */
public interface Splitter<U, I> {

    public DataModel<U, I>[] split(DataModel<U, I> data);
}
