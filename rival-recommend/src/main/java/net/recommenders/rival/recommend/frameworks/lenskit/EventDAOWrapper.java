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
package net.recommenders.rival.recommend.frameworks.lenskit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.recommenders.rival.core.DataModel;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;

/**
 *
 * @author Alejandro
 */
public class EventDAOWrapper implements EventDAO {

    private EventCollectionDAO wrapper;

    public EventDAOWrapper(DataModel<Long, Long> model) {
        List<Rating> events = new ArrayList();
        RatingBuilder rb = new RatingBuilder();
        for (Long u : model.getUserItemPreferences().keySet()) {
            rb.setUserId(u);
            for (Long i : model.getUserItemPreferences().get(u).keySet()) {
                rb.setItemId(i);
                rb.setRating(model.getUserItemPreferences().get(u).get(i));
                Set<Long> timestamps = model.getUserItemTimestamps().get(u).get(i);
                long t = -1;
                if (timestamps != null) {
                    for (Long tt : timestamps) {
                        t = tt;
                        break;
                    }
                }
                rb.setTimestamp(t);
                events.add(rb.build());
            }
        }
        wrapper = new EventCollectionDAO(events);
    }

    @Override
    public Cursor<Event> streamEvents() {
        return wrapper.streamEvents();
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type) {
        return wrapper.streamEvents(type);
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type, SortOrder so) {
        return wrapper.streamEvents(type, so);
    }
}
