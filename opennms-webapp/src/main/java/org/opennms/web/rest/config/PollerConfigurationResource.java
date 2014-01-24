package org.opennms.web.rest.config;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.springframework.context.annotation.Scope;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@PerRequest
@Scope("prototype")
public class PollerConfigurationResource {
    @Context
    private ResourceContext m_context;
    
    @Context 
    private UriInfo m_uriInfo;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public PollerConfiguration getPollerConfigurationForLocation(@PathParam("location") final String location) {
        return null;
    }
}
