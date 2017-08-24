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

import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastUserIndex;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 *
 * @author Alejandro
 */
public class UserIndexWrapper implements FastUserIndex<Long> {

    private final FastUserIndex<Long> wrapper;

    public UserIndexWrapper(TemporalDataModelIF<Long, Long> training, TemporalDataModelIF<Long, Long> test) {
        Set<Long> users = new HashSet<>();
        for (Long u : training.getUsers()) {
            users.add(u);
        }
        for (Long u : test.getUsers()) {
            users.add(u);
        }
        wrapper = SimpleFastUserIndex.load(users.stream());
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

}
