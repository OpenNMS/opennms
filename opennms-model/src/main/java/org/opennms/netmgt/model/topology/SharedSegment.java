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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;

public class SharedSegment implements Topology{
    
    public static List<BridgeBridgeLink> getBridgeBridgeLinks(SharedSegment segment) throws BridgeTopologyException {
        BridgePort designatedPort = segment.getDesignatedPort();
        OnmsNode designatedNode = new OnmsNode();
        designatedNode.setId(designatedPort.getNodeId());
        List<BridgeBridgeLink> links = new ArrayList<BridgeBridgeLink>();
        for (BridgePort port:segment.getBridgePortsOnSegment()) {
            if (port.equals(designatedPort)) {
                continue;
            }
            BridgeBridgeLink link = new BridgeBridgeLink();
            OnmsNode node = new OnmsNode();
            node.setId(port.getNodeId());
            link.setNode(node);
            link.setBridgePort(port.getBridgePort());
            link.setBridgePortIfIndex(port.getBridgePortIfIndex());
            link.setVlan(port.getVlan());
            link.setDesignatedNode(designatedNode);
            link.setDesignatedPort(designatedPort.getBridgePort());
            link.setDesignatedPortIfIndex(designatedPort.getBridgePortIfIndex());
            link.setDesignatedVlan(designatedPort.getVlan());
            links.add(link);
        }
        return links;

    }

    public static List<BridgeMacLink> getBridgeMacLinks(SharedSegment segment) throws BridgeTopologyException {
        List<BridgeMacLink> maclinks = new ArrayList<BridgeMacLink>();
        for (String mac: segment.getMacsOnSegment()) {
                maclinks.add(BridgeForwardingTableEntry.
                             getBridgeMacLinkFromBridgePort(
                                segment.getDesignatedPort(), 
                                mac));
        }
        return maclinks;
    }

    
    public static SharedSegment createFrom(BridgeMacLink link) throws BridgeTopologyException {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeMacLink(link));
        segment.getMacsOnSegment().add(link.getMacAddress());
        segment.setDesignatedBridge(link.getNode().getId());
        return segment;
    }

    public static SharedSegment createFrom(BridgeBridgeLink link) throws BridgeTopologyException {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeBridgeLink(link));
        segment.getBridgePortsOnSegment().add(BridgePort.getFromDesignatedBridgeBridgeLink(link));
        segment.setDesignatedBridge(link.getDesignatedNode().getId());
        return segment;
    }
    
    public static SharedSegment split(BroadcastDomain domain,
            SharedSegment upSegment, Set<String> macs, Set<BridgePort> ports,
            Set<BridgeForwardingTableEntry> forwarders, Integer designated) {
        // remove ports from upSegment
        upSegment.getBridgePortsOnSegment().removeAll(ports);

        //remove forwarders with specified ports
        for (BridgePort port: ports) {
            Set<BridgeForwardingTableEntry> updated = new HashSet<BridgeForwardingTableEntry>();
            for (BridgeForwardingTableEntry forward: domain.getForwarders(port.getNodeId())) {
                if (forward.getBridgePort() == port.getBridgePort() ) {
                    continue;
                }
               updated.add(forward);
            }
            domain.setForwarders(port.getNodeId(), updated);
        }

        //Add macs from forwarders
        Map<String, Integer> forfpmacs = new HashMap<String, Integer>();
        for (BridgePort port: upSegment.getBridgePortsOnSegment()) {
            for (BridgeForwardingTableEntry forward: domain.getForwarders(port.getNodeId())) {
                if (forward.getBridgePort() == port.getBridgePort() ) {
                    int itemsfound=1;
                    if (forfpmacs.containsKey(forward.getMacAddress())) {
                        itemsfound = forfpmacs.get(forward.getMacAddress());
                        itemsfound++;
                    } 
                    forfpmacs.put(forward.getMacAddress(), itemsfound);   
                }
            }
        }
        for (BridgePort port: upSegment.getBridgePortsOnSegment()) {
            Set<BridgeForwardingTableEntry> updated = new HashSet<BridgeForwardingTableEntry>();
            for (BridgeForwardingTableEntry forward: domain.getForwarders(port.getNodeId())) {
                if (forward.getBridgePort() == port.getBridgePort() 
                    && forfpmacs.containsKey(forward.getMacAddress()) 
                    && forfpmacs.get(forward.getMacAddress()).intValue() >= upSegment.getBridgePortsOnSegment().size() ) {
                    continue;
                }
                updated.add(forward);
            }
            domain.setForwarders(port.getNodeId(), updated);
        }
        for (String mac: forfpmacs.keySet()) {
            if (forfpmacs.get(mac).intValue() >= upSegment.getBridgePortsOnSegment().size()) {
                upSegment.getMacsOnSegment().add(mac);
            }
        }
        
        //create segment
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().addAll(ports);
        segment.getMacsOnSegment().addAll(macs);
        segment.setDesignatedBridge(designated);
        domain.getSharedSegments().add(segment);
        //add forwarders
        for (BridgeForwardingTableEntry forward: forwarders) {
            domain.addForwarding(forward);
        }
        return segment;
    }
    
    public static void merge(BroadcastDomain domain,
        SharedSegment segment, Set<String> macs, Set<BridgePort> ports,
        Set<BridgeForwardingTableEntry> forwarders) {
        segment.getBridgePortsOnSegment().addAll(ports);
        segment.getMacsOnSegment().retainAll(macs);
        for (BridgeForwardingTableEntry forward: forwarders) {
            domain.addForwarding(forward);
        }
    }

    public static SharedSegment createAndAddToBroadcastDomain(BroadcastDomain domain, BridgePort port, Set<String> macs) {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(port);
        segment.getMacsOnSegment().addAll(macs);
        segment.setDesignatedBridge(port.getNodeId());
        domain.getSharedSegments().add(segment);
        domain.cleanForwarders(macs);
        return segment;
    }
        
    public static SharedSegment create() {
        return new SharedSegment();
                
    }
    
    private Integer m_designatedBridgeId;
    private Set<String> m_macsOnSegment = new HashSet<String>();
    private Set<BridgePort> m_portsOnSegment = new HashSet<BridgePort>();


    public boolean setDesignatedBridge(Integer designatedBridge) {
        m_designatedBridgeId = designatedBridge;
            return true;
    }

    public Integer getDesignatedBridge() {
        return m_designatedBridgeId;
    }

    public BridgePort getDesignatedPort() throws BridgeTopologyException {
        if (m_designatedBridgeId == null) {
            throw new BridgeTopologyException("Designated Bridge NodeId cannot be null", this);
        }
        BridgePort designatedbridge= getBridgePort(m_designatedBridgeId);
        if (designatedbridge == null) {
            throw new BridgeTopologyException("Designated BridgePort cannot be null", this);
        }
        return designatedbridge;
    }

    public boolean isEmpty() {
        return m_portsOnSegment.isEmpty();
    }

    public Set<BridgePort> getBridgePortsOnSegment() {
        return m_portsOnSegment;
    }        
    
    public boolean noMacsOnSegment() {
        return m_macsOnSegment.isEmpty();
    }

    public Set<Integer> getBridgeIdsOnSegment() {
        Set<Integer> nodes = new HashSet<Integer>();
        for (BridgePort link : m_portsOnSegment) {
            if (link == null || link.getNodeId() == null)
                continue;
            nodes.add(link.getNodeId());
        }
        return nodes;
    }

    public Set<String> getMacsOnSegment() {
        return m_macsOnSegment;
    }

    public boolean containsMac(String mac) {
        return m_macsOnSegment.contains(mac);
    }

    public BridgePort getBridgePort(Integer nodeid) throws BridgeTopologyException {
        if (nodeid == null)
            return null;
        for (BridgePort port: m_portsOnSegment) {
            if (port == null) {
                throw new BridgeTopologyException("Shared Segment: BridgePort cannot be null.", this);
            }
            if (port.getNodeId() == null) {
                throw new BridgeTopologyException("Shared Segment: BridgePort nodeid cannot be null.", this);
            }
            if (port.getBridgePort() == null) {
                throw new BridgeTopologyException("Shared Segment: BridgePort bridgeport cannot be null.", this);
            }
            if ( port.getNodeId().intValue() == nodeid.intValue()) {
                return port;
            }
        }
        return null;        
    }
    
    public boolean containsPort(BridgePort port) {
        return m_portsOnSegment.contains(port);
    }
    
    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append("segment -> designated bridge:[");
        strbfr.append(getDesignatedBridge());
        strbfr.append("]\n");
        for (BridgePort blink:  m_portsOnSegment) {
            strbfr.append("        -> port:");            
            if (blink == null) {
                strbfr.append("[null]");
            } else {
                strbfr.append(blink.printTopology());
            }
            strbfr.append("\n");
        }
        strbfr.append("        -> macs:");
        strbfr.append(getMacsOnSegment());
        return strbfr.toString();    	
    }

}
