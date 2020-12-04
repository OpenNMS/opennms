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

package org.opennms.netmgt.graph.service;

import static org.opennms.netmgt.graph.service.GraphProviderManager.getActualProperties;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.api.service.osgi.GraphContainerProviderRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class DefaultGraphService implements GraphService {

    private List<GraphContainerProvider> graphContainerProviders = new CopyOnWriteArrayList<>();
    private final BundleContext bundleContext;
    private final Map<GraphContainerProvider, ServiceRegistration<GraphContainerProviderRegistration>> serviceRegistrationMap = new ConcurrentHashMap<>();

    public DefaultGraphService() {
        this(null);
    }

    public DefaultGraphService(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public List<GraphContainerInfo> getGraphContainerInfos() {
        return graphContainerProviders.stream().map(cp -> cp.getContainerInfo()).collect(Collectors.toList());
    }

    @Override
    public GraphContainerInfo getGraphContainerInfo(String containerId) {
        return getGraphContainerInfos().stream().filter(ci -> ci.getId().equals(containerId)).findAny().orElse(null);
    }

    @Override
    public GraphContainerInfo getGraphContainerInfoByNamespace(String namespace) {
        final Optional<GraphContainerInfo> any = getGraphContainerInfos().stream().filter(ci -> ci.getNamespaces().contains(namespace)).findAny();
        return any.orElse(null);
    }

    @Override
    public GenericGraphContainer getGraphContainer(String containerId) {
        final Optional<GraphContainerProvider> any = graphContainerProviders.stream().filter(cp -> cp.getContainerInfo().getId().equals(containerId)).findAny();
        if (any.isPresent()) {
            return any.get().loadGraphContainer().asGenericGraphContainer();
        }
        return null;
    }

    @Override
    public GraphInfo getGraphInfo(String graphNamespace) {
        final GraphContainerInfo graphContainerInfo = getGraphContainerInfoByNamespace(graphNamespace);
        if (graphContainerInfo != null) {
            return graphContainerInfo.getGraphInfo(graphNamespace);
        }
        return null;
    }

    @Override
    public GenericGraph getGraph(String containerId, String graphNamespace) {
        final ImmutableGraphContainer graphContainer = getGraphContainer(containerId);
        if (graphContainer != null) {
            final ImmutableGraph graph = graphContainer.getGraph(graphNamespace);
            if (graph != null) {
                return graph.asGenericGraph();
            }
            return null;
        }
        return null;
    }

    public void onBind(GraphContainerProvider graphContainerProvider, Map<String, String> props) {
        // Ensure id and namespace is unique
        final GraphContainerInfo containerInfo = graphContainerProvider.getContainerInfo();
        if (getGraphContainerInfo(containerInfo.getId()) != null) {
            throw new IllegalArgumentException("A GraphContainerProvider with id '" + containerInfo.getId() + "' already exists. Ignoring container");
        }
        for (String eachNamespace : containerInfo.getNamespaces()) {
            if (getGraphInfo(eachNamespace) != null) {
                throw new IllegalArgumentException("A Graph with namespace '" + eachNamespace + "' already exists. Ignoring container.");
            }
        }
        graphContainerProviders.add(graphContainerProvider);

        // Allow other services to listen for GraphContainerProvider.
        // That way it is ensured that the service is already known by the GraphProvider
        if (bundleContext != null) {
            final ServiceRegistration<GraphContainerProviderRegistration> serviceRegistration = bundleContext.registerService(GraphContainerProviderRegistration.class, () -> graphContainerProvider, new Hashtable<>(getActualProperties(props)));
            serviceRegistrationMap.put(graphContainerProvider, serviceRegistration);
        }
    }

    public void onUnbind(GraphContainerProvider graphContainerProvider, Map<String, String> props) {
        if(graphContainerProvider == null) {
            return;
        }
        graphContainerProviders.remove(graphContainerProvider);
        final ServiceRegistration<GraphContainerProviderRegistration> serviceRegistration = serviceRegistrationMap.remove(graphContainerProvider);
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }
}
