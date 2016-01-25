package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;

public class SharedSegment {
    
    Integer m_designatedBridge;
    List<BridgeMacLink> m_bridgeportsOnSegment = new ArrayList<BridgeMacLink>();
    List<BridgeBridgeLink> m_bridgeportsOnLink = new ArrayList<BridgeBridgeLink>();
    BroadcastDomain m_domain;
    
    public SharedSegment(){};
    
    public SharedSegment(BroadcastDomain domain) {
        m_domain =domain;
    }
    
    public BroadcastDomain getBroadcastDomain() {
        return m_domain; 
    }

    public void setBroadcastDomain(BroadcastDomain domain) {
        m_domain = domain; 
    }

    public SharedSegment(BroadcastDomain domain, BridgeMacLink link) {
        m_domain =domain;
        m_designatedBridge = link.getNode().getId();
        m_bridgeportsOnSegment.add(link);
    }

    public SharedSegment(BroadcastDomain domain, Integer designatedBridge,Integer designatedPort) {
        m_domain =domain;
        m_designatedBridge=designatedBridge;
    }
    
    
    public void setDesignatedBridge(Integer designatedBridge) {
        m_designatedBridge = designatedBridge;
    }

    public Integer getDesignatedBridge() {
        return m_designatedBridge;
    }


    public Integer getDesignatedPort() {
        return getPortForBridge(m_designatedBridge);
    }


    public boolean isEmpty() {
        return m_bridgeportsOnSegment.isEmpty() && m_bridgeportsOnLink.isEmpty();
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

    public void setBridgeMacLinks(List<BridgeMacLink> links) {
        m_bridgeportsOnSegment = links;
    }

    public void add(BridgeMacLink link) {
        m_bridgeportsOnSegment.add(link);
    }
    
    public void add(BridgeBridgeLink dlink) {
        if (m_bridgeportsOnLink.isEmpty()) {
            m_bridgeportsOnLink.add(dlink);
            return;
        } 
        BridgeBridgeLink first= m_bridgeportsOnLink.iterator().next();
        if (first.getNode().getId().intValue() == dlink.getNode().getId().intValue()) {
            m_bridgeportsOnLink.add(dlink);
            return;
        }

        BridgeBridgeLink x = new BridgeBridgeLink();
        x.setNode(first.getNode());
        x.setBridgePort(first.getBridgePort());
        x.setBridgePortIfIndex(first.getBridgePortIfIndex());
        x.setBridgePortIfName(first.getBridgePortIfName());
        x.setVlan(first.getVlan());
        
        x.setDesignatedNode(dlink.getNode());
        x.setDesignatedPort(dlink.getBridgePort());
        x.setDesignatedPortIfIndex(dlink.getBridgePortIfIndex());
        x.setDesignatedPortIfName(dlink.getBridgePortIfName());
        x.setDesignatedVlan(dlink.getVlan());
        m_bridgeportsOnLink.add(x);
        
        if (first.getNode().getId().intValue() != dlink.getDesignatedNode().getId().intValue()) {
            BridgeBridgeLink y = new BridgeBridgeLink();
            y.setNode(first.getNode());
            y.setBridgePort(first.getBridgePort());
            y.setBridgePortIfIndex(first.getBridgePortIfIndex());
            y.setBridgePortIfName(first.getBridgePortIfName());
            y.setVlan(first.getVlan());
            
            y.setDesignatedNode(dlink.getDesignatedNode());
            y.setDesignatedPort(dlink.getDesignatedPort());
            y.setDesignatedPortIfIndex(dlink.getDesignatedPortIfIndex());
            y.setDesignatedPortIfName(dlink.getDesignatedPortIfName());
            y.setDesignatedVlan(dlink.getDesignatedVlan());
            m_bridgeportsOnLink.add(y);
        }
    }

    //   this=topSegment {tmac...} {(tbridge,tport)....}U{bridgeId, bridgeIdPortId} 
    //        |
    //     bridge Id
    //        |
    //      shared {smac....} {(sbridge,sport).....}U{bridgeId,bridgePort)
    //       | | |
    //       A B C
    //    move all the macs and port on shared
    //  ------> topSegment {tmac...}U{smac....} {(tbridge,tport)}U{(sbridge,sport).....}
    public void mergeBridge(SharedSegment shared, Integer bridgeId) {
        for (BridgeBridgeLink dlink: shared.getBridgeBridgeLinks())
            add(dlink);

        for (BridgeMacLink link: shared.getBridgeMacLinks()) 
            m_bridgeportsOnSegment.add(link);
        
        removeBridge(bridgeId);

    }

    public void assign(List<BridgeMacLink> links, BridgeBridgeLink dlink) {
        
        add(dlink);
                
        Map<BridgeMacLinkHash,BridgeMacLink> sharedsegmentmaclinks = new HashMap<BridgeMacLinkHash,BridgeMacLink>();
        for (BridgeMacLink link: links)
            sharedsegmentmaclinks.put(new BridgeMacLinkHash(link),link);
        //intersection is not null, then we have to add all the BridgeMacLink
        // for each mac address
        for (BridgeMacLink link: m_bridgeportsOnSegment)
            sharedsegmentmaclinks.put(new BridgeMacLinkHash(link),link);

        m_bridgeportsOnSegment = new ArrayList<BridgeMacLink>(sharedsegmentmaclinks.values());
    }

    public void removeBridge(int bridgeId) {
        if (!m_bridgeportsOnLink.isEmpty()) {
            BridgeBridgeLink first = m_bridgeportsOnLink.iterator().next();
            List<BridgeBridgeLink> curlist = new ArrayList<BridgeBridgeLink>();
            if (first.getNode().getId().intValue() == bridgeId) {
                for (BridgeBridgeLink link: m_bridgeportsOnLink) {
                    if (link.getDesignatedNode().getId() == first.getDesignatedNode().getId())
                        continue;
                    link.setNode(first.getDesignatedNode());
                    link.setBridgePort(first.getDesignatedPort());
                    link.setBridgePortIfIndex(first.getDesignatedPortIfIndex());
                    link.setBridgePortIfName(first.getDesignatedPortIfName());
                    link.setVlan(first.getDesignatedVlan());
                    curlist.add(link);
                }
            } else {
                for (BridgeBridgeLink link: m_bridgeportsOnLink) {
                    if (link.getDesignatedNode().getId().intValue() == bridgeId )
                        continue;
                    curlist.add(link);
                }
            }
            m_bridgeportsOnLink=curlist;
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
    
    public void removeMacs(Map<Integer,List<BridgeMacLink>> throughset) {
        Set<String> mactoberemoved = new HashSet<String>();
        for (Integer port: throughset.keySet()) {
            for (BridgeMacLink link: throughset.get(port))
                mactoberemoved.add(link.getMacAddress());
        }

        List<BridgeMacLink> curlist = new ArrayList<BridgeMacLink>();
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (mactoberemoved.contains(link.getMacAddress()))
                continue;
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

    public Set<String> getMacsOnSegment() {
        Set<String>macs = new HashSet<String>();
            for (BridgeMacLink link: m_bridgeportsOnSegment)
                macs.add(link.getMacAddress());
        return macs;

    }

    public Set<Integer> getBridgeIdsOnSegment() {
        Set<Integer> nodes = new HashSet<Integer>();
        for (BridgeBridgeLink link: m_bridgeportsOnLink) {
            nodes.add(link.getNode().getId());
            nodes.add(link.getDesignatedNode().getId());
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
        for (BridgeBridgeLink link: m_bridgeportsOnLink) {
            if (link.getNode().getId().intValue() == nodeid.intValue() && 
                    link.getBridgePort().intValue() == bridgeport.intValue())
                return true;
            if (link.getDesignatedNode().getId().intValue() == nodeid.intValue() 
                    && link.getDesignatedPort().intValue() == bridgeport.intValue())
                return true;
        }
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (link.getNode().getId().intValue() == nodeid.intValue() 
                    && link.getBridgePort().intValue() == bridgeport.intValue()) 
                return true;
        }
        return false;
    }
    
    public Integer getPortForBridge(Integer nodeid) {
        if (nodeid == null)
            return null;
        if (m_bridgeportsOnSegment.isEmpty()) {
            for (BridgeBridgeLink link: m_bridgeportsOnLink) {
                if (link.getNode().getId().intValue() == nodeid.intValue() )
                    return link.getBridgePort();
                if (link.getDesignatedNode().getId().intValue() == nodeid.intValue() )
                    return link.getDesignatedPort() ;
            }
            return null;
        }
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (link.getNode().getId().intValue() == nodeid.intValue()) 
                return link.getBridgePort();
        }
        return null;
    }

    
}
