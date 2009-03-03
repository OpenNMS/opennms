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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.SnmpConfigDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
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
            m_snmpPeerFactory.saveCurrent();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
        
    }
    
}
