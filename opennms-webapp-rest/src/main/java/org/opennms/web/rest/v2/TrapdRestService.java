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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.TrapdConfigDao;
import org.opennms.web.rest.v2.api.TrapdRestApi;
import org.opennms.web.rest.v2.model.Snmpv3UserDto;
import org.opennms.web.rest.v2.model.TrapdConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrapdRestService implements TrapdRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(TrapdRestService.class);
    private static final Set<String> AUTH_PROTOCOLS = new HashSet<>(Arrays.asList("MD5", "SHA", "SHA-224", "SHA-256", "SHA-512"));
    private static final Set<String> PRIVACY_PROTOCOLS = new HashSet<>(Arrays.asList("DES", "AES", "AES192", "AES256"));

    @Autowired
    private TrapdConfigDao trapdConfigDao;

    @Override
    public Response uploadTrapdConfiguration(final Attachment attachment, final SecurityContext securityContext) {
        if (attachment == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing uploaded file field 'upload'.").build();
        }

        final TrapdConfiguration config;
        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            config = JaxbUtils.unmarshal(TrapdConfiguration.class, inputStream);
        } catch (Exception e) {
            LOG.warn("Failed to parse uploaded trapd configuration.", e);
            return Response.status(Status.BAD_REQUEST).entity("Invalid trapd XML configuration.").build();
        }

        try {
            trapdConfigDao.updateConfig(config);
            return Response.ok().build();
        } catch (ValidationException e) {
            LOG.warn("Uploaded trapd configuration failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Failed to persist uploaded trapd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist trapd configuration.").build();
        }
    }

    @Override
    public Response getTrapdConfiguration(final SecurityContext securityContext) {
        try {
            TrapdConfiguration config = trapdConfigDao.getMaskedConfig();
            if (config == null) {
                return Response.status(Status.NOT_FOUND).entity("Trapd configuration not found.").build();
            }
            return Response.ok(TrapdConfigDto.toDto(config)).build();
        } catch (Exception e) {
            LOG.error("Failed to retrieve trapd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve trapd configuration.").build();
        }
    }

    @Override
    public Response updateTrapdConfiguration(TrapdConfigDto configDto, SecurityContext securityContext) {
        if (configDto == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing trapd configuration in request body.").build();
        }

        String fieldValidation = validateTrapdConfigDtoFields(configDto);
        if (fieldValidation != null) {
            return Response.status(Status.BAD_REQUEST).entity(fieldValidation).build();
        }

        TrapdConfiguration payload = configDto.toEntity();
        final String validationMessage = validateTrapdConfigurationPayload(payload);
        if (validationMessage != null) {
            return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
        }

        try {
            trapdConfigDao.updateConfigWithoutUsers(payload);
            return Response.ok().build();
        } catch (ValidationException e) {
            LOG.warn("Provided trapd configuration failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Failed to persist provided trapd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist trapd configuration.").build();
        }
    }

    @Override
    public Response saveTrapdUser(Snmpv3UserDto user, SecurityContext securityContext) {
        if (user == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing SNMPv3 user in request body.").build();
        }

        try {
            TrapdConfiguration config = trapdConfigDao.getMaskedConfig();
            if (config == null) {
                return Response.status(Status.NOT_FOUND).entity("Trapd configuration not found.").build();
            }

            if (findUserIndexBySecurityName(config, user.getSecurityName()) >= 0) {
                return Response.status(Status.CONFLICT)
                        .entity("SNMPv3 user with securityName '" + user.getSecurityName() + "' already exists.")
                        .build();
            }

            final String validationMessage = validateSnmpv3UserPayload(user);
            if (validationMessage != null) {
                return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
            }

            // Ensure required attributes are present when persisting a newly-created config.
            if (!config.hasSnmpTrapPort()) {
                config.setSnmpTrapPort(162);
            }
            if (!config.hasNewSuspectOnTrap()) {
                config.setNewSuspectOnTrap(false);
            }

            config.addSnmpv3User(user.toEntity());
            trapdConfigDao.updateConfig(config);
            return Response.ok().build();
        } catch (ValidationException e) {
            LOG.warn("Provided SNMPv3 user failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Failed to persist provided SNMPv3 user.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist trapd user.").build();
        }
    }

    @Override
    public Response updateTrapdUser(String securityName, Snmpv3UserDto user, SecurityContext securityContext) {
        if (StringUtils.isBlank(securityName)) {
            return Response.status(Status.BAD_REQUEST).entity("Valid security name is required.").build();
        }

        if (user == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing SNMPv3 user in request body.").build();
        }

        if (!StringUtils.equals(securityName, user.getSecurityName())) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Path securityName must match payload securityName.")
                    .build();
        }

        try {
            final TrapdConfiguration config = trapdConfigDao.getMaskedConfig();
            if (config == null) {
                return Response.status(Status.NOT_FOUND).entity("Trapd configuration not found.").build();
            }

            final int index = findUserIndexBySecurityName(config, securityName);
            if (index < 0) {
                return Response.status(Status.NOT_FOUND)
                        .entity("SNMPv3 user with securityName '" + securityName + "' was not found.")
                        .build();
            }

            final String validationMessage = validateSnmpv3UserPayload(user);
            if (validationMessage != null) {
                return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
            }

            config.setSnmpv3User(index, user.toEntity());
            trapdConfigDao.updateConfig(config);
            return Response.ok().build();
        } catch (ValidationException e) {
            LOG.warn("Updating SNMPv3 user failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Failed to update SNMPv3 user for securityName {}.", securityName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to update trapd user.").build();
        }
    }

    @Override
    public Response deleteTrapdUser(String securityName, SecurityContext securityContext) {
        if (StringUtils.isBlank(securityName)) {
            return Response.status(Status.BAD_REQUEST).entity("Valid security name is required.").build();
        }

        try {
            final TrapdConfiguration config = trapdConfigDao.getMaskedConfig();
            if (config == null) {
                return Response.status(Status.NOT_FOUND).entity("Trapd configuration not found.").build();
            }

            final int index = findUserIndexBySecurityName(config, securityName);
            if (index < 0) {
                return Response.status(Status.NOT_FOUND)
                        .entity("SNMPv3 user with securityName '" + securityName + "' was not found.")
                        .build();
            }

            config.removeSnmpv3UserAt(index);
            trapdConfigDao.updateConfig(config);
            return Response.noContent().build();
        } catch (ValidationException e) {
            LOG.warn("Removing SNMPv3 user failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Failed to delete SNMPv3 user for securityName {}.", securityName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to delete trapd user.").build();
        }
    }

    private String validateTrapdConfigDtoFields(TrapdConfigDto configDto) {
        if (StringUtils.isBlank(configDto.getSnmpTrapAddress())) {
            return "snmpTrapAddress is required.";
        }
        if (configDto.getSnmpTrapPort() == null || configDto.getSnmpTrapPort() < 1 || configDto.getSnmpTrapPort() > 65535) {
            return "snmpTrapPort is required and must be between 1 and 65535.";
        }
        if (configDto.getThreads() != null && configDto.getThreads() < 0) {
            return "threads must be non-negative.";
        }
        if (configDto.getQueueSize() != null && configDto.getQueueSize() < 0) {
            return "queueSize must be non-negative.";
        }
        if (configDto.getBatchSize() != null && configDto.getBatchSize() < 0) {
            return "batchSize must be non-negative.";
        }
        if (configDto.getBatchInterval() != null && configDto.getBatchInterval() < 0) {
            return "batchInterval must be non-negative.";
        }
        return null;
    }

    private int findUserIndexBySecurityName(final TrapdConfiguration config, final String securityName) {
        for (int i = 0; i < config.getSnmpv3UserCount(); i++) {
            final Snmpv3User existing = config.getSnmpv3User(i);
            if (StringUtils.equals(existing.getSecurityName(), securityName)) {
                return i;
            }
        }
        return -1;
    }

    private String validateTrapdConfigurationPayload(final TrapdConfiguration config) {
        if (!config.hasSnmpTrapPort()) {
            return "snmpTrapPort is required.";
        }
        if (!config.hasNewSuspectOnTrap()) {
            return "newSuspectOnTrap is required.";
        }
        return null;
    }


    private String validateSnmpv3UserPayload(final Snmpv3UserDto user) {
        if (StringUtils.isBlank(user.getSecurityName())) {
            return "securityName is required.";
        }

        final Integer securityLevel = user.getSecurityLevel();
        if (securityLevel == null) {
            return "securityLevel is required.";
        }
        if (securityLevel < 1 || securityLevel > 3) {
            return "securityLevel must be between 1 and 3.";
        }

        if (!StringUtils.isBlank(user.getAuthProtocol()) && !AUTH_PROTOCOLS.contains(user.getAuthProtocol())) {
            return "Unsupported authProtocol.";
        }
        if (!StringUtils.isBlank(user.getPrivacyProtocol()) && !PRIVACY_PROTOCOLS.contains(user.getPrivacyProtocol())) {
            return "Unsupported privacyProtocol.";
        }

        final boolean hasAuthProtocol = !StringUtils.isBlank(user.getAuthProtocol());
        final boolean hasAuthPassphrase = !StringUtils.isBlank(user.getAuthPassphrase());
        final boolean hasPrivacyProtocol = !StringUtils.isBlank(user.getPrivacyProtocol());
        final boolean hasPrivacyPassphrase = !StringUtils.isBlank(user.getPrivacyPassphrase());

        if (hasAuthProtocol != hasAuthPassphrase) {
            return "authProtocol and authPassphrase must be provided together.";
        }
        if (hasPrivacyProtocol != hasPrivacyPassphrase) {
            return "privacyProtocol and privacyPassphrase must be provided together.";
        }

        if (securityLevel == 1 && (hasAuthProtocol || hasPrivacyProtocol)) {
            return "securityLevel 1 does not allow auth or privacy credentials.";
        }
        if (securityLevel == 2 && (!hasAuthProtocol || hasPrivacyProtocol)) {
            return "securityLevel 2 requires auth credentials and does not allow privacy credentials.";
        }
        if (securityLevel == 3 && (!hasAuthProtocol || !hasPrivacyProtocol)) {
            return "securityLevel 3 requires both auth and privacy credentials.";
        }

        return null;
    }

}
