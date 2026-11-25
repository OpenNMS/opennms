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
package org.opennms.features.config.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.AbstractResourceTypesDao;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.ResourceTypesDaoCm;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * {@link ResourceTypesDaoCm} against the configuration manager and the JSON store on a real database: the handlers
 * the DAO registers are called by the configuration manager, and the validation rejects what the DAO can't read.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-config-service.xml"})
@JUnitTemporaryDatabase
public class ResourceTypesDaoCmIT {
    private static final String CONFIG_NAME = ResourceTypesDaoCm.CONFIG_NAME;
    private static final Path SHIPPED = Path.of("../../../opennms-base-assembly/src/main/filtered/etc/resource-types.d");

    @Autowired
    private ConfigurationManagerService cm;

    @Autowired
    private JsonStore jsonStore;

    private ResourceTypesDaoCm dao;

    @Before
    public void setUp() throws IOException {
        final DataCollectionConfigDao dcDao = mock(DataCollectionConfigDao.class);
        when(dcDao.getConfiguredResourceTypes()).thenReturn(Map.of());
        when(dcDao.getLastUpdate()).thenReturn(new Date(0));
        DataCollectionConfigFactory.setInstance(dcDao);

        // what the 37.0.0 changelog registers
        final ConfigDefinition definition = XsdHelper.buildConfigDefinition(CONFIG_NAME, "resource-types.xsd",
                "resource-types", ConfigurationManagerService.BASE_PATH, true);
        cm.registerConfigDefinition(CONFIG_NAME, definition);
        importShipped("postgresql-resource");
        importShipped("jmx-resource");

        dao = new ResourceTypesDaoCm();
        dao.bind(cm, jsonStore);
    }

    @After
    public void tearDown() {
        cm.unregisterSchema(CONFIG_NAME);
        DataCollectionConfigFactory.setInstance(null);
    }

    @Test
    public void loadsStoredConfigurations() {
        assertEquals("PostgreSQL Tablespace", dao.getResourceTypeByName("pgTableSpace").getLabel());
        assertNotNull(dao.getResourceTypeByName("kafkaLag"));
    }

    @Test
    public void createdUpdatedAndDeletedConfigurationsAreTracked() throws IOException {
        assertNull(dao.getResourceTypeByName("nxosIntf"));
        final Date initial = dao.getLastUpdate();

        importShipped("nxos-resource");
        assertNotNull(dao.getResourceTypeByName("nxosIntf"));
        assertTrue(dao.getLastUpdate().after(initial));

        final String renamed = cm.getJSONStrConfiguration(CONFIG_NAME, "nxos-resource").get().replace("\"nxosIntf\"", "\"nxosRenamed\"");
        cm.updateConfiguration(CONFIG_NAME, "nxos-resource", new JsonAsString(renamed), true);
        assertNull(dao.getResourceTypeByName("nxosIntf"));
        assertNotNull(dao.getResourceTypeByName("nxosRenamed"));

        cm.unregisterConfiguration(CONFIG_NAME, "nxos-resource");
        assertNull(dao.getResourceTypeByName("nxosRenamed"));
    }

    @Test
    public void rejectsConfigurationViolatingTheSchema() {
        // no label and no strategies
        assertThrows(ValidationException.class, () -> cm.registerConfiguration(CONFIG_NAME, "broken",
                new JsonAsString("{\"resourceType\":[{\"name\":\"broken\"}]}")));
        assertNull(dao.getResourceTypeByName("broken"));
    }

    @Test
    public void publishesToTheJsonStore() throws Exception {
        final String published = jsonStore.get(AbstractResourceTypesDao.JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT).get();
        final ResourceTypes read = JacksonUtils.createDefaultObjectMapper().readValue(published, ResourceTypes.class);
        assertTrue(read.getResourceTypes().stream().anyMatch(t -> "pgTableSpace".equals(t.getName())));
        assertTrue(read.getResourceTypes().stream().anyMatch(t -> "kafkaLag".equals(t.getName())));
    }

    private void importShipped(final String configId) throws IOException {
        final String xml = Files.readString(SHIPPED.resolve(configId + ".xml"), StandardCharsets.UTF_8);
        final ConfigDefinition definition = cm.getRegisteredConfigDefinition(CONFIG_NAME).get();
        cm.registerConfiguration(CONFIG_NAME, configId, new JsonAsString(XsdHelper.getConverter(definition).xmlToJson(xml)));
    }
}
