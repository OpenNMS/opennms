package org.opennms.serviceregistration.strategies;

import java.util.Hashtable;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.opennms.serviceregistration.ServiceRegistrationStrategy;

public class JMDNSStrategy implements ServiceRegistrationStrategy {
	private boolean registered = false;
	private JmDNS jmdns;
	private ServiceInfo si;

	public JMDNSStrategy() throws Exception {
	}

	public void initialize(String serviceType, String serviceName, int port) throws Exception {
		this.initialize(serviceType, serviceName, port, null);
	}
	
	public void initialize(String serviceType, String serviceName, int port, Hashtable<String,String> properties) throws Exception {
		if (registered == true) {
			throw new Exception("You have already registered a service with this object!");
		}
		
		serviceType   = "_" + serviceType.toLowerCase() + "._tcp.local.";
		jmdns = JmDNS.create();
		si = ServiceInfo.create(serviceType, serviceName, port, 0, 0, properties);
	}

	public void register() throws Exception {
		if (registered == false) {
			jmdns.registerService(si);
			registered = true;
		} else {
			System.err.println("WARNING: register() called but the service has already been registered!");
		}
	}

	public void unregister() throws Exception {
		if (registered == true) {
			if (jmdns != null && si != null) {
				jmdns.unregisterService(si);
				jmdns.close();
				registered = false;
			} else {
				System.err.println("WARNING: unregister() has been called, but registration previously failed.");
			}
		} else {
			System.err.println("WARNING: unregister() called but no service has been registered.");
		}
		
	}

}
