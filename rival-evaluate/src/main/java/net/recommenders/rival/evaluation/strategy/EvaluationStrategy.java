package net.recommenders.rival.evaluation.strategy;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;

/**
 * An inteface for evaluation strategies.
 * @author Alejandro
 */
public interface EvaluationStrategy<U, I> {

    public enum OUTPUT_FORMAT {

        SIMPLE,
        TRECEVAL;
    }

    /**
     * Get the items to rank.
     * @param user  The user.
     * @return  The items to rank.
     */
    public Set<I> getCandidateItemsToRank(U user);

    /**
     * Print rankings for a user.
     * @param user  The user.
     * @param scoredItems   The scored items to print.
     * @param out   Where to print.
     * @param format    The format of the printer.
     */
    public void printRanking(U user, List<Pair<I, Double>> scoredItems, PrintStream out, OUTPUT_FORMAT format);

    /**
     * Print the ground truth.
     * @param user  The user.
     * @param out   Where to print.
     * @param format    The format of the printer.
     */
    public void printGroundtruth(U user, PrintStream out, OUTPUT_FORMAT format);

    /**
     *
     * @param <A>
     * @param <B>
     */
    public static class Pair<A, B> {

        private A first;
        private B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }
}
