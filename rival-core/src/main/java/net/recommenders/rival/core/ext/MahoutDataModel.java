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
package net.recommenders.rival.core.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.recommenders.rival.core.TemporalDataModelIF;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

/**
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class MahoutDataModel implements TemporalDataModelIF<Long, Long> {

    private GenericDataModel model;
    private FastByIDMap<Collection<Preference>> data;
    private FastByIDMap<FastByIDMap<Long>> timestampData;

    public MahoutDataModel() {
        clear();
    }

    private void generateDatamodel() {
        FastByIDMap<PreferenceArray> userData = GenericDataModel.toDataMap(data, true);
        model = new GenericDataModel(userData, timestampData);
        data = null;
        timestampData = null;
    }

    @Override
    public void addPreference(Long u, Long i, Double d) {
        if (model != null) {
            throw new IllegalArgumentException("DataModel already generated. It is not possible to add more information.");
        }
        Collection<Preference> prefs = null;
        if (!data.containsKey(u)) {
            prefs = new ArrayList<Preference>();
            data.put(u, prefs);
        } else {
            prefs = data.get(u);
        }
        prefs.add(new GenericPreference(u, i, d.floatValue()));
    }

    @Override
    public void addTimestamp(Long u, Long i, Long t) {
        if (model != null) {
            throw new IllegalArgumentException("DataModel already generated. It is not possible to add more information.");
        }
        FastByIDMap<Long> prefs = null;
        if (!timestampData.containsKey(u)) {
            prefs = new FastByIDMap<Long>();
            timestampData.put(u, prefs);
        } else {
            prefs = timestampData.get(u);
        }
        prefs.put(i, t);
    }

    @Override
    public Double getUserItemPreference(Long u, Long i) {
        if (model == null) {
            generateDatamodel();
        }
        try {
            return model.getPreferenceValue(u, i) * 1.0;
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return Double.NaN;
    }

    @Override
    public Iterable<Long> getUserItemTimestamps(Long u, Long i) {
        if (model == null) {
            generateDatamodel();
        }
        List<Long> t = new ArrayList<>();
        try {
            t.add(model.getPreferenceTime(u, i));
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public Iterable<Long> getUserItems(Long u) {
        if (model == null) {
            generateDatamodel();
        }
        try {
            return model.getItemIDsFromUser(u);
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }

    @Override
    public Iterable<Long> getItems() {
        if (model == null) {
            generateDatamodel();
        }
        List<Long> items = new ArrayList<>();
        LongPrimitiveIterator lpi = model.getItemIDs();
        while (lpi.hasNext()) {
            items.add(lpi.nextLong());
        }
        return items;
    }

    @Override
    public Iterable<Long> getUsers() {
        if (model == null) {
            generateDatamodel();
        }
        List<Long> users = new ArrayList<>();
        LongPrimitiveIterator lpi = model.getUserIDs();
        while (lpi.hasNext()) {
            users.add(lpi.nextLong());
        }
        return users;
    }

    @Override
    public int getNumItems() {
        if (model == null) {
            generateDatamodel();
        }
        return model.getNumItems();
    }

    @Override
    public int getNumUsers() {
        if (model == null) {
            generateDatamodel();
        }
        return model.getNumUsers();
    }

    @Override
    public void clear() {
        model = null;
        data = new FastByIDMap<Collection<Preference>>();
        timestampData = new FastByIDMap<FastByIDMap<Long>>();
    }
}
