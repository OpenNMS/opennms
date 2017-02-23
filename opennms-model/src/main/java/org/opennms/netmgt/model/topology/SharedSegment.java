package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;

public class SharedSegment {
    
    BridgePort m_designatedBridge;
    List<BridgeMacLink> m_macLinksOnSegment = new ArrayList<BridgeMacLink>();
    Set<BridgePort> m_portsOnSegment = new HashSet<BridgePort>();
    BroadcastDomain m_domain;

    // Indexes used to ensure calls to containsMac() and containsPort() are quick
    Set<String> m_macAddressesOnSegment = new HashSet<>();
    Map<Integer, Set<Integer>> m_bridgePortsByNodeId = new HashMap<>();

    private BridgePort getFromBridgeMacLink(BridgeMacLink link) {
        BridgePort bp = new BridgePort();
        bp.setNode(link.getNode());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setBridgePortIfName(link.getBridgePortIfName());
        bp.setVlan(link.getVlan());
        bp.setCreateTime(link.getBridgeMacLinkCreateTime());
        bp.setPollTime(link.getBridgeMacLinkLastPollTime());
        return bp;
    }

    private BridgePort getFromBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNode(link.getNode());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setBridgePortIfName(link.getBridgePortIfName());
        bp.setVlan(link.getVlan());
        bp.setCreateTime(link.getBridgeBridgeLinkCreateTime());
        bp.setPollTime(link.getBridgeBridgeLinkLastPollTime());
        return bp;
    }

    private BridgePort getFromDesignatedBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNode(link.getDesignatedNode());
        bp.setBridgePort(link.getDesignatedPort());
        bp.setBridgePortIfIndex(link.getDesignatedPortIfIndex());
        bp.setBridgePortIfName(link.getDesignatedPortIfName());
        bp.setVlan(link.getDesignatedVlan());
        bp.setCreateTime(link.getBridgeBridgeLinkCreateTime());
        bp.setPollTime(link.getBridgeBridgeLinkLastPollTime());
        return bp;
    }

    private BridgeBridgeLink getBridgeBridgeLink(BridgePort bp) {
        BridgeBridgeLink link = new BridgeBridgeLink();
        link.setNode(bp.getNode());
        link.setBridgePort(bp.getBridgePort());
        link.setBridgePortIfIndex(bp.getBridgePortIfIndex());
        link.setBridgePortIfName(bp.getBridgePortIfName());
        link.setVlan(bp.getVlan());
        link.setDesignatedNode(m_designatedBridge.getNode());
        link.setDesignatedPort(m_designatedBridge.getBridgePort());
        link.setDesignatedPortIfIndex(m_designatedBridge.getBridgePortIfIndex());
        link.setDesignatedPortIfName(m_designatedBridge.getBridgePortIfName());
        link.setDesignatedVlan(m_designatedBridge.getVlan());
        link.setBridgeBridgeLinkCreateTime(m_designatedBridge.getCreateTime());
        link.setBridgeBridgeLinkLastPollTime(m_designatedBridge.getPollTime());
        return link;
    }
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
        m_designatedBridge = getFromBridgeMacLink(link);
        m_macLinksOnSegment.add(link);
        m_portsOnSegment.add(m_designatedBridge);
        indexMacLinks();
        indexBridgePorts();
    }

    public SharedSegment(BroadcastDomain domain, BridgeBridgeLink link) {
        m_domain =domain;
        m_portsOnSegment.add(getFromDesignatedBridgeBridgeLink(link));
        m_portsOnSegment.add(getFromBridgeBridgeLink(link));
        indexBridgePorts();
    }
        
    public void setDesignatedBridge(Integer designatedBridge) {
        if (designatedBridge == null)
            return;
        if (m_designatedBridge != null && designatedBridge != null 
                && m_designatedBridge.getNode().getId() == designatedBridge.intValue())
            return;
        for (BridgePort port: m_portsOnSegment) {
            if (port.getNode().getId().intValue() == designatedBridge.intValue()) {
                m_designatedBridge = port;
                break;
            }
        }
    }

    public Integer getDesignatedBridge() {
        return m_designatedBridge.getNode().getId();
    }


    public Integer getDesignatedPort() {
        return m_designatedBridge.getBridgePort();
    }


    public boolean isEmpty() {
        return m_macLinksOnSegment.isEmpty() && m_portsOnSegment.isEmpty();
    }

    public Set<BridgePort> getBridgePortsOnSegment() {
        return m_portsOnSegment;
    }        

    public List<BridgeBridgeLink> getBridgeBridgeLinks() {
        List<BridgeBridgeLink> links = new ArrayList<BridgeBridgeLink>();
        for (BridgePort port: m_portsOnSegment) {
            if (port.equals(m_designatedBridge))
                continue;
            links.add(getBridgeBridgeLink(port));
        }
        return links;
    }
    
    public List<BridgeMacLink> getBridgeMacLinks() {
        return m_macLinksOnSegment;
    }
    
    public boolean noMacsOnSegment() {
        return m_macLinksOnSegment.isEmpty();
    }

    public void setBridgeMacLinks(List<BridgeMacLink> links) {
        m_macLinksOnSegment = links;
        for (BridgeMacLink link: links) {
            m_portsOnSegment.add(getFromBridgeMacLink(link));
        }
        indexMacLinks();
        indexBridgePorts();
    }

    public void add(BridgeMacLink link) {
        final BridgePort port = getFromBridgeMacLink(link);
        m_macLinksOnSegment.add(link);
        m_portsOnSegment.add(port);
        indexMacLink(link);
        indexBridgePort(port);
    }

    public void add(BridgeBridgeLink dlink) {
        final BridgePort designated = getFromDesignatedBridgeBridgeLink(dlink);
        final BridgePort bridge = getFromBridgeBridgeLink(dlink);
        m_portsOnSegment.add(designated);
        m_portsOnSegment.add(bridge);
        indexBridgePort(designated);
        indexBridgePort(bridge);
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
        List<BridgeMacLink> toadd = new ArrayList<BridgeMacLink>();
        for (BridgeMacLink link: m_macLinksOnSegment) {
            if (link.getNode().getId().intValue() != m_designatedBridge.getNode().getId().intValue())
                continue;
            for (BridgePort mport: shared.getBridgePortsOnSegment()) {
                if (mport.getNode().getId().intValue() == bridgeId.intValue())
                    continue;
                BridgeMacLink nlink = new BridgeMacLink();
                nlink.setNode(mport.getNode());
                nlink.setBridgePort(mport.getBridgePort());
                nlink.setBridgePortIfIndex(mport.getBridgePortIfIndex());
                nlink.setBridgePortIfName(mport.getBridgePortIfName());
                nlink.setVlan(mport.getVlan());
                nlink.setMacAddress(link.getMacAddress());
                nlink.setBridgeMacLinkCreateTime(link.getBridgeMacLinkCreateTime());
                nlink.setBridgeMacLinkLastPollTime(link.getBridgeMacLinkLastPollTime());
                nlink.setBridgeDot1qTpFdbStatus(link.getBridgeDot1qTpFdbStatus());
                toadd.add(nlink);
            }
        }
        m_macLinksOnSegment.addAll(toadd);
        
        for (BridgeMacLink mlink: shared.getBridgeMacLinks()) {
            if (mlink.getNode().getId().intValue() == bridgeId.intValue()) {
                for (BridgePort port : m_portsOnSegment) {
                    BridgeMacLink nlink = new BridgeMacLink();
                    nlink.setNode(port.getNode());
                    nlink.setBridgePort(port.getBridgePort());
                    nlink.setBridgePortIfIndex(port.getBridgePortIfIndex());
                    nlink.setBridgePortIfName(port.getBridgePortIfName());
                    nlink.setVlan(port.getVlan());
                    nlink.setMacAddress(mlink.getMacAddress());
                    nlink.setBridgeMacLinkCreateTime(mlink.getBridgeMacLinkCreateTime());
                    nlink.setBridgeMacLinkLastPollTime(mlink.getBridgeMacLinkLastPollTime());
                    nlink.setBridgeDot1qTpFdbStatus(mlink.getBridgeDot1qTpFdbStatus());
                    m_macLinksOnSegment.add(nlink);
                }
                continue;
            } 
            m_macLinksOnSegment.add(mlink);
        }
        for (BridgePort port: shared.getBridgePortsOnSegment()) {
            if (port.getNode().getId().intValue() == bridgeId.intValue())
                continue;
            m_portsOnSegment.add(port);
        }
        indexMacLinks();
        indexBridgePorts();
    }

    public void assign(List<BridgeMacLink> links, BridgeBridgeLink dlink) {
        
        add(dlink);
                
        Map<BridgeMacLinkHash,BridgeMacLink> sharedsegmentmaclinks = new HashMap<BridgeMacLinkHash,BridgeMacLink>();
        for (BridgeMacLink link: links)
            sharedsegmentmaclinks.put(new BridgeMacLinkHash(link),link);
        //intersection is not null, then we have to add all the BridgeMacLink
        // for each mac address
        for (BridgeMacLink link: m_macLinksOnSegment) {
            sharedsegmentmaclinks.put(new BridgeMacLinkHash(link),link);
            m_portsOnSegment.add(getFromBridgeMacLink(link));
        }
        m_macLinksOnSegment = new ArrayList<BridgeMacLink>(sharedsegmentmaclinks.values());
        indexMacLinks();
        indexBridgePorts();
    }

    public void removeBridge(int bridgeId) {
        if (m_portsOnSegment.isEmpty())
            return;
        Set<BridgePort> updateportsonsegment = new HashSet<BridgePort>();
        for (BridgePort port: m_portsOnSegment) {
            if (port.getNode().getId().intValue() == bridgeId)
                continue;
            updateportsonsegment.add(port);
        }
        m_portsOnSegment = updateportsonsegment;
        
        List<BridgeMacLink> updatemacsonsegment = new ArrayList<BridgeMacLink>();
        for (BridgeMacLink link: m_macLinksOnSegment) {
            if (link.getNode().getId().intValue() == bridgeId ) {
               continue;
            }
            updatemacsonsegment.add(link);
        }
        m_macLinksOnSegment = updatemacsonsegment;
        indexMacLinks();
    }
    
    public void removeMacs(Map<Integer,List<BridgeMacLink>> throughset) {
        Set<String> mactoberemoved = new HashSet<String>();
        for (Integer port: throughset.keySet()) {
            for (BridgeMacLink link: throughset.get(port))
                mactoberemoved.add(link.getMacAddress());
        }

        List<BridgeMacLink> curlist = new ArrayList<BridgeMacLink>();
        for (BridgeMacLink link: m_macLinksOnSegment) {
            if (mactoberemoved.contains(link.getMacAddress()))
                continue;
            curlist.add(link);
        }
        m_macLinksOnSegment=curlist;
        indexMacLinks();
    }
    
    public Integer getFirstNoDesignatedBridge() {
        for (Integer bridgeId: getBridgeIdsOnSegment()) {
            if (m_designatedBridge == null || bridgeId != m_designatedBridge.getNode().getId())
                return bridgeId;
        }
        return null;
    }

    public Set<String> getMacsOnSegment() {
        return m_macAddressesOnSegment;
    }

    public Set<Integer> getBridgeIdsOnSegment() {
        Set<Integer> nodes = new HashSet<Integer>();
        for (BridgePort link: m_portsOnSegment) {
            nodes.add(link.getNode().getId());
        }
        return nodes;
    }

    public boolean containsMac(String mac) {
        if (mac == null) {
            return false;
        }
        return m_macAddressesOnSegment.contains(mac);
    }

    public boolean containsPort(Integer nodeid, Integer bridgeport) {
        if (nodeid == null || bridgeport == null) {
            return false;
        }
        return m_bridgePortsByNodeId.getOrDefault(nodeid, Collections.emptySet()).contains(bridgeport);
    }

    public Integer getPortForBridge(Integer nodeid) {
        if (nodeid == null)
            return null;
        if (m_macLinksOnSegment.isEmpty()) {
            for (BridgePort link: m_portsOnSegment) {
                if (link.getNode().getId().intValue() == nodeid.intValue() )
                    return link.getBridgePort();
            }
            return null;
        }
        return null;
    }

    /**
     * Indexes all of the mac links.
     */
    private void indexMacLinks() {
        m_macAddressesOnSegment.clear();
        for (BridgeMacLink macLink : m_macLinksOnSegment) {
            indexMacLink(macLink);
        }
    }

    private void indexMacLink(BridgeMacLink macLink) {
        m_macAddressesOnSegment.add(macLink.getMacAddress());
    }

    /**
     * Indexes all of the bridge ports.
     */
    private void indexBridgePorts() {
        m_bridgePortsByNodeId.clear();
        for (BridgePort bridgePort: m_portsOnSegment) {
            indexBridgePort(bridgePort);
        }
    }

    private void indexBridgePort(BridgePort bridgePort) {
        final int nodeId = bridgePort.getNode().getId();
        Set<Integer> bridgePortsOnNode = m_bridgePortsByNodeId.get(nodeId);
        if (bridgePortsOnNode == null) {
            bridgePortsOnNode = new HashSet<>();
            m_bridgePortsByNodeId.put(nodeId, bridgePortsOnNode);
        }
        bridgePortsOnNode.add(bridgePort.getBridgePort().intValue());
    }
}
