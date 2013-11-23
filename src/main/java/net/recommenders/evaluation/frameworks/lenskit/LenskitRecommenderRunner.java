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
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.scored.ScoredId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author alan
 */
public class LenskitRecommenderRunner extends AbstractRunner{
    private final static Logger logger = LoggerFactory.getLogger(LenskitRecommenderRunner.class);


    public void runRecommender() throws IOException {

        File trainingFile = new File(parameters.get(Recommend.trainingSet));
        File testFile = new File(parameters.get(Recommend.testSet));
        EventDAO base = new SimpleFileRatingDAO(trainingFile,"\t");

        EventDAO dao = new EventCollectionDAO(Cursors.makeList(base.streamEvents()));

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(ItemScorer.class).to(ItemItemScorer.class);

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
