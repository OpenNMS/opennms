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
package org.opennms.netmgt.topology.views;

import static org.hamcrest.MatcherAssert.assertThat;
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
import org.opennms.netmgt.topology.views.impl.TopologyViewJsonStore;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Exercises the JSON-key-value-store-backed {@link TopologyViewDao} against a
 * real (temporary) database. The {@link JsonStore} is a {@code PostgresJsonStore}
 * over the temp datasource (the standard test wiring), so this verifies the
 * store round-trips views through the {@code kvstore_jsonb} table.
 *
 * <p>Note name-uniqueness is enforced by the REST resource (a 409 on a name
 * clash), not the DAO, so there is no unique-constraint test here; that is
 * covered by {@code TopologyViewRestServiceIT}.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TopologyViewDaoIT {

    @Autowired
    private JsonStore jsonStore;

    private TopologyViewJsonStore dao;

    private static final String DEFINITION =
            "{\"nodes\":[{\"id\":\"n1\",\"label\":\"core-sw1\",\"x\":120,\"y\":80}]," +
            "\"edges\":[],\"labels\":[],\"viewport\":{\"zoom\":1,\"panX\":0,\"panY\":0}}";

    @Before
    public void setUp() {
        dao = new TopologyViewJsonStore(jsonStore);
    }

    @Test
    @JUnitTemporaryDatabase
    public void canCrudView() {
        assertThat(dao.findAll(), hasSize(0));

        // Create
        final TopologyView view = new TopologyView();
        view.setName("Core DC");
        view.setDefinition(DEFINITION);
        view.setOwner("admin");
        view.setCreated(new Date());
        final String id = dao.save(view);
        assertNotNull("save should assign an id", id);

        assertThat(dao.findAll(), hasSize(1));

        // Read back, all fields round-trip
        final TopologyView loaded = dao.get(id);
        assertThat(loaded, notNullValue());
        assertThat(loaded.getId(), is(id));
        assertThat(loaded.getName(), is("Core DC"));
        assertThat(loaded.getDefinition(), is(DEFINITION));
        assertThat(loaded.getOwner(), is("admin"));
        assertThat(loaded.getCreated(), notNullValue());
        assertThat(loaded.getLastModified(), nullValue());

        // Lookup by (unique) name
        assertThat(dao.findByName("Core DC"), notNullValue());

        // Update
        loaded.setName("Core DC (edited)");
        loaded.setLastModified(new Date());
        dao.update(loaded);

        assertThat(dao.findByName("Core DC"), nullValue());
        assertThat(dao.findByName("Core DC (edited)"), notNullValue());
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
    public void initSeedsTheDefaultViewIdempotently() {
        assertThat(dao.findByName("Default"), nullValue());

        dao.init();
        assertThat(dao.findByName("Default"), notNullValue());

        // Running again must not create a second Default.
        dao.init();
        assertThat(dao.findAll(), hasSize(1));
    }

    @Test
    @JUnitTemporaryDatabase
    public void initDoesNotOverwriteARenamedDefault() {
        dao.init();
        final TopologyView seeded = dao.findByName("Default");
        assertThat(seeded, notNullValue());

        // Rename and edit the seeded Default; its store key is unchanged.
        seeded.setName("My Dashboard");
        seeded.setDefinition(DEFINITION);
        dao.update(seeded);

        // A later startup must not resurrect a fresh empty Default over it.
        dao.init();
        assertThat(dao.findAll(), hasSize(1));
        assertThat(dao.findByName("Default"), nullValue());
        final TopologyView reloaded = dao.get(seeded.getId());
        assertThat(reloaded.getName(), is("My Dashboard"));
        assertThat(reloaded.getDefinition(), is(DEFINITION));
    }
}
