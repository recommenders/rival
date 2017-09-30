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
package net.recommenders.rival.recommend.frameworks;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import net.recommenders.rival.recommend.frameworks.lenskit.LenskitRecommenderRunner;
import net.recommenders.rival.recommend.frameworks.mahout.MahoutRecommenderRunner;
import net.recommenders.rival.recommend.frameworks.ranksys.RanksysRecommenderRunner;

import java.util.Properties;

/**
 * Runner for recommendation methods.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
public final class RecommendationRunner {

    /**
     * The property key for the recommender.
     */
    public static final String RECOMMENDER = "recommender";
    /**
     * The property key for the similarity.
     */
    public static final String SIMILARITY = "similarity";
    /**
     * The property key for the factorizer.
     */
    public static final String FACTORIZER = "factorizer";
    /**
     * The property key for the neighborhood.
     */
    public static final String NEIGHBORHOOD = "neighborhood";
    /**
     * The property key for the factors.
     */
    public static final String FACTORS = "factors";
    /**
     * The property key for the iterations.
     */
    public static final String ITERATIONS = "iterations";
    /**
     * The property key for the training set.
     */
    public static final String TRAINING_SET = "training";
    /**
     * The property key for the test set.
     */
    public static final String TEST_SET = "test";
    /**
     * The property key for the user index.
     */
    public static final String USER_INDEX = "user_index";
    /**
     * The property key for the item index.
     */
    public static final String ITEM_INDEX = "item_index";
    /**
     * The property key for the output.
     */
    public static final String OUTPUT = "output";
    /**
     * The property key for the framework.
     */
    public static final String FRAMEWORK = "framework";
    /**
     * The property key for Mahout.
     */
    public static final String MAHOUT = "mahout";
    /**
     * The property key for LensKit.
     */
    public static final String LENSKIT = "lenskit";
    /**
     * The property key for Librec.
     */
    public static final String LIBREC = "librec";
    /**
     * The property key for RankSys.
     */
    public static final String RANKSYS = "ranksys";
    /**
     * The canonical path.
     */
    private static String statPath;
    /**
     * The execution time.
     */
    private static long time;

    /**
     * Utility classes should not have a public or default constructor.
     */
    private RecommendationRunner() {
    }

    /**
     * Main method for running a recommendation.
     *
     * @param args CLI arguments
     */
    public static void main(final String[] args) {
        String propertyFile = System.getProperty("file");
        if (propertyFile == null) {
            System.out.println("Property file not given, exiting.");
            System.exit(0);
        }
        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        recommend(properties);
    }

    /**
     * Run recommendations based on properties.
     *
     * @param properties the properties
     */
    public static void recommend(final Properties properties) {
        AbstractRunner rr = instantiateRecommender(properties);
        run(rr);
    }

    /**
     * Run recommendations based on an already instantiated recommender.
     *
     * @param rr abstract recommender already initialized
     */
    public static void run(final AbstractRunner rr) {
        time = System.currentTimeMillis();
        boolean statsExist = false;
        statPath = rr.getCanonicalFileName();
        statsExist = rr.isAlreadyRecommended();
        try {
            rr.run(AbstractRunner.RUN_OPTIONS.OUTPUT_RECS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        if (!statsExist) {
            writeStats(statPath, "time", time);
        }
    }

    /**
     * Instantiates a recommender according to the provided properties mapping.
     *
     * @param properties the properties to be used when initializing the
     * recommender
     * @return the recommender instantiated
     */
    public static AbstractRunner<Long, Long> instantiateRecommender(final Properties properties) {
        if (properties.getProperty(RECOMMENDER) == null) {
            System.out.println("No recommenderClass specified, exiting.");
            return null;
        }
        if (properties.getProperty(TRAINING_SET) == null) {
            System.out.println("No training set specified, exiting.");
            return null;
        }
        if (properties.getProperty(TEST_SET) == null) {
            System.out.println("No training set specified, exiting.");
            return null;
        }

        AbstractRunner<Long, Long> rr = null;
        if (properties.getProperty(FRAMEWORK).equals(MAHOUT)) {
            rr = new MahoutRecommenderRunner(properties);
        } else if (properties.getProperty(FRAMEWORK).equals(LENSKIT)) {
            rr = new LenskitRecommenderRunner(properties);
        } else if (properties.getProperty(FRAMEWORK).equals(RANKSYS)) {
            rr = new RanksysRecommenderRunner(properties);
        }
        return rr;
    }

    /**
     * Write the system stats to file.
     *
     * @param path the path to write to
     * @param statLabel what statistics is being written
     * @param stat the value
     */
    public static void writeStats(final String path, final String statLabel, final long stat) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true), "UTF-8"));
            out.write(statLabel + "\t" + stat + "\n");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
