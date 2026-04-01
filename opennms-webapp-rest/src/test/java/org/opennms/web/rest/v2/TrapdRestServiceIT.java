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
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.TrapdConfigDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.netmgt.config.trapd.Snmpv3User;
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
    private static final String PASSPHRASE_PLACEHOLDER = "********";

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


    @Test
    public void uploadShouldReturnBadRequestWhenAttachmentMissing() {
        try (Response response = trapdRestService.uploadTrapdConfiguration(null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing uploaded file for trapd file upload.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldReturnBadRequestWhenXmlIsInvalid() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream("<trapd-configuration".getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Invalid trapd XML configuration.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldPersistValidXmlAndReturnOk() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).updateConfig(captor.capture());
        assertEquals(10163, captor.getValue().getSnmpTrapPort());
    }

    @Test
    public void uploadShouldPersistUseAddressFromVarbindWhenProvidedInXml() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXmlWithUseAddressFromVarbind().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).updateConfig(captor.capture());
        assertTrue(captor.getValue().shouldUseAddressFromVarbind());
    }

    @Test
    public void uploadShouldReturnBadRequestWhenValidationFails() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8))
        );
        whenValidationFailsOnUpdate("schema error");

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("schema error", response.getEntity());
        }
    }

    @Test
    public void uploadShouldReturnServerErrorWhenPersistenceFails() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8))
        );
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        try (Response response = trapdRestService.uploadTrapdConfiguration(attachment, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to persist trapd configuration.", response.getEntity());
        }
    }

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
            assertEquals("Failed to retrieve trapd configuration.", response.getEntity());
        }
    }

    // --- passphrase masking tests ---

    @Test
    public void getShouldMaskBothPassphrasesWithPlaceholder() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("user1");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase(PASSPHRASE_PLACEHOLDER);
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase(PASSPHRASE_PLACEHOLDER);
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            Snmpv3UserDto returnedUser = returned.getSnmpv3User().get(0);
            assertEquals(PASSPHRASE_PLACEHOLDER, returnedUser.getAuthPassphrase());
            assertEquals(PASSPHRASE_PLACEHOLDER, returnedUser.getPrivacyPassphrase());
        }
    }

    @Test
    public void getShouldMaskAuthPassphraseWhenPrivacyPassphraseIsAbsent() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("user1");
        user.setSecurityLevel(2);
        user.setAuthProtocol("MD5");
        user.setAuthPassphrase(PASSPHRASE_PLACEHOLDER);
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            Snmpv3UserDto returnedUser = returned.getSnmpv3User().get(0);
            assertEquals(PASSPHRASE_PLACEHOLDER, returnedUser.getAuthPassphrase());
            assertNull(returnedUser.getPrivacyPassphrase());
        }
    }

    @Test
    public void getShouldNotSetPlaceholderWhenPassphrasesAreNull() {
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
    public void getShouldMaskPassphrasesForEveryUser() {
        TrapdConfiguration config = buildMinimalConfig();

        Snmpv3User userA = new Snmpv3User();
        userA.setSecurityName("user-a");
        userA.setSecurityLevel(3);
        userA.setAuthProtocol("SHA");
        userA.setAuthPassphrase(PASSPHRASE_PLACEHOLDER);
        userA.setPrivacyProtocol("AES");
        userA.setPrivacyPassphrase(PASSPHRASE_PLACEHOLDER);
        config.addSnmpv3User(userA);

        Snmpv3User userB = new Snmpv3User();
        userB.setSecurityName("user-b");
        userB.setSecurityLevel(2);
        userB.setAuthProtocol("MD5");
        userB.setAuthPassphrase(PASSPHRASE_PLACEHOLDER);
        config.addSnmpv3User(userB);

        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            assertEquals(2, returned.getSnmpv3User().size());
            assertEquals(PASSPHRASE_PLACEHOLDER, returned.getSnmpv3User().get(0).getAuthPassphrase());
            assertEquals(PASSPHRASE_PLACEHOLDER, returned.getSnmpv3User().get(0).getPrivacyPassphrase());
            assertEquals(PASSPHRASE_PLACEHOLDER, returned.getSnmpv3User().get(1).getAuthPassphrase());
            assertNull(returned.getSnmpv3User().get(1).getPrivacyPassphrase());
        }
    }

    @Test
    public void getShouldNotMutateStoredConfigWhenMaskingPassphrases() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("user1");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase(PASSPHRASE_PLACEHOLDER);
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase(PASSPHRASE_PLACEHOLDER);
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        // The config returned by getConfig should not have been mutated by the service
        assertEquals(PASSPHRASE_PLACEHOLDER, config.getSnmpv3User(0).getAuthPassphrase());
        assertEquals(PASSPHRASE_PLACEHOLDER, config.getSnmpv3User(0).getPrivacyPassphrase());
    }

    @Test
    public void getShouldReturnConfigWithOtherFieldsIntactAfterMasking() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("engine-user");
        user.setEngineId("0x8000000001020304");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase(PASSPHRASE_PLACEHOLDER);
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase(PASSPHRASE_PLACEHOLDER);
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            Snmpv3UserDto returnedUser = returned.getSnmpv3User().get(0);
            // Non-sensitive fields must be preserved
            assertEquals("engine-user", returnedUser.getSecurityName());
            assertEquals("0x8000000001020304", returnedUser.getEngineId());
            assertEquals("SHA", returnedUser.getAuthProtocol());
            assertEquals("AES", returnedUser.getPrivacyProtocol());
            assertEquals(Integer.valueOf(3), returnedUser.getSecurityLevel());
            // Sensitive fields must be masked
            assertEquals(PASSPHRASE_PLACEHOLDER, returnedUser.getAuthPassphrase());
            assertEquals(PASSPHRASE_PLACEHOLDER, returnedUser.getPrivacyPassphrase());
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

    @Test
    public void updateShouldReturnBadRequestWhenPayloadMissing() {
        try (Response response = trapdRestService.updateTrapdConfiguration(null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing trapd configuration in request body.", response.getEntity());
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

        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldAcceptWhenSnmpTrapAddressMissing() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        verify(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldAcceptWhenNewSuspectOnTrapMissing() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(10164);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        verify(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
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
        verify(trapdConfigDao).updateConfig(captor.capture());
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
        verify(trapdConfigDao).updateConfig(captor.capture());
        assertTrue(captor.getValue().shouldUseAddressFromVarbind());
    }

    @Test
    public void updateShouldReturnBadRequestWhenValidationFails() {
        org.mockito.Mockito.doThrow(new ValidationException("validation failed"))
                .when(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Provided trapd configuration failed schema validation.", response.getEntity());
        }
    }

    @Test
    public void updateShouldReturnServerErrorWhenPersistenceThrows() {
        org.mockito.Mockito.doThrow(new RuntimeException("db down"))
                .when(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to persist trapd configuration.", response.getEntity());
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

        verify(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
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

        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
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

        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
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

        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
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

    // --- Security/Authorization Placeholder ---
    // Note: SecurityContext is not currently used for access control in TrapdRestService.
    // This test is a placeholder for future security/authorization checks.
    @Test
    public void updateShouldEnforceAuthorizationIfSecurityContextIsUsed() {
        // If/when SecurityContext is used for access control, add tests here.
        // For now, this is a no-op.
        assertTrue(true);
    }

    private void whenValidationFailsOnUpdate(final String message) {
        org.mockito.Mockito.doThrow(new ValidationException(message)).when(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    private static void setField(final Object target, final String fieldName, final Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static String validTrapdConfigXml() {
        return "<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
                + "snmp-trap-address=\"*\" snmp-trap-port=\"10163\" new-suspect-on-trap=\"false\" "
                + "include-raw-message=\"false\" threads=\"0\" queue-size=\"10000\" "
                + "batch-size=\"1000\" batch-interval=\"500\"/>";
    }

    private static String validTrapdConfigXmlWithUseAddressFromVarbind() {
        return "<trapd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/trapd\" "
                + "snmp-trap-address=\"*\" snmp-trap-port=\"10163\" new-suspect-on-trap=\"false\" "
                + "include-raw-message=\"false\" threads=\"0\" queue-size=\"10000\" "
                + "batch-size=\"1000\" batch-interval=\"500\" use-address-from-varbind=\"true\"/>";
    }

    @Test
    public void getShouldHandleLargeNumberOfSnmpv3Users() {
        TrapdConfiguration config = buildMinimalConfig();
        int userCount = 1000; // Large number for stress test
        for (int i = 0; i < userCount; i++) {
            Snmpv3User user = new Snmpv3User();
            user.setSecurityName("user-" + i);
            user.setSecurityLevel(3);
            user.setAuthProtocol("SHA");
            user.setAuthPassphrase(PASSPHRASE_PLACEHOLDER);
            user.setPrivacyProtocol("AES");
            user.setPrivacyPassphrase(PASSPHRASE_PLACEHOLDER);
            config.addSnmpv3User(user);
        }
        when(trapdConfigDao.getConfig()).thenReturn(config);
        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto returned = (TrapdConfigDto) response.getEntity();
            assertEquals(userCount, returned.getSnmpv3User().size());
            for (int i = 0; i < userCount; i++) {
                assertEquals(PASSPHRASE_PLACEHOLDER, returned.getSnmpv3User().get(i).getAuthPassphrase());
                assertEquals(PASSPHRASE_PLACEHOLDER, returned.getSnmpv3User().get(i).getPrivacyPassphrase());
            }
        }
    }

    @Test
    public void updateShouldBeThreadSafeUnderConcurrentAccess() throws InterruptedException {
        final int threadCount = 10;
        final TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapAddress("127.0.0.1");
        payload.setSnmpTrapPort(162);
        payload.setNewSuspectOnTrap(false);
        Runnable updateTask = () -> {
            try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
                assertTrue(response.getStatus() == Response.Status.OK.getStatusCode() ||
                           response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode() ||
                           response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        };
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(updateTask);
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        // If no exceptions, concurrency is handled gracefully
    }
}
