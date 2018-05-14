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
    
    public static int maxlevel = 30;
    public static final int DOMAIN_MATCH_MIN_SIZE = 20;
    public static final float DOMAIN_MATCH_MIN_RATIO = 0.5f;
    
    public static boolean checkMacSets(Set<String> setA, Set<String> setB) {
        Set<String>retainedSet = new HashSet<String>(setB);
        retainedSet.retainAll(setA);
        // should contain at list 20 or 50% of the all size
        if (retainedSet.size() > DOMAIN_MATCH_MIN_SIZE
            || retainedSet.size() > setA.size() * DOMAIN_MATCH_MIN_RATIO
            || retainedSet.size() > setB.size() * DOMAIN_MATCH_MIN_RATIO
                ) {
            return true;
        }
        return false;
    }

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

    public static Bridge getUpperBridge(BroadcastDomain domain, Bridge electableroot, int level) throws BridgeTopologyException {
        if (level == maxlevel) {
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

    public static Map<Integer,Integer> getUpperForwardingBridgePorts(BroadcastDomain domain, Bridge bridge, Map<Integer,Integer> downports, int level) throws BridgeTopologyException {
        if (level == maxlevel) {
            throw new BridgeTopologyException("getUpperForwardingBridgePorts: too many iteration", bridge);
        }
        
        if (bridge.isRootBridge()) {
            return downports;
        }

        SharedSegment upSegment = domain.getSharedSegment(bridge.getNodeId(), bridge.getRootPort());
        if (upSegment == null) {
            throw new BridgeTopologyException("getUpperForwardingBridgePorts: no up segment", bridge);
        }
        
        Bridge upBridge = domain.getBridge(upSegment.getDesignatedBridge());
        if (upBridge == null) {
            throw new BridgeTopologyException("getUpperForwardingBridgePorts: no designated bridge on segment", bridge);
        }
        BridgePort bp = upSegment.getBridgePort(upBridge.getNodeId());
        downports.put(bp.getNodeId(),bp.getBridgePort());
        return getUpperForwardingBridgePorts(domain, upBridge, downports,++level);
    }
    
    public static Set<BridgeForwardingTableEntry> calculateBFT(
            BroadcastDomain domain, Bridge bridge)
            throws BridgeTopologyException {
                
        Map<Integer, Set<String>> bft = new HashMap<Integer, Set<String>>();
        Map<Integer, BridgePort> portifindexmap = new HashMap<Integer, BridgePort>();
        Integer bridgeId = bridge.getNodeId();

        
        Map<Integer,Integer> upperForwardingBridgePorts = getUpperForwardingBridgePorts(domain, bridge,new HashMap<Integer,Integer>(),0);
        
        Map<Integer,Integer> bridgeIdtobridgePortOnBridge = new HashMap<Integer, Integer>();
        
        for (Integer upperbridgeid: upperForwardingBridgePorts.keySet()) {
            bridgeIdtobridgePortOnBridge.put(upperbridgeid, bridge.getRootPort());
        }
                
        // 
        for (SharedSegment segment : domain.getSharedSegments()) {
            
           Integer bridgeport = null;
            
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) {
                BridgePort bport = segment.getBridgePort(bridgeId);
                portifindexmap.put(bport.getBridgePort(), bport);
                bridgeport = bport.getBridgePort();
            } else {
                bridgeport = getCalculateBFT(domain, segment, bridge, bridgeIdtobridgePortOnBridge,new HashSet<Integer>(),0);
            }

            if (!bft.containsKey(bridgeport)) {
                bft.put(bridgeport, new HashSet<String>());
            }
            bft.get(bridgeport).addAll(segment.getMacsOnSegment());
        }

        Set<BridgeForwardingTableEntry> links = new HashSet<BridgeForwardingTableEntry>();
        
        for (Integer forwbridgeId : domain.getForwarding().keySet()) {
            if (forwbridgeId.intValue() == bridgeId.intValue()) {
                links.addAll(domain.getForwarders(bridgeId));
                continue;
            }
            Integer bridgeport = bridgeIdtobridgePortOnBridge.get(forwbridgeId);
            Integer removedBridgePort = upperForwardingBridgePorts.get(forwbridgeId);
            if (removedBridgePort == null) {
                removedBridgePort = domain.getBridge(forwbridgeId).getRootPort();
            }

            for (BridgeForwardingTableEntry forwarder: domain.getForwarders(forwbridgeId)) {
                if (forwarder.getBridgePort().intValue() == removedBridgePort.intValue()) {
                    continue;
                }
                bft.get(bridgeport).add(forwarder.getMacAddress());
            }
        }

        for (Integer bridgePort : bft.keySet()) {
            for (String mac : bft.get(bridgePort)) {
                BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
                link.setNodeId(bridgeId);
                link.setBridgePort(bridgePort);
                if (portifindexmap.get(bridgePort) != null) {
                    link.setBridgePortIfIndex(portifindexmap.get(bridgePort).getBridgePortIfIndex());
                    link.setVlan(portifindexmap.get(bridgePort).getVlan());
                }
                link.setMacAddress(mac);
                link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
                links.add(link);
            }
        }
        return links;
    }
    
    public static Integer getCalculateBFT(BroadcastDomain domain, SharedSegment segment,Bridge bridge, Map<Integer,Integer> bridgetobridgeport, Set<Integer> downBridgeIds,int level) throws BridgeTopologyException {
        if (level == maxlevel) {
            throw new BridgeTopologyException("getCalculateBFT: too many iteration", domain);
        }
        
        for (Integer bridgeIdOnsegment: segment.getBridgeIdsOnSegment()) {
            if (bridgetobridgeport.containsKey(bridgeIdOnsegment)) {
                Integer bridgeport = bridgetobridgeport.get(bridgeIdOnsegment);
                for (Integer bridgeidonsegment: downBridgeIds) {
                    bridgetobridgeport.put(bridgeidonsegment, bridgeport);
                }

                return bridgeport;
            }
        }        
        // if segment is on the bridge then...
        Integer upBridgeId = segment.getDesignatedBridge();

        if (upBridgeId.intValue() == bridge.getNodeId().intValue()) {
            for (Integer bridgeidonsegment: downBridgeIds) {
                bridgetobridgeport.put(bridgeidonsegment, segment.getDesignatedPort().getBridgePort());
            }
            return segment.getDesignatedPort().getBridgePort();
        }
        // if segment is a root segment add mac on port
        if (upBridgeId.intValue() == domain.getRootBridge().getNodeId().intValue()) {
            for (Integer bridgeidonsegment: downBridgeIds) {
                bridgetobridgeport.put(bridgeidonsegment, bridge.getRootPort());
            }
            return bridge.getRootPort();
        }
        
        downBridgeIds.addAll(segment.getBridgeIdsOnSegment());
                
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
            throw new BridgeTopologyException("getCalculateBFT: cannot find up bridge on domain", domain);
        }
        SharedSegment up = domain.getSharedSegment(upBridge.getNodeId(),upBridge.getRootPort());
        if (up == null) {
            throw new BridgeTopologyException("getCalculateBFT: cannot find up segment on domain", domain);
        }

        return getCalculateBFT(domain,up, bridge,bridgetobridgeport,downBridgeIds, ++level);
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
        if (level == maxlevel) {
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
        //assigning again the forwarders to segment if is the case 
        Map<String, Set<BridgePort>> forwardermap =  new HashMap<String, Set<BridgePort>>();
        for (Integer forwarderNode: domain.getForwarding().keySet()) {
            for (BridgeForwardingTableEntry forwarder: domain.getForwarders(forwarderNode)) {
                if (!forwardermap.containsKey(forwarder.getMacAddress())) {
                    forwardermap.put(forwarder.getMacAddress(), new HashSet<BridgePort>());                    
                }
                forwardermap.get(forwarder.getMacAddress()).add(BridgePort.getFromBridgeForwardingTableEntry(forwarder));
            }
        }
        for (String mac: forwardermap.keySet()) {
            SharedSegment first = domain.getSharedSegment(forwardermap.get(mac).iterator().next());
            if (first == null) {
                continue;
            }
            if (forwardermap.get(mac).containsAll(first.getBridgePortsOnSegment())) {
                first.getMacsOnSegment().add(mac);
            }
        }
        domain.cleanForwarders();
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

    public void cleanForwarders() {
        cleanForwarders(getMacsOnDomain());
    }
    
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
        m_forwarding.clear();
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
        
        bridgeIds.stream()
                .map(id -> getBridge(id))
                .filter(bridge -> bridge != null)
                .forEach(bridge -> {
                    for (SharedSegment segment: getSharedSegments(bridge.getNodeId())) {
                        if (segment.getDesignatedBridge().intValue() == bridge.getNodeId().intValue()) {
                            strbfr.append(segment.printTopology());
                            bridgesDownLevel.addAll(segment.getBridgeIdsOnSegment());
                        }
                    }
                });
        bridgesDownLevel.removeAll(bridgeIds);
    	if (!bridgesDownLevel.isEmpty())
    		strbfr.append(printTopologyFromLevel(bridgesDownLevel,level+1));
    	return strbfr.toString();
    }    
}
