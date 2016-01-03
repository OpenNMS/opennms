package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;

public class SharedSegment {
    
    Integer m_designatedBridge;
    Integer m_designatedPort;
    List<BridgeMacLink> m_bridgeportsOnSegment = new ArrayList<BridgeMacLink>();
    List<BridgeBridgeLink> m_bridgeportsOnLink = new ArrayList<BridgeBridgeLink>();
    
    public SharedSegment() {
        
    }
    
    public SharedSegment(BridgeMacLink link) {
        m_designatedBridge = link.getNode().getId();
        m_designatedPort = link.getBridgePort();
        m_bridgeportsOnSegment.add(link);
    }

    public SharedSegment(Integer designatedBridge,Integer designatedPort) {
        m_designatedBridge=designatedBridge;
        m_designatedPort = designatedPort;
    }
    
    
    public void setDesignatedBridge(Integer designatedBridge) {
        m_designatedBridge = designatedBridge;
        m_designatedPort = getPortForBridge(designatedBridge);
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

        if (noMacsOnSegment() && shared.noMacsOnSegment()) {
            List<BridgeBridgeLink> links = new ArrayList<BridgeBridgeLink>();
            BridgeBridgeLink top=null;
            BridgeBridgeLink bottom=null;
            for (BridgeBridgeLink tlink: getBridgeBridgeLinks()) {
                if (tlink.getNode().getId() == bridgeId || tlink.getDesignatedNode().getId() == bridgeId) {
                    top = tlink;
                    continue;
                }
                links.add(tlink);
            }
            for (BridgeBridgeLink slink: shared.getBridgeBridgeLinks()) {
                if (slink.getNode().getId() == bridgeId || slink.getDesignatedNode().getId() == bridgeId) {
                    bottom = slink;
                    continue;
                }
                links.add(slink);
            }
            
            if (bottom != null && top != null) {
                BridgeBridgeLink dlink = new BridgeBridgeLink();
                if (top.getNode().getId() == bridgeId) {
                    dlink.setNode(top.getDesignatedNode());
                    dlink.setBridgePort(top.getDesignatedPort());
                    dlink.setBridgePortIfIndex(top.getDesignatedPortIfIndex());
                    dlink.setBridgePortIfName(top.getDesignatedPortIfName());
                } else {
                    dlink.setNode(top.getNode());
                    dlink.setBridgePort(top.getBridgePort());
                    dlink.setBridgePortIfIndex(top.getBridgePortIfIndex());
                    dlink.setBridgePortIfName(top.getBridgePortIfName());
                }
                if (bottom.getNode().getId() == bridgeId) {
                    dlink.setDesignatedNode(top.getDesignatedNode());
                    dlink.setDesignatedPort(top.getDesignatedPort());
                    dlink.setDesignatedPortIfIndex(top.getDesignatedPortIfIndex());
                    dlink.setDesignatedPortIfName(top.getDesignatedPortIfName());
                } else {
                    dlink.setDesignatedNode(top.getNode());
                    dlink.setDesignatedPort(top.getBridgePort());
                    dlink.setDesignatedPortIfIndex(top.getBridgePortIfIndex());
                    dlink.setDesignatedPortIfName(top.getBridgePortIfName());
                }
                links.add(dlink);

            }
            m_bridgeportsOnLink = links;
            return;
        }

        List<BridgeMacLink> linkOnSegment = new ArrayList<BridgeMacLink>();
        // top has no mac and shared has mac
        if (noMacsOnSegment() && !shared.noMacsOnSegment()) {
            for (BridgeMacLink bottom: shared.getBridgeMacLinks()) {
                if (bottom.getNode().getId() == bridgeId)
                    continue;
                linkOnSegment.add(bottom);
            }
            for (String mac: shared.getMacsOnSegment()) {
                for (BridgeBridgeLink top: getBridgeBridgeLinks()) {
                    if (top.getNode().getId() != bridgeId) {
                        BridgeMacLink nlink= new BridgeMacLink();
                        nlink.setNode(top.getNode());
                        nlink.setBridgePort(top.getBridgePort());
                        nlink.setBridgePortIfIndex(top.getBridgePortIfIndex());
                        nlink.setBridgePortIfName(top.getBridgePortIfName());
                        nlink.setVlan(top.getVlan());
                        nlink.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                        nlink.setMacAddress(mac);
                        linkOnSegment.add(nlink);
                    }
                    if (top.getDesignatedNode().getId() != bridgeId) {
                        BridgeMacLink nlink= new BridgeMacLink();
                        nlink.setNode(top.getDesignatedNode());
                        nlink.setBridgePort(top.getDesignatedPort());
                        nlink.setBridgePortIfIndex(top.getDesignatedPortIfIndex());
                        nlink.setBridgePortIfName(top.getBridgePortIfName());
                        nlink.setVlan(top.getVlan());
                        nlink.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                        nlink.setMacAddress(mac);
                        linkOnSegment.add(nlink);
                    }
                }
            }
            m_bridgeportsOnSegment=linkOnSegment;
            m_bridgeportsOnLink.clear();
            return;
        }
        
        // top has mac and shared has no mac
        if (!noMacsOnSegment() && shared.noMacsOnSegment()) {
            for (BridgeMacLink top: getBridgeMacLinks()) {
                if (top.getNode().getId() == bridgeId)
                    continue;
                linkOnSegment.add(top);
            }
            for (String mac: shared.getMacsOnSegment()) {
                for (BridgeBridgeLink bottom: shared.getBridgeBridgeLinks()) {
                    if (bottom.getNode().getId() != bridgeId) {
                        BridgeMacLink nlink= new BridgeMacLink();
                        nlink.setNode(bottom.getNode());
                        nlink.setBridgePort(bottom.getBridgePort());
                        nlink.setBridgePortIfIndex(bottom.getBridgePortIfIndex());
                        nlink.setBridgePortIfName(bottom.getBridgePortIfName());
                        nlink.setVlan(bottom.getVlan());
                        nlink.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                        nlink.setMacAddress(mac);
                        linkOnSegment.add(nlink);
                    }
                    if (bottom.getDesignatedNode().getId() != bridgeId) {
                        BridgeMacLink nlink= new BridgeMacLink();
                        nlink.setNode(bottom.getDesignatedNode());
                        nlink.setBridgePort(bottom.getDesignatedPort());
                        nlink.setBridgePortIfIndex(bottom.getDesignatedPortIfIndex());
                        nlink.setBridgePortIfName(bottom.getBridgePortIfName());
                        nlink.setVlan(bottom.getVlan());
                        nlink.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                        nlink.setMacAddress(mac);
                        linkOnSegment.add(nlink);
                    }
                }
            }
            m_bridgeportsOnSegment=linkOnSegment;
            m_bridgeportsOnLink.clear();
            return;
        }

        for (String mac: shared.getMacsOnSegment()) {
            Set<Integer> parsed = new HashSet<Integer>();
            parsed.add(bridgeId);
            for (BridgeMacLink link: getBridgeMacLinks()) {
                if (link.getNode().getId() != bridgeId)
                    linkOnSegment.add(link);
                if (parsed.contains(link.getNode().getId()))
                    continue;
                parsed.add(link.getNode().getId());
                BridgeMacLink nlink= new BridgeMacLink();
                nlink.setNode(link.getNode());
                nlink.setBridgePort(link.getBridgePort());
                nlink.setBridgePortIfIndex(nlink.getBridgePortIfIndex());
                nlink.setBridgePortIfName(link.getBridgePortIfName());
                nlink.setVlan(link.getVlan());
                nlink.setBridgeDot1qTpFdbStatus(link.getBridgeDot1qTpFdbStatus());
                nlink.setMacAddress(mac);
                linkOnSegment.add(nlink);
            }
        }
        
        for (String mac: getMacsOnSegment()) {
            Set<Integer> parsed = new HashSet<Integer>();
            parsed.add(bridgeId);
            for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                if (link.getNode().getId() != bridgeId)
                    linkOnSegment.add(link);
                if (parsed.contains(link.getNode().getId()))
                    continue;
                parsed.add(link.getNode().getId());
                BridgeMacLink nlink= new BridgeMacLink();
                nlink.setNode(link.getNode());
                nlink.setBridgePort(link.getBridgePort());
                nlink.setBridgePortIfIndex(nlink.getBridgePortIfIndex());
                nlink.setBridgePortIfName(link.getBridgePortIfName());
                nlink.setVlan(link.getVlan());
                nlink.setBridgeDot1qTpFdbStatus(link.getBridgeDot1qTpFdbStatus());
                nlink.setMacAddress(mac);
                linkOnSegment.add(nlink);
            }
        }
        
        m_bridgeportsOnSegment = linkOnSegment;
        
        

    }

    public void assign(List<BridgeMacLink> links, BridgeBridgeLink dlink) {
        if (isEmpty()) {
            if (links.isEmpty())
                m_bridgeportsOnLink.add(dlink);
            else
                m_bridgeportsOnSegment = links;
            return;
        }
        // if there are no macs on segment...just add the BridgeBridgeLink
        // only one port and one node for shared is allowed
        // so we should create a segment based on this
        //
        if (noMacsOnSegment() && links.isEmpty()) {
            BridgeBridgeLink first = m_bridgeportsOnLink.iterator().next();
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
            m_bridgeportsOnLink.add(x);
            m_bridgeportsOnLink.add(y);
            return;
        }
        
        // we are assigning links to a segment that has mac address
        // the set of link must be ...merged using mac address, 
        // this means that only incoming macs 
        // must be saved to the segment
        // intersection is null
        // we need to convert all the 
        // local links to BridgeBridgeLink
        // and add the BridgeBridgeLink
        if (links.isEmpty()) {
            convertSegmentToLink();
            m_bridgeportsOnLink.add(dlink);
            return;
        }
        
        List<BridgeMacLink> sharedsegmentmaclinks = new ArrayList<BridgeMacLink>();
        Set<Integer> nodeidadding = new HashSet<Integer>();
        for (BridgeMacLink link: links) {
            if (containsMac(link.getMacAddress()))
                sharedsegmentmaclinks.add(link);
            nodeidadding.add(link.getNode().getId());
        }

        //intersection is not null, then we have to add all the BridgeMacLink
        // for each mac address
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (nodeidadding.contains(link.getNode().getId()))
                    continue;
                sharedsegmentmaclinks.add(link);
        }

        m_bridgeportsOnSegment = sharedsegmentmaclinks;
        m_bridgeportsOnLink.clear();
    }

    private void convertSegmentToLink() {
        Set<Integer> parsed = new HashSet<Integer>();
        BridgeMacLink first = m_bridgeportsOnSegment.iterator().next();
        parsed.add(first.getNode().getId());
        for (BridgeMacLink linkB: m_bridgeportsOnSegment) {
            if (parsed.contains(linkB.getNode().getId()))
                    continue;
            parsed.add(linkB.getNode().getId());

            BridgeBridgeLink dlink = new BridgeBridgeLink();
            dlink.setNode(first.getNode());
            dlink.setBridgePort(first.getBridgePort());
            dlink.setBridgePortIfIndex(first.getBridgePortIfIndex());
            dlink.setBridgePortIfName(first.getBridgePortIfName());
            dlink.setVlan(first.getVlan());
            
            dlink.setDesignatedNode(linkB.getNode());
            dlink.setDesignatedPort(linkB.getBridgePort());
            dlink.setDesignatedPortIfIndex(linkB.getBridgePortIfIndex());
            dlink.setDesignatedPortIfName(linkB.getBridgePortIfName());
            dlink.setDesignatedVlan(linkB.getVlan());
            
            m_bridgeportsOnLink.add(dlink);            
        }
        m_bridgeportsOnSegment.clear();
    }

    public void removeBridge(int bridgeId) {
        if (noMacsOnSegment()) {
            List<BridgeBridgeLink> curlist = new ArrayList<BridgeBridgeLink>();
            BridgeBridgeLink first = m_bridgeportsOnLink.iterator().next();
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
    
    public void removeMacs(Map<Integer,List<BridgeMacLink>> throughset) {
        Set<String> mactoberemoved = new HashSet<String>();
        for (Integer port: throughset.keySet()) {
            for (BridgeMacLink link: throughset.get(port))
                mactoberemoved.add(link.getMacAddress());
        }

        if (mactoberemoved.containsAll(getMacsOnSegment())) {
            convertSegmentToLink();
            return;
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
                if (link.getNode().getId().intValue() == nodeid.intValue() && 
                        link.getBridgePort().intValue() == bridgeport.intValue())
                    return true;
                if (link.getDesignatedNode().getId().intValue() == nodeid.intValue() 
                        && link.getDesignatedPort().intValue() == bridgeport.intValue())
                    return true;
            }
            return false;
        }
        for (BridgeMacLink link: m_bridgeportsOnSegment) {
            if (link.getNode().getId().intValue() == nodeid.intValue() 
                    && link.getBridgePort().intValue() == bridgeport.intValue()) 
                return true;
        }
        return false;
    }
    
    public Integer getPortForBridge(Integer nodeid) {
        if (noMacsOnSegment()) {
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
