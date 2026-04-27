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
import java.nio.charset.StandardCharsets;
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
    private static final int MIN_PASSPHRASE_BYTES = 8;

    @Autowired
    private TrapdConfigDao trapdConfigDao;

    @Override
    public Response uploadTrapdConfiguration(final Attachment attachment, final SecurityContext securityContext) {
        if (attachment == null) {
            return Response.status(Status.BAD_REQUEST).entity("Missing uploaded file for trapd file upload.").build();
        }

        final TrapdConfiguration config;
        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            config = JaxbUtils.unmarshal(TrapdConfiguration.class, inputStream);
        } catch (Exception e) {
            LOG.warn("Failed to parse uploaded trapd configuration.", e);
            return Response.status(Status.BAD_REQUEST).entity("Invalid trapd XML configuration.").build();
        }

        String validationMessage = validateTrapdConfigRequest(TrapdConfigDto.toDto(config));
        if (validationMessage != null) {
            return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
        }

        try {
            trapdConfigDao.replaceConfig(config);
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
            TrapdConfiguration config = trapdConfigDao.getConfig();
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

        String validationMessage = validateTrapdConfigRequest(configDto);
        if (validationMessage != null) {
            return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
        }

        TrapdConfiguration payload = configDto.toEntity();
        try {
            trapdConfigDao.replaceConfig(payload);
            return Response.ok().build();
        } catch (ValidationException e) {
            LOG.warn("Provided trapd configuration failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity("Provided trapd configuration failed schema validation.").build();
        } catch (Exception e) {
            LOG.error("Failed to persist provided trapd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist trapd configuration.").build();
        }
    }

    /**
     * Validates the trapd configuration request DTO.
     *
     * <p>Only {@code snmpTrapPort} is required. All other numeric fields
     * ({@code threads}, {@code queueSize}, {@code batchSize}, {@code batchInterval})
     * are optional — {@link org.opennms.netmgt.config.trapd.TrapdConfiguration} provides
     * safe defaults (0, 10000, 1000, 500 respectively) when they are omitted.
     * Range checks are only applied when the client explicitly supplies a value.</p>
     *
     * @return an error message string, or {@code null} if the request is valid.
     */
    private String validateTrapdConfigRequest(final TrapdConfigDto configDto) {
        // snmpTrapPort is a required field (no default in TrapdConfiguration).
        if (configDto.getSnmpTrapPort() == null || configDto.getSnmpTrapPort() < 1 || configDto.getSnmpTrapPort() > 65535) {
            return "snmpTrapPort is required and must be between 1 and 65535.";
        }

        // newSuspectOnTrap is required by the XSD. Because this endpoint replaces the whole
        // config, accepting a null here would silently disable new-suspect generation on
        // configs that previously had it enabled.
        if (configDto.getNewSuspectOnTrap() == null) {
            return "newSuspectOnTrap is required.";
        }

        // Optional fields: only validate range when the client explicitly provides a value.
        // TrapdConfiguration defaults: threads=0 (auto), queueSize=10000, batchSize=1000, batchInterval=500.
        if (configDto.getThreads() != null && configDto.getThreads() < 0) {
            return "threads must be non-negative.";
        }
        if (configDto.getQueueSize() != null && configDto.getQueueSize() < 1) {
            return "queueSize must be greater than 0.";
        }
        if (configDto.getBatchSize() != null && configDto.getBatchSize() < 1) {
            return "batchSize must be greater than 0.";
        }
        if (configDto.getBatchInterval() != null && configDto.getBatchInterval() < 0) {
            return "batchInterval must be non-negative.";
        }

        if (configDto.getSnmpv3User() != null) {
            int index = 0;
            for (Snmpv3UserDto user : configDto.getSnmpv3User()) {
                String userValidation = validateSnmpv3UserPayload(user);
                if (userValidation != null) {
                    return "Invalid SNMPv3 user at index " + index + ": " + userValidation;
                }
                index++;
            }
        }
        return null;
    }

    /**
     * Validates a single SNMPv3 user payload.
     *
     * <p>{@code securityName} is the only required field. All other fields —
     * including {@code securityLevel} — are optional per the XSD
     * ({@code use="optional"}, no default). When {@code securityLevel} is
     * supplied its value must be 1 (noAuthNoPriv), 2 (authNoPriv), or 3 (authPriv),
     * and it must be consistent with the provided auth/privacy credentials.
     * Cross-field pairing checks (e.g. authProtocol ↔ authPassphrase) are always
     * applied regardless of whether securityLevel is present.</p>
     *
     * @return an error message string, or {@code null} if the user is valid.
     */
    private String validateSnmpv3UserPayload(final Snmpv3UserDto user) {
        if (user == null) {
            return "entry must not be null.";
        }

        if (StringUtils.isBlank(user.getSecurityName())) {
            return "securityName is required.";
        }

        // securityLevel is optional; only validate range when explicitly provided.
        final Integer securityLevel = user.getSecurityLevel();
        if (securityLevel != null && (securityLevel < 1 || securityLevel > 3)) {
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

        // SNMP4J rejects short passphrases at UsmUser construction; catch it here so the trap
        // daemon doesn't fail to restart on reload. A well-formed ${scv:...} placeholder
        // trivially passes the length check; the resolved secret must also be long enough.
        if (hasAuthPassphrase && user.getAuthPassphrase().getBytes(StandardCharsets.UTF_8).length < MIN_PASSPHRASE_BYTES) {
            return "authPassphrase must be at least " + MIN_PASSPHRASE_BYTES + " bytes.";
        }
        if (hasPrivacyPassphrase && user.getPrivacyPassphrase().getBytes(StandardCharsets.UTF_8).length < MIN_PASSPHRASE_BYTES) {
            return "privacyPassphrase must be at least " + MIN_PASSPHRASE_BYTES + " bytes.";
        }

        if (securityLevel != null && securityLevel == 1 && (hasAuthProtocol || hasPrivacyProtocol)) {
            return "securityLevel 1 does not allow auth or privacy credentials.";
        }
        if (securityLevel != null && securityLevel == 2 && (!hasAuthProtocol || hasPrivacyProtocol)) {
            return "securityLevel 2 requires auth credentials and does not allow privacy credentials.";
        }
        if (securityLevel != null && securityLevel == 3 && (!hasAuthProtocol || !hasPrivacyProtocol)) {
            return "securityLevel 3 requires both auth and privacy credentials.";
        }

        return null;
    }

}
