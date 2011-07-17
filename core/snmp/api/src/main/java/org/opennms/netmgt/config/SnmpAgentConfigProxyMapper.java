package org.opennms.netmgt.config;

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
