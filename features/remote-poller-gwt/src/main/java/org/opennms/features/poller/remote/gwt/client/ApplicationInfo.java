package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.EqualsUtil;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ApplicationInfo implements Serializable, IsSerializable, Comparable<ApplicationInfo> {
	private static final long serialVersionUID = 1L;
	private Integer m_id;
	private String m_name;
	private Set<GWTMonitoredService> m_services;
	private Set<String> m_locations;
	private StatusDetails m_statusDetails = StatusDetails.uninitialized();
	private Long m_priority = null;

	public ApplicationInfo() {
	}

	public ApplicationInfo(final int id, final String name, final Set<GWTMonitoredService> services, final Set<String> locationNames, final StatusDetails statusDetails) {
		m_id = id;
		m_name = name;
		m_services = services;
		m_locations = locationNames;
		m_statusDetails = statusDetails;
	}

	public Integer getId() {
		return m_id;
	}
	public void setId(final Integer id) {
		m_id = id;
	}
	public String getName() {
		return m_name;
	}
	public void setName(final String name) {
		m_name = name;
	}
	public Set<GWTMonitoredService> getServices() {
		return m_services;
	}
	public void setServices(final Set<GWTMonitoredService> services) {
		m_services = services;
	}
	public Set<String> getLocations() {
		return m_locations;
	}
	public void setLocations(final Set<String> locations) {
		m_locations = locations;
	}
	public void addLocation(final String name) {
		m_locations.add(name);
	}
	public StatusDetails getStatusDetails() {
		return m_statusDetails;
	}
	public void setStatusDetails(final StatusDetails statusDetails) {
		m_statusDetails = statusDetails;
	}
	public Long getPriority() {
		return m_priority == null? 0L : m_priority;
	}
	public void setPriority(final Long priority) {
		m_priority = priority;
	}
	public GWTMarkerState getMarkerState() {
		return new GWTMarkerState(m_name, null, m_statusDetails == null? Status.UNINITIALIZED : m_statusDetails.getStatus());
	}

	public boolean equals(Object aThat) {
		if (this == aThat) return true;
		if (!(aThat instanceof ApplicationInfo)) return false;
		ApplicationInfo that = (ApplicationInfo)aThat;
		return
			EqualsUtil.areEqual(this.getId(), that.getId()) &&
			EqualsUtil.areEqual(this.getName(), that.getName())
		;
	}

	public int hashCode() {
		return new HashCodeBuilder()
			.append(this.getId())
			.append(this.getName())
			.toHashcode();
	}

	public int compareTo(final ApplicationInfo that) {
		return new CompareToBuilder()
			.append(this.getStatusDetails(), that.getStatusDetails())
			.append(this.getPriority(), that.getPriority())
			.append(this.getName(), that.getName())
			.append(this.getId(), that.getId())
			.toComparison();
	}

	public String summary() {
		return "ApplicationInfo[id=" + m_id + ",name=" + m_name + "]";
	}
	
	public String toString() {
		return "ApplicationInfo[id=" + m_id
			+ ",name=" + m_name
			+ ",services=[" + StringUtils.join(m_services, ", ")
			+ "],locations=[" + StringUtils.join(m_locations, ", ")
			+ "],status=" + getStatusDetails()
			+ ",priority=" + getPriority() + "]";
	}
}
