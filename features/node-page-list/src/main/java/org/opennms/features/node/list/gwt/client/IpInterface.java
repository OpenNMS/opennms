package org.opennms.features.node.list.gwt.client;

public class IpInterface {

    private String m_ipAddress;
    private String m_ipHostName;
    private String m_managed;
    
    public IpInterface(String ipAddress, String ipHostName, String managed) {
        setIpAddress(ipAddress);
        setIpHostName(ipHostName);
        setManaged(managed);
    }
    public String getIpAddress() {
        return m_ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        m_ipAddress = ipAddress;
    }
    public String getIpHostName() {
        return m_ipHostName;
    }
    public void setIpHostName(String ipHostName) {
        m_ipHostName = ipHostName;
    }
    public String getManaged() {
        return m_managed;
    }
    public void setManaged(String managed) {
        m_managed = managed;
    }

}
