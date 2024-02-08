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


