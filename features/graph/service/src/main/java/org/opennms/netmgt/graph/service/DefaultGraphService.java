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
