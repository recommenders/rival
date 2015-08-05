package net.recommenders.rival.evaluation.strategy;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import net.recommenders.rival.evaluation.Pair;

/**
 * An interface for evaluation strategies.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 *
 * @param <U> generic type for users
 * @param <I> generic type for items
 */
public interface EvaluationStrategy<U, I> {

    /**
     * Enumeration that defines two output formats: a simple one (tab-separated)
     * and another compatible with the one used by the treceval program.
     */
    public enum OUTPUT_FORMAT {

        SIMPLE,
        TRECEVAL;
    }

    /**
     * Get the items to rank.
     *
     * @param user The user.
     * @return The items to rank.
     */
    public Set<I> getCandidateItemsToRank(U user);

    /**
     * Print rankings for a user.
     *
     * @param user The user.
     * @param scoredItems The scored items to print.
     * @param out Where to print.
     * @param format The format of the printer (see {@link OUTPUT_FORMAT}).
     */
    public void printRanking(U user, List<Pair<I, Double>> scoredItems, PrintStream out, OUTPUT_FORMAT format);

    /**
     * Print the ground truth.
     *
     * @param user The user.
     * @param out Where to print.
     * @param format The format of the printer (see {@link OUTPUT_FORMAT}).
     */
    public void printGroundtruth(U user, PrintStream out, OUTPUT_FORMAT format);
}
