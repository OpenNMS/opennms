package org.opennms.netmgt.model;

import org.opennms.netmgt.model.topology.Topology;

public class OnmsTopologyProtocol {

    public static OnmsTopologyProtocol createFromTopologySupportedProtocol(Topology.ProtocolSupported protocol) {
        return new OnmsTopologyProtocol(protocol.name());
    }

    public static OnmsTopologyProtocol createFromString(String protocol) {
        return new OnmsTopologyProtocol(protocol);
    }

    
    final private String m_protocol;
    private OnmsTopologyProtocol(String protocol) {
        m_protocol = protocol;
    }
    
    public String getProtocol() {
        return m_protocol;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_protocol == null) ? 0 : m_protocol.hashCode());
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
        OnmsTopologyProtocol other = (OnmsTopologyProtocol) obj;
        if (m_protocol == null) {
            if (other.m_protocol != null)
                return false;
        } else if (!m_protocol.equals(other.m_protocol))
            return false;
        return true;
    }
    
    
    

}
