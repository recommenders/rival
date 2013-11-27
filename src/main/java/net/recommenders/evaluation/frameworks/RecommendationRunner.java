/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.frameworks;

import net.recommenders.evaluation.frameworks.lenskit.LenskitRecommenderRunner;
import net.recommenders.evaluation.frameworks.mahout.MahoutRecommenderRunner;

import java.io.*;
import java.util.Properties;

/**
 *
 * @author alejandr
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
        if(propertyFile == null){
            System.out.println("Property file not given, exiting.");
            System.exit(0);
        }
        final Properties properties = new Properties();
        try{
            properties.load(new FileInputStream(propertyFile));
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException ie){
            ie.printStackTrace();
        }
        recommend(properties);
    }

    public static void recommend(Properties properties){
        if(properties.getProperty(recommender) == null){
            System.out.println("No recommenderClass specified, exiting.");
            return;
        }
        if (properties.getProperty(trainingSet) == null){
            System.out.println("No training set specified, exiting.");
            return;
        }
        if (properties.getProperty(testSet) == null){
            System.out.println("No training set specified, exiting.");
            return;
        }
        time = System.currentTimeMillis();

        AbstractRunner rr;
        if (properties.getProperty(framework).equals(MAHOUT)){
            rr = new MahoutRecommenderRunner(properties);
            statPath = rr.getCanonicalFileName();
            try{
                rr.run();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (properties.getProperty(framework).equals(LENSKIT)){
            rr = new LenskitRecommenderRunner(properties);
            statPath = rr.getCanonicalFileName();
            try {
                rr.run();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        time = System.currentTimeMillis() - time;
        writeStats(statPath, "time", time);
        writeStats(statPath, "memory", memory);
    }

    public static void writeStats(String path, String statLabel, long stat){
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path, true));
            out.write(statLabel + "\t" + stat + "\n");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}
