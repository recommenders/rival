package net.recommenders.rival.split.strategy;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Alejandro
 */
public interface EvaluationStrategy<U, I> {

    public enum OUTPUT_FORMAT {

        SIMPLE,
        TRECEVAL;
    }

    public Set<I> getCandidateItemsToRank(U user);

    public void printRanking(U user, List<Pair<I, Double>> scoredItems, PrintStream out, OUTPUT_FORMAT format);

    public void printGroundtruth(U user, PrintStream out, OUTPUT_FORMAT format);

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
