package net.recommenders.rival.evaluation.strategy;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.SimpleParser;

/**
 * Runner of multiple evaluation strategies.
 *
 * @author Alejandro
 */
public class MultipleStrategyRunner {

    public static final String SPLITS_FOLDER = "split.folder";
    public static final String TRAINING_SUFFIX = "split.training.suffix";
    public static final String TEST_SUFFIX = "split.test.suffix";
    public static final String RECOMMENDATION_FOLDER = "recommendation.folder";
    public static final String RECOMMENDATION_SUFFIX = "recommendation.suffix";
    public static final String OUTPUT_FORMAT = "output.format";
    public static final String OUTPUT_FOLDER = "output.ranking.folder";
    public static final String GROUNDTRUTH_FOLDER = "output.groundtruth.folder";
    public static final String STRATEGIES = "strategy.classes";
    public static final String RELEVANCE_THRESHOLDS = "strategy.relevance.thresholds";
    public static final String RELPLUSN_N = "strategy.relplusn.N";
    public static final String RELPLUSN_SEED = "strategy.relplusn.seed";

    /**
     * Main class.
     *
     * @param args Input arguments.
     * @throws Exception if no properties can be read.
     */
    public static void main(String[] args) throws Exception {
        String propertyFile = System.getProperty("propertyFile");

        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        for (String pr : properties.stringPropertyNames()) {
            System.out.println(pr + " : " + properties.getProperty(pr));
        }

        run(properties);
    }

    /**
     * Run a strategy.
     *
     * @param properties The properties of the strategy to run.
     * @throws IOException If files cannot be read.
     * @throws ClassNotFoundException If class does not exist.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static void run(Properties properties) throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // get splits
        File splitsFolder = new File(properties.getProperty(SPLITS_FOLDER));
        String trainingSuffix = properties.getProperty(TRAINING_SUFFIX);
        String testSuffix = properties.getProperty(TEST_SUFFIX);
        Set<String> splits = new HashSet<String>();
        getAllSplits(splits, splitsFolder, trainingSuffix, testSuffix);
        // read more parameters
        File inputFolder = new File(properties.getProperty(RECOMMENDATION_FOLDER));
        String inputSuffix = properties.getProperty(RECOMMENDATION_SUFFIX);
        File rankingFolder = new File(properties.getProperty(OUTPUT_FOLDER));
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(StrategyRunner.OUTPUT_OVERWRITE, "true"));
        File groundtruthFolder = new File(properties.getProperty(GROUNDTRUTH_FOLDER));
        EvaluationStrategy.OUTPUT_FORMAT format = properties.getProperty(OUTPUT_FORMAT).equals(EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL.toString()) ? EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL : EvaluationStrategy.OUTPUT_FORMAT.SIMPLE;
        String[] thresholds = properties.getProperty(RELEVANCE_THRESHOLDS).split(",");
        String[] strategyClassNames = properties.getProperty(STRATEGIES).split(",");
        // process info for each split
        for (String split : splits) {
            File trainingFile = new File(split + trainingSuffix);
            System.out.println("Parsing started: training file" + trainingFile);
            DataModel<Long, Long> trainingModel = new SimpleParser().parseData(trainingFile);
            System.out.println("Parsing finished: training file ");
            File testFile = new File(split + testSuffix);
            System.out.println("Parsing started: test file" + testFile);
            DataModel<Long, Long> testModel = new SimpleParser().parseData(testFile);
            System.out.println("Parsing finished: test file");
            Set<String> recommendationFiles = new HashSet<String>();
            getAllRecommendationFiles(recommendationFiles, inputFolder, new File(split).getName(), inputSuffix);
            for (String inputFile : recommendationFiles) {
                System.out.println("Recommendation file: " + inputFile);
                String inputFileName = new File(inputFile).getName();
                // read recommendations: user \t item \t score
                final Map<Long, List<EvaluationStrategy.Pair<Long, Double>>> mapUserRecommendations = new HashMap<Long, List<EvaluationStrategy.Pair<Long, Double>>>();
                BufferedReader in = new BufferedReader(new FileReader(inputFile));
                String line = null;
                while ((line = in.readLine()) != null) {
                    StrategyRunner.readLine(line, mapUserRecommendations);
                }
                in.close();
                // generate output for each strategy
                for (String strategyClassName : strategyClassNames) {
                    Class<?> strategyClass = Class.forName(strategyClassName);
                    for (String threshold : thresholds) {
                        System.out.println("Generating " + strategyClassName + " with threshold " + threshold);
                        // get strategy and generate output
                        if (strategyClassName.contains("RelPlusN")) {
                            String[] Ns = properties.getProperty(RELPLUSN_N).split(",");
                            String[] seeds = properties.getProperty(RELPLUSN_SEED).split(",");
                            for (String N : Ns) {
                                for (String seed : seeds) {
                                    EvaluationStrategy<Long, Long> strategy = new RelPlusN(trainingModel, testModel, Integer.parseInt(N), Double.parseDouble(threshold), Long.parseLong(seed));
                                    generateOutput(testModel, mapUserRecommendations, strategy, format, rankingFolder, groundtruthFolder, inputFileName, strategyClass.getSimpleName(), threshold, "__" + N + "__" + seed, overwrite);
                                }
                            }
                        } else {
                            EvaluationStrategy<Long, Long> strategy = (EvaluationStrategy<Long, Long>) strategyClass.getConstructor(DataModel.class, DataModel.class, double.class).newInstance(trainingModel, testModel, Double.parseDouble(threshold));
                            generateOutput(testModel, mapUserRecommendations, strategy, format, rankingFolder, groundtruthFolder, inputFileName, strategyClass.getSimpleName(), threshold, "", overwrite);
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate the output to write.
     *
     * @param testModel The test datamodel.
     * @param mapUserRecommendations A map with the recommendations for the
     * users.
     * @param strategy The strategy to use.
     * @param format The format of the printer
     * @param rankingFolder Where to write output.
     * @param groundtruthFolder Where to read test set.
     * @param inputFileName The file names to read.
     * @param strategyClassSimpleName The class name of the strategy.
     * @param threshold The relevance threshold.
     * @param suffix The file suffix.
     * @param overwrite Whether or not to overwrite the results file.
     * @throws FileNotFoundException if file does not exist.
     */
    public static void generateOutput(final DataModel<Long, Long> testModel, final Map<Long, List<EvaluationStrategy.Pair<Long, Double>>> mapUserRecommendations, EvaluationStrategy<Long, Long> strategy, EvaluationStrategy.OUTPUT_FORMAT format, File rankingFolder, File groundtruthFolder, String inputFileName, String strategyClassSimpleName, String threshold, String suffix, Boolean overwrite) throws FileNotFoundException {
        File outRanking = new File(rankingFolder, "out" + "__" + inputFileName + "__" + strategyClassSimpleName + "__" + threshold + suffix);
        File outGroundtruth = new File(groundtruthFolder, "gr" + "__" + inputFileName + "__" + strategyClassSimpleName + "__" + threshold + suffix);
        StrategyRunner.generateOutput(testModel, mapUserRecommendations, strategy, format, outRanking, outGroundtruth, overwrite);
    }

    /**
     * Get all training/test splits.
     *
     * @param splits The splits.
     * @param path The path where the splits are.
     * @param trainingSuffix The suffix of the training files.
     * @param testSuffix The suffix of the test files.
     */
    public static void getAllSplits(Set<String> splits, File path, String trainingSuffix, String testSuffix) {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                getAllSplits(splits, file, trainingSuffix, testSuffix);
            } else if (file.getName().endsWith(trainingSuffix)) {
                splits.add(file.getAbsolutePath().replaceAll(trainingSuffix + "$", ""));
            } else if (file.getName().endsWith(testSuffix)) {
                splits.add(file.getAbsolutePath().replaceAll(testSuffix + "$", ""));
            }
        }
    }

    /**
     * Get all recommendation files.
     *
     * @param recommendationFiles The recommendation files (what is this?)
     * @param path The path of the recommendation files.
     * @param prefix The prefix of the recommendation files.
     * @param suffix The suffix of the recommendation files.
     */
    public static void getAllRecommendationFiles(Set<String> recommendationFiles, File path, String prefix, String suffix) {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                getAllRecommendationFiles(recommendationFiles, file, prefix, suffix);
            } else if (file.getName().startsWith(prefix) && file.getName().endsWith(suffix)) {
                recommendationFiles.add(file.getAbsolutePath());
            }
        }
    }
}
