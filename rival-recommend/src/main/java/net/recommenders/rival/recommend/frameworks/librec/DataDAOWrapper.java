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
package net.recommenders.rival.recommend.frameworks.librec;

import com.google.common.collect.BiMap;
import java.io.File;
import java.io.PrintStream;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.conf.Configured;
import net.librec.data.DataAppender;
import net.librec.data.DataContext;
import net.librec.data.DataModel;
import net.librec.data.DataSplitter;
import net.librec.data.model.TextDataModel;
import net.librec.math.structure.DataSet;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class DataDAOWrapper implements DataModel {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 170160729L;
    /**
     * Librec's DataModel that will be used as wrapper.
     */
    private DataModel wrapper;

    /**
     * Constructs the wrapper using the provided model.
     *
     * @param model the model to be used to create the wrapped model
     */
    public DataDAOWrapper(final TemporalDataModelIF<Long, Long> model) {
        super();
        try {
            // generate file based on the model
            File path = File.createTempFile("librec", "datadao");
            PrintStream out = new PrintStream(path);
            for (Long u : model.getUsers()) {
                for (Long i : model.getUserItems(u)) {
                    Double d = model.getUserItemPreference(u, i);
                    Iterable<Long> time = model.getUserItemTimestamps(u, i);
                    if (time == null) {
                        out.println(u + "\t" + i + "\t" + d);
                    } else {
                        for (Long t : time) {
                            out.println(u + "\t" + i + "\t" + d + "\t" + t);
                            break;
                        }
                    }
                }
            }
            out.close();
            // create the wrapper based on this file
            Configuration confTraining = new Configuration();
            confTraining.set(Configured.CONF_DATA_INPUT_PATH, path.getAbsolutePath());
            confTraining.set(Configured.CONF_DATA_COLUMN_FORMAT, "UIR");
            confTraining.set("data.model.splitter", "ratio");
            confTraining.set("data.splitter.trainset.ratio", "0.999");
            confTraining.set("data.splitter.ratio", "rating");
            wrapper = new TextDataModel(confTraining);
            wrapper.buildDataModel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void buildDataModel() throws LibrecException {
        wrapper.buildDataModel();
    }

    @Override
    public void loadDataModel() throws LibrecException {
        wrapper.loadDataModel();
    }

    @Override
    public void saveDataModel() throws LibrecException {
        wrapper.saveDataModel();
    }

    @Override
    public DataSplitter getDataSplitter() {
        return wrapper.getDataSplitter();
    }

    @Override
    public DataSet getTrainDataSet() {
        return wrapper.getTrainDataSet();
    }

    @Override
    public DataSet getTestDataSet() {
        return wrapper.getTestDataSet();
    }

    @Override
    public DataSet getValidDataSet() {
        return wrapper.getValidDataSet();
    }

    @Override
    public DataSet getDatetimeDataSet() {
        return wrapper.getDatetimeDataSet();
    }

    @Override
    public BiMap<String, Integer> getUserMappingData() {
        return wrapper.getUserMappingData();
    }

    @Override
    public BiMap<String, Integer> getItemMappingData() {
        return wrapper.getItemMappingData();
    }

    @Override
    public DataAppender getDataAppender() {
        return wrapper.getDataAppender();
    }

    @Override
    public DataContext getContext() {
        return wrapper.getContext();
    }

}
