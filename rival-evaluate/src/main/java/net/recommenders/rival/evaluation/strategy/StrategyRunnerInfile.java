package net.recommenders.rival.evaluation.strategy;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy.Pair;

/**
 * Runner for a strategy where the information is not completely stored in
 * memory, only in a per user basis.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class StrategyRunnerInfile {

    /**
     * Variables that represent the name of several properties in the file.
     */
    public static final String TRAINING_FILE = "split.training.file";
    public static final String TEST_FILE = "split.test.file";
    public static final String INPUT_FILE = "recommendation.file";
    public static final String OUTPUT_FORMAT = "output.format";
    public static final String OUTPUT_OVERWRITE = "output.overwrite";
    public static final String OUTPUT_FILE = "output.file.ranking";
    public static final String GROUNDTRUTH_FILE = "output.file.groundtruth";
    public static final String STRATEGY = "strategy.class";
    public static final String RELEVANCE_THRESHOLD = "strategy.relevance.threshold";
    public static final String RELPLUSN_N = "strategy.relplusn.N";
    public static final String RELPLUSN_SEED = "strategy.relplusn.seed";

    /**
     * Main function. It receives the property file using a system property
     * ('propertyFile')
     *
     * @param args (not used)
     * @throws Exception when
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

        run(properties);
    }

    /**
     * Process the property file and runs the specified strategies on some data.
     *
     * @param properties The property file
     * @throws IOException when
     * @throws ClassNotFoundException when
     * @throws IllegalAccessException when
     * @throws IllegalArgumentException when
     * @throws InstantiationException when
     * @throws InvocationTargetException when
     * @throws NoSuchMethodException when
     * @throws SecurityException when
     */
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
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(OUTPUT_OVERWRITE, "false"));
        File rankingFile = new File(properties.getProperty(OUTPUT_FILE));
        File groundtruthFile = new File(properties.getProperty(GROUNDTRUTH_FILE));
        EvaluationStrategy.OUTPUT_FORMAT format = properties.getProperty(OUTPUT_FORMAT).equals(EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL.toString()) ? EvaluationStrategy.OUTPUT_FORMAT.TRECEVAL : EvaluationStrategy.OUTPUT_FORMAT.SIMPLE;
        Double threshold = Double.parseDouble(properties.getProperty(RELEVANCE_THRESHOLD));
        String strategyClassName = properties.getProperty(STRATEGY);
        Class<?> strategyClass = Class.forName(strategyClassName);
        // get strategy
        EvaluationStrategy<Long, Long> strategy = null;
        if (strategyClassName.contains("RelPlusN")) {
            Integer N = Integer.parseInt(properties.getProperty(RELPLUSN_N));
            Long seed = Long.parseLong(properties.getProperty(RELPLUSN_SEED));
            strategy = new RelPlusN(trainingModel, testModel, N, threshold, seed);
        } else {
            Object strategyObj = strategyClass.getConstructor(DataModel.class, DataModel.class, double.class).newInstance(trainingModel, testModel, threshold);
            if (strategyObj instanceof EvaluationStrategy) {
                @SuppressWarnings("unchecked")
                EvaluationStrategy<Long, Long> strategyTemp = (EvaluationStrategy<Long, Long>) strategyObj;
                strategy = strategyTemp;
            }
        }
        // generate output
        generateOutput(testModel, inputFile, strategy, format, rankingFile, groundtruthFile, overwrite);
    }

    /**
     * Runs a particular strategy on some data using pre-computed
     * recommendations and outputs the result into a file.
     *
     * @param testModel The test split
     * @param userRecommendationFile The file where recommendations are stored
     * @param strategy The strategy to be used
     * @param format The format of the output
     * @param rankingFile The file where the ranking will be printed
     * @param groundtruthFile The file where the ground truth will be printed
     * @param overwrite The flag that specifies what to do if rankingFile or
     * groundtruthFile already exists
     * @throws FileNotFoundException when
     * @throws IOException when
     */
    public static void generateOutput(final DataModel<Long, Long> testModel, final File userRecommendationFile, EvaluationStrategy<Long, Long> strategy, EvaluationStrategy.OUTPUT_FORMAT format, File rankingFile, File groundtruthFile, Boolean overwrite) throws FileNotFoundException, IOException {
        PrintStream outRanking = null;
        if (rankingFile.exists() && !overwrite) {
            System.out.println("Ignoring " + rankingFile);
        } else {
            outRanking = new PrintStream(rankingFile);
        }
        PrintStream outGroundtruth = null;
        if (groundtruthFile.exists() && !overwrite) {
            System.out.println("Ignoring " + groundtruthFile);
        } else {
            outGroundtruth = new PrintStream(groundtruthFile);
        }
        for (Long user : testModel.getUsers()) {
            if (outRanking != null) {
                final List<EvaluationStrategy.Pair<Long, Double>> allScoredItems = readScoredItems(userRecommendationFile, user);
                if (allScoredItems == null) {
                    continue;
                }
                final Set<Long> items = strategy.getCandidateItemsToRank(user);
                final List<EvaluationStrategy.Pair<Long, Double>> scoredItems = new ArrayList<EvaluationStrategy.Pair<Long, Double>>();
                for (EvaluationStrategy.Pair<Long, Double> scoredItem : allScoredItems) {
                    if (items.contains(scoredItem.getFirst())) {
                        scoredItems.add(scoredItem);
                    }
                }
                strategy.printRanking(user, scoredItems, outRanking, format);
            }
            if (outGroundtruth != null) {
                strategy.printGroundtruth(user, outGroundtruth, format);
            }
        }
        if (outRanking != null) {
            outRanking.close();
        }
        if (outGroundtruth != null) {
            outGroundtruth.close();
        }
    }

    /**
     * Method that reads the scores given to items by a recommender only for a
     * given user (it ignores the rest).
     *
     * @param userRecommendationFile The file with the recommendation scores
     * @param user The user
     * @return the pairs (item, score) contained in the file for that user
     * @throws IOException when
     * @see StrategyRunnerInfile#readLine(java.lang.String, java.util.Map)
     */
    public static List<EvaluationStrategy.Pair<Long, Double>> readScoredItems(File userRecommendationFile, Long user) throws IOException {
        final Map<Long, List<Pair<Long, Double>>> mapUserRecommendations = new HashMap<Long, List<Pair<Long, Double>>>();
        BufferedReader in = new BufferedReader(new FileReader(userRecommendationFile));
        String line = null;
        boolean foundUser = false;
        // read recommendations: user \t item \t score
        while ((line = in.readLine()) != null) {
            String[] toks = line.split("\t");
            String u = toks[0];
            if (u.equals(user + "")) {
                readLine(line, mapUserRecommendations);
                foundUser = true;
            } else if (foundUser) {
                // assuming a sorted file (at least, per user)
                break;
            }
        }
        in.close();
        return mapUserRecommendations.get(user);
    }

    /**
     * Method that reads a line that contains a(some) recommendation(s) and
     * store it in a map.
     *
     * The line can have a simple format <br> "user \t item \t score" <br> or
     * the one used in MyMediaLite <br> "user \t [item:score,item:score,...]".
     *
     * @param line The line to be parsed.
     * @param mapUserRecommendations The map where the parsed recommendations
     * will be stored.
     */
    public static void readLine(String line, Map<Long, List<Pair<Long, Double>>> mapUserRecommendations) {
        String[] toks = line.split("\t");
        // mymedialite format: user \t [item:score,item:score,...]
        if (line.contains(":") && line.contains(",")) {
            Long user = Long.parseLong(toks[0]);
            String items = toks[1].replace("[", "").replace("]", "");
            for (String pair : items.split(",")) {
                String[] pairToks = pair.split(":");
                Long item = Long.parseLong(pairToks[0]);
                Double score = Double.parseDouble(pairToks[1]);
                List<Pair<Long, Double>> userRec = mapUserRecommendations.get(user);
                if (userRec == null) {
                    userRec = new ArrayList<Pair<Long, Double>>();
                    mapUserRecommendations.put(user, userRec);
                }
                userRec.add(new Pair<Long, Double>(item, score));
            }
        } else {
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
    }
}
