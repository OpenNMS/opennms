package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeMacLink;

public class BroadcastDomain {

    private class Bridge {

        final Integer m_id;
        Set<String> m_bridgeIds = new HashSet<String>();
        Set<String> m_portMacs = new HashSet<String>();
        Integer m_rootPort;
        boolean m_isRootBridge=false;

        public Bridge(Integer id) {
            super();
            m_id = id;
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

        public Bridge(Integer id, String bridgeId) {
            m_id = id;
            m_bridgeIds.add(bridgeId);
        }

        public Set<String> getBridgeIds() {
            return m_bridgeIds;
        }

        public void setBridgeIds(Set<String> bridgeIds) {
            m_bridgeIds = bridgeIds;
        }

        public void addBridgeId(String bridgeId) {
            m_bridgeIds.add(bridgeId);
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
            if (m_id == null) {
                if (other.m_id != null)
                    return false;
            } else if (!m_id.equals(other.m_id))
                return false;
            return true;
        }

        
    }

    List<Bridge> m_bridges = new ArrayList<Bridge>();
    List<SharedSegment> m_topology = new ArrayList<SharedSegment>();
    
    volatile Map<Integer,Map<Integer,Set<String>>> m_bftMap = new HashMap<Integer, Map<Integer,Set<String>>>();

    volatile Map<Integer,Map<Integer,Integer>> m_nodebridgeportifindex = new HashMap<Integer, Map<Integer,Integer>>();
    
    volatile Map<Integer, Map<Integer,Integer>> m_nodebridgeportvlan =new HashMap<Integer, Map<Integer,Integer>>();

    /*     FIXME check reuse code
     * 
            List<SharedSegment> segments = new ArrayList<SharedSegment>();
            for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
                for (SharedSegment segment: segments) {
                    if (segment.containsMac(link.getMacAddress())) {
                        segment.add(link);
                        break;
                    }
                    if (segment.containsPort(link.getNode().getId(), link.getBridgePort())) {
                        segment.add(link);
                        break;
                    }
                }
                SharedSegment segment = new SharedSegment();
                segment.add(link);
                segments.add(segment);
            }

            for (BridgeBridgeLink link: m_bridgeBridgeLinkDao.findAll()) {
                for (SharedSegment segment: segments) {
                    if (segment.containsPort(link.getNode().getId(), link.getBridgePort())) {
                        segment.add(link);
                        break;
                    }
                    if (segment.containsPort(link.getDesignatedNode().getId(), link.getDesignatedPort())) {
                        segment.add(link);
                        break;
                    }
                }
                SharedSegment segment = new SharedSegment();
                segment.add(link);
                segments.add(segment);
            }
            for (SharedSegment segment: segments)
                m_bridgeTopologyDao.parse(segment);

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

    public boolean topologyChanged(int nodeid) {
        return false;
    }

    public List<Integer> getUpdatedNodes(int nodeid) {
      return null;  
    };

    public Date getUpdateTime(int nodeid) {
        return null;
    }

    public List<Bridge> getBridges() {
        return m_bridges;
    }
    
    public void setBridges(List<Bridge> bridges) {
        m_bridges = bridges;
    }
            
    public void addBridge(Bridge bridge) {
        m_bridges.add(bridge);
    }
    
    public Set<String> getMacsOnDomain() {
        Set<String>macs = new HashSet<String>();
        return macs;
    }
    
    public boolean checkBridgeOnDomain(List<BridgeMacLink> links) {
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
    
    public List<SharedSegment> getTopology() {
        return null;
    }
    
    
}
