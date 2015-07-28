package net.recommenders.rival.recommend.frameworks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Runner of multiple recommenders.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>
 */
public class MultipleRecommendationRunner {

    /**
     * Property keys.
     */
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

    /**
     * Instantiates the runners based on input (property) files.
     *
     * @param args not used.
     */
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

        runLenskitRecommenders(paths, properties);
        runMahoutRecommenders(paths, properties);
    }

    /**
     * Runs the Lenskit recommenders created in @main.
     *
     * @param paths the input and output paths.
     * @param properties the properties.
     * @param neighborhoods the different neihborhood types to instantiate.
     * @param svdIterations the number of iterations that should be performed in
     * SVD-based recommenders.
     */
    public static void runLenskitRecommenders(Set<String> paths, Properties properties) {
        for (AbstractRunner<Long, Long> rec : instantiateLenskitRecommenders(paths, properties)) {
            RecommendationRunner.run(rec);
        }
    }

    public static AbstractRunner<Long, Long>[] instantiateLenskitRecommenders(Set<String> paths, Properties properties) {
        List<AbstractRunner<Long, Long>> recList = new ArrayList();
        try {
            String[] ibRecs = properties.getProperty(LENSKIT_ITEMBASED_RECS).split(",");
            String[] ubRecs = properties.getProperty(LENSKIT_USERBASED_RECS).split(",");
            String[] svdRecs = properties.getProperty(LENSKIT_SVD_RECS).split(",");
            String[] similarities = properties.getProperty(LENSKIT_SIMILARITIES).split(",");
            String[] neighborhoods = properties.getProperty(N).split(",");

            for (String path : paths) {
                Properties prop = new Properties();
                prop.setProperty(RecommendationRunner.trainingSet, path + "_train.dat");
                prop.setProperty(RecommendationRunner.testSet, path + "_test.dat");
                prop.setProperty(RecommendationRunner.output, properties.getProperty(OUTPUT, ""));
                prop.setProperty(RecommendationRunner.framework, "lenskit");
                for (String ubRec : ubRecs) {
                    if (ubRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.recommender, ubRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.similarity, sim);
                        for (String n : neighborhoods) {
                            prop.setProperty(RecommendationRunner.neighborhood, n);
                            AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                            recList.add(ar);
                        }
                        prop.remove(RecommendationRunner.similarity);
//                    prop.remove(RecommendationRunner.neighborhood);
                    }
                }
                for (String ibRec : ibRecs) {
                    if (ibRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.recommender, ibRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.similarity, sim);
                        AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                        recList.add(ar);
                        prop.remove(RecommendationRunner.similarity);
                    }
                }
                for (String svdRec : svdRecs) {
                    if (svdRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.recommender, svdRec);
                    for (String f : neighborhoods) {
                        prop.setProperty(RecommendationRunner.factors, f);
                        AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                        recList.add(ar);
                    }
                    prop.remove(RecommendationRunner.factorizer);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Properties not set (Lenskit recommenders)");
        }
        AbstractRunner<Long, Long>[] recs = recList.toArray(new AbstractRunner[0]);
        return recs;
    }

    /**
     * Runs Mahout-based recommender created in @main.
     *
     * @param paths the input and output paths.
     * @param properties the properties.
     * @param neighborhoods the different neihborhood types to instantiate.
     * @param svdIterations the number of iterations that should be performed in
     * SVD-based recommenders.
     */
    public static void runMahoutRecommenders(Set<String> paths, Properties properties) {
        for (AbstractRunner<Long, Long> rec : instantiateMahoutRecommenders(paths, properties)) {
            RecommendationRunner.run(rec);
        }
    }

    public static AbstractRunner<Long, Long>[] instantiateMahoutRecommenders(Set<String> paths, Properties properties) {
        List<AbstractRunner<Long, Long>> recList = new ArrayList();
        try {
            String[] ibRecs = properties.getProperty(MAHOUT_ITEMBASED_RECS).split(",");
            String[] ubRecs = properties.getProperty(MAHOUT_USERBASED_RECS).split(",");
            String[] svdRecs = properties.getProperty(MAHOUT_SVD_RECS).split(",");
            String[] similarities = properties.getProperty(MAHOUT_SIMILARITIES).split(",");
            String[] neighborhoods = properties.getProperty(N).split(",");

            String[] factorizers = properties.getProperty(MAHOUT_SVD_FACTORIZER).split(",");

            for (String path : paths) {
                Properties prop = new Properties();
                prop.setProperty(RecommendationRunner.trainingSet, path + "_train.dat");
                prop.setProperty(RecommendationRunner.testSet, path + "_test.dat");
                prop.setProperty(RecommendationRunner.output, properties.getProperty(OUTPUT, ""));
                prop.setProperty(RecommendationRunner.framework, "mahout");
                // first IB because it (should) does not have neighborhood
                for (String ibRec : ibRecs) {
                    if (ibRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.recommender, ibRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.similarity, sim);
                        AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                        recList.add(ar);
                        prop.remove(RecommendationRunner.similarity);
                    }
                }
                for (String ubRec : ubRecs) {
                    if (ubRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.recommender, ubRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.similarity, sim);
                        for (String n : neighborhoods) {
                            prop.setProperty(RecommendationRunner.neighborhood, n);
                            AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                            recList.add(ar);
                        }
                        prop.remove(RecommendationRunner.similarity);
//                    prop.remove(RecommendationRunner.neighborhood);
                    }
                }
                for (String svdRec : svdRecs) {
                    if (svdRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.recommender, svdRec);
                    for (String fact : factorizers) {
                        prop.setProperty(RecommendationRunner.factorizer, fact);
                        for (String f : neighborhoods) {
                            prop.setProperty(RecommendationRunner.factors, f);
                            AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                            recList.add(ar);
                        }
                        prop.remove(RecommendationRunner.factorizer);

                    }
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Properties not set (Mahout recommenders)");
        }
        AbstractRunner<Long, Long>[] recs = recList.toArray(new AbstractRunner[0]);
        return recs;
    }

    /**
     * List all files at a certain path.
     *
     * @param setOfPaths the set of files at a certain path
     * @param inputPath the path to check
     */
    public static void listAllFiles(Set<String> setOfPaths, String inputPath) {
        for (File file : new File(inputPath).listFiles()) {
            if (file.isDirectory()) {
                listAllFiles(setOfPaths, file.getAbsolutePath());
            } else if (file.getName().contains("_train.dat")) {
                setOfPaths.add(file.getAbsolutePath().replaceAll("_train.dat", ""));
            }
        }
    }
}
