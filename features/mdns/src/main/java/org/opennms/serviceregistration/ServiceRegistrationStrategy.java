/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.serviceregistration;

import java.util.Map;

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
	public void initialize(String serviceType, String serviceName, int port, Map<String,String> properties) throws Exception;

	/**
	 * Register the service.
	 */
	public void register() throws Exception;
	
	/**
	 * Unregister the service.
	 */
	public void unregister() throws Exception;
}
