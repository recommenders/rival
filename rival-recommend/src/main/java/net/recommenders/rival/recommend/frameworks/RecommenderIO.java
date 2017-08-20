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
package net.recommenders.rival.recommend.frameworks;

//import org.grouplens.lenskit.scored.ScoredId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Recommender-related IO operations.
 *
 * @author <a href="http://github.com/alansaid">Alan</a>.
 */
public final class RecommenderIO {

    /**
     * Utility classes should not have a public or default constructor.
     */
    private RecommenderIO() {
    }

    /**
     * Write recommendations to file.
     *
     * @param user the user
     * @param recommendations the recommendations
     * @param path directory where fileName will be written (if not null)
     * @param fileName name of the file, if null recommendations will not be
     * printed
     * @param append flag to decide if recommendations should be appended to
     * file
     * @param model if not null, recommendations will be saved here
     */
    public static void writeData(final long user, final List<Preference<Long, Long>> recommendations, final String path, final String fileName, final boolean append, final TemporalDataModelIF<Long, Long> model) {
        BufferedWriter out = null;
        try {
            File dir = null;
            if (path != null) {
                dir = new File(path);
                if (!dir.isDirectory()) {
                    if (!dir.mkdir() && (fileName != null)) {
                        System.out.println("Directory " + path + " could not be created");
                        return;
                    }
                }
            }
            if ((path != null) && (fileName != null)) {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "/" + fileName, append), "UTF-8"));
            }
            for (Preference<Long, Long> recItem : recommendations) {
                if (out != null) {
                    out.write(user + "\t" + recItem.getItem()+ "\t" + recItem.getScore() + "\n");
                }
                if (model != null) {
                    model.addPreference(user, recItem.getItem(), recItem.getScore());
                }
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
//            logger.error(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Preference<U, I> {

        private U user;
        private I item;
        private double score;

        public Preference(U user, I item, double score) {
            this.user = user;
            this.item = item;
            this.score = score;
        }

        public U getUser() {
            return user;
        }

        public I getItem() {
            return item;
        }

        public double getScore() {
            return score;
        }

    }
}
