/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
