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

import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastUserIndex;
import es.uam.eps.ir.ranksys.fast.preference.SimpleFastPreferenceData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;
import net.recommenders.rival.core.DataModelIF;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;

/**
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class RankSysDataModel<U, I> implements DataModelIF<U, I> {

    private SimpleFastPreferenceData<U, I> model;
    private List<Tuple3<U, I, Double>> tuples;
    private Set<U> users;
    private Set<I> items;

    public RankSysDataModel() {
        clear();
    }

    private void generateDatamodel() {
        FastUserIndex<U> uIndex = SimpleFastUserIndex.load(users.stream());
        FastItemIndex<I> iIndex = SimpleFastItemIndex.load(items.stream());
        model = SimpleFastPreferenceData.load(tuples.stream(), uIndex, iIndex);
    }

    @Override
    public void addPreference(U u, I i, Double d) {
        if (model != null) {
            throw new IllegalArgumentException("DataModel already generated. It is not possible to add more information.");
        }
        users.add(u);
        items.add(i);
        tuples.add(Tuple.tuple(u, i, d));
    }

    @Override
    public Double getUserItemPreference(U u, I i) {
        if (model == null) {
            generateDatamodel();
        }
        try {
            return model.getPreference(u, i).get().v2;
        } catch (NoSuchElementException e) {
            return Double.NaN;
        }
    }

    @Override
    public Iterable<I> getUserItems(U u) {
        if (model == null) {
            generateDatamodel();
        }
        Stream<I> s = model.getUserPreferences(u).map(p -> p.v1());
        return new Iterable<I>() {
            @Override
            public Iterator<I> iterator() {
                return s.iterator();
            }
        };
    }

    @Override
    public Iterable<I> getItems() {
        if (model == null) {
            generateDatamodel();
        }
        Stream<I> s = model.getItemsWithPreferences();
        return new Iterable<I>() {
            @Override
            public Iterator<I> iterator() {
                return s.iterator();
            }
        };
    }

    @Override
    public Iterable<U> getUsers() {
        if (model == null) {
            generateDatamodel();
        }
        Stream<U> s = model.getUsersWithPreferences();
        return new Iterable<U>() {
            @Override
            public Iterator<U> iterator() {
                return s.iterator();
            }
        };
    }

    @Override
    public int getNumItems() {
        if (model == null) {
            generateDatamodel();
        }
        return model.numItems();
    }

    @Override
    public int getNumUsers() {
        if (model == null) {
            generateDatamodel();
        }
        return model.numUsers();
    }

    @Override
    public void clear() {
        model = null;
        tuples = new ArrayList<>();
        users = new HashSet<>();
        items = new HashSet<>();
    }
}
