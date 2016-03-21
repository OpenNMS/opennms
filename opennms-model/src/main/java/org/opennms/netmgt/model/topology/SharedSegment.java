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
    List<BridgeMacLink> m_macsOnSegment = new ArrayList<BridgeMacLink>();
    List<BridgeBridgeLink> m_portsOnSegment = new ArrayList<BridgeBridgeLink>();
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
        m_macsOnSegment.add(link);
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
        return m_macsOnSegment.isEmpty() && m_portsOnSegment.isEmpty();
    }

    public List<BridgeBridgeLink> getBridgeBridgeLinks() {
        return m_portsOnSegment;
    }
    
    public List<BridgeMacLink> getBridgeMacLinks() {
        return m_macsOnSegment;
    }
    
    public boolean noMacsOnSegment() {
        return m_macsOnSegment.isEmpty();
    }

    public void setBridgeMacLinks(List<BridgeMacLink> links) {
        m_macsOnSegment = links;
    }

    public void add(BridgeMacLink link) {
        m_macsOnSegment.add(link);
    }
    
    public void add(BridgeBridgeLink dlink) {
        if (m_portsOnSegment.isEmpty()) {
            m_portsOnSegment.add(dlink);
            return;
        } 
        BridgeBridgeLink first= m_portsOnSegment.iterator().next();
        if (first.getDesignatedNode().getId().intValue() == dlink.getDesignatedNode().getId().intValue()) {
            m_portsOnSegment.add(dlink);
            return;
        }
        if (first.getDesignatedNode().getId().intValue() == dlink.getNode().getId().intValue()) {
            m_portsOnSegment.add(dlink.getReverseBridgeBridgeLink());
            return;
        }
        
        BridgeBridgeLink x = new BridgeBridgeLink();

        x.setNode(dlink.getNode());
        x.setBridgePort(dlink.getBridgePort());
        x.setBridgePortIfIndex(dlink.getBridgePortIfIndex());
        x.setBridgePortIfName(dlink.getBridgePortIfName());
        x.setVlan(dlink.getVlan());

        x.setDesignatedNode(first.getDesignatedNode());
        x.setDesignatedPort(first.getDesignatedPort());
        x.setDesignatedPortIfIndex(first.getDesignatedPortIfIndex());
        x.setDesignatedPortIfName(first.getDesignatedPortIfName());
        x.setVlan(first.getDesignatedVlan());
        
        m_portsOnSegment.add(x);
        
        BridgeBridgeLink y = new BridgeBridgeLink();
        x.setNode(dlink.getDesignatedNode());
        x.setBridgePort(dlink.getDesignatedPort());
        x.setBridgePortIfIndex(dlink.getDesignatedPortIfIndex());
        x.setBridgePortIfName(dlink.getDesignatedPortIfName());
        x.setVlan(dlink.getDesignatedVlan());
            
        y.setDesignatedNode(first.getDesignatedNode());
        y.setDesignatedPort(first.getDesignatedPort());
        y.setDesignatedPortIfIndex(first.getDesignatedPortIfIndex());
        y.setDesignatedPortIfName(first.getDesignatedPortIfName());
        y.setVlan(first.getDesignatedVlan());
        m_portsOnSegment.add(y);
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
        // if there is a single port on the segment
        BridgeBridgeLink first = m_portsOnSegment.iterator().next();
        if (m_macsOnSegment.isEmpty() && m_portsOnSegment.size() == 1 && first.getNode().getId().intValue() == 
                first.getDesignatedNode().getId().intValue()) {
            
            if (shared.getBridgeBridgeLinks().isEmpty()) {
                for (BridgeMacLink mlink: shared.getBridgeMacLinks()) {
                    mlink.setNode(first.getDesignatedNode());
                    mlink.setBridgePort(first.getDesignatedPort());
                    mlink.setBridgePortIfIndex(first.getDesignatedPortIfIndex());
                    mlink.setBridgePortIfName(first.getDesignatedPortIfName());
                    mlink.setVlan(first.getDesignatedVlan());
                    m_macsOnSegment.add(mlink);
                }
                m_portsOnSegment.clear();
                return;
            } 
            
            BridgeBridgeLink sharedFirst = shared.getBridgeBridgeLinks().iterator().next();
            
            if (sharedFirst.getDesignatedNode().getId().intValue() == bridgeId.intValue()) {
                m_portsOnSegment.clear();
            } else {
                first.setNode(sharedFirst.getDesignatedNode());
                first.setBridgePort(sharedFirst.getDesignatedPort());
                first.setBridgePortIfIndex(sharedFirst.getDesignatedPortIfIndex());
                first.setBridgePortIfName(sharedFirst.getDesignatedPortIfName());
                first.setVlan(sharedFirst.getDesignatedVlan());
            }
            for (BridgeBridgeLink blink : shared.getBridgeBridgeLinks()) {
                if (blink.getNode().getId().intValue() == bridgeId.intValue())
                    continue;
                blink.setDesignatedNode(first.getDesignatedNode());
                blink.setDesignatedPort(first.getDesignatedPort());
                blink.setDesignatedPortIfIndex(first.getDesignatedPortIfIndex());
                blink.setDesignatedPortIfName(first.getDesignatedPortIfName());
                blink.setDesignatedVlan(first.getDesignatedVlan());
                m_portsOnSegment.add(blink);
            }

            for (BridgeMacLink mlink: shared.getBridgeMacLinks()) {
                if (mlink.getNode().getId().intValue() == bridgeId.intValue()) {
                    mlink.setNode(first.getDesignatedNode());
                    mlink.setBridgePort(first.getDesignatedPort());
                    mlink.setBridgePortIfIndex(first.getDesignatedPortIfIndex());
                    mlink.setBridgePortIfName(first.getDesignatedPortIfName());
                    mlink.setVlan(first.getDesignatedVlan());
                } 
                m_macsOnSegment.add(mlink);    
            }
            return;            
        }

        
        for (BridgeMacLink mlink: shared.getBridgeMacLinks()) {
            if (mlink.getNode().getId().intValue() == bridgeId.intValue()) {
                mlink.setNode(first.getDesignatedNode());
                mlink.setBridgePort(first.getDesignatedPort());
                mlink.setBridgePortIfIndex(first.getDesignatedPortIfIndex());
                mlink.setBridgePortIfName(first.getDesignatedPortIfName());
                mlink.setVlan(first.getDesignatedVlan());
            }
            m_macsOnSegment.add(mlink);
        }            

        if (shared.getBridgeBridgeLinks().isEmpty())
            return;
        
        BridgeBridgeLink sharedFirst = shared.getBridgeBridgeLinks().iterator().next();
        
        if (sharedFirst.getDesignatedNode().getId().intValue() == bridgeId.intValue()) {
            for (BridgeBridgeLink blink : shared.getBridgeBridgeLinks()) {
                blink.setDesignatedNode(first.getDesignatedNode());
                blink.setDesignatedPort(first.getDesignatedPort());
                blink.setDesignatedPortIfIndex(first.getDesignatedPortIfIndex());
                blink.setDesignatedPortIfName(first.getDesignatedPortIfName());
                blink.setDesignatedVlan(first.getDesignatedVlan());
                m_portsOnSegment.add(blink);
            }
            return;
        } 
        
        BridgeBridgeLink nbblink = new BridgeBridgeLink();
        nbblink.setNode(sharedFirst.getDesignatedNode());
        nbblink.setBridgePort(sharedFirst.getDesignatedPort());
        nbblink.setBridgePortIfIndex(sharedFirst.getDesignatedPortIfIndex());
        nbblink.setBridgePortIfName(sharedFirst.getDesignatedPortIfName());
        nbblink.setVlan(sharedFirst.getDesignatedVlan());
        nbblink.setDesignatedNode(first.getDesignatedNode());
        nbblink.setDesignatedPort(first.getDesignatedPort());
        nbblink.setDesignatedPortIfIndex(first.getDesignatedPortIfIndex());
        nbblink.setDesignatedPortIfName(first.getDesignatedPortIfName());
        nbblink.setDesignatedVlan(first.getDesignatedVlan());
        m_portsOnSegment.add(nbblink);
        
        for (BridgeBridgeLink blink : shared.getBridgeBridgeLinks()) {
            if (blink.getNode().getId().intValue() == bridgeId.intValue())
                continue;
            blink.setDesignatedNode(first.getDesignatedNode());
            blink.setDesignatedPort(first.getDesignatedPort());
            blink.setDesignatedPortIfIndex(first.getDesignatedPortIfIndex());
            blink.setDesignatedPortIfName(first.getDesignatedPortIfName());
            blink.setDesignatedVlan(first.getDesignatedVlan());
            m_portsOnSegment.add(blink);
        }

    }

    public void assign(List<BridgeMacLink> links, BridgeBridgeLink dlink) {
        
        add(dlink);
                
        Map<BridgeMacLinkHash,BridgeMacLink> sharedsegmentmaclinks = new HashMap<BridgeMacLinkHash,BridgeMacLink>();
        for (BridgeMacLink link: links)
            sharedsegmentmaclinks.put(new BridgeMacLinkHash(link),link);
        //intersection is not null, then we have to add all the BridgeMacLink
        // for each mac address
        for (BridgeMacLink link: m_macsOnSegment)
            sharedsegmentmaclinks.put(new BridgeMacLinkHash(link),link);

        m_macsOnSegment = new ArrayList<BridgeMacLink>(sharedsegmentmaclinks.values());
    }

    public void removeBridge(int bridgeId) {
        List<BridgeMacLink> updatemacsonsegment = new ArrayList<BridgeMacLink>();
        List<BridgeBridgeLink> updateportonsegment = new ArrayList<BridgeBridgeLink>();
        List<BridgeBridgeLink> deleteportonsegment = new ArrayList<BridgeBridgeLink>();
        BridgeBridgeLink first = m_portsOnSegment.iterator().next();
        if (!m_portsOnSegment.isEmpty()) {
            if (first.getDesignatedNode().getId().intValue() == bridgeId) {
                for (BridgeBridgeLink link: m_portsOnSegment) {
                    link.setDesignatedNode(first.getNode());
                    link.setDesignatedPort(first.getBridgePort());
                    link.setDesignatedPortIfIndex(first.getBridgePortIfIndex());
                    link.setDesignatedPortIfName(first.getBridgePortIfName());
                    link.setDesignatedVlan(first.getVlan());
                    if (link.getNode().getId() == first.getNode().getId()) {
                        deleteportonsegment.add(link);
                        continue;
                    }
                    updateportonsegment.add(link);
                }
            } else {
                for (BridgeBridgeLink link: m_portsOnSegment) {
                    if (link.getNode().getId().intValue() == bridgeId ) {
                        deleteportonsegment.add(link);
                        continue;
                    }
                    updateportonsegment.add(link);
                }
            }
        }

        if (updateportonsegment.isEmpty() && m_macsOnSegment.isEmpty()) {
            m_portsOnSegment=deleteportonsegment;
            return;
        }
        m_portsOnSegment = updateportonsegment;

        for (BridgeMacLink link: m_macsOnSegment) {
            if (link.getNode().getId().intValue() == bridgeId ) {
               continue;
            }
            updatemacsonsegment.add(link);
        }
        m_macsOnSegment = updatemacsonsegment;
        
    }
    
    public void removeMacs(Map<Integer,List<BridgeMacLink>> throughset) {
        Set<String> mactoberemoved = new HashSet<String>();
        for (Integer port: throughset.keySet()) {
            for (BridgeMacLink link: throughset.get(port))
                mactoberemoved.add(link.getMacAddress());
        }

        List<BridgeMacLink> curlist = new ArrayList<BridgeMacLink>();
        for (BridgeMacLink link: m_macsOnSegment) {
            if (mactoberemoved.contains(link.getMacAddress()))
                continue;
            curlist.add(link);
        }
        m_macsOnSegment=curlist;
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
            for (BridgeMacLink link: m_macsOnSegment)
                macs.add(link.getMacAddress());
        return macs;

    }

    public Set<Integer> getBridgeIdsOnSegment() {
        Set<Integer> nodes = new HashSet<Integer>();
        for (BridgeBridgeLink link: m_portsOnSegment) {
            nodes.add(link.getNode().getId());
            nodes.add(link.getDesignatedNode().getId());
        }
        for ( BridgeMacLink link: m_macsOnSegment) {
            nodes.add(link.getNode().getId());
        }
        return nodes;
    }

    public boolean containsMac(String mac) {
        if ( mac == null) 
            return false;
        for (BridgeMacLink link: m_macsOnSegment) {
            if (mac.equals(link.getMacAddress()))
                return true;
        }
        return false;
    }

    public boolean containsPort(Integer nodeid, Integer bridgeport) {
        for (BridgeBridgeLink link: m_portsOnSegment) {
            if (link.getNode().getId().intValue() == nodeid.intValue() && 
                    link.getBridgePort().intValue() == bridgeport.intValue())
                return true;
            if (link.getDesignatedNode().getId().intValue() == nodeid.intValue() 
                    && link.getDesignatedPort().intValue() == bridgeport.intValue())
                return true;
        }
        for (BridgeMacLink link: m_macsOnSegment) {
            if (link.getNode().getId().intValue() == nodeid.intValue() 
                    && link.getBridgePort().intValue() == bridgeport.intValue()) 
                return true;
        }
        return false;
    }
    
    public Integer getPortForBridge(Integer nodeid) {
        if (nodeid == null)
            return null;
        if (m_macsOnSegment.isEmpty()) {
            for (BridgeBridgeLink link: m_portsOnSegment) {
                if (link.getNode().getId().intValue() == nodeid.intValue() )
                    return link.getBridgePort();
                if (link.getDesignatedNode().getId().intValue() == nodeid.intValue() )
                    return link.getDesignatedPort() ;
            }
            return null;
        }
        for (BridgeMacLink link: m_macsOnSegment) {
            if (link.getNode().getId().intValue() == nodeid.intValue()) 
                return link.getBridgePort();
        }
        return null;
    }

    
}
