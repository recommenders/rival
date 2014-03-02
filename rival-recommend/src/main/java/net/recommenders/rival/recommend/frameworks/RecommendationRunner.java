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
 * @author Alan
 */
public class RecommendationRunner {

    public static final String recommender = "recommender";
    public static final String similarity = "similarity";
    public static final String factorizer = "factorizer";
    public static final String neighborhood = "neighborhood";
    public static final String factors = "factors";
    public static final String iterations = "iterations";
    public static final String trainingSet = "training";
    public static final String testSet = "test";
    public static final String output = "output";
    public static final String framework = "framework";
    public static final String MAHOUT = "mahout";
    public static final String LENSKIT = "lenskit";
    public static String statPath;
    public static long time;

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
