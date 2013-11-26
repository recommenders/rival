package net.recommenders.evaluation.strategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.recommenders.evaluation.core.DataModel;
import net.recommenders.evaluation.parser.SimpleParser;
import net.recommenders.evaluation.strategy.EvaluationStrategy.Pair;

/**
 *
 * @author Alejandro
 */
public class StrategyRunner {

    public static final String TRAINING_FILE = "split.training.file";
    public static final String TEST_FILE = "split.test.file";
    public static final String INPUT_FILE = "recommendation.file";
    public static final String OUTPUT_FORMAT = "output.format";
    public static final String OUTPUT_FILE = "output.file.ranking";
    public static final String GROUNDTRUTH_FILE = "output.file.groundtruth";
    public static final String STRATEGY = "strategy.class";
    public static final String RELEVANCE_THRESHOLD = "strategy.relevance.threshold";
    public static final String KOREN_N = "strategy.koren.N";
    public static final String KOREN_SEED = "strategy.koren.seed";

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

        run(properties);
    }

    public static void run(Properties properties) throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // read splits
        System.out.println("Parsing started: training file");
        File trainingFile = new File(properties.getProperty(TRAINING_FILE));
        DataModel<Long, Long> trainingModel = new SimpleParser().parseData(trainingFile);
        System.out.println("Parsing finished: training file");
        System.out.println("Parsing started: test file");
        File testFile = new File(properties.getProperty(TEST_FILE));
        DataModel<Long, Long> testModel = new SimpleParser().parseData(testFile);
        System.out.println("Parsing finished: test file");
        // read other parameters
        File inputFile = new File(properties.getProperty(INPUT_FILE));
        File rankingFile = new File(properties.getProperty(OUTPUT_FILE));
        File groundtruthFile = new File(properties.getProperty(GROUNDTRUTH_FILE));
        EvaluationStrategy.OUTPUT_FORMAT format = properties.getProperty(OUTPUT_FORMAT).equals(EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL.toString()) ? EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL : EvaluationStrategy.OUTPUT_FORMAT.SIMPLE;
        Double threshold = Double.parseDouble(properties.getProperty(RELEVANCE_THRESHOLD));
        String strategyClassName = properties.getProperty(STRATEGY);
        Class<?> strategyClass = Class.forName(strategyClassName);
        // get strategy
        EvaluationStrategy<Long, Long> strategy = null;
        if (strategyClassName.contains("Koren")) {
            Integer N = Integer.parseInt(properties.getProperty(KOREN_N));
            Long seed = Long.parseLong(properties.getProperty(KOREN_SEED));
            strategy = new Koren(trainingModel, testModel, N, threshold, seed);
        } else {
            strategy = (EvaluationStrategy<Long, Long>) strategyClass.getConstructor(DataModel.class, DataModel.class, double.class).newInstance(trainingModel, testModel, threshold);
        }
        // read recommendations: user \t item \t score
        final Map<Long, List<Pair<Long, Double>>> mapUserRecommendations = new HashMap<Long, List<Pair<Long, Double>>>();
        BufferedReader in = new BufferedReader(new FileReader(inputFile));
        String line = null;
        while ((line = in.readLine()) != null) {
            String[] toks = line.split("\t");
            Long user = Long.parseLong(toks[0]);
            Long item = Long.parseLong(toks[1]);
            Double score = Double.parseDouble(toks[2]);
            List<Pair<Long, Double>> userRec = mapUserRecommendations.get(user);
            if (userRec == null) {
                userRec = new ArrayList<Pair<Long, Double>>();
                mapUserRecommendations.put(user, userRec);
            }
            userRec.add(new Pair<Long, Double>(item, score));
        }
        in.close();
        // generate output
        PrintStream outRanking = new PrintStream(rankingFile);
        PrintStream outGroundtruth = new PrintStream(groundtruthFile);
        for (Long user : testModel.getUsers()) {
            final List<Pair<Long, Double>> allScoredItems = mapUserRecommendations.get(user);
            final Set<Long> items = strategy.getCandidateItemsToRank(user);
            final List<Pair<Long, Double>> scoredItems = new ArrayList<Pair<Long, Double>>();
            for (Pair<Long, Double> scoredItem : allScoredItems) {
                if (items.contains(scoredItem.getFirst())) {
                    scoredItems.add(scoredItem);
                }
            }
            strategy.printRanking(user, scoredItems, outRanking, format);
            strategy.printGroundtruth(user, outGroundtruth, format);
        }
        outRanking.close();
        outGroundtruth.close();
    }
}
