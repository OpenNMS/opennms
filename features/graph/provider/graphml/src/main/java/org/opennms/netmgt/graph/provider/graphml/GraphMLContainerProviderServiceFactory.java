/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.graphml;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphMLContainerProviderServiceFactory implements ManagedServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GraphMLContainerProviderServiceFactory.class);
    private static final String GRAPH_LOCATION = "graphLocation";

    private final BundleContext bundleContext;
    private final Map<String, List<ServiceRegistration<?>>> containerRegistrations = Maps.newHashMap();

    public GraphMLContainerProviderServiceFactory(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public String getName() {
        return "This Factory creates GraphML Container Providers";
    }

    @Override
    public void updated(String pid, @SuppressWarnings("rawtypes") Dictionary properties) {
        LOG.debug("updated(String, Dictionary) invoked");
        if (!containerRegistrations.containsKey(pid)) {
            LOG.debug("Service with pid '{}' is new. Register {}", pid, GraphmlGraphContainerProvider.class.getSimpleName());
            final Dictionary<String,Object> metaData = new Hashtable<>();
            metaData.put(Constants.SERVICE_PID, pid);

            // Expose the Container Provider
            final String location = (String)properties.get(GRAPH_LOCATION);
            try {
                final GraphmlGraphContainerProvider graphmlGraphContainerProvider = new GraphmlGraphContainerProvider(location);
                registerService(pid, GraphContainerProvider.class, graphmlGraphContainerProvider, metaData);
            } catch (InvalidGraphException | IOException e) {
                LOG.error("An error occurred while loading GraphMLContainerProvider from file {}. Ignoring...", location, e);
            }
        } else {
            LOG.warn("Service with pid '{}' updated. Updating is not supported. Ignoring...");
        }
    }

    @Override
    public void deleted(String pid) {
        LOG.debug("deleted(String) invoked");
        List<ServiceRegistration<?>> serviceRegistrations = containerRegistrations.get(pid);
        if (serviceRegistrations != null) {
            LOG.debug("Unregister services for pid '{}'", pid);
            serviceRegistrations.forEach(ServiceRegistration::unregister);
            containerRegistrations.remove(pid);
        }
    }

    private <T> void registerService(String pid, Class<T> serviceType, T serviceImpl, Dictionary<String, Object> serviceProperties) {
        final ServiceRegistration<T> serviceRegistration = bundleContext.registerService(serviceType, serviceImpl, serviceProperties);
        containerRegistrations.putIfAbsent(pid, Lists.newArrayList());
        containerRegistrations.get(pid).add(serviceRegistration);
    }
}


