package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;

public class SharedSegment {
    
    Integer m_designatedBridge;
    Integer m_designatedPort;
    List<BridgeMacLink> m_bridgeportsOnSegment = new ArrayList<BridgeMacLink>();
    List<BridgeBridgeLink> m_bridgeportsOnLink = null;
    
    public SharedSegment() {
        
    }
    
    public SharedSegment(Integer designatedBridge,Integer designatedPort) {
        m_designatedBridge=designatedBridge;
        m_designatedPort = designatedPort;
    }
    
    
    public void setDesignatedBridge(Integer designatedBridge) {
        m_designatedBridge = designatedBridge;
    }

    public void setDesignatedPort(Integer designatedPort) {
        m_designatedPort = designatedPort;
    }

    public Integer getDesignatedBridge() {
        return m_designatedBridge;
    }


    public Integer getDesignatedPort() {
        return m_designatedPort;
    }


    public boolean isEmpty() {
        if (noMacsOnSegment())
            return m_bridgeportsOnLink.isEmpty();
        return m_bridgeportsOnSegment.isEmpty();
    }
    
    //FIXME
    public void mergeBridge(SharedSegment shared, Integer bridgeId) {
        
    }

    //FIXME
    public void assign(List<BridgeMacLink> links) {
        
    }

    public void removeBridge(int bridgeId) {
        if (noMacsOnSegment()) {
            List<BridgeBridgeLink> curlist = new ArrayList<BridgeBridgeLink>();
            for (BridgeBridgeLink link: m_bridgeportsOnLink) {
                if (link.getNode().getId().intValue() == bridgeId ||
                        link.getDesignatedNode().getId().intValue() == bridgeId)
                    continue;
                curlist.add(link);
            }
            m_bridgeportsOnLink=curlist;
            return;
        }
        List<BridgeMacLink> curlist = new ArrayList<BridgeMacLink>();
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (link.getNode().getId().intValue() == bridgeId ) {
                continue;
            }
            curlist.add(link);
                
        }
        m_bridgeportsOnSegment=curlist;
    }
    
    public Integer getFirstNoDesignatedBridge() {
        for (Integer bridgeId: getBridgeIdsOnSegment()) {
            if (m_designatedBridge == null || bridgeId != m_designatedBridge)
                return bridgeId;
        }
        return null;
    }

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
        m_bridgeportsOnSegment.add(link);
    }
    
    public void add(BridgeBridgeLink link) {
        m_bridgeportsOnLink.add(link);
    }

    public Set<String> getMacsOnSegment() {
        Set<String>macs = new HashSet<String>();
            for (BridgeMacLink link: m_bridgeportsOnSegment)
                macs.add(link.getMacAddress());
        return macs;

    }

    public Set<Integer> getBridgeIdsOnSegment() {
        Set<Integer> nodes = new HashSet<Integer>();
        if (noMacsOnSegment()) {
            for (BridgeBridgeLink link: m_bridgeportsOnLink) {
                nodes.add(link.getNode().getId());
                nodes.add(link.getDesignatedNode().getId());
            }
            return nodes;
        }
        for ( BridgeMacLink link: m_bridgeportsOnSegment) {
            nodes.add(link.getNode().getId());
        }
        return nodes;
    }

    public boolean containsMac(String mac) {
        if ( mac == null) 
            return false;
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (mac.equals(link.getMacAddress()))
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
    
    public Integer getPortForBridge(Integer nodeid) {
        if (noMacsOnSegment()) {
            for (BridgeBridgeLink link: m_bridgeportsOnLink) {
                if (link.getNode().getId() == nodeid )
                    return link.getBridgePort();
                if (link.getDesignatedNode().getId() == nodeid )
                    return link.getDesignatedPort() ;
            }
            return null;
        }
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (link.getNode().getId() == nodeid) 
                return link.getBridgePort();
        }
        return null;
    }

    
}
