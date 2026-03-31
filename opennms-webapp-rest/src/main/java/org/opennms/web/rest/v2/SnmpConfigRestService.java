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
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.Configuration;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.MonitoringLocationUtils;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.web.rest.v2.api.SnmpConfigRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/**
 * Web Service using REST for retrieving and saving SNMP Configuration information.
 */
@Component
public class SnmpConfigRestService implements SnmpConfigRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpConfigRestService.class);
    private static final String MODULE_NAME = "web rest api";

    private static final String COMMA_DELIMITER = ",";
    private static final String RANGE_DELIMITER = "-";
    private static final String IPLIKE_VALIDATION_REGEX = "(([0-9]{1,3}(([,-])[0-9]{1,3})*|\\*)\\.){3}([0-9]{1,3}(([,-])[0-9]{1,3})*|\\*)";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String DEFINITION_MISSING_CONTENTS_MESSAGE = "Definition must have at least one specific IP, IP range or IP match specified.";
    public static final String DEFINITION_CANNOT_MIX_RANGE_AND_IPMATCH_MESSAGE =
            "Cannot have an IP match expression along with IP ranges or specific IP addresses.";
    public static final String DEFINITION_NO_ITEMS_REMOVED_MESSAGE = "No configuration items removed, mostly likely no matching definitions found.";

    record DefinitionContents(boolean hasRange, boolean hasSpecific, boolean hasIpMatch) {}

    @Autowired
    private MonitoringLocationDao monitoringLocationDao;

    @Autowired
    private EventProxy eventProxy;

    @Override
    public Response getSnmpConfig() {
        try {
            SnmpConfig config = SnmpPeerFactory.getInstance().getSnmpConfig();

            return Response.ok(config, MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            LOG.error("Error retrieving SnmpConfig config: {}", e.getMessage(), e);
            throw createServerException("Error retrieving SNMP config.");
        }
    }

    @Override
    public Response getConfigForIp(final String ipAddress, final String location) {
        try {
            InetAddress addr = safeGetInetAddress(ipAddress);

            if (addr == null) {
                return createBadRequestResponse("Missing or invalid 'ipAddress'.");
            }

            final String validLocation = convertToValidLocation(location);

            if (validLocation == null) {
                return createBadRequestResponse("Missing or invalid 'location'.");
            }

            SnmpAgentConfig agentConfig =
                SnmpPeerFactory.getInstance().getAgentConfig(addr, validLocation, false);

            return Response.ok(agentConfig).build();
        } catch (Exception e) {
            LOG.error("Error looking up SNMP Config: {}", e.getMessage(), e);
            throw createServerException("Error looking up SNMP config.");
        }
    }

    @Override
    public Response saveDefaultOverrides(Configuration config) {
        if (config == null) {
            return createBadRequestResponse("Missing or invalid request body.");
        }

        try {
            // Validate the config
            // JaxbUtils.unmarshal validates via 'snmp-config.xsd', so we use this for validation
            SnmpConfig validationConfig = new SnmpConfig(config, new ArrayList<>());
            String configXml = JaxbUtils.marshal(validationConfig);
            SnmpConfig validatedConfig = JaxbUtils.unmarshal(SnmpConfig.class, configXml);

            SnmpPeerFactory.getInstance().saveDefaultOverrides(config);
        } catch (DataAccessException dae) {
            LOG.error("Data access error saving SNMP default overrides, failed schema validation: {}", dae.getMessage(), dae);
            throw createServerException("Error saving SNMP default overrides, failed schema validation.");
        } catch (Exception e) {
            LOG.error("Error saving SNMP default overrides: {}", e.getMessage(), e);
            throw createServerException("Error saving SNMP default overrides.");
        }

        return Response.noContent().build();
    }

    @Override
    public Response addDefinition(Definition definition) {
        try {
            if (definition == null) {
                return createBadRequestResponse("Missing or invalid request parameters.");
            }

            DefinitionContents contents = getDefinitionContents(definition);

            if (!contents.hasRange && !contents.hasSpecific && !contents.hasIpMatch) {
                return createBadRequestResponse(DEFINITION_MISSING_CONTENTS_MESSAGE);
            }

            if (contents.hasIpMatch && (contents.hasRange || contents.hasSpecific)) {
                return createBadRequestResponse(DEFINITION_CANNOT_MIX_RANGE_AND_IPMATCH_MESSAGE);
            }

            final String convertedLocation = convertToValidLocation(definition.getLocation());

            if (convertedLocation == null) {
                return createBadRequestResponse("Missing or invalid 'location'.");
            } else {
                definition.setLocation(convertedLocation);
            }

            // Validate IP addresses in the definition
            String ipValidationError = validateDefinitionIpAddresses(definition);
            if (ipValidationError != null) {
                return createBadRequestResponse(ipValidationError);
            }

            // Validate IP match expressions in the definition
            String ipMatchValidationError = validateDefinitionIpMatches(definition);

            if (ipMatchValidationError != null) {
                return createBadRequestResponse(ipMatchValidationError);
            }

            SnmpPeerFactory.getInstance().saveDefinition(definition, true);
        } catch (DataAccessException dae) {
            LOG.error("Data access error adding SNMP definition, failed schema validation: {}", dae.getMessage(), dae);
            throw createServerException("Error saving SNMP definition, failed schema validation.");
        } catch (Exception e) {
            LOG.error("Error adding SNMP definition: {}", e.getMessage(), e);
            throw createServerException("Error adding SNMP definition.");
        }

        // URI to view the updated definitions
        URI uri = URI.create("/snmp-config");

        return Response.created(uri).build();
    }

    @Override
    public Response removeDefinition(final String specifics, final String ranges,
                                     final String ipMatches, final String location) {
        try {
            final String validLocation = convertToValidLocation(location);

            if (validLocation == null) {
                return createBadRequestResponse("Missing or invalid 'location'.");
            }

            List<String> rangeItems = splitAndTrim(ranges, COMMA_DELIMITER);
            List<String> definitionIpMatches = splitAndTrim(ipMatches, COMMA_DELIMITER);
            List<String> definitionSpecifics = splitAndTrim(specifics, COMMA_DELIMITER);
            List<Range> definitionRanges = new ArrayList<>();

            for (String specific : definitionSpecifics) {
                if (safeGetInetAddress(specific) == null) {
                    return createBadRequestResponse("The specific IP address '" + specific + "' was invalid.");
                }
            }

            for (String range : rangeItems) {
                List<String> rangeSplit = splitAndTrim(range, RANGE_DELIMITER);

                if (rangeSplit.size() != 2) {
                    return createBadRequestResponse("Invalid range '" + range + "'.");
                }

                Range r = new Range(rangeSplit.get(0), rangeSplit.get(1));

                if (safeGetInetAddress(r.getBegin()) == null) {
                    return createBadRequestResponse("The range begin IP address '" + r.getBegin() + "' was invalid.");
                }

                if (safeGetInetAddress(r.getEnd()) == null) {
                    return createBadRequestResponse("The range end IP address '" + r.getEnd() + "' was invalid.");
                }

                definitionRanges.add(r);
            }

            if (definitionSpecifics.isEmpty() && definitionRanges.isEmpty() && definitionIpMatches.isEmpty()) {
                return createBadRequestResponse("Must supply at least one specific or range of IP addresses or IP match expression to remove.");
            }

            // removes and also saves
            boolean result = SnmpPeerFactory.getInstance()
                    .removeRangesFromDefinition(definitionRanges, definitionSpecifics, definitionIpMatches, validLocation, MODULE_NAME);

            if (!result) {
                LOG.info(DEFINITION_NO_ITEMS_REMOVED_MESSAGE);
                return createBadRequestResponse(DEFINITION_NO_ITEMS_REMOVED_MESSAGE);
            }
        } catch (DataAccessException | WebApplicationException ex) {
            LOG.error("Error removing an SNMP config definition: {}", ex.getMessage(), ex);
            throw createServerException("Error removing SNMP definition.");
        } catch (Exception e) {
            LOG.error("Unexpected error removing an SNMP config definition: {}", e.getMessage(), e);
            throw createServerException("Error removing SNMP definition.");
        }

        return Response.noContent().build();
    }

    @Override
    public Response saveProfile(SnmpProfile profile) {
        if (Strings.isNullOrEmpty(profile.getLabel())) {
            return createBadRequestResponse("Missing or invalid 'label'.");
        }

        SnmpPeerFactory.getInstance().saveProfile(profile);

        return Response.noContent().build();
    }

    @Override
    public Response removeProfile(final String label) {
        if (Strings.isNullOrEmpty(label)) {
            return createBadRequestResponse("Missing or invalid 'label'.");
        }

        boolean success = SnmpPeerFactory.getInstance().removeProfile(label);

        if (success) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Profile with label " + label + " not found.").build();
        }
    }

    @Override
    public Response downloadConfig(final String format) {
        final boolean isXml = format != null && format.equalsIgnoreCase("xml");
        final String fileName = isXml ? "snmp-config.xml" : "snmp-config.json";
        byte[] byteArray = null;

        try {
            final SnmpConfig snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();

            if (isXml) {
                String xml = JaxbUtils.marshal(snmpConfig);
                byteArray = xml.getBytes(StandardCharsets.UTF_8);
            } else {
                // Need to use old codehaus.jackson mapper so that @JsonProperty annotations are followed
                // We have to use codehaus annotations because Rest services are configured in
                // applicationContext-cxf-common.xml and applicationContext-cxf-rest-v2.xml to use
                // the codehaus.jackson JsonProvider
                // The codehaus.jackson DefaultPrettyPrinter doesn't have the best output,
                // it adds extra whitespace, but it'll do for now
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snmpConfig);
                byteArray = json.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOG.error("Error serializing SnmpConfig JSON: {}", e.getMessage(), e);
            throw createServerException("Error retrieving SNMP config.");
        }

        final String contentType = isXml ? "application/xml" : "application/json";

        return Response.ok().type(contentType + ";charset=" + StandardCharsets.UTF_8)
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .header("Pragma", "public")
                .header("Cache-Control", "no-cache, must-revalidate")
                .entity(byteArray).build();
    }

    @Override
    public Response uploadConfig(final Attachment attachment) {
        return uploadConfigInternal(attachment, false);
    }

    @Override
    public Response uploadConfigXml(final Attachment attachment) {
        return uploadConfigInternal(attachment, true);
    }

    private Response uploadConfigInternal(final Attachment attachment, final boolean isXml) {
        if (attachment == null) {
            return createBadRequestResponse("Missing configuration file.");
        }

        SnmpConfig config = null;
        String contents = "";

        try (InputStream stream = attachment.getObject(InputStream.class)) {
            contents = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("Error reading uploaded file: {}", e.getMessage(), e);
            return createBadRequestResponse("Could not read configuration file.");
        }

        try {
            if (isXml) {
                config = JaxbUtils.unmarshal(SnmpConfig.class, contents);
            } else {
                // Use old codehaus.jackson mapper so that @JsonProperty annotations are followed
                config = objectMapper.readValue(contents, SnmpConfig.class);

                // Validate the config
                // JaxbUtils.unmarshal validates via 'snmp-config.xsd', so for Json files we perform this extra step
                String configXml = JaxbUtils.marshal(config);
                config = JaxbUtils.unmarshal(SnmpConfig.class, configXml);
            }
        } catch (DataAccessException | JsonProcessingException e) {
            LOG.error("Error parsing or validating uploaded file: {}", e.getMessage(), e);
            return createBadRequestResponse("Invalid configuration file.");
        } catch (Exception e) {
            LOG.error("Error processing uploaded file: {}", e.getMessage(), e);
            throw createServerException("Unexpected error processing uploaded file.");
        }

        config.fixSecurityLevel();

        try {
            SnmpPeerFactory.getInstance().setAndSaveConfig(config);
        } catch (Exception e) {
            throw createServerException("Could not save updated config");
        }

        return Response.ok().build();
    }

    /**
     * If 'location' is null, empty or 'Default', return 'Default'
     * Otherwise check if it is a valid monitoring location
     * If so, return the location, otherwise return null for an invalid location.
     */
    private String convertToValidLocation(String location) {
        if (MonitoringLocationUtils.isDefaultLocationName(location)) {
            return MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
        }

        // If a non-default location was specified, check if it is a valid monitoring location
        if (monitoringLocationDao.get(location) == null) {
            return null;
        }

        return location;
    }

    private DefinitionContents getDefinitionContents(Definition definition) {
        boolean hasRange = definition.getRanges().stream()
                .anyMatch(range -> !Strings.isNullOrEmpty(range.getBegin()) && !Strings.isNullOrEmpty(range.getEnd()));

        boolean hasSpecific = definition.getSpecifics().stream().anyMatch(spec -> !Strings.isNullOrEmpty(spec));
        boolean hasIpMatch = definition.getIpMatches().stream().anyMatch(match -> !Strings.isNullOrEmpty(match));

        return new DefinitionContents(hasRange, hasSpecific, hasIpMatch);
    }

    private static Response createBadRequestResponse(String message) {
        return
            Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.TEXT_PLAIN)
            .entity(message).build();
    }

    private static WebApplicationException createServerException(String message) {
        return new WebApplicationException(
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.TEXT_PLAIN)
            .entity(message).build());
    }

    /** Return a valid InetAddress, or null if it could not be parsed. */
    private static InetAddress safeGetInetAddress(String ipAddress) {
        InetAddress addr = null;

        try {
            if (!Strings.isNullOrEmpty(ipAddress)) {
                addr = InetAddressUtils.addr(ipAddress);
            }
        } catch (Exception e) {
            LOG.debug("Invalid IP address: {}", ipAddress, e);
        }

        return addr;
    }

    private static List<String> splitAndTrim(final String source, final String delimiter) {
        String trimmed = !Strings.isNullOrEmpty(source) ? source.trim() : "";

        List<String> items =
            Arrays.stream(trimmed.split(delimiter))
                .map(String::trim)
                .filter(s -> !Strings.isNullOrEmpty(s))
                .toList();

        return items;
    }

    /**
     * Validates IP addresses in the Definition's specifics and ranges.
     * @return error message if validation fails, null if valid
     */
    private static String validateDefinitionIpAddresses(final Definition definition) {
        for (final String specific : definition.getSpecifics()) {
            if (Strings.isNullOrEmpty(specific)) {
                return "Invalid specific IP address: empty value.";
            }
            InetAddress addr = null;

            try {
                addr = InetAddressUtils.addr(specific);
            } catch (IllegalArgumentException ignored) {
            }

            if (addr == null) {
                return String.format("Invalid specific IP address: %s", specific);
            }
        }

        for (final Range range : definition.getRanges()) {
            if (Strings.isNullOrEmpty(range.getBegin()) || Strings.isNullOrEmpty(range.getEnd())) {
                return String.format("Invalid range: begin and end must be specified. begin=%s, end=%s",
                    range.getBegin(), range.getEnd());
            }

            InetAddress beginAddr = null;
            InetAddress endAddr = null;

            try {
                beginAddr = InetAddressUtils.addr(range.getBegin());
                endAddr = InetAddressUtils.addr(range.getEnd());
            } catch (IllegalArgumentException ignored) {
            }

            if (beginAddr == null) {
                return String.format("Invalid range begin IP address: %s", range.getBegin());
            }
            if (endAddr == null) {
                return String.format("Invalid range end IP address: %s", range.getEnd());
            }

            // Ensure both addresses are the same IP version
            // They should be either both Inet4Address or both Inet6Address, but not one of each
            boolean beginIsV4 = beginAddr instanceof java.net.Inet4Address;
            boolean endIsV4 = endAddr instanceof java.net.Inet4Address;

            if (beginIsV4 != endIsV4) {
                return String.format("Invalid range: begin and end must be same IP version. begin=%s, end=%s",
                    range.getBegin(), range.getEnd());
            }
        }

        return null;
    }

    /**
     * Validates IP match expressions in the Definition's ipMatch list.
     * @return error message if validation fails, null if valid
     */
    private static String validateDefinitionIpMatches(final Definition definition) {
        for (final String ipMatch : definition.getIpMatches()) {
            if (Strings.isNullOrEmpty(ipMatch)) {
                return "Invalid IP match expression: empty value.";
            }

            if (!ipMatch.matches(IPLIKE_VALIDATION_REGEX)) {
                return String.format("Invalid IP match expression: '%s'.", ipMatch);
            }

            // TODO: Consider more robust validation, for example checking octets are in the 0-255 range
        }

        return null;
    }
}
