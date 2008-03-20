package org.opennms.netmgt.correlation.drools;

import java.util.Date;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.core.style.ToStringCreator;


public class FlapCount {
    Long m_nodeid;
    String m_ipAddr;
    String m_svcName;
    Integer m_locationMonitor;
    boolean m_alerted;

    Integer m_count;
    
    public FlapCount(Long nodeid, String ipAddr, String svcName, Integer locationMonitor) {
        m_nodeid = nodeid;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_locationMonitor = locationMonitor;
        m_count = 1;
        m_alerted = false;
        
        log().info("FlapCount.created : "+this);
    }
    
    public void increment() {
        m_count += 1;
        log().info("FlapCount.increment : "+this);
    }
    
    public void decrement() {
        m_count -= 1;
        log().info("FlapCount.decrement : "+this);
    }

    public Integer getCount() {
        return m_count;
    }

    public void setCount(Integer count) {
        m_count = count;
    }

    public String getIpAddr() {
        return m_ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        m_ipAddr = ipAddr;
    }

    public Long getNodeid() {
        return m_nodeid;
    }

    public void setNodeid(Long nodeid) {
        m_nodeid = nodeid;
    }

    public String getSvcName() {
        return m_svcName;
    }

    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }

    public boolean isAlerted() {
        return m_alerted;
    }

    public void setAlerted(boolean alerted) {
        m_alerted = alerted;
    }

    public Integer getLocationMonitor() {
        return m_locationMonitor;
    }

    public void setLocationMonitor(Integer locationMonitor) {
        m_locationMonitor = locationMonitor;
    }
    
    public String toString() {
        ToStringCreator creator = new ToStringCreator(this);
        creator.append("nodeid", m_nodeid);
        creator.append("ipAddr", m_ipAddr);
        creator.append("svcName", m_svcName);
        creator.append("locMon", m_locationMonitor);
        creator.append("count", m_count);
        return creator.toString();
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
