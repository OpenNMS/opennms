/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.topology.views.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.topology.views.TopologyView;
import org.opennms.netmgt.topology.views.TopologyViewDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Stores custom topology views in the generic JSON key-value store
 * ({@link JsonStore}) instead of a dedicated table. Each view is one JSON blob
 * under the {@value #CONTEXT} context, keyed by a generated UUID. The view's
 * {@code definition} is kept as an opaque string inside the blob (the store
 * never interprets the canvas), and timestamps travel as epoch millis.
 *
 * <p>Listing the catalog ({@link #findAll()} / {@link #findByName(String)})
 * enumerates the context; the catalog is human-scale (a handful of views), so
 * this is inexpensive.
 */
public class TopologyViewJsonStore implements TopologyViewDao {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyViewJsonStore.class);

    static final String CONTEXT = "topology-views";

    /** Stable id/key for the seeded baseline view. */
    static final String DEFAULT_VIEW_ID = "default";
    static final String DEFAULT_VIEW_NAME = "Default";
    private static final String EMPTY_DEFINITION =
            "{\"nodes\":[],\"edges\":[],\"labels\":[],\"viewport\":{\"zoom\":1,\"panX\":0,\"panY\":0}}";

    private final JsonStore jsonStore;
    private final Gson gson = new Gson();

    public TopologyViewJsonStore(final JsonStore jsonStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
    }

    /** Wire as the bean {@code init-method}: seed the Default view if absent. */
    public void init() {
        try {
            // Key off the stable id, not the name: a user can rename the Default
            // view (its key stays DEFAULT_VIEW_ID), and re-seeding by name would
            // then overwrite that renamed view with a fresh empty Default. We
            // only re-seed if the baseline entry was actually deleted -- and also
            // skip if some other view already claims the "Default" name (e.g. the
            // user deleted the seeded view and created their own "Default"), so a
            // restart can't resurrect a duplicate name.
            if (get(DEFAULT_VIEW_ID) == null && findByName(DEFAULT_VIEW_NAME) == null) {
                final TopologyView def = new TopologyView();
                def.setId(DEFAULT_VIEW_ID);
                def.setName(DEFAULT_VIEW_NAME);
                def.setDefinition(EMPTY_DEFINITION);
                def.setOwner("system");
                def.setCreated(new Date());
                put(def);
                LOG.info("Seeded the Default topology view");
            }
        } catch (final Exception e) {
            // Never block context startup on a seeding hiccup.
            LOG.warn("Could not seed the Default topology view: {}", e.getMessage());
        }
    }

    @Override
    public List<TopologyView> findAll() {
        final Map<String, String> entries = jsonStore.enumerateContext(CONTEXT);
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }
        final List<TopologyView> views = new ArrayList<>(entries.size());
        for (final Map.Entry<String, String> entry : entries.entrySet()) {
            final TopologyView view = deserialize(entry.getKey(), entry.getValue());
            if (view != null) {
                views.add(view);
            }
        }
        return views;
    }

    @Override
    public TopologyView get(final String id) {
        if (id == null) {
            return null;
        }
        return jsonStore.get(id, CONTEXT).map(json -> deserialize(id, json)).orElse(null);
    }

    @Override
    public TopologyView findByName(final String name) {
        if (name == null) {
            return null;
        }
        return findAll().stream().filter(v -> name.equals(v.getName())).findFirst().orElse(null);
    }

    @Override
    public String save(final TopologyView view) {
        Objects.requireNonNull(view);
        view.setId(UUID.randomUUID().toString());
        if (view.getCreated() == null) {
            view.setCreated(new Date());
        }
        put(view);
        return view.getId();
    }

    @Override
    public void update(final TopologyView view) {
        Objects.requireNonNull(view);
        Objects.requireNonNull(view.getId(), "cannot update a view without an id");
        put(view);
    }

    @Override
    public void delete(final TopologyView view) {
        if (view != null && view.getId() != null) {
            jsonStore.delete(view.getId(), CONTEXT);
        }
    }

    private void put(final TopologyView view) {
        jsonStore.put(view.getId(), gson.toJson(toStored(view)), CONTEXT);
    }

    private TopologyView deserialize(final String id, final String json) {
        try {
            final Stored stored = gson.fromJson(json, Stored.class);
            if (stored == null) {
                return null;
            }
            final TopologyView view = new TopologyView();
            view.setId(id);
            view.setName(stored.name);
            view.setDefinition(stored.definition);
            view.setOwner(stored.owner);
            view.setCreated(stored.created != null ? new Date(stored.created) : null);
            view.setLastModified(stored.lastModified != null ? new Date(stored.lastModified) : null);
            return view;
        } catch (final JsonSyntaxException e) {
            LOG.warn("Skipping malformed topology view '{}': {}", id, e.getMessage());
            return null;
        }
    }

    private static Stored toStored(final TopologyView view) {
        final Stored stored = new Stored();
        stored.name = view.getName();
        stored.definition = view.getDefinition();
        stored.owner = view.getOwner();
        stored.created = view.getCreated() != null ? view.getCreated().getTime() : null;
        stored.lastModified = view.getLastModified() != null ? view.getLastModified().getTime() : null;
        return stored;
    }

    /** On-the-wire (in-store) shape: timestamps as epoch millis, id is the key. */
    private static final class Stored {
        private String name;
        private String definition;
        private String owner;
        private Long created;
        private Long lastModified;
    }
}
