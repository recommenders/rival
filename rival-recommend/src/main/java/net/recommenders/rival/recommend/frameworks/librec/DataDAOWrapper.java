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
import java.util.List;
import java.util.concurrent.TimeUnit;
import librec.data.DataDAO;
import librec.data.SparseMatrix;
import librec.data.SparseTensor;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class DataDAOWrapper extends DataDAO {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 140160729L;
    /**
     * Librec's DataDAO that will be used as wrapper.
     */
    private DataDAO wrapper;

    /**
     * Constructs the wrapper using the provided model.
     *
     * @param model the model to be used to create the wrapped model
     */
    public DataDAOWrapper(final TemporalDataModelIF<Long, Long> model) {
        super(null);
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
            wrapper = new DataDAO(path.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public SparseMatrix[] readData() throws Exception {
        return wrapper.readData();
    }

    @Override
    public SparseMatrix[] readData(double binThold) throws Exception {
        return wrapper.readData(binThold);
    }

    @Override
    public SparseMatrix[] readData(int[] cols, double binThold) throws Exception {
        return wrapper.readData(cols, binThold);
    }

    @Override
    public SparseMatrix[] readTensor(int[] cols, double binThold) throws Exception {
        return wrapper.readTensor(cols, binThold);
    }

    @Override
    public void writeArff(String relation, String toPath) throws Exception {
        wrapper.writeArff(relation, toPath);
    }

    @Override
    public void writeData(String toPath) throws Exception {
        wrapper.writeData(toPath);
    }

    @Override
    public void writeData(String toPath, String sep) throws Exception {
        wrapper.writeData(toPath, sep);
    }

    @Override
    public void printDistr(boolean isWriteOut) throws Exception {
        wrapper.printDistr(isWriteOut);
    }

    @Override
    public void printSpecs() throws Exception {
        wrapper.printSpecs();
    }

    @Override
    public int numDays() {
        return wrapper.numDays();
    }

    @Override
    public int numItems() {
        return wrapper.numItems();
    }

    @Override
    public int numRatings() {
        return wrapper.numRatings();
    }

    @Override
    public int numUsers() {
        return wrapper.numUsers();
    }

    @Override
    public String getDataDirectory() {
        return wrapper.getDataDirectory();
    }

    @Override
    public String getDataName() {
        return wrapper.getDataName();
    }

    @Override
    public String getDataPath() {
        return wrapper.getDataPath();
    }

    @Override
    public int getItemId(String rawId) {
        return wrapper.getItemId(rawId);
    }

    @Override
    public String getItemId(int innerId) {
        return wrapper.getItemId(innerId);
    }

    @Override
    public BiMap<String, Integer> getItemIds() {
        return wrapper.getItemIds();
    }

    @Override
    public long getMaxTimestamp() {
        return wrapper.getMaxTimestamp();
    }

    @Override
    public long getMinTimestamp() {
        return wrapper.getMinTimestamp();
    }

    @Override
    public SparseMatrix getRateMatrix() {
        return wrapper.getRateMatrix();
    }

    @Override
    public SparseTensor getRateTensor() {
        return wrapper.getRateTensor();
    }

    @Override
    public List<Double> getRatingScale() {
        return wrapper.getRatingScale();
    }

    @Override
    public int getUserId(String rawId) {
        return wrapper.getUserId(rawId);
    }

    @Override
    public String getUserId(int innerId) {
        return wrapper.getUserId(innerId);
    }

    @Override
    public BiMap<String, Integer> getUserIds() {
        return wrapper.getUserIds();
    }

    @Override
    public void setHeadline(boolean isHeadline) {
        wrapper.setHeadline(isHeadline);
    }

    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
        wrapper.setTimeUnit(timeUnit);
    }

    @Override
    public boolean isHeadline() {
        return wrapper.isHeadline();
    }

    @Override
    public boolean isItemAsUser() {
        return wrapper.isItemAsUser();
    }

}
