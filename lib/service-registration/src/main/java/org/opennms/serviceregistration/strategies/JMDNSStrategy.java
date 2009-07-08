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
