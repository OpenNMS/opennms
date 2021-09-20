/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.test.snmp;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.snmp.SnmpAgentAddress;

public class SnmpAgentConfigProxyMapper {
	private static SnmpAgentConfigProxyMapper m_instance;
	private Map<InetAddress,SnmpAgentAddress> m_addressToProxyAddress = new HashMap<InetAddress,SnmpAgentAddress>();

	protected SnmpAgentConfigProxyMapper() {
	}
	
	public static SnmpAgentConfigProxyMapper getInstance() {
		if (m_instance == null) {
			m_instance = new SnmpAgentConfigProxyMapper();
		}
		return m_instance;
	}

	public static void setInstance(final SnmpAgentConfigProxyMapper mapper) {
		m_instance = mapper;
	}

	/**
	 * Returns the proxied {@link SnmpAgentAddress} that the provided host address.
	 * is mapped to.
	 * @param address a "real" SNMP agent address
	 * @return the monitored host address
	 */
	public SnmpAgentAddress getAddress(final InetAddress address) {
		return m_addressToProxyAddress.get(address);
	}

	/**
	 * Sets the proxied {@link SnmpAgentAddress} for a given {@link SnmpAgentAddress}
	 * @param hostAddress the "real" SNMP agent address
	 * @param proxyAgentAddress the SNMP agent address it is mapped to
	 */
	public void addProxy(final InetAddress hostAddress, final SnmpAgentAddress proxyAgentAddress) {
		m_addressToProxyAddress.put(hostAddress, proxyAgentAddress);
	}

	/**
	 * Whether or not the given address is in use in the proxy mapper.
	 */
	public boolean contains(final SnmpAgentAddress listenAddress) {
		return (m_addressToProxyAddress.values().contains(listenAddress));
	}
}
