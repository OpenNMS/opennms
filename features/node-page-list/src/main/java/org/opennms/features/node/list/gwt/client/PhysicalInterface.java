package org.opennms.features.node.list.gwt.client;

public class PhysicalInterface {
    
    private String m_index;
    private String m_snmpIfDescr;
    private String m_snmpIfName;
    private String m_snmpIfAlias;
    private String m_snmpIfSpeed;
    private String m_ipAddress;
    
    /**
     * 
     * @param index
     * @param snmpIfDesc
     * @param snmpIfName
     * @param snmpIfAlias
     * @param snmpIfSpeed
     * @param ipAddress
     */
    public PhysicalInterface(String index, String snmpIfDesc, String snmpIfName, String snmpIfAlias, String snmpIfSpeed, String ipAddress) {
        setIndex(index);
        setSnmpIfDescr(snmpIfDesc);
        setSnmpIfName(snmpIfName);
        setSnmpIfAlias(snmpIfAlias);
        setSnmpIfSpeed(snmpIfSpeed);
        setIpAddress(ipAddress);
    }
    
    public String getIndex() {
        return m_index;
    }

    public void setIndex(String index) {
        m_index = index;
    }

    public void setSnmpIfDescr(String snmpIfIndex) {
        m_snmpIfDescr = snmpIfIndex;
    }

    public String getSnmpIfDescr() {
        return m_snmpIfDescr;
    }

    public String getSnmpIfName() {
        return m_snmpIfName;
    }

    public void setSnmpIfName(String snmpIfName) {
        m_snmpIfName = snmpIfName;
    }

    public String getSnmpIfAlias() {
        return m_snmpIfAlias;
    }

    public void setSnmpIfAlias(String snmpIfAlias) {
        m_snmpIfAlias = snmpIfAlias;
    }

    public void setSnmpIfSpeed(String snmpIfSpeed) {
        m_snmpIfSpeed = snmpIfSpeed;
    }

    public String getSnmpIfSpeed() {
        return m_snmpIfSpeed;
    }

    public String getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        m_ipAddress = ipAddress;
    };

}
