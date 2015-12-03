package org.opennms.netmgt.model.topology;

public class BridgeForwardingTableEntry {

    final BridgePort m_bridgePort;
    final Integer m_vlanId;
    final String  m_mac;
    
    
    public BridgeForwardingTableEntry(BridgePort bridgePort, Integer vlanId,
            String mac) {
        super();
        m_bridgePort = bridgePort;
        m_vlanId = vlanId;
        this.m_mac = mac;
    }
    
    public BridgePort getBridgePort() {
        return m_bridgePort;
    }
    
    public Integer getVlanId() {
        return m_vlanId;
    }
    
    public String getMac() {
        return m_mac;
    }
    
    
}
