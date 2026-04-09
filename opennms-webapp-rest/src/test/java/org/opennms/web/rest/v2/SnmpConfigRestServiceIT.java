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

import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.SnmpConfigRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    private static final org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();

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

        SnmpConfig config = (SnmpConfig) response.getEntity();
        assertConfigValid(config);
    }

    private void assertConfigValid(SnmpConfig config) {
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
        Definition definition = new Definition();
        definition.addRange(new Range("10.99.0.1", "10.99.0.2"));
        definition.setLocation("Default");
        definition.setReadCommunity("testing99");

        Response response = snmpConfigRestApi.addDefinition(definition);
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
        response = snmpConfigRestApi.removeDefinition("10.99.0.1", null, null, "Default");
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
        response = snmpConfigRestApi.removeDefinition("10.99.0.2", null, null, "Default");
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
    public void testAddAndRemoveDefinitionRanges() {
        // get the original config
        final SnmpConfig originalConfig = getCurrentConfig();

        // Add a new definition
        Definition definition = new Definition();
        definition.addRange(new Range("10.99.0.1", "10.99.0.99"));
        definition.addSpecific("10.0.0.1");
        definition.setLocation("Default");
        definition.setReadCommunity("testing99");

        Response response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(201, response.getStatus());

        // config should have changed
        final SnmpConfig updatedConfigAfterAdd = getCurrentConfig();
        assertNotEquals(originalConfig, updatedConfigAfterAdd);

        // Check if config was updated with new community string
        List<String> ipsToTest =
                List.of("10.0.0.1", "10.99.0.1", "10.99.0.2", "10.99.0.98", "10.99.0.99");

        ipsToTest.forEach(ip -> {
            Response resp = snmpConfigRestApi.getConfigForIp(ip, "Default");
            assertNotNull(resp);
            assertEquals(200, resp.getStatus());

            SnmpAgentConfig config = (SnmpAgentConfig) resp.getEntity();
            assertNotNull(config);
            assertEquals("testing99", config.getReadCommunity());
        });

        response = snmpConfigRestApi.removeDefinition(null, "10.99.0.1-10.99.0.99", null, "Default");
        assertNotNull(response);
        assertEquals(204, response.getStatus());

        // config should have changed
        final SnmpConfig updatedConfigAfterDelete = getCurrentConfig();
        assertNotEquals(updatedConfigAfterDelete, updatedConfigAfterAdd);

        // Check if config reverted to the default
        List<String> deletedRangeIps =
                List.of("10.99.0.1", "10.99.0.2", "10.99.0.98", "10.99.0.99");

        deletedRangeIps.forEach(ip -> {
            Response resp = snmpConfigRestApi.getConfigForIp(ip, "Default");
            assertNotNull(resp);
            assertEquals(200, resp.getStatus());

            SnmpAgentConfig config = (SnmpAgentConfig) resp.getEntity();
            assertNotNull(config);
            assertEquals("public", config.getReadCommunity());
        });

        // this one was not yet deleted, should still have updated config
        response = snmpConfigRestApi.getConfigForIp("10.0.0.1", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        SnmpAgentConfig config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);
        assertEquals("testing99", config.getReadCommunity());

        // Now delete the specific item
        response = snmpConfigRestApi.removeDefinition("10.0.0.1", null, null, "Default");
        assertNotNull(response);
        assertEquals(204, response.getStatus());

        // config should have changed
        final SnmpConfig updatedConfigAfterSecondDelete = getCurrentConfig();
        assertNotEquals(updatedConfigAfterSecondDelete, updatedConfigAfterAdd);

        response = snmpConfigRestApi.getConfigForIp("10.0.0.1", "Default");
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        config = (SnmpAgentConfig) response.getEntity();
        assertNotNull(config);
        assertEquals("public", config.getReadCommunity());
    }

    @Test
    public void testAddIpMatch() {
        // get the original config
        final SnmpConfig originalConfig = getCurrentConfig();

        // Add a new definition
        Definition definition = new Definition();
        definition.addIpMatch("10.0.0.*");
        definition.setLocation("Default");
        definition.setReadCommunity("testing99");

        Response response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(201, response.getStatus());

        // config should have changed
        final SnmpConfig updatedConfigAfterAdd = getCurrentConfig();
        assertNotEquals(originalConfig, updatedConfigAfterAdd);

        // Check if config was updated with new community string
        assertTrue(updatedConfigAfterAdd.getDefinitions().stream()
                .anyMatch(d -> d.getIpMatches().contains("10.0.0.*")));

        final int originalDefinitionCount = originalConfig.getDefinitions().size();
        final int updatedDefinitionCount = updatedConfigAfterAdd.getDefinitions().size();
        assertEquals(originalDefinitionCount + 1, updatedDefinitionCount);
    }

    @Test
    public void testRemoveDefinitionWithOutOfRangeSpecificsDoesNotChangeConfig() {
        // get the original config
        final SnmpConfig originalConfig = getCurrentConfig();

        // try to delete a non-existent specific IP
        Response response = snmpConfigRestApi.removeDefinition("99.99.99.99", null, null, "Default");
        assertNotNull(response);
        assertEquals(400, response.getStatus());

        String message = (String) response.getEntity();
        assertEquals(SnmpConfigRestService.DEFINITION_NO_ITEMS_REMOVED_MESSAGE, message);

        // get the updated config
        final SnmpConfig updatedConfig = getCurrentConfig();

        // config should not have changed
        assertEquals(originalConfig, updatedConfig);
    }

    @Test
    public void testRemoveDefinitionWithOutOfRangeRangesAndSpecificsAndIpMatchesDoesNotChangeConfig() {
        // get the original config
        final SnmpConfig originalConfig = getCurrentConfig();

        // try to delete non-existent items
        final String specifics = "99.99.99.99";
        final String ranges = "91.0.0.1-91.0.0.20,93.0.0.1-95.0.0.9";
        final String ipMatches = "88.0.0.*";

        Response response = snmpConfigRestApi.removeDefinition(specifics, ranges, ipMatches, "Default");
        assertNotNull(response);
        assertEquals(400, response.getStatus());

        String message = (String) response.getEntity();
        assertEquals(SnmpConfigRestService.DEFINITION_NO_ITEMS_REMOVED_MESSAGE, message);

        // get the updated config
        final SnmpConfig updatedConfig = getCurrentConfig();

        // config should not have changed
        assertEquals(originalConfig, updatedConfig);
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
        Definition definition = new Definition();
        definition.addRange(new Range("", "10.99.0.2"));
        definition.setLocation("Default");
        definition.setReadCommunity("testing99");

        response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals(SnmpConfigRestService.DEFINITION_MISSING_CONTENTS_MESSAGE, message);

        // invalid firstIpAddress, this is actually validating in SnmpConfigManager
        final Definition invalidFirstIpDefinition = new Definition();
        invalidFirstIpDefinition.addRange(new Range("10.", "10.99.0.2"));
        invalidFirstIpDefinition.setLocation("Default");
        invalidFirstIpDefinition.setReadCommunity("testing99");

        response = snmpConfigRestApi.addDefinition(invalidFirstIpDefinition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals("Invalid range begin IP address: 10.", message);

        // invalid lastIpAddress
        final Definition invalidLastIpDefinition = new Definition();
        invalidLastIpDefinition.addRange(new Range("10.0.0.1", "10."));
        invalidLastIpDefinition.setLocation("Default");
        invalidLastIpDefinition.setReadCommunity("testing99");

        response = snmpConfigRestApi.addDefinition(invalidLastIpDefinition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals("Invalid range end IP address: 10.", message);

        // invalid ipMatch expression
        definition = new Definition();
        definition.addIpMatch("10.0.0.");
        definition.setReadCommunity("testing99");

        response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals("Invalid IP match expression: '10.0.0.'.", message);

        // invalid location
        definition = new Definition();
        definition.addRange(new Range("10.0.0.1", "10.0.0.9"));
        definition.setLocation("LocationNONE");
        definition.setReadCommunity("testing99");

        response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        message = (String) response.getEntity();
        assertEquals("Missing or invalid 'location'.", message);
    }

    @Test
    public void testSaveProfile() throws Exception {
        SnmpProfile profile = new SnmpProfile();
        profile.setLabel("profile4");
        profile.setFilterExpression("IPADDR IPLIKE 160.1.2.*");
        profile.setVersion("v2c");
        profile.setReadCommunity("public160");
        profile.setWriteCommunity("private160");

        final Response saveResponse = snmpConfigRestApi.saveProfile(profile);
        assertNotNull(saveResponse);
        assertEquals(204, saveResponse.getStatus());

        final Response response = snmpConfigRestApi.getSnmpConfig();
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        SnmpConfig config = (SnmpConfig) response.getEntity();
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

        SnmpConfig config = (SnmpConfig) configResponse.getEntity();
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
        SnmpProfile profile = new SnmpProfile();
        profile.setLabel("");
        profile.setFilterExpression("IPADDR IPLIKE 160.1.2.*");
        profile.setVersion("v2c");
        profile.setReadCommunity("public160");
        profile.setWriteCommunity("private160");

        final Response response = snmpConfigRestApi.saveProfile(profile);
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

    @Test
    public void testDownloadJson() {
        final Response response = snmpConfigRestApi.downloadConfig(null);
        assertEquals(200, response.getStatus());

        SnmpConfig config = null;

        try {
            byte[] bytes = (byte[]) response.getEntity();
            assertNotNull(bytes);

            String json = new String(bytes, StandardCharsets.UTF_8);
            config = mapper.readValue(json, SnmpConfig.class);
        } catch (Exception e) {
            Assert.fail("Error retrieving or parsing downloaded Json file.");
        }

        assertConfigValid(config);
    }

    @Test
    public void testDownloadXml() {
        final Response response = snmpConfigRestApi.downloadConfig("xml");
        assertEquals(200, response.getStatus());

        SnmpConfig config = null;

        try {
            byte[] bytes = (byte[]) response.getEntity();
            assertNotNull(bytes);

            String xml = new String(bytes, StandardCharsets.UTF_8);
            config = JaxbUtils.unmarshal(SnmpConfig.class, xml);
        } catch (Exception e) {
            Assert.fail("Error retrieving or parsing downloaded XML file.");
        }

        assertConfigValid(config);
    }

    @Test
    public void testUploadJsonValid() {
        String filename = "snmp-config.valid.json";
        InputStream is = getClass().getResourceAsStream("/SNMP-CONF/" + filename);
        assertNotNull(is);

        Attachment attachment = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(filename);
        when(attachment.getContentDisposition()).thenReturn(cd);
        when(attachment.getObject(InputStream.class)).thenReturn(is);

        Response resp = snmpConfigRestApi.uploadConfig(attachment);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testUploadJsonNull() {
        Response resp = snmpConfigRestApi.uploadConfig(null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());

        String errorMessage = (String) resp.getEntity();
        assertEquals("Missing configuration file.", errorMessage);
    }

    @Test
    public void testUploadJsonInvalid() {
        String filename = "snmp-config.invalid.json";
        InputStream is = getClass().getResourceAsStream("/SNMP-CONF/" + filename);
        assertNotNull(is);

        Attachment attachment = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(filename);
        when(attachment.getContentDisposition()).thenReturn(cd);
        when(attachment.getObject(InputStream.class)).thenReturn(is);

        Response resp = snmpConfigRestApi.uploadConfig(attachment);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());

        String errorMessage = (String) resp.getEntity();
        assertEquals("Invalid configuration file.", errorMessage);
    }

    @Test
    public void testUploadJsonBadFormat() {
        String filename = "snmp-config.bad-format.json";
        InputStream is = getClass().getResourceAsStream("/SNMP-CONF/" + filename);
        assertNotNull(is);

        Attachment attachment = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(filename);
        when(attachment.getContentDisposition()).thenReturn(cd);
        when(attachment.getObject(InputStream.class)).thenReturn(is);

        Response resp = snmpConfigRestApi.uploadConfig(attachment);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());

        String errorMessage = (String) resp.getEntity();
        assertEquals("Invalid configuration file.", errorMessage);
    }

    @Test
    public void testUploadXmlValid() {
        String filename = "snmp-config.valid.xml";
        InputStream is = getClass().getResourceAsStream("/SNMP-CONF/" + filename);
        assertNotNull(is);

        Attachment attachment = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(filename);
        when(attachment.getContentDisposition()).thenReturn(cd);
        when(attachment.getObject(InputStream.class)).thenReturn(is);

        Response resp = snmpConfigRestApi.uploadConfigXml(attachment);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testUploadXmlNull() {
        Response resp = snmpConfigRestApi.uploadConfigXml(null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());

        String errorMessage = (String) resp.getEntity();
        assertEquals("Missing configuration file.", errorMessage);
    }

    @Test
    public void testUploadXmlInvalid() {
        String filename = "snmp-config.invalid.xml";
        InputStream is = getClass().getResourceAsStream("/SNMP-CONF/" + filename);
        assertNotNull(is);

        Attachment attachment = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(filename);
        when(attachment.getContentDisposition()).thenReturn(cd);
        when(attachment.getObject(InputStream.class)).thenReturn(is);

        Response resp = snmpConfigRestApi.uploadConfigXml(attachment);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());

        String errorMessage = (String) resp.getEntity();
        assertEquals("Invalid configuration file.", errorMessage);
    }

    @Test
    public void testUploadXmlBadFormat() {
        String filename = "snmp-config.bad-format.xml";
        InputStream is = getClass().getResourceAsStream("/SNMP-CONF/" + filename);
        assertNotNull(is);

        Attachment attachment = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(filename);
        when(attachment.getContentDisposition()).thenReturn(cd);
        when(attachment.getObject(InputStream.class)).thenReturn(is);

        Response resp = snmpConfigRestApi.uploadConfigXml(attachment);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());

        String errorMessage = (String) resp.getEntity();
        assertEquals("Invalid configuration file.", errorMessage);
    }

    @Test
    public void testAddDefinitionWithRangeAndIpMatchFails() {
        // Definition with both range and ipMatch
        Definition definition = new Definition();
        definition.addRange(new Range("10.1.1.1", "10.1.1.10"));
        definition.addIpMatch("10.1.1.*");
        definition.setLocation("Default");
        definition.setReadCommunity("test");

        Response response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertEquals(SnmpConfigRestService.DEFINITION_CANNOT_MIX_RANGE_AND_IPMATCH_MESSAGE, message);
    }

    @Test
    public void testAddDefinitionWithSpecificAndIpMatchFails() {
        // Definition with both specific and ipMatch
        Definition definition = new Definition();
        definition.addSpecific("10.2.2.2");
        definition.addIpMatch("10.2.2.*");
        definition.setLocation("Default");
        definition.setReadCommunity("test");

        Response response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertEquals(SnmpConfigRestService.DEFINITION_CANNOT_MIX_RANGE_AND_IPMATCH_MESSAGE, message);
    }

    @Test
    public void testAddDefinitionWithInvalidSpecificIp() {
        // Definition with invalid specific IP address
        final Definition definition = new Definition();
        definition.addSpecific("not-an-ip");
        definition.setLocation("Default");
        definition.setReadCommunity("test");

        Response response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertEquals("Invalid specific IP address: not-an-ip", message);
    }

    @Test
    public void testAddDefinitionWithInvalidRangeBeginIp() {
        // Definition with invalid range begin IP
        final Definition definition = new Definition();
        definition.addRange(new Range("invalid-begin", "10.1.1.10"));
        definition.setLocation("Default");
        definition.setReadCommunity("test");

        Response response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertEquals("Invalid range begin IP address: invalid-begin", message);
    }

    @Test
    public void testAddDefinitionWithInvalidRangeEndIp() {
        // Definition with invalid range end IP
        final Definition definition = new Definition();
        definition.addRange(new Range("10.1.1.1", "invalid-end"));
        definition.setLocation("Default");
        definition.setReadCommunity("test");

        Response response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertEquals("Invalid range end IP address: invalid-end", message);
    }

    @Test
    public void testAddDefinitionWithMixedIpVersionsInRange() {
        // Definition with IPv4 begin and IPv6 end
        Definition definition = new Definition();
        definition.addRange(new Range("10.1.1.1", "::1"));
        definition.setLocation("Default");
        definition.setReadCommunity("test");

        Response response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertEquals("Invalid range: begin and end must be same IP version. begin=10.1.1.1, end=::1", message);
    }

    @Test
    public void testAddDefinitionWithInvalidIpMatch() {
        // Definition with invalid ip-match pattern (fails XSD validation)
        Definition definition = new Definition();
        definition.addIpMatch("invalid-pattern!");
        definition.setLocation("Default");
        definition.setReadCommunity("test");

        Response response = snmpConfigRestApi.addDefinition(definition);
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        String message = (String) response.getEntity();
        assertTrue(message.contains("Invalid IP match expression: 'invalid-pattern!'."));
    }

    @Test
    public void testSaveDefaultOverrides_Success() {
        // Get initial config
        Response response = snmpConfigRestApi.getSnmpConfig();
        assertEquals(200, response.getStatus());
        SnmpConfig initialConfig = (SnmpConfig) response.getEntity();

        assertEquals("public", initialConfig.getReadCommunity());
        assertEquals("private", initialConfig.getWriteCommunity());
        assertEquals(Integer.valueOf(800), initialConfig.getTimeout());
        assertEquals(Integer.valueOf(3), initialConfig.getRetry());

        // Create new default overrides
        org.opennms.netmgt.config.snmp.Configuration newDefaults =
            new org.opennms.netmgt.config.snmp.Configuration(
                9999,                // port
                5,                   // retry
                5000,                // timeout
                "newPublic",         // readCommunity
                "newPrivate",        // writeCommunity
                "proxy.example.com", // proxyHost
                "v2c",               // version
                20,                  // maxVarsPerPdu
                3,                   // maxRepetitions
                2000,                // maxRequestSize
                "secName",           // securityName
                2,                   // securityLevel
                "authPass",          // authPassphrase
                "SHA",               // authProtocol
                "engineId",          // engineId
                "ctxEngineId",       // contextEngineId
                "ctxName",           // contextName
                "privPass",          // privacyPassphrase
                "DES",               // privacyProtocol
                "entId"              // enterpriseId
            );

        // Save the overrides
        response = snmpConfigRestApi.saveDefaultOverrides(newDefaults);
        assertNotNull(response);
        assertEquals(204, response.getStatus());

        // Verify the config was updated
        response = snmpConfigRestApi.getSnmpConfig();
        assertEquals(200, response.getStatus());
        SnmpConfig updatedConfig = (SnmpConfig) response.getEntity();

        assertEquals(Integer.valueOf(9999), updatedConfig.getPort());
        assertEquals(Integer.valueOf(5), updatedConfig.getRetry());
        assertEquals(Integer.valueOf(5000), updatedConfig.getTimeout());
        assertEquals("newPublic", updatedConfig.getReadCommunity());
        assertEquals("newPrivate", updatedConfig.getWriteCommunity());
        assertEquals("proxy.example.com", updatedConfig.getProxyHost());
        assertEquals("v2c", updatedConfig.getVersion());
        assertEquals(Integer.valueOf(20), updatedConfig.getMaxVarsPerPdu());
        assertEquals(Integer.valueOf(3), updatedConfig.getMaxRepetitions());
        assertEquals(Integer.valueOf(2000), updatedConfig.getMaxRequestSize());
        assertEquals("secName", updatedConfig.getSecurityName());
        assertEquals(Integer.valueOf(2), updatedConfig.getSecurityLevel());
        assertEquals("authPass", updatedConfig.getAuthPassphrase());
        assertEquals("SHA", updatedConfig.getAuthProtocol());
        assertEquals("engineId", updatedConfig.getEngineId());
        assertEquals("ctxEngineId", updatedConfig.getContextEngineId());
        assertEquals("ctxName", updatedConfig.getContextName());
        assertEquals("privPass", updatedConfig.getPrivacyPassphrase());
        assertEquals("DES", updatedConfig.getPrivacyProtocol());
        assertEquals("entId", updatedConfig.getEnterpriseId());
    }

    @Test
    public void testSaveDefaultOverrides_SetValuesToNull() {
        // Get initial config with non-null values
        Response response = snmpConfigRestApi.getSnmpConfig();
        assertEquals(200, response.getStatus());
        SnmpConfig initialConfig = (SnmpConfig) response.getEntity();

        // Verify initial state has non-null values
        assertNotNull(initialConfig);
        assertNotNull(initialConfig.getReadCommunity());
        assertNotNull(initialConfig.getWriteCommunity());

        // Create config with null values
        org.opennms.netmgt.config.snmp.Configuration nullDefaults =
            new org.opennms.netmgt.config.snmp.Configuration(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
            );

        // Save the overrides
        response = snmpConfigRestApi.saveDefaultOverrides(nullDefaults);
        assertNotNull(response);
        assertEquals(204, response.getStatus());

        // Verify nullable values are now null
        response = snmpConfigRestApi.getSnmpConfig();
        assertEquals(200, response.getStatus());
        SnmpConfig updatedConfig = (SnmpConfig) response.getEntity();

        assertNull(updatedConfig.getReadCommunity());
        assertNull(updatedConfig.getWriteCommunity());
        assertNull(updatedConfig.getProxyHost());
        assertNull(updatedConfig.getVersion());
        assertNull(updatedConfig.getSecurityName());
        assertNull(updatedConfig.getAuthPassphrase());
    }

    @Test
    public void testSaveDefaultOverrides_PartialUpdate() {
        // Get initial config
        Response response = snmpConfigRestApi.getSnmpConfig();
        assertEquals(200, response.getStatus());
        SnmpConfig initialConfig = (SnmpConfig) response.getEntity();

        // Create config with mix of null and non-null values
        org.opennms.netmgt.config.snmp.Configuration partialDefaults =
            new org.opennms.netmgt.config.snmp.Configuration(
                8888,          // port - non-null
                null,          // retry - null
                4000,          // timeout - non-null
                "updatedRead", // readCommunity - non-null
                null,          // writeCommunity - null
                null,          // proxyHost - null
                "v3",          // version - non-null
                null,          // maxVarsPerPdu - null
                5,             // maxRepetitions - non-null
                null,          // maxRequestSize - null
                null, null, null, null, null, null, null, null, null, null
            );

        // Save the overrides
        response = snmpConfigRestApi.saveDefaultOverrides(partialDefaults);
        assertNotNull(response);
        assertEquals(204, response.getStatus());

        // Verify mixed values
        response = snmpConfigRestApi.getSnmpConfig();
        assertEquals(200, response.getStatus());
        SnmpConfig updatedConfig = (SnmpConfig) response.getEntity();

        assertEquals(Integer.valueOf(8888), updatedConfig.getPort());
        assertEquals(Integer.valueOf(0), updatedConfig.getRetry()); // getRetry() returns 0 when null
        assertEquals(Integer.valueOf(4000), updatedConfig.getTimeout());
        assertEquals("updatedRead", updatedConfig.getReadCommunity());
        assertNull(updatedConfig.getWriteCommunity());
        assertEquals("v3", updatedConfig.getVersion());
        assertEquals(Integer.valueOf(5), updatedConfig.getMaxRepetitions());
    }

    @Test
    public void testSaveDefaultOverrides_PreservesDefinitionsAndProfiles() {
        // Get initial config and count definitions and profiles
        Response response = snmpConfigRestApi.getSnmpConfig();
        assertEquals(200, response.getStatus());
        SnmpConfig initialConfig = (SnmpConfig) response.getEntity();
        int initialDefinitionCount = initialConfig.getDefinitions().size();
        assertTrue(initialDefinitionCount > 0);
        int initialProfileCount = initialConfig.getSnmpProfiles().getSnmpProfiles().size();
        assertTrue(initialProfileCount > 0);

        // Create new defaults
        org.opennms.netmgt.config.snmp.Configuration newDefaults =
            new org.opennms.netmgt.config.snmp.Configuration(
                7777, 7, 7000, "testRead", "testWrite",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
            );

        // Save the overrides
        response = snmpConfigRestApi.saveDefaultOverrides(newDefaults);
        assertNotNull(response);
        assertEquals(204, response.getStatus());

        // Verify defaults were updated but definitions remain
        response = snmpConfigRestApi.getSnmpConfig();
        assertEquals(200, response.getStatus());
        SnmpConfig updatedConfig = (SnmpConfig) response.getEntity();

        assertEquals(Integer.valueOf(7777), updatedConfig.getPort());
        assertEquals("testRead", updatedConfig.getReadCommunity());

        // Definitions should be preserved
        assertEquals(initialDefinitionCount, updatedConfig.getDefinitions().size());

        // Profiles should be preserved
        assertEquals(initialProfileCount, updatedConfig.getSnmpProfiles().getSnmpProfiles().size());
    }

    @Test
    public void testSaveDefaultOverrides_MultipleUpdates() {
        // First update
        org.opennms.netmgt.config.snmp.Configuration config1 =
            new org.opennms.netmgt.config.snmp.Configuration(
                100, 1, 1000, "read1", "write1",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
            );
        Response response = snmpConfigRestApi.saveDefaultOverrides(config1);
        assertEquals(204, response.getStatus());

        response = snmpConfigRestApi.getSnmpConfig();
        SnmpConfig updatedConfig1 = (SnmpConfig) response.getEntity();
        assertEquals("read1", updatedConfig1.getReadCommunity());
        assertEquals(Integer.valueOf(100), updatedConfig1.getPort());

        // Second update
        org.opennms.netmgt.config.snmp.Configuration config2 =
            new org.opennms.netmgt.config.snmp.Configuration(
                200, 2, 2000, "read2", "write2",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
            );
        response = snmpConfigRestApi.saveDefaultOverrides(config2);
        assertEquals(204, response.getStatus());

        response = snmpConfigRestApi.getSnmpConfig();
        SnmpConfig updatedConfig2 = (SnmpConfig) response.getEntity();
        assertEquals("read2", updatedConfig2.getReadCommunity());
        assertEquals(Integer.valueOf(200), updatedConfig2.getPort());

        // Third update - set some to null
        org.opennms.netmgt.config.snmp.Configuration config3 =
            new org.opennms.netmgt.config.snmp.Configuration(
                300, null, 3000, null, "write3",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
            );
        response = snmpConfigRestApi.saveDefaultOverrides(config3);
        assertEquals(204, response.getStatus());

        response = snmpConfigRestApi.getSnmpConfig();
        SnmpConfig updatedConfig3 = (SnmpConfig) response.getEntity();
        assertNull(updatedConfig3.getReadCommunity());
        assertEquals(Integer.valueOf(0), updatedConfig3.getRetry()); // getRetry() returns 0 when null
        assertEquals("write3", updatedConfig3.getWriteCommunity());
        assertEquals(Integer.valueOf(300), updatedConfig3.getPort());
    }


    @Test
    public void testSaveDefaultOverrides_ValidationFailure() {
        // Create a Configuration with invalid port value (greater than 65535) to trigger schema validation failure
        org.opennms.netmgt.config.snmp.Configuration edgeCaseConfig =
            new org.opennms.netmgt.config.snmp.Configuration(
                99999,               // invalid port value - outside maximum
                null,
                null,
                "",
                "",
                null, null, null, null, null, null,
                null,
                null, null, null, null, null, null, null, null
            );

        // This should fail since the port is out of range
        assertThrows(WebApplicationException.class, () -> snmpConfigRestApi.saveDefaultOverrides(edgeCaseConfig));

        // assert that the expected error was logged, specifically the schema validation failure for the port field
        MockLogAppender.assertLogMatched(Level.ERROR, "Data access error saving SNMP default overrides, failed schema validation: ");
        MockLogAppender.assertLogMatched(Level.ERROR,
                "ValidatingMarshalRecord$MarshalSAXParseException; cvc-maxInclusive-valid: Value '99999' is not facet-valid with respect to maxInclusive '65535' for type '#AnonType_portconfiguration'");

        // Verify that the config was NOT saved
        Response response = snmpConfigRestApi.getSnmpConfig();
        assertEquals(200, response.getStatus());
        SnmpConfig updatedConfig = (SnmpConfig) response.getEntity();
        assertEquals(Integer.valueOf(161), updatedConfig.getPort());
    }

    @Test
    public void testSaveDefaultOverrides_NullConfig() {
        // Attempt to save null config - should fail
        Response response = snmpConfigRestApi.saveDefaultOverrides(null);

        assertNotNull(response);
        // Status should indicate error
        assertEquals(400, response.getStatus());

        String message = (String) response.getEntity();
        assertEquals("Missing or invalid request body.", message);
    }

    /**
     * Helper method to get the Rest service's current SNMP config.
     */
    private SnmpConfig getCurrentConfig() {
        SnmpConfig config = null;

        // get the original config
        final Response response = snmpConfigRestApi.downloadConfig(null);

        if (response == null || response.getStatus() != 200) {
            return null;
        }

        try {
            byte[] bytes = (byte[]) response.getEntity();
            String json = new String(bytes, StandardCharsets.UTF_8);
            config = mapper.readValue(json, SnmpConfig.class);
        } catch (Exception e) {
            Assert.fail("Error retrieving or parsing downloaded Json file.");
        }

        return config;
    }
}
