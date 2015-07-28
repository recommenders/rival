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
package net.recommenders.rival.recommend.frameworks.mahout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

/**
 *
 * @author Alejandro
 */
public class DataModelWrapper implements DataModel {

    private GenericDataModel wrapper;

    public DataModelWrapper(net.recommenders.rival.core.DataModel<Long, Long> model) {
        FastByIDMap<Collection<Preference>> data = new FastByIDMap();
        FastByIDMap<FastByIDMap<Long>> timestampData = new FastByIDMap();
        for (Long u : model.getUserItemPreferences().keySet()) {
            List<Preference> prefs = new ArrayList();
            FastByIDMap<Long> userTimestamps = new FastByIDMap();
            timestampData.put(u, userTimestamps);
            for (Long i : model.getUserItemPreferences().get(u).keySet()) {
                Set<Long> timestamps = model.getUserItemTimestamps().get(u).get(i);
                long t = -1;
                if (timestamps != null) {
                    for (Long tt : timestamps) {
                        t = tt;
                        break;
                    }
                }
                userTimestamps.put(i, t);
                prefs.add(new GenericPreference(u, i, model.getUserItemPreferences().get(u).get(i).floatValue()));
            }
            data.put(u, prefs);
        }

        FastByIDMap<PreferenceArray> userData = GenericDataModel.toDataMap(data, true);
        wrapper = new GenericDataModel(userData, timestampData);
    }

    @Override
    public LongPrimitiveIterator getUserIDs() throws TasteException {
        return wrapper.getUserIDs();
    }

    @Override
    public PreferenceArray getPreferencesFromUser(long l) throws TasteException {
        return wrapper.getPreferencesFromUser(l);
    }

    @Override
    public FastIDSet getItemIDsFromUser(long l) throws TasteException {
        return wrapper.getItemIDsFromUser(l);
    }

    @Override
    public LongPrimitiveIterator getItemIDs() throws TasteException {
        return wrapper.getItemIDs();
    }

    @Override
    public PreferenceArray getPreferencesForItem(long l) throws TasteException {
        return wrapper.getPreferencesForItem(l);
    }

    @Override
    public Float getPreferenceValue(long l, long l1) throws TasteException {
        return wrapper.getPreferenceValue(l, l1);
    }

    @Override
    public Long getPreferenceTime(long l, long l1) throws TasteException {
        return wrapper.getPreferenceTime(l, l1);
    }

    @Override
    public int getNumItems() throws TasteException {
        return wrapper.getNumItems();
    }

    @Override
    public int getNumUsers() throws TasteException {
        return wrapper.getNumUsers();
    }

    @Override
    public int getNumUsersWithPreferenceFor(long l) throws TasteException {
        return wrapper.getNumUsersWithPreferenceFor(l);
    }

    @Override
    public int getNumUsersWithPreferenceFor(long l, long l1) throws TasteException {
        return wrapper.getNumUsersWithPreferenceFor(l, l1);
    }

    @Override
    public void setPreference(long l, long l1, float f) throws TasteException {
        wrapper.setPreference(l, l1, f);
    }

    @Override
    public void removePreference(long l, long l1) throws TasteException {
        wrapper.removePreference(l, l1);
    }

    @Override
    public boolean hasPreferenceValues() {
        return wrapper.hasPreferenceValues();
    }

    @Override
    public float getMaxPreference() {
        return wrapper.getMaxPreference();
    }

    @Override
    public float getMinPreference() {
        return wrapper.getMinPreference();
    }

    @Override
    public void refresh(Collection<Refreshable> clctn) {
        wrapper.refresh(clctn);
    }
}
