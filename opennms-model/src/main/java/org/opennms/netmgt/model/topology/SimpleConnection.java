package org.opennms.netmgt.model.topology;

import java.util.List;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;

public class SimpleConnection {
    final List<BridgeMacLink> m_links;
    final BridgeBridgeLink m_dlink;
    
    public SimpleConnection(List<BridgeMacLink> links, BridgeBridgeLink dlink){
        m_links = links;
        m_dlink = dlink;
    }

    public List<BridgeMacLink> getLinks() {
        return m_links;
    }

    public BridgeBridgeLink getDlink() {
        return m_dlink;
    }

}
