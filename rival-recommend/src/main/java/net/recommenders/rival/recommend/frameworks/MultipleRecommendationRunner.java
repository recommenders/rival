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
public final class MultipleRecommendationRunner {

    /**
     * Property key.
     */
    public static final String INPUT = "input";
    /**
     * Property key.
     */
    public static final String MAHOUT_ITEMBASED_RECS = "mahout.rec.ib";
    /**
     * Property key.
     */
    public static final String MAHOUT_USERBASED_RECS = "mahout.rec.ub";
    /**
     * Property key.
     */
    public static final String MAHOUT_SIMILARITIES = "mahout.sim";
    /**
     * Property key.
     */
    public static final String MAHOUT_SVD_RECS = "mahout.rec.svd";
    /**
     * Property key.
     */
    public static final String MAHOUT_SVD_FACTORIZER = "mahout.svd.factorizer";
    /**
     * Property key.
     */
    public static final String LENSKIT_ITEMBASED_RECS = "lenskit.rec.ib";
    /**
     * Property key.
     */
    public static final String LENSKIT_USERBASED_RECS = "lenskit.rec.ub";
    /**
     * Property key.
     */
    public static final String LENSKIT_SIMILARITIES = "lenskit.sim";
    /**
     * Property key.
     */
    public static final String LENSKIT_SVD_RECS = "lenskit.rec.svd";
    /**
     * Property key.
     */
    public static final String RANKSYS_ITEMBASED_RECS = "ranksys.rec.ib";
    /**
     * Property key.
     */
    public static final String RANKSYS_USERBASED_RECS = "ranksys.rec.ub";
    /**
     * Property key.
     */
    public static final String RANKSYS_SIMILARITIES = "ranksys.sim";
    /**
     * Property key.
     */
    public static final String RANKSYS_SVD_RECS = "ranksys.rec.svd";
    /**
     * Property key.
     */
    public static final String RANKSYS_SVD_FACTORIZER = "ranksys.svd.factorizer";
    /**
     * Property key.
     */
    public static final String N = "neighborhood"; //also factors
    /**
     * Property key.
     */
    public static final String SVD_ITER = "svd.iterations";
    /**
     * Property key.
     */
    public static final String OUTPUT = "output";

    /**
     * Utility classes should not have a public or default constructor.
     */
    private MultipleRecommendationRunner() {
    }

    /**
     * Instantiates and runs the runners based on input (property) files.
     *
     * @param args not used.
     */
    public static void main(final String[] args) {
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
        runRanksysRecommenders(paths, properties);
    }

    /**
     * Runs the Lenskit recommenders.
     *
     * @param paths the input and output paths.
     * @param properties the properties.
     */
    public static void runLenskitRecommenders(final Set<String> paths, final Properties properties) {
        for (AbstractRunner<Long, Long> rec : instantiateLenskitRecommenders(paths, properties)) {
            RecommendationRunner.run(rec);
        }
    }

    /**
     *
     * Instantiates recommenders based on the provided properties.
     *
     * @param paths the input and output paths.
     * @param properties the properties.
     * @return an array of recommenders, prepared to be run.
     */
    @SuppressWarnings("unchecked")
    public static AbstractRunner<Long, Long>[] instantiateLenskitRecommenders(final Set<String> paths, final Properties properties) {
        List<AbstractRunner<Long, Long>> recList = new ArrayList<AbstractRunner<Long, Long>>();
        try {
            String[] ibRecs = properties.getProperty(LENSKIT_ITEMBASED_RECS).split(",");
            String[] ubRecs = properties.getProperty(LENSKIT_USERBASED_RECS).split(",");
            String[] svdRecs = properties.getProperty(LENSKIT_SVD_RECS).split(",");
            String[] similarities = properties.getProperty(LENSKIT_SIMILARITIES).split(",");
            String[] neighborhoods = properties.getProperty(N).split(",");

            for (String path : paths) {
                Properties prop = new Properties();
                prop.setProperty(RecommendationRunner.TRAINING_SET, path + "_train.dat");
                prop.setProperty(RecommendationRunner.TEST_SET, path + "_test.dat");
                prop.setProperty(RecommendationRunner.OUTPUT, properties.getProperty(OUTPUT, ""));
                prop.setProperty(RecommendationRunner.FRAMEWORK, "lenskit");
                for (String ubRec : ubRecs) {
                    if (ubRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.RECOMMENDER, ubRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.SIMILARITY, sim);
                        for (String n : neighborhoods) {
                            prop.setProperty(RecommendationRunner.NEIGHBORHOOD, n);
                            AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                            recList.add(ar);
                        }
                        prop.remove(RecommendationRunner.SIMILARITY);
//                    prop.remove(RecommendationRunner.neighborhood);
                    }
                }
                for (String ibRec : ibRecs) {
                    if (ibRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.RECOMMENDER, ibRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.SIMILARITY, sim);
                        AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                        recList.add(ar);
                        prop.remove(RecommendationRunner.SIMILARITY);
                    }
                }
                for (String svdRec : svdRecs) {
                    if (svdRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.RECOMMENDER, svdRec);
                    for (String f : neighborhoods) {
                        prop.setProperty(RecommendationRunner.FACTORS, f);
                        AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                        recList.add(ar);
                    }
                    prop.remove(RecommendationRunner.FACTORIZER);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Properties not set (Lenskit recommenders)");
        }
        AbstractRunner<Long, Long>[] recs = recList.toArray(new AbstractRunner[0]);
        return recs;
    }

    /**
     * Runs Mahout-based recommenders.
     *
     * @param paths the input and output paths.
     * @param properties the properties.
     */
    public static void runMahoutRecommenders(final Set<String> paths, final Properties properties) {
        for (AbstractRunner<Long, Long> rec : instantiateMahoutRecommenders(paths, properties)) {
            RecommendationRunner.run(rec);
        }
    }

    /**
     *
     * Instantiates recommenders based on the provided properties.
     *
     * @param paths the input and output paths.
     * @param properties the properties.
     * @return an array of recommenders, prepared to be run.
     */
    @SuppressWarnings("unchecked")
    public static AbstractRunner<Long, Long>[] instantiateMahoutRecommenders(final Set<String> paths, final Properties properties) {
        List<AbstractRunner<Long, Long>> recList = new ArrayList<AbstractRunner<Long, Long>>();
        try {
            String[] ibRecs = properties.getProperty(MAHOUT_ITEMBASED_RECS).split(",");
            String[] ubRecs = properties.getProperty(MAHOUT_USERBASED_RECS).split(",");
            String[] svdRecs = properties.getProperty(MAHOUT_SVD_RECS).split(",");
            String[] similarities = properties.getProperty(MAHOUT_SIMILARITIES).split(",");
            String[] neighborhoods = properties.getProperty(N).split(",");

            String[] factorizers = properties.getProperty(MAHOUT_SVD_FACTORIZER).split(",");

            for (String path : paths) {
                Properties prop = new Properties();
                prop.setProperty(RecommendationRunner.TRAINING_SET, path + "_train.dat");
                prop.setProperty(RecommendationRunner.TEST_SET, path + "_test.dat");
                prop.setProperty(RecommendationRunner.OUTPUT, properties.getProperty(OUTPUT, ""));
                prop.setProperty(RecommendationRunner.FRAMEWORK, "mahout");
                // first IB because it (should) does not have neighborhood
                for (String ibRec : ibRecs) {
                    if (ibRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.RECOMMENDER, ibRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.SIMILARITY, sim);
                        AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                        recList.add(ar);
                        prop.remove(RecommendationRunner.SIMILARITY);
                    }
                }
                for (String ubRec : ubRecs) {
                    if (ubRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.RECOMMENDER, ubRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.SIMILARITY, sim);
                        for (String n : neighborhoods) {
                            prop.setProperty(RecommendationRunner.NEIGHBORHOOD, n);
                            AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                            recList.add(ar);
                        }
                        prop.remove(RecommendationRunner.SIMILARITY);
//                    prop.remove(RecommendationRunner.neighborhood);
                    }
                }
                for (String svdRec : svdRecs) {
                    if (svdRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.RECOMMENDER, svdRec);
                    for (String fact : factorizers) {
                        prop.setProperty(RecommendationRunner.FACTORIZER, fact);
                        for (String f : neighborhoods) {
                            prop.setProperty(RecommendationRunner.FACTORS, f);
                            AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                            recList.add(ar);
                        }
                        prop.remove(RecommendationRunner.FACTORIZER);

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
     * Runs Ranksys-based recommenders.
     *
     * @param paths the input and output paths.
     * @param properties the properties.
     */
    public static void runRanksysRecommenders(final Set<String> paths, final Properties properties) {
        for (AbstractRunner<Long, Long> rec : instantiateRanksysRecommenders(paths, properties)) {
            RecommendationRunner.run(rec);
        }
    }

    /**
     *
     * Instantiates recommenders based on the provided properties.
     *
     * @param paths the input and output paths.
     * @param properties the properties.
     * @return an array of recommenders, prepared to be run.
     */
    @SuppressWarnings("unchecked")
    public static AbstractRunner<Long, Long>[] instantiateRanksysRecommenders(final Set<String> paths, final Properties properties) {
        List<AbstractRunner<Long, Long>> recList = new ArrayList<AbstractRunner<Long, Long>>();
        try {
            String[] ibRecs = properties.getProperty(RANKSYS_ITEMBASED_RECS).split(",");
            String[] ubRecs = properties.getProperty(RANKSYS_USERBASED_RECS).split(",");
            String[] svdRecs = properties.getProperty(RANKSYS_SVD_RECS).split(",");
            String[] similarities = properties.getProperty(RANKSYS_SIMILARITIES).split(",");
            String[] neighborhoods = properties.getProperty(N).split(",");

            String[] factorizers = properties.getProperty(RANKSYS_SVD_FACTORIZER).split(",");

            for (String path : paths) {
                Properties prop = new Properties();
                prop.setProperty(RecommendationRunner.TRAINING_SET, path + "_train.dat");
                prop.setProperty(RecommendationRunner.TEST_SET, path + "_test.dat");
                prop.setProperty(RecommendationRunner.OUTPUT, properties.getProperty(OUTPUT, ""));
                prop.setProperty(RecommendationRunner.FRAMEWORK, "ranksys");
                // first IB because it (should) does not have neighborhood
                for (String ibRec : ibRecs) {
                    if (ibRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.RECOMMENDER, ibRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.SIMILARITY, sim);
                        AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                        recList.add(ar);
                        prop.remove(RecommendationRunner.SIMILARITY);
                    }
                }
                for (String ubRec : ubRecs) {
                    if (ubRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.RECOMMENDER, ubRec);
                    for (String sim : similarities) {
                        prop.setProperty(RecommendationRunner.SIMILARITY, sim);
                        for (String n : neighborhoods) {
                            prop.setProperty(RecommendationRunner.NEIGHBORHOOD, n);
                            AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                            recList.add(ar);
                        }
                        prop.remove(RecommendationRunner.SIMILARITY);
//                    prop.remove(RecommendationRunner.neighborhood);
                    }
                }
                for (String svdRec : svdRecs) {
                    if (svdRec.trim().isEmpty()) {
                        continue;
                    }
                    prop.setProperty(RecommendationRunner.RECOMMENDER, svdRec);
                    for (String fact : factorizers) {
                        prop.setProperty(RecommendationRunner.FACTORIZER, fact);
                        for (String f : neighborhoods) {
                            prop.setProperty(RecommendationRunner.FACTORS, f);
                            AbstractRunner<Long, Long> ar = RecommendationRunner.instantiateRecommender(prop);
                            recList.add(ar);
                        }
                        prop.remove(RecommendationRunner.FACTORIZER);

                    }
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Properties not set (Ranksys recommenders)");
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
    public static void listAllFiles(final Set<String> setOfPaths, final String inputPath) {
        if (inputPath == null) {
            return;
        }
        File[] files = new File(inputPath).listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                listAllFiles(setOfPaths, file.getAbsolutePath());
            } else if (file.getName().contains("_train.dat")) {
                setOfPaths.add(file.getAbsolutePath().replaceAll("_train.dat", ""));
            }
        }
    }
}
