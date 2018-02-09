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

import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;

public class BroadcastDomain implements BridgeTopology {

    public static Bridge electRootBridge(BroadcastDomain domain) throws BridgeTopologyException {
        if (domain.getBridges().size() == 1) {
            return domain.getBridges().iterator().next();
        }
        //well only one root bridge should be defined....
        //otherwise we need to skip calculation
        //so here is the place were we can
        //manage multi stp domains...
        //ignoring for the moment....
        for (Bridge electable: domain.getBridges()) {
            if (electable.getDesignated() != null) {
                return getUpperBridge(domain,electable,0);
            }
        }
        return null;
    }

    private static Bridge getUpperBridge(BroadcastDomain domain, Bridge electableroot, int level) throws BridgeTopologyException {
        if (level == 30) {
            throw new BridgeTopologyException("getUpperBridge, too many iterations", electableroot);
        }
        for (Bridge electable: domain.getBridges()) {
            if (electable.getIdentifiers().contains(electableroot.getDesignated())) {
                return getUpperBridge(domain,electable, ++level);
            }
        }
        return electableroot;        
    }

    
    public static Set<BridgeForwardingTableEntry> calculateRootBFT(BroadcastDomain domain) throws BridgeTopologyException {
        Bridge root = domain.getRootBridge();
        if (root == null) {
            return null;
        }
        return calculateBFT(domain,root);
    }
    
    public static Set<BridgeForwardingTableEntry> calculateBFT(BroadcastDomain domain,Bridge bridge) throws BridgeTopologyException {
        Map<Integer,Set<String>> bft = new HashMap<Integer, Set<String>>();
        Map<Integer,BridgePort> portifindexmap = new HashMap<Integer, BridgePort>();
        Integer bridgeId = bridge.getNodeId();
        Set<BridgeForwardingTableEntry> links = new HashSet<BridgeForwardingTableEntry>();
        
        for (SharedSegment segment: domain.getSharedSegments(bridgeId)) {
            BridgePort bridgeport = segment.getBridgePort(bridgeId);
            portifindexmap.put(bridgeport.getBridgePort(), bridgeport);

        }
        
        for (SharedSegment segment: domain.getSharedSegments()) { //FIXME ConcurrentModificationException NMS-9557
            
            Set<String> macs = segment.getMacsOnSegment();                
            if (macs.isEmpty()) {
                continue;
            }
            Integer bridgeport = goUp(domain,segment,bridge,0);
            if (!bft.containsKey(bridgeport)) {
                bft.put(bridgeport, new HashSet<String>());
            }
            bft.get(bridgeport).addAll(macs);
            links.addAll(domain.getForwarders(bridgeId));
       }
            
        for (Integer bridgePort: bft.keySet()) {
            for (String mac: bft.get(bridgePort)) {
                BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
                link.setNodeId(bridgeId);
                link.setBridgePort(bridgePort);
                if (portifindexmap.get(bridgePort) == null) {
                    continue;
                }
                link.setBridgePortIfIndex(portifindexmap.get(bridgePort).getBridgePortIfIndex());
                link.setVlan(portifindexmap.get(bridgePort).getVlan());
                link.setMacAddress(mac);
                link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                links.add(link);
            }
        }
        return links;
    }
    
    private static Integer goUp(BroadcastDomain domain, SharedSegment down,Bridge bridge, int level) throws BridgeTopologyException {
        if (level == 30) {
            throw new BridgeTopologyException("goUp: to many iteration", down);
        }
            Integer upBridgeId = down.getDesignatedBridge();
            // if segment is on the bridge then...
            if (upBridgeId.intValue() == bridge.getNodeId().intValue()) {
                return down.getDesignatedPort().getBridgePort();
            }
            // if segment is a root segment add mac on port
            if (upBridgeId.intValue() == domain.getRootBridge().getNodeId().intValue()) {
                return bridge.getRootPort();
            }
            // iterate until you got it
            Bridge upBridge = null;
            for (Bridge cbridge: domain.getBridges()) {
                if (cbridge.getNodeId().intValue() == bridge.getNodeId().intValue())
                    continue;
                if (cbridge.getNodeId().intValue() == upBridgeId.intValue()) {
                    upBridge=cbridge;
                    break;
                }
            }
            if (upBridge == null) {
                return null;
            }
            SharedSegment up = domain.getSharedSegment(upBridge.getNodeId(),upBridge.getRootPort());
            if (up == null) {
                return null;
            }
        return goUp(domain,up, bridge,++level);
    }    

    
    public static void hierarchySetUp(BroadcastDomain domain, Bridge root) throws BridgeTopologyException {
        if (root==null || root.isRootBridge()) {
            return;
        }
        root.setRootBridge();
        if (domain.getBridges().size() == 1) {
            return;
        }
        for (SharedSegment segment : domain.getSharedSegments(root.getNodeId())) {
            segment.setDesignatedBridge(root.getNodeId());
            tier(domain,segment, root.getNodeId(), 0);
        }
    }
    
    private static void tier(BroadcastDomain domain, SharedSegment segment, Integer rootid, int level) throws BridgeTopologyException {
        if (segment == null) {
            return;
        }
        level++;
        if (level == 30) {
            return;
        }
        for (Integer bridgeid: segment.getBridgeIdsOnSegment()) {
            if (bridgeid.intValue() == rootid.intValue())
                continue;
            Bridge bridge = domain.getBridge(bridgeid);
            if (bridge == null)
                return;
            bridge.setRootPort(segment.getBridgePort(bridgeid).getBridgePort());
            for (SharedSegment s2: domain.getSharedSegments(bridgeid)) {
                if (s2.getDesignatedBridge() != null && s2.getDesignatedBridge().intValue() == rootid.intValue())
                    continue;
                s2.setDesignatedBridge(bridgeid);
                tier(domain,s2,bridgeid,level);
            }
        }
    }

    public static boolean loadTopologyEntry(BroadcastDomain domain, SharedSegment segment) {
        for (BridgePort port: segment.getBridgePortsOnSegment()) {
            for ( Bridge bridge: domain.getBridges() ) {
                if ( port.getNodeId().intValue() == bridge.getNodeId().intValue()) {
                    domain.getSharedSegments().add(segment);
                    return true;
                }
            }
        }
        return false;
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
    public static void clearTopologyForBridge(BroadcastDomain domain, Integer bridgeid) throws BridgeTopologyException {
        Bridge bridge = domain.getBridge(bridgeid);
        if (bridge == null) {
            throw new BridgeTopologyException("clearTopologyForBridge: Bridge must be not null:", domain);
        }
        if (bridge.getNodeId() == null) {
            throw new BridgeTopologyException("clearTopologyForBridge: Bridge Nodeid must be not null:", domain);
        }
        if (domain.getBridges().size() == 1) {
            domain.clearTopology();
            return;
        }
        SharedSegment topsegment = null;
        if (bridge.isRootBridge()) {
            for (SharedSegment segment: domain.getSharedSegments(bridge.getNodeId())) {
                Integer newRootId = null;
                for (BridgePort port: segment.getBridgePortsOnSegment() ) {
                    if (port == null 
                            || port.getNodeId() == null
                            ||port.getBridgePort() == null) {
                    continue;
                    }
                    if (segment.getDesignatedBridge() == null || port.getNodeId().intValue() != segment.getDesignatedBridge().intValue()) {
                        newRootId = port.getNodeId();
                    }
                }
                if (newRootId == null)
                    continue;
                Bridge newRootBridge=domain.getBridge(newRootId);
                if (newRootBridge == null)
                    continue;
                topsegment = domain.getSharedSegment(newRootId,newRootBridge.getRootPort());
                hierarchySetUp(domain,newRootBridge);
                break;
            }
        } else {
            topsegment = domain.getSharedSegment(bridge.getNodeId(), bridge.getRootPort());
        }
 
        if (topsegment == null ) {
            return;
        }
        
        BridgePort toberemoved = topsegment.getBridgePort(bridge.getNodeId());
        if (toberemoved == null) {
            return;
        } else {
            topsegment.getBridgePortsOnSegment().remove(toberemoved);
        }
                
        List<SharedSegment> topology = new ArrayList<SharedSegment>();
 
        for (SharedSegment segment: domain.getSharedSegments()) {
            if (segment.getBridgeIdsOnSegment().contains(bridge.getNodeId())) { 
                for (BridgePort port: segment.getBridgePortsOnSegment()) {
                    if ( port.getNodeId().intValue() == bridge.getNodeId().intValue()) {
                        continue;
                    }
                    topsegment.getBridgePortsOnSegment().add(port);
                }
                topsegment.getMacsOnSegment().addAll(segment.getMacsOnSegment());    
            } else {
                topology.add(segment);
            }
        }
        domain.m_topology = topology;
        domain.getForwarding().remove(bridge.getNodeId());
        bridge.setRootPort(null);
    }

    public static void removeBridge(BroadcastDomain domain,int bridgeId) throws BridgeTopologyException {
        Bridge bridge = null;
        for (Bridge curbridge: domain.getBridges()) {
            if (curbridge.getNodeId() == bridgeId) {
                bridge=curbridge;
                break;
            }
        }
        // if not in domain: return
        if (bridge==null)
            return;
        // if last bridge in domain: clear all and return
        if (domain.getBridges().size() == 1) {
            domain.getSharedSegments().clear();
            domain.getBridges().clear();
            return;
        }
        
        clearTopologyForBridge(domain,bridgeId);
        Set<Bridge> bridges = new HashSet<Bridge>();
        for (Bridge cur: domain.getBridges()) {
            if (cur.getNodeId().intValue() == bridgeId) 
                continue;
            bridges.add(cur);
        }
        domain.m_bridges = bridges;            
    }

    
    private volatile Set<Bridge> m_bridges = new HashSet<Bridge>();
    private volatile List<SharedSegment> m_topology = new ArrayList<SharedSegment>();    
    private Map<Integer,Set<BridgeForwardingTableEntry>> m_forwarding = new HashMap<Integer,Set<BridgeForwardingTableEntry>>();

    public void cleanForwarders(Set<String> macs) {
        Map<Integer, Set<BridgeForwardingTableEntry>> forwardingMap=new HashMap<Integer, Set<BridgeForwardingTableEntry>>();
        for (Integer bridgeId: m_forwarding.keySet()) {
            Set<BridgeForwardingTableEntry> forwarders = new HashSet<BridgeForwardingTableEntry>();
            for (BridgeForwardingTableEntry forward: m_forwarding.get(bridgeId)) {
                if (macs.contains(forward.getMacAddress())) {
                    continue;
                }
                forwarders.add(forward);
            }
            if (forwarders.isEmpty()) {
                continue;
            }
            forwardingMap.put(bridgeId, forwarders);
        }
        m_forwarding = forwardingMap;
    }

    public void addForwarding(BridgeForwardingTableEntry forward) {
        Integer bridgeid = forward.getNodeId();
        if (bridgeid == null) {
            return;
        }
        if (!m_forwarding.containsKey(bridgeid)) {
            m_forwarding.put(bridgeid, new HashSet<BridgeForwardingTableEntry>());
        }
        m_forwarding.get(bridgeid).add(forward);
    }
    
    public void setForwarding(Map<Integer,Set<BridgeForwardingTableEntry>> forwarding) {
        m_forwarding = forwarding;
    }
    
    public void setForwarders(Integer bridgeid, Set<BridgeForwardingTableEntry> forwarders) {
        m_forwarding.put(bridgeid, forwarders);
    }

    public Map<Integer,Set<BridgeForwardingTableEntry>> getForwarding() {
        return m_forwarding;
    }

    public Set<BridgeForwardingTableEntry> getForwarders(Integer bridgeId) {
        if (m_forwarding.containsKey(bridgeId)) {
            return m_forwarding.get(bridgeId);
        }
        return new HashSet<BridgeForwardingTableEntry>();
    }
        
    public void clearTopology() {
        m_topology.clear();
        for (Bridge bridge: m_bridges) {
            bridge.setRootPort(null);
        }
    }
    
    public boolean isEmpty() {
        return m_bridges.isEmpty();
    }

    public Set<Integer> getBridgeNodesOnDomain() {
        Set<Integer> bridgeIds = new HashSet<Integer>();
        for (Bridge bridge: m_bridges) 
            bridgeIds.add(bridge.getNodeId());
        return bridgeIds;
    }

    public Set<Bridge> getBridges() {
        return m_bridges;
    }
    
    public List<SharedSegment> getSharedSegments() {
        return m_topology;
    }
        
    public Bridge getRootBridge() {
        for (Bridge bridge: m_bridges) {
            if (bridge.isRootBridge())
                return bridge;
        }
        return null;
    }

    public Bridge getBridge(int bridgeId) {
        for (Bridge bridge: m_bridges) {
            if (bridge.getNodeId().intValue() == bridgeId)
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
        
    public List<SharedSegment> getSharedSegments(Integer bridgeId) {
        List<SharedSegment> segmentsOnBridge = new ArrayList<SharedSegment>();
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) 
                segmentsOnBridge.add(segment);
        }
        return segmentsOnBridge;
    }
    
    public Set<Bridge> getBridgeOnSharedSegment(SharedSegment segment) {
        Set<Integer> nodeidsOnSegment = new HashSet<Integer>(segment.getBridgeIdsOnSegment());
        Set<Bridge> bridgesOn = new HashSet<Bridge>();
        for (Bridge bridge: m_bridges) {
            if (nodeidsOnSegment.contains(bridge.getNodeId()))
                bridgesOn.add(bridge);
        }
        return bridgesOn;
    }

    public SharedSegment getSharedSegment(Integer bridgeid, Integer bridgePort) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(bridgeid);
        bp.setBridgePort(bridgePort);
        return getSharedSegment(bp);
    }    

    public SharedSegment getSharedSegment(BridgePort bridgePort) {
        if (bridgePort == null)
            return null;
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgePortsOnSegment().contains(bridgePort)) { 
                return segment;
            }
        }
        return null;
    }    
    
    
    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append("------broadcast domain-----");
        for (Bridge bridge: getBridges()) {
            strbfr.append("\n");
            strbfr.append(bridge.printTopology());
        }
        Bridge rootBridge = getRootBridge();
    	if ( rootBridge != null && !m_topology.isEmpty()) {
    		Set<Integer> rootids = new HashSet<Integer>();
    		rootids.add(rootBridge.getNodeId());
    		strbfr.append(printTopologyFromLevel(rootids,0));
    	} else {
    	    for (SharedSegment shared: m_topology) {
    	        strbfr.append(shared.printTopology());
    	    }
    	}
        for (Set<BridgeForwardingTableEntry> bfteset: m_forwarding.values()) {
            for (BridgeForwardingTableEntry bfte:bfteset) {
                strbfr.append("\n");
                strbfr.append("        -> forwarder: ");
                strbfr.append(bfte.printTopology());
            }
        }
        strbfr.append("\n");
        strbfr.append("------broadcast domain-----");
    	return strbfr.toString();
    }
    
    public String printTopologyFromLevel(Set<Integer> bridgeIds, int level) {
    	Set<Integer> bridgesDownLevel = new HashSet<Integer>();
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append("\n------level ");
    	strbfr.append(level);
        strbfr.append(" -----\n");

        strbfr.append("bridges on level:");
        strbfr.append(bridgeIds);
        strbfr.append("\n");
        for (Integer bridgeid : bridgeIds) {
        	for (SharedSegment segment: getSharedSegments(bridgeid)) {
        		if (segment.getDesignatedBridge().intValue() == bridgeid.intValue()) {
        			strbfr.append(segment.printTopology());
                                strbfr.append("\n");
        			bridgesDownLevel.addAll(segment.getBridgeIdsOnSegment());
        		}
        	}
        }
        
        strbfr.append("------level ");
    	strbfr.append(level);
        strbfr.append(" -----");
        bridgesDownLevel.removeAll(bridgeIds);
    	if (!bridgesDownLevel.isEmpty())
    		strbfr.append(printTopologyFromLevel(bridgesDownLevel,level+1));
    	return strbfr.toString();
    }    
}
