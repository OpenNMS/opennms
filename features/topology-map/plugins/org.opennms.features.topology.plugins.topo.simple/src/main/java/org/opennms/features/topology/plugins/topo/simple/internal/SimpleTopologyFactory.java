/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.simple.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.plugins.topo.simple.SimpleGraphProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

public class SimpleTopologyFactory implements ManagedServiceFactory {
		
	private static final String TOPOLOGY_LOCATION = "topologyLocation";
	private static final String LABEL = "label";

	private BundleContext m_bundleContext;
	private Map<String, SimpleGraphProvider> m_providers = new HashMap<String, SimpleGraphProvider>();
	private Map<String, ServiceRegistration<GraphProvider>> m_registrations = new HashMap<String, ServiceRegistration<GraphProvider>>();

	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}

	@Override
	public String getName() {
		return "This Factory creates Simple Topology Providers";
	}

	@Override
	public void updated(String pid, @SuppressWarnings("unchecked") Dictionary properties) throws ConfigurationException {
		
		try {
			String location = (String)properties.get(TOPOLOGY_LOCATION);
			URI url = new URI(location);
			if (!m_providers.containsKey(pid)) {
				SimpleGraphProvider topoProvider = new SimpleGraphProvider();
				topoProvider.setTopologyLocation(url);

				m_providers.put(pid, topoProvider);

				Dictionary<String,Object> metaData = new Hashtable<String,Object>();
				metaData.put(Constants.SERVICE_PID, pid);

				if (properties.get(LABEL) != null) {
					metaData.put(LABEL, properties.get(LABEL));
				}

				ServiceRegistration<GraphProvider> registration = m_bundleContext.registerService(GraphProvider.class, topoProvider, metaData);

				m_registrations.put(pid, registration);

			} else {
				m_providers.get(pid).setTopologyLocation(url);

				ServiceRegistration<GraphProvider> registration = m_registrations.get(pid);

				Dictionary<String,Object> metaData = new Hashtable<String,Object>();
				metaData.put(Constants.SERVICE_PID, pid);

				if (properties.get(LABEL) != null) {
					metaData.put(LABEL, properties.get(LABEL));
				}

				registration.setProperties(metaData);
			}

		} catch (URISyntaxException e) {
			throw new ConfigurationException(TOPOLOGY_LOCATION, "Topology location must be a valid URI", e);
		} catch (MalformedURLException e) {
			throw new ConfigurationException(TOPOLOGY_LOCATION, "Topology location must be a valid URL", e);
		} catch (JAXBException e) {
			throw new ConfigurationException(TOPOLOGY_LOCATION, "Topology location could not be deserialized", e);
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

}
