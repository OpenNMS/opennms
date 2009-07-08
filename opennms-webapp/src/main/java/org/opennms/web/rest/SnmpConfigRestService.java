/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
<p>REST service to the OpenNMS SNMP configuration <code>snmp-config.xml</code></p>
<p>This current implementation only supports setting and getting of the configuration elements:
<ul>
<li>community string</li>
<li>SNMP version</li>
<li>Port</li>
<li>Retries</li>
<li>Timeouts</li>
</ul>
</p>
<p>The implementation only supports a PUT request because it is an implied "Update" of the configuration
since it requires an IP address and all IPs have a default configuration.  This request is is passed to
the factory for optimization of the configuration store:<code>snmp-config.xml</code>.</p>
<p>Example 1: Change SNMP community string.  <i>Note: Community string is the only required element</i></p>
<pre>
curl -v -X PUT -H "Content-Type: application/xml" \
     -H "Accept: application/xml" \
     -d "&lt;snmp-info&gt;
         &lt;community&gt;yRuSonoZ&lt;/community&gt;
         &lt;port&gt;161&lt;/port&gt;
         &lt;retries&gt;1&lt;/retries&gt;
         &lt;timeout&gt;2000&lt;/timeout&gt;
         &lt;version&gt;v2c&lt;/version&gt;
         &lt;/snmp-info&gt;" \
     -u admin:admin http://localhost:8980/opennms/rest/snmpConfig/10.1.1.1
</pre>
<p>Example 2: Query SNMP community string.</p>
<pre>
curl -v -X GET -u admin:admin http://localhost:8980/opennms/rest/snmpConfig/10.1.1.1
</pre>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
@Component
@PerRequest
@Scope("prototype")
@Path("snmpConfig")
@Transactional
public class SnmpConfigRestService extends OnmsRestService {
    
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{ipAddr}")
    public SnmpInfo getSnmpInfo(@PathParam("ipAddr") String ipAddr) {
        try {
            SnmpAgentConfig config = m_snmpPeerFactory.getAgentConfig(InetAddress.getByName(ipAddr));
            return new SnmpInfo(config);
        } catch (UnknownHostException e) {
            throw new WebApplicationException(Response.serverError().build());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{ipAddr}")
    public Response setSnmpInfo(@PathParam("ipAddr") String ipAddr, SnmpInfo snmpInfo) {
        try {
            SnmpEventInfo eventInfo = snmpInfo.createEventInfo(ipAddr);
            m_snmpPeerFactory.define(eventInfo);
            //TODO: this shouldn't be a static call
            SnmpPeerFactory.saveCurrent();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
        
    }
   
    /**
     * Updates a specific interface
     */
    @PUT
    @Path("{ipAddr}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateInterface(@PathParam("ipAddr") String ipAddress, MultivaluedMapImpl params) {
        try {
            SnmpInfo info = new SnmpInfo();
            setProperties(params, info);
            SnmpEventInfo eventInfo = info.createEventInfo(ipAddress);
            m_snmpPeerFactory.define(eventInfo);
            SnmpPeerFactory.saveCurrent();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }


}
