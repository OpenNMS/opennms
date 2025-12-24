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
package org.opennms.web.rest.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.SnmpConfigRestApi;
import org.opennms.web.rest.v2.cm.SnmpConfigRestCmJaxbConfigTestDao;
import org.opennms.web.rest.v2.model.SnmpConfigInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:mock-cm-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpConfigRestServiceIT {

    @Autowired
    private SnmpConfigRestApi snmpConfigRestApi;

    @Autowired
    private SnmpConfigRestCmJaxbConfigTestDao snmpConfigRestCmJaxbConfigTestDao;

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    @Before
    public void setUp() {
        try {
            initCm();
        } catch (Exception e) {
            Assert.fail("initCm failed");
        }
    }

    @After
    public void after() throws IOException {
        configurationManagerService.unregisterSchema(snmpConfigRestCmJaxbConfigTestDao.getConfigName());
    }

    // See: org.opennms.features.config.service.impl.SnmpConfigCmJaxbConfigDaoIT
    private void initCm() throws Exception {
        ConfigDefinition def = XsdHelper.buildConfigDefinition(snmpConfigRestCmJaxbConfigTestDao.getConfigName(),
                "snmp-config.xsd", "snmp-config",
                ConfigurationManagerService.BASE_PATH, false);

        configurationManagerService.registerConfigDefinition(snmpConfigRestCmJaxbConfigTestDao.getConfigName(), def);
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("snmp-config.xml");
        Optional<ConfigDefinition> registeredDef = configurationManagerService.getRegisteredConfigDefinition(snmpConfigRestCmJaxbConfigTestDao.getConfigName());
        String xmlStr = Files.readString(Path.of(xmlPath.getPath()));
        ConfigConverter converter = XsdHelper.getConverter(registeredDef.get());

        JsonAsString configObject = new JsonAsString(converter.xmlToJson(xmlStr));
        configurationManagerService.registerConfiguration(snmpConfigRestCmJaxbConfigTestDao.getConfigName(),
                snmpConfigRestCmJaxbConfigTestDao.getDefaultConfigId(), configObject);
    }

    @Test
    public void testGetSnmpConfig() throws Exception {
        Response response = snmpConfigRestApi.getSnmpConfig();
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        String responseJson = (String) response.getEntity();
        assertNotNull(responseJson);

        ObjectMapper mapper = new ObjectMapper();
        SnmpConfig config = mapper.readValue(responseJson, SnmpConfig.class);
        assertNotNull(config);

        assertFalse(config.getDefinitions().isEmpty());
        assertFalse(config.getSnmpProfiles().getSnmpProfiles().isEmpty());

        assertEquals(2, config.getDefinitions().size());
        assertEquals(3, config.getSnmpProfiles().getSnmpProfiles().size());

        assertEquals("public", config.getReadCommunity());
        assertEquals("private", config.getWriteCommunity());
    }

    @Test
    public void testGetSnmpConfigForIp() throws Exception {
        Response response = snmpConfigRestApi.getConfigForIp("10.0.0.1", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        SnmpAgentConfig config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);

        assertEquals("v2c", config.getVersionAsString());
        assertEquals(7000, config.getTTL().longValue());
        assertEquals("profile2", config.getProfileLabel());

        assertEquals("public", config.getReadCommunity());
        assertEquals("private", config.getWriteCommunity());
    }

    @Ignore("Ignoring in order to fix IT errors running snmpConfigRestApi.addDefinition: java.sql.SQLException: HikariDataSource HikariDataSource (HikariPool-1) has been closed.")
    // @Test
    @Transactional
    public void testAddAndRemoveSnmpDefinitions() throws Exception {
        // Add a new definition
        SnmpConfigInfoDto dto = new SnmpConfigInfoDto();
        dto.setFirstIpAddress("10.99.0.1");
        dto.setLastIpAddress("10.99.0.2");
        dto.setLocation("Default");
        dto.setReadCommunity("testing99");

        Response response = snmpConfigRestApi.addDefinition(dto);
        assertNotNull(response);
        assertEquals(201, response.getStatus());

        // Check if config was updated with new community string
        response = snmpConfigRestApi.getConfigForIp("10.99.0.1", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        SnmpAgentConfig config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);
        assertEquals("testing99", config.getReadCommunity());

        response = snmpConfigRestApi.getConfigForIp("10.99.0.2", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);
        assertEquals("testing99", config.getReadCommunity());

        // make sure community string for previously-existing item was not changed
        response = snmpConfigRestApi.getConfigForIp("10.0.0.1", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);
        assertEquals("public", config.getReadCommunity());
        assertEquals("profile2", config.getProfileLabel());

        // Delete part of the definition
        response = snmpConfigRestApi.removeDefinition("10.99.0.1", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        // Check if config reverted to the default
        response = snmpConfigRestApi.getConfigForIp("10.99.0.1", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);
        assertEquals("public", config.getReadCommunity());

        // config for not-yet-deleted item should still be there
        response = snmpConfigRestApi.getConfigForIp("10.99.0.2", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);
        assertEquals("testing99", config.getReadCommunity());

        // Delete the rest of the definition
        response = snmpConfigRestApi.removeDefinition("10.99.0.2", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        // config for not-yet-deleted item should still be there
        response = snmpConfigRestApi.getConfigForIp("10.99.0.2", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);
        assertEquals("public", config.getReadCommunity());
    }
}
