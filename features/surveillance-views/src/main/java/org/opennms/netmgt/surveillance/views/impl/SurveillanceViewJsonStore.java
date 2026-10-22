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
package org.opennms.netmgt.surveillance.views.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.surveillance.views.SurveillanceView;
import org.opennms.netmgt.surveillance.views.SurveillanceViewDao;
import org.opennms.netmgt.surveillance.views.SurveillanceViewDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Stores surveillance view definitions in the generic JSON key-value store
 * ({@link JsonStore}), replacing the legacy {@code etc/surveillance-views.xml}
 * (which is not read: pre-existing customized views are recreated through the
 * REST API/UI). Each view is one JSON blob under the {@value #CONTEXT}
 * context, keyed by a generated UUID; the default-view setting is a single
 * blob in its own {@value #SETTINGS_CONTEXT} context so enumerating views
 * stays clean.
 *
 * <p>{@link #init()} seeds the stock default view (the same grid the legacy
 * XML shipped) into an empty store, so fresh installs and upgrades start with
 * a working view.
 *
 * <p>Listing the catalog ({@link #findAll()} / {@link #findByName(String)})
 * enumerates the context; the catalog is human-scale (a handful of views), so
 * this is inexpensive.
 */
public class SurveillanceViewJsonStore implements SurveillanceViewDao {

    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewJsonStore.class);

    static final String CONTEXT = "surveillance-views";
    static final String SETTINGS_CONTEXT = "surveillance-views-settings";
    static final String SETTINGS_KEY = "settings";

    /** Stable id for the seeded stock view. */
    static final String DEFAULT_VIEW_ID = "default";
    static final String DEFAULT_VIEW_NAME = "default";

    private final JsonStore jsonStore;
    private final Gson gson = new Gson();

    public SurveillanceViewJsonStore(final JsonStore jsonStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
    }

    /**
     * Wire as the bean {@code init-method}: seed the stock default view if the
     * store holds no views at all. Once any view exists, restarts never add or
     * resurrect anything.
     */
    public void init() {
        try {
            if (!findAll().isEmpty()) {
                return;
            }
            final SurveillanceView view = stockDefaultView();
            view.setId(DEFAULT_VIEW_ID);
            view.setCreated(new Date());
            put(view);
            setDefaultViewId(DEFAULT_VIEW_ID);
            LOG.info("Seeded the stock default surveillance view");
        } catch (final Exception e) {
            // Never block context startup on a seeding hiccup.
            LOG.warn("Could not seed the default surveillance view: {}", e.getMessage());
        }
    }

    /** The grid the legacy etc/surveillance-views.xml shipped by default. */
    private static SurveillanceView stockDefaultView() {
        final SurveillanceView view = new SurveillanceView();
        view.setName(DEFAULT_VIEW_NAME);
        view.setRefreshSeconds(SurveillanceView.DEFAULT_REFRESH_SECONDS);
        view.setOwner("system");
        view.getRows().add(new SurveillanceViewDef("Routers", "Routers"));
        view.getRows().add(new SurveillanceViewDef("Switches", "Switches"));
        view.getRows().add(new SurveillanceViewDef("Servers", "Servers"));
        view.getColumns().add(new SurveillanceViewDef("PROD", "Production"));
        view.getColumns().add(new SurveillanceViewDef("TEST", "Test"));
        view.getColumns().add(new SurveillanceViewDef("DEV", "Development"));
        return view;
    }

    @Override
    public List<SurveillanceView> findAll() {
        final Map<String, String> entries = jsonStore.enumerateContext(CONTEXT);
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }
        final List<SurveillanceView> views = new ArrayList<>(entries.size());
        for (final Map.Entry<String, String> entry : entries.entrySet()) {
            final SurveillanceView view = deserialize(entry.getKey(), entry.getValue());
            if (view != null) {
                views.add(view);
            }
        }
        return views;
    }

    @Override
    public SurveillanceView get(final String id) {
        if (id == null) {
            return null;
        }
        return jsonStore.get(id, CONTEXT).map(json -> deserialize(id, json)).orElse(null);
    }

    @Override
    public SurveillanceView findByName(final String name) {
        if (name == null) {
            return null;
        }
        return findAll().stream().filter(v -> name.equals(v.getName())).findFirst().orElse(null);
    }

    @Override
    public String save(final SurveillanceView view) {
        Objects.requireNonNull(view);
        view.setId(UUID.randomUUID().toString());
        if (view.getCreated() == null) {
            view.setCreated(new Date());
        }
        put(view);
        return view.getId();
    }

    @Override
    public void update(final SurveillanceView view) {
        Objects.requireNonNull(view);
        Objects.requireNonNull(view.getId(), "cannot update a view without an id");
        put(view);
    }

    @Override
    public void delete(final SurveillanceView view) {
        if (view == null || view.getId() == null) {
            return;
        }
        jsonStore.delete(view.getId(), CONTEXT);
        // A dangling default would silently resolve to nothing; clear it.
        if (view.getId().equals(getDefaultViewId())) {
            setDefaultViewId(null);
        }
    }

    @Override
    public String getDefaultViewId() {
        return jsonStore.get(SETTINGS_KEY, SETTINGS_CONTEXT).map(json -> {
            try {
                final Settings settings = gson.fromJson(json, Settings.class);
                return settings == null ? null : settings.defaultViewId;
            } catch (final JsonSyntaxException e) {
                LOG.warn("Ignoring malformed surveillance view settings: {}", e.getMessage());
                return null;
            }
        }).orElse(null);
    }

    @Override
    public void setDefaultViewId(final String viewId) {
        final Settings settings = new Settings();
        settings.defaultViewId = viewId;
        jsonStore.put(SETTINGS_KEY, gson.toJson(settings), SETTINGS_CONTEXT);
    }

    private void put(final SurveillanceView view) {
        jsonStore.put(view.getId(), gson.toJson(toStored(view)), CONTEXT);
    }

    private SurveillanceView deserialize(final String id, final String json) {
        try {
            final Stored stored = gson.fromJson(json, Stored.class);
            if (stored == null) {
                return null;
            }
            final SurveillanceView view = new SurveillanceView();
            view.setId(id);
            view.setName(stored.name);
            view.setRefreshSeconds(stored.refreshSeconds);
            view.setRows(fromStoredDefs(stored.rows));
            view.setColumns(fromStoredDefs(stored.columns));
            view.setOwner(stored.owner);
            view.setCreated(stored.created != null ? new Date(stored.created) : null);
            view.setLastModified(stored.lastModified != null ? new Date(stored.lastModified) : null);
            return view;
        } catch (final JsonSyntaxException e) {
            LOG.warn("Skipping malformed surveillance view '{}': {}", id, e.getMessage());
            return null;
        }
    }

    private static Stored toStored(final SurveillanceView view) {
        final Stored stored = new Stored();
        stored.name = view.getName();
        stored.refreshSeconds = view.getRefreshSeconds();
        stored.rows = toStoredDefs(view.getRows());
        stored.columns = toStoredDefs(view.getColumns());
        stored.owner = view.getOwner();
        stored.created = view.getCreated() != null ? view.getCreated().getTime() : null;
        stored.lastModified = view.getLastModified() != null ? view.getLastModified().getTime() : null;
        return stored;
    }

    private static List<StoredDef> toStoredDefs(final List<SurveillanceViewDef> defs) {
        final List<StoredDef> storedDefs = new ArrayList<>(defs.size());
        for (final SurveillanceViewDef def : defs) {
            final StoredDef storedDef = new StoredDef();
            storedDef.label = def.getLabel();
            storedDef.categories = new ArrayList<>(def.getCategories());
            storedDef.reportCategory = def.getReportCategory();
            storedDefs.add(storedDef);
        }
        return storedDefs;
    }

    private static List<SurveillanceViewDef> fromStoredDefs(final List<StoredDef> storedDefs) {
        final List<SurveillanceViewDef> defs = new ArrayList<>();
        if (storedDefs == null) {
            return defs;
        }
        for (final StoredDef storedDef : storedDefs) {
            final SurveillanceViewDef def = new SurveillanceViewDef();
            def.setLabel(storedDef.label);
            if (storedDef.categories != null) {
                def.setCategories(new ArrayList<>(storedDef.categories));
            }
            def.setReportCategory(storedDef.reportCategory);
            defs.add(def);
        }
        return defs;
    }

    /** On-the-wire (in-store) shape: timestamps as epoch millis, id is the key. */
    private static final class Stored {
        private String name;
        private Integer refreshSeconds;
        private List<StoredDef> rows;
        private List<StoredDef> columns;
        private String owner;
        private Long created;
        private Long lastModified;
    }

    private static final class StoredDef {
        private String label;
        private List<String> categories;
        private String reportCategory;
    }

    private static final class Settings {
        private String defaultViewId;
    }
}
