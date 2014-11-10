/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
public class PollerConfigurationResource {
    private static final Logger LOG = LoggerFactory.getLogger(PollerConfigurationResource.class);

    @Resource(name="poller-configuration.xml")
    private ConfigurationResource<PollerConfiguration> m_pollerConfigResource;
    
    @Resource(name="monitoring-locations.xml")
    private ConfigurationResource<MonitoringLocationsConfiguration> m_monitoringLocationsConfigResource;

    @Context
    private ResourceContext m_context;

    @Context 
    private UriInfo m_uriInfo;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getPollerConfigurationForLocation(@PathParam("location") final String location) throws ConfigurationResourceException {
        LOG.debug("getPollerConfigurationForLocation(location={})", location);
        final MonitoringLocationsConfiguration monitoringConfig = m_monitoringLocationsConfigResource.get();

        LOG.debug("monitoring config: {}", monitoringConfig);
        final LocationDef def = monitoringConfig.getLocation(location);
        if (def == null) {
            LOG.warn("Unable to find monitoring location {}", location);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final String pollingPackageName = def.getPollingPackageName();
        if (pollingPackageName == null || "".equals(pollingPackageName)) {
            LOG.warn("Monitoring location {} does not have a polling package defined.", location);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final PollerConfiguration pollerConfig = m_pollerConfigResource.get().getPollerConfigurationForPackage(pollingPackageName);
        return Response.ok(pollerConfig).build();
    }
}
