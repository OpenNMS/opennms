/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import java.net.InetAddress;

import javax.annotation.PreDestroy;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpConfigAccessService;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.web.svclayer.model.SnmpInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * <p>
 * REST service to the OpenNMS SNMP configuration <code>snmp-config.xml</code>
 * </p>
 * <p>
 * This current implementation setting and getting all parameters which are in
 * snmp-config.xml<br/>
 * <br/>
 * <b>Be aware</b> that setting the SNMP configuration for a rage of IPs is
 * currently not supported by this REST service!
 * </p>
 * 
 * <p>
 * The implementation only supports a PUT request because it is an implied
 * "Update" of the configuration since it requires an IP address and all IPs
 * have a default configuration. This request is is passed to the factory for
 * optimization of the configuration store:<code>snmp-config.xml</code>.
 * </p>
 * <p>
 * Example 1: Change SNMP configuration. 
 * </p>
 * 
 * <pre>
 * curl -v -X PUT -H "Content-Type: application/xml" \
 *      -H "Accept: application/xml" \
 *      -d "&lt;snmp-info&gt;
 *          &lt;community&gt;yRuSonoZ&lt;/community&gt;
 *          &lt;port&gt;161&lt;/port&gt;
 *          &lt;retries&gt;1&lt;/retries&gt;
 *          &lt;timeout&gt;2000&lt;/timeout&gt;
 *          &lt;version&gt;v2c&lt;/version&gt;
 *          &lt;/snmp-info&gt;" \
 *      -u admin:admin http://localhost:8980/opennms/rest/snmpConfig/10.1.1.1
 * </pre>
 * <p>
 * Example 2: Query SNMP community string.
 * </p>
 * 
 * <pre>
 * curl -v -X GET -u admin:admin http://localhost:8980/opennms/rest/snmpConfig/10.1.1.1
 * </pre>
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component
@PerRequest
@Scope("prototype")
@Path("snmpConfig")
@Transactional
public class SnmpConfigRestService extends OnmsRestService {
    @Context 
    UriInfo m_uriInfo;

    @Autowired
    private SnmpConfigAccessService m_accessService;

    @PreDestroy
    protected void tearDown() {
        if (m_accessService != null) {
            m_accessService.flushAll();
        }
    }

    /**
     * <p>getSnmpInfo</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.snmpinfo.SnmpInfo} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{ipAddr}")
    public SnmpInfo getSnmpInfo(@PathParam("ipAddr") String ipAddr) {
        readLock();
        try {
            final InetAddress addr = InetAddressUtils.addr(ipAddr);
            if (addr == null) {
                throw new WebApplicationException(Response.serverError().build());
            }
            final SnmpAgentConfig config = m_accessService.getAgentConfig(addr);
            return new SnmpInfo(config);
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>setSnmpInfo</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @param snmpInfo a {@link org.opennms.web.snmpinfo.SnmpInfo} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{ipAddr}")
    public Response setSnmpInfo(@PathParam("ipAddr") final String ipAddress, final SnmpInfo snmpInfo) {
        writeLock();
        try {
            final SnmpEventInfo eventInfo;
            if (ipAddress.contains("-")) {
                final String[] addrs = SnmpConfigRestService.getAddresses(ipAddress);
                eventInfo = snmpInfo.createEventInfo(addrs[0], addrs[1]);
            } else {
                eventInfo = snmpInfo.createEventInfo(ipAddress);
            }

            m_accessService.define(eventInfo);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } catch (final Throwable e) {
            return Response.serverError().build();
        } finally {
            writeUnlock();
        }
    }
   
    /**
     * Updates a specific interface
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{ipAddr}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateInterface(@PathParam("ipAddr") final String ipAddress, final MultivaluedMapImpl params) {
        writeLock();
        try {
            final SnmpInfo info = new SnmpInfo();
            setProperties(params, info);
            final SnmpEventInfo eventInfo;
            if (ipAddress.contains("-")) {
                final String[] addrs = SnmpConfigRestService.getAddresses(ipAddress);
                eventInfo = info.createEventInfo(addrs[0], addrs[1]);
            } else {
                eventInfo = info.createEventInfo(ipAddress);
            }
            m_accessService.define(eventInfo);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } catch (final Throwable e) {
            return Response.serverError().build();
        } finally {
            writeUnlock();
        }
    }

    protected static String[] getAddresses(final String input) {
        if (input == null || input.trim().isEmpty()) {
            return new String[] { null, null };
        } else {
            return input.trim().split("-", 2);
        }
    }
}
