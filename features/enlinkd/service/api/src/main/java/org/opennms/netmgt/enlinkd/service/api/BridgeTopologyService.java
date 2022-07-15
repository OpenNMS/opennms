/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.api;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink;
import org.opennms.netmgt.model.OnmsNode;

public interface BridgeTopologyService extends TopologyService {

    int maxlevel = 30;

    static List<BridgeMacLink> create(BridgePort bp, Set<String> macs, BridgeMacLink.BridgeMacLinkType type) {
        List<BridgeMacLink> maclinks = new ArrayList<>();
        macs.forEach(mac -> maclinks.add(create(bp, mac, type)));
        return maclinks;
    }

    static BridgeMacLink create(BridgePort bp, String macAddress, BridgeMacLink.BridgeMacLinkType type) {
        BridgeMacLink maclink = new BridgeMacLink();
        OnmsNode node = new OnmsNode();
        node.setId(bp.getNodeId());
        maclink.setNode(node);
        maclink.setBridgePort(bp.getBridgePort());
        maclink.setBridgePortIfIndex(bp.getBridgePortIfIndex());
        maclink.setMacAddress(macAddress);
        maclink.setVlan(bp.getVlan());
        maclink.setLinkType(type);
        return maclink;
    }

    static BridgePortWithMacs create(BridgePort port, Set<String> macs) throws BridgeTopologyException {
        if (port == null) {
            throw new BridgeTopologyException("cannot create BridgePortWithMacs bridge port is null");
        }
        if (macs == null) {
            throw new BridgeTopologyException("cannot create BridgePortWithMacs macs is null");
        }
        return new BridgePortWithMacs(port,macs);

    }

    static void hierarchySetUp(BroadcastDomain domain, Bridge root) throws BridgeTopologyException {
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

    static void tier(BroadcastDomain domain, SharedSegment segment, Integer rootid, int level) throws BridgeTopologyException {
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

    static boolean loadTopologyEntry(BroadcastDomain domain, SharedSegment segment) {
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
    static void clearTopologyForBridge(BroadcastDomain domain, Integer bridgeid) throws BridgeTopologyException {
        Bridge bridge = domain.getBridge(bridgeid);
        if (bridge == null) {
            throw new BridgeTopologyException("clearTopologyForBridge: Bridge must be not null:", domain);
        }
        if (bridge.getNodeId() == null) {
            throw new BridgeTopologyException("clearTopologyForBridge: Bridge Nodeid must be not null:", domain);
        }

        if (bridge.isNewTopology()) {
            return;
        }

        Set<Bridge> notnew = new HashSet<>();
        for (Bridge cbridge: domain.getBridges()) {
            if (cbridge.isNewTopology()) {
                continue;
            }
            notnew.add(cbridge);
        }

        if (notnew.size() == 1) {
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
        domain.cleanForwarders(bridge.getNodeId());
        bridge.setRootPort(null);
        if (toberemoved == null) {
            return;
        } else {
            topsegment.getBridgePortsOnSegment().remove(toberemoved);
        }

        List<SharedSegment> topology = new ArrayList<>();

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

        domain.setTopology(topology);
        //assigning again the forwarders to segment if is the case
        Map<String, Set<BridgePort>> forwardermap = new HashMap<>();
        for (BridgePortWithMacs forwarder: domain.getForwarding()) {
            for (String mac: forwarder.getMacs()) {
                if (!forwardermap.containsKey(mac)) {
                    forwardermap.put(mac, new HashSet<>());
                }
                forwardermap.get(mac).add(forwarder.getPort());
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

    static void removeBridge(BroadcastDomain domain, int bridgeId) throws BridgeTopologyException {
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
        Set<Bridge> bridges = new HashSet<>();
        for (Bridge cur: domain.getBridges()) {
            if (cur.getNodeId() == bridgeId)
                continue;
            bridges.add(cur);
        }
        domain.setBridges(bridges);
    }

    static Set<BridgeForwardingTableEntry> get(BridgePortWithMacs bft) {
        Set<BridgeForwardingTableEntry> bftentries = new HashSet<>();
        bft.getMacs().forEach(mac -> {
            BridgeForwardingTableEntry bftentry = new BridgeForwardingTableEntry();
            bftentry.setNodeId(bft.getPort().getNodeId());
            bftentry.setBridgePort(bft.getPort().getBridgePort());
            bftentry.setBridgePortIfIndex(bft.getPort().getBridgePortIfIndex());
            bftentry.setVlan(bft.getPort().getVlan());
            bftentry.setMacAddress(mac);
            bftentry.setBridgeDot1qTpFdbStatus(BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
            bftentries.add(bftentry);
        });
        return bftentries;
    }

    // this indicates the total size of in memory bft
    boolean collectBft(int nodeid, int maxsize);

    void collectedBft(int nodeid);
    // Load the topology from the scratch
    void load();
    
    List<SharedSegment> getSharedSegments(int nodeid);

    SharedSegment getSharedSegment(String mac);
    
    void delete(int nodeid) throws BridgeTopologyException;
    
    BroadcastDomain reconcile(BroadcastDomain domain,int nodeid) throws BridgeTopologyException;

    void reconcile(int nodeId, Date now);

    void store(int nodeId, BridgeElement bridge);

    void store(int nodeId, BridgeStpLink link);

    void store(int nodeId, List<BridgeForwardingTableEntry> bft);
    
    void store(BroadcastDomain domain, Date now) throws BridgeTopologyException;
    
    void add(BroadcastDomain domain);
        
    void updateBridgeOnDomain(BroadcastDomain domain,Integer nodeid);

    Set<BroadcastDomain> findAll();
    
    BroadcastDomain getBroadcastDomain(int nodeId);
    
    Map<Integer, Set<BridgeForwardingTableEntry>> getUpdateBftMap();
    
    Set<BridgeForwardingTableEntry> useBridgeTopologyUpdateBFT(int nodeid);
    
    List<TopologyShared> match();
    
    List<MacPort> getMacPorts(); 
    

    
}
