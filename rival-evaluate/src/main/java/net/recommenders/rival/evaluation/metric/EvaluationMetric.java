package net.recommenders.rival.evaluation.metric;

import java.util.Map;

/**
 * An evaluation metric expressing the quality of the evaluated system.
 * @author Alejandro
 */
public interface EvaluationMetric<V> {

    /**
     * Get the value of the metric.
     * @return The value of the metric.
     */
    public double getValue();

    /**
     * Get the value of the metric on a per-user basis.
     * @return Map containing the values per user.
     */
    public Map<V, Double> getValuePerUser();
}
