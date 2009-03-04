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
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
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
    
    @XmlRootElement(name="snmp-info")
    public static class SnmpInfo {

        private String m_community;
        private String m_version;
        private int m_port;
        private int m_retries;
        private int m_timeout;
        
        public SnmpInfo() {
            
        }

        /**
         * @param config
         */
        public SnmpInfo(SnmpAgentConfig config) {
            m_community = config.getReadCommunity();
            m_port = config.getPort();
            m_timeout = config.getTimeout();
            m_retries = config.getRetries();
            m_version = config.getVersionAsString();
        }

        /**
         * @return the community
         */
        public String getCommunity() {
            return m_community;
        }

        /**
         * @param community the community to set
         */
        public void setCommunity(String community) {
            m_community = community;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            return m_version;
        }

        /**
         * @param version the version to set
         */
        public void setVersion(String version) {
            m_version = version;
        }

        /**
         * @return the port
         */
        public int getPort() {
            return m_port;
        }

        /**
         * @param port the port to set
         */
        public void setPort(int port) {
            m_port = port;
        }

        /**
         * @return the retries
         */
        public int getRetries() {
            return m_retries;
        }

        /**
         * @param retries the retries to set
         */
        public void setRetries(int retries) {
            m_retries = retries;
        }

        /**
         * @return the timeout
         */
        public int getTimeout() {
            return m_timeout;
        }

        /**
         * @param timeout the timeout to set
         */
        public void setTimeout(int timeout) {
            m_timeout = timeout;
        }

        /**
         * @return
         */
        public SnmpEventInfo createEventInfo(String ipAddr) throws UnknownHostException {
            SnmpEventInfo eventInfo = new SnmpEventInfo();
            eventInfo.setCommunityString(m_community);
            eventInfo.setVersion(m_version);
            eventInfo.setPort(m_port);
            eventInfo.setTimeout(m_timeout);
            eventInfo.setRetryCount(m_retries);
            eventInfo.setFirstIPAddress(ipAddr);
            return eventInfo;
        }
        
        
    }
    
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
