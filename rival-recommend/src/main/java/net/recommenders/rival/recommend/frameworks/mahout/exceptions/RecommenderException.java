package net.recommenders.rival.recommend.frameworks.mahout.exceptions;

/**
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
@SuppressWarnings("serial")
public class RecommenderException extends Exception {

    public RecommenderException(String msg) {
        super(msg);
    }

    public RecommenderException(String msg, Throwable t) {
        super(msg, t);
    }
}
