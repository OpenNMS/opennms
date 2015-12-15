package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;

// FIXME
// check synchronized and volatile
// 
public class BroadcastDomain {

    Set<Bridge> m_bridges = new HashSet<Bridge>();
    List<SharedSegment> m_topology = new ArrayList<SharedSegment>();
    
    Integer m_rootBridgeId;
    List<BridgeMacLink> m_rootBridgeBFT = new ArrayList<BridgeMacLink>();
    
    boolean m_calculating = false;
    
    Map<Integer, List<BridgeMacLink>> m_notYetParsedBFTMap = new HashMap<Integer, List<BridgeMacLink>>();
    List<BridgeStpLink> m_STPLinks = new ArrayList<BridgeStpLink>();
    
    private class SimpleConnection {
        final List<BridgeMacLink> m_links;
        final BridgeBridgeLink m_dlink;
        
        SimpleConnection(List<BridgeMacLink> links, BridgeBridgeLink dlink){
            m_links = links;
            m_dlink = dlink;
        }

        public List<BridgeMacLink> getLinks() {
            return m_links;
        }

        public BridgeBridgeLink getDlink() {
            return m_dlink;
        }
    }

    //FIXME
    // least condition theorem for simple connections
    // X and Y are bridges
    // m_1 m_2 m_3 are mac addresses
    // m_x is a mac address of bridge X
    // piX, pjX are port on bridgeX
    // FDB(pi,X) is the Bridge Forwarding Set for port pi on bridge X
    // TS(pi,X) is the Through Set for port pi on bridge X
    // minimun requiremnt:
    // X and Y are simple connected by xy on X and yx on Y
    // condition 1
    // if exists m_x and m_y :     m_x belongs FDB(yx,Y) 
    //                             m_y belongs FDB(xy,X)
    //
    // condition 2
    // if exists m_x, m_1 and m_2, p1 and p2 on Y : m_x belongs to FDB(yx,Y) 
    //                                              m_1 belongs to FDB(p1,Y) FDB(xy,X)
    //                                              m_2 belongs to FDB(p2,Y) FDB(xy,X)
    //
    // condition 3
    // if exist m_1,m_2,m_3 and p1,p2 on Y and p3 on X: m_1 belongs to FDB(p1,Y) FDB(xy,X) 
    //                                                  m_2 belongs to FDB(p2,Y) FDB(xy,X) 
    //                                                  m_3 belongs to FDB(yx,Y) FDB(p3,X)
    //
    // condition 4
    // if exist m_1,m_k,m_3 and p1 on Y and p3 on X: m_1 belongs to FDB(p1,Y) FDB(xy,X)
    //                                               m_k belongs to FDB(yx,Y) FDB(xy,X) 
    //                                               m_3 belongs to FDB(yx,Y) FDB(p3,X)
    //
    // condition 5
    // intersection is made only by macs living on xy of X and yx of Y
    // these is no other common forwarding port
    // first step is to find the common macs.
    // then we work on this set (if the size is only 2......no way)
    // get m_1 m_2 m_3 and check the ports on the two bridges...to match rules
    // 
    private class BridgeTopologyHelper {
        
        final Integer m_xBridge;
        final Integer m_yBridge;
        final List<BridgeMacLink> m_xBFT;
        final List<BridgeMacLink> m_yBFT;
        public BridgeTopologyHelper(Integer xBridge, Integer yBridge,
                List<BridgeMacLink> xBFT, List<BridgeMacLink> yBFT) {
            super();
            m_xBridge = xBridge;
            m_yBridge = yBridge;
            m_xBFT = xBFT;
            m_yBFT = yBFT;
        }
        
        public Integer getFirstBridgeDesignatedPort() {
            return null;
        }
        
        public Integer getSecondBridgePort() {
            return null;
        }
        
        public SimpleConnection getSimpleConnection() {
            return new SimpleConnection(null, null);
        }

        public Map<Integer,List<BridgeMacLink>> getTroughSet() {
            return null;
        }
    }
    
    private class Bridge {

        final Integer m_id;
        Set<BridgeElement> m_bridgeIds = new HashSet<BridgeElement>();
        Integer m_rootPort;
        boolean m_isRootBridge=false;

        public Bridge(Integer id) {
            super();
            m_id = id;
        }

        public boolean hasBridgeId(String bridgeId) {
            if (bridgeId == null)
                return false;
            for (BridgeElement element: m_bridgeIds) {
                if (bridgeId.equals(element.getBaseBridgeAddress()))
                        return true;
            }
            return false;
        }
                
        public Set<String> getOtherStpRoots() {
            Set<String> stpRoots = new HashSet<String>();
            for (BridgeElement element: m_bridgeIds) {
                if (InetAddressUtils.isValidStpBridgeId(element.getStpDesignatedRoot())) {
                    String stpRoot = InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot());
                    if ( stpRoot.equals(element.getBaseBridgeAddress()))
                            continue;
                    stpRoots.add(stpRoot);
                }
            }
            return stpRoots;
        }

        public Bridge(BridgeElement bridgeElement) {
            super();
            m_id = bridgeElement.getNode().getId();
            m_bridgeIds.add(bridgeElement);
        }


        public Integer getRootPort() {
            return m_rootPort;
        }

        public void setRootPort(Integer rootPort) {
            m_rootPort = rootPort;
        }

        public boolean isRootBridge() {
            return m_isRootBridge;
        }

        public void setRootBridge(boolean isRootBridge) {
            m_isRootBridge = isRootBridge;
        }

        public void addBridgeElement(BridgeElement bridgeElement) {
            if (bridgeElement.getNode().getId() != m_id)
                return; 
            for (BridgeElement curBridgeElement: m_bridgeIds) {
                if (curBridgeElement.getBaseBridgeAddress().equals(bridgeElement.getBaseBridgeAddress())) {
                    curBridgeElement = bridgeElement;
                    return;
                }
            }
            m_bridgeIds.add(bridgeElement);
        }

        public Integer getId() {
            return m_id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
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
            Bridge other = (Bridge) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (m_id == null) {
                if (other.m_id != null)
                    return false;
            } else if (!m_id.equals(other.m_id))
                return false;
            return true;
        }

        private BroadcastDomain getOuterType() {
            return BroadcastDomain.this;
        }
        
        
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

    public void addTopologyEntry(SharedSegment segment) {
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

    public void loadBFT(int nodeId, List<BridgeMacLink> maclinks,List<BridgeStpLink> stplinks) {
        m_notYetParsedBFTMap.put(nodeId, maclinks);
        List<BridgeStpLink> allstplinks = new ArrayList<BridgeStpLink>();
        //remove all stp link in the list to let them
        // substituted with the new incoming list
        for (BridgeStpLink link: m_STPLinks) {
            if (link.getNode().getId().intValue() == nodeId)
                continue;
            allstplinks.add(link);
        }
        allstplinks.addAll(stplinks);
        m_STPLinks=allstplinks;
    }
    
    public List<SharedSegment> getTopology() {
        return m_topology;
    }
        
    public void removeBridge(int bridgeId) {
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
            return;
        }
        
        // if is root: rearrange topology with a new root before deleting.
        if (bridge.isRootBridge()) {
            for (SharedSegment segment: getSharedSegmentOnBridge(bridgeId)) {
                Integer newRootId = segment.getFirstNoDesignatedBridge();
                if (newRootId == null)
                    continue;
                m_rootBridgeBFT = getEffectiveBFT(newRootId);
                m_rootBridgeId = newRootId;
                hierarchySetUp();
                break;
            }
        }

        //all the topology will be merged with the segment for bridge designated port
        SharedSegment topsegment = getSharedSegment(bridgeId, bridge.getRootPort());
        if (topsegment != null)
            topsegment.removeBridge(bridgeId);
        
        for (SharedSegment segment: removeSharedSegmentOnBridge(bridgeId)) {
            if (topsegment != null)
                topsegment.mergeBridge(segment,bridgeId);
        }
        
        Set<Bridge> bridges = new HashSet<Bridge>();
        for (Bridge cur: m_bridges) {
            if (cur.getId().intValue() == bridgeId) 
                continue;
            bridges.add(bridge);
        }
        m_bridges = bridges;
    }
    
    public synchronized boolean isEmpty() {
        return m_bridges.isEmpty();
    }
        
    public synchronized boolean isCalculating() {
        return m_calculating;
    }

    public synchronized Set<Integer> getUpdatedNodes() {
        Set<Integer> updatedNodes = new HashSet<Integer>();
        for (Bridge bridge: m_bridges)
            updatedNodes.add(bridge.getId());
        return updatedNodes;
    }

    public synchronized void calculate() {
        m_calculating = false;
        selectRootBridge();
        hierarchySetUp();

        // the root bridge is the only bridge in topology
        if (m_topology.isEmpty() && m_notYetParsedBFTMap.isEmpty() && m_rootBridgeBFT != null) {
            Map<Integer, SharedSegment> rootleafs=new HashMap<Integer, SharedSegment>();
            for (BridgeMacLink link: m_rootBridgeBFT) {
                if (!rootleafs.containsKey(link.getBridgePort()))
                    rootleafs.put(link.getBridgePort(), new SharedSegment());
                rootleafs.get(link.getBridgePort()).add(link);
            }
            for (SharedSegment rootleaf: rootleafs.values()) 
                m_topology.add(rootleaf);
            return;
        }
        Set<Integer> nodeids = new HashSet<Integer>();
        nodeids.addAll(m_notYetParsedBFTMap.keySet());
        for (Integer nodeid: nodeids) 
            findBridgesTopo(m_rootBridgeId, m_rootBridgeBFT, nodeid, m_notYetParsedBFTMap.remove(nodeid));
       
    }
    
    private void findBridgesTopo(Integer rBridgeId, List<BridgeMacLink> rBFT, Integer xBridgeId, List<BridgeMacLink> xBFT) {
        BridgeTopologyHelper rx = new BridgeTopologyHelper(rBridgeId, xBridgeId, rBFT, xBFT);
        Integer rxDesignatedPort = rx.getFirstBridgeDesignatedPort();
        Integer xrDesignatedPort = rx.getSecondBridgePort();
        //get the starting point shared segment of the top bridge
        // where the bridge is learned should not be null
        SharedSegment topSegment = getSharedSegment(rBridgeId,rxDesignatedPort);
        if (topSegment == null) {
            topSegment = new SharedSegment(rBridgeId,rxDesignatedPort);
            topSegment.assign(rx.getSimpleConnection().getLinks(),rx.getSimpleConnection().getDlink());
            m_topology.add(topSegment);
        }

        for (Bridge yBridge: getBridgeOnSharedSegment(topSegment)) {
            Integer yBridgeId = yBridge.getId();
            BridgeTopologyHelper xy = new BridgeTopologyHelper(xBridgeId, yBridgeId, xBFT, getEffectiveBFT(yBridgeId));
            Integer xyDesignatedPort = xy.getFirstBridgeDesignatedPort();
            Integer yxDesignatedPort = xy.getSecondBridgePort();
            Integer yrDesignatedPort = yBridge.getRootPort();
            // X is a leaf of Y
            if (xyDesignatedPort == xrDesignatedPort && yxDesignatedPort != yrDesignatedPort) {
                findBridgesTopo(yBridge.getId(), getEffectiveBFT(yBridge.getId()), xBridgeId, xBFT);
                return;
            }
            // Y is a leaf of X
            if (xyDesignatedPort != xrDesignatedPort && yxDesignatedPort == yrDesignatedPort) {
                //create a SharedSegment with root port
                SharedSegment leafSegment = new SharedSegment(xBridgeId, xyDesignatedPort);
                leafSegment.assign(xy.getSimpleConnection().getLinks(),xy.getSimpleConnection().getDlink());
                m_topology.add(leafSegment);
                topSegment.removeBridge(yBridge.getId());
            }            
            // this is a clear violation  of the topology tree rule
            if (xyDesignatedPort != xrDesignatedPort && yxDesignatedPort != yrDesignatedPort) {
                m_topology.clear();
                return;
            }            
        }
        // if we are here is because X is NOT a leaf of any bridge found
        // on topSegment so X is connected to top Segment by it's root 
        // port or rx is a direct connection
        topSegment.assign(rx.getSimpleConnection().getLinks(),rx.getSimpleConnection().getDlink());
        for (Integer xbridgePort: rx.getTroughSet().keySet()) {
            SharedSegment xleafSegment = new SharedSegment(xBridgeId, xbridgePort);
            xleafSegment.setBridgeMacLinks(rx.getTroughSet().get(xbridgePort));
            m_topology.add(xleafSegment);
        }
        
    }

    private List<SharedSegment> getSharedSegmentOnBridge(Integer bridgeId) {
        List<SharedSegment> segmentsOnBridge = new ArrayList<SharedSegment>();
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) 
                segmentsOnBridge.add(segment);
        }
        return segmentsOnBridge;
    }

    private List<SharedSegment> removeSharedSegmentOnBridge(Integer bridgeId) {
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
    
    private Set<Bridge> getBridgeOnSharedSegment(SharedSegment segment) {
        Set<Integer> nodeidsOnSegment = new HashSet<Integer>();
        if (segment.noMacsOnSegment()) {
            for (BridgeBridgeLink link : segment.getBridgeBridgeLinks()) {
                nodeidsOnSegment.add(link.getNode().getId());
                nodeidsOnSegment.add(link.getDesignatedNode().getId());
            }
        } else {
            for (BridgeMacLink link: segment.getBridgeMacLinks()) {
                nodeidsOnSegment.add(link.getNode().getId());
            }
        }
        Set<Bridge> bridgesOn = new HashSet<BroadcastDomain.Bridge>();
        for (Bridge bridge: m_bridges) {
            if (nodeidsOnSegment.contains(bridge.getId()))
                bridgesOn.add(bridge);
        }
        return bridgesOn;
    }

    private SharedSegment getSharedSegment(Integer bridgeId, Integer bridgePort) {
        if (bridgeId == null || bridgePort == null)
            return null;
        for (SharedSegment segment: m_topology) {
            if (segment.containsPort(bridgeId, bridgePort)) 
                return segment;
        }
        return null;
    }
    
    private void selectRootBridge() {
        Bridge rootBridge= null;
        //if there is only one bridge....
        if (m_bridges.size() == 1)
            rootBridge = m_bridges.iterator().next();

        //if null try set the stp roots
        if (rootBridge == null) {
            Set<String> rootBridgeIds=new HashSet<String>();
            for (Bridge bridge: m_bridges) {
                rootBridgeIds.addAll(bridge.getOtherStpRoots());
            }
            //well only one root bridge should be defined....
            //otherwise we need to skip calculation
            //so here is the place were we can
            //manage multi stp domains...
            //ignoring for the moment....
            for (String rootBridgeId: rootBridgeIds) {
                for (Bridge bridge: m_bridges) {
                    if (bridge.hasBridgeId(rootBridgeId)) {
                        rootBridge = bridge;
                        break;
                    }
                }
            }
        }

        // no spanning tree root?
        // then find root among switches with
        // updated bft with max bft size
        if (rootBridge == null) {
            int size = 0;
            Integer rootNodeid = null;
            for (Integer nodeid:  m_notYetParsedBFTMap.keySet()) {
                int cursize = m_notYetParsedBFTMap.get(nodeid).size();
                if (size < cursize) {
                    rootNodeid = nodeid;
                    size = cursize;
                }
            }
            if (rootNodeid != null ) {
                for (Bridge bridge: m_bridges) {
                    if (bridge.getId().intValue() == rootNodeid.intValue()) {
                        rootBridge = bridge;
                        break;
                    }
                }
            }
        }
        
        // still not found...get the first
        if (rootBridge == null)
            rootBridge=m_bridges.iterator().next();
        
        m_rootBridgeId = rootBridge.getId();
        rootBridge.setRootBridge(true);
        rootBridge.setRootPort(null);
        if (m_notYetParsedBFTMap.containsKey(m_rootBridgeId))
            m_rootBridgeBFT = m_notYetParsedBFTMap.remove(m_rootBridgeId);
        else
            m_rootBridgeBFT = getEffectiveBFT(rootBridge.getId());
    }
        
    private List<BridgeMacLink> getEffectiveBFT(Integer bridgeId) {
        List<BridgeMacLink> links = new ArrayList<BridgeMacLink>();
        for (SharedSegment segment: getSharedSegmentOnBridge(bridgeId)) {
            Integer bridgePort = null;
            if (segment.noMacsOnSegment()) {
                for (BridgeBridgeLink link: segment.getBridgeBridgeLinks()) {
                    if (link.getNode().getId() == bridgeId) {
                        bridgePort = link.getBridgePort();
                        break;
                    }
                    if (link.getDesignatedNode().getId() == bridgeId) {
                        bridgePort = link.getDesignatedPort();
                        break;
                    }
                }
            } else {
                for (BridgeMacLink link: segment.getBridgeMacLinks()) {
                    if (link.getNode().getId() == bridgeId) {
                        bridgePort = link.getBridgePort();
                        links.add(link);
                    }
                }
            }
            
            for (String mac: getForwardingSet(segment, bridgeId)) {
                BridgeMacLink link = new BridgeMacLink();
                link.setBridgePort(bridgePort);
                link.setMacAddress(mac);
                links.add(link);
            }
        }
        return links;
    }

    private Set<String> getForwardingSet(SharedSegment segment, Integer bridgeId) {
        Set<String> macs = new HashSet<String>();
        for (Bridge bridge: getBridgeOnSharedSegment(segment)) {
            if (bridge.getId() == bridgeId)
                continue;
            for (SharedSegment s2: getSharedSegmentOnBridge(bridge.getId())) {
                macs.addAll(s2.getMacsOnSegment());
                macs.addAll(getForwardingSet(s2, bridge.getId()));
            }
        }
       return macs;
    }

    
    private void hierarchySetUp() {
        //top level
        for (SharedSegment segment: getSharedSegmentOnBridge(m_rootBridgeId)) {
            segment.setDesignatedBridge(m_rootBridgeId);
            segment.setDesignatedPort(segment.getPortForBridge(m_rootBridgeId));
            tier(segment, m_rootBridgeId);
        }
    }
    
    private void tier(SharedSegment segment, Integer rootid) {
        for (Bridge bridge: getBridgeOnSharedSegment(segment)) {
            if (bridge.getId() == rootid)
                continue;
            for (SharedSegment s2: getSharedSegmentOnBridge(bridge.getId())) {
                s2.setDesignatedBridge(bridge.getId());
                s2.setDesignatedPort(s2.getPortForBridge(bridge.getId()));
                tier(s2,bridge.getId());
            }
        }
    }

}
