package org.opennms.web.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

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
    
    @Autowired
    private SnmpConfigDao m_snmpConfigDao;
    
    @Context 
    UriInfo m_uriInfo;
    
    @Context
    ResourceContext m_context;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("defaults")
    public SnmpConfiguration defaults() {
        return m_snmpConfigDao.getDefaults();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{ipAddr}")
    public SnmpAgentConfig getAgentConfig(@PathParam("ipAddr") String ipAddr) {
        try {
            return m_snmpConfigDao.get(InetAddress.getByName(ipAddr));
        } catch (UnknownHostException e) {
            return throwException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Path("defaults")
    public Response updateDefaults(SnmpConfiguration defaultConfig) {
        log().debug("updateDefaults: Updating Defalut configuraiton to  " + defaultConfig);
        m_snmpConfigDao.saveAsDefaults(defaultConfig);
        return Response.ok(defaultConfig).build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{ipAddr}")
    public Response updateAgentConfig(@PathParam("ipAddr") String ipAddr, SnmpAgentConfig agentConfig) {
        log().debug("updateAgentCofnig: updating configuration for " +ipAddr+ " to " + agentConfig);
        try {
            agentConfig.setAddress(InetAddress.getByName(ipAddr));
            m_snmpConfigDao.saveOrUpdate(agentConfig);
            return Response.ok(agentConfig).build();
        } catch (UnknownHostException e) {
            return throwException(Status.BAD_REQUEST, e.getMessage());
        }
    }
    
}
