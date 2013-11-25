/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.frameworks.mahout;

import net.recommenders.evaluation.frameworks.AbstractRunner;
import net.recommenders.evaluation.frameworks.Recommend;
import net.recommenders.evaluation.frameworks.mahout.exceptions.RecommenderException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author alejandr, alan
 */
public class MahoutRecommenderRunner extends AbstractRunner {

    public static final int DEFAULT_NEIGHBORHOOD_SIZE = 50;
    public MahoutRecommenderRunner(Properties _properties){
        super(_properties);
    }

    public void runRecommender() throws IOException, TasteException {
        DataModel trainModel = new FileDataModel(new File(properties.getProperty(Recommend.trainingSet)));
        DataModel testModel = new FileDataModel(new File(properties.getProperty(Recommend.testSet)));
        GenericRecommenderBuilder grb = new GenericRecommenderBuilder();
        Recommender recommender = null;
        try{
            if(properties.get(Recommend.factors) == null)
                recommender = grb.buildRecommender(trainModel, properties.getProperty(Recommend.recommender), properties.getProperty(Recommend.similarity), Integer.parseInt(properties.getProperty(Recommend.neighborhood)));
            if(properties.get(Recommend.factors) != null)
                recommender = grb.buildRecommender(trainModel, properties.getProperty(Recommend.recommender), properties.getProperty(Recommend.factorizer), Integer.parseInt(properties.getProperty(Recommend.iterations)), Integer.parseInt(properties.getProperty(Recommend.factors)));
            System.out.println(recommender.getClass().getName());
        }catch (RecommenderException e){
            e.printStackTrace();
        }

        LongPrimitiveIterator users = testModel.getUserIDs();
        while (users.hasNext()) {
            long u = users.nextLong();
            List<RecommendedItem> items = recommender.recommend(u, trainModel.getNumItems());
            writeData(properties.getProperty(Recommend.output), u, items);
        }
    }


    public void setProperties(Properties parameters) {
        this.properties = parameters;
    }


}
