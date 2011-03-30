package org.opennms.serviceregistration.strategies;

import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.opennms.serviceregistration.ServiceRegistrationStrategy;

public class JMDNSStrategy implements ServiceRegistrationStrategy {
	private boolean m_registered = false;
	private JmDNS m_jmdns;
	private ServiceInfo m_si;

	public JMDNSStrategy() throws Exception {
	}

	public void initialize(final String serviceType, final String serviceName, final int port) throws Exception {
		initialize(serviceType, serviceName, port, null);
	}
	
	public void initialize(final String serviceType, final String serviceName, final int port, final Map<String,String> properties) throws Exception {
		if (m_registered == true) {
			throw new Exception("You have already m_registered a service with this object!");
		}
		
		final String serviceTypeName   = "_" + serviceType.toLowerCase() + "._tcp.local.";
		m_jmdns = JmDNS.create();
		m_si = ServiceInfo.create(serviceTypeName, serviceName, port, 0, 0, properties);
	}

	public void register() throws Exception {
		if (m_registered == false) {
			m_jmdns.registerService(m_si);
			m_registered = true;
		} else {
			System.err.println("WARNING: register() called but the service has already been m_registered!");
		}
	}

	public void unregister() throws Exception {
		if (m_registered == true) {
			if (m_jmdns != null && m_si != null) {
				m_jmdns.unregisterService(m_si);
				m_jmdns.close();
				m_registered = false;
			} else {
				System.err.println("WARNING: unregister() has been called, but registration previously failed.");
			}
		} else {
			System.err.println("WARNING: unregister() called but no service has been m_registered.");
		}
		
	}

}
