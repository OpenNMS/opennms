/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;

public class SharedSegment {
    
    BridgePort m_designatedBridge;
    Set<String> m_macsOnSegment = new HashSet<String>();
    Set<BridgePort> m_portsOnSegment = new HashSet<BridgePort>();
    BroadcastDomain m_domain;
    
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
    public boolean hasDesignatedBridgeport() {
        return (m_designatedBridge != null);
    }
    
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
        m_designatedBridge = BridgePort.getBridgeFromBridgeMacLink(link);
        m_macsOnSegment.add(link.getMacAddress());
        m_portsOnSegment.add(m_designatedBridge);

    }

    public SharedSegment(BroadcastDomain domain, List<BridgeMacLink> links) {
        m_domain =domain;
        for (BridgeMacLink link: links) {
            m_portsOnSegment.add(BridgePort.getBridgeFromBridgeMacLink(link));
            m_macsOnSegment.add(link.getMacAddress());
        }

    }

    public SharedSegment(BroadcastDomain domain, Set<BridgePort> ports, Set<String> macs) {
        m_domain =domain;
        m_portsOnSegment.addAll(ports);
        m_macsOnSegment = macs;
    }
        
    public void setDesignatedBridge(Integer designatedBridge) {
        if (designatedBridge == null)
            return;
        if (m_designatedBridge != null && designatedBridge != null 
                && m_designatedBridge.getNode().getId() == designatedBridge.intValue())
            return;
        for (BridgePort port: m_portsOnSegment) {
            if (port == null)
                continue;
            if ( port.getNode() != null &&
                    port.getNode().getId() != null
                    && port.getNode().getId().intValue() == designatedBridge.intValue()) {
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
            if (port == null) 
                continue;
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
    			maclinks.add(BridgePort.getBridgeMacLink(bp, mac));
    		}
    	}
        return maclinks;
    }
    
    public boolean noMacsOnSegment() {
        return m_macsOnSegment.isEmpty();
    }

    public void add(BridgeMacLink link) {
        m_macsOnSegment.add(link.getMacAddress());
        m_portsOnSegment.add(BridgePort.getBridgeFromBridgeMacLink(link));
    }

    public void add(BridgeBridgeLink link) {
        m_portsOnSegment.add(BridgePort.getFromBridgeBridgeLink(link));
        m_portsOnSegment.add(BridgePort.getFromDesignatedBridgeBridgeLink(link));
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
        if (bridgeId == null)
            return;
    	Set<BridgePort> portsOnSegment = new HashSet<BridgePort>();
        for (BridgePort bp: m_portsOnSegment) {
        	if ( bp.getNode() == null ||
        	     bp.getNode().getId() == null ||  
        	        bp.getNode().getId().intValue() == bridgeId.intValue())
        		continue;
        	portsOnSegment.add(bp);
        }
        for (BridgePort port: shared.getBridgePortsOnSegment()) {
            if (port.getNode() == null || 
                    port.getNode().getId() == null
                    || port.getNode().getId().intValue() == bridgeId.intValue())
                continue;
            portsOnSegment.add(port);
        }
        m_portsOnSegment = portsOnSegment;
    	m_macsOnSegment.addAll(shared.getMacsOnSegment());    	
    }

    public void retain(Set<String> macs, BridgePort dlink) {
        m_portsOnSegment.add(dlink);
        m_macsOnSegment.retainAll(macs);
    }
    
    public void assign(Set<String> macs, BridgePort dlink) {
        m_portsOnSegment.add(dlink);
        m_macsOnSegment = macs;
    }

    public void removeBridge(int bridgeId) {
        if (m_portsOnSegment.isEmpty())
            return;
        Set<BridgePort> updateportsonsegment = new HashSet<BridgePort>();
        for (BridgePort port: m_portsOnSegment) {
            if (port.getNode() != null &&
                    port.getNode().getId() != null
                    && port.getNode().getId().intValue() == bridgeId)
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
            if (link == null || link.getNode() == null)
                continue;
            if (link.getNode().getId() != null)
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
        	if (port.getNode().getId().intValue() != nodeid.intValue()) {
        		continue;
        	}
        	if (port.getBridgePort().intValue() != bridgeport.intValue()) {
        		continue;
        	}
        	return true;
        }
        return false;
    }

    public BridgePort getBridgePort(Integer nodeid) {
        if (nodeid == null)
            return null;
        for (BridgePort link: m_portsOnSegment) {
                if (link.getNode() != null &&
                        link.getNode().getId() != null &&
                        link.getNode().getId().intValue() == nodeid.intValue() )
                    return link;
        }
        return null;        
    }
    
    public Integer getPortForBridge(Integer nodeid) {
        if (nodeid == null)
            return null;
        for (BridgePort link: m_portsOnSegment) {
                if (link.getNode() != null 
                        && link.getNode().getId() != null 
                        && link.getNode().getId().intValue() == nodeid.intValue() )
                    return link.getBridgePort();
        }
        return null;
    }

    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
            strbfr.append("segment ->\nsegment bridges:");
            strbfr.append(getBridgeIdsOnSegment());
            strbfr.append(", designated bridge:[");
            strbfr.append(getDesignatedBridge());
            strbfr.append(", designated port:");
            strbfr.append(getDesignatedPort());
            strbfr.append("]\n");
            for (BridgePort blink:  m_portsOnSegment)
                strbfr.append(blink.printTopology());
            for (String mac: getMacsOnSegment()) {
                strbfr.append("segment mac:");
                strbfr.append(mac);
                strbfr.append("\n");
            }
            
            return strbfr.toString();    	
    }
}
