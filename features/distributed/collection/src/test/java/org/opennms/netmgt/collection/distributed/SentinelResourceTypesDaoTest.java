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
package org.opennms.netmgt.collection.distributed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.AbstractResourceTypesDao;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.opennms.netmgt.config.datacollection.StorageStrategy;

public class SentinelResourceTypesDaoTest {

    private static final String KEY = AbstractResourceTypesDao.JSON_STORE_KEY;
    private static final String CONTEXT = ConfigDaoConstants.JSON_KEY_STORE_CONTEXT;

    private JsonStore jsonStore;

    @Before
    public void setUp() {
        final DataCollectionConfigDao dcDao = mock(DataCollectionConfigDao.class);
        when(dcDao.getConfiguredResourceTypes()).thenReturn(Map.of("fromDc", type("fromDc", "Data Collection"),
                "overridden", type("overridden", "Data Collection")));
        when(dcDao.getLastUpdate()).thenReturn(new Date(1000));
        DataCollectionConfigFactory.setInstance(dcDao);
        jsonStore = mock(JsonStore.class);
    }

    @After
    public void tearDown() {
        DataCollectionConfigFactory.setInstance(null);
    }

    @Test
    public void readsPublishedTypes() throws Exception {
        publish(1, type("published", "Published"), type("overridden", "Published"));
        final SentinelResourceTypesDao dao = new SentinelResourceTypesDao(jsonStore, 0);

        assertEquals("Published", dao.getResourceTypeByName("published").getLabel());
        assertEquals("published types override the data collection ones", "Published", dao.getResourceTypeByName("overridden").getLabel());
        assertEquals("Data Collection", dao.getResourceTypeByName("fromDc").getLabel());
    }

    @Test
    public void missingKeyDoesNotBlockOrFail() {
        when(jsonStore.getLastUpdated(KEY, CONTEXT)).thenReturn(OptionalLong.empty());
        final SentinelResourceTypesDao dao = new SentinelResourceTypesDao(jsonStore, 0);

        assertEquals(2, dao.getResourceTypes().size());
        assertNull(dao.getResourceTypeByName("published"));
    }

    @Test
    public void storeFailureKeepsKnownTypes() throws Exception {
        publish(1, type("published", "Published"));
        final SentinelResourceTypesDao dao = new SentinelResourceTypesDao(jsonStore, 0);
        assertEquals("Published", dao.getResourceTypeByName("published").getLabel());

        when(jsonStore.getLastUpdated(KEY, CONTEXT)).thenThrow(new RuntimeException("database down"));
        assertEquals("Published", dao.getResourceTypeByName("published").getLabel());
    }

    @Test
    public void picksUpNewerDocumentOnly() throws Exception {
        publish(1, type("first", "First"));
        final SentinelResourceTypesDao dao = new SentinelResourceTypesDao(jsonStore, 0);
        assertEquals("First", dao.getResourceTypeByName("first").getLabel());
        final Date firstUpdate = dao.getLastUpdate();

        // unchanged timestamp: the document isn't read again
        dao.getResourceTypes();
        verify(jsonStore, times(1)).get(KEY, CONTEXT);

        publish(2, type("second", "Second"));
        assertEquals("Second", dao.getResourceTypeByName("second").getLabel());
        assertNull(dao.getResourceTypeByName("first"));
        assertTrue(dao.getLastUpdate().after(firstUpdate));
    }

    @Test
    public void pollsAtMostOncePerInterval() throws Exception {
        publish(1, type("first", "First"));
        final SentinelResourceTypesDao dao = new SentinelResourceTypesDao(jsonStore, 60_000);
        dao.getResourceTypes();
        publish(2, type("second", "Second"));

        assertNull("not polled again within the interval", dao.getResourceTypeByName("second"));
        verify(jsonStore, times(1)).getLastUpdated(KEY, CONTEXT);
    }

    private void publish(final long lastUpdated, final ResourceType... types) throws Exception {
        final ResourceTypes document = new ResourceTypes();
        document.setResourceTypes(List.of(types));
        when(jsonStore.getLastUpdated(KEY, CONTEXT)).thenReturn(OptionalLong.of(lastUpdated));
        when(jsonStore.get(KEY, CONTEXT)).thenReturn(Optional.of(JacksonUtils.createDefaultObjectMapper().writeValueAsString(document)));
    }

    private static ResourceType type(final String name, final String label) {
        final ResourceType type = new ResourceType();
        type.setName(name);
        type.setLabel(label);
        type.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"));
        type.setStorageStrategy(new StorageStrategy("org.opennms.netmgt.collection.support.IndexStorageStrategy"));
        return type;
    }
}
