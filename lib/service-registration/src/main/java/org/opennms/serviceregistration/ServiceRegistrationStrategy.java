/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007, 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

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
