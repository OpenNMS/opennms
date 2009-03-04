package org.opennms.serviceregistration;

import java.util.Hashtable;

public interface ServiceRegistrationStrategy {

	/**
	 * Initialize the service registration strategy.
	 * @param serviceType  the service type string (eg, "http")
	 * @param serviceName  the name of the service (eg, "My Service")
	 * @param port         the port the service is listening on
	 * @throws Exception
	 */
	public void initialize(String serviceType, String serviceName, int port) throws Exception;
	
	/**
	 * Initialize the service registration strategy.
	 * 
	 * @param serviceType   the service type string (eg, "http")
	 * @param serviceName   the name of the service (eg, "My Service")
	 * @param port          the port the service is listening on
	 * @param properties    other properties (eg, path = "/opennms")
	 * @throws Exception
	 */
	public void initialize(String serviceType, String serviceName, int port, Hashtable<String,String> properties) throws Exception;

	/**
	 * Register the service.
	 */
	public void register() throws Exception;
	
	/**
	 * Unregister the service.
	 */
	public void unregister() throws Exception;
}
