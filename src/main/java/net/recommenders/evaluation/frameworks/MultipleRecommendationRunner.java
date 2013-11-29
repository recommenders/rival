package net.recommenders.evaluation.frameworks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: alan
 * Date: 2013-11-26
 * Time: 15:25
 */
public class MultipleRecommendationRunner {
    public static final String input = "input";
    public static final String mahoutItemBasedRecs = "mahout.rec.ib";
    public static final String mahoutUserBasedRecs = "mahout.rec.ub";
    public static final String mahoutSimilarities = "mahout.sim";

    public static final String mahoutSVDRecs = "mahout.rec.svd";
    public static final String mahoutSVDFactorizer = "mahout.svd.factorizer";

    public static final String lenskitItemBasedRecs = "lenskit.rec.ib";
    public static final String lenskitUserBasedRecs = "lenskit.rec.ub";
    public static final String lenskitSimilarities = "lenskit.sim";

    public static final String lenskitSVDRecs = "lenskit.rec.svd";

    public static final String n = "neighborhood"; //also factors
    public static final String svdIter = "svd.iterations";
    public static final String output = "output";
    public static String[] neighborhoods;
    public static String[] svdIterations;
    public static String[] ibRecs;
    public static String[] ubRecs;
    public static String[] svdRecs;
    public static String[] similarities;

    public static Properties properties;
    public static ArrayList<String> paths;
    public static void main(String[] args) {
        paths = new ArrayList<String>();
        String propertyFile = System.getProperty("file");
        properties = new Properties();
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        for (Object pr : properties.stringPropertyNames())
            System.out.println((String)pr + " : " + properties.getProperty((String)pr));

        listAllFiles(properties.getProperty(input));
        neighborhoods = properties.getProperty(n).split(",");
        svdIterations = properties.getProperty(svdIter).split(",");


        runLenskitRecommeders();
        runMahoutRecommeders();

    }

    public static void runLenskitRecommeders(){
        try{
            ibRecs = properties.getProperty(lenskitItemBasedRecs).split(",");
            ubRecs = properties.getProperty(lenskitUserBasedRecs).split(",");
            svdRecs = properties.getProperty(lenskitSVDRecs).split(",");
            similarities = properties.getProperty(lenskitSimilarities).split(",");
        } catch (NullPointerException e){
            System.out.println("Properties not set");
            return;
        }

        for(String path : paths){
            Properties prop = new Properties();
            prop.setProperty(RecommendationRunner.trainingSet, path + ".train");
            prop.setProperty(RecommendationRunner.testSet, path + ".test");
            prop.setProperty(RecommendationRunner.output, properties.getProperty(output));
            prop.setProperty(RecommendationRunner.framework, "lenskit");
            for (String ubRec : ubRecs){
                prop.setProperty(RecommendationRunner.recommender, ubRec);
                for (String sim : similarities){
                    prop.setProperty(RecommendationRunner.similarity, sim);
                    for (String n : neighborhoods){
                        prop.setProperty(RecommendationRunner.neighborhood, n);
                        RecommendationRunner.recommend(prop);
                    }
                    prop.remove(RecommendationRunner.similarity);
//                    prop.remove(RecommendationRunner.neighborhood);
                }
            }
            for (String ibRec : ibRecs){
                prop.setProperty(RecommendationRunner.recommender, ibRec);
                for (String sim : similarities){
                    prop.setProperty(RecommendationRunner.similarity, sim);
                    RecommendationRunner.recommend(prop);
                    prop.remove(RecommendationRunner.similarity);
                }
            }
            for (String svdRec : svdRecs){
                prop.setProperty(RecommendationRunner.recommender, svdRec);
                for (String f : neighborhoods){
                    prop.setProperty(RecommendationRunner.factors, f);
                    RecommendationRunner.recommend(prop);
                }
                prop.remove(RecommendationRunner.factorizer);
            }
        }
    }

    public static void runMahoutRecommeders(){
        try{
            ibRecs = properties.getProperty(mahoutItemBasedRecs).split(",");
            ubRecs = properties.getProperty(mahoutUserBasedRecs).split(",");
            svdRecs = properties.getProperty(mahoutSVDRecs).split(",");

            similarities = properties.getProperty(mahoutSimilarities).split(",");

        } catch (NullPointerException e){
            System.out.println("Properties not set");
            return;
        }
        String[] factorizers = properties.getProperty(mahoutSVDFactorizer).split(",");

        for(String path : paths){
            Properties prop = new Properties();
            prop.setProperty(RecommendationRunner.trainingSet, path + ".train");
            prop.setProperty(RecommendationRunner.testSet, path + ".test");
            prop.setProperty(RecommendationRunner.output, properties.getProperty(output));
            prop.setProperty(RecommendationRunner.framework, "mahout");
            for (String ubRec : ubRecs){
                prop.setProperty(RecommendationRunner.recommender, ubRec);
                for (String sim : similarities){
                    prop.setProperty(RecommendationRunner.similarity, sim);
                    for (String n : neighborhoods){
                        prop.setProperty(RecommendationRunner.neighborhood, n);
                        RecommendationRunner.recommend(prop);
                    }
                    prop.remove(RecommendationRunner.similarity);
//                    prop.remove(RecommendationRunner.neighborhood);
                }
            }
            for (String ibRec : ibRecs){
                prop.setProperty(RecommendationRunner.recommender, ibRec);
                for (String sim : similarities){
                    prop.setProperty(RecommendationRunner.similarity, sim);
                    RecommendationRunner.recommend(prop);
                    prop.remove(RecommendationRunner.similarity);
                }
            }
            for (String svdRec : svdRecs){
                prop.setProperty(RecommendationRunner.recommender, svdRec);
                for (String fact : factorizers){
                    prop.setProperty(RecommendationRunner.factorizer, fact);
                    for (String f : neighborhoods){
                        prop.setProperty(RecommendationRunner.factors, f);
                        RecommendationRunner.recommend(prop);
                    }
                    prop.remove(RecommendationRunner.factorizer);

                }
            }
        }
    }

    public static void listAllFiles(String path){
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles){
            if (file.isDirectory())
                listAllFiles(file.getAbsolutePath());
            else if (file.getName().contains("train"))
                paths.add(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")));
        }
    }
}
