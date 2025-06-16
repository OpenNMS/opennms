/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
