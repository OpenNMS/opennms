package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;

public class BroadcastDomain {

    Set<Bridge> m_bridges = new HashSet<Bridge>();
    List<SharedSegment> m_topology = new ArrayList<SharedSegment>();
    boolean m_topologyChanged = false;
    volatile Map<Integer, List<BridgeMacLink>> m_notYetParsedBFTMap = new HashMap<Integer, List<BridgeMacLink>>();
    volatile Map<Integer, List<BridgeStpLink>> m_notYetParsedSTPMap = new HashMap<Integer, List<BridgeStpLink>>();
    Integer m_rootBridgeId;
    volatile List<BridgeMacLink> m_rootBridgeBFT = new ArrayList<BridgeMacLink>();

    //FIXME here you have to find the links...
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
        
        public Integer getXYPort() {
            return null;
        }
        
        public Integer getYXPort() {
            return null;
        }
    }
    
    private class Bridge {

        final Integer m_id;
        Set<BridgeElement> m_bridgeIds = new HashSet<BridgeElement>();
        Set<String> m_portMacs = new HashSet<String>();
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

        public Set<String> getPortMacs() {
            return m_portMacs;
        }

        public void addPortMac(Set<String> portMacs) {
            m_portMacs.addAll(portMacs);
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
    
    public synchronized boolean topologyChanged() {
        return m_topologyChanged;
    }

    public synchronized Set<Integer> getUpdatedNodes() {
        Set<Integer> updatedNodes = new HashSet<Integer>();
        for (Bridge bridge: m_bridges)
            updatedNodes.add(bridge.getId());
        return updatedNodes;
    }
    
    public synchronized void addBridgeElement(BridgeElement bridgeElement) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getId() == bridgeElement.getNode().getId()) {
                bridge.addBridgeElement(bridgeElement);
            }
        }
        m_bridges.add(new Bridge(bridgeElement));
    }

    public synchronized void addTopologyEntry(SharedSegment segment) {
        m_topology.add(segment);
        for (Integer nodeId : segment.getBridgeIdsOnSegment()) {
            m_bridges.add(new Bridge(nodeId));
        }
    }
    
    public synchronized boolean containsAtleastOne(Set<Integer> nodeids) {
        for (Bridge bridge: m_bridges) {
            for (Integer nodeid:nodeids) {
                if (bridge.getId().intValue() == nodeid.intValue())
                    return true;
            }
        }
        return false;
    }
    
    public synchronized boolean containBridgeId(Integer nodeid) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == nodeid.intValue())
                return true;
        }
        return false;
    }

    public synchronized Set<String> getMacsOnDomain() {
        Set<String>macs = new HashSet<String>();
        for (SharedSegment segment: m_topology) 
            macs.addAll(segment.getMacsOnSegment());
        return macs;
    }

    public synchronized void loadBFT(int nodeId, List<BridgeMacLink> maclinks, List<BridgeStpLink> stplinks) {
        m_topologyChanged = true;
        m_notYetParsedBFTMap.put(nodeId, maclinks);
        m_notYetParsedSTPMap.put(nodeId, stplinks);
    }

    public synchronized boolean checkBridgeOnDomain(List<BridgeMacLink> links) {
        Set<String>incomingSet = new HashSet<String>();
        for (BridgeMacLink link: links)
            incomingSet.add(link.getMacAddress());
        
        Set<String>retainedSet = new HashSet<String>();
        retainedSet.addAll(getMacsOnDomain());
        retainedSet.retainAll(incomingSet);
        // should contain at list 5% of the all size
        if (retainedSet.size() <= incomingSet.size()*0.05 ) {
            return false;
        }
        return true;
    }
    
    public synchronized List<SharedSegment> getTopology() {
        return m_topology;
    }
        
    public synchronized void deleteBridge(int nodeid) {
        List<SharedSegment> segments = new ArrayList<SharedSegment>();
        for (SharedSegment segment: m_topology) {
            segment.delete(nodeid);
            if (segment.isEmpty())
                continue;
            segments.add(segment);
        }
        m_topology=segments;
        
        Set<Bridge> bridges = new HashSet<Bridge>();
        for (Bridge bridge: m_bridges) {
            if (bridge.getId().intValue() == nodeid) 
                continue;
            bridges.add(bridge);
        }
        m_bridges = bridges;
        m_topologyChanged = true;
    }
    
    public synchronized boolean isEmpty() {
        return m_bridges.isEmpty();
    }
    
    //FIXME implement this method
    public synchronized void calculate() {
       //here use the partial algorithm to perform topology calculation
       // do not forget to setNode for objects 
       // the main element are the following....
       // choose the root!
       if (m_rootBridgeBFT.isEmpty())
           electRootBridge();

       for (Integer nodeid: m_notYetParsedBFTMap.keySet()) {
           findBridgesTopo(m_rootBridgeId, m_rootBridgeBFT, nodeid, m_notYetParsedBFTMap.get(nodeid));
       }
       
       //once calculated....
       m_notYetParsedBFTMap.clear();
       // mantain the bft of the root
       // found the port of the root where the bridge is 
       // work on the segments there to find the link
       // use the least condition theorem from lowercamp
       // traverse the segment starting from the first found
       // shared segment
       // operation on shared segment are split and merge 
       m_topologyChanged = false;
    }

    //FIXME
    public void findBridgesTopo(Integer RBridgeId, List<BridgeMacLink> RBft, Integer XBridgeId, List<BridgeMacLink> XBFT) {
        BridgeTopologyHelper RX = new BridgeTopologyHelper(RBridgeId, XBridgeId, RBft, XBFT);
        Integer RXport = RX.getXYPort();
        Integer XRport = RX.getYXPort();
        SharedSegment topSegment = getSharedSegment(RBridgeId,RXport);
        if (topSegment == null) {
            topSegment = new SharedSegment(RBridgeId,RXport);
            // FIXME 
            //topSegment.assign the macaddress
            m_topology.add(topSegment);

            //FIXME
            //add the other segments for each port on each bridge;
            return;
        }
        
        for (Bridge Y: getBridgeOnSharedSegment(topSegment)) {
            BridgeTopologyHelper XY = new BridgeTopologyHelper(RBridgeId, XBridgeId, RBft, XBFT);
            Integer XYport = XY.getXYPort();
            Integer YXport = XY.getYXPort();
            Integer YRport = Y.getRootPort();
            // X is a leaf of Y
            if (XYport == XRport && YXport != YRport) {
                //FIXME
                //Remove Y from topSegment
                //FIXME we have to find a real BFT for Y bridgeId
                findBridgesTopo(Y.getId(), getEffectiveBFT(Y.getId()), XBridgeId, XBFT);
                return;
            }
            // Y is a leaf of X
            if (XYport != XRport && YXport == YRport) {
                //FIXME create a SharedSegment with root port
                SharedSegment leafSegment = new SharedSegment(XBridgeId, XYport);
                //leafSegment assign bridgemaclinks
                //topSegment remove bridgemaclinks from Y
                m_topology.add(leafSegment);
            }            
        }
        //FIXME
        //Se sono qui e' perche' non sono una foglia, ho fogliato
        // e mi devo aggiungere al top segment
        //topSegment.assign()
        
        
    }

    //FIXME
    public List<BridgeMacLink> getEffectiveBFT(Integer bridgeId) {
        return null;
    }
    //FIXME
    public List<Bridge> getBridgeOnSharedSegment(SharedSegment segment) {
        return null;
    }

    //FIXME
    public SharedSegment getSharedSegment(Integer bridgeId, Integer BridgePort) {
        return null;
    }
    
    public void electRootBridge() {
        //check the spanning tree if exists.
        Set<String> rootBridgeIds=new HashSet<String>();
        for (Bridge bridge: m_bridges) {
            rootBridgeIds = bridge.getOtherStpRoots();
            if (rootBridgeIds.isEmpty())
                continue;
            break;
        }
        Bridge rootBridge= null;
        for (String rootBridgeId: rootBridgeIds) {
            for (Bridge bridge: m_bridges) {
                if (bridge.hasBridgeId(rootBridgeId)) {
                    rootBridge = bridge;
                    break;
                }
            }
        }

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
        if (rootBridge == null)
            return;
        m_rootBridgeId = rootBridge.getId();
        rootBridge.setRootBridge(true);
        rootBridge.setRootPort(null);
        m_rootBridgeBFT = m_notYetParsedBFTMap.remove(rootBridge.getId());
    }
}


/*
protected void saveBridgeTopology(final BridgeTopologyLink bridgelink) {
        if (bridgelink == null)
                return;

        OnmsNode node = m_nodeDao.get(bridgelink.getBridgeTopologyPort().getNodeid());
        if (node == null)
                return;
        OnmsNode designatenode = null;
        if (bridgelink.getDesignateBridgePort() != null) {
                designatenode = m_nodeDao.get(bridgelink.getDesignateBridgePort().getNodeid());
        }
        
        if (bridgelink.getMacs().isEmpty() && designatenode != null) {
                BridgeBridgeLink link = new BridgeBridgeLink();
                link.setNode(node);
                link.setBridgePort(bridgelink.getBridgeTopologyPort().getBridgePort());
                if (m_nodebridgeportifindex.containsKey(bridgelink.getBridgeTopologyPort().getNodeid()))
                    link.setBridgePortIfIndex(m_nodebridgeportifindex.get(bridgelink.getBridgeTopologyPort().getNodeid()).get(bridgelink.getBridgeTopologyPort().getBridgePort()));
                if (m_nodebridgeportvlan.containsKey(bridgelink.getBridgeTopologyPort().getNodeid()))
                    link.setVlan(m_nodebridgeportvlan.get(bridgelink.getBridgeTopologyPort().getNodeid()).get(bridgelink.getBridgeTopologyPort().getBridgePort()));
                link.setDesignatedNode(designatenode);
                link.setDesignatedPort(bridgelink.getDesignateBridgePort().getBridgePort());
                if (m_nodebridgeportifindex.containsKey(bridgelink.getDesignateBridgePort().getNodeid()))
                    link.setDesignatedPortIfIndex(m_nodebridgeportifindex.get(bridgelink.getDesignateBridgePort().getNodeid()).get(bridgelink.getDesignateBridgePort().getBridgePort()));
                if (m_nodebridgeportvlan.containsKey(bridgelink.getDesignateBridgePort().getNodeid()))
                    link.setDesignatedVlan(m_nodebridgeportvlan.get(bridgelink.getDesignateBridgePort().getNodeid()).get(bridgelink.getDesignateBridgePort().getBridgePort()));
                saveBridgeBridgeLink(link);
                return;
        } 

        for (String mac: bridgelink.getMacs()) {
                BridgeMacLink maclink1 = new BridgeMacLink();
                maclink1.setNode(node);
                maclink1.setBridgePort(bridgelink.getBridgeTopologyPort().getBridgePort());
                if (m_nodebridgeportifindex.containsKey(bridgelink.getBridgeTopologyPort().getNodeid()))
                    maclink1.setBridgePortIfIndex(m_nodebridgeportifindex.get(bridgelink.getBridgeTopologyPort().getNodeid()).get(bridgelink.getBridgeTopologyPort().getBridgePort()));
                if (m_nodebridgeportvlan.containsKey(bridgelink.getBridgeTopologyPort().getNodeid()))
                    maclink1.setVlan(m_nodebridgeportvlan.get(bridgelink.getBridgeTopologyPort().getNodeid()).get(bridgelink.getBridgeTopologyPort().getBridgePort()));
                maclink1.setMacAddress(mac);
                saveBridgeMacLink(maclink1);
                if (designatenode == null)
                        continue;
                BridgeMacLink maclink2 = new BridgeMacLink();
                maclink2.setNode(designatenode);
                maclink2.setBridgePort(bridgelink.getDesignateBridgePort().getBridgePort());
                if (m_nodebridgeportifindex.containsKey(bridgelink.getDesignateBridgePort().getNodeid()))
                    maclink2.setBridgePortIfIndex(m_nodebridgeportifindex.get(bridgelink.getDesignateBridgePort().getNodeid()).get(bridgelink.getDesignateBridgePort().getBridgePort()));
                if (m_nodebridgeportvlan.containsKey(bridgelink.getDesignateBridgePort().getNodeid()))
                    maclink2.setVlan(m_nodebridgeportvlan.get(bridgelink.getDesignateBridgePort().getNodeid()).get(bridgelink.getDesignateBridgePort().getBridgePort()));
                maclink2.setMacAddress(mac);
                saveBridgeMacLink(maclink2);
        }
}
*/

