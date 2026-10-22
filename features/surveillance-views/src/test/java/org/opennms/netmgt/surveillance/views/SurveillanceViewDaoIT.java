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
package org.opennms.netmgt.surveillance.views;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.surveillance.views.impl.SurveillanceViewJsonStore;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Exercises the JSON-key-value-store-backed {@link SurveillanceViewDao}
 * against a real (temporary) database, including the seeding of the stock
 * default view into an empty store. The {@link JsonStore} is a
 * {@code PostgresJsonStore} over the temp datasource (the standard test
 * wiring), so this verifies round-trips through the {@code kvstore_jsonb}
 * table.
 *
 * <p>Name-uniqueness is enforced by the REST resource (a 409 on a name
 * clash), not the DAO; that is covered by {@code SurveillanceViewRestServiceIT}.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SurveillanceViewDaoIT {

    @Autowired
    private JsonStore jsonStore;

    private SurveillanceViewJsonStore dao;

    @Before
    public void setUp() {
        dao = new SurveillanceViewJsonStore(jsonStore);
    }

    private static SurveillanceView exampleView(final String name) {
        final SurveillanceView view = new SurveillanceView();
        view.setName(name);
        view.setRefreshSeconds(120);
        view.setOwner("admin");
        final SurveillanceViewDef row = new SurveillanceViewDef("Routers", "Routers", "Core");
        row.setReportCategory("Network Reports");
        view.getRows().add(row);
        view.getColumns().add(new SurveillanceViewDef("PROD", "Production"));
        view.getColumns().add(new SurveillanceViewDef("TEST", "Test"));
        return view;
    }

    @Test
    @JUnitTemporaryDatabase
    public void canCrudView() {
        assertThat(dao.findAll(), hasSize(0));

        final String id = dao.save(exampleView("Ops"));
        assertNotNull("save should assign an id", id);
        assertThat(dao.findAll(), hasSize(1));

        // Read back, all fields round-trip
        final SurveillanceView loaded = dao.get(id);
        assertThat(loaded, notNullValue());
        assertThat(loaded.getId(), is(id));
        assertThat(loaded.getName(), is("Ops"));
        assertThat(loaded.getRefreshSeconds(), is(120));
        assertThat(loaded.getOwner(), is("admin"));
        assertThat(loaded.getRows(), hasSize(1));
        assertThat(loaded.getRows().get(0).getLabel(), is("Routers"));
        assertThat(loaded.getRows().get(0).getCategories(), contains("Routers", "Core"));
        assertThat(loaded.getRows().get(0).getReportCategory(), is("Network Reports"));
        assertThat(loaded.getColumns(), hasSize(2));
        assertThat(loaded.getColumns().get(0).getLabel(), is("PROD"));
        assertThat(loaded.getColumns().get(1).getCategories(), contains("Test"));
        assertThat(loaded.getColumns().get(1).getReportCategory(), nullValue());
        assertThat(loaded.getCreated(), notNullValue());
        assertThat(loaded.getLastModified(), nullValue());

        // Lookup by (unique) name
        assertThat(dao.findByName("Ops"), notNullValue());

        // Update
        loaded.setName("Ops (edited)");
        loaded.setLastModified(new Date());
        dao.update(loaded);

        assertThat(dao.findByName("Ops"), nullValue());
        assertThat(dao.findByName("Ops (edited)"), notNullValue());
        assertThat(dao.get(id).getLastModified(), notNullValue());

        // Delete
        dao.delete(loaded);
        assertThat(dao.findAll(), hasSize(0));
    }

    @Test
    @JUnitTemporaryDatabase
    public void findByNameReturnsNullWhenMissing() {
        assertThat(dao.findByName("does-not-exist"), nullValue());
    }

    @Test
    @JUnitTemporaryDatabase
    public void defaultViewIdRoundTripsAndClearsWithItsView() {
        assertThat(dao.getDefaultViewId(), nullValue());

        final String id = dao.save(exampleView("Ops"));
        dao.setDefaultViewId(id);
        assertThat(dao.getDefaultViewId(), is(id));

        // Deleting the default view must not leave a dangling default.
        dao.delete(dao.get(id));
        assertThat(dao.getDefaultViewId(), nullValue());
    }

    @Test
    @JUnitTemporaryDatabase
    public void initSeedsTheStockDefaultViewIntoAnEmptyStore() {
        dao.init();

        assertThat(dao.findAll(), hasSize(1));

        final SurveillanceView seeded = dao.findByName("default");
        assertThat(seeded, notNullValue());
        assertThat(seeded.getRefreshSeconds(), is(300));
        assertThat(seeded.getOwner(), is("system"));
        assertThat(seeded.getRows(), hasSize(3));
        assertThat(seeded.getRows().get(0).getLabel(), is("Routers"));
        assertThat(seeded.getRows().get(0).getCategories(), contains("Routers"));
        assertThat(seeded.getColumns(), hasSize(3));
        assertThat(seeded.getColumns().get(0).getLabel(), is("PROD"));
        assertThat(seeded.getColumns().get(0).getCategories(), contains("Production"));
        assertThat(dao.getDefaultViewId(), is(seeded.getId()));

        // Seeding is idempotent: no duplicates on restart.
        dao.init();
        assertThat(dao.findAll(), hasSize(1));
    }

    @Test
    @JUnitTemporaryDatabase
    public void initNeverAddsToANonEmptyStore() {
        final String id = dao.save(exampleView("Ops"));

        // A store with any views is left alone: nothing is seeded or
        // resurrected, and the default setting is not touched.
        dao.init();
        assertThat(dao.findAll(), hasSize(1));
        assertThat(dao.findByName("default"), nullValue());
        assertThat(dao.getDefaultViewId(), nullValue());

        // ...even if the seeded view itself was deliberately deleted
        dao.delete(dao.get(id));
        final String seededId = dao.save(exampleView("Custom"));
        dao.init();
        assertThat(dao.findAll(), hasSize(1));
        assertThat(dao.get(seededId).getName(), is("Custom"));
    }
}
