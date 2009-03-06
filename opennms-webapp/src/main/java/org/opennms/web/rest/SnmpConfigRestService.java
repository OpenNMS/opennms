/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * March 23, 2009: We got a C+ on this now, going for the A, later
 * 
 * Created: August 23, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
import org.opennms.secret.web.snmpinfo.SnmpInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("snmpConfiguration")
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
    
}
