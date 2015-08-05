package net.recommenders.rival.evaluation.metric;

import java.util.Map;

/**
 * An evaluation metric expressing the quality of the evaluated system.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <V> generic type for users
 */
public interface EvaluationMetric<V> {

    /**
     * Get the overall value of the metric.
     *
     * @return The overall value of the metric.
     */
    public double getValue();

    /**
     * Get the value of the metric on a per-user basis.
     *
     * @return Map containing the values per user.
     */
    public Map<V, Double> getValuePerUser();

    /**
     * Get the value of the metric for a specific user.
     *
     * @param u user whose metric value will be computed
     *
     * @return a value for user u.
     */
    public double getValue(V u);

    /**
     * Computes the evaluation metric. This method should be called
     * <b>before</b> asking for the values of the metric.
     */
    public void compute();
}
