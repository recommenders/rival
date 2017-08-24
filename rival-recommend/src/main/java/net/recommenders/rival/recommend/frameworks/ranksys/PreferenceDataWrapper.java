/*
 * Copyright 2017 recommenders.net.
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
package net.recommenders.rival.recommend.frameworks.ranksys;

import es.uam.eps.ir.ranksys.core.preference.IdPref;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.fast.preference.IdxPref;
import es.uam.eps.ir.ranksys.fast.preference.SimpleFastPreferenceData;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.recommenders.rival.core.TemporalDataModelIF;
import org.jooq.lambda.tuple.Tuple3;

/**
 *
 * @author Alejandro
 */
public class PreferenceDataWrapper implements FastPreferenceData<Long, Long> {

    private final FastPreferenceData<Long, Long> wrapper;

    public PreferenceDataWrapper(TemporalDataModelIF<Long, Long> data, FastUserIndex<Long> uIndex, FastItemIndex<Long> iIndex) {
        List<Tuple3<Long, Long, Double>> tuples = new ArrayList<>();
        for (Long u : data.getUsers()) {
            for (Long i : data.getUserItems(u)) {
                tuples.add(new Tuple3<>(u, i, data.getUserItemPreference(u, i)));
            }
        }
        wrapper = SimpleFastPreferenceData.load(tuples.stream(), uIndex, iIndex);
    }

    @Override
    public int numUsers(int iidx) {
        return wrapper.numUsers(iidx);
    }

    @Override
    public int numItems(int uidx) {
        return wrapper.numItems(uidx);
    }

    @Override
    public IntStream getUidxWithPreferences() {
        return wrapper.getUidxWithPreferences();
    }

    @Override
    public IntStream getIidxWithPreferences() {
        return wrapper.getIidxWithPreferences();
    }

    @Override
    public Stream<? extends IdxPref> getUidxPreferences(int uidx) {
        return wrapper.getUidxPreferences(uidx);
    }

    @Override
    public Stream<? extends IdxPref> getIidxPreferences(int iidx) {
        return wrapper.getIidxPreferences(iidx);
    }

    @Override
    public IntIterator getUidxIidxs(int uidx) {
        return wrapper.getUidxIidxs(uidx);
    }

    @Override
    public DoubleIterator getUidxVs(int uidx) {
        return wrapper.getUidxVs(uidx);
    }

    @Override
    public IntIterator getIidxUidxs(int iidx) {
        return wrapper.getIidxUidxs(iidx);
    }

    @Override
    public DoubleIterator getIidxVs(int iidx) {
        return wrapper.getIidxVs(iidx);
    }

    @Override
    public boolean useIteratorsPreferentially() {
        return wrapper.useIteratorsPreferentially();
    }

    @Override
    public int numUsersWithPreferences() {
        return wrapper.numUsersWithPreferences();
    }

    @Override
    public int numItemsWithPreferences() {
        return wrapper.numItemsWithPreferences();
    }

    @Override
    public int numUsers(Long i) {
        return wrapper.numUsers(i);
    }

    @Override
    public int numItems(Long u) {
        return wrapper.numItems(u);
    }

    @Override
    public int numPreferences() {
        return wrapper.numPreferences();
    }

    @Override
    public Stream<Long> getUsersWithPreferences() {
        return wrapper.getUsersWithPreferences();
    }

    @Override
    public Stream<Long> getItemsWithPreferences() {
        return wrapper.getItemsWithPreferences();
    }

    @Override
    public Stream<? extends IdPref<Long>> getUserPreferences(Long u) {
        return wrapper.getUserPreferences(u);
    }

    @Override
    public Stream<? extends IdPref<Long>> getItemPreferences(Long i) {
        return wrapper.getItemPreferences(i);
    }

    @Override
    public boolean containsUser(Long u) {
        return wrapper.containsUser(u);
    }

    @Override
    public int numUsers() {
        return wrapper.numUsers();
    }

    @Override
    public Stream<Long> getAllUsers() {
        return wrapper.getAllUsers();
    }

    @Override
    public boolean containsItem(Long i) {
        return wrapper.containsItem(i);
    }

    @Override
    public int numItems() {
        return wrapper.numItems();
    }

    @Override
    public Stream<Long> getAllItems() {
        return wrapper.getAllItems();
    }

    @Override
    public int user2uidx(Long u) {
        return wrapper.user2uidx(u);
    }

    @Override
    public Long uidx2user(int uidx) {
        return wrapper.uidx2user(uidx);
    }

    @Override
    public int item2iidx(Long i) {
        return wrapper.item2iidx(i);
    }

    @Override
    public Long iidx2item(int iidx) {
        return wrapper.iidx2item(iidx);
    }

}
