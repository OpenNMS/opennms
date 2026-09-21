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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.TrapdConfigDao;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.SecurityHelper;
import org.opennms.test.JUnitConfigurationEnvironment;

import javax.ws.rs.core.SecurityContext;
import org.opennms.web.rest.v2.model.Snmpv3UserDto;
import org.opennms.web.rest.v2.model.TrapdConfigDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class TrapdRestServiceIT {

    @Autowired
    private TrapdRestService trapdRestService;

    @Autowired
    private TrapdConfigDao trapdConfigDao;

    @Before
    public void setUp() throws Exception {
        trapdRestService = new TrapdRestService();
        trapdConfigDao = mock(TrapdConfigDao.class);
        setField(trapdRestService, "trapdConfigDao", trapdConfigDao);
    }

    // --- Download XML Tests ---

    @Test
    public void downloadXmlShouldReturnOkWithCorrectHeaders() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        try (Response response = trapdRestService.downloadTrapdConfig("xml", adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getMediaType().toString().contains("application/xml"));
            assertEquals("attachment; filename=trapd-config.xml", response.getHeaderString("Content-Disposition"));
            assertEquals("public", response.getHeaderString("Pragma"));
            assertEquals("no-cache, must-revalidate", response.getHeaderString("Cache-Control"));
        }
    }

    @Test
    public void downloadXmlShouldReturnBodyWithConfigData() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        try (Response response = trapdRestService.downloadTrapdConfig("xml", adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            String xml = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(xml.contains("snmp-trap-port=\"162\""));
            assertTrue(xml.contains("snmp-trap-address=\"*\""));
        }
    }

    @Test
    public void downloadXmlShouldReturnNotFoundWhenConfigMissing() {
        when(trapdConfigDao.getConfig()).thenReturn(null);

        try (Response response = trapdRestService.downloadTrapdConfig("xml", adminSecurityContext())) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("Trapd configuration not found.", response.getEntity());
        }
    }

    @Test
    public void downloadXmlShouldReturnServerErrorWhenExceptionThrown() {
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(trapdConfigDao).getConfig();

        try (Response response = trapdRestService.downloadTrapdConfig("xml", adminSecurityContext())) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Error retrieving Trapd config.", response.getEntity());
        }
    }

    @Test
    public void downloadXmlShouldBeCaseInsensitiveForFormatParam() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        for (String format : new String[]{"XML", "Xml", "xMl"}) {
            try (Response response = trapdRestService.downloadTrapdConfig(format, adminSecurityContext())) {
                assertEquals("Expected XML response for format=" + format,
                        Response.Status.OK.getStatusCode(), response.getStatus());
                assertEquals("attachment; filename=trapd-config.xml", response.getHeaderString("Content-Disposition"));
            }
        }
    }

    // --- Download JSON Tests ---

    @Test
    public void downloadJsonShouldReturnOkWithCorrectHeaders() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        try (Response response = trapdRestService.downloadTrapdConfig("json", adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getMediaType().toString().contains("application/json"));
            assertEquals("attachment; filename=trapd-config.json", response.getHeaderString("Content-Disposition"));
            assertEquals("public", response.getHeaderString("Pragma"));
            assertEquals("no-cache, must-revalidate", response.getHeaderString("Cache-Control"));
        }
    }

    @Test
    public void downloadJsonShouldReturnBodyWithConfigData() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        try (Response response = trapdRestService.downloadTrapdConfig("json", adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            String json = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(json.contains("snmpTrapPort"));
            assertTrue(json.contains("162"));
        }
    }

    @Test
    public void downloadJsonShouldDefaultToJsonWhenFormatIsNull() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        try (Response response = trapdRestService.downloadTrapdConfig(null, adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals("attachment; filename=trapd-config.json", response.getHeaderString("Content-Disposition"));
        }
    }

    @Test
    public void downloadJsonShouldReturnNotFoundWhenConfigMissing() {
        when(trapdConfigDao.getConfig()).thenReturn(null);

        try (Response response = trapdRestService.downloadTrapdConfig("json", adminSecurityContext())) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("Trapd configuration not found.", response.getEntity());
        }
    }

    @Test
    public void downloadJsonShouldReturnServerErrorWhenExceptionThrown() {
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(trapdConfigDao).getConfig();

        try (Response response = trapdRestService.downloadTrapdConfig("json", adminSecurityContext())) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Error retrieving Trapd config.", response.getEntity());
        }
    }

    @Test
    public void downloadShouldReturnBadRequestForUnsupportedFormat() {
        try (Response response = trapdRestService.downloadTrapdConfig("csv", adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Invalid format parameter. Supported values are 'json' and 'xml'.", response.getEntity());
        }
    }

    @Test
    public void downloadJsonShouldBeCaseInsensitiveForFormatParam() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        for (String format : new String[]{"JSON", "Json", "jSoN"}) {
            try (Response response = trapdRestService.downloadTrapdConfig(format, adminSecurityContext())) {
                assertEquals("Expected JSON response for format=" + format,
                        Response.Status.OK.getStatusCode(), response.getStatus());
                assertEquals("attachment; filename=trapd-config.json", response.getHeaderString("Content-Disposition"));
            }
        }
    }

    // --- Upload XML Tests ---

    @Test
    public void uploadShouldReturnBadRequestWhenAttachmentMissingXml() {
        try (Response response = trapdRestService.uploadTrapdConfigurationXml(null, adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing uploaded file for Trap Configuration XML file upload.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldReturnBadRequestWhenXmlIsInvalid() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream("<trapd-configuration".getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfigurationXml(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Invalid Trapd XML configuration.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldPersistValidXmlAndReturnOk() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfigurationXml(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals(10163, captor.getValue().getSnmpTrapPort());
    }

    @Test
    public void uploadShouldPersistUseAddressFromVarbindWhenProvidedInXml() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXmlWithUseAddressFromVarbind().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfigurationXml(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertTrue(captor.getValue().shouldUseAddressFromVarbind());
    }

    @Test
    public void uploadShouldReturnBadRequestWhenValidationFailsXml() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8))
        );
        whenValidationFailsOnUpdate("Invalid Trapd XML configuration.");

        try (Response response = trapdRestService.uploadTrapdConfigurationXml(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Invalid Trapd XML configuration.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldReturnServerErrorWhenPersistenceFailsXml() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8))
        );
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        try (Response response = trapdRestService.uploadTrapdConfigurationXml(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to persist Trapd configuration.", response.getEntity());
        }
    }

    // --- Upload JSON Tests ---

    @Test
    public void uploadShouldReturnBadRequestWhenAttachmentMissingJson() {
        try (Response response = trapdRestService.uploadTrapdConfiguration(null, adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing uploaded file for Trap Configuration JSON file upload.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldReturnBadRequestWhenXmlPassedInsteadOfJsonIsInvalid() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream("<trapd-configuration".getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Invalid Trapd JSON configuration.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldReturnBadRequestWhenJsonIsInvalid() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream("{ \"something }".getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Invalid Trapd JSON configuration.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldPersistValidJsonAndReturnOk() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigJson().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals(10163, captor.getValue().getSnmpTrapPort());
    }

    @Test
    public void uploadShouldPersistUseAddressFromVarbindWhenProvidedInJson() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigJsonWithUseAddressFromVarbind().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertTrue(captor.getValue().shouldUseAddressFromVarbind());
    }

    @Test
    public void uploadShouldReturnBadRequestWhenValidationFailsJson() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigJson().getBytes(StandardCharsets.UTF_8))
        );
        whenValidationFailsOnUpdate("Invalid Trapd JSON configuration.");

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Invalid Trapd JSON configuration.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldReturnServerErrorWhenPersistenceFailsJson() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigJson().getBytes(StandardCharsets.UTF_8))
        );
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to persist Trapd configuration.", response.getEntity());
        }
    }

    // --- Get Config Tests ---

    @Test
    public void getShouldReturnOkWithConfigWhenExists() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setSnmpTrapAddress("127.0.0.1");
        config.setNewSuspectOnTrap(false);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof TrapdConfigDto);
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            assertEquals(Integer.valueOf(162), returned.getSnmpTrapPort());
            assertEquals("127.0.0.1", returned.getSnmpTrapAddress());
        }
    }

    @Test
    public void getShouldReturnNotFoundWhenNoConfigurationExists() {
        when(trapdConfigDao.getConfig()).thenReturn(null);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("Trapd configuration not found.", response.getEntity());
        }
    }

    @Test
    public void getShouldReturnServerErrorWhenExceptionThrown() {
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(trapdConfigDao).getConfig();

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to retrieve Trapd configuration.", response.getEntity());
        }
    }

    @Test
    public void getShouldReturnSnmpv3UserFieldsUnchangedButStillMaskCredentials() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setId("test-user-id-1");
        user.setSecurityName("engine-user");
        user.setEngineId("0x8000000001020304");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("authpass");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("privpass");
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            Snmpv3UserDto returnedUser = returned.getSnmpv3User().get(0);
            assertEquals("test-user-id-1", returnedUser.getId());
            assertEquals("engine-user", returnedUser.getSecurityName());
            assertEquals("0x8000000001020304", returnedUser.getEngineId());
            assertEquals("SHA", returnedUser.getAuthProtocol());
            assertEquals("AES", returnedUser.getPrivacyProtocol());
            assertEquals(Integer.valueOf(3), returnedUser.getSecurityLevel());
            assertEquals(SecurityHelper.MASKED_PASSWORD, returnedUser.getAuthPassphrase());
            assertEquals(SecurityHelper.MASKED_PASSWORD, returnedUser.getPrivacyPassphrase());
        }
    }

    @Test
    public void getShouldReturnNullPassphrasesWhenNotSet() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("user1");
        user.setSecurityLevel(1);
        // no auth or privacy passphrase set
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            Snmpv3UserDto returnedUser = returned.getSnmpv3User().get(0);
            assertNull(returnedUser.getAuthPassphrase());
            assertNull(returnedUser.getPrivacyPassphrase());
        }
    }

    @Test
    public void getShouldReturnAllSnmpv3Users() {
        TrapdConfiguration config = buildMinimalConfig();

        Snmpv3User userA = new Snmpv3User();
        userA.setSecurityName("user-a");
        config.addSnmpv3User(userA);

        Snmpv3User userB = new Snmpv3User();
        userB.setSecurityName("user-b");
        config.addSnmpv3User(userB);

        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            assertEquals(2, returned.getSnmpv3User().size());
            assertEquals("user-a", returned.getSnmpv3User().get(0).getSecurityName());
            assertEquals("user-b", returned.getSnmpv3User().get(1).getSecurityName());
        }
    }

    private static TrapdConfiguration buildMinimalConfig() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setSnmpTrapAddress("*");
        config.setNewSuspectOnTrap(false);
        return config;
    }

    /** Builds the smallest valid {@link TrapdConfigDto} accepted by the update endpoint. */
    private static TrapdConfigDto buildMinimalUpdatePayload() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);
        return payload;
    }

    // --- Update Config Tests ---

    @Test
    public void updateShouldReturnBadRequestWhenPayloadMissing() {
        try (Response response = trapdRestService.updateTrapdConfiguration(null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing Trapd configuration in request body.", response.getEntity());
        }
    }

    @Test
    public void updateShouldReturnBadRequestWhenSnmpTrapPortMissing() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("snmpTrapPort is required and must be between 1 and 65535.", response.getEntity());
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldAcceptWhenSnmpTrapAddressMissing() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        verify(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldRejectWhenNewSuspectOnTrapMissing() {
        // Because this endpoint replaces the whole config, omitting newSuspectOnTrap would
        // silently disable new-suspect generation on configs that previously had it enabled.
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(10164);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("newSuspectOnTrap is required.", response.getEntity());
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldMergePayloadAndPersist() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);
        payload.setThreads(4);
        payload.setQueueSize(1000);
        payload.setIncludeRawMessage(true);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        TrapdConfiguration persisted = captor.getValue();
        assertEquals(10164, persisted.getSnmpTrapPort());
        assertEquals(4, persisted.getThreads());
        assertTrue(persisted.isIncludeRawMessage());
    }

    @Test
    public void updateShouldPersistUseAddressFromVarbindWhenProvided() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(1162);
        payload.setNewSuspectOnTrap(false);
        payload.setUseAddressFromVarbind(true);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertTrue(captor.getValue().shouldUseAddressFromVarbind());
    }

    @Test
    public void updateShouldReturnBadRequestWhenValidationFails() {
        org.mockito.Mockito.doThrow(new ValidationException("validation failed"))
                .when(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Provided Trapd configuration failed schema validation.", response.getEntity());
        }
    }

    @Test
    public void updateShouldReturnServerErrorWhenPersistenceThrows() {
        org.mockito.Mockito.doThrow(new RuntimeException("db down"))
                .when(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to persist Trapd configuration.", response.getEntity());
        }
    }

    // --- SNMPv3 User Validation Tests ---

    @Test
    public void updateShouldAcceptSnmpv3UserWithoutSecurityLevel() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user-no-level");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        verify(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldClearSnmpv3UsersWhenEmptyListProvided() {
        // Reproduces the bug: sending snmpv3User=[] should result in replaceConfig being called
        // with an entity that has no SNMPv3 users (not the old ones retained from a merge).
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        payload.setSnmpv3User(java.util.Collections.emptyList());

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals(0, captor.getValue().getSnmpv3UserCount());
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenSecurityNameMissing() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();

        Snmpv3UserDto user = new Snmpv3UserDto();
        // securityName intentionally omitted
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityName is required."));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldAcceptSnmpv3UserWithValidSecurityLevelBoundaries() {
        // securityLevel 1, 2, 3 are all valid
        for (int level : new int[]{1, 2, 3}) {
            TrapdConfigDto payload = buildMinimalUpdatePayload();
            Snmpv3UserDto user = new Snmpv3UserDto();
            user.setSecurityName("user-level-" + level);
            user.setSecurityLevel(level);
            if (level >= 2) {
                user.setAuthProtocol("SHA");
                user.setAuthPassphrase("authpass123");
            }
            if (level == 3) {
                user.setPrivacyProtocol("AES");
                user.setPrivacyPassphrase("privpass123");
            }
            payload.setSnmpv3User(java.util.List.of(user));

            try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
                assertEquals("Expected OK for securityLevel=" + level,
                        Response.Status.OK.getStatusCode(), response.getStatus());
            }
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenSecurityLevelOutOfRange() {
        // Level 0 is below minimum
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user-bad-level");
        user.setSecurityLevel(0);
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityLevel must be between 1 and 3."));
        }

        // Level 4 is above maximum
        payload = buildMinimalUpdatePayload();
        user = new Snmpv3UserDto();
        user.setSecurityName("user-bad-level");
        user.setSecurityLevel(4);
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityLevel must be between 1 and 3."));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenAuthProtocolWithoutPassphrase() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setAuthProtocol("SHA");
        // authPassphrase intentionally omitted
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("authProtocol and authPassphrase must be provided together."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenAuthPassphraseWithoutProtocol() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        // authProtocol intentionally omitted
        user.setAuthPassphrase("somepassphrase");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("authProtocol and authPassphrase must be provided together."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenPrivacyProtocolWithoutPassphrase() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setPrivacyProtocol("AES");
        // privacyPassphrase intentionally omitted
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("privacyProtocol and privacyPassphrase must be provided together."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenPrivacyPassphraseWithoutProtocol() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        // privacyProtocol intentionally omitted
        user.setPrivacyPassphrase("privpass");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("privacyProtocol and privacyPassphrase must be provided together."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenAuthPassphraseTooShort() {
        // SNMP4J rejects passphrases < 8 bytes at UsmUser construction; the REST layer
        // catches this early so the trap daemon doesn't fail to restart after a reload.
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("short"); // 5 bytes
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("authPassphrase must be at least 8 bytes."));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenPrivacyPassphraseTooShort() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("authpass");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("tiny"); // 4 bytes
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("privacyPassphrase must be at least 8 bytes."));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldRejectAuthPassphraseStartingWithStar() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("*notmasked"); // starts with * but is not MASKED_PASSWORD
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("authPassphrase must not begin with '*'."));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldRejectPrivacyPassphraseStartingWithStar() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("authpass1");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("*notmasked"); // starts with * but is not MASKED_PASSWORD
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("privacyPassphrase must not begin with '*'."));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldAcceptPassphraseWithStarNotAtStart() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("pass*word"); // * is not at the start
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        verify(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldAcceptScvPlaceholderPassphrase() {
        // ${scv:alias:key} is always > 8 bytes; the literal length check passes trivially
        // and the placeholder is resolved by the daemon at runtime via SecureCredentialsVault.
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("${scv:a:k}"); // 10 bytes
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        verify(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenUnsupportedAuthProtocol() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setAuthProtocol("INVALID");
        user.setAuthPassphrase("authpass");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("Unsupported authProtocol."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenUnsupportedPrivacyProtocol() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setPrivacyProtocol("INVALID");
        user.setPrivacyPassphrase("privpass");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("Unsupported privacyProtocol."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenLevel1HasAuthCredentials() {
        // securityLevel 1 (noAuthNoPriv) must not have auth credentials
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setSecurityLevel(1);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("authpass");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityLevel 1 does not allow auth or privacy credentials."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenLevel2MissingAuthCredentials() {
        // securityLevel 2 (authNoPriv) requires auth but no privacy
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setSecurityLevel(2);
        // auth credentials intentionally omitted
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityLevel 2 requires auth credentials and does not allow privacy credentials."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenLevel2HasPrivacyCredentials() {
        // securityLevel 2 (authNoPriv) must not have privacy credentials
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setSecurityLevel(2);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("authpass");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("privpass");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityLevel 2 requires auth credentials and does not allow privacy credentials."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenLevel3MissingPrivacyCredentials() {
        // securityLevel 3 (authPriv) requires both auth and privacy
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("authpass");
        // privacy credentials intentionally omitted
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityLevel 3 requires both auth and privacy credentials."));
        }
    }

    @Test
    public void updateShouldApplyPairingCheckEvenWhenSecurityLevelIsAbsent() {
        // Cross-field pairing is always enforced regardless of securityLevel presence.
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        // securityLevel omitted — only authProtocol provided, passphrase missing
        user.setAuthProtocol("SHA");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("authProtocol and authPassphrase must be provided together."));
        }
    }

    @Test
    public void updateShouldRejectFirstInvalidUserInList() {
        // Only the first invalid user should trigger the error
        TrapdConfigDto payload = buildMinimalUpdatePayload();

        Snmpv3UserDto validUser = new Snmpv3UserDto();
        validUser.setSecurityName("valid-user");

        Snmpv3UserDto invalidUser = new Snmpv3UserDto();
        // securityName missing — invalid
        invalidUser.setSecurityLevel(1);

        payload.setSnmpv3User(java.util.List.of(validUser, invalidUser));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityName is required."));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    // --- Boundary Value Tests ---

    @Test
    public void updateShouldAcceptSnmpTrapPortAtLowerAndUpperBound() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setNewSuspectOnTrap(false);
        // Lower bound
        payload.setSnmpTrapPort(1);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
        // Upper bound
        payload.setSnmpTrapPort(65535);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void updateShouldRejectSnmpTrapPortOutOfBounds() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setNewSuspectOnTrap(false);
        // Below lower bound
        payload.setSnmpTrapPort(0);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("snmpTrapPort is required and must be between 1 and 65535.", response.getEntity());
        }
        // Above upper bound
        payload.setSnmpTrapPort(65536);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("snmpTrapPort is required and must be between 1 and 65535.", response.getEntity());
        }
    }

    @Test
    public void updateShouldAcceptZeroOnlyForFieldsThatAllowIt() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(162);
        payload.setNewSuspectOnTrap(false);
        payload.setThreads(0);
        payload.setBatchInterval(0);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void updateShouldRejectZeroForQueueSizeAndBatchSize() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(162);
        payload.setNewSuspectOnTrap(false);

        payload.setQueueSize(0);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("queueSize must be greater than 0.", response.getEntity());
        }

        payload.setQueueSize(null);
        payload.setBatchSize(0);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("batchSize must be greater than 0.", response.getEntity());
        }
    }

    @Test
    public void updateShouldRejectNegativeForOptionalFields() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(162);
        payload.setNewSuspectOnTrap(false);
        payload.setThreads(-1);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("threads must be non-negative.", response.getEntity());
        }
        payload.setThreads(null);
        payload.setQueueSize(-1);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("queueSize must be greater than 0.", response.getEntity());
        }
        payload.setQueueSize(null);
        payload.setBatchSize(-1);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("batchSize must be greater than 0.", response.getEntity());
        }
        payload.setBatchSize(null);
        payload.setBatchInterval(-1);
        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("batchInterval must be non-negative.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldNotCallReplaceConfigWhenXmlParsingFails() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream("<trapd-configuration".getBytes(StandardCharsets.UTF_8))
        );

        trapdRestService.uploadTrapdConfigurationXml(attachment, adminSecurityContext());

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void uploadShouldRejectXmlWhenLevel3MissingPrivacyCredentials() {
        final String xml = """
                <trapd-configuration xmlns="http://xmlns.opennms.org/xsd/config/trapd" \
                snmp-trap-address="*" snmp-trap-port="10163" new-suspect-on-trap="false"> \
                <snmpv3-user security-name="user" security-level="3" \
                auth-protocol="SHA" auth-passphrase="authpass"/> \
                </trapd-configuration>""";
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfigurationXml(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityLevel 3 requires both auth and privacy credentials."));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void getShouldReturnEmptySnmpv3UserListWhenNoUsersConfigured() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            assertTrue(returned.getSnmpv3User().isEmpty());
        }
    }

    @Test
    public void updateShouldNotSetSnmpv3UsersOnEntityWhenListIsNull() {
        // When snmpv3User is null (omitted from request), toEntity() must not call setSnmpv3User
        // so the entity's user count defaults to 0 (fresh TrapdConfiguration).
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        // snmpv3User deliberately not set — remains null

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals(0, captor.getValue().getSnmpv3UserCount());
    }

    @Test
    public void updateShouldPersistSnmpv3UsersWhenValidUsersProvided() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("my-user");
        user.setSecurityLevel(2);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("authpass123");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals(1, captor.getValue().getSnmpv3UserCount());
        assertEquals("my-user", captor.getValue().getSnmpv3User(0).getSecurityName());
        assertEquals("SHA", captor.getValue().getSnmpv3User(0).getAuthProtocol());
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenLevel1HasPrivacyCredentials() {
        // securityLevel 1 (noAuthNoPriv) must not have privacy-only credentials either
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setSecurityLevel(1);
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("privpass");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityLevel 1 does not allow auth or privacy credentials."));
        }
    }

    @Test
    public void updateShouldRejectSnmpv3UserWhenLevel3MissingAuthCredentials() {
        // securityLevel 3 with only privacy (no auth) must be rejected
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("user1");
        user.setSecurityLevel(3);
        // auth intentionally omitted
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("privpass");
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("securityLevel 3 requires both auth and privacy credentials."));
        }
    }

    @Test
    public void updateShouldIncludeSecurityNameInValidationErrorMessage() {
        // The error message format is: "Invalid SNMPv3 user at index <index>: <reason>"
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("bad-user");
        user.setSecurityLevel(0); // out of range
        payload.setSnmpv3User(java.util.List.of(user));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            String entity = (String) response.getEntity();
            assertTrue(entity.contains("at index 0"));
            assertTrue(entity.contains("securityLevel must be between 1 and 3."));
        }
    }

    @Test
    public void updateShouldRejectNullSnmpv3UserEntryWithIndexInMessage() {
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        payload.setSnmpv3User(java.util.Collections.singletonList(null));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            String entity = (String) response.getEntity();
            assertTrue(entity.contains("Invalid SNMPv3 user at index 0:"));
            assertTrue(entity.contains("entry must not be null."));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    // --- Credential masking tests ---

    @Test
    public void getShouldReturnMaskedPassphrasesForSnmpv3Users() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("testUser");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("realAuthPass");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("realPrivPass");
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            assertEquals(1, returned.getSnmpv3User().size());
            Snmpv3UserDto userDto = returned.getSnmpv3User().get(0);
            assertEquals(SecurityHelper.MASKED_PASSWORD, userDto.getAuthPassphrase());
            assertEquals(SecurityHelper.MASKED_PASSWORD, userDto.getPrivacyPassphrase());
            // original entity must not be mutated
            assertEquals("realAuthPass", user.getAuthPassphrase());
            assertEquals("realPrivPass", user.getPrivacyPassphrase());
        }
    }

    @Test
    public void downloadShouldReturnPlaintextPassphrasesNotMasked() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("testUser");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("realAuthPass");
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.downloadTrapdConfig("json", adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            String json = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertTrue(json.contains("realAuthPass"));
            assertFalse(json.contains(SecurityHelper.MASKED_PASSWORD));
        }
    }

    @Test
    public void updateWithMaskedPassphraseShouldPreserveExistingCredential() {
        TrapdConfiguration existingConfig = buildMinimalConfig();
        Snmpv3User existingUser = new Snmpv3User();
        existingUser.setId("user-id-1");
        existingUser.setSecurityName("testUser");
        existingUser.setAuthProtocol("SHA");
        existingUser.setAuthPassphrase("realAuthPass");
        existingConfig.addSnmpv3User(existingUser);
        when(trapdConfigDao.getConfig()).thenReturn(existingConfig);

        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto incomingUser = new Snmpv3UserDto();
        incomingUser.setId("user-id-1");
        incomingUser.setSecurityName("testUser");
        incomingUser.setAuthProtocol("SHA");
        incomingUser.setAuthPassphrase(SecurityHelper.MASKED_PASSWORD);
        payload.setSnmpv3User(java.util.List.of(incomingUser));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals("realAuthPass", captor.getValue().getSnmpv3User(0).getAuthPassphrase());
    }

    /**
     * Two users sharing the same securityName but with different credentials must each
     * resolve a masked passphrase against <em>their own</em> stored secret, correlated by id.
     * This is the core scenario that securityName-based matching could not handle.
     */
    @Test
    public void updateWithDuplicateSecurityNamesResolvesEachByItsOwnId() {
        TrapdConfiguration existingConfig = buildMinimalConfig();
        Snmpv3User userA = new Snmpv3User();
        userA.setId("id-a");
        userA.setSecurityName("dupUser");
        userA.setAuthProtocol("SHA");
        userA.setAuthPassphrase("secretPassphraseA");
        existingConfig.addSnmpv3User(userA);
        Snmpv3User userB = new Snmpv3User();
        userB.setId("id-b");
        userB.setSecurityName("dupUser");
        userB.setAuthProtocol("MD5");
        userB.setAuthPassphrase("secretPassphraseB");
        existingConfig.addSnmpv3User(userB);
        when(trapdConfigDao.getConfig()).thenReturn(existingConfig);

        // Submit both users back with masked passphrases, in reverse order, to prove the
        // resolution is keyed by id and not by list position or security name.
        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto incomingB = new Snmpv3UserDto();
        incomingB.setId("id-b");
        incomingB.setSecurityName("dupUser");
        incomingB.setAuthProtocol("MD5");
        incomingB.setAuthPassphrase(SecurityHelper.MASKED_PASSWORD);
        Snmpv3UserDto incomingA = new Snmpv3UserDto();
        incomingA.setId("id-a");
        incomingA.setSecurityName("dupUser");
        incomingA.setAuthProtocol("SHA");
        incomingA.setAuthPassphrase(SecurityHelper.MASKED_PASSWORD);
        payload.setSnmpv3User(java.util.List.of(incomingB, incomingA));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        TrapdConfiguration saved = captor.getValue();
        assertEquals("secretPassphraseA", saved.getSnmpv3UserById("id-a").getAuthPassphrase());
        assertEquals("secretPassphraseB", saved.getSnmpv3UserById("id-b").getAuthPassphrase());
    }

    /**
     * A masked passphrase paired with an id that does not match any stored user must be
     * rejected — there is no secret to resolve, so it is treated like a new entry.
     */
    @Test
    public void updateWithMaskedPassphraseAndUnknownIdReturnsBadRequest() {
        TrapdConfiguration existingConfig = buildMinimalConfig();
        Snmpv3User existingUser = new Snmpv3User();
        existingUser.setId("known-id");
        existingUser.setSecurityName("testUser");
        existingUser.setAuthProtocol("SHA");
        existingUser.setAuthPassphrase("realAuthPass");
        existingConfig.addSnmpv3User(existingUser);
        when(trapdConfigDao.getConfig()).thenReturn(existingConfig);

        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto incomingUser = new Snmpv3UserDto();
        incomingUser.setId("stale-or-unknown-id");
        incomingUser.setSecurityName("testUser");
        incomingUser.setAuthProtocol("SHA");
        incomingUser.setAuthPassphrase(SecurityHelper.MASKED_PASSWORD);
        payload.setSnmpv3User(java.util.List.of(incomingUser));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("masked passphrase"));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateWithRealPassphraseShouldUpdateToNewValue() {
        TrapdConfiguration existingConfig = buildMinimalConfig();
        Snmpv3User existingUser = new Snmpv3User();
        existingUser.setSecurityName("testUser");
        existingUser.setAuthProtocol("SHA");
        existingUser.setAuthPassphrase("oldAuthPass");
        existingConfig.addSnmpv3User(existingUser);
        when(trapdConfigDao.getConfig()).thenReturn(existingConfig);

        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto incomingUser = new Snmpv3UserDto();
        incomingUser.setSecurityName("testUser");
        incomingUser.setAuthProtocol("SHA");
        incomingUser.setAuthPassphrase("newAuthPass");
        payload.setSnmpv3User(java.util.List.of(incomingUser));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals("newAuthPass", captor.getValue().getSnmpv3User(0).getAuthPassphrase());
    }

    @Test
    public void updateWithMaskedPassphraseForNewUserShouldReturnBadRequest() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig()); // no users

        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto incomingUser = new Snmpv3UserDto();
        incomingUser.setSecurityName("brandNewUser");
        incomingUser.setAuthProtocol("SHA");
        incomingUser.setAuthPassphrase(SecurityHelper.MASKED_PASSWORD);
        payload.setSnmpv3User(java.util.List.of(incomingUser));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("masked passphrase"));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    /**
     * Two incoming users sharing the same non-blank id would break id-based credential
     * correlation and persist an unreachable duplicate, so the request must be rejected.
     */
    @Test
    public void updateWithDuplicateUserIdsReturnsBadRequest() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto userA = new Snmpv3UserDto();
        userA.setId("dup-id");
        userA.setSecurityName("userA");
        Snmpv3UserDto userB = new Snmpv3UserDto();
        userB.setId("dup-id");
        userB.setSecurityName("userB");
        payload.setSnmpv3User(java.util.List.of(userA, userB));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("Duplicate SNMPv3 user id"));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    /**
     * Multiple new users with blank ids are legitimate (ids are assigned at persist time) and
     * must not be mistaken for duplicates.
     */
    @Test
    public void updateWithMultipleBlankUserIdsIsAllowed() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto userA = new Snmpv3UserDto();
        userA.setSecurityName("newUserA");
        Snmpv3UserDto userB = new Snmpv3UserDto();
        userB.setSecurityName("newUserB");
        payload.setSnmpv3User(java.util.List.of(userA, userB));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        verify(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void uploadJsonWithDuplicateUserIdsReturnsBadRequest() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        String json = """
                {"snmpTrapAddress":"*","snmpTrapPort":162,"newSuspectOnTrap":false,\
                "snmpv3User":[{"id":"dup-id","securityName":"userA"},\
                {"id":"dup-id","securityName":"userB"}]}""";
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("Duplicate SNMPv3 user id"));
        }

        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    // --- SCV expression tests ---

    @Test
    public void getShouldReturnScvExpressionUnmasked() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("scvUser");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("${scv:myalias:authkey}");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("${scv:myalias:privkey}");
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            Snmpv3UserDto userDto = returned.getSnmpv3User().get(0);
            assertEquals("${scv:myalias:authkey}", userDto.getAuthPassphrase());
            assertEquals("${scv:myalias:privkey}", userDto.getPrivacyPassphrase());
        }
    }

    @Test
    public void updateWithScvExpressionShouldPersistItAsIs() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        TrapdConfigDto payload = buildMinimalUpdatePayload();
        Snmpv3UserDto incomingUser = new Snmpv3UserDto();
        incomingUser.setSecurityName("scvUser");
        incomingUser.setAuthProtocol("SHA");
        incomingUser.setAuthPassphrase("${scv:myalias:authkey}");
        payload.setSnmpv3User(java.util.List.of(incomingUser));

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals("${scv:myalias:authkey}", captor.getValue().getSnmpv3User(0).getAuthPassphrase());
    }

    @Test
    public void uploadJsonWithScvExpressionShouldPersistItAsIs() {
        when(trapdConfigDao.getConfig()).thenReturn(buildMinimalConfig());

        String json = """
                {"snmpTrapAddress":"*","snmpTrapPort":162,"newSuspectOnTrap":false,\
                "snmpv3User":[{"securityName":"scvUser","authProtocol":"SHA",\
                "authPassphrase":"${scv:myalias:authkey}"}]}""";
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals("${scv:myalias:authkey}", captor.getValue().getSnmpv3User(0).getAuthPassphrase());
    }

    @Test
    public void uploadJsonWithMaskedPassphraseShouldPreserveExistingCredential() {
        TrapdConfiguration existingConfig = buildMinimalConfig();
        Snmpv3User existingUser = new Snmpv3User();
        existingUser.setId("upload-id-1");
        existingUser.setSecurityName("testUser");
        existingUser.setAuthProtocol("SHA");
        existingUser.setAuthPassphrase("realAuthPass");
        existingConfig.addSnmpv3User(existingUser);
        when(trapdConfigDao.getConfig()).thenReturn(existingConfig);

        String json = """
                {"snmpTrapAddress":"*","snmpTrapPort":162,"newSuspectOnTrap":false,\
                "snmpv3User":[{"id":"upload-id-1","securityName":"testUser","authProtocol":"SHA",\
                "authPassphrase":"******"}]}""";
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, adminSecurityContext())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).replaceConfig(captor.capture());
        assertEquals("realAuthPass", captor.getValue().getSnmpv3User(0).getAuthPassphrase());
    }

    // --- Authorization Tests ---

    @Test
    public void downloadShouldReturn403ForNonAdminUser() {
        try (Response response = trapdRestService.downloadTrapdConfig("json", nonAdminSecurityContext())) {
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
            assertEquals("Admin role required to download Trapd configuration.", response.getEntity());
        }
        verify(trapdConfigDao, never()).getConfig();
    }

    @Test
    public void downloadXmlShouldReturn403ForNonAdminUser() {
        try (Response response = trapdRestService.downloadTrapdConfig("xml", nonAdminSecurityContext())) {
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
            assertEquals("Admin role required to download Trapd configuration.", response.getEntity());
        }
        verify(trapdConfigDao, never()).getConfig();
    }

    @Test
    public void uploadJsonShouldReturn403ForNonAdminUser() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigJson().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, nonAdminSecurityContext())) {
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
            assertEquals("Admin role required to upload Trapd configuration.", response.getEntity());
        }
        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void uploadXmlShouldReturn403ForNonAdminUser() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfigurationXml(attachment, nonAdminSecurityContext())) {
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
            assertEquals("Admin role required to upload Trapd configuration.", response.getEntity());
        }
        verify(trapdConfigDao, never()).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    private void whenValidationFailsOnUpdate(final String message) {
        org.mockito.Mockito.doThrow(new ValidationException(message)).when(trapdConfigDao).replaceConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    private static SecurityContext adminSecurityContext() {
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(true);
        return sc;
    }

    private static SecurityContext nonAdminSecurityContext() {
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(false);
        return sc;
    }

    private static void setField(final Object target, final String fieldName, final Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static String validTrapdConfigXml() {
        return """
                <trapd-configuration xmlns="http://xmlns.opennms.org/xsd/config/trapd" \
                snmp-trap-address="*" snmp-trap-port="10163" new-suspect-on-trap="false" \
                include-raw-message="false" threads="0" queue-size="10000" \
                batch-size="1000" batch-interval="500"/>""";
    }

    private static String validTrapdConfigXmlWithUseAddressFromVarbind() {
        return """
                <trapd-configuration xmlns="http://xmlns.opennms.org/xsd/config/trapd" \
                snmp-trap-address="*" snmp-trap-port="10163" new-suspect-on-trap="false" \
                include-raw-message="false" threads="0" queue-size="10000" \
                batch-size="1000" batch-interval="500" use-address-from-varbind="true"/>""";
    }

    private static String validTrapdConfigJson() {
        return """
                {
                "snmpTrapAddress": "*", \
                "snmpTrapPort": 10163, \
                "newSuspectOnTrap": false, \
                "includeRawMessage": false, \
                "threads": 0, \
                "queueSize": 10000, \
                "batchSize": 1000, \
                "batchInterval": 500 \
                }""";
    }

    private static String validTrapdConfigJsonWithUseAddressFromVarbind() {
        return """
                {
                "snmpTrapAddress": "*", \
                "snmpTrapPort": 10163, \
                "newSuspectOnTrap": false, \
                "includeRawMessage": false, \
                "threads": 0, \
                "queueSize": 10000, \
                "batchSize": 1000, \
                "batchInterval": 500, \
                "useAddressFromVarbind": true \
                }""";
    }
}
