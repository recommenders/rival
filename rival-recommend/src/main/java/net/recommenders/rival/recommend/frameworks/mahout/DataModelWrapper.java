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
 * Mahout's DataModel wrapper for {@link net.recommenders.rival.core.DataModel}.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class DataModelWrapper implements DataModel {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 220150729L;
    /**
     * Mahout's datamodel that will be used as wrapper.
     */
    private GenericDataModel wrapper;

    /**
     * Constructs the wrapper using the provided model.
     *
     * @param model the model to be used to create the wrapped model
     */
    public DataModelWrapper(final net.recommenders.rival.core.TemporalDataModelIF<Long, Long> model) {
        FastByIDMap<Collection<Preference>> data = new FastByIDMap<Collection<Preference>>();
        FastByIDMap<FastByIDMap<Long>> timestampData = new FastByIDMap<FastByIDMap<Long>>();
        for (Long u : model.getUsers()) {
            List<Preference> prefs = new ArrayList<Preference>();
            FastByIDMap<Long> userTimestamps = new FastByIDMap<Long>();
            timestampData.put(u, userTimestamps);
            for (Long i : model.getUserItems(u)) {
                Iterable<Long> timestamps = model.getUserItemTimestamps(u, i);
                long t = -1;
                if (timestamps != null) {
                    for (Long tt : timestamps) {
                        t = tt;
                        break;
                    }
                }
                userTimestamps.put(i, t);
                prefs.add(new GenericPreference(u, i, model.getUserItemPreference(u, i).floatValue()));
            }
            data.put(u, prefs);
        }

        FastByIDMap<PreferenceArray> userData = GenericDataModel.toDataMap(data, true);
        wrapper = new GenericDataModel(userData, timestampData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongPrimitiveIterator getUserIDs() throws TasteException {
        return wrapper.getUserIDs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PreferenceArray getPreferencesFromUser(final long l) throws TasteException {
        return wrapper.getPreferencesFromUser(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FastIDSet getItemIDsFromUser(final long l) throws TasteException {
        return wrapper.getItemIDsFromUser(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongPrimitiveIterator getItemIDs() throws TasteException {
        return wrapper.getItemIDs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PreferenceArray getPreferencesForItem(final long l) throws TasteException {
        return wrapper.getPreferencesForItem(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getPreferenceValue(final long l, final long l1) throws TasteException {
        return wrapper.getPreferenceValue(l, l1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPreferenceTime(final long l, final long l1) throws TasteException {
        return wrapper.getPreferenceTime(l, l1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumItems() throws TasteException {
        return wrapper.getNumItems();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumUsers() throws TasteException {
        return wrapper.getNumUsers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumUsersWithPreferenceFor(final long l) throws TasteException {
        return wrapper.getNumUsersWithPreferenceFor(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumUsersWithPreferenceFor(final long l, final long l1) throws TasteException {
        return wrapper.getNumUsersWithPreferenceFor(l, l1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreference(final long l, final long l1, final float f) throws TasteException {
        wrapper.setPreference(l, l1, f);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePreference(final long l, final long l1) throws TasteException {
        wrapper.removePreference(l, l1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPreferenceValues() {
        return wrapper.hasPreferenceValues();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getMaxPreference() {
        return wrapper.getMaxPreference();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getMinPreference() {
        return wrapper.getMinPreference();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh(final Collection<Refreshable> clctn) {
        wrapper.refresh(clctn);
    }
}
