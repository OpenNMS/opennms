package org.opennms.serviceregistration.strategies;

import java.util.Map;

import org.opennms.serviceregistration.ServiceRegistrationStrategy;

public class NullStrategy implements ServiceRegistrationStrategy {

	public void initialize(final String serviceType, final String serviceName, final int port) throws Exception {
	}

	public void initialize(final String serviceType, final String serviceName, final int port, final Map<String, String> properties) throws Exception {
	}

	public void register() throws Exception {
	}

	public void unregister() throws Exception {
	}

}
