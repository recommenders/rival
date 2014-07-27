package net.recommenders.rival.examples.movielens100k;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.recommenders.rival.core.DataModel;
import net.recommenders.rival.core.SimpleParser;
import net.recommenders.rival.evaluation.metric.divnov.AggrDiv;
import net.recommenders.rival.evaluation.metric.divnov.EFD;
import net.recommenders.rival.evaluation.metric.divnov.EILD;
import net.recommenders.rival.evaluation.metric.divnov.EPC;
import net.recommenders.rival.evaluation.metric.divnov.EPD;
import net.recommenders.rival.evaluation.metric.divnov.GiniIndex;
import net.recommenders.rival.evaluation.metric.divnov.dist.ItemDistance;
import net.recommenders.rival.evaluation.metric.divnov.dist.JaccardGenreItemDistance;
import net.recommenders.rival.evaluation.metric.divnov.dist.StoredItemDistance;

/**
 *
 * @author <a href="http://github.com/saulvargas">Saúl</a>.
 */
public class NoveltyDiversityMetricsEvaluator {

    public static void main(String[] args) throws IOException {
        String dataPath = "data/ml-100k/";
        String modelPath = "data/model/";
        String recPath = "data/recommendations/";
        int nFolds = 5;
//        CrossValidatedMahoutKNNRecommenderEvaluator.main(args);
        evaluate(nFolds, dataPath, modelPath, recPath);
    }

    public static Map<Long, Set<Long>> readGenres(String itemInfoFile) throws IOException {
        Map<Long, Set<Long>> itemGenresMap = new HashMap<Long, Set<Long>>();

        BufferedReader reader = new BufferedReader(new FileReader(itemInfoFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\\|");
            Long item = Long.parseLong(tokens[0]);
            Set<Long> genres = new HashSet<Long>();
            for (int i = 1; i <= 18; i++) {
                if (tokens[i + 5].equals("1")) {
                    genres.add((long) i);
                }
            }
            itemGenresMap.put(item, genres);
        }

        return itemGenresMap;
    }

    public static void evaluate(int nFolds, String dataPath, String splitPath, String recPath) throws IOException {
        double epcRes = 0.0;
        double efdRes = 0.0;
        double epdRes = 0.0;
        double eildRes = 0.0;
        double aggrdivRes = 0.0;
        double giniRes = 0.0;

        Map<Long, Set<Long>> itemGenresMap = readGenres(dataPath + "u.item");
        ItemDistance<Long> dist = new JaccardGenreItemDistance<Long, Long>(itemGenresMap);

        for (int i = 0; i < nFolds; i++) {
            File trainingFile = new File(splitPath + "train_" + i + ".csv");
            File testFile = new File(splitPath + "test_" + i + ".csv");
            File recFile = new File(recPath + "recs_" + i + ".csv");
            DataModel<Long, Long> trainingModel = null;
            DataModel<Long, Long> testModel = null;
            DataModel<Long, Long> recModel = null;
            try {
                trainingModel = new SimpleParser().parseData(trainingFile);
                testModel = new SimpleParser().parseData(testFile);
                recModel = new SimpleParser().parseData(recFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ItemDistance<Long> sdist = new StoredItemDistance<Long>(trainingModel.getItems(), dist);

            EPC epc = new EPC(recModel, testModel, new int[]{10}, trainingModel);
            epc.compute();
            epcRes += epc.getValueAt(10);

            EFD efd = new EFD(recModel, testModel, new int[]{10}, trainingModel);
            efd.compute();
            efdRes += efd.getValueAt(10);

            EPD epd = new EPD(recModel, testModel, new int[]{10}, trainingModel, sdist);
            epd.compute();
            epdRes += epd.getValueAt(10);

            EILD eild = new EILD(recModel, testModel, new int[]{10}, sdist);
            eild.compute();
            eildRes += eild.getValueAt(10);
            
            AggrDiv aggrDiv = new AggrDiv(recModel, testModel, new int[]{10}, trainingModel.getNumItems());
            aggrDiv.compute();
            aggrdivRes += aggrDiv.getValueAt(10);
            
            GiniIndex giniIndex = new GiniIndex(recModel, testModel, new int[]{10}, trainingModel.getNumItems());
            giniIndex.compute();
            giniRes += giniIndex.getValueAt(10);

        }
        System.out.println("EPC@10: " + epcRes / nFolds);
        System.out.println("EFD@10: " + efdRes / nFolds);
        System.out.println("EPD@10: " + epdRes / nFolds);
        System.out.println("EILD@10: " + eildRes / nFolds);
        System.out.println("AggrDiv@10: " + aggrdivRes / nFolds);
        System.out.println("GiniIndex@10: " + giniRes / nFolds);

    }
}
