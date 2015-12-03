package org.opennms.netmgt.model.topology;

import java.util.Set;

public class BridgePort {

    final Bridge m_bridge;
    final Integer m_bridgePortId;
    final Integer m_BridgePortIfIndex;

    Set<BridgeForwardingTableEntry> m_forwardingtable;
    
    public BridgePort(Bridge bridge, Integer bridgePortId, Integer bridgePortIfIndex) {
        super();
        m_bridge = bridge;
        m_bridgePortId = bridgePortId;
        m_BridgePortIfIndex = bridgePortIfIndex;
        m_bridge.addBridgePort(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((m_BridgePortIfIndex == null)
                                                ? 0
                                                : m_BridgePortIfIndex.hashCode());
        result = prime * result
                + ((m_bridge == null) ? 0 : m_bridge.hashCode());
        result = prime * result
                + ((m_bridgePortId == null) ? 0 : m_bridgePortId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BridgePort other = (BridgePort) obj;
        if (m_BridgePortIfIndex == null) {
            if (other.m_BridgePortIfIndex != null)
                return false;
        } else if (!m_BridgePortIfIndex.equals(other.m_BridgePortIfIndex))
            return false;
        if (m_bridge == null) {
            if (other.m_bridge != null)
                return false;
        } else if (!m_bridge.equals(other.m_bridge))
            return false;
        if (m_bridgePortId == null) {
            if (other.m_bridgePortId != null)
                return false;
        } else if (!m_bridgePortId.equals(other.m_bridgePortId))
            return false;
        return true;
    }

    public Set<BridgeForwardingTableEntry> getForwardingtable() {
        return m_forwardingtable;
    }

    public void setForwardingtable(Set<BridgeForwardingTableEntry> forwardingtable) {
        m_forwardingtable = forwardingtable;
    }
    
    public void addForwardingTableEntry(BridgeForwardingTableEntry forwardingTableEntry) {
        m_forwardingtable.add(forwardingTableEntry);
    }

    public Bridge getBridge() {
        return m_bridge;
    }

    public Integer getBridgePortId() {
        return m_bridgePortId;
    }

    public Integer getBridgePortIfIndex() {
        return m_BridgePortIfIndex;
    }



    
}
