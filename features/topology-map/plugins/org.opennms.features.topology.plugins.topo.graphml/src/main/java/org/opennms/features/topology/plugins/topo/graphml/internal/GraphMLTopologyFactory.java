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
import java.util.Map;

import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.DefaultMetaInfo;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaInfo;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLSearchProvider;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLTopologyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import com.google.common.collect.Maps;

public class GraphMLTopologyFactory implements ManagedServiceFactory {
		
	private static final String TOPOLOGY_LOCATION = "topologyLocation";
	private static final String LABEL = "label";

	private BundleContext m_bundleContext;
	private MetaInfo metaInfo = new DefaultMetaInfo();
	private Map<String, GraphMLTopologyProvider> m_providers = Maps.newHashMap();
	private Map<String, ServiceRegistration<GraphProvider>> m_registrations =  Maps.newHashMap();

	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}

	@Override
	public String getName() {
		return "This Factory creates GraphML Topology Providers";
	}

	@Override
	public void updated(String pid, @SuppressWarnings("rawtypes") Dictionary properties) throws ConfigurationException {
		String location = (String)properties.get(TOPOLOGY_LOCATION);
		if (!m_providers.containsKey(pid)) {
			GraphMLTopologyProvider topoProvider = new GraphMLTopologyProvider("graphml");
			topoProvider.setTopologyLocation(location);
			topoProvider.setMetaInfo(getMetaInfo());
			Dictionary<String,Object> metaData = new Hashtable<>();
			metaData.put(Constants.SERVICE_PID, pid);
			if (properties.get(LABEL) != null) {
				metaData.put(LABEL, properties.get(LABEL));
			}

			// wrap as hop provider
			VertexHopGraphProvider vertexHopGraphProvider = new VertexHopGraphProvider(topoProvider);
			ServiceRegistration<GraphProvider> registration = m_bundleContext.registerService(GraphProvider.class, vertexHopGraphProvider, metaData);
			m_registrations.put(pid, registration);
			m_providers.put(pid, topoProvider);

			// Create and register a SearchProvider for the GraphML document
			GraphMLSearchProvider searchProvider = new GraphMLSearchProvider(topoProvider);
			m_bundleContext.registerService(SearchProvider.class, searchProvider, new Hashtable<>());
		} else {
			m_providers.get(pid).setTopologyLocation(location);
			ServiceRegistration<GraphProvider> registration = m_registrations.get(pid);
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
		ServiceRegistration<GraphProvider> registration = m_registrations.remove(pid);
		if (registration != null) {
			registration.unregister();
		}
		m_providers.remove(pid);
	}

	public MetaInfo getMetaInfo() {
		return metaInfo;
	}

	public void setMetaInfo(MetaInfo metaInfo) {
		this.metaInfo = metaInfo;
	}
}
