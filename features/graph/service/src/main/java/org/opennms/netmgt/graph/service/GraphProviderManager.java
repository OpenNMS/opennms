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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link GraphProviderManager} is converting any exposed {@link GraphProvider} to a {@link GraphContainerProvider}.
 *
 * @author mvrueden
 */
public class GraphProviderManager {

    private static final String LABEL_KEY = "label";
    private static final String DESCRIPTION_KEY = "description";
    private static final String CONTAINER_ID_KEY = "containerId";

    private final BundleContext bundleContext;
    private final Map<GraphProvider, ServiceRegistration<GraphContainerProvider>> graphProviderServices = new HashMap<>();

    public GraphProviderManager(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    public void onBind(GraphProvider graphProvider, Map<String, String> properties) {
        // Determine optional defined label, description and container id.
        // Otherwise default to the ones defined in the GraphInfo.
        final GraphInfo graphInfo = graphProvider.getGraphInfo();
        final String label = properties.getOrDefault(LABEL_KEY, graphInfo.getLabel());
        final String description = properties.getOrDefault(DESCRIPTION_KEY, graphInfo.getDescription());
        final String containerId = properties.getOrDefault(CONTAINER_ID_KEY, graphInfo.getNamespace());

        // Build the container info
        final DefaultGraphContainerInfo containerInfo = new DefaultGraphContainerInfo(containerId);
        containerInfo.setDescription(description);
        containerInfo.setLabel(label);

        // Expose the ContainerProvider
        final Map<String, String> actualProperties = getActualProperties(properties); // forward service properties to container service
        final SingleGraphContainerProvider singleGraphContainerProvider = new SingleGraphContainerProvider(graphProvider, containerInfo);
        final ServiceRegistration<GraphContainerProvider> serviceRegistration = bundleContext.registerService(GraphContainerProvider.class, singleGraphContainerProvider, new Hashtable<>(actualProperties));
        graphProviderServices.put(graphProvider, serviceRegistration);
    }

    public void onUnbind(GraphProvider graphProvider, Map<String, String> properties) {
        final ServiceRegistration<GraphContainerProvider> removedService = graphProviderServices.remove(graphProvider);
        if (removedService != null) {
            removedService.unregister();
        }
    }

    public static Map<String, String> getActualProperties(Map<String, String> properties) {
        final Map<String, String> actualProperties = new HashMap<>();
        properties.keySet().stream()
                .filter(key -> !key.startsWith("service") && !key.startsWith("osgi") && !key.equals("objectClass"))
                .forEach(key -> actualProperties.put(key, properties.get(key)));
        return actualProperties;
    }
}
