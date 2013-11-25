/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.frameworks;

import net.recommenders.evaluation.frameworks.lenskit.LenskitRecommenderRunner;
import net.recommenders.evaluation.frameworks.mahout.MahoutRecommenderRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author alejandr
 */


public class Recommend {

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



    public static void main(String[] args) {
        //String test = "/Users/alan/Documents/workspace/data/movielens1m100k/test.tsv";
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

        if(properties.getProperty(recommender) == null){
            System.out.println("No recommenderClass specified, exiting.");
            System.exit(0);
        }
        if (properties.getProperty(trainingSet) == null){
            System.out.println("No training set specified, exiting.");
            System.exit(0);
        }
        if (properties.getProperty(testSet) == null){
            System.out.println("No training set specified, exiting.");
            System.exit(0);
        }

        if (properties.getProperty(framework).equals(MAHOUT)){
            MahoutRecommenderRunner rr = new MahoutRecommenderRunner(properties);
            try{
                rr.runRecommender();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (properties.getProperty(framework).equals(LENSKIT)){
            System.out.println("recommend");
            LenskitRecommenderRunner rr = new LenskitRecommenderRunner(properties);
            try {
                rr.runRecommender();
            } catch (IOException e){
                e.printStackTrace();
            }

        }

    }


}
