package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.OnmsNode;

public class BroadcastDomain {

    public class BridgeMacLinkHash {

        final Integer nodeid;
        final Integer bridgeport;
        final String mac;
        public BridgeMacLinkHash(BridgeMacLink maclink) {
            super();
            nodeid = maclink.getNode().getId();
            bridgeport = maclink.getBridgePort();
            mac = maclink.getMacAddress();
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result
                    + ((bridgeport == null) ? 0 : bridgeport.hashCode());
            result = prime * result + ((mac == null) ? 0 : mac.hashCode());
            result = prime * result
                    + ((nodeid == null) ? 0 : nodeid.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BridgeMacLinkHash other = (BridgeMacLinkHash) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (bridgeport == null) {
                if (other.bridgeport != null)
                    return false;
            } else if (!bridgeport.equals(other.bridgeport))
                return false;
            if (mac == null) {
                if (other.mac != null)
                    return false;
            } else if (!mac.equals(other.mac))
                return false;
            if (nodeid == null) {
                if (other.nodeid != null)
                    return false;
            } else if (!nodeid.equals(other.nodeid))
                return false;
            return true;
        }
        private BroadcastDomain getOuterType() {
            return BroadcastDomain.this;
        }
    }
    
    volatile Set<Bridge> m_bridges = new HashSet<Bridge>();

    volatile List<SharedSegment> m_topology = new ArrayList<SharedSegment>();    
    volatile Integer m_rootBridgeId;
    volatile List<BridgeMacLink> m_rootBridgeBFT = new ArrayList<BridgeMacLink>();
    
    volatile Map<Bridge, Set<BridgeMacLink>> m_notYetParsedBFTMap = new HashMap<Bridge, Set<BridgeMacLink>>();
    volatile List<BridgeStpLink> m_STPLinks = new ArrayList<BridgeStpLink>();
    
    boolean m_lock = false;

    public void clearTopology() {
        m_topology.clear();
    }
    
    public boolean isEmpty() {
        return m_bridges.isEmpty();
    }
        
    public boolean isCalculating() {
        return m_lock;
    }

    public boolean hasTopologyUpdatedBft() {
        return !m_notYetParsedBFTMap.isEmpty();
    }

    public Set<Integer> getBridgeNodesOnDomain() {
        Set<Integer> bridgeIds = new HashSet<Integer>();
        for (Bridge bridge: m_bridges) 
            bridgeIds.add(bridge.getId());
        return bridgeIds;
    }
    
    public synchronized void getLock() {
        m_lock = true;
    }

    public synchronized void releaseLock() {
        m_lock = false;
    }

    public Set<Bridge> getBridges() {
        return m_bridges;
    }
    
    public List<SharedSegment> getTopology() {
        return m_topology;
    }
        
    public Integer getRootBridgeId() {
        return m_rootBridgeId;
    }

    public void setRootBridgeId(Integer rootBridgeId) {
        m_rootBridgeId = rootBridgeId;
    }

    public List<BridgeMacLink> getRootBridgeBFT() {
        return m_rootBridgeBFT;
    }

    public void setRootBridgeBFT(List<BridgeMacLink> rootBridgeBFT) {
        m_rootBridgeBFT = rootBridgeBFT;
    }

    public Bridge getRootBridge() {
        for (Bridge bridge: m_bridges) {
            if (bridge.isRootBridge())
                return bridge;
        }
        return null;
    }

    public List<BridgeStpLink> getSTPLinks() {
        return m_STPLinks;
    }
        
    public Set<String> getMacsOnDomain() {
        Set<String>macs = new HashSet<String>();
        for (SharedSegment segment: m_topology) 
            macs.addAll(segment.getMacsOnSegment());
        return macs;
    }

    public void addBridgeElement(BridgeElement bridgeElement) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getId() == bridgeElement.getNode().getId()) {
                bridge.addBridgeElement(bridgeElement);
                return;
            }
        }
        m_bridges.add(new Bridge(bridgeElement));
    }

    public void add(SharedSegment segment) {
        m_topology.add(segment);
    }
    
    public void loadTopologyEntry(SharedSegment segment) {
        segment.setBroadcastDomain(this);
        m_topology.add(segment);
        for (Integer nodeId : segment.getBridgeIdsOnSegment()) {
            m_bridges.add(new Bridge(nodeId));
        }
    }
    
    public void addSTPEntry(BridgeStpLink stplink ) {
        m_STPLinks.add(stplink);
    }
    
    public boolean containsAtleastOne(Set<Integer> nodeids) {
        for (Bridge bridge: m_bridges) {
            for (Integer nodeid:nodeids) {
                if (bridge.getId().intValue() == nodeid.intValue())
                    return true;
            }
        }
        return false;
    }
    
    public boolean containBridgeId(Integer nodeid) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == nodeid.intValue())
                return true;
        }
        return false;
    }

    public void loadBFT(int nodeId, List<BridgeMacLink> maclinks,List<BridgeStpLink> stplinks, List<BridgeElement> elements) {
        Map<BridgeMacLinkHash,BridgeMacLink> effectiveBFT=new HashMap<BridgeMacLinkHash,BridgeMacLink>();
        for (BridgeMacLink maclink: maclinks) {
            effectiveBFT.put(new BridgeMacLinkHash(maclink), maclink);
        }
        Bridge added = null;
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == nodeId) {
                added = bridge;
            }
        }
        if (added == null) {
            added = new Bridge(nodeId);
            m_bridges.add(added);
        }
        m_notYetParsedBFTMap.put(added, new HashSet<BridgeMacLink>(effectiveBFT.values()));
        added.setBridgeElements(elements);

        if (stplinks != null && !stplinks.isEmpty()) {
            //remove all stp link in the list to let them
            // substituted with the new incoming list
            List<BridgeStpLink> allstplinks = new ArrayList<BridgeStpLink>();
            for (BridgeStpLink link: m_STPLinks) {
                if (link.getNode().getId().intValue() == nodeId)
                    continue;
                allstplinks.add(link);
            }
            allstplinks.addAll(stplinks);
            m_STPLinks=allstplinks;
        }        
    }
    
    public synchronized void removeBridge(int bridgeId) {
        Bridge bridge = null;
        for (Bridge curbridge: m_bridges) {
            if (curbridge.getId() == bridgeId) {
                bridge=curbridge;
                break;
            }
        }
        // if not in domain: return
        if (bridge==null)
            return;
        // if last bridge in domain: clear all and return
        if (m_bridges.size() == 1) {
            m_topology.clear();
            m_bridges.clear();
            m_rootBridgeId =  null;
            m_rootBridgeBFT.clear();
            m_STPLinks.clear();
            m_notYetParsedBFTMap.clear();
            return;
        }
        
        clearTopologyForBridge(bridge);
        Set<Bridge> bridges = new HashSet<Bridge>();
        for (Bridge cur: m_bridges) {
            if (cur.getId().intValue() == bridgeId) 
                continue;
            bridges.add(bridge);
        }
        m_bridges = bridges;
        m_notYetParsedBFTMap.remove(bridge);
        
        List<BridgeStpLink> stplinks = new ArrayList<BridgeStpLink>();
        for (BridgeStpLink link : m_STPLinks) {
            if (link.getNode().getId().intValue() == bridgeId )
                continue;
            stplinks.add(link);
        }
        m_STPLinks = stplinks;
            
    }

    public void clearTopologyForBridge(Bridge bridge) {
        if (bridge.isRootBridge()) {
            for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(bridge.getId())) {
                Integer newRootId = segment.getFirstNoDesignatedBridge();
                if (newRootId == null)
                    continue;
                Bridge newRootBridge=null;
                for (Bridge curBridge: m_bridges) {
                    if (curBridge.getId().intValue() == newRootId.intValue()) {
                        newRootBridge=curBridge;
                        break;
                    }
                }
                if (newRootBridge == null)
                    continue;
                m_rootBridgeBFT = getEffectiveBFT(newRootBridge);
                m_rootBridgeId = newRootId;
                newRootBridge.setRootBridge(true);
                newRootBridge.setRootPort(null);
                hierarchySetUp();
                break;
            }
        }

        //all the topology will be merged with the segment for bridge designated port
        SharedSegment topsegment = getSharedSegment(bridge.getId(), bridge.getRootPort());
        if (topsegment != null)
            topsegment.removeBridge(bridge.getId());
        
        for (SharedSegment segment: removeSharedSegmentOnTopologyForBridge(bridge.getId())) {
            if (topsegment != null)
                topsegment.mergeBridge(segment,bridge.getId());
        }        
    }
    
    public List<SharedSegment> getSharedSegmentOnTopologyForBridge(Integer bridgeId) {
        List<SharedSegment> segmentsOnBridge = new ArrayList<SharedSegment>();
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) 
                segmentsOnBridge.add(segment);
        }
        return segmentsOnBridge;
    }

    public List<SharedSegment> removeSharedSegmentOnTopologyForBridge(Integer bridgeId) {
        List<SharedSegment> segmentsOnBridge = new ArrayList<SharedSegment>();
        List<SharedSegment> topology = new ArrayList<SharedSegment>();
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) 
                segmentsOnBridge.add(segment);
            else 
                topology.add(segment);
        }
        m_topology = topology;
        return segmentsOnBridge;
    }
    
    public Set<Bridge> getBridgeOnSharedSegment(SharedSegment segment) {
        Set<Integer> nodeidsOnSegment = new HashSet<Integer>(segment.getBridgeIdsOnSegment());
        Set<Bridge> bridgesOn = new HashSet<Bridge>();
        for (Bridge bridge: m_bridges) {
            if (nodeidsOnSegment.contains(bridge.getId()))
                bridgesOn.add(bridge);
        }
        return bridgesOn;
    }

    public SharedSegment getSharedSegment(Integer bridgeId, Integer bridgePort) {
        if (bridgeId == null || bridgePort == null)
            return null;
        for (SharedSegment segment: m_topology) {
            if (segment.containsPort(bridgeId, bridgePort)) 
                return segment;
        }
        return null;
    }
    
        
    public List<BridgeMacLink> getEffectiveBFT(Bridge bridge) {
        Map<Integer,Set<String>> bft = new HashMap<Integer, Set<String>>();
        Integer bridgeId = bridge.getId();
        List<BridgeMacLink> links = new ArrayList<BridgeMacLink>();
        OnmsNode node=new OnmsNode();
        node.setId(bridgeId);
        for (SharedSegment segment: m_topology) {
            if (segment.getMacsOnSegment().isEmpty())
                continue;
            Integer bridgeport = getTopLevelPortUpBridge(segment,bridge);
            if (!bft.containsKey(bridgeport))
                bft.put(bridgeport, new HashSet<String>());
            bft.get(bridgeport).addAll(segment.getMacsOnSegment());
       }
            
        for (Integer bridgePort: bft.keySet()) {
            for (String mac: bft.get(bridgePort)) {
                BridgeMacLink link = new BridgeMacLink();
                link.setNode(node);
                link.setBridgePort(bridgePort);
                link.setMacAddress(mac);
                link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                links.add(link);
            }
        }
        return links;
    }

    private Integer getTopLevelPortUpBridge(SharedSegment down,Bridge bridge) {
            Integer upBridgeId = down.getDesignatedBridge();
            // if segment is on the bridge then...
            if (upBridgeId.intValue() == bridge.getId().intValue())
                return down.getDesignatedPort();
            // if segment is a root segment add mac on port
            if (upBridgeId.intValue() == m_rootBridgeId.intValue())
                return bridge.getRootPort();
            // iterate until you got it
            Bridge upBridge = null;
            for (Bridge cbridge: m_bridges) {
                if (cbridge.getId().intValue() == upBridgeId.intValue()) {
                    upBridge=cbridge;
                    break;
                }
            }
            if (upBridge == null)
                return null;
            SharedSegment shared = getSharedSegment(upBridgeId,upBridge.getRootPort());
            if (shared == null)
                return null;
        return getTopLevelPortUpBridge(shared, bridge);
    }

    public synchronized Map<Bridge,List<BridgeMacLink>> removeUpdateMap() {
        Map<Bridge,List<BridgeMacLink>> map = new HashMap<Bridge, List<BridgeMacLink>>();
        for (Bridge bridge: m_notYetParsedBFTMap.keySet()) {
            map.put(bridge, new ArrayList<BridgeMacLink>(m_notYetParsedBFTMap.get(bridge)));
        }
        m_notYetParsedBFTMap.clear();
        return map;
    }
    
    public void hierarchySetUp() {
        if (m_bridges.size() == 1)
            return;
        for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(m_rootBridgeId)) {
            segment.setDesignatedBridge(m_rootBridgeId);
            tier(segment, m_rootBridgeId);
        }
    }
    
    private void tier(SharedSegment segment, Integer rootid) {
        for (Integer bridgeid: segment.getBridgeIdsOnSegment()) {
            if (bridgeid.intValue() == rootid.intValue())
                continue;
            for (SharedSegment s2: getSharedSegmentOnTopologyForBridge(bridgeid)) {
                if (s2.getDesignatedBridge().intValue() == rootid.intValue())
                    continue;
                s2.setDesignatedBridge(bridgeid);
                tier(s2,bridgeid);
            }
        }
    }
    
}
