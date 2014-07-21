package org.opennms.web.isis;

public class IsisElementNode {

    private String m_isisSysID;
    private String m_isisSysAdminState;
	private String m_isisCreateTime;
	private String m_isisLastPollTime;
	
	public String getIsisSysID() {
		return m_isisSysID;
	}
	public void setIsisSysID(String isisSysID) {
		m_isisSysID = isisSysID;
	}
	public String getIsisSysAdminState() {
		return m_isisSysAdminState;
	}
	public void setIsisSysAdminState(String isisSysAdminState) {
		m_isisSysAdminState = isisSysAdminState;
	}
	public String getIsisCreateTime() {
		return m_isisCreateTime;
	}
	public void setIsisCreateTime(String isisCreateTime) {
		m_isisCreateTime = isisCreateTime;
	}
	public String getIsisLastPollTime() {
		return m_isisLastPollTime;
	}
	public void setIsisLastPollTime(String isisLastPollTime) {
		m_isisLastPollTime = isisLastPollTime;
	}

}
