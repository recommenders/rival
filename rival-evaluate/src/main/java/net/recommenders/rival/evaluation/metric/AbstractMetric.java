package net.recommenders.rival.evaluation.metric;

import net.recommenders.rival.core.DataModel;

import java.util.Map;

/**
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public abstract class AbstractMetric implements EvaluationMetric {

    /**
     * The predictions.
     */
    protected DataModel<Long, Long> predictions;
    /**
     * The test set.
     */
    protected DataModel<Long, Long> test;

    /**
     * The level of recall.
     */
    protected int at = 0;
    /**
     * Default constructor for the metric.
     *
     * @param predictions The predictions.
     * @param test The test set.
     */
    public AbstractMetric(DataModel<Long, Long> predictions, DataModel<Long, Long> test) {
        this.predictions = predictions;
        this.test = test;
    }

    /**
     * Default constructor for the metric.
     *
     * @param predictions The predictions.
     * @param test The test set.
     */
    public AbstractMetric(DataModel<Long, Long> predictions, DataModel<Long, Long> test, int at) {
        this.predictions = predictions;
        this.test = test;
        this.at = at;
    }


}
