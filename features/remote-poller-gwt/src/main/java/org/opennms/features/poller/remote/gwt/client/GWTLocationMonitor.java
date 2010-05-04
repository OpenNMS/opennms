package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Date;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTLocationMonitor implements Serializable, IsSerializable, Comparable<GWTLocationMonitor> {
	private static final long serialVersionUID = 1L;

	private Integer m_id;
	private String m_status;
	private String m_definitionName;
	private String m_name;
	private Date m_lastCheckInTime;

	public Integer getId() {
		return m_id;
	}
	public void setId(final Integer id) {
		m_id = id;
	}

	public String getStatus() {
		return m_status;
	}
	public void setStatus(final String string) {
		m_status = string;
	}
	public String getDefinitionName() {
		return m_definitionName;
	}
	public void setDefinitionName(final String definitionName) {
		m_definitionName = definitionName;
	}
	public String getName() {
		return m_name;
	}
	public void setName(final String name) {
		m_name = name;
	}
	public Date getLastCheckInTime() {
		return m_lastCheckInTime;
	}
	public void setLastCheckInTime(final Date lastCheckInTime) {
		m_lastCheckInTime = lastCheckInTime;
	}

	public String toString() {
		return "GWTLocationMonitor[name=" + m_name + ",status=" + m_status + ",lastCheckInTime=" + m_lastCheckInTime + "]";
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof GWTLocationMonitor)) return false;
		GWTLocationMonitor that = (GWTLocationMonitor)o;
		if (this.getId().equals(that.getId())) return true;
		return false;
	}

	public int hashCode() {
		return new HashCodeBuilder()
			.append(this.getId())
			.append(this.getName())
			.append(this.getDefinitionName())
			.toHashcode();
	}

	public int compareTo(GWTLocationMonitor that) {
		return new CompareToBuilder()
			.append(this.getDefinitionName(), that.getDefinitionName())
			.append(this.getName(), that.getName())
			.append(this.getStatus(), that.getStatus())
			.append(this.getLastCheckInTime(), that.getLastCheckInTime())
			.toComparison();
	}
}
