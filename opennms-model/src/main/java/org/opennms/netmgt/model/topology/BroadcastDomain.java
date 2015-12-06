package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;

public class BroadcastDomain {

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

        public Set<BridgeElement> getBridgeElementIds() {
            return m_bridgeIds;
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

        public void setPortMacs(Set<String> portMacs) {
            m_portMacs = portMacs;
        }

        public void addPortMac(String portMac) {
            m_portMacs.add(portMac);
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

    Set<Integer> m_updatedNodes = new HashSet<Integer>();
    Set<Bridge> m_bridges = new HashSet<Bridge>();
    List<SharedSegment> m_topology = new ArrayList<SharedSegment>();
    boolean m_topologyChanged = false;
    volatile Map<Integer, List<BridgeMacLink>> m_notYetParsedBFTMap = new HashMap<Integer, List<BridgeMacLink>>();
    volatile Map<Integer, List<BridgeStpLink>> m_notYetParsedSTPMap = new HashMap<Integer, List<BridgeStpLink>>();

    
    public synchronized boolean topologyChanged() {
        return m_topologyChanged;
    }

    public synchronized Set<Integer> getUpdatedNodes() {
      return m_updatedNodes;  
    }

    //FIXME
    public synchronized Date getUpdateTime() {
        return null;
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

    }
    
    public synchronized boolean isEmpty() {
        return m_bridges.isEmpty();
    }
    
    public synchronized void calculate() {
        
    }
}

/*     FIXME check reuse code
 * 
volatile Map<Integer,Map<Integer,Set<String>>> m_bftMap = new HashMap<Integer, Map<Integer,Set<String>>>();

volatile Map<Integer,Map<Integer,Integer>> m_nodebridgeportifindex = new HashMap<Integer, Map<Integer,Integer>>();

volatile Map<Integer, Map<Integer,Integer>> m_nodebridgeportvlan =new HashMap<Integer, Map<Integer,Integer>>();

@Override
public void storeBridgeToIfIndexMap(int nodeid, Map<Integer,Integer> bridgeportifindex) {
    m_nodebridgeportifindex.put(nodeid, bridgeportifindex);
}

@Override
public void storeBridgetoVlanMap(int nodeId, Set<Integer> bridgeports, Integer vlanid) {
    Map<Integer,Integer> portvlan = new HashMap<Integer, Integer>();
    if (m_nodebridgeportvlan.containsKey(nodeId)) 
        portvlan = m_nodebridgeportvlan.get(nodeId);
    for (Integer bridgeport: bridgeports) {
        if (portvlan.containsKey(bridgeport)) {
            if (portvlan.get(bridgeport) == vlanid) {
                continue;
            }   else { 
                // port is a trunk
                portvlan.remove(bridgeport);
                continue;
            }
        }
        portvlan.put(bridgeport, vlanid);
    }
    m_nodebridgeportvlan.put(nodeId, portvlan);
}
*/

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

