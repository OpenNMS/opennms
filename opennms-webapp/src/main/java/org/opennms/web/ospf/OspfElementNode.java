package org.opennms.web.ospf;

public class OspfElementNode {

	private String m_ospfRouterId;
	private Integer m_ospfVersionNumber;
	private String m_ospfAdminStat;
	private String m_ospfCreateTime;
	private String m_ospfLastPollTime;

	public String getOspfRouterId() {
		return m_ospfRouterId;
	}
	public void setOspfRouterId(String ospfRouterId) {
		m_ospfRouterId = ospfRouterId;
	}
	public Integer getOspfVersionNumber() {
		return m_ospfVersionNumber;
	}
	public void setOspfVersionNumber(Integer ospfVersionNumber) {
		m_ospfVersionNumber = ospfVersionNumber;
	}
	public String getOspfAdminStat() {
		return m_ospfAdminStat;
	}
	public void setOspfAdminStat(String ospfAdminStat) {
		m_ospfAdminStat = ospfAdminStat;
	}
	public String getOspfCreateTime() {
		return m_ospfCreateTime;
	}
	public void setOspfCreateTime(String ospfCreateTime) {
		m_ospfCreateTime = ospfCreateTime;
	}
	public String getOspfLastPollTime() {
		return m_ospfLastPollTime;
	}
	public void setOspfLastPollTime(String ospfLastPollTime) {
		m_ospfLastPollTime = ospfLastPollTime;
	}

}
