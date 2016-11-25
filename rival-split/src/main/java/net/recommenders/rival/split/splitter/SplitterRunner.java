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
package net.recommenders.rival.split.splitter;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.DataModelUtils;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Class that splits a dataset according to some properties.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public final class SplitterRunner {

    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String DATASET_SPLITTER = "dataset.splitter";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_PERUSER = "split.peruser";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_PERITEMS = "split.peritems";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_SEED = "split.seed";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_CV_NFOLDS = "split.cv.nfolds";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_RANDOM_PERCENTAGE = "split.random.percentage";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_OUTPUT_FOLDER = "split.output.folder";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_OUTPUT_OVERWRITE = "split.output.overwrite";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_TRAINING_PREFIX = "split.training.prefix";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_TRAINING_SUFFIX = "split.training.suffix";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_TEST_PREFIX = "split.test.prefix";
    /**
     * Variable that represent the name of a property in the file.
     */
    public static final String SPLIT_TEST_SUFFIX = "split.test.suffix";

    /**
     * Variable that represent the field delimiter for each line
     */
    public static final String SPLIT_FIELD_DELIMITER = "split.delimiter";

    /**
     * Utility classes should not have a public or default constructor.
     */
    private SplitterRunner() {
    }

    /**
     * Runs a Splitter instance based on the properties.
     *
     * @param <U>         user identifier type
     * @param <I>         item identifier type
     * @param properties  property file
     * @param data        the data to be split
     * @param doDataClear flag to clear the memory used for the data before
     *                    saving the splits
     * @throws FileNotFoundException        see
     *                                      {@link net.recommenders.rival.core.DataModelUtils#saveDataModel(DataModelIF, String, boolean, String)}
     * @throws UnsupportedEncodingException see
     *                                      {@link net.recommenders.rival.core.DataModelUtils#saveDataModel(DataModelIF, String, boolean, String)}
     */
    public static <U, I> void run(final Properties properties, final TemporalDataModelIF<U, I> data, final boolean doDataClear)
            throws FileNotFoundException, UnsupportedEncodingException {
        System.out.println("Start splitting");
        TemporalDataModelIF<U, I>[] splits;
        // read parameters
        String outputFolder = properties.getProperty(SPLIT_OUTPUT_FOLDER);
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(SPLIT_OUTPUT_OVERWRITE, "false"));
        String fieldDelimiter = properties.getProperty(SPLIT_FIELD_DELIMITER, "\t");
        String splitTrainingPrefix = properties.getProperty(SPLIT_TRAINING_PREFIX);
        String splitTrainingSuffix = properties.getProperty(SPLIT_TRAINING_SUFFIX);
        String splitTestPrefix = properties.getProperty(SPLIT_TEST_PREFIX);
        String splitTestSuffix = properties.getProperty(SPLIT_TEST_SUFFIX);
        // generate splits
        Splitter<U, I> splitter = instantiateSplitter(properties);
        splits = splitter.split(data);
        if (doDataClear) {
            data.clear();
        }
        System.out.println("Saving splits");
        // save splits
        for (int i = 0; i < splits.length / 2; i++) {
            TemporalDataModelIF<U, I> training = splits[2 * i];
            TemporalDataModelIF<U, I> test = splits[2 * i + 1];
            String trainingFile = outputFolder + splitTrainingPrefix + i + splitTrainingSuffix;
            String testFile = outputFolder + splitTestPrefix + i + splitTestSuffix;
            DataModelUtils.saveDataModel(training, trainingFile, overwrite, fieldDelimiter);
            DataModelUtils.saveDataModel(test, testFile, overwrite, fieldDelimiter);
        }
    }

    /**
     * Instantiates a splitter based on the properties.
     *
     * @param <U>        user identifier type
     * @param <I>        item identifier type
     * @param properties the properties to be used.
     * @return a splitter according to the properties mapping provided.
     */
    public static <U, I> Splitter<U, I> instantiateSplitter(final Properties properties) {
        // read parameters
        String splitterClassName = properties.getProperty(DATASET_SPLITTER);
        Boolean perUser = Boolean.parseBoolean(properties.getProperty(SPLIT_PERUSER));
        Boolean doSplitPerItems = Boolean.parseBoolean(properties.getProperty(SPLIT_PERITEMS, "true"));
        // generate splitter
        Splitter<U, I> splitter = null;
        if (splitterClassName.contains("CrossValidation")) {
            Long seed = Long.parseLong(properties.getProperty(SPLIT_SEED));
            Integer nFolds = Integer.parseInt(properties.getProperty(SPLIT_CV_NFOLDS));
            splitter = new CrossValidationSplitter<>(nFolds, perUser, seed);
        } else if (splitterClassName.contains("Random")) {
            Long seed = Long.parseLong(properties.getProperty(SPLIT_SEED));
            Float percentage = Float.parseFloat(properties.getProperty(SPLIT_RANDOM_PERCENTAGE));
            splitter = new RandomSplitter<>(percentage, perUser, seed, doSplitPerItems);
        } else if (splitterClassName.contains("Temporal")) {
            Float percentage = Float.parseFloat(properties.getProperty(SPLIT_RANDOM_PERCENTAGE));
            splitter = new TemporalSplitter<>(percentage, perUser, doSplitPerItems);
        } else if (splitterClassName.contains("Validation")) {
            Long seed = Long.parseLong(properties.getProperty(SPLIT_SEED));
            Float percentage = Float.parseFloat(properties.getProperty(SPLIT_RANDOM_PERCENTAGE));
            Splitter<U, I> randomSplitter = new RandomSplitter<>(percentage, perUser, seed, doSplitPerItems);
            splitter = new ValidationSplitter<>(randomSplitter);
        }
        return splitter;
    }
}
