package net.recommenders.evaluation.frameworks.mahout;


import net.recommenders.evaluation.frameworks.mahout.exceptions.RecommenderException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.RandomRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.lang.reflect.InvocationTargetException;


public final class GenericRecommenderBuilder<T>
        implements RecommenderBuilder {

    public static final int DEFAULT_N = 50;
    public static final int NOITER = 0;
    public static final int NOFACTORS = 0;



    @Override
    public Recommender buildRecommender(DataModel dataModel)
            throws TasteException {
        return new RandomRecommender(dataModel);
    }

    /**
     * Default CF recommender.
     * @param dataModel
     * @param recType
     * @return
     * @throws TasteException
     * @throws RecommenderException
     */
    public Recommender buildRecommender(DataModel dataModel, String recType)
            throws TasteException, RecommenderException {
        return buildRecommender(dataModel, recType, null, null, NOITER, NOFACTORS, null);
    }

    /**
     * Recommender based on given recType and simType
     * @param dataModel
     * @param recType
     * @param simType
     * @return
     * @throws TasteException
     * @throws RecommenderException
     */
    public Recommender buildRecommender(DataModel dataModel, String recType, String simType)
            throws TasteException, RecommenderException {
        return buildRecommender(dataModel, recType, simType, null, NOITER, NOFACTORS, null);
    }

    /**
     * Recommender based on given recType, simType and neighborhood type
     * @param dataModel
     * @param recType
     * @param simType
     * @param nbType
     * @return
     * @throws TasteException
     * @throws RecommenderException
     */
    public Recommender buildRecommender(DataModel dataModel, String recType, String simType, String nbType)
            throws TasteException, RecommenderException {
        return buildRecommender(dataModel, recType, simType, nbType, NOITER, NOFACTORS, null);
    }

    public Recommender buildRecommender(DataModel dataModel, String recType, String facType, int iterations, int factors)
            throws TasteException, RecommenderException {
        return buildRecommender(dataModel, recType, null, null, iterations, factors, facType);
    }


    /**
     * @param dataModel
     *            the data model
     * @param recType
     *            the type of the recommender, e.g. Basic
     * @param similarityType
     *            the type of the similarity, e.g. PersonCorrelation
     * @return
     * @throws TasteException
     * @throws RecommenderException
     */
    public Recommender buildRecommender(final DataModel dataModel,
                                        final String recType,
                                        final String similarityType,
                                        final String neighborhoodType,
                                        final int factors,
                                        final int iterations,
                                        String facType)
            throws TasteException, RecommenderException {
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
                }
                catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new RecommenderException("Could not create Similarity class " + e.getMessage());
                }
            }
            /**
             * Instantiate neighborhood class
             */
            Object neighObj = null;
            if (neighborhoodType != null){
                Class<?> neighborhoodClass = null;
                try {
                    neighborhoodClass = Class.forName(neighborhoodType);
                    neighObj = neighborhoodClass.getConstructor(int.class, UserSimilarity.class, DataModel.class).newInstance(DEFAULT_N, simObj, dataModel);
                }
                catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new RecommenderException("Could not create Neighborhood class " + e.getMessage());
                }
            }
            /**
             * Instantiate factorizer class
             */
            if (facType != null){
                Class<?> factorizerClass = null;
                try{
                factorizerClass = Class.forName(facType);
                } catch (ClassNotFoundException e){
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
            }
            catch (ClassNotFoundException e) {
                throw new RecommenderException("Could not create Recommender class " + e.getMessage());
            }
            final Object recObj;
            try {
                if (facType != null){
                    recObj = recommenderClass.getConstructor(DataModel.class, Factorizer.class).newInstance(dataModel, (Factorizer)simObj);
                }
                // user-based similarity with neighborhood
                else if (neighborhoodType != null && similarityType != null) {
                    recObj = recommenderClass.getConstructor(DataModel.class, UserNeighborhood.class, UserSimilarity.class).newInstance(dataModel, neighObj, simObj);
                }
                // item-based similarity, no neighborhood
                else if (similarityType != null) {
                    recObj = recommenderClass.getConstructor(DataModel.class, ItemSimilarity.class).newInstance(dataModel, simObj);
                }
                else {
                    recObj = recommenderClass.getConstructor(DataModel.class).newInstance(dataModel);
                }
            }
            catch (Exception e){ //ClassNotFoundException e) {
                throw new RecommenderException("Could not create Recommender: " + e.getMessage());
            }
            return (Recommender) recObj;
        }
        catch (IllegalArgumentException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        }
        catch (SecurityException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        }
        catch (InstantiationException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        }
        catch (IllegalAccessException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        }
        catch (InvocationTargetException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        }
        catch (NoSuchMethodException e) {
            throw new RecommenderException("Could not create generic Recommender: " + e.getMessage(), e);
        }
    }

}
