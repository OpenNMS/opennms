package org.opennms.web.rest.config;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
public class CollectdConfigurationResource {
    private static final Logger LOG = LoggerFactory.getLogger(CollectdConfigurationResource.class);

    @Autowired
    private ConfigurationResource<CollectdConfiguration> m_collectdConfigResource;

    @Context
    private ResourceContext m_context;

    @Context 
    private UriInfo m_uriInfo;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public CollectdConfiguration getCollectdConfigurationForLocation(@PathParam("location") final String location) throws ConfigurationResourceException {
        LOG.info("getCollectConfigurationForLocation(location={})", location);
        return m_collectdConfigResource.get();
    }
}
