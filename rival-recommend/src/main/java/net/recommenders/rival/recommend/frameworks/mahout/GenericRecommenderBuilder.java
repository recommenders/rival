package net.recommenders.rival.recommend.frameworks.mahout;

import java.lang.reflect.InvocationTargetException;
import net.recommenders.rival.recommend.frameworks.mahout.exceptions.RecommenderException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.RandomRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.knn.user.UserSimilarity;

/**
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 * @param <T> generic parameter
 */
public final class GenericRecommenderBuilder<T>
        implements RecommenderBuilder {

    /**
     * Number of neighbors.
     */
    public static final int DEFAULT_N = 50;
    /**
     * Number of neighbors when no neighbors are to be used.
     */
    public static final int NO_N = 0;
    /**
     * No iterations.
     */
    public static final int NOITER = 0;
    /**
     * No factors.
     */
    public static final int NOFACTORS = 0;

    @Override
    public Recommender buildRecommender(DataModel dataModel)
            throws TasteException {
        return new RandomRecommender(dataModel);
    }

    /**
     * Default CF recommender.
     *
     * @param dataModel the data model
     * @param recType the recommender type
     * @return the recommender
     * @throws TasteException  when
     * @throws RecommenderException when
     */
    public Recommender buildRecommender(DataModel dataModel, String recType)
            throws TasteException, RecommenderException {
        return buildRecommender(dataModel, recType, null, DEFAULT_N, NOFACTORS, NOITER, null);
    }

    /**
     * Recommender based on given recType and simType
     *
     * @param dataModel the data model
     * @param recType the recommender type
     * @param simType the similarity type
     * @return the recommender
     * @throws TasteException when
     * @throws RecommenderException when
     */
    public Recommender buildRecommender(DataModel dataModel, String recType, String simType)
            throws TasteException, RecommenderException {
        return buildRecommender(dataModel, recType, simType, DEFAULT_N, NOFACTORS, NOITER, null);
    }

    /**
     * Recommender based on given recType, simType and neighborhood type
     *
     * @param dataModel the data model
     * @param recType the recommender type
     * @param simType the similarity type
     * @param nbSize the neighborhood size
     * @return the recommender
     * @throws TasteException when
     * @throws RecommenderException when
     */
    public Recommender buildRecommender(DataModel dataModel, String recType, String simType, int nbSize)
            throws TasteException, RecommenderException {
        return buildRecommender(dataModel, recType, simType, nbSize, NOFACTORS, NOITER, null);
    }

    /**
     * SVD
     *
     * @param dataModel the data model
     * @param recType the recommender type
     * @param facType the factorizer
     * @param iterations number of iterations
     * @param factors number of factors
     * @return the recommender
     * @throws TasteException when
     * @throws RecommenderException when
     */
    public Recommender buildRecommender(DataModel dataModel, String recType, String facType, int iterations, int factors)
            throws TasteException, RecommenderException {
        return buildRecommender(dataModel, recType, null, NO_N, factors, iterations, facType);
    }

    /**
     * @param dataModel the data model
     * @param recType the type of the recommender, e.g. Basic
     * @param similarityType the type of the similarity, e.g. PersonCorrelation
     * @param neighborhoodSize the neighborhood size
     * @param factors  number of factors
     * @param iterations number of iterations
     * @param facType the factorizer
     * @return the recommender
     * @throws TasteException when
     * @throws RecommenderException when
     */
    public Recommender buildRecommender(final DataModel dataModel,
            final String recType,
            final String similarityType,
            final int neighborhoodSize,
            final int factors,
            final int iterations,
            String facType)
            throws TasteException, RecommenderException {
        String neighborhoodType = "org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood";
        try {
            Object simObj = null;
            /**
             * Instantiate similarity class
             */
            if (similarityType != null) {
                Class<?> similarityClass = null;
                try {
                    similarityClass = Class.forName(similarityType);
                    simObj = similarityClass.getConstructor(DataModel.class).newInstance(dataModel);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new RecommenderException("Could not create Similarity class " + e.getMessage());
                }
            }
            /**
             * Instantiate neighborhood class
             */
            Object neighObj = null;
            //if (neighborhoodType != null){
            if (neighborhoodSize != NO_N) {
                Class<?> neighborhoodClass = null;
                try {
                    neighborhoodClass = Class.forName(neighborhoodType);
                    neighObj = neighborhoodClass.getConstructor(int.class, UserSimilarity.class, DataModel.class).newInstance(neighborhoodSize, simObj, dataModel);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new RecommenderException("Could not create Neighborhood class " + e.getMessage());
                }
            }
            /**
             * Instantiate factorizer class
             */
            if (facType != null) {
                Class<?> factorizerClass = null;
                try {
                    factorizerClass = Class.forName(facType);
                } catch (ClassNotFoundException e) {
                    System.out.println("Could not create Factorizer " + e.getMessage());
                }
                simObj = factorizerClass.getConstructor(DataModel.class, int.class, int.class).newInstance(dataModel, factors, iterations);

            }
            /**
             * Instantiate recommender class
             */
            final Class<?> recommenderClass;
            try {
                recommenderClass = Class.forName(recType);
            } catch (ClassNotFoundException e) {
                throw new RecommenderException("Could not create Recommender class " + e.getMessage());
            }
            final Object recObj;
            try {
                if (facType != null) {
                    recObj = recommenderClass.getConstructor(DataModel.class, Factorizer.class).newInstance(dataModel, (Factorizer) simObj);
                } // user-based similarity with neighborhood
                //                else if (neighborhoodType != null && similarityType != null) {
                else if (recType.contains("UserBased")) {
                    recObj = recommenderClass.getConstructor(DataModel.class, UserNeighborhood.class, UserSimilarity.class).newInstance(dataModel, neighObj, simObj);
                } // item-based similarity, no neighborhood
                else if (similarityType != null) {
                    recObj = recommenderClass.getConstructor(DataModel.class, ItemSimilarity.class).newInstance(dataModel, simObj);
                } else {
                    recObj = recommenderClass.getConstructor(DataModel.class).newInstance(dataModel);
                }
            } catch (Exception e) { //ClassNotFoundException e) {
                throw new RecommenderException("Could not create Recommender: " + e.getMessage());
            }
            return (Recommender) recObj;
        } catch (IllegalArgumentException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        } catch (SecurityException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        }
    }
}
