/*
 * Copyright 2016 recommenders.net.
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
package net.recommenders.rival.examples.mdp;

/**
 *
 * @author Alejandro
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String datasetFile = "../../problem__marco_di_pietro/tripadvisor_ratings_u0_i0_.csv";
        String out = "../../problem__marco_di_pietro/";
        
        MahoutItemBasedCFRecSysEvaluator eval = new MahoutItemBasedCFRecSysEvaluator(/*5*/ 2, 4.0, MahoutItemBasedCFRecSysEvaluator.Distance.PEARSON);
//        eval.split(datasetFile, out, false, 0, ",");
//        eval.recommend(out, out);
        eval.buildEvaluationModels(out, out, out);
        eval.evaluate(out, out);
    }
}
