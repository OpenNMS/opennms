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

package org.opennms.web.rest.v1.config;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("collectionConfigurationResource")
public class CollectionConfigurationResource {
    private static final Logger LOG = LoggerFactory.getLogger(CollectionConfigurationResource.class);

    @Autowired
    private MonitoringLocationDao m_monitoringLocationDao;

    @Resource(name="collectd-configuration.xml")
    private ConfigurationResource<CollectdConfiguration> m_collectdConfigResource;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getCollectdConfigurationForLocation(@PathParam("location") final String location) throws ConfigurationResourceException {

        final OnmsMonitoringLocation def = m_monitoringLocationDao.get(location);
        if (def == null) {
            LOG.warn("Unable to find monitoring location {}", location);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final List<String> collectionPackageNames = def.getCollectionPackageNames();
        if (collectionPackageNames != null && collectionPackageNames.size() > 0) {
            final CollectdConfiguration collectdConfig = m_collectdConfigResource.get().getCollectdConfigurationForPackages(collectionPackageNames);
            return Response.ok(collectdConfig).build();
        }

        LOG.warn("Monitoring location {} does not have a collection package defined.", location);
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
