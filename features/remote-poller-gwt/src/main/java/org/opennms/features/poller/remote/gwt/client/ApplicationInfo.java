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

	public ApplicationInfo() {}

	public ApplicationInfo(final int id, final String name, final Set<GWTMonitoredService> services, final Set<String> locationNames) {
		m_id = id;
		m_name = name;
		m_services = services;
		m_locations = locationNames;
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
	
	public boolean equals(Object o) {
		if (!(o instanceof ApplicationInfo)) return false;
		ApplicationInfo that = (ApplicationInfo)o;
		if (this.getId() == null && that.getId() == null) {
			return true;
		} else if (this.getId() == null) {
			return false;
		}
		return this.getId().equals(that.getId());
	}

	public int compareTo(final ApplicationInfo that) {
		int compareVal = this.getName().compareTo(that.getName());
		if (compareVal != 0) return compareVal;
		compareVal = this.getStatus().compareTo(that.getStatus());
		if (compareVal != 0) return compareVal;
		return this.getId().compareTo(that.getId());
	}

	public String toString() {
		return "ApplicationInfo[id=" + m_id + ",name=" + m_name + ",services=[" + Utils.join(m_services, ", ") + "]]";
	}
}
