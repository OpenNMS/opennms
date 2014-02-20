package org.opennms.web.rest.config;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
public class CollectionConfigurationResource {
    private static final Logger LOG = LoggerFactory.getLogger(CollectionConfigurationResource.class);

    @Resource(name="collectd-configuration.xml")
    private ConfigurationResource<CollectdConfiguration> m_collectdConfigResource;

    @Resource(name="monitoring-locations.xml")
    private ConfigurationResource<MonitoringLocationsConfiguration> m_monitoringLocationsConfigResource;

    @Context
    private ResourceContext m_context;

    @Context 
    private UriInfo m_uriInfo;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getCollectdConfigurationForLocation(@PathParam("location") final String location) throws ConfigurationResourceException {
        LOG.info("getCollectConfigurationForLocation(location={})", location);
        final MonitoringLocationsConfiguration monitoringConfig = m_monitoringLocationsConfigResource.get();

        LOG.debug("monitoring config: {}", monitoringConfig);
        final LocationDef def = monitoringConfig.getLocation(location);
        if (def == null) {
            LOG.warn("Unable to find monitoring location {}", location);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final String collectionPackageName = def.getCollectionPackageName();
        if (collectionPackageName == null || "".equals(collectionPackageName)) {
            LOG.warn("Monitoring location {} does not have a collection package defined.", location);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final CollectdConfiguration collectdConfig = m_collectdConfigResource.get().getCollectdConfigurationForPackage(collectionPackageName);
        return Response.ok(collectdConfig).build();
    }
}
