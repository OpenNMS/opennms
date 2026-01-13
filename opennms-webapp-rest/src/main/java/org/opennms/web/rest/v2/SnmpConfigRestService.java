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

import java.net.InetAddress;
import java.net.URI;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.MonitoringLocationUtils;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.rest.v2.api.SnmpConfigRestApi;
import org.opennms.web.rest.v2.model.SnmpConfigInfoDto;
import org.opennms.web.rest.v2.model.SnmpConfigProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Web Service using REST for retrieving and saving SNMP Configuration information.
 */
@Component
public class SnmpConfigRestService implements SnmpConfigRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpConfigRestService.class);
    private static final String MODULE_NAME = "web rest api";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MonitoringLocationDao monitoringLocationDao;

    @Autowired
    private EventProxy eventProxy;

    @Override
    public Response getSnmpConfig() {
        try {
            SnmpConfig config = SnmpPeerFactory.getInstance().getSnmpConfig();
            String json = "";

            try {
                // We use ObjectMapper here so that fields are in JSON-friendly camelCase instead of the kebab-case
                // that Response.ok(config) would produce due to XmlAttributes in SnmpConfig.
                json = objectMapper.writeValueAsString(config);
            } catch (JsonProcessingException e) {
                LOG.error("Error serializing SnmpConfig JSON: {}", e.getMessage(), e);
                throw createServerException("Error retrieving SNMP config.");
            }

            return Response.ok(json, MediaType.APPLICATION_JSON).build();
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
    public Response addDefinition(SnmpConfigInfoDto dto) {
        try {
            if (dto == null) {
                return createBadRequestResponse("Missing or invalid request parameters.");
            }

            if (safeGetInetAddress(dto.getFirstIpAddress()) == null) {
                return createBadRequestResponse("Missing or invalid 'firstIpAddress'.");
            }

            // If lastIpAddress was supplied, make sure it is a valid IP address
            if (!Strings.isNullOrEmpty(dto.getLastIpAddress()) &&
                    safeGetInetAddress(dto.getLastIpAddress()) == null) {
                return createBadRequestResponse("Invalid 'lastIpAddress'.");
            }

            final String convertedLocation = convertToValidLocation(dto.getLocation());

            if (convertedLocation == null) {
                return createBadRequestResponse("Missing or invalid 'location'.");
            } else {
                dto.setLocation(convertedLocation);
            }

            SnmpEventInfo eventInfo = dto.createEventInfo(dto.getFirstIpAddress(), dto.getLastIpAddress());

            SnmpPeerFactory.getInstance().define(eventInfo);
            SnmpPeerFactory.getInstance().saveCurrent();

            Event eventToSend = eventInfo.createEvent(MODULE_NAME);

            if (eventToSend == null) {
                final String errorMessage = "Error creating event for definition.";
                LOG.error(errorMessage);
                throw createServerException(errorMessage);
            }

            sendEvent(eventToSend);
        } catch (WebApplicationException webEx) {
            LOG.error("Error sending event while adding a definition: {}", webEx.getMessage(), webEx);
            throw webEx;
        } catch (Exception e) {
            LOG.error("Error adding SNMP definition: {}", e.getMessage(), e);
            throw createServerException("Error adding SNMP definition.");
        }

        URI uri = URI.create("/snmp-config/lookup/" + dto.getFirstIpAddress());

        return Response.created(uri).build();
    }

    @Override
    public Response removeDefinition(final String ipAddress, final String location) {
        try {
            InetAddress addr = safeGetInetAddress(ipAddress);

            if (addr == null) {
                return createBadRequestResponse("Missing or invalid 'ipAddress'.");
            }

            final String validLocation = convertToValidLocation(location);

            if (validLocation == null) {
                return createBadRequestResponse("Missing or invalid 'location'.");
            }

            // removes and also saves
            SnmpPeerFactory.getInstance().removeFromDefinition(addr, validLocation, MODULE_NAME);
        } catch (WebApplicationException webEx) {
            LOG.error("Error removing an SNMP config definition: {}", webEx.getMessage(), webEx);
            throw webEx;
        } catch (Exception e) {
            LOG.error("Error removing an SNMP config definition: {}", e.getMessage(), e);
            throw createServerException("Error removing SNMP definition.");
        }

        return Response.noContent().build();
    }

    @Override
    public Response saveProfile(SnmpConfigProfileDto dto) {
        if (Strings.isNullOrEmpty(dto.getLabel())) {
            return createBadRequestResponse("Missing or invalid 'label'.");
        }

        // incoming DTO should have any fields filled out that differ from defaults
        final SnmpProfile updatedProfile = SnmpConfigProfileDto.toSnmpProfile(dto);

        // Save to config
        SnmpPeerFactory.getInstance().saveProfile(updatedProfile);

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

    /**
     * Sends the given event via the EventProxy to the system. If null no event is sent.
     * @param eventToSend The Event to send. If null, no event is sent. * @return <code>true</code> if the event was sent successfully and no exception occurred, <code>false</code> if eventToSend is null.
     * @throws WebApplicationException on error.
     */
    private void sendEvent(Event eventToSend) throws WebApplicationException {
        try {
            eventProxy.send(eventToSend);
        } catch (Exception e) {
            LOG.error("Error sending event: {}", e.getMessage(), e);
            throw createServerException("Could not send event " + eventToSend.getUei());
        }
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
}
