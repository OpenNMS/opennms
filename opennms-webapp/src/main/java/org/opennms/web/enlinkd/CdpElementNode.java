package org.opennms.web.enlinkd;

public class CdpElementNode {

    private String m_cdpGlobalRun;
    private String m_cdpGlobalDeviceId;
    private String m_cdpCreateTime;
    private String m_cdpLastPollTime;

    public String getCdpCreateTime() {
        return m_cdpCreateTime;
    }

    public void setCdpCreateTime(String cdpCreateTime) {
        m_cdpCreateTime = cdpCreateTime;
    }

    public String getCdpLastPollTime() {
        return m_cdpLastPollTime;
    }

    public void setCdpLastPollTime(String cdpLastPollTime) {
        m_cdpLastPollTime = cdpLastPollTime;
    }

    public String getCdpGlobalRun() {
        return m_cdpGlobalRun;
    }

    public void setCdpGlobalRun(String cdpGlobalRun) {
        m_cdpGlobalRun = cdpGlobalRun;
    }

    public String getCdpGlobalDeviceId() {
        return m_cdpGlobalDeviceId;
    }

    public void setCdpGlobalDeviceId(String cdpGlobalDeviceId) {
        m_cdpGlobalDeviceId = cdpGlobalDeviceId;
    }

}
