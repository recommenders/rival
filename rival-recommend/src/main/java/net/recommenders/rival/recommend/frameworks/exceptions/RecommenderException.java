package net.recommenders.rival.recommend.frameworks.exceptions;

/**
 * Exception to be used in the recommender module.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
@SuppressWarnings("serial")
public class RecommenderException extends Exception {

    /**
     * Constructs an exception with the specified message.
     *
     * @param msg the message to be shown later.
     */
    public RecommenderException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param msg the message to be shown later.
     * @param t the cause of the exception.
     */
    public RecommenderException(String msg, Throwable t) {
        super(msg, t);
    }
}
