package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTApplication implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;
	private Integer m_id;
	private String m_name;
	private Set<GWTMonitoredService> m_services;

	public GWTApplication() {}

	public GWTApplication(final int id, final String name, final Set<GWTMonitoredService> services) {
		m_id = id;
		m_name = name;
		m_services = services;
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

	public boolean equals(Object o) {
		if (!(o instanceof GWTApplication)) return false;
		GWTApplication that = (GWTApplication)o;
		if (this.getId() == null && that.getId() == null) {
			return true;
		} else if (this.getId() == null) {
			return false;
		}
		return this.getId().equals(that.getId());
	}

	public String toString() {
		return "GWTApplication[id=" + m_id + ",name=" + m_name + ",services=[" + Utils.join(m_services, ", ") + "]]";
	}
}
