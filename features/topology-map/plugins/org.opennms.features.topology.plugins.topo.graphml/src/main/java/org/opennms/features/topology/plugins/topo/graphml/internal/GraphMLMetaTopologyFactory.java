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
package org.opennms.features.topology.plugins.topo.graphml.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.ScriptEngineManager;

import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.osgi.script.OSGiScriptEngineManager;
import org.opennms.features.topology.api.IconRepository;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLMetaTopologyProvider;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLSearchProvider;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLTopologyProvider;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLDefaultVertexStatusProvider;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLEdgeStatusProvider;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLPropagateVertexStatusProvider;
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLScriptVertexStatusProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphMLMetaTopologyFactory implements ManagedServiceFactory {

	private static final Logger LOG = LoggerFactory.getLogger(GraphMLMetaTopologyFactory.class);
	private static final String TOPOLOGY_LOCATION = "topologyLocation";
	private static final String LABEL = "label";

	private final GraphMLServiceAccessor m_serviceAccessor;
	private final BundleContext m_bundleContext;
	private final Map<String, List<ServiceRegistration<?>>> m_serviceRegistration = Maps.newHashMap();

	public GraphMLMetaTopologyFactory(BundleContext bundleContext, GraphMLServiceAccessor serviceAccessor) {
		m_bundleContext = Objects.requireNonNull(bundleContext);
		m_serviceAccessor = Objects.requireNonNull(serviceAccessor);
	}

	@Override
	public String getName() {
		return "This Factory creates GraphML Topology Providers";
	}

	@Override
	public void updated(String pid, @SuppressWarnings("rawtypes") Dictionary properties) throws ConfigurationException {
		LOG.debug("updated(String, Dictionary) invoked");
		String location = (String)properties.get(TOPOLOGY_LOCATION);
		if (!m_serviceRegistration.containsKey(pid)) {
			LOG.debug("Service with pid '{}' is new. Register {}", pid, GraphMLMetaTopologyProvider.class.getSimpleName());
			final Dictionary<String,Object> metaData = new Hashtable<>();
			metaData.put(Constants.SERVICE_PID, pid);
			if (properties.get(LABEL) != null) {
				metaData.put(LABEL, properties.get(LABEL));
			}

			// Expose the MetaTopologyProvider
			try {
				final GraphMLMetaTopologyProvider metaTopologyProvider = new GraphMLMetaTopologyProvider(m_serviceAccessor);
				metaTopologyProvider.setTopologyLocation(location);
				metaTopologyProvider.reload();
				registerService(pid, MetaTopologyProvider.class, metaTopologyProvider, metaData);

				// Create and register additional services
				final Set<String> iconKeys = metaTopologyProvider.getGraphProviders().stream()
						.map(GraphProvider::getNamespace)
						.flatMap(eachNamespace -> metaTopologyProvider.getGraphProvider(eachNamespace).getCurrentGraph().getVertices().stream())
						.map(Vertex::getIconKey)
						.filter(Objects::nonNull)
						.collect(Collectors.toSet());
				registerService(pid, IconRepository.class, new GraphMLIconRepository(iconKeys));

				// Create an OSGi aware script engine manager
				final ScriptEngineManager scriptEngineManager = new OSGiScriptEngineManager(m_bundleContext);
				metaTopologyProvider.getGraphProviders().forEach(it -> {
					// Find Topology Provider
					final GraphMLTopologyProvider rawTopologyProvider = metaTopologyProvider.getGraphProvider(it.getNamespace());

					// EdgeStatusProvider
					registerService(pid, EdgeStatusProvider.class, new GraphMLEdgeStatusProvider(rawTopologyProvider, scriptEngineManager, m_serviceAccessor));

					// SearchProvider
					registerService(pid, SearchProvider.class, new GraphMLSearchProvider(rawTopologyProvider));

					// Vertex Status Provider
					// Only add status provider if explicitly set in GraphML document
					this.buildStatusProvider(metaTopologyProvider, scriptEngineManager, rawTopologyProvider)
							.ifPresent(statusProvider -> registerService(pid, StatusProvider.class, statusProvider));
				});
			} catch (InvalidGraphException | IOException e) {
				LOG.error("An error occurred while loading GraphMLTopology from file {}. Ignoring...", location, e);
			}
		} else {
			LOG.warn("Service with pid '{}' updated. Updating is not supported. Ignoring...", pid);
		}
	}

	@Override
	public void deleted(String pid) {
		LOG.debug("deleted(String) invoked");
		List<ServiceRegistration<?>> serviceRegistrations = m_serviceRegistration.get(pid);
		if (serviceRegistrations != null) {
			LOG.debug("Unregister services for pid '{}'", pid);
			serviceRegistrations.forEach(ServiceRegistration::unregister);
			m_serviceRegistration.remove(pid);
		}
	}

	private <T> void registerService(String pid, Class<T> serviceType, T serviceImpl) {
		registerService(pid, serviceType, serviceImpl, new Hashtable<>());
	}

	private <T> void registerService(String pid, Class<T> serviceType, T serviceImpl, Dictionary<String, Object> serviceProperties) {
		final ServiceRegistration<T> serviceRegistration = m_bundleContext.registerService(serviceType, serviceImpl, serviceProperties);
		m_serviceRegistration.putIfAbsent(pid, Lists.newArrayList());
		m_serviceRegistration.get(pid).add(serviceRegistration);
	}

	private Optional<StatusProvider> buildStatusProvider(final GraphMLMetaTopologyProvider metaTopologyProvider,
														 final ScriptEngineManager scriptEngineManager,
														 final GraphMLTopologyProvider rawTopologyProvider) {
		switch (rawTopologyProvider.getVertexStatusProviderType()) {
			case NO_STATUS_PROVIDER:
				return Optional.empty();

			case DEFAULT_STATUS_PROVIDER:
				return Optional.of(new GraphMLDefaultVertexStatusProvider(
						rawTopologyProvider.getNamespace(),
						(nodeIds) -> m_serviceAccessor.getAlarmDao().getNodeAlarmSummariesIncludeAcknowledgedOnes(nodeIds)));

			case SCRIPT_STATUS_PROVIDER:
				return Optional.of(new GraphMLScriptVertexStatusProvider(
						rawTopologyProvider.getNamespace(),
						(nodeIds) -> m_serviceAccessor.getAlarmDao().getNodeAlarmSummariesIncludeAcknowledgedOnes(nodeIds),
						scriptEngineManager,
						m_serviceAccessor));

			case PROPAGATE_STATUS_PROVIDER:
				return Optional.of(new GraphMLPropagateVertexStatusProvider(
						rawTopologyProvider.getNamespace(),
						metaTopologyProvider,
						m_bundleContext));

			default:
				throw new IllegalStateException("unhandled VertexStatusProviderType: " + rawTopologyProvider.getVertexStatusProviderType());
		}
	}
}

