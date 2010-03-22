package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Date;

public class GWTLocationMonitor implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer m_id;
	private String m_status;
	private String m_definitionName;
	private String m_name;
	private Date m_lastCheckInTime;

	public int getId() {
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
		return "GWTLocationMonitor[id=" + m_id + ",status=" + m_status + ",definitionName=" + m_definitionName + ",name=" + m_name + ",lastCheckInTime=" + m_lastCheckInTime + "]";
	}
}
