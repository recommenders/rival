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

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.recommenders.rival.core.TemporalDataModelIF;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.data.dao.EntityQuery;
import org.lenskit.data.dao.Query;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingBuilder;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;

/**
 * Lenskit's EventDAO wrapper for {@link net.recommenders.rival.core.DataModel}.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class EventDAOWrapper implements DataAccessObject {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 170150729L;
    /**
     * Lenskit's DataAccessObject that will be used as wrapper.
     */
    private DataAccessObject wrapper;

    /**
     * Constructs the wrapper using the provided model.
     *
     * @param model the model to be used to create the wrapped model
     */
    public EventDAOWrapper(final TemporalDataModelIF<Long, Long> model) {
        List<Rating> events = new ArrayList<>();
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
        wrapper = EntityCollectionDAO.create(events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<EntityType> getEntityTypes() {
        return wrapper.getEntityTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongSet getEntityIds(EntityType et) {
        return wrapper.getEntityIds(et);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entity lookupEntity(EntityType et, long l) {
        return wrapper.lookupEntity(et, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends Entity> E lookupEntity(EntityType et, long l, Class<E> type) {
        return wrapper.lookupEntity(et, l, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectStream<Entity> streamEntities(EntityType et) {
        return wrapper.streamEntities(et);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends Entity> ObjectStream<E> streamEntities(EntityQuery<E> eq) {
        return wrapper.streamEntities(eq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends Entity> ObjectStream<IdBox<List<E>>> streamEntityGroups(EntityQuery<E> eq, TypedName<Long> tn) {
        return wrapper.streamEntityGroups(eq, tn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query<Entity> query(EntityType et) {
        return wrapper.query(et);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V extends Entity> Query<V> query(Class<V> type) {
        return wrapper.query(type);
    }
}
