package net.recommenders.rival.split.splitter;

import java.io.File;
import net.recommenders.rival.core.DataModel;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Alejandro
 */
public class SplitterRunner {

    public static final String DATASET_SPLITTER = "dataset.splitter";
    public static final String SPLIT_PERUSER = "split.peruser";
    public static final String SPLIT_PERITEMS = "split.peritems";
    public static final String SPLIT_SEED = "split.seed";
    public static final String SPLIT_CV_NFOLDS = "split.cv.nfolds";
    public static final String SPLIT_RANDOM_PERCENTAGE = "split.random.percentage";
    public static final String SPLIT_OUTPUT_FOLDER = "split.output.folder";
    public static final String SPLIT_OUTPUT_OVERWRITE = "split.output.overwrite";
    public static final String SPLIT_TRAINING_PREFIX = "split.training.prefix";
    public static final String SPLIT_TRAINING_SUFFIX = "split.training.suffix";
    public static final String SPLIT_TEST_PREFIX = "split.test.prefix";
    public static final String SPLIT_TEST_SUFFIX = "split.test.suffix";

    public static void run(Properties properties, DataModel<Long, Long> data, boolean doDataClear) throws ClassNotFoundException, FileNotFoundException {
        System.out.println("Start splitting");
        DataModel<Long, Long>[] splits = null;
        // read parameters
        String splitterClassName = properties.getProperty(DATASET_SPLITTER);
        Boolean perUser = Boolean.parseBoolean(properties.getProperty(SPLIT_PERUSER));
        Boolean doSplitPerItems = Boolean.parseBoolean(properties.getProperty(SPLIT_PERITEMS, "true"));
        String outputFolder = properties.getProperty(SPLIT_OUTPUT_FOLDER);
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(SPLIT_OUTPUT_OVERWRITE, "false"));
        String splitTrainingPrefix = properties.getProperty(SPLIT_TRAINING_PREFIX);
        String splitTrainingSuffix = properties.getProperty(SPLIT_TRAINING_SUFFIX);
        String splitTestPrefix = properties.getProperty(SPLIT_TEST_PREFIX);
        String splitTestSuffix = properties.getProperty(SPLIT_TEST_SUFFIX);
        // generate splits
        if (splitterClassName.contains("CrossValidation")) {
            Long seed = Long.parseLong(properties.getProperty(SPLIT_SEED));
            Integer nFolds = Integer.parseInt(properties.getProperty(SPLIT_CV_NFOLDS));
            splits = new CrossValidationSplitter(nFolds, perUser, seed).split(data);
        } else if (splitterClassName.contains("Random")) {
            Long seed = Long.parseLong(properties.getProperty(SPLIT_SEED));
            Float percentage = Float.parseFloat(properties.getProperty(SPLIT_RANDOM_PERCENTAGE));
            splits = new RandomSplitter(percentage, perUser, seed, doSplitPerItems).split(data);
        } else if (splitterClassName.contains("Temporal")) {
            Float percentage = Float.parseFloat(properties.getProperty(SPLIT_RANDOM_PERCENTAGE));
            splits = new TemporalSplitter(percentage, perUser, doSplitPerItems).split(data);
        }
        if (doDataClear) {
            data.clear();
        }
        System.out.println("Saving splits");
        // save splits
        for (int i = 0; i < splits.length / 2; i++) {
            DataModel<Long, Long> training = splits[2 * i];
            DataModel<Long, Long> test = splits[2 * i + 1];
            String trainingFile = outputFolder + splitTrainingPrefix + i + splitTrainingSuffix;
            String testFile = outputFolder + splitTestPrefix + i + splitTestSuffix;
            saveDataModel(training, trainingFile, overwrite);
            saveDataModel(test, testFile, overwrite);
        }
    }

    public static void saveDataModel(DataModel<Long, Long> model, String outfile, boolean overwrite) throws FileNotFoundException {
        if (new File(outfile).exists() && !overwrite) {
            System.out.println("Ignoring " + outfile);
        } else {
            PrintStream out = new PrintStream(outfile);
            for (Long user : model.getUsers()) {
                Map<Long, Double> userPrefModel = model.getUserItemPreferences().get(user);
                Map<Long, Set<Long>> userTimeModel = model.getUserItemTimestamps().get(user);
                for (Long item : userPrefModel.keySet()) {
                    Double pref = userPrefModel.get(item);
                    Set<Long> time = userTimeModel != null ? userTimeModel.get(item) : null;
                    if (time == null) {
                        out.println(user + "\t" + item + "\t" + pref + "\t-1");
                    } else {
                        for (Long t : time) {
                            out.println(user + "\t" + item + "\t" + pref + "\t" + t);
                        }
                    }
                }
            }
            out.close();
        }
    }
}
