package net.recommenders.evaluation.frameworks.mahout.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: alan
 * Date: 2013-11-05
 * Time: 09:54
 */
@SuppressWarnings("serial")
public class RecommenderException extends Exception{
    public RecommenderException(String msg){
        super(msg);
    }

    public RecommenderException(String msg, Throwable t){
        super(msg,t);
    }
}
