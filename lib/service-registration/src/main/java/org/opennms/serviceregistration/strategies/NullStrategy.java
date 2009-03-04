package org.opennms.serviceregistration.strategies;

import java.util.Hashtable;

import org.opennms.serviceregistration.ServiceRegistrationStrategy;

public class NullStrategy implements ServiceRegistrationStrategy {

	public void initialize(String serviceType, String serviceName, int port) throws Exception {
	}

	public void initialize(String serviceType, String serviceName, int port, Hashtable<String, String> properties) throws Exception {
	}

	public void register() throws Exception {
	}

	public void unregister() throws Exception {
	}

}
