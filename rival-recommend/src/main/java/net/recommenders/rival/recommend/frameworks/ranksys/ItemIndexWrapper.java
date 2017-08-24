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

import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastItemIndex;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 *
 * @author Alejandro
 */
public class ItemIndexWrapper implements FastItemIndex<Long> {

    private final FastItemIndex<Long> wrapper;

    public ItemIndexWrapper(TemporalDataModelIF<Long, Long> training, TemporalDataModelIF<Long, Long> test) {
        Set<Long> items = new HashSet<>();
        for (Long u : training.getItems()) {
            items.add(u);
        }
        for (Long u : test.getItems()) {
            items.add(u);
        }
        wrapper = SimpleFastItemIndex.load(items.stream());
    }

    @Override
    public int item2iidx(Long i) {
        return wrapper.item2iidx(i);
    }

    @Override
    public Long iidx2item(int iidx) {
        return wrapper.iidx2item(iidx);
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

}
