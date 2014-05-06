package org.opennms.web.lldp;

public class LldpElementNode {

	private String m_lldpChassisIdString;
	private String m_lldpSysName;
	private String m_lldpCreateTime;
	private String m_lldpLastPollTime;
	
	
	public String getLldpChassisIdString() {
		return m_lldpChassisIdString;
	}
	public void setLldpChassisIdString(String lldpSysIdString) {
		m_lldpChassisIdString = lldpSysIdString;
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
