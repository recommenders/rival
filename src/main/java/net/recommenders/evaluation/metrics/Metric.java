/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.metrics;

import java.util.Map;

/**
 *
 * @author alejandr
 */
public interface Metric<V> {

    public double getValue();

    public Map<V, Double> getValuePerUser();
}
