package net.recommenders.rival.split.splitter;

import net.recommenders.rival.core.DataModel;

/**
 * Interface for the data splitter.
 * @author Alejandro
 */
public interface Splitter<U, I> {

    /**
     * Splits the data.
     * @param data  The data.
     * @return  The split data model.
     */
    public DataModel<U, I>[] split(DataModel<U, I> data);
}
