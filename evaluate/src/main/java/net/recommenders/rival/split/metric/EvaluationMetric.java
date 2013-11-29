package net.recommenders.rival.split.metric;

import java.util.Map;

/**
 *
 * @author Alejandro
 */
public interface EvaluationMetric<V> {

    public double getValue();

    public Map<V, Double> getValuePerUser();
}
