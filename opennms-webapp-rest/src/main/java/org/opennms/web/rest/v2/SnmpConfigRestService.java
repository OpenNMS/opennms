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
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.google.common.base.Strings;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;
import org.opennms.web.rest.v2.api.SnmpConfigRestApi;
import org.opennms.web.rest.v2.model.SnmpConfigInfoDto;
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

    @Autowired
    private MonitoringLocationDao monitoringLocationDao;

    @Override
    public Response getSnmpConfig(SecurityContext securityContext) {
        try {
            SnmpConfig config = SnmpPeerFactory.getInstance().getSnmpConfig();
            return Response.ok(config).build();
        } catch (Exception e) {
            throw createServerException("Error retrieving SNMP config.");
        }
    }

    @Override
    public Response getConfigForIp(String ipAddress, String location, final SecurityContext securityContext) {
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
                    SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ipAddress), validLocation, false);

            return Response.ok(agentConfig).build();
        } catch (Exception e) {
            throw createServerException("Error looking up SNMP config.");
        }
    }

    @Override
    public Response addDefinition(SnmpConfigInfoDto dto, final SecurityContext securityContext) {
        try {
            SnmpEventInfo eventInfo = dto.createEventInfo(dto.getFirstIpAddress(), dto.getLastIpAddress());

            SnmpPeerFactory.getInstance().define(eventInfo);
            SnmpPeerFactory.getInstance().saveCurrent();

            Event eventToSend = eventInfo.createEvent(MODULE_NAME);

            if (eventToSend == null) {
                throw createServerException("Error creating event for definition.");
            }

            sendEvent(eventToSend);
        } catch (WebApplicationException webEx) {
            throw webEx;
        } catch (Exception e) {
            throw createServerException("Error adding SNMP definition.");
        }

        return Response.ok().build();
    }

    @Override
    public Response removeDefinition(String ipAddress, String location, final SecurityContext securityContext) {
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
            throw webEx;
        } catch (Exception e) {
            throw createServerException("Error adding SNMP definition.");
        }

        return Response.ok().build();
    }

    /**
     * Sends the given event via the EventProxy to the system. If null no event is sent.
     * @param eventToSend The Event to send. If null, no event is sent. * @return <code>true</code> if the event was sent successfully and no exception occurred, <code>false</code> if eventToSend is null.
     * @throws WebApplicationException on error.
     */
    private void sendEvent(Event eventToSend) throws WebApplicationException {
        try {
            EventProxy eventProxy = Util.createEventProxy();

            if (eventProxy == null) {
                throw createServerException("Event proxy object is null, unable to send event " + eventToSend.getUei());
            }

            eventProxy.send(eventToSend);
        } catch (Throwable e) {
            throw createServerException("Could not send event " + eventToSend.getUei());
        }
    }

    /**
     * If 'location' is null, empty or 'Default', return 'Default'
     * Otherwise check if it is a valid monitoring location
     * If so, return the location, otherwise return null for an invalid location.
     */
    private String convertToValidLocation(String location) {
        final boolean isDefaultLocation = Strings.isNullOrEmpty(location) || location.equalsIgnoreCase(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);

        if (isDefaultLocation) {
            return MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
        }

        // If a non-default location was specified, check if it is a valid monitoring location
        final List<OnmsMonitoringLocation> locationList = monitoringLocationDao.findAll();

        if (locationList.stream().noneMatch(loc -> loc.getLocationName().equals(location))) {
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
        } catch (Exception ignored) {
        }

        return addr;
    }

    /** Get a sanitized, trimmed string. */
    private static String safeGetTrimmedString(String s) {
        if (s == null) {
            return null;
        }

        return WebSecurityUtils.sanitizeString(s).trim();
    }
}
