/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.frameworks.lenskit;

import net.recommenders.evaluation.frameworks.Recommend;
import net.recommenders.evaluation.frameworks.AbstractRunner;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.grouplens.lenskit.vectors.similarity.PearsonCorrelation;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 *
 * @author alan
 */
public class LenskitRecommenderRunner extends AbstractRunner{
    private final static Logger logger = LoggerFactory.getLogger(LenskitRecommenderRunner.class);


    /**
     *

     EventDAO dao = new EventCollectionDAO(rs);
     LenskitConfiguration config = new LenskitConfiguration();
     config.bind(EventDAO.class).to(dao);
     config.bind(ItemScorer.class).to(UserUserItemScorer.class);
     config.bind(NeighborhoodFinder.class).to(SimpleNeighborhoodFinder.class);
     config.within(UserSimilarity.class)
     .bind(VectorSimilarity.class)
     .to(PearsonCorrelation.class);


     factory.set(NeighborhoodSize.class).to(50);

     // this is the default
 //here !!<------
    LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);
    rec = engine.createRecommender();

     */

         /*        factory.setComponent(UserVectorNormalizer.class,
     VectorNormalizer.class,
     IdentityVectorNormalizer.class);*/

    public void runRecommender() throws IOException {

        File trainingFile = new File(parameters.get(Recommend.trainingSet));
        File testFile = new File(parameters.get(Recommend.testSet));
        EventDAO base = new SimpleFileRatingDAO(trainingFile,"\t");

        EventDAO dao = new EventCollectionDAO(Cursors.makeList(base.streamEvents()));

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        /*
        if (parameters.get(Recommend.similarity) != null){
            try {
                Class.forName(parameters.get(Recommend.similarity)).getConstructor().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoSuchMethodException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        */
        config.bind(ItemScorer.class).to(ItemItemScorer.class);

        config.within(ItemSimilarity.class).bind(VectorSimilarity.class).to(CosineVectorSimilarity.class);

//        config.within(ItemSimilarity.class).bind(VectorSimilarity.class).to(PearsonCorrelation.class);

        UserDAO test = new PrefetchingUserDAO(new SimpleFileRatingDAO(testFile, "\t"));


        Recommender rec = null;
        try{
            rec = LenskitRecommender.build(config);
        } catch (RecommenderBuildException e){
            logger.error(e.getMessage());
            System.out.println("RecommenderBuildException thrown");
        }




        ItemRecommender irec = rec.getItemRecommender();
        assert irec != null;


        if(irec == null)
            System.out.println("irec is null");

        for(long user : test.getUserIds()){
            List<ScoredId> recs = irec.recommend(user);
            writeData(parameters.get(Recommend.output), user, recs);
        }

    }





}
