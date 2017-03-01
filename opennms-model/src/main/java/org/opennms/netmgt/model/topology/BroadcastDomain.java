package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastDomain {
    
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastDomain.class);

    volatile Set<Bridge> m_bridges = new HashSet<Bridge>();

    volatile List<SharedSegment> m_topology = new ArrayList<SharedSegment>();    
    
    boolean m_lock = false;

    Object m_locker;
    public void clearTopology() {
        m_topology.clear();
    }
    
    public boolean isEmpty() {
        return m_bridges.isEmpty();
    }

    public Set<Integer> getBridgeNodesOnDomain() {
        Set<Integer> bridgeIds = new HashSet<Integer>();
        for (Bridge bridge: m_bridges) 
            bridgeIds.add(bridge.getId());
        return bridgeIds;
    }
    
    public synchronized boolean getLock(Object locker) {
        if (m_lock)
            return false;
        if (locker == null)
            return false;
        m_lock=true;
        m_locker=locker;
        return true;
    }

    public synchronized boolean releaseLock(Object locker) {
        if (locker == null)
            return false;
        if (!m_lock )
            return false;
        if (!m_locker.equals(locker))
            return false;
        m_locker = null;
        m_lock=false; 
        return true;
                
    }

    public Set<Bridge> getBridges() {
        return m_bridges;
    }
    
    public List<SharedSegment> getTopology() {
        return m_topology;
    }
        
    public boolean hasRootBridge() {
        for (Bridge bridge: m_bridges) {
            if (bridge.isRootBridge())
                return true;
        }
        return false;
        
    }
    public Integer getRootBridgeId() {
        for (Bridge bridge: m_bridges) {
            if (bridge.isRootBridge())
                return bridge.getId();
        }
        return null;
    }

    public Bridge getRootBridge() {
        for (Bridge bridge: m_bridges) {
            if (bridge.isRootBridge())
                return bridge;
        }
        return null;
    }

    public void addBridge(Bridge bridge) {
        if (m_bridges.contains(bridge))
            return;
        m_bridges.add(bridge);
    }

    public Bridge getBridge(int bridgeId) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == bridgeId)
                return bridge;
        }
        return null;
    }

    public Set<String> getMacsOnDomain() {
        Set<String>macs = new HashSet<String>();
        for (SharedSegment segment: m_topology) 
            macs.addAll(segment.getMacsOnSegment());
        return macs;
    }

    public void add(SharedSegment segment) {
        m_topology.add(segment);
    }
    
    public void loadTopologyEntry(SharedSegment segment) {
        segment.setBroadcastDomain(this);
        m_topology.add(segment);
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
    
    public boolean containBridgeId(int nodeid) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == nodeid)
                return true;
        }
        return false;
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
            return;
        }
        
        Set<Bridge> bridges = new HashSet<Bridge>();
        for (Bridge cur: m_bridges) {
            if (cur.getId().intValue() == bridgeId) 
                continue;
            bridges.add(cur);
        }
        m_bridges = bridges;            
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
    
    public void hierarchySetUp(Bridge root) {
        if (root.isRootBridge())
            return;
        root.setRootBridge(true);
        root.setRootPort(null);
        if (m_bridges.size() == 1)
            return;
        for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(root.getId())) {
            segment.setDesignatedBridge(root.getId());
            tier(segment, root.getId());
        }
    }
    
    private void tier(SharedSegment segment, Integer rootid) {
        for (Integer bridgeid: segment.getBridgeIdsOnSegment()) {
            if (bridgeid.intValue() == rootid.intValue())
                continue;
            Bridge bridge = getBridge(bridgeid);
            bridge.setRootPort(segment.getPortForBridge(bridgeid));
            bridge.setRootBridge(false);
            for (SharedSegment s2: getSharedSegmentOnTopologyForBridge(bridgeid)) {
                if (s2.getDesignatedBridge() != null && s2.getDesignatedBridge().intValue() == rootid.intValue())
                    continue;
                s2.setDesignatedBridge(bridgeid);
                tier(s2,bridgeid);
            }
        }
    }
    
    public void clearTopologyForBridge(Integer bridgeId) {
    	Bridge bridge = getBridge(bridgeId);
    	if (bridge == null)
    		return;
        if (bridge.isRootBridge()) {
            LOG.debug("clearTopologyForBridge: clearTopologyForBridge: bridge {}, is root bridge. setting up a new hierarchy before clean",
                      bridge.getId());
            for (SharedSegment segment: getSharedSegmentOnTopologyForBridge(bridgeId)) {
                Integer newRootId = segment.getFirstNoDesignatedBridge();
                if (newRootId == null)
                    continue;
                Bridge newRootBridge=null;
                for (Bridge curBridge: getBridges()) {
                    if (curBridge.getId().intValue() == newRootId.intValue()) {
                        newRootBridge=curBridge;
                        break;
                    }
                }
                if (newRootBridge == null)
                    continue;
                hierarchySetUp(newRootBridge);
                break;
            }
        }
        LOG.debug("clearTopologyForBridge: clearTopologyForBridge: bridge {}, bridge root port {}",
                  bridge.getId(), bridge.getRootPort());
        //all the topology will be merged with the segment for bridge designated port
        SharedSegment topsegment = getSharedSegment(bridge.getId(), bridge.getRootPort());
        if (topsegment != null) {
            LOG.debug("clearTopologyForBridge: clearTopologyForBridge: removing bridge {}: top segment nodes {}, macs {}, designated {}, port {}",
                      bridge.getId(),topsegment.getBridgeIdsOnSegment(),topsegment.getMacsOnSegment(),
                      topsegment.getDesignatedBridge(),topsegment.getDesignatedPort());
            topsegment.removeBridge(bridge.getId());
            LOG.debug("clearTopologyForBridge: clearTopologyForBridge: removed bridge {}: top segment nodes {}, macs {}, designated {}, port {}",
                      bridge.getId(),topsegment.getBridgeIdsOnSegment(),topsegment.getMacsOnSegment(),
                      topsegment.getDesignatedBridge(),topsegment.getDesignatedPort());
        } else {
            LOG.debug("clearTopologyForBridge: clearTopologyForBridge {}: no top segment found",
                      bridge.getId());
        }

        for (SharedSegment segment: removeSharedSegmentOnTopologyForBridge(bridge.getId())) {
            LOG.debug("clearTopologyForBridge: clearTopologyForBridge merging bridge {} on top for segment: nodes {}, macs {}, designated {}, port {}",
                      bridge.getId(),segment.getBridgeIdsOnSegment(),segment.getMacsOnSegment(),
                      segment.getDesignatedBridge(),segment.getDesignatedPort());
            if (topsegment != null)
                topsegment.mergeBridge(segment,bridge.getId());
        }        

    }

    public List<BridgeMacLink> calculateRootBFT() {
    	Bridge root = getRootBridge();
    	if (root == null)
    		return null;
    	return calculateBFT(root);
    }
    
    public List<BridgeMacLink> calculateBFT(Bridge bridge) {
        LOG.debug("calculate: getBFT: bridge: {}, bridge root port {}",
                  bridge.getId(), bridge.getRootPort());

        Map<Integer,Set<String>> bft = new HashMap<Integer, Set<String>>();
        Integer bridgeId = bridge.getId();
        List<BridgeMacLink> links = new ArrayList<BridgeMacLink>();
        OnmsNode node=new OnmsNode();
        node.setId(bridgeId);
        for (SharedSegment segment: getTopology()) {
            
            Set<String> macs = segment.getMacsOnSegment();
            
            if (macs == null || macs.isEmpty())
                continue;
            Integer bridgeport = goUp(segment,bridge,0);
            LOG.info("calculate: getBFT: bridge: {}, assigning macs {} to port {}", bridgeId,macs, bridgeport );
            if (!bft.containsKey(bridgeport))
                bft.put(bridgeport, new HashSet<String>());
            bft.get(bridgeport).addAll(macs);
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
    
    private Integer goUp(SharedSegment down,Bridge bridge, int level) {
        if (level == 30) {
            LOG.warn("calculate: level {}: bridge: {}, too many iteration on topology exiting.....",level,bridge.getId());
            clearTopology();
            return -1;
        }
        LOG.debug("calculate: goUp: level: {}, checking up segment designated bridge {}, designated port {}, bridge {}, with root port {}",
                  level,down.getDesignatedBridge(),down.getDesignatedPort(), bridge.getId(), bridge.getRootPort());
            Integer upBridgeId = down.getDesignatedBridge();
            // if segment is on the bridge then...
            if (upBridgeId.intValue() == bridge.getId().intValue()) {
                LOG.debug("calculate: goUp: level: {}, return bridge {}, designated port {}",
                          level,down.getDesignatedBridge(),down.getDesignatedPort());
                return down.getDesignatedPort();
            }
            // if segment is a root segment add mac on port
            if (upBridgeId.intValue() == getRootBridgeId().intValue()) {
                LOG.debug("calculate: goUp: level: {}, got root bridge, returning bridge {}, root port {}",
                          level,bridge.getId(),bridge.getRootPort());
                return bridge.getRootPort();
            }
            // iterate until you got it
            Bridge upBridge = null;
            for (Bridge cbridge: getBridges()) {
                if (cbridge.getId().intValue() == bridge.getId().intValue())
                    continue;
                LOG.debug("calculate: goUp: level: {}, searching bridge {}, parsing bridge {}",
                          level,down.getDesignatedBridge(),cbridge.getId());
                if (cbridge.getId().intValue() == upBridgeId.intValue()) {
                    LOG.debug("calculate: goUp: level: {}, searching bridge {}, found bridge {}, with root port {}",
                              level,down.getDesignatedBridge(),cbridge.getId(), cbridge.getRootPort());
                    upBridge=cbridge;
                    break;
                }
            }
            if (upBridge == null) {
                LOG.debug("calculate: goUp: level: {}, searching bridge {}, no bridge found!!!",
                          level,down.getDesignatedBridge());
                return null;
            }
            SharedSegment up = getSharedSegment(upBridge.getId(),upBridge.getRootPort());
            if (up == null) {
                LOG.debug("calculate: goUp: level: {}, no shared segment found on bridge {}, root port {}!!!",
                          level,down.getDesignatedBridge());
                return null;
            }
        return goUp(up, bridge,++level);
    }    

    public void clear() {
        m_topology.clear();
        m_bridges.clear();
    }

}
