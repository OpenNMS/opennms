package org.opennms.netmgt.dao.jdbc.monsvc;

import org.opennms.netmgt.dao.jdbc.ipif.IpInterfaceId;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class MonitoredServiceId {
	
	private Integer m_nodeId;
	private String m_ipAddr;
	private Integer m_ifIndex;
	private Integer m_serviceId;
	
	public MonitoredServiceId(Integer nodeId, String ipAddr, Integer ifIndex, Integer serviceId) {
		m_nodeId = nodeId;
		m_ipAddr = ipAddr;
		m_ifIndex = ifIndex;
		m_serviceId = serviceId;
	}
	
	public MonitoredServiceId(OnmsMonitoredService iface) {
		this(iface.getNodeId(), iface.getIpAddress(), iface.getIfIndex(), iface.getServiceId());
	}
	
	public IpInterfaceId getIpInterfaceId() {
		return new IpInterfaceId(m_nodeId, m_ipAddr, m_ifIndex);
	}

	public boolean equals(Object obj) {
		if (obj instanceof MonitoredServiceId) {
			MonitoredServiceId key = (MonitoredServiceId) obj;
			return (
					m_nodeId.equals(key.m_nodeId)
					&& m_ipAddr.equals(key.m_ipAddr)
					&& (m_ifIndex == null ? key.m_ifIndex == null : m_ifIndex.equals(key.m_ifIndex))
					&& (m_serviceId.equals(key.m_serviceId))
					);
		}
		return false;
	}

	public int hashCode() {
		return m_nodeId.hashCode() ^ m_ipAddr.hashCode() ^ m_serviceId.hashCode();
	}

	public Integer getServiceId() {
		return m_serviceId;
	}

	public Integer getIfIndex() {
		return m_ifIndex;
	}
	
	public String getIpAddr() {
		return m_ipAddr;
	}
	
	public Integer getNodeId() {
		return m_nodeId;
	}


}
