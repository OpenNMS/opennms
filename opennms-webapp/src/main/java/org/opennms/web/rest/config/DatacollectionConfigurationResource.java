package org.opennms.web.rest.config;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
public class DatacollectionConfigurationResource {
    private static final Logger LOG = LoggerFactory.getLogger(DatacollectionConfigurationResource.class);

    @Resource(name="dataCollectionConfigDao")
    private DefaultDataCollectionConfigDao m_dataCollectionConfigDao;

    @Context
    private ResourceContext m_context;

    @Context 
    private UriInfo m_uriInfo;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getDatacollectionConfiguration() throws ConfigurationResourceException {
        LOG.info("getDatacollectionConfigurationForLocation()");
        
        final DatacollectionConfig dcc = m_dataCollectionConfigDao.getContainer().getObject();
        if (dcc == null) {
            return Response.status(404).build();
        }

        return Response.ok(dcc).build();
    }
}
