package org.opennms.netmgt.model.topology;

import java.util.Set;

public class BridgePortWithMacs implements Topology {

    public static BridgePortWithMacs create(BridgePort port, Set<String> macs) throws BridgeTopologyException {
        if (port == null) {
            throw new BridgeTopologyException("cannot create BridgePortWithMacs bridge port is null");
        }
        if (macs == null) {
            throw new BridgeTopologyException("cannot create BridgePortWithMacs macs is null");
        }
        return new BridgePortWithMacs(port,macs);
        
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_macs == null) ? 0 : m_macs.hashCode());
        result = prime * result + ((m_port == null) ? 0 : m_port.hashCode());
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
        BridgePortWithMacs other = (BridgePortWithMacs) obj;
        if (m_macs == null) {
            if (other.m_macs != null)
                return false;
        } else if (!m_macs.equals(other.m_macs))
            return false;
        if (m_port == null) {
            if (other.m_port != null)
                return false;
        } else if (!m_port.equals(other.m_port))
            return false;
        return true;
    }

    private final BridgePort m_port;
    private final Set<String> m_macs;
    
 
    private BridgePortWithMacs(BridgePort port, Set<String> macs) {
        m_port=port;
        m_macs=macs;
    }

    public BridgePort getPort() {
        return m_port;
    }

    public Set<String> getMacs() {
        return m_macs;
    }

    @Override
    public String printTopology() {
        StringBuffer strbfr = new StringBuffer();
        strbfr.append(m_port.printTopology());
        strbfr.append(" macs:");
        strbfr.append(m_macs);
        
        return null;
    }


    
}
