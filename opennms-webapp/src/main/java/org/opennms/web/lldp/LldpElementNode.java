package org.opennms.web.lldp;

public class LldpElementNode {

	private String m_lldpSysIdString;
	private String m_lldpSysName;
	private String   m_lldpCreateTime;
	private String   m_lldpLastPollTime;
	public String getLldpSysIdString() {
		return m_lldpSysIdString;
	}
	public void setLldpSysIdString(String lldpSysIdString) {
		m_lldpSysIdString = lldpSysIdString;
	}
	public String getLldpSysName() {
		return m_lldpSysName;
	}
	public void setLldpSysName(String lldpSysName) {
		m_lldpSysName = lldpSysName;
	}
	public String getLldpCreateTime() {
		return m_lldpCreateTime;
	}
	public void setLldpCreateTime(String lldpCreateTime) {
		m_lldpCreateTime = lldpCreateTime;
	}
	public String getLldpLastPollTime() {
		return m_lldpLastPollTime;
	}
	public void setLldpLastPollTime(String lldpLastPollTime) {
		m_lldpLastPollTime = lldpLastPollTime;
	}
	
}
