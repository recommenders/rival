/*
 * Copyright 2015 recommenders.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.recommenders.rival.recommend.frameworks.mahout;

import java.lang.reflect.InvocationTargetException;
import net.recommenders.rival.recommend.frameworks.exceptions.RecommenderException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.RandomRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 * A generic recommender builder for Mahout recommenders in order to avoid
 * generating separate builders for each recommender type.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
public final class GenericRecommenderBuilder implements RecommenderBuilder {

    /**
     * Number of neighbors.
     */
    public static final int DEFAULT_N = 50;
    /**
     * Number of neighbors when no neighbors are to be used.
     */
    public static final int NO_N = 0;
    /**
     * Number of iterations.
     */
    public static final int NOITER = 0;
    /**
     * Number of factors.
     */
    public static final int NOFACTORS = 0;

    /**
     * Builds a random recommender which will recommend items from the data
     * model passed as a parameter.
     *
     * @param dataModel the data model
     * @return the recommender
     * @throws TasteException when the recommender is instantiated incorrectly.
     */
    @Override
    public Recommender buildRecommender(final DataModel dataModel)
            throws TasteException {
        return new RandomRecommender(dataModel);
    }

    /**
     * CF recommender with default parameters.
     *
     * @param dataModel the data model
     * @param recType the recommender type (as Mahout class)
     * @return the recommender
     * @throws RecommenderException see {@link #buildRecommender(org.apache.mahout.cf.taste.model.DataModel, java.lang.String, java.lang.String, int, int, int, java.lang.String)}
     */
    public Recommender buildRecommender(final DataModel dataModel, final String recType)
            throws RecommenderException {
        return buildRecommender(dataModel, recType, null, DEFAULT_N, NOFACTORS, NOITER, null);
    }

    /**
     * Recommender based on given recType and simType (with default parameters).
     *
     * @param dataModel the data model
     * @param recType the recommender type (as Mahout class)
     * @param simType the similarity type (as Mahout class)
     * @return the recommender
     * @throws RecommenderException see {@link #buildRecommender(org.apache.mahout.cf.taste.model.DataModel, java.lang.String, java.lang.String, int, int, int, java.lang.String)}
     */
    public Recommender buildRecommender(final DataModel dataModel, final String recType, final String simType)
            throws RecommenderException {
        return buildRecommender(dataModel, recType, simType, DEFAULT_N, NOFACTORS, NOITER, null);
    }

    /**
     * Recommender based on given recType, simType and neighborhood type.
     *
     * @param dataModel the data model
     * @param recType the recommender type (as Mahout class)
     * @param simType the similarity type (as Mahout class)
     * @param nbSize the neighborhood size
     * @return the recommender
     * @throws RecommenderException see {@link #buildRecommender(org.apache.mahout.cf.taste.model.DataModel, java.lang.String, java.lang.String, int, int, int, java.lang.String)}
     */
    public Recommender buildRecommender(final DataModel dataModel, final String recType, final String simType, final int nbSize)
            throws RecommenderException {
        return buildRecommender(dataModel, recType, simType, nbSize, NOFACTORS, NOITER, null);
    }

    /**
     * SVD.
     *
     * @param dataModel the data model
     * @param recType the recommender type (as Mahout class)
     * @param facType the factorizer (as Mahout class)
     * @param iterations number of iterations
     * @param factors number of factors
     * @return the recommender
     * @throws RecommenderException see
     * {@link #buildRecommender(org.apache.mahout.cf.taste.model.DataModel, java.lang.String, java.lang.String, int, int, int, java.lang.String)}
     */
    public Recommender buildRecommender(final DataModel dataModel, final String recType, final String facType, final int iterations, final int factors)
            throws RecommenderException {
        return buildRecommender(dataModel, recType, null, NO_N, factors, iterations, facType);
    }

    /**
     * General method to instantiate a Mahout recommender.
     *
     * @param dataModel the data model
     * @param recType the type of the recommender (as Mahout class)
     * @param similarityType the type of the similarity (as Mahout class), e.g.
     * PearsonCorrelation
     * @param neighborhoodSize the neighborhood size
     * @param factors number of factors
     * @param iterations number of iterations
     * @param facType the factorizer (as Mahout class)
     * @return the recommender
     * @throws RecommenderException when there is a problem with instantiation
     */
    public Recommender buildRecommender(final DataModel dataModel,
            final String recType,
            final String similarityType,
            final int neighborhoodSize,
            final int factors,
            final int iterations,
            final String facType)
            throws RecommenderException {
        String neighborhoodType = "org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood";
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
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Similarity class " + e.getMessage());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Similarity class " + e.getMessage());
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Similarity class " + e.getMessage());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Similarity class " + e.getMessage());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Similarity class " + e.getMessage());
            } catch (SecurityException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Similarity class " + e.getMessage());
            }
        }
        /**
         * Instantiate neighborhood class
         */
        Object neighObj = null;
        if (neighborhoodSize != NO_N) {
            Class<?> neighborhoodClass = null;
            try {
                neighborhoodClass = Class.forName(neighborhoodType);
                neighObj = neighborhoodClass.getConstructor(int.class, UserSimilarity.class, DataModel.class).newInstance(neighborhoodSize, simObj, dataModel);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Neighborhood class " + e.getMessage());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Neighborhood class " + e.getMessage());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Neighborhood class " + e.getMessage());
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Neighborhood class " + e.getMessage());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Neighborhood class " + e.getMessage());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Neighborhood class " + e.getMessage());
            } catch (SecurityException e) {
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
                simObj = factorizerClass.getConstructor(DataModel.class, int.class, int.class).newInstance(dataModel, factors, iterations);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Factorizer " + e.getMessage());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Factorizer " + e.getMessage());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Factorizer " + e.getMessage());
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Factorizer " + e.getMessage());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Factorizer " + e.getMessage());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Factorizer " + e.getMessage());
            } catch (SecurityException e) {
                e.printStackTrace();
                throw new RecommenderException("Could not create Factorizer " + e.getMessage());
            }
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
            } else if (recType.contains("UserBased")) {
                // user-based similarity with neighborhood
                recObj = recommenderClass.getConstructor(DataModel.class, UserNeighborhood.class, UserSimilarity.class).newInstance(dataModel, neighObj, simObj);
            } else if (similarityType != null) {
                // item-based similarity, no neighborhood
                recObj = recommenderClass.getConstructor(DataModel.class, ItemSimilarity.class).newInstance(dataModel, simObj);
            } else {
                recObj = recommenderClass.getConstructor(DataModel.class).newInstance(dataModel);
            }
        } catch (IllegalAccessException e) {
            throw new RecommenderException("Could not create Recommender: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RecommenderException("Could not create Recommender: " + e.getMessage());
        } catch (InstantiationException e) {
            throw new RecommenderException("Could not create Recommender: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RecommenderException("Could not create Recommender: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new RecommenderException("Could not create Recommender: " + e.getMessage());
        } catch (SecurityException e) {
            throw new RecommenderException("Could not create Recommender: " + e.getMessage());
        }
        return (Recommender) recObj;
    }
}
