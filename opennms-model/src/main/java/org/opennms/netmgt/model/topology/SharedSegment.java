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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeMacLinkType;

public class SharedSegment implements Topology{
    
    public static List<BridgeBridgeLink> getBridgeBridgeLinks(SharedSegment segment) throws BridgeTopologyException {
        return BridgeForwardingTableEntry.create(segment.getDesignatedPort(), segment.getBridgePortsOnSegment());
    }

    public static List<BridgeMacLink> getBridgeMacLinks(SharedSegment segment) throws BridgeTopologyException {
        return BridgeForwardingTableEntry.create(segment.getDesignatedPort(), segment.getMacsOnSegment(), BridgeMacLinkType.BRIDGE_LINK);
    }
    
    public static SharedSegment create(BridgeMacLink link) throws BridgeTopologyException {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeMacLink(link));
        segment.getMacsOnSegment().add(link.getMacAddress());
        segment.setDesignatedBridge(link.getNode().getId());
        return segment;
    }

    public static SharedSegment create(BridgeBridgeLink link) throws BridgeTopologyException {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeBridgeLink(link));
        segment.getBridgePortsOnSegment().add(BridgePort.getFromDesignatedBridgeBridgeLink(link));
        segment.setDesignatedBridge(link.getDesignatedNode().getId());
        return segment;
    }
        
    public static void merge(BroadcastDomain domain,
        SharedSegment upsegment, 
        Map<BridgePortWithMacs, Set<BridgePortWithMacs>> splitted, 
        Set<String> macsonsegment, 
        BridgePort rootport,
        Set<BridgePortWithMacs> throughset) {
        
        splitted.keySet().stream().forEach( designated -> {
            Set<BridgePortWithMacs> ports = splitted.get(designated);
            SharedSegment splitsegment = new SharedSegment();
            splitsegment.getBridgePortsOnSegment().add(designated.getPort());
            splitsegment.setDesignatedBridge(designated.getPort().getNodeId());
            Set<String> macs = new HashSet<String>(designated.getMacs());
            ports.stream().forEach( bft -> 
            {
                macs.retainAll(bft.getMacs());
                domain.cleanForwarders(bft.getPort().getNodeId());
                upsegment.getBridgePortsOnSegment().remove(bft.getPort());
                splitsegment.getBridgePortsOnSegment().add(bft.getPort());
            });
            splitsegment.getMacsOnSegment().addAll(macs);
            domain.getSharedSegments().add(splitsegment);
            domain.cleanForwarders(macs);
        });
        
        //Add macs from forwarders
        Map<String, Integer> forfpmacs = new HashMap<String, Integer>();
        upsegment.getBridgePortsOnSegment().stream().forEach( port ->  
        {
            domain.getForwarders(port.getNodeId()).stream().filter(forward -> forward.getPort().equals(port)).
            forEach( forward -> 
            {    
                forward.getMacs().stream().forEach(mac -> {
                    int itemsfound=1;
                    if (forfpmacs.containsKey(mac)) {
                        itemsfound = forfpmacs.get(mac);
                        itemsfound++;
                    } 
                    forfpmacs.put(mac, itemsfound);   
                });
            });
            
            Set<String> clearmacs = new HashSet<String>();
            forfpmacs.keySet().stream().forEach( mac -> {
                if (forfpmacs.get(mac).intValue() == upsegment.getBridgePortsOnSegment().size()) {
                    upsegment.getMacsOnSegment().add(mac);
                    clearmacs.add(mac);
                }
            });
            domain.cleanForwarders(clearmacs);
            
        });

        upsegment.getBridgePortsOnSegment().add(rootport);
        upsegment.getMacsOnSegment().retainAll(macsonsegment);
        domain.cleanForwarders(upsegment.getMacsOnSegment());

        throughset.stream().forEach(bft -> SharedSegment.createAndAddToBroadcastDomain(domain,
                                                                                       bft));
    }

    public static SharedSegment createAndAddToBroadcastDomain(BroadcastDomain domain, BridgePortWithMacs bft) {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(bft.getPort());
        segment.getMacsOnSegment().addAll(bft.getMacs());
        segment.setDesignatedBridge(bft.getPort().getNodeId());
        domain.getSharedSegments().add(segment);
        domain.cleanForwarders(bft.getMacs());
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
