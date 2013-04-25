/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.web.snmpinfo.SnmpInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

/**
 *<p>REST service to the OpenNMS SNMP configuration <code>snmp-config.xml</code></p>
 *<p>This current implementation only supports setting and getting of the configuration elements:
 *<ul>
 *<li>community string</li>
 *<li>SNMP version</li>
 *<li>Port</li>
 *<li>Retries</li>
 *<li>Timeouts</li>
 *</ul>
 *</p>
 *<p>The implementation only supports a PUT request because it is an implied "Update" of the configuration
 *since it requires an IP address and all IPs have a default configuration.  This request is is passed to
 *the factory for optimization of the configuration store:<code>snmp-config.xml</code>.</p>
 *<p>Example 1: Change SNMP community string.  <i>Note: Community string is the only required element</i></p>
 *<pre>
 *curl -v -X PUT -H "Content-Type: application/xml" \
 *     -H "Accept: application/xml" \
 *     -d "&lt;snmp-info&gt;
 *         &lt;community&gt;yRuSonoZ&lt;/community&gt;
 *         &lt;port&gt;161&lt;/port&gt;
 *         &lt;retries&gt;1&lt;/retries&gt;
 *         &lt;timeout&gt;2000&lt;/timeout&gt;
 *         &lt;version&gt;v2c&lt;/version&gt;
 *         &lt;/snmp-info&gt;" \
 *     -u admin:admin http://localhost:8980/opennms/rest/snmpConfig/10.1.1.1
 *</pre>
 *<p>Example 2: Query SNMP community string.</p>
 *<pre>
 *curl -v -X GET -u admin:admin http://localhost:8980/opennms/rest/snmpConfig/10.1.1.1
 *</pre>
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
    private SnmpPeerFactory m_snmpPeerFactory;
    
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
    		SnmpAgentConfig config = m_snmpPeerFactory.getAgentConfig(addr);
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
            final SnmpEventInfo eventInfo = snmpInfo.createEventInfo(ipAddress);
            m_snmpPeerFactory.define(eventInfo);
            //TODO: this shouldn't be a static call
            SnmpPeerFactory.saveCurrent();
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
            final SnmpEventInfo eventInfo = info.createEventInfo(ipAddress);
            m_snmpPeerFactory.define(eventInfo);
            SnmpPeerFactory.saveCurrent();
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } catch (final Throwable e) {
            return Response.serverError().build();
        } finally {
            writeUnlock();
        }
    }

}
