package org.opennms.netmgt.model.topology;

import java.util.List;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;

public class BridgeTopology {
    List<BridgeMacLink> m_bridgeMacLinks;
    List<BridgeBridgeLink> m_bridgeBridgeLinks;

    public List<BridgeMacLink> getBridgeMacLinks() {
        return m_bridgeMacLinks;
    }
    public void setBridgeMacLinks(List<BridgeMacLink> bridgeMacLinks) {
        m_bridgeMacLinks = bridgeMacLinks;
    }
    public List<BridgeBridgeLink> getBridgeBridgeLinks() {
        return m_bridgeBridgeLinks;
    }
    public void setBridgeBridgeLinks(List<BridgeBridgeLink> bridgeBridgeLinks) {
        m_bridgeBridgeLinks = bridgeBridgeLinks;
    }
    
    
}
