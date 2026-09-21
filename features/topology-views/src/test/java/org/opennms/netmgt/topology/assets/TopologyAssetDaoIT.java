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
package org.opennms.netmgt.topology.assets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.topology.assets.impl.TopologyAssetKvStore;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Exercises the {@link TopologyAssetDao} against a real (temporary) database:
 * metadata through the {@code PostgresJsonStore} ({@code kvstore_jsonb}) and
 * bytes through a {@code PostgresBlobStore} ({@code kvstore_bytea}) — the same
 * pairing the production wiring builds.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TopologyAssetDaoIT {

    @Autowired
    private JsonStore jsonStore;

    private TopologyAssetDao dao;

    private static final byte[] PNG_ISH = "not-really-a-png-but-bytes".getBytes(StandardCharsets.UTF_8);

    @Before
    public void setUp() {
        // Same construction as the production component-dao wiring: the blob
        // store is built from the (here: temporary) datasource internally.
        dao = new TopologyAssetKvStore(jsonStore, DataSourceFactory.getInstance());
    }

    private static TopologyAsset background(final String name) {
        final TopologyAsset asset = new TopologyAsset();
        asset.setName(name);
        asset.setKind(TopologyAsset.KIND_BACKGROUND);
        asset.setMimeType("image/png");
        asset.setOwner("admin");
        return asset;
    }

    @Test
    @JUnitTemporaryDatabase
    public void roundTripsMetadataAndBytes() {
        assertThat(dao.findAll(), hasSize(0));

        final TopologyAsset saved = dao.save(background("dc floor plan"), PNG_ISH);
        assertNotNull(saved.getId());
        assertThat(saved.getSizeBytes(), is((long) PNG_ISH.length));
        assertNotNull(saved.getCreated());
        assertNotNull(saved.getLastModified());

        final Optional<TopologyAsset> read = dao.get(saved.getId());
        assertTrue(read.isPresent());
        assertThat(read.get().getName(), is("dc floor plan"));
        assertThat(read.get().getKind(), is(TopologyAsset.KIND_BACKGROUND));
        assertThat(read.get().getMimeType(), is("image/png"));
        assertThat(read.get().getOwner(), is("admin"));
        assertThat(read.get().getSizeBytes(), is((long) PNG_ISH.length));
        assertThat(read.get().getCreated(), is(saved.getCreated()));

        final Optional<byte[]> bytes = dao.getBytes(saved.getId());
        assertTrue(bytes.isPresent());
        assertArrayEquals(PNG_ISH, bytes.get());
    }

    @Test
    @JUnitTemporaryDatabase
    public void listsMetadataForAllAssets() {
        dao.save(background("a"), PNG_ISH);
        final TopologyAsset icon = new TopologyAsset();
        icon.setName("router glyph");
        icon.setKind(TopologyAsset.KIND_ICON);
        icon.setMimeType("image/webp");
        dao.save(icon, PNG_ISH);

        final List<TopologyAsset> all = dao.findAll();
        assertThat(all, hasSize(2));
        assertTrue(all.stream().anyMatch(a -> TopologyAsset.KIND_ICON.equals(a.getKind())));
        assertTrue(all.stream().anyMatch(a -> TopologyAsset.KIND_BACKGROUND.equals(a.getKind())));
    }

    @Test
    @JUnitTemporaryDatabase
    public void deleteRemovesMetadataAndBytes() {
        final TopologyAsset saved = dao.save(background("doomed"), PNG_ISH);

        assertTrue(dao.delete(saved.getId()));
        assertFalse(dao.get(saved.getId()).isPresent());
        assertFalse(dao.getBytes(saved.getId()).isPresent());

        // Deleting again (or a never-existing id) reports false, not an error.
        assertFalse(dao.delete(saved.getId()));
        assertFalse(dao.delete("no-such-asset"));
    }
}
