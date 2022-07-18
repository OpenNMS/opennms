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

package org.opennms.netmgt.enlinkd.service.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BroadcastDomain implements Topology {


    private final Set<Bridge> m_bridges = new HashSet<>();
    private final List<SharedSegment> m_topology = new ArrayList<>();
    private final Set<BridgePortWithMacs> m_forwarding = new HashSet<>();

    public void setBridges(Set<Bridge> bridges) {
        m_bridges.clear();
        m_bridges.addAll(bridges);
    }

    public void setTopology(List<SharedSegment> topology) {
        m_topology.clear();
        m_topology.addAll(topology);
    }

    public void cleanForwarders() {
        cleanForwarders(getMacsOnSegments());
    }
    
    public void cleanForwarders(Set<String> macs) {
        m_forwarding.forEach(bpm -> bpm.getMacs().removeAll(macs));
    }

    public void addForwarding(BridgePort forwardport, String forwardmac) {
      for (BridgePortWithMacs bpm: m_forwarding) {
            if (bpm.getPort().equals(forwardport)) {
                bpm.getMacs().add(forwardmac);
                return;
            }
        }
        BridgePortWithMacs bpm = new BridgePortWithMacs(forwardport,new HashSet<>());
        bpm.getMacs().add(forwardmac);
        m_forwarding.add(bpm);
    }
        
    public void setForwarders(Set<BridgePortWithMacs> forwarders) {
        m_forwarding.addAll(forwarders);
    }

    public Set<BridgePortWithMacs> getForwarding() {
        return m_forwarding;
    }

    public Set<BridgePortWithMacs> getForwarders(Integer bridgeId) {
        Set<BridgePortWithMacs> bridgeforwarders = new HashSet<>();
        m_forwarding.stream().filter(bfm -> Objects.equals(bfm.getPort().getNodeId(), bridgeId)).forEach(bridgeforwarders::add);
        return bridgeforwarders;
    }

    public void cleanForwarders(Integer bridgeId) {
        Set<BridgePortWithMacs> bridgeforwarders = getForwarders(bridgeId);
        m_forwarding.removeAll(bridgeforwarders);
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
        Set<Integer> bridgeIds = new HashSet<>();
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
            if (bridge.getNodeId() == bridgeId)
                return bridge;
        }
        return null;
    }

    public Set<String> getMacsOnSegments() {
        Set<String>macs = new HashSet<>();
        for (SharedSegment segment: m_topology) 
            macs.addAll(segment.getMacsOnSegment());
        return macs;
    }
        
    public List<SharedSegment> getSharedSegments(Integer bridgeId) {
        List<SharedSegment> segmentsOnBridge = new ArrayList<>();
        for (SharedSegment segment: m_topology) {
            if (segment.getBridgeIdsOnSegment().contains(bridgeId)) 
                segmentsOnBridge.add(segment);
        }
        return segmentsOnBridge;
    }
    
    public Set<Bridge> getBridgeOnSharedSegment(SharedSegment segment) {
        Set<Integer> nodeidsOnSegment = new HashSet<>(segment.getBridgeIdsOnSegment());
        Set<Bridge> bridgesOn = new HashSet<>();
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
    
    @Override
    public String printTopology() {
    	final StringBuffer strbfr = new StringBuffer();
        strbfr.append("<--- broadcast domain ....-----");
        getBridges().forEach(bridge -> {
            strbfr.append("\n");
            strbfr.append(bridge.printTopology());            
        });
        
        Bridge rootBridge = getRootBridge();
    	if ( rootBridge != null && !m_topology.isEmpty()) {
    		Set<Integer> rootids = new HashSet<>();
    		rootids.add(rootBridge.getNodeId());
    		strbfr.append(printTopologyFromLevel(rootids,0));
    	} else {
    	    m_topology.forEach(shared ->  strbfr.append(shared.printTopology()));
    	}
        strbfr.append("\n----forwarders----");
        m_forwarding.forEach(bfte -> {
                strbfr.append("\nforward -> ");
                strbfr.append(bfte.printTopology());
        });
        strbfr.append("\n.... broadcast domain .....--->");
    	return strbfr.toString();
    }
    
    public String printTopologyFromLevel(Set<Integer> bridgeIds, int level) {
    	Set<Integer> bridgesDownLevel = new HashSet<>();
    	final StringBuffer strbfr = new StringBuffer();
        strbfr.append("\n------level ");
    	strbfr.append(level);
        strbfr.append(" -----\n");

        strbfr.append("bridges on level:");
        strbfr.append(bridgeIds);
        
        bridgeIds.stream()
                .map(this::getBridge)
                .filter(Objects::nonNull)
                .forEach(bridge -> {
                    for (SharedSegment segment: getSharedSegments(bridge.getNodeId())) {
                        if (segment.getDesignatedBridge().intValue() == bridge.getNodeId().intValue()) {
                            strbfr.append("\n");
                            strbfr.append(segment.printTopology());
                            bridgesDownLevel.addAll(segment.getBridgeIdsOnSegment());
                        }
                    }
                });
        bridgesDownLevel.removeAll(bridgeIds);
    	if (!bridgesDownLevel.isEmpty()) {
    		strbfr.append(printTopologyFromLevel(bridgesDownLevel,level+1));
    	}
    	return strbfr.toString();
    }    
}
