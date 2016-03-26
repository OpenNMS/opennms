package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;

public class SharedSegment {
    private class BridgePort {

        private OnmsNode m_node;
        private Integer m_bridgePort;
        private Integer m_bridgePortIfIndex;
        private String  m_bridgePortIfName;
        private Integer m_vlan;
        private Date m_createTime;
        private Date m_pollTime;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result
                    + ((m_bridgePort == null) ? 0 : m_bridgePort.hashCode());
            result = prime * result
                    + ((m_node == null) ? 0 : m_node.getId().hashCode());
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
            BridgePort other = (BridgePort) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (m_bridgePort == null) {
                if (other.m_bridgePort != null)
                    return false;
            } else if (!m_bridgePort.equals(other.m_bridgePort))
                return false;
            if (m_node == null) {
                if (other.m_node != null)
                    return false;
            } else if (!m_node.getId().equals(other.m_node.getId()))
                return false;
            return true;
        }

        public OnmsNode getNode() {
            return m_node;
        }
        public void setNode(OnmsNode node) {
            m_node = node;
        }
        public Integer getBridgePort() {
            return m_bridgePort;
        }
        public void setBridgePort(Integer bridgePort) {
            m_bridgePort = bridgePort;
        }
        public Integer getBridgePortIfIndex() {
            return m_bridgePortIfIndex;
        }
        public void setBridgePortIfIndex(Integer bridgePortIfIndex) {
            m_bridgePortIfIndex = bridgePortIfIndex;
        }
        public String getBridgePortIfName() {
            return m_bridgePortIfName;
        }
        public void setBridgePortIfName(String bridgePortIfName) {
            m_bridgePortIfName = bridgePortIfName;
        }
        public Integer getVlan() {
            return m_vlan;
        }
        public void setVlan(Integer vlan) {
            m_vlan = vlan;
        }
        private SharedSegment getOuterType() {
            return SharedSegment.this;
        }
        public Date getCreateTime() {
            return m_createTime;
        }
        public void setCreateTime(Date time) {
            m_createTime = time;
        }
        public Date getPollTime() {
            return m_pollTime;
        }
        public void setPollTime(Date time) {
            m_pollTime = time;
        }
        
    }
    
    BridgePort m_designatedBridge;
    List<BridgeMacLink> m_macsOnSegment = new ArrayList<BridgeMacLink>();
    Set<BridgePort> m_portsOnSegment = new HashSet<BridgePort>();
    BroadcastDomain m_domain;
    
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
        m_macsOnSegment.add(link);
        m_portsOnSegment.add(m_designatedBridge);
    }

    public SharedSegment(BroadcastDomain domain, BridgeBridgeLink link) {
        m_domain =domain;
        m_portsOnSegment.add(getFromDesignatedBridgeBridgeLink(link));
        m_portsOnSegment.add(getFromBridgeBridgeLink(link));
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
        return m_macsOnSegment.isEmpty() && m_portsOnSegment.isEmpty();
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
        return m_macsOnSegment;
    }
    
    public boolean noMacsOnSegment() {
        return m_macsOnSegment.isEmpty();
    }

    public void setBridgeMacLinks(List<BridgeMacLink> links) {
        m_macsOnSegment = links;
        for (BridgeMacLink link: links)
            m_portsOnSegment.add(getFromBridgeMacLink(link));
    }

    public void add(BridgeMacLink link) {
        m_macsOnSegment.add(link);
        m_portsOnSegment.add(getFromBridgeMacLink(link));
    }
    
    public void add(BridgeBridgeLink dlink) {
        m_portsOnSegment.add(getFromDesignatedBridgeBridgeLink(dlink));
        m_portsOnSegment.add(getFromBridgeBridgeLink(dlink));
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
        for (BridgeMacLink link: m_macsOnSegment) {
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
        m_macsOnSegment.addAll(toadd);
        
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
                    m_macsOnSegment.add(nlink);
                }
                continue;
            } 
            m_macsOnSegment.add(mlink);    
        }
        for (BridgePort port: shared.getBridgePortsOnSegment()) {
            if (port.getNode().getId().intValue() == bridgeId.intValue())
                continue;
            m_portsOnSegment.add(port);
        }
    }

    public void assign(List<BridgeMacLink> links, BridgeBridgeLink dlink) {
        
        add(dlink);
                
        Map<BridgeMacLinkHash,BridgeMacLink> sharedsegmentmaclinks = new HashMap<BridgeMacLinkHash,BridgeMacLink>();
        for (BridgeMacLink link: links)
            sharedsegmentmaclinks.put(new BridgeMacLinkHash(link),link);
        //intersection is not null, then we have to add all the BridgeMacLink
        // for each mac address
        for (BridgeMacLink link: m_macsOnSegment) {
            sharedsegmentmaclinks.put(new BridgeMacLinkHash(link),link);
            m_portsOnSegment.add(getFromBridgeMacLink(link));
        }
        m_macsOnSegment = new ArrayList<BridgeMacLink>(sharedsegmentmaclinks.values());
    }

    public void removeBridge(int bridgeId) {
        if (m_portsOnSegment.isEmpty())
            return;
        Set<BridgePort> updateportsonsegment = new HashSet<SharedSegment.BridgePort>();
        for (BridgePort port: m_portsOnSegment) {
            if (port.getNode().getId().intValue() == bridgeId)
                continue;
            updateportsonsegment.add(port);
        }
        m_portsOnSegment = updateportsonsegment;
        
        List<BridgeMacLink> updatemacsonsegment = new ArrayList<BridgeMacLink>();
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
            if (m_designatedBridge == null || bridgeId != m_designatedBridge.getNode().getId())
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
        for (BridgePort link: m_portsOnSegment) {
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
        for (BridgePort link: m_portsOnSegment) {
            if (link.getNode().getId().intValue() == nodeid.intValue() && 
                    link.getBridgePort().intValue() == bridgeport.intValue())
                return true;
        }
        return false;
    }
    
    public Integer getPortForBridge(Integer nodeid) {
        if (nodeid == null)
            return null;
        if (m_macsOnSegment.isEmpty()) {
            for (BridgePort link: m_portsOnSegment) {
                if (link.getNode().getId().intValue() == nodeid.intValue() )
                    return link.getBridgePort();
            }
            return null;
        }
        return null;
    }

    
}
