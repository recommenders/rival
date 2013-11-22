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

    public static final String recommender = "recommenderClass";
    public static final String similarity = "similarityClass";
    public static final String factorizer = "factorizerClass";
    public static final String neighborhood = "neighborhoodClass";
    public static final String factors = "factors";
    public static final String iterations = "iterations";
    public static final String trainingSet = "training";
    public static final String testSet = "test";
    public static final String output = "outputPath";
    public static final String framework = "framework";
    public static final String MAHOUT = "mahout";
    public static final String LENSKIT = "lenskit";



    public static void main(String[] args) {
        //String test = "/Users/alan/Documents/workspace/data/movielens1m100k/test.tsv";
        String propertyFile = System.getProperty("file");


        final Properties properties = new Properties();
        try{
            properties.load(new FileInputStream(propertyFile));
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException ie){
            ie.printStackTrace();
        }
        Map<String, String> params = new HashMap<String, String>();

        if(properties.getProperty(recommender) != null)
            params.put(recommender, properties.getProperty(recommender));
        else{
            System.out.println("No recommenderClass specified, exiting.");
            System.exit(0);
        }
        if (properties.getProperty(trainingSet) != null)
            params.put(trainingSet, properties.getProperty(trainingSet));
        else {
            System.out.println("No training set specified, exiting.");
            System.exit(0);
        }
        if (properties.getProperty(testSet) != null)
            params.put(testSet, properties.getProperty(testSet));
        else {
            System.out.println("No training set specified, exiting.");
            System.exit(0);
        }
        if (properties.getProperty(similarity) != null)
            params.put(similarity, properties.getProperty(similarity));
        if (properties.getProperty(neighborhood) != null)
            params.put(neighborhood, properties.getProperty(neighborhood));
        if (properties.getProperty(factorizer) != null)
            params.put(factorizer, properties.getProperty(factorizer));
        if (properties.getProperty(factors) != null)
            params.put(factors, properties.getProperty(factors));
        if (properties.getProperty(iterations) != null)
            params.put(iterations, properties.getProperty(iterations));
        if (properties.getProperty(output) != null)
            params.put(output, properties.getProperty(output)+"/"+properties.getProperty(framework));


        if (properties.getProperty(framework).equals(MAHOUT)){
            MahoutRecommenderRunner rr = new MahoutRecommenderRunner();
            rr.setParameters(params);
            try{
                rr.runRecommender();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (properties.getProperty(framework).equals(LENSKIT)){
            System.out.println("recommend");
            LenskitRecommenderRunner rr = new LenskitRecommenderRunner();
            rr.setParameters(params);
            try {
                rr.runRecommender();
            } catch (IOException e){
                e.printStackTrace();
            }

        }

    }


}
