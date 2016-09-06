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
import net.recommenders.rival.core.TemporalDataModelIF;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;

/**
 * Lenskit's EventDAO wrapper for {@link net.recommenders.rival.core.DataModel}.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class EventDAOWrapper implements EventDAO {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 120150729L;
    /**
     * Lenskit's EventDAO that will be used as wrapper.
     */
    private EventCollectionDAO wrapper;

    /**
     * Constructs the wrapper using the provided model.
     *
     * @param model the model to be used to create the wrapped model
     */
    public EventDAOWrapper(final TemporalDataModelIF<Long, Long> model) {
        List<Rating> events = new ArrayList<Rating>();
        RatingBuilder rb = new RatingBuilder();
        for (Long u : model.getUsers()) {
            rb.setUserId(u);
            for (Long i : model.getUserItems(u)) {
                rb.setItemId(i);
                rb.setRating(model.getUserItemPreference(u, i));
                Iterable<Long> timestamps = model.getUserItemTimestamps(u, i);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor<Event> streamEvents() {
        return wrapper.streamEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends Event> Cursor<E> streamEvents(final Class<E> type) {
        return wrapper.streamEvents(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends Event> Cursor<E> streamEvents(final Class<E> type, final SortOrder so) {
        return wrapper.streamEvents(type, so);
    }
}
