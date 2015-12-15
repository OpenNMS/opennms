package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;

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
        // if there is not yet links on segment add it
        if (isEmpty() && !links.isEmpty()) {
            m_bridgeportsOnSegment = links;
            return;
        }
        // if there are no macs on segment...just add the BridgeBridgeLink
        if (noMacsOnSegment() && dlink != null) {
            m_bridgeportsOnLink.add(dlink);
            return;
        }
        
        // we are assigning links to a segment that has mac address
        // the set of link must be ...merged using mac address, 
        // this means that
        // only intersection of common macs is
        // must be saved to segment
        
        Set<String> macsonLinks=new HashSet<String>();
        for (BridgeMacLink link: links)
            macsonLinks.add(link.getMacAddress());
        
        Set<String> retained = new HashSet<String>();
        retained.addAll(getMacsOnSegment());
        retained.retainAll(macsonLinks);
        // intersection is null
        // we need to convert all the 
        // local links to BridgeBridgeLink
        // and add the BridgeBridgeLink
        if (retained.isEmpty()) {
            convertSegmentToLink();
            m_bridgeportsOnLink.add(dlink);
            m_bridgeportsOnSegment.clear();
            return;
        }
        
        //intersection is not null, then we have to add all the BridgeMacLink
        // for each mac address
        List<BridgeMacLink> linksonsegment = new ArrayList<BridgeMacLink>();
        for (String mac: retained) {
            for (BridgeMacLink link: m_bridgeportsOnSegment) {
                if (mac.equals(link.getMacAddress())) 
                    linksonsegment.add(link);
            }
            for (BridgeMacLink link: links) {
                if (mac.equals(link.getMacAddress())) 
                    linksonsegment.add(link);
            }
        }
        m_bridgeportsOnSegment = linksonsegment;
        m_bridgeportsOnLink.clear();
    }

    private void convertSegmentToLink() {
        Set<Integer> parsed = new HashSet<Integer>();
        for (BridgeMacLink linkA: m_bridgeportsOnSegment) {
            if (parsed.contains(linkA.getNode().getId()))
                    continue;
            parsed.add(linkA.getNode().getId());
            for (BridgeMacLink linkB: m_bridgeportsOnSegment) {
                if (parsed.contains(linkB.getNode().getId()))
                        continue;
                parsed.add(linkB.getNode().getId());

                BridgeBridgeLink dlink = new BridgeBridgeLink();
                dlink.setNode(linkA.getNode());
                dlink.setBridgePort(linkA.getBridgePort());
                dlink.setBridgePortIfIndex(linkA.getBridgePortIfIndex());
                dlink.setBridgePortIfName(linkA.getBridgePortIfName());
                dlink.setVlan(linkA.getVlan());
                
                dlink.setDesignatedNode(linkB.getNode());
                dlink.setDesignatedPort(linkB.getBridgePort());
                dlink.setDesignatedPortIfIndex(linkB.getBridgePortIfIndex());
                dlink.setDesignatedPortIfName(linkB.getBridgePortIfName());
                dlink.setDesignatedVlan(linkB.getVlan());
                
                m_bridgeportsOnLink.add(dlink);
            }
            
        }
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
