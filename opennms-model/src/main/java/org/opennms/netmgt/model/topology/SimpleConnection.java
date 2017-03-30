package org.opennms.netmgt.model.topology;

import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;

public class SimpleConnection {
    final Set<String> m_links;
    final BridgeBridgeLink m_dlink;
    
    public SimpleConnection(Set<String> links, BridgeBridgeLink dlink){
        m_links = links;
        m_dlink = dlink;
    }

    public Set<String> getMacs() {
        return m_links;
    }

    public BridgeBridgeLink getDlink() {
        return m_dlink;
    }

}
