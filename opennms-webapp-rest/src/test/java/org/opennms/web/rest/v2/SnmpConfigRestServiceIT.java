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
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.SnmpConfigRestApi;
import org.opennms.web.rest.v2.model.SnmpConfigInfoDto;
import org.opennms.web.rest.v2.model.SnmpConfigProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
    "classpath*:/META-INF/opennms/component-service.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
    "classpath:mock-cm-dao.xml",
    "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
    "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml",
    "classpath*:/META-INF/opennms/applicationContext-config-service.xml",
    "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpConfigRestServiceIT extends AbstractSpringJerseyRestTestCase {
    public SnmpConfigRestServiceIT () {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private SnmpConfigRestApi snmpConfigRestApi;

    @Before
    public void setUp() {
        try {
            // NOTE: Make sure 'snmpPeerFactory' setup in 'applicationContext-rest-test.xml' is
            // set to 'MockSnmpPeerFactory'
            // <bean id="snmpPeerFactory" class="org.opennms.netmgt.config.mock.MockSnmpPeerFactory"/>
            URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("snmp-config.xml");
            FileSystemResource resource = new FileSystemResource(xmlPath.getPath());

            SnmpPeerFactory factory = new SnmpPeerFactory(resource);
            SnmpPeerFactory.setInstance(factory);
        } catch (Exception e) {
            Assert.fail("setUp failed");
        }
    }

    @After
    public void after() {
        SnmpPeerFactory.setInstance(new SnmpPeerFactory());
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
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

        assertEquals(3, config.getDefinitions().size());
        assertEquals(3, config.getSnmpProfiles().getSnmpProfiles().size());

        assertEquals("public", config.getReadCommunity());
        assertEquals("private", config.getWriteCommunity());

        final SnmpProfile profile1 = config.getSnmpProfiles().getSnmpProfiles().stream()
                .filter(p -> p.getLabel().equalsIgnoreCase("profile1")).findFirst().orElse(null);
        assertNotNull(profile1);
        assertEquals("profile1", profile1.getLabel());
        assertEquals("v1", profile1.getVersion());
        assertEquals("horizon", profile1.getReadCommunity());
        assertEquals(10000, profile1.getTimeout().intValue());

        final SnmpProfile profile2 = config.getSnmpProfiles().getSnmpProfiles().stream()
                .filter(p -> p.getLabel().equalsIgnoreCase("profile2")).findFirst().orElse(null);
        assertNotNull(profile2);
        assertEquals("profile2", profile2.getLabel());
        assertEquals("v1", profile2.getVersion());
        assertNull(profile2.getReadCommunity());
        assertEquals(6000, profile2.getTTL().longValue());
        assertEquals("iphostname LIKE '%opennms%'", profile2.getFilterExpression());

        final SnmpProfile profile3 = config.getSnmpProfiles().getSnmpProfiles().stream()
                .filter(p -> p.getLabel().equalsIgnoreCase("profile3")).findFirst().orElse(null);
        assertNotNull(profile3);
        assertEquals("profile3", profile3.getLabel());
        assertEquals("v1", profile3.getVersion());
        assertEquals("meridian", profile3.getReadCommunity());
        assertNull(profile3.getTTL());
        assertEquals("IPADDR IPLIKE 172.1.*.*", profile3.getFilterExpression());
    }

    @Test
    public void testGetSnmpConfigForIpHavingSpecificConfigurationForRange() throws Exception {
        final List<String> ipAddresses = List.of("10.0.0.1", "10.4.252.40");

        ipAddresses.stream().forEach(address -> {
            final Response response = snmpConfigRestApi.getConfigForIp(address, "Default");
            assertNotNull(response);
            assertEquals(200, response.getStatus());

            final SnmpAgentConfig config = (SnmpAgentConfig) response.getEntity();
            assertNotNull(config);

            assertEquals("v2c", config.getVersionAsString());
            assertEquals(7000, config.getTTL().longValue());
            assertEquals("profile2", config.getProfileLabel());

            assertEquals("public", config.getReadCommunity());
            assertEquals("private", config.getWriteCommunity());
        });
    }

    @Test
    public void testGetSnmpConfigForIpHavingSpecificConfiguration() throws Exception {
        final Response response = snmpConfigRestApi.getConfigForIp("10.9.9.1", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        final SnmpAgentConfig config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);

        assertEquals("v2c", config.getVersionAsString());
        assertEquals(999, config.getTTL().longValue());
        assertNull(config.getProfileLabel());

        assertEquals("public991", config.getReadCommunity());
        assertEquals("private991", config.getWriteCommunity());
    }

    @Test
    public void testGetSnmpConfigForIpHavingDefaultConfiguration() throws Exception {
        final List<String> ipAddresses = List.of("10.10.0.1", "192.168.20.1");

        ipAddresses.stream().forEach(address -> {
            final Response response = snmpConfigRestApi.getConfigForIp(address, "Default");
            assertNotNull(response);
            assertEquals(200, response.getStatus());

            final SnmpAgentConfig config = (SnmpAgentConfig) response.getEntity();
            assertNotNull(config);

            assertEquals("v2c", config.getVersionAsString());
            assertEquals(800, config.getTimeout());
            assertNull(config.getProfileLabel());

            assertEquals("public", config.getReadCommunity());
            assertEquals("private", config.getWriteCommunity());
        });
    }

    @Test
    public void testGetConfigForIpBadRequests() {
        // invalid IP
        Response response = snmpConfigRestApi.getConfigForIp("10.", "Default");
        assertNotNull(response);
        assertEquals(400, response.getStatus());

        String message = (String) response.getEntity();
        assertEquals("Missing or invalid 'ipAddress'.", message);

        // missing IP
        response = snmpConfigRestApi.getConfigForIp("", "Default");
        assertNotNull(response);
        assertEquals(400, response.getStatus());

        message = (String) response.getEntity();
        assertEquals("Missing or invalid 'ipAddress'.", message);

        // invalid location
        response = snmpConfigRestApi.getConfigForIp("10.0.0.1", "INVALID");
        assertNotNull(response);
        assertEquals(400, response.getStatus());

        message = (String) response.getEntity();
        assertEquals("Missing or invalid 'location'.", message);
    }

    @Test
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
        assertEquals(204, response.getStatus());

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
        assertEquals(204, response.getStatus());

        // config for not-yet-deleted item should still be there
        response = snmpConfigRestApi.getConfigForIp("10.99.0.2", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);
        assertEquals("public", config.getReadCommunity());
    }

    @Test
    public void testAddDefinitionBadRequest() {
        // null dto
        Response response = snmpConfigRestApi.addDefinition(null);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertEquals("Missing or invalid request parameters.", message);

        // missing firstIpAddress
        SnmpConfigInfoDto dto = new SnmpConfigInfoDto();
        // dto.setFirstIpAddress("10.99.0.1");
        dto.setFirstIpAddress("");
        dto.setLastIpAddress("10.99.0.2");
        dto.setLocation("Default");
        dto.setReadCommunity("testing99");

        response = snmpConfigRestApi.addDefinition(dto);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals("Missing or invalid 'firstIpAddress'.", message);

        // invalid firstIpAddress
        dto = new SnmpConfigInfoDto();
        dto.setFirstIpAddress("10.");
        dto.setLastIpAddress("10.99.0.2");
        dto.setLocation("Default");
        dto.setReadCommunity("testing99");

        response = snmpConfigRestApi.addDefinition(dto);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals("Missing or invalid 'firstIpAddress'.", message);

        // invalid lastIpAddress
        dto = new SnmpConfigInfoDto();
        dto.setFirstIpAddress("10.0.0.1");
        dto.setLastIpAddress("10.");
        dto.setLocation("Default");
        dto.setReadCommunity("testing99");

        response = snmpConfigRestApi.addDefinition(dto);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals("Invalid 'lastIpAddress'.", message);

        // invalid location
        dto = new SnmpConfigInfoDto();
        dto.setFirstIpAddress("10.0.0.1");
        dto.setLastIpAddress("10.0.0.9");
        dto.setLocation("LocationNONE");
        dto.setReadCommunity("testing99");

        response = snmpConfigRestApi.addDefinition(dto);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals("Missing or invalid 'location'.", message);
    }

    @Test
    public void testSaveProfile() throws Exception {
        SnmpConfigProfileDto dto = new SnmpConfigProfileDto();
        dto.setLabel("profile4");
        dto.setFilterExpression("IPADDR IPLIKE 160.1.2.*");
        dto.setVersion("v2c");
        dto.setReadCommunity("public160");
        dto.setWriteCommunity("private160");

        final Response saveResponse = snmpConfigRestApi.saveProfile(dto);
        assertNotNull(saveResponse);
        assertEquals(204, saveResponse.getStatus());

        final Response response = snmpConfigRestApi.getSnmpConfig();
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        String responseJson = (String) response.getEntity();
        assertNotNull(responseJson);

        ObjectMapper mapper = new ObjectMapper();
        SnmpConfig config = mapper.readValue(responseJson, SnmpConfig.class);
        assertNotNull(config);

        assertEquals(4, config.getSnmpProfiles().getSnmpProfiles().size());

        final SnmpProfile profile4 = config.getSnmpProfiles().getSnmpProfiles().stream()
                .filter(p -> p.getLabel().equalsIgnoreCase("profile4")).findFirst().orElse(null);
        assertNotNull(profile4);
        assertEquals("profile4", profile4.getLabel());
        assertEquals("v2c", profile4.getVersion());
        assertEquals("public160", profile4.getReadCommunity());
        assertEquals("private160", profile4.getWriteCommunity());
        assertEquals("IPADDR IPLIKE 160.1.2.*", profile4.getFilterExpression());
    }

    @Test
    public void testRemoveProfile() throws Exception {
        final Response removeResponse = snmpConfigRestApi.removeProfile("profile1");
        assertNotNull(removeResponse);
        assertEquals(204, removeResponse.getStatus());

        final Response configResponse = snmpConfigRestApi.getSnmpConfig();
        assertNotNull(configResponse);
        assertEquals(200, configResponse.getStatus());

        String json = (String) configResponse.getEntity();
        assertNotNull(json);

        ObjectMapper mapper = new ObjectMapper();
        SnmpConfig config = mapper.readValue(json, SnmpConfig.class);
        assertNotNull(config);

        assertEquals(2, config.getSnmpProfiles().getSnmpProfiles().size());

        final SnmpProfile profile1 = config.getSnmpProfiles().getSnmpProfiles().stream()
                .filter(p -> p.getLabel().equalsIgnoreCase("profile1")).findFirst().orElse(null);
        assertNull(profile1);

        final SnmpProfile profile2 = config.getSnmpProfiles().getSnmpProfiles().stream()
                .filter(p -> p.getLabel().equalsIgnoreCase("profile2")).findFirst().orElse(null);
        assertNotNull(profile2);

        final SnmpProfile profile3 = config.getSnmpProfiles().getSnmpProfiles().stream()
                .filter(p -> p.getLabel().equalsIgnoreCase("profile3")).findFirst().orElse(null);
        assertNotNull(profile3);
    }

    @Test
    public void testSaveProfileBadRequest() {
        SnmpConfigProfileDto dto = new SnmpConfigProfileDto();
        dto.setLabel("");
        dto.setFilterExpression("IPADDR IPLIKE 160.1.2.*");
        dto.setVersion("v2c");
        dto.setReadCommunity("public160");
        dto.setWriteCommunity("private160");

        final Response response = snmpConfigRestApi.saveProfile(dto);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertEquals("Missing or invalid 'label'.", message);
    }

    @Test
    public void testRemoveProfileBadRequest() {
        // non-existent profile
        Response response = snmpConfigRestApi.removeProfile("profileNONE");
        assertNotNull(response);
        assertEquals(404, response.getStatus());
        String message = (String) response.getEntity();
        assertEquals("Profile with label profileNONE not found.", message);

        // missing label in request
        response = snmpConfigRestApi.removeProfile("");
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals("Missing or invalid 'label'.", message);
    }
}
