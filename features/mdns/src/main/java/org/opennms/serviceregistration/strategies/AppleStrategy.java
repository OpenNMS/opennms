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

import org.opennms.serviceregistration.ServiceRegistrationStrategy;

import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;
import com.apple.dnssd.TXTRecord;

public class AppleStrategy implements ServiceRegistrationStrategy, RegisterListener {
	private boolean m_registered = false;
	private DNSSDRegistration m_registration;
	private String m_serviceType;
	private String m_serviceName;
	private int m_port;
	private Map<String,String> m_properties;

	public AppleStrategy() throws Exception {
	}

	public void initialize(final String serviceType, final String serviceName, final int port) throws Exception {
		initialize(serviceType, serviceName, port, null);
	}
	
	public void initialize(final String serviceType, final String serviceName, final int port, final Map<String,String> properties) throws Exception {
		if (m_registered == true) {
			throw new Exception("You have already m_registered a service with this object!");
		}
		
		m_serviceType   = "_" + serviceType.toLowerCase() + "._tcp";
		m_serviceName   = serviceName;
		m_port          = port;
		m_properties    = properties;
	}

	public void register() throws Exception {
		if (m_registered == false) {
			final TXTRecord txt = new TXTRecord();
			if (m_properties != null) {
				for (final String key : m_properties.keySet()) {
					txt.set(key, m_properties.get(key));
				}
			}
			DNSSD.register(0, 0, m_serviceName, m_serviceType, null, null, m_port, txt, this);
		} else {
			System.err.println("WARNING: register() called but the service has already been m_registered!");
		}
	}

	public void unregister() throws Exception {
		if (m_registered == true) {
			if (m_registration != null) {
				m_registration.stop();
				m_registration = null;
			} else {
				System.err.println("WARNING: unregister() has been called, but m_registration previously failed.");
			}
		} else {
			System.err.println("WARNING: unregister() called but no service has been m_registered.");
		}
		
	}

	public void serviceRegistered(final DNSSDRegistration registration, final int flags, final String serviceName, final String regType, final String domain) {
		m_registration = registration;
		m_registered = true;
	}

	public void operationFailed(final DNSSDService service, final int errorCode) {
		m_registered = false;
		System.err.println("m_registration failed for service '" + service + "' with error code '" + errorCode + "'");
	}

}
