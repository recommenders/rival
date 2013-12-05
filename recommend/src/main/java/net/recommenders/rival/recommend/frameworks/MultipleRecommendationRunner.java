package net.recommenders.rival.recommend.frameworks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class MultipleRecommendationRunner {

    public static final String INPUT = "input";
    public static final String MAHOUT_ITEMBASED_RECS = "mahout.rec.ib";
    public static final String MAHOUT_USERBASED_RECS = "mahout.rec.ub";
    public static final String MAHOUT_SIMILARITIES = "mahout.sim";
    public static final String MAHOUT_SVD_RECS = "mahout.rec.svd";
    public static final String MAHOUT_SVD_FACTORIZER = "mahout.svd.factorizer";
    public static final String LENSKIT_ITEMBASED_RECS = "lenskit.rec.ib";
    public static final String LENSKIT_USERBASED_RECS = "lenskit.rec.ub";
    public static final String LENSKIT_SIMILARITIES = "lenskit.sim";
    public static final String LENSKIT_SVD_RECS = "lenskit.rec.svd";
    public static final String N = "neighborhood"; //also factors
    public static final String SVD_ITER = "svd.iterations";
    public static final String OUTPUT = "output";

    public static void main(String[] args) {
        final Set<String> paths = new HashSet<String>();
        final String propertyFile = System.getProperty("file");
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String pr : properties.stringPropertyNames()) {
            System.out.println(pr + " : " + properties.getProperty(pr));
        }

        listAllFiles(paths, properties.getProperty(INPUT));
        String[] neighborhoods = properties.getProperty(N).split(",");
        String[] svdIterations = properties.getProperty(SVD_ITER).split(",");

        runLenskitRecommenders(paths, properties, neighborhoods, svdIterations);
        runMahoutRecommenders(paths, properties, neighborhoods, svdIterations);
    }

    public static void runLenskitRecommenders(Set<String> paths, Properties properties, String[] neighborhoods, String[] svdIterations) {
        try {
            String[] ibRecs = properties.getProperty(LENSKIT_ITEMBASED_RECS).split(",");
            String[] ubRecs = properties.getProperty(LENSKIT_USERBASED_RECS).split(",");
            String[] svdRecs = properties.getProperty(LENSKIT_SVD_RECS).split(",");
            String[] similarities = properties.getProperty(LENSKIT_SIMILARITIES).split(",");

            for (String path : paths) {
                Properties prop = new Properties();
                prop.setProperty(RecommendationRunner.trainingSet, path + ".train");
                prop.setProperty(RecommendationRunner.testSet, path + ".test");
                prop.setProperty(RecommendationRunner.output, properties.getProperty(OUTPUT));
                prop.setProperty(RecommendationRunner.framework, "lenskit");
                for (String ubRec : ubRecs) {
                    prop.setProperty(RecommendationRunner.recommender, ubRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.similarity, sim);
                        for (String n : neighborhoods) {
                            prop.setProperty(RecommendationRunner.neighborhood, n);
                            RecommendationRunner.recommend(prop);
                        }
                        prop.remove(RecommendationRunner.similarity);
//                    prop.remove(RecommendationRunner.neighborhood);
                    }
                }
                for (String ibRec : ibRecs) {
                    prop.setProperty(RecommendationRunner.recommender, ibRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.similarity, sim);
                        RecommendationRunner.recommend(prop);
                        prop.remove(RecommendationRunner.similarity);
                    }
                }
                for (String svdRec : svdRecs) {
                    prop.setProperty(RecommendationRunner.recommender, svdRec);
                    for (String f : neighborhoods) {
                        prop.setProperty(RecommendationRunner.factors, f);
                        RecommendationRunner.recommend(prop);
                    }
                    prop.remove(RecommendationRunner.factorizer);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Properties not set");
            return;
        }
    }

    public static void runMahoutRecommenders(Set<String> paths, Properties properties, String[] neighborhoods, String[] svdIterations) {
        try {
            String[] ibRecs = properties.getProperty(MAHOUT_ITEMBASED_RECS).split(",");
            String[] ubRecs = properties.getProperty(MAHOUT_USERBASED_RECS).split(",");
            String[] svdRecs = properties.getProperty(MAHOUT_SVD_RECS).split(",");
            String[] similarities = properties.getProperty(MAHOUT_SIMILARITIES).split(",");

            String[] factorizers = properties.getProperty(MAHOUT_SVD_FACTORIZER).split(",");

            for (String path : paths) {
                Properties prop = new Properties();
                prop.setProperty(RecommendationRunner.trainingSet, path + ".train");
                prop.setProperty(RecommendationRunner.testSet, path + ".test");
                prop.setProperty(RecommendationRunner.output, properties.getProperty(OUTPUT));
                prop.setProperty(RecommendationRunner.framework, "mahout");
                for (String ubRec : ubRecs) {
                    prop.setProperty(RecommendationRunner.recommender, ubRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.similarity, sim);
                        for (String n : neighborhoods) {
                            prop.setProperty(RecommendationRunner.neighborhood, n);
                            RecommendationRunner.recommend(prop);
                        }
                        prop.remove(RecommendationRunner.similarity);
//                    prop.remove(RecommendationRunner.neighborhood);
                    }
                }
                for (String ibRec : ibRecs) {
                    prop.setProperty(RecommendationRunner.recommender, ibRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.similarity, sim);
                        RecommendationRunner.recommend(prop);
                        prop.remove(RecommendationRunner.similarity);
                    }
                }
                for (String svdRec : svdRecs) {
                    prop.setProperty(RecommendationRunner.recommender, svdRec);
                    for (String fact : factorizers) {
                        prop.setProperty(RecommendationRunner.factorizer, fact);
                        for (String f : neighborhoods) {
                            prop.setProperty(RecommendationRunner.factors, f);
                            RecommendationRunner.recommend(prop);
                        }
                        prop.remove(RecommendationRunner.factorizer);

                    }
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Properties not set");
            return;
        }
    }

    public static void listAllFiles(Set<String> paths, String path) {
        for (File file : new File(path).listFiles()) {
            if (file.isDirectory()) {
                listAllFiles(paths, file.getAbsolutePath());
            } else if (file.getName().contains("train")) {
                paths.add(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")));
            }
        }
    }
}
