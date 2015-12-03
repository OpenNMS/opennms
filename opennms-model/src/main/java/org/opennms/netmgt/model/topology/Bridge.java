package org.opennms.netmgt.model.topology;

import java.util.HashSet;
import java.util.Set;

public class Bridge {

    final Integer m_id;
    Set<String> m_bridgeIds = new HashSet<String>();
    Set<String> m_portMacs = new HashSet<String>();
    Set<BridgePort> m_bridgePorts = new HashSet<BridgePort>();
    
    public Bridge(Integer id) {
        super();
        m_id = id;
    }

    public Bridge(Integer id, String bridgeId) {
        m_id = id;
        m_bridgeIds.add(bridgeId);
    }

    public Set<String> getBridgeIds() {
        return m_bridgeIds;
    }

    public void setBridgeIds(Set<String> bridgeIds) {
        m_bridgeIds = bridgeIds;
    }

    public void addBridgeId(String bridgeId) {
        m_bridgeIds.add(bridgeId);
    }

    public Set<String> getPortMacs() {
        return m_portMacs;
    }

    public void setPortMacs(Set<String> portMacs) {
        m_portMacs = portMacs;
    }

    public void addPortMac(String portMac) {
        m_portMacs.add(portMac);
    }
    public Set<BridgePort> getBridgePorts() {
        return m_bridgePorts;
    }

    public void setBridgePorts(Set<BridgePort> bridgePorts) {
        m_bridgePorts = bridgePorts;
    }

    public void addBridgePort(BridgePort bridgePort) {
        m_bridgePorts.add(bridgePort);
    }

    public Integer getId() {
        return m_id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
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
        Bridge other = (Bridge) obj;
        if (m_id == null) {
            if (other.m_id != null)
                return false;
        } else if (!m_id.equals(other.m_id))
            return false;
        return true;
    }

    
}
