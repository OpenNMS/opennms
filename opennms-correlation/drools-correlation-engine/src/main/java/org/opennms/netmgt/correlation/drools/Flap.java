package org.opennms.netmgt.correlation.drools;

import java.util.Date;

public class Flap {
    Long m_nodeid;
    String m_ipAddr;
    String m_svcName;
    Date m_startTime;
    Date m_endTime;
    Integer m_locationMonitor;
    boolean m_counted;
    Integer m_timerId;
    
    public Flap(Long nodeid, String ipAddr, String svcName, Integer locationMonitor, Integer timerId) {
        m_nodeid = nodeid;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_locationMonitor = locationMonitor;
        m_timerId = timerId;
        m_startTime = new Date();
        m_counted = false;
    }
    
    public Date getEndTime() {
        return m_endTime;
    }
    public void setEndTime(Date end) {
        m_endTime = end;
    }
    public String getIpAddr() {
        return m_ipAddr;
    }
    public void setIpAddr(String ipAddr) {
        m_ipAddr = ipAddr;
    }
    public Integer getLocationMonitor() {
        return m_locationMonitor;
    }
    public void setLocationMonitor(Integer locationMonitor) {
        m_locationMonitor = locationMonitor;
    }
    public Long getNodeid() {
        return m_nodeid;
    }
    public void setNodeid(Long nodeid) {
        m_nodeid = nodeid;
    }
    public Date getStartTime() {
        return m_startTime;
    }
    public void setStartTime(Date start) {
        m_startTime = start;
    }
    public String getSvcName() {
        return m_svcName;
    }
    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }

    public boolean isCounted() {
        return m_counted;
    }

    public void setCounted(boolean counted) {
        m_counted = counted;
    }

    public Integer getTimerId() {
        return m_timerId;
    }

    public void setTimerId(Integer timerId) {
        m_timerId = timerId;
    }
    
    
}
