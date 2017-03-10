package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;

public class SharedSegment {
    
    BridgePort m_designatedBridge;
    Set<String> m_macsOnSegment = new HashSet<String>();
    Set<BridgePort> m_portsOnSegment = new HashSet<BridgePort>();
    BroadcastDomain m_domain;

    private BridgePort getBridgeFromBridgeMacLink(BridgeMacLink link) {
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

    private BridgeMacLink getBridgeMacLink(BridgePort bp, String mac) {
    	BridgeMacLink maclink = new BridgeMacLink();
        maclink.setNode(bp.getNode());
        maclink.setBridgePort(bp.getBridgePort());
        maclink.setBridgePortIfIndex(bp.getBridgePortIfIndex());
        maclink.setBridgePortIfName(bp.getBridgePortIfName());
        maclink.setMacAddress(mac);
        maclink.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        maclink.setVlan(bp.getVlan());
        maclink.setBridgeMacLinkCreateTime(bp.getCreateTime());
        maclink.setBridgeMacLinkLastPollTime(bp.getPollTime());
        return maclink;
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
        m_designatedBridge = getBridgeFromBridgeMacLink(link);
        m_macsOnSegment.add(link.getMacAddress());
        m_portsOnSegment.add(m_designatedBridge);

    }

    public SharedSegment(BroadcastDomain domain, List<BridgeMacLink> links) {
        m_domain =domain;
        for (BridgeMacLink link: links) {
        	m_portsOnSegment.add(getBridgeFromBridgeMacLink(link));
            m_macsOnSegment.add(link.getMacAddress());
        }

    }

    public SharedSegment(BroadcastDomain domain, BridgeBridgeLink link, Set<String> macs) {
        m_domain =domain;
        m_portsOnSegment.add(getFromDesignatedBridgeBridgeLink(link));
        m_portsOnSegment.add(getFromBridgeBridgeLink(link));
        m_macsOnSegment = macs;
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
        return m_portsOnSegment.isEmpty();
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
    	List<BridgeMacLink> maclinks = new ArrayList<BridgeMacLink>();
    	for (String mac: m_macsOnSegment) {
    		for (BridgePort bp: m_portsOnSegment) {
    			maclinks.add(getBridgeMacLink(bp, mac));
    		}
    	}
        return maclinks;
    }
    
    public boolean noMacsOnSegment() {
        return m_macsOnSegment.isEmpty();
    }

    public void add(BridgeMacLink link) {
        m_macsOnSegment.add(link.getMacAddress());
        m_portsOnSegment.add(getBridgeFromBridgeMacLink(link));
    }

    public void add(BridgeBridgeLink dlink) {
        final BridgePort designated = getFromDesignatedBridgeBridgeLink(dlink);
        final BridgePort bridge = getFromBridgeBridgeLink(dlink);
        m_portsOnSegment.add(designated);
        m_portsOnSegment.add(bridge);
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
    	Set<BridgePort> portsOnSegment = new HashSet<BridgePort>();
        for (BridgePort bp: m_portsOnSegment) {
        	if (bp.getNode().getId().intValue() == bridgeId.intValue())
        		continue;
        	portsOnSegment.add(bp);
        }
        for (BridgePort port: shared.getBridgePortsOnSegment()) {
            if (port.getNode().getId().intValue() == bridgeId.intValue())
                continue;
            portsOnSegment.add(port);
        }
        m_portsOnSegment = portsOnSegment;
    	m_macsOnSegment.addAll(shared.getMacsOnSegment());    	
    }

    public void assign(Set<String> macs, BridgeBridgeLink dlink) {
    	if (isEmpty() ) {
     		add(dlink);
    		m_macsOnSegment = macs;
    		return;
    	}
 	    add(dlink);          
 	    m_macsOnSegment.retainAll(macs);
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
    }
    
    public void removeMacs(Set<String> mactoberemoved) {
        m_macsOnSegment.removeAll(mactoberemoved);
    }
    
    public Integer getFirstNoDesignatedBridge() {
        for (Integer bridgeId: getBridgeIdsOnSegment()) {
            if (m_designatedBridge == null || bridgeId != m_designatedBridge.getNode().getId())
                return bridgeId;
        }
        return null;
    }

    public Set<String> getMacsOnSegment() {
        return m_macsOnSegment;
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
		return m_macsOnSegment.contains(mac);
    }

    public boolean containsPort(Integer nodeid, Integer bridgeport) {
        if (nodeid == null || bridgeport == null) {
            return false;
        }
        for (BridgePort port: m_portsOnSegment) {
        	if (port.getNode().getId() != nodeid) {
        		continue;
        	}
        	if (port.getBridgePort() != bridgeport) {
        		continue;
        	}
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
    
    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
            strbfr.append("segment:[bridges:");
            strbfr.append(getBridgeIdsOnSegment());
            strbfr.append(", designated bridge:[");
            strbfr.append(getDesignatedBridge());
            strbfr.append("], designated port:");
            strbfr.append(getDesignatedPort());
            strbfr.append(", macs:");
            strbfr.append(getMacsOnSegment());
            strbfr.append("]\n");
            for (BridgeBridgeLink blink:  getBridgeBridgeLinks())
            	strbfr.append(blink.printTopology());
            for (BridgeMacLink mlink: getBridgeMacLinks()) 
            	strbfr.append(mlink.printTopology());
            
            return strbfr.toString();    	
    }
}
