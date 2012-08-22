package org.opennms.features.topology.plugins.topo.simple.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.opennms.features.topology.api.TopologyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

public class SimpleTopologyFactory implements ManagedServiceFactory {
		
	private static final String TOPOLOGY_LOCATION = "topologyLocation";
	private static final String LABEL = "label";

	private BundleContext m_bundleContext;
	private Map<String, SimpleTopologyProvider> m_providers = new HashMap<String, SimpleTopologyProvider>();
	private Map<String, ServiceRegistration> m_registrations = new HashMap<String, ServiceRegistration>();

	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
	}

	@Override
	public String getName() {
		return "This Factory creates Simple Topology Providers";
	}

	@Override
	public void updated(String pid, Dictionary properties) throws ConfigurationException {
		
		if (!m_providers.containsKey(pid)) {
			SimpleTopologyProvider topoProvider = new SimpleTopologyProvider();
			topoProvider.setTopologyLocation((String)properties.get(TOPOLOGY_LOCATION));
			
			m_providers.put(pid, topoProvider);
			
			Properties metaData = new Properties();
			metaData.put(Constants.SERVICE_PID, pid);
			
			if (properties.get(LABEL) != null) {
				metaData.put(LABEL, properties.get(LABEL));
			}
			
			ServiceRegistration registration = m_bundleContext.registerService(new String[] { TopologyProvider.class.getName(), EditableTopologyProvider.class.getName() },
												topoProvider, metaData);
			
			m_registrations.put(pid, registration);
			
		} else {
			m_providers.get(pid).setTopologyLocation((String)properties.get(TOPOLOGY_LOCATION));
			
			ServiceRegistration registration = m_registrations.get(pid);
			
			Properties metaData = new Properties();
			metaData.put(Constants.SERVICE_PID, pid);
			
			if (properties.get(LABEL) != null) {
				metaData.put(LABEL, properties.get(LABEL));
			}

			registration.setProperties(metaData);
		}

	}

	@Override
	public void deleted(String pid) {
		ServiceRegistration registration = m_registrations.remove(pid);
		if (registration != null) {
			registration.unregister();
		}
		
		m_providers.remove(pid);
			
	}

}
