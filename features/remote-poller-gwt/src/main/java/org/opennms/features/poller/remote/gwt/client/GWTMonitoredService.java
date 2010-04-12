package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Collection;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTMonitoredService implements Serializable, IsSerializable, Comparable<GWTMonitoredService> {
	private static final long serialVersionUID = 1L;

	private int m_id;
	private int m_ifIndex;
	private int m_ipInterfaceId;
	private String m_ipAddress;
	private String m_hostname;
	private int m_nodeId;
	private String m_serviceName;
	private Collection<String> m_applications;

	public void setId(final int id) {
		m_id = id;
	}
	public Integer getId() {
		return m_id;
	}
	public void setIfIndex(final Integer ifIndex) {
		m_ifIndex = ifIndex;
	}
	public Integer getIfIndex() {
		return m_ifIndex;
	}
	public void setIpInterfaceId(final Integer id) {
		m_ipInterfaceId = id;
	}
	public Integer getIpInterfaceId() {
		return m_ipInterfaceId;
	}
	public void setIpAddress(final String ipAddress) {
		m_ipAddress = ipAddress;
	}
	public String getIpAddress() {
		return m_ipAddress;
	}
	public void setHostname(final String hostname) {
		m_hostname = hostname;
	}
	public String getHostname() {
		return m_hostname;
	}
	public void setNodeId(final Integer nodeId) {
		m_nodeId = nodeId;
	}
	public Integer getNodeId() {
		return m_nodeId;
	}
	public void setServiceName(final String serviceName) {
		m_serviceName = serviceName;
	}
	public String getServiceName() {
		return m_serviceName;
	}
	public Collection<String> getApplications() {
		return m_applications;
	}
	public void setApplications(final Collection<String> applications) {
		m_applications = applications;
	}

	public String toString() {
		return "GWTMonitoredService[id=" + m_id + ",service=" + m_serviceName + ",nodeId=" + m_nodeId + ",ipInterfaceId=" + m_ipInterfaceId + "]";
	}

	public int compareTo(GWTMonitoredService that) {
		int lastCompare = this.getServiceName().compareTo(that.getServiceName());
		if (lastCompare != 0) return lastCompare;

		lastCompare = this.getNodeId().compareTo(that.getNodeId());
		if (lastCompare != 0) return lastCompare;
		
		lastCompare = this.getIpInterfaceId().compareTo(that.getIpInterfaceId());
		if (lastCompare != 0) return lastCompare;

		lastCompare = this.getApplications().toString().compareTo(that.getApplications().toString());
		if (lastCompare != 0) return lastCompare;

		lastCompare = Integer.valueOf(this.getId()).compareTo(that.getId());
		return lastCompare;
	}

}
