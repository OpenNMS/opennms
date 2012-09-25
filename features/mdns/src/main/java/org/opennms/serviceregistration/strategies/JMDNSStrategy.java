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
