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
package net.recommenders.rival.evaluation.statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * Runner of methods to compute whether the evaluation measures are
 * statistically significant
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class StatisticsRunner {

    /**
     * Variables that represent the name of several properties in the file.
     */
    public static final String BASELINE_FILE = "algorithm.baseline.file";
    public static final String TEST_METHODS_FILES = "algorithm.methods.files";
    public static final String INPUT_FORMAT = "input.format.statistics";
    public static final String OUTPUT_OVERWRITE = "output.overwrite.statistics";
    public static final String OUTPUT_FILE = "output.file.statistics";
    public static final String STATISTICS = "statistics.functions";
    public static final String ALPHA = "statistics.alpha";
    public static final String AVOID_USERS = "statistics.users_to_avoid";

    /**
     * Main method for running a single evaluation strategy.
     *
     * @param args Arguments.
     * @throws Exception If file not found.
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
     * Run all the statistic functions included in the properties mapping.
     *
     * @param properties The properties to be executed.
     * @throws IOException when a file cannot be parsed
     * @throws IllegalArgumentException when some property cannot be parsed
     */
    public static void run(Properties properties) throws IOException, IllegalArgumentException {
        // read parameters for output (do this at the beginning to avoid unnecessary reading)
        File outputFile = new File(properties.getProperty(OUTPUT_FILE));
        Boolean overwrite = Boolean.parseBoolean(properties.getProperty(OUTPUT_OVERWRITE, "false"));
        PrintStream outStatistics = null;
        if (outputFile.exists() && !overwrite) {
            throw new IllegalArgumentException("Cannot generate statistics because " + outputFile + " exists and overwrite is " + overwrite);
        } else {
            outStatistics = new PrintStream(outputFile);
        }
        // read format
        String format = properties.getProperty(INPUT_FORMAT);
        // read users to avoid
        String[] usersToAvoidArray = properties.getProperty(AVOID_USERS, "").split(",");
        Set<String> usersToAvoid = new HashSet<String>();
        for (String u : usersToAvoidArray) {
            usersToAvoid.add(u);
        }
        // read baseline <-- this file is mandatory
        File baselineFile = new File(properties.getProperty(BASELINE_FILE));
        Map<String, Map<String, Double>> baselineMapMetricUserValues = readMetricFile(baselineFile, format, usersToAvoid);
        // read methods <-- at least one file should be provided
        String[] methodFiles = properties.getProperty(TEST_METHODS_FILES).split(",");
        if (methodFiles.length < 1) {
            throw new IllegalArgumentException("At least one test file should be provided!");
        }
        Map<String, Map<String, Map<String, Double>>> methodsMapMetricUserValues = new HashMap();
        for (String m : methodFiles) {
            File file = new File(m);
            Map<String, Map<String, Double>> mapMetricUserValues = readMetricFile(file, format, usersToAvoid);
            methodsMapMetricUserValues.put(m, mapMetricUserValues);
        }
        run(properties, outStatistics, baselineFile.getName(), baselineMapMetricUserValues, methodsMapMetricUserValues);
        // close files
        outStatistics.close();
    }

    /**
     *
     * Run all the statistic functions included in the properties mapping,
     * taking the results of the metrics from memory.
     *
     * @param properties the properties to be used.
     * @param outStatistics stream where the output will be printed to.
     * @param baselineName name of the baseline method (for printing purposes)
     * @param baselineMapMetricUserValues result values for each metric for the
     * baseline method.
     * @param methodsMapMetricUserValues result values for each metric for each
     * recommender that should be compared against the baseline method.
     */
    public static void run(Properties properties, PrintStream outStatistics, String baselineName, Map<String, Map<String, Double>> baselineMapMetricUserValues, Map<String, Map<String, Map<String, Double>>> methodsMapMetricUserValues) {
        // read alpha
        Double alpha = Double.parseDouble(properties.getProperty(ALPHA));
        // for each statistic function, call a different method and produce a different output depending on the number of methods
        String[] statFunctions = properties.getProperty(STATISTICS).split(",");
        for (String statFunction : statFunctions) {
            if (statFunction.equals("confidence_interval")) {
                for (String metric : baselineMapMetricUserValues.keySet()) {
                    Map<String, Double> userMetricValuesBaseline = baselineMapMetricUserValues.get(metric);
                    for (String method : methodsMapMetricUserValues.keySet()) {
                        if (methodsMapMetricUserValues.get(method).containsKey(metric)) {
                            Map<String, Double> userMetricValuesMethod = methodsMapMetricUserValues.get(method).get(metric);
                            // samples are paired
                            double[] interval = new ConfidenceInterval().getConfidenceInterval(alpha, userMetricValuesBaseline, userMetricValuesMethod, true);
                            outStatistics.println(baselineName + "\t" + method + "\t" + metric + "\t" + statFunction + "_lower" + "\t" + interval[0]);
                            outStatistics.println(baselineName + "\t" + method + "\t" + metric + "\t" + statFunction + "_upper" + "\t" + interval[1]);
                        }
                    }
                }
            } else if (statFunction.startsWith("effect_size")) {
                String effectSizeMethod = statFunction.replaceAll("effect_size_", "");
                for (String metric : baselineMapMetricUserValues.keySet()) {
                    Map<String, Double> userMetricValuesBaseline = baselineMapMetricUserValues.get(metric);
                    for (String method : methodsMapMetricUserValues.keySet()) {
                        if (methodsMapMetricUserValues.get(method).containsKey(metric)) {
                            Map<String, Double> userMetricValuesMethod = methodsMapMetricUserValues.get(method).get(metric);
                            double es = new EffectSize<String>(userMetricValuesBaseline, userMetricValuesMethod).getEffectSize(effectSizeMethod);
                            outStatistics.println(baselineName + "\t" + method + "\t" + metric + "\t" + statFunction + "\t" + es);
                        }
                    }
                }
            } else if (statFunction.equals("standard_error")) {
                for (String metric : baselineMapMetricUserValues.keySet()) {
                    Map<String, Double> userMetricValuesBaseline = baselineMapMetricUserValues.get(metric);
                    for (String method : methodsMapMetricUserValues.keySet()) {
                        if (methodsMapMetricUserValues.get(method).containsKey(metric)) {
                            Map<String, Double> userMetricValuesMethod = methodsMapMetricUserValues.get(method).get(metric);
                            double se = new StandardError<String>(userMetricValuesBaseline, userMetricValuesMethod).getStandardError();
                            outStatistics.println(baselineName + "\t" + method + "\t" + metric + "\t" + statFunction + "\t" + se);
                        }
                    }
                }
            } else if (statFunction.startsWith("statistical_significance")) {
                String statFunctionMethod = statFunction.replaceAll("statistical_significance_", "");
                for (String metric : baselineMapMetricUserValues.keySet()) {
                    Map<String, Double> userMetricValuesBaseline = baselineMapMetricUserValues.get(metric);
                    for (String method : methodsMapMetricUserValues.keySet()) {
                        if (methodsMapMetricUserValues.get(method).containsKey(metric)) {
                            Map<String, Double> userMetricValuesMethod = methodsMapMetricUserValues.get(method).get(metric);
                            double p = new StatisticalSignificance(userMetricValuesBaseline, userMetricValuesMethod).getPValue(statFunctionMethod);
                            outStatistics.println(baselineName + "\t" + method + "\t" + metric + "\t" + statFunction + "\t" + p);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * Reads results from the metric file.
     *
     * @param input The metric file.
     * @param format The format of the file.
     * @param usersToAvoid User ids to be avoided in the subsequent significance
     * testing (e.g., 'all')
     * @return A map where for each metric, each user has been assigned her
     * corresponding metric value.
     * @throws IOException
     */
    private static Map<String, Map<String, Double>> readMetricFile(File input, String format, Set<String> usersToAvoid) throws IOException {
        Map<String, Map<String, Double>> mapMetricUserValue = new HashMap<String, Map<String, Double>>();
        BufferedReader br = new BufferedReader(new FileReader(input));
        String line = null;
        while ((line = br.readLine()) != null) {
            readLine(format, line, mapMetricUserValue, usersToAvoid);
        }
        br.close();
        return mapMetricUserValue;
    }

    /**
     * Read a line from the metric file.
     *
     * @param format The format of the file.
     * @param line The line.
     * @param mapMetricUserValue Map where metric values for each user will be
     * stored.
     * @param usersToAvoid User ids to be avoided in the subsequent significance
     * testing (e.g., 'all')
     */
    public static void readLine(String format, String line, Map<String, Map<String, Double>> mapMetricUserValue, Set<String> usersToAvoid) {
        String[] toks = line.split("\t");
        // default (also trec_eval) format: metric \t user|all \t value
        if (format.equals("default")) {
            String metric = toks[0];
            String user = toks[1];
            Double score = Double.parseDouble(toks[2]);
            if (usersToAvoid.contains(user)) {
                return;
            }
            Map<String, Double> userValueMap = mapMetricUserValue.get(metric);
            if (userValueMap == null) {
                userValueMap = new HashMap<String, Double>();
                mapMetricUserValue.put(metric, userValueMap);
            }
            userValueMap.put(user, score);
        }
    }
}
