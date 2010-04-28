package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ApplicationInfo implements Serializable, IsSerializable, Comparable<ApplicationInfo> {
	private static final long serialVersionUID = 1L;
	private Integer m_id;
	private String m_name;
	private Set<GWTMonitoredService> m_services;
	private Set<String> m_locations;
	private Status m_status = Status.UNINITIALIZED;
	private String m_reason = "";
	private Long m_priority = null;

	public ApplicationInfo() {
	}

	public ApplicationInfo(final int id, final String name, final Set<GWTMonitoredService> services, final Set<String> locationNames, final Status status, final String reason) {
		m_id = id;
		m_name = name;
		m_services = services;
		m_locations = locationNames;
		m_status = status;
		m_reason = reason;
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
	public Status getStatus() {
		return m_status;
	}
	public void setStatus(final Status status) {
		m_status = status;
	}
	public String getReason() {
		return m_reason;
	}
	public void setReason(final String reason) {
		m_reason = reason;
	}
	public Long getPriority() {
		return m_priority == null? 0L : m_priority;
	}
	public void setPriority(final Long priority) {
		m_priority = priority;
	}
	public GWTMarkerState getMarkerState() {
		return new GWTMarkerState(m_name, null, m_status);
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
		return 2 * (this.getId() == null? 1 : this.getId().hashCode() + this.getName() == null? 1 : this.getName().hashCode());
	}

	public int compareTo(final ApplicationInfo that) {
		int compareVal;
		compareVal = this.getStatus() == null? 0 : this.getStatus().compareTo(that.getStatus());
		if (compareVal != 0) return compareVal;
		compareVal = this.getPriority().compareTo(that.getPriority());
		if (compareVal != 0) return compareVal;
		compareVal = this.getName() == null? 0 : this.getName().compareTo(that.getName());
		if (compareVal != 0) return compareVal;
		return this.getId().compareTo(that.getId());
	}

	public String summary() {
		return "ApplicationInfo[id=" + m_id + ",name=" + m_name + "]";
	}
	
	public String toString() {
		return "ApplicationInfo[id=" + m_id
			+ ",name=" + m_name
			+ ",services=[" + Utils.join(m_services, ", ")
			+ "],locations=[" + Utils.join(m_locations, ", ")
			+ "],status=" + getStatus()
			+ ",priority=" + getPriority() + "]";
	}
}
