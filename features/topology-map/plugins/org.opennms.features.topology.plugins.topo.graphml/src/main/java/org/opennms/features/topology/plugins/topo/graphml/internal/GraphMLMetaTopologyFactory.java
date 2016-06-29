/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.IconRepository;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLMetaTopologyProvider;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLSearchProvider;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLTopologyProvider;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLVertexStatusProvider;
import org.opennms.netmgt.dao.api.AlarmDao;
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

	private BundleContext m_bundleContext;
	private AlarmDao alarmDao;
	private Map<String, GraphMLMetaTopologyProvider> m_providers = Maps.newHashMap();
	private Map<String, ServiceRegistration<MetaTopologyProvider>> m_registrations =  Maps.newHashMap();
	private Map<String, List<ServiceRegistration<SearchProvider>>> m_searchProviders = Maps.newHashMap();
	private Map<String, ServiceRegistration<IconRepository>> m_iconRepositories = Maps.newHashMap();
	private Map<String, List<ServiceRegistration<StatusProvider>>> m_vertexStatusProviders = Maps.newHashMap();

	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}

	public void setAlarmDao(AlarmDao alarmDao) {
		this.alarmDao = Objects.requireNonNull(alarmDao);
	}

	@Override
	public String getName() {
		return "This Factory creates GraphML Topology Providers";
	}

	@Override
	public void updated(String pid, @SuppressWarnings("rawtypes") Dictionary properties) throws ConfigurationException {
		LOG.debug("updated(String, Dictionary) invoked");
		String location = (String)properties.get(TOPOLOGY_LOCATION);
		if (!m_providers.containsKey(pid)) {
			LOG.debug("Service with pid '{}' is new. Register {}", pid, GraphMLMetaTopologyProvider.class.getSimpleName());
			final Dictionary<String,Object> metaData = new Hashtable<>();
			metaData.put(Constants.SERVICE_PID, pid);
			if (properties.get(LABEL) != null) {
				metaData.put(LABEL, properties.get(LABEL));
			}

			// Expose the MetaTopologyProvider
			final GraphMLMetaTopologyProvider metaTopologyProvider = new GraphMLMetaTopologyProvider();
			metaTopologyProvider.setTopologyLocation(location);
			metaTopologyProvider.load();
			ServiceRegistration<MetaTopologyProvider> registration = m_bundleContext.registerService(MetaTopologyProvider.class, metaTopologyProvider, metaData);

			m_registrations.put(pid, registration);
			m_providers.put(pid, metaTopologyProvider);

			// Create and register a SearchProvider for each GraphMLTopologyProvider
			m_searchProviders.putIfAbsent(pid, Lists.newArrayList());

			Set<String> iconKeys = metaTopologyProvider.getGraphProviders().stream()
					.map(eachProvider -> eachProvider.getVertexNamespace())
					.flatMap(eachNamespace -> metaTopologyProvider.getRawTopologyProvider(eachNamespace).getVertices().stream())
					.map(eachVertex -> eachVertex.getIconKey())
					.filter(eachIconKey -> eachIconKey != null)
					.collect(Collectors.toSet());

			m_iconRepositories.put(pid, m_bundleContext.registerService(IconRepository.class, new GraphMLIconRepository(iconKeys), new Hashtable<>()));
			m_vertexStatusProviders.putIfAbsent(pid, Lists.newArrayList());

			metaTopologyProvider.getGraphProviders().forEach(it -> {
				GraphMLTopologyProvider rawTopologyProvider = metaTopologyProvider.getRawTopologyProvider(it.getVertexNamespace());

				GraphMLSearchProvider searchProvider = new GraphMLSearchProvider(rawTopologyProvider);
				ServiceRegistration<SearchProvider> searchProviderServiceRegistration = m_bundleContext.registerService(SearchProvider.class, searchProvider, new Hashtable<>());
				m_searchProviders.get(pid).add(searchProviderServiceRegistration);

				// Only add status provider if explicitly set in graphml document
				if (rawTopologyProvider.requiresStatusProvider()) {
					GraphMLVertexStatusProvider statusProvider = new GraphMLVertexStatusProvider(
							rawTopologyProvider.getVertexNamespace(),
							(nodeIds) -> alarmDao.getNodeAlarmSummariesIncludeAcknowledgedOnes(nodeIds));
					ServiceRegistration<StatusProvider> statusProviderRegistration = m_bundleContext.registerService(StatusProvider.class, statusProvider, new Hashtable<>());
					m_vertexStatusProviders.get(pid).add(statusProviderRegistration);
				}

			});
		} else {
			// TODO we set the new location, but a reload is never triggered
			LOG.debug("Service with pid '{}' updated. Updating properties.", pid);
			m_providers.get(pid).setTopologyLocation(location);
			ServiceRegistration<MetaTopologyProvider> registration = m_registrations.get(pid);
			Dictionary<String,Object> metaData = new Hashtable<>();
			metaData.put(Constants.SERVICE_PID, pid);
			if (properties.get(LABEL) != null) {
				metaData.put(LABEL, properties.get(LABEL));
			}
			registration.setProperties(metaData);
		}
	}

	@Override
	public void deleted(String pid) {
		LOG.debug("deleted(String) invoked");
		ServiceRegistration<MetaTopologyProvider> registration = m_registrations.remove(pid);
		if (registration != null) {
			LOG.debug("Unregister MetaTopologyProvider with pid '{}'", pid);
			registration.unregister();
			m_providers.remove(pid);

			m_searchProviders.get(pid).forEach(eachServiceRegistration -> eachServiceRegistration.unregister());
			m_searchProviders.remove(pid);

			m_vertexStatusProviders.get(pid).forEach(eachServiceRegistration -> eachServiceRegistration.unregister());
			m_vertexStatusProviders.remove(pid);

			m_iconRepositories.get(pid).unregister();
			m_iconRepositories.remove(pid);
		}
	}
}
