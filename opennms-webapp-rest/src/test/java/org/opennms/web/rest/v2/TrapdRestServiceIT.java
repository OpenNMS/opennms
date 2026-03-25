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
import org.opennms.features.config.service.util.ConfigConvertUtil;
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
    public void testJsonToObjectSupportsKebabCaseProperties() {
        final String json = "{"
                + "\"snmp-trap-address\":\"0.0.0.0\","
                + "\"snmp-trap-port\":1162,"
                + "\"new-suspect-on-trap\":true,"
                + "\"include-raw-message\":true,"
                + "\"queue-size\":2000,"
                + "\"batch-size\":250,"
                + "\"batch-interval\":100,"
                + "\"threads\":4,"
                + "\"use-address-from-varbind\":true"
                + "}";

        final TrapdConfiguration config = ConfigConvertUtil.jsonToObject(json, TrapdConfiguration.class);

        assertEquals("0.0.0.0", config.getSnmpTrapAddress());
        assertEquals(1162, config.getSnmpTrapPort());
        assertTrue(config.isNewSuspectOnTrap());
        assertTrue(config.isIncludeRawMessage());
        assertEquals(2000, config.getQueueSize());
        assertEquals(250, config.getBatchSize());
        assertEquals(100, config.getBatchInterval());
        assertEquals(4, config.getThreads());
        assertTrue(config.shouldUseAddressFromVarbind());
    }

    @Test
    public void testJsonToObjectSupportsCamelCaseProperties() {
        final String json = "{"
                + "\"snmpTrapAddress\":\"127.0.0.1\","
                + "\"snmpTrapPort\":2162,"
                + "\"newSuspectOnTrap\":false,"
                + "\"batchSize\":10"
                + "}";

        final TrapdConfiguration config = ConfigConvertUtil.jsonToObject(json, TrapdConfiguration.class);

        assertEquals("127.0.0.1", config.getSnmpTrapAddress());
        assertEquals(2162, config.getSnmpTrapPort());
        assertFalse(config.isNewSuspectOnTrap());
        assertEquals(10, config.getBatchSize());
    }

    @Test
    public void testUseAddressFromVarbindDefaultsToFalse() {
        final TrapdConfiguration config = ConfigConvertUtil.jsonToObject("{}", TrapdConfiguration.class);
        assertFalse(config.shouldUseAddressFromVarbind());
    }

    @Test
    public void testObjectToJsonOmitsInternalHasFlags() {
        final TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(true);

        final String json = ConfigConvertUtil.objectToJson(config);

        assertTrue(json.contains("\"snmp-trap-port\":162"));
        assertTrue(json.contains("\"new-suspect-on-trap\":true"));
        assertFalse(json.contains("\"has-snmp-trap-port\""));
        assertFalse(json.contains("\"has-new-suspect-on-trap\""));
    }


    @Test
    public void uploadShouldReturnBadRequestWhenAttachmentMissing() {
        try (Response response = trapdRestService.uploadTrapdConfiguration(null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing uploaded file field 'upload'.", response.getEntity());
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
        user.setAuthPassphrase("real-auth-secret");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("real-priv-secret");
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
        user.setAuthPassphrase("real-auth-secret");
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
        userA.setAuthPassphrase("secret-a-auth");
        userA.setPrivacyProtocol("AES");
        userA.setPrivacyPassphrase("secret-a-priv");
        config.addSnmpv3User(userA);

        Snmpv3User userB = new Snmpv3User();
        userB.setSecurityName("user-b");
        userB.setSecurityLevel(2);
        userB.setAuthProtocol("MD5");
        userB.setAuthPassphrase("secret-b-auth");
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
        user.setAuthPassphrase("real-auth-secret");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("real-priv-secret");
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }

        // Original stored config must be untouched
        assertEquals("real-auth-secret", config.getSnmpv3User(0).getAuthPassphrase());
        assertEquals("real-priv-secret", config.getSnmpv3User(0).getPrivacyPassphrase());
    }

    @Test
    public void getShouldReturnConfigWithOtherFieldsIntactAfterMasking() {
        TrapdConfiguration config = buildMinimalConfig();
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("engine-user");
        user.setEngineId("0x8000000001020304");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("real-auth-secret");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("real-priv-secret");
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

    @Test
    public void saveUserShouldReturnBadRequestWhenPayloadMissing() {
        try (Response response = trapdRestService.saveTrapdUser(null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing SNMPv3 user in request body.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldPersistUserAndReturnOk() {
        TrapdConfiguration existing = new TrapdConfiguration();
        existing.setSnmpTrapPort(1162);
        existing.setNewSuspectOnTrap(false);
        when(trapdConfigDao.getConfig()).thenReturn(existing);

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setEngineId("8000000001020304");
        userDto.setSecurityName("opennms-user");
        userDto.setSecurityLevel(3);
        userDto.setAuthProtocol("SHA");
        userDto.setAuthPassphrase("auth-pass");
        userDto.setPrivacyProtocol("AES");
        userDto.setPrivacyPassphrase("priv-pass");

        try (Response response = trapdRestService.saveTrapdUser(userDto, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).updateConfig(captor.capture());
        TrapdConfiguration persisted = captor.getValue();
        assertEquals(1, persisted.getSnmpv3UserCount());
        assertEquals("opennms-user", persisted.getSnmpv3User(0).getSecurityName());
    }

    @Test
    public void saveUserShouldRejectWhenSecurityNameMissing() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityLevel(1);

        try (Response response = trapdRestService.saveTrapdUser(userDto, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityName is required.", response.getEntity());
        }

        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenSecurityLevelMissing() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityName("opennms-user");

        try (Response response = trapdRestService.saveTrapdUser(userDto, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel is required.", response.getEntity());
        }

        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenSecurityLevelThreeMissingPrivacy() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityName("opennms-user");
        userDto.setSecurityLevel(3);
        userDto.setAuthProtocol("SHA");
        userDto.setAuthPassphrase("auth-pass");

        try (Response response = trapdRestService.saveTrapdUser(userDto, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 3 requires both auth and privacy credentials.", response.getEntity());
        }

        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldReturnBadRequestWhenValidationFails() {
        TrapdConfiguration existing = new TrapdConfiguration();
        existing.setSnmpTrapPort(1162);
        existing.setNewSuspectOnTrap(false);
        when(trapdConfigDao.getConfig()).thenReturn(existing);
        whenValidationFailsOnUpdate("user validation failed");

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityName("opennms-user");
        userDto.setSecurityLevel(1);

        try (Response response = trapdRestService.saveTrapdUser(userDto, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("user validation failed", response.getEntity());
        }
    }

    @Test
    public void saveUserShouldReturnServerErrorWhenPersistenceThrows() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityName("opennms-user");
        userDto.setSecurityLevel(1);

        try (Response response = trapdRestService.saveTrapdUser(userDto, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to persist trapd user.", response.getEntity());
        }
    }

    @Test
    public void saveUserShouldCreateNewConfigWhenNoneExists() {
        when(trapdConfigDao.getConfig()).thenReturn(null);

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityName("opennms-user");
        userDto.setSecurityLevel(1);

        try (Response response = trapdRestService.saveTrapdUser(userDto, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).updateConfig(captor.capture());
        TrapdConfiguration persisted = captor.getValue();
        assertEquals(1, persisted.getSnmpv3UserCount());
        assertEquals(162, persisted.getSnmpTrapPort());
    }

    @Test
    public void saveUserShouldReturnConflictWhenSecurityNameAlreadyExists() {
        TrapdConfiguration existing = new TrapdConfiguration();
        existing.setSnmpTrapPort(1162);
        existing.setNewSuspectOnTrap(false);
        Snmpv3User existingUser = new Snmpv3User();
        existingUser.setSecurityName("duplicate-user");
        existing.addSnmpv3User(existingUser);
        when(trapdConfigDao.getConfig()).thenReturn(existing);

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityName("duplicate-user");
        userDto.setSecurityLevel(1);

        try (Response response = trapdRestService.saveTrapdUser(userDto, null)) {
            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            assertEquals("SNMPv3 user with securityName 'duplicate-user' already exists.", response.getEntity());
        }

        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    // --- validateSnmpv3UserPayload rule tests ---

    @Test
    public void saveUserShouldRejectWhenSecurityLevelOutOfRange() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityName("opennms-user");
        userDto.setSecurityLevel(5);

        try (Response response = trapdRestService.saveTrapdUser(userDto, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel must be between 1 and 3.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenAuthProtocolIsInvalid() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(2);
        user.setAuthProtocol("MD2");
        user.setAuthPassphrase("auth-pass");

        try (Response response = trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Unsupported authProtocol.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenPrivacyProtocolIsInvalid() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");
        user.setPrivacyProtocol("3DES");
        user.setPrivacyPassphrase("priv-pass");

        try (Response response = trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Unsupported privacyProtocol.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenAuthProtocolProvidedWithoutPassphrase() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(2);
        user.setAuthProtocol("SHA");
        // no authPassphrase

        try (Response response = trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("authProtocol and authPassphrase must be provided together.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenPrivacyProtocolProvidedWithoutPassphrase() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");
        user.setPrivacyProtocol("AES");
        // no privacyPassphrase

        try (Response response = trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("privacyProtocol and privacyPassphrase must be provided together.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenAuthPassphraseProvidedWithoutProtocol() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(2);
        // no authProtocol
        user.setAuthPassphrase("auth-pass");

        try (Response response = trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("authProtocol and authPassphrase must be provided together.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenPrivacyPassphraseProvidedWithoutProtocol() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");
        // no privacyProtocol
        user.setPrivacyPassphrase("priv-pass");

        try (Response response = trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("privacyProtocol and privacyPassphrase must be provided together.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenSecurityLevelOneHasAuthCredentials() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(1);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");

        try (Response response = trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 1 does not allow auth or privacy credentials.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenSecurityLevelTwoMissingAuthCredentials() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(2);
        // no auth protocol/passphrase

        try (Response response = trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 2 requires auth credentials and does not allow privacy credentials.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenSecurityLevelTwoHasPrivacyCredentials() {
        when(trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(2);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("priv-pass");

        try (Response response = trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 2 requires auth credentials and does not allow privacy credentials.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldReturnBadRequestWhenSecurityNameNull() {
        try (Response response = trapdRestService.deleteTrapdUser(null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Valid security name is required.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldReturnBadRequestWhenSecurityNameBlank() {
        try (Response response = trapdRestService.deleteTrapdUser("   ", null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Valid security name is required.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldReturnNotFoundWhenNoConfig() {
        when(trapdConfigDao.getConfig()).thenReturn(null);

        try (Response response = trapdRestService.deleteTrapdUser("missing-user", null)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("Trapd configuration not found.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldReturnNotFoundWhenSecurityNameMissing() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.deleteTrapdUser("missing-user", null)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("SNMPv3 user with securityName 'missing-user' was not found.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldRemoveUserAndReturnNoContent() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("user-to-delete");
        user.setSecurityLevel(1);
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.deleteTrapdUser("user-to-delete", null)) {
            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).updateConfig(captor.capture());
        assertEquals(0, captor.getValue().getSnmpv3UserCount());
    }

    @Test
    public void deleteUserShouldRemoveOnlyMatchingSecurityName() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User keep = new Snmpv3User();
        keep.setSecurityName("keep-user");
        Snmpv3User remove = new Snmpv3User();
        remove.setSecurityName("remove-user");
        config.addSnmpv3User(keep);
        config.addSnmpv3User(remove);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = trapdRestService.deleteTrapdUser("remove-user", null)) {
            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).updateConfig(captor.capture());
        assertEquals(1, captor.getValue().getSnmpv3UserCount());
        assertEquals("keep-user", captor.getValue().getSnmpv3User(0).getSecurityName());
    }

    @Test
    public void deleteUserShouldReturnBadRequestWhenValidationFails() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("test-user");
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);
        whenValidationFailsOnUpdate("delete validation error");

        try (Response response = trapdRestService.deleteTrapdUser("test-user", null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("delete validation error", response.getEntity());
        }
    }

    @Test
    public void deleteUserShouldReturnServerErrorWhenPersistenceThrows() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("test-user");
        config.addSnmpv3User(user);
        when(trapdConfigDao.getConfig()).thenReturn(config);
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        try (Response response = trapdRestService.deleteTrapdUser("test-user", null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to delete trapd user.", response.getEntity());
        }
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenSecurityNameNull() {
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = trapdRestService.updateTrapdUser(null, user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Valid security name is required.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenSecurityNameBlank() {
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = trapdRestService.updateTrapdUser("", user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Valid security name is required.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenPathAndPayloadSecurityNameMismatch() {
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("payload-user");
        user.setSecurityLevel(1);

        try (Response response = trapdRestService.updateTrapdUser("path-user", user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Path securityName must match payload securityName.", response.getEntity());
        }
        verify(trapdConfigDao, never()).getConfig();
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenPayloadNull() {
        try (Response response = trapdRestService.updateTrapdUser("existing-user", null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing SNMPv3 user in request body.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnNotFoundWhenNoConfig() {
        when(trapdConfigDao.getConfig()).thenReturn(null);

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = trapdRestService.updateTrapdUser("opennms-user", user, null)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("Trapd configuration not found.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnNotFoundWhenSecurityNameMissing() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("missing-user");

        try (Response response = trapdRestService.updateTrapdUser("missing-user", user, null)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("SNMPv3 user with securityName 'missing-user' was not found.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenSecurityLevelMissing() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User existing = new Snmpv3User();
        existing.setSecurityName("existing-user");
        config.addSnmpv3User(existing);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("existing-user");

        try (Response response = trapdRestService.updateTrapdUser("existing-user", user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel is required.", response.getEntity());
        }

        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenPayloadValidationFails() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User existing = new Snmpv3User();
        existing.setSecurityName("existing-user");
        config.addSnmpv3User(existing);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        // securityLevel 3 requires privacy — missing privacy intentionally
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("existing-user");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");

        try (Response response = trapdRestService.updateTrapdUser("existing-user", user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 3 requires both auth and privacy credentials.", response.getEntity());
        }
        verify(trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReplaceUserBySecurityNameAndReturnOk() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User existing = new Snmpv3User();
        existing.setSecurityName("old-user");
        existing.setSecurityLevel(1);
        config.addSnmpv3User(existing);
        when(trapdConfigDao.getConfig()).thenReturn(config);

        Snmpv3UserDto updatedDto = new Snmpv3UserDto();
        updatedDto.setSecurityName("old-user");
        updatedDto.setSecurityLevel(3);
        updatedDto.setAuthProtocol("SHA");
        updatedDto.setAuthPassphrase("auth-pass");
        updatedDto.setPrivacyProtocol("AES");
        updatedDto.setPrivacyPassphrase("priv-pass");

        try (Response response = trapdRestService.updateTrapdUser("old-user", updatedDto, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(trapdConfigDao).updateConfig(captor.capture());
        assertEquals(1, captor.getValue().getSnmpv3UserCount());
        assertEquals("old-user", captor.getValue().getSnmpv3User(0).getSecurityName());
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenSchemaValidationFails() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User existing = new Snmpv3User();
        existing.setSecurityName("existing-user");
        config.addSnmpv3User(existing);
        when(trapdConfigDao.getConfig()).thenReturn(config);
        whenValidationFailsOnUpdate("schema update error");

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityName("existing-user");
        userDto.setSecurityLevel(1);

        try (Response response = trapdRestService.updateTrapdUser("existing-user", userDto, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("schema update error", response.getEntity());
        }
    }

    @Test
    public void updateUserShouldReturnServerErrorWhenPersistenceThrows() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User existing = new Snmpv3User();
        existing.setSecurityName("existing-user");
        config.addSnmpv3User(existing);
        when(trapdConfigDao.getConfig()).thenReturn(config);
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        Snmpv3UserDto userDto = new Snmpv3UserDto();
        userDto.setSecurityName("existing-user");
        userDto.setSecurityLevel(1);

        try (Response response = trapdRestService.updateTrapdUser("existing-user", userDto, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to update trapd user.", response.getEntity());
        }
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
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("snmpTrapPort is required.", response.getEntity());
        }

        verify(trapdConfigDao, never()).updateConfigWithoutUsers(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateShouldReturnBadRequestWhenNewSuspectOnTrapMissing() {
        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapPort(10164);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("newSuspectOnTrap is required.", response.getEntity());
        }

        verify(trapdConfigDao, never()).updateConfigWithoutUsers(org.mockito.Mockito.any(TrapdConfiguration.class));
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
        verify(trapdConfigDao).updateConfigWithoutUsers(captor.capture());
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
        verify(trapdConfigDao).updateConfigWithoutUsers(captor.capture());
        assertTrue(captor.getValue().shouldUseAddressFromVarbind());
    }

    @Test
    public void updateShouldReturnBadRequestWhenValidationFails() {
        org.mockito.Mockito.doThrow(new ValidationException("validation failed"))
                .when(trapdConfigDao).updateConfigWithoutUsers(org.mockito.Mockito.any(TrapdConfiguration.class));

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("validation failed", response.getEntity());
        }
    }

    @Test
    public void updateShouldReturnServerErrorWhenPersistenceThrows() {
        org.mockito.Mockito.doThrow(new RuntimeException("db down"))
                .when(trapdConfigDao).updateConfigWithoutUsers(org.mockito.Mockito.any(TrapdConfiguration.class));

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapPort(10164);
        payload.setNewSuspectOnTrap(false);

        try (Response response = trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to persist trapd configuration.", response.getEntity());
        }
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
}
