/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.splitter;

import net.recommenders.evaluation.core.DataModel;

/**
 *
 * @author alejandr
 */
public interface Splitter<U, I> {

    public DataModel<U, I>[] split(DataModel<U, I> data);
}
