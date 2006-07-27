package org.opennms.web.svclayer.catstatus.model;


public class StatusService {

	private String m_name;
	private Boolean m_outagestatus;
	private long m_outagetime;
	
	
	public String getName() {
		return m_name;
	}
	public void setName(String m_name) {
		this.m_name = m_name;
	}
	public Boolean getOutageStatus() {
		return m_outagestatus;
	}
	public void setOutageStatus(Boolean m_outagestatus) {
		this.m_outagestatus = m_outagestatus;
	}
	public long getOutageTime() {
		return m_outagetime;
	}
	public void setOutageTime(long m_outagetime) {
		this.m_outagetime = m_outagetime;
	}
	
}
