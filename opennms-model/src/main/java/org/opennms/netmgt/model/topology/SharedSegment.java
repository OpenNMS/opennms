package org.opennms.netmgt.model.topology;

import java.util.List;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;

public class SharedSegment {

    List<BridgeMacLink> m_bridgeportsOnSegment;
    List<BridgeBridgeLink> m_bridgeportsOnLink;
        
    public List<BridgeBridgeLink> getBridgeBridgeLinks() {
        return m_bridgeportsOnLink;
    }
    
    public List<BridgeMacLink> getBridgeMacLinks() {
        return m_bridgeportsOnSegment;
    }
    
    public boolean noMacsOnSegment() {
        return m_bridgeportsOnSegment.isEmpty();
    }
    
    public void add(BridgeMacLink link) {
        
    }
    
    public void add(BridgeBridgeLink link) {
        
    }

    public boolean containsMac(String mac) {
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (mac != null && mac.equals(link.getMacAddress()))
                return true;
        }
        return false;
    }

    public boolean containsPort(Integer nodeid, Integer bridgeport) {
        if (noMacsOnSegment()) {
            for (BridgeBridgeLink link: m_bridgeportsOnLink) {
                if (link.getNode().getId() == nodeid && link.getBridgePort() == bridgeport)
                    return true;
                if (link.getDesignatedNode().getId() == nodeid && link.getDesignatedPort() == bridgeport)
                    return true;
            }
            return false;
        }
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (link.getNode().getId() == nodeid && link.getBridgePort() == bridgeport) 
                return true;
        }
        return false;
    }

    
}
