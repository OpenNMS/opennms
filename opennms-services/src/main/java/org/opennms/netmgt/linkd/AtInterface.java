package org.opennms.netmgt.linkd;

import java.net.InetAddress;

public class AtInterface {

    Integer m_nodeid;
    Integer m_ifIndex;
    public Integer getIfIndex() {
        return m_ifIndex;
    }
    public void setIfIndex(Integer ifIndex) {
        m_ifIndex = ifIndex;
    }



    String m_macAddress;
    InetAddress m_ipAddress;
    public Integer getNodeid() {
        return m_nodeid;
    }
    public void setNodeid(Integer nodeid) {
        m_nodeid = nodeid;
    }
    public String getMacAddress() {
        return m_macAddress;
    }
    public void setMacAddress(String macAddress) {
        m_macAddress = macAddress;
    }
    
    public InetAddress getIpAddress() {
        return m_ipAddress;
    }
    public void setIpAddress(InetAddress ipAddress) {
        m_ipAddress = ipAddress;
    }
    
    
    
    public AtInterface(Integer nodeid, String macAddress, InetAddress ipAddress) {
        super();
        m_nodeid = nodeid;
        m_macAddress = macAddress;
        m_ipAddress = ipAddress;
    }
    
    
}
