package net.recommenders.rival.recommend.frameworks;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import net.recommenders.rival.recommend.frameworks.lenskit.LenskitRecommenderRunner;
import net.recommenders.rival.recommend.frameworks.mahout.MahoutRecommenderRunner;

import java.util.Properties;

/**
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
public class RecommendationRunner {

    /**
     * The property key for the recommender
     */
    public static final String recommender = "recommender";
    /**
     * The property key for the similarity
     */
    public static final String similarity = "similarity";
    /**
     * The property key for the factorizer
     */
    public static final String factorizer = "factorizer";
    /**
     * The property key for the neighborhood
     */
    public static final String neighborhood = "neighborhood";
    /**
     * The property key for the factors
     */
    public static final String factors = "factors";
    /**
     * The property key for the iterations
     */
    public static final String iterations = "iterations";
    /**
     * The property key for the training set
     */
    public static final String trainingSet = "training";
    /**
     * The property key for the test set
     */
    public static final String testSet = "test";
    /**
     * The property key for the output
     */
    public static final String output = "output";
    /**
     * The property key for the framework
     */
    public static final String framework = "framework";
    /**
     * The property key for Mahout
     */
    public static final String MAHOUT = "mahout";
    /**
     * The property key for LensKit
     */
    public static final String LENSKIT = "lenskit";
    /**
     * The canonical path
     */
    public static String statPath;
    /**
     * The execution time
     */
    public static long time;

    /**
     * Main method for running a recommendation.
     * @param args  CLI arguments
     */
    public static void main(String[] args) {
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
     * Run recommendations based on properties
     * @param properties    the properties
     */
    public static void recommend(Properties properties) {
        if (properties.getProperty(recommender) == null) {
            System.out.println("No recommenderClass specified, exiting.");
            return;
        }
        if (properties.getProperty(trainingSet) == null) {
            System.out.println("No training set specified, exiting.");
            return;
        }
        if (properties.getProperty(testSet) == null) {
            System.out.println("No training set specified, exiting.");
            return;
        }
        time = System.currentTimeMillis();

        AbstractRunner rr = null;
        boolean statsExist = false;
        if (properties.getProperty(framework).equals(MAHOUT)) {
            rr = new MahoutRecommenderRunner(properties);
        } else if (properties.getProperty(framework).equals(LENSKIT)) {
            rr = new LenskitRecommenderRunner(properties);
        }
        statPath = rr.getCanonicalFileName();
        statsExist = rr.getAlreadyRecommended();
        try {
            rr.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        if (!statsExist) {
            writeStats(statPath, "time", time);
        }
    }

    /**
     * Write the system stats to file
     * @param path  the path to write to
     * @param statLabel what statistics is being written
     * @param stat  the value
     */
    public static void writeStats(String path, String statLabel, long stat) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path, true));
            out.write(statLabel + "\t" + stat + "\n");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
