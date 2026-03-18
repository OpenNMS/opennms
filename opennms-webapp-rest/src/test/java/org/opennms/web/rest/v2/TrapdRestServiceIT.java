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

    @Autowired
    private TrapdRestService m_trapdRestService;

    @Autowired
    private TrapdConfigDao m_trapdConfigDao;

    @Before
    public void setUp() throws Exception {
        m_trapdRestService = new TrapdRestService();
        m_trapdConfigDao = mock(TrapdConfigDao.class);
        setField(m_trapdRestService, "m_trapdConfigDao", m_trapdConfigDao);
    }

    @Test
    public void uploadShouldReturnBadRequestWhenAttachmentMissing() {
        try (Response response = m_trapdRestService.uploadTrapdConfiguration(null, null)) {
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

        try (Response response = m_trapdRestService.uploadTrapdConfiguration(attachment, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Invalid trapd XML configuration.", response.getEntity());
        }
    }

    @Test
    public void uploadShouldPersistValidXmlAndReturnDto() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = m_trapdRestService.uploadTrapdConfiguration(attachment, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof TrapdConfigDto);
            TrapdConfigDto dto = (TrapdConfigDto) response.getEntity();
            assertEquals(Integer.valueOf(10163), dto.getSnmpTrapPort());
            assertEquals("*", dto.getSnmpTrapAddress());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(m_trapdConfigDao).updateConfig(captor.capture());
        assertEquals(10163, captor.getValue().getSnmpTrapPort());
    }

    @Test
    public void uploadShouldPersistUseAddressFromVarbindWhenProvidedInXml() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXmlWithUseAddressFromVarbind().getBytes(StandardCharsets.UTF_8))
        );

        try (Response response = m_trapdRestService.uploadTrapdConfiguration(attachment, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof TrapdConfigDto);
            TrapdConfigDto dto = (TrapdConfigDto) response.getEntity();
            assertEquals(Boolean.TRUE, dto.getUseAddressFromVarbind());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(m_trapdConfigDao).updateConfig(captor.capture());
        assertTrue(captor.getValue().shouldUseAddressFromVarbind());
    }

    @Test
    public void uploadShouldReturnBadRequestWhenValidationFails() {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(InputStream.class)).thenReturn(
                new ByteArrayInputStream(validTrapdConfigXml().getBytes(StandardCharsets.UTF_8))
        );
        whenValidationFailsOnUpdate("schema error");

        try (Response response = m_trapdRestService.uploadTrapdConfiguration(attachment, null)) {
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
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(m_trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        try (Response response = m_trapdRestService.uploadTrapdConfiguration(attachment, null)) {
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
        when(m_trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = m_trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof TrapdConfigDto);
            TrapdConfigDto dto = (TrapdConfigDto) response.getEntity();
            assertEquals(Integer.valueOf(162), dto.getSnmpTrapPort());
            assertEquals("127.0.0.1", dto.getSnmpTrapAddress());
        }
    }

    @Test
    public void getShouldReturnNotFoundWhenNoConfigurationExists() {
        when(m_trapdConfigDao.getConfig()).thenReturn(null);

        try (Response response = m_trapdRestService.getTrapdConfiguration(null)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("Trapd configuration not found.", response.getEntity());
        }
    }

    @Test
    public void saveUserShouldReturnBadRequestWhenPayloadMissing() {
        try (Response response = m_trapdRestService.saveTrapdUser(null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing SNMPv3 user in request body.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldPersistUserAndReturnDto() {
        TrapdConfiguration existing = new TrapdConfiguration();
        existing.setSnmpTrapPort(1162);
        existing.setNewSuspectOnTrap(false);
        when(m_trapdConfigDao.getConfig()).thenReturn(existing);

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setEngineId("8000000001020304");
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("priv-pass");

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof Snmpv3UserDto);
            Snmpv3UserDto dto = (Snmpv3UserDto) response.getEntity();
            assertEquals("opennms-user", dto.getSecurityName());
            assertEquals(Integer.valueOf(3), dto.getSecurityLevel());
            assertEquals("AES", dto.getPrivacyProtocol());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(m_trapdConfigDao).updateConfig(captor.capture());
        TrapdConfiguration persisted = captor.getValue();
        assertEquals(1, persisted.getSnmpv3UserCount());
        assertEquals("opennms-user", persisted.getSnmpv3User(0).getSecurityName());
    }

    @Test
    public void saveUserShouldRejectWhenSecurityNameMissing() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityLevel(1);

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityName is required.", response.getEntity());
        }

        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenSecurityLevelThreeMissingPrivacy() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 3 requires both auth and privacy credentials.", response.getEntity());
        }

        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldReturnBadRequestWhenValidationFails() {
        TrapdConfiguration existing = new TrapdConfiguration();
        existing.setSnmpTrapPort(1162);
        existing.setNewSuspectOnTrap(false);
        when(m_trapdConfigDao.getConfig()).thenReturn(existing);
        whenValidationFailsOnUpdate("user validation failed");

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("user validation failed", response.getEntity());
        }
    }

    @Test
    public void saveUserShouldReturnServerErrorWhenPersistenceThrows() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(m_trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to persist trapd user.", response.getEntity());
        }
    }

    @Test
    public void saveUserShouldCreateNewConfigWhenNoneExists() {
        when(m_trapdConfigDao.getConfig()).thenReturn(null);

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(1);

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof Snmpv3UserDto);
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(m_trapdConfigDao).updateConfig(captor.capture());
        TrapdConfiguration persisted = captor.getValue();
        assertEquals(1, persisted.getSnmpv3UserCount());
        assertEquals(162, persisted.getSnmpTrapPort());
    }

    // --- validateSnmpv3UserPayload rule tests ---

    @Test
    public void saveUserShouldRejectWhenSecurityLevelOutOfRange() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(5);

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel must be between 1 and 3.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenAuthProtocolIsInvalid() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setAuthProtocol("MD2");
        user.setAuthPassphrase("auth-pass");

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Unsupported authProtocol.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenPrivacyProtocolIsInvalid() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");
        user.setPrivacyProtocol("3DES");
        user.setPrivacyPassphrase("priv-pass");

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Unsupported privacyProtocol.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenAuthProtocolProvidedWithoutPassphrase() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setAuthProtocol("SHA");
        // no authPassphrase

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("authProtocol and authPassphrase must be provided together.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenPrivacyProtocolProvidedWithoutPassphrase() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");
        user.setPrivacyProtocol("AES");
        // no privacyPassphrase

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("privacyProtocol and privacyPassphrase must be provided together.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenSecurityLevelOneHasAuthCredentials() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(1);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 1 does not allow auth or privacy credentials.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenSecurityLevelTwoMissingAuthCredentials() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(2);
        // no auth protocol/passphrase

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 2 requires auth credentials and does not allow privacy credentials.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void saveUserShouldRejectWhenSecurityLevelTwoHasPrivacyCredentials() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(2);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");
        user.setPrivacyProtocol("AES");
        user.setPrivacyPassphrase("priv-pass");

        try (Response response = m_trapdRestService.saveTrapdUser(user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 2 requires auth credentials and does not allow privacy credentials.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldReturnBadRequestWhenIndexNull() {
        try (Response response = m_trapdRestService.deleteTrapdUser(null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Valid user index is required.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldReturnBadRequestWhenIndexNegative() {
        try (Response response = m_trapdRestService.deleteTrapdUser(-1, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Valid user index is required.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldReturnNotFoundWhenNoConfig() {
        when(m_trapdConfigDao.getConfig()).thenReturn(null);

        try (Response response = m_trapdRestService.deleteTrapdUser(0, null)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("Trapd configuration not found.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldReturnBadRequestWhenIndexOutOfRange() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        when(m_trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = m_trapdRestService.deleteTrapdUser(5, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("out of range"));
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void deleteUserShouldRemoveUserAndReturnDto() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("user-to-delete");
        user.setSecurityLevel(1);
        config.addSnmpv3User(user);
        when(m_trapdConfigDao.getConfig()).thenReturn(config);

        try (Response response = m_trapdRestService.deleteTrapdUser(0, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof Snmpv3UserDto);
            Snmpv3UserDto dto = (Snmpv3UserDto) response.getEntity();
            assertEquals("user-to-delete", dto.getSecurityName());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(m_trapdConfigDao).updateConfig(captor.capture());
        assertEquals(0, captor.getValue().getSnmpv3UserCount());
    }

    @Test
    public void deleteUserShouldReturnBadRequestWhenValidationFails() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("test-user");
        config.addSnmpv3User(user);
        when(m_trapdConfigDao.getConfig()).thenReturn(config);
        whenValidationFailsOnUpdate("delete validation error");

        try (Response response = m_trapdRestService.deleteTrapdUser(0, null)) {
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
        when(m_trapdConfigDao.getConfig()).thenReturn(config);
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(m_trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        try (Response response = m_trapdRestService.deleteTrapdUser(0, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to delete trapd user.", response.getEntity());
        }
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenIndexNull() {
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = m_trapdRestService.updateTrapdUser(null, user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Valid user index is required.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenIndexNegative() {
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = m_trapdRestService.updateTrapdUser(-1, user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Valid user index is required.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenPayloadNull() {
        try (Response response = m_trapdRestService.updateTrapdUser(0, null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing SNMPv3 user in request body.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnNotFoundWhenNoConfig() {
        when(m_trapdConfigDao.getConfig()).thenReturn(null);

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = m_trapdRestService.updateTrapdUser(0, user, null)) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("Trapd configuration not found.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenIndexOutOfRange() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        when(m_trapdConfigDao.getConfig()).thenReturn(config);

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = m_trapdRestService.updateTrapdUser(5, user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertTrue(((String) response.getEntity()).contains("out of range"));
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenPayloadValidationFails() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User existing = new Snmpv3User();
        existing.setSecurityName("existing-user");
        config.addSnmpv3User(existing);
        when(m_trapdConfigDao.getConfig()).thenReturn(config);

        // securityLevel 3 requires privacy — missing privacy intentionally
        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");
        user.setSecurityLevel(3);
        user.setAuthProtocol("SHA");
        user.setAuthPassphrase("auth-pass");

        try (Response response = m_trapdRestService.updateTrapdUser(0, user, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("securityLevel 3 requires both auth and privacy credentials.", response.getEntity());
        }
        verify(m_trapdConfigDao, never()).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
    }

    @Test
    public void updateUserShouldReplaceUserAtIndexAndReturnDto() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User existing = new Snmpv3User();
        existing.setSecurityName("old-user");
        existing.setSecurityLevel(1);
        config.addSnmpv3User(existing);
        when(m_trapdConfigDao.getConfig()).thenReturn(config);

        Snmpv3UserDto updated = new Snmpv3UserDto();
        updated.setSecurityName("new-user");
        updated.setSecurityLevel(3);
        updated.setAuthProtocol("SHA");
        updated.setAuthPassphrase("auth-pass");
        updated.setPrivacyProtocol("AES");
        updated.setPrivacyPassphrase("priv-pass");

        try (Response response = m_trapdRestService.updateTrapdUser(0, updated, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(response.getEntity() instanceof Snmpv3UserDto);
            Snmpv3UserDto dto = (Snmpv3UserDto) response.getEntity();
            assertEquals("new-user", dto.getSecurityName());
            assertEquals(Integer.valueOf(3), dto.getSecurityLevel());
            assertEquals("AES", dto.getPrivacyProtocol());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(m_trapdConfigDao).updateConfig(captor.capture());
        assertEquals(1, captor.getValue().getSnmpv3UserCount());
        assertEquals("new-user", captor.getValue().getSnmpv3User(0).getSecurityName());
    }

    @Test
    public void updateUserShouldReturnBadRequestWhenSchemaValidationFails() {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(162);
        config.setNewSuspectOnTrap(false);
        Snmpv3User existing = new Snmpv3User();
        existing.setSecurityName("existing-user");
        config.addSnmpv3User(existing);
        when(m_trapdConfigDao.getConfig()).thenReturn(config);
        whenValidationFailsOnUpdate("schema update error");

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = m_trapdRestService.updateTrapdUser(0, user, null)) {
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
        when(m_trapdConfigDao.getConfig()).thenReturn(config);
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(m_trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        Snmpv3UserDto user = new Snmpv3UserDto();
        user.setSecurityName("opennms-user");

        try (Response response = m_trapdRestService.updateTrapdUser(0, user, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to update trapd user.", response.getEntity());
        }
    }

    @Test
    public void updateShouldReturnBadRequestWhenPayloadMissing() {
        try (Response response = m_trapdRestService.updateTrapdConfiguration(null, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Missing trapd configuration in request body.", response.getEntity());
        }
    }

    @Test
    public void updateShouldMergePayloadAndPersist() {
        TrapdConfiguration existing = new TrapdConfiguration();
        existing.setSnmpTrapAddress("127.0.0.1");
        existing.setSnmpTrapPort(1162);
        existing.setThreads(4);
        existing.setQueueSize(1000);
        when(m_trapdConfigDao.getConfig()).thenReturn(existing, existing);

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapPort(10164);
        payload.setIncludeRawMessage(Boolean.TRUE);

        try (Response response = m_trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto dto = (TrapdConfigDto) response.getEntity();
            assertEquals(Integer.valueOf(10164), dto.getSnmpTrapPort());
            assertEquals(Integer.valueOf(4), dto.getThreads());
            assertEquals("127.0.0.1", dto.getSnmpTrapAddress());
            assertEquals(Boolean.TRUE, dto.getIncludeRawMessage());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(m_trapdConfigDao).updateConfig(captor.capture());
        TrapdConfiguration persisted = captor.getValue();
        assertEquals(10164, persisted.getSnmpTrapPort());
        assertEquals(4, persisted.getThreads());
    }

    @Test
    public void updateShouldPersistUseAddressFromVarbindWhenProvided() {
        TrapdConfiguration existing = new TrapdConfiguration();
        existing.setSnmpTrapAddress("127.0.0.1");
        existing.setSnmpTrapPort(1162);
        existing.setNewSuspectOnTrap(false);
        existing.setUseAddressFromVarbind(false);
        when(m_trapdConfigDao.getConfig()).thenReturn(existing, existing);

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setUseAddressFromVarbind(Boolean.TRUE);

        try (Response response = m_trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            TrapdConfigDto dto = (TrapdConfigDto) response.getEntity();
            assertEquals(Boolean.TRUE, dto.getUseAddressFromVarbind());
        }

        ArgumentCaptor<TrapdConfiguration> captor = ArgumentCaptor.forClass(TrapdConfiguration.class);
        verify(m_trapdConfigDao).updateConfig(captor.capture());
        assertTrue(captor.getValue().shouldUseAddressFromVarbind());
    }

    @Test
    public void updateShouldReturnBadRequestWhenValidationFails() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());
        whenValidationFailsOnUpdate("validation failed");

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapPort(10164);

        try (Response response = m_trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("validation failed", response.getEntity());
        }
    }

    @Test
    public void updateShouldReturnServerErrorWhenPersistenceThrows() {
        when(m_trapdConfigDao.getConfig()).thenReturn(new TrapdConfiguration());
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(m_trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));

        TrapdConfigDto payload = new TrapdConfigDto();
        payload.setSnmpTrapPort(10164);

        try (Response response = m_trapdRestService.updateTrapdConfiguration(payload, null)) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            assertEquals("Failed to persist trapd configuration.", response.getEntity());
        }
    }

    private void whenValidationFailsOnUpdate(final String message) {
        org.mockito.Mockito.doThrow(new ValidationException(message)).when(m_trapdConfigDao).updateConfig(org.mockito.Mockito.any(TrapdConfiguration.class));
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

