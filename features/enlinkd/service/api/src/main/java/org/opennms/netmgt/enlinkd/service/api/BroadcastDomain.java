/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.service.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.util.Assert;

public class BroadcastDomain implements Topology {


    public static final int maxlevel = 30;
    private final Set<Bridge> m_bridges = new HashSet<>();
    private final List<SharedSegment> m_topology = new ArrayList<>();
    private final Set<BridgePortWithMacs> m_forwarding = new HashSet<>();

    public void add(BridgePortWithMacs bft) {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(bft.getPort());
        segment.getMacsOnSegment().addAll(bft.getMacs());
        segment.setDesignatedBridge(bft.getPort().getNodeId());
        m_topology.add(segment);
        cleanForwarders(bft.getMacs());
    }

    public void merge(SharedSegment upsegment,
                             Map<BridgePortWithMacs, Set<BridgePortWithMacs>> splitted,
                             Set<String> macsonsegment,
                             BridgePort rootport,
                             Set<BridgePortWithMacs> throughset) {

        Assert.notNull(upsegment);
        if (!m_topology.contains(upsegment)) {
            return;
        }
        splitted.keySet().forEach(designated -> {
            Set<BridgePortWithMacs> ports = splitted.get(designated);
            SharedSegment splitsegment = new SharedSegment();
            splitsegment.getBridgePortsOnSegment().add(designated.getPort());
            splitsegment.setDesignatedBridge(designated.getPort().getNodeId());
            Set<String> macs = new HashSet<>(designated.getMacs());
            ports.forEach(bft ->
            {
                macs.retainAll(bft.getMacs());
                cleanForwarders(bft.getPort().getNodeId());
                upsegment.getBridgePortsOnSegment().remove(bft.getPort());
                splitsegment.getBridgePortsOnSegment().add(bft.getPort());
            });
            splitsegment.getMacsOnSegment().addAll(macs);
            m_topology.add(splitsegment);
            cleanForwarders(macs);
        });

        //Add macs from forwarders
        Map<String, Integer> forfpmacs = new HashMap<>();
        upsegment.getBridgePortsOnSegment().forEach(port ->
        {
            getForwarders(port.getNodeId()).stream().filter(forward -> forward.getPort().equals(port)).
            forEach( forward ->
                    forward.getMacs().forEach(mac -> {
                        int itemsfound=1;
                        if (forfpmacs.containsKey(mac)) {
                            itemsfound = forfpmacs.get(mac);
                            itemsfound++;
                        }
                        forfpmacs.put(mac, itemsfound);
                    }));

            Set<String> clearmacs = new HashSet<>();
            forfpmacs.keySet().forEach(mac -> {
                if (forfpmacs.get(mac) == upsegment.getBridgePortsOnSegment().size()) {
                    upsegment.getMacsOnSegment().add(mac);
                    clearmacs.add(mac);
                }
            });
            cleanForwarders(clearmacs);

        });

        upsegment.getBridgePortsOnSegment().add(rootport);
        upsegment.getMacsOnSegment().retainAll(macsonsegment);
        cleanForwarders(upsegment.getMacsOnSegment());

        throughset.forEach(this::add);
    }

    public void removeBridge(int bridgeId) {
        Bridge bridge = getBridge(bridgeId);
        // if not in domain: return
        if (bridge==null)
            return;
        // if last bridge in domain: clear all and return
        if (getBridges().size() == 1) {
            m_topology.clear();
            m_bridges.clear();
            return;
        }

        clearTopologyForBridge(bridgeId);
        m_bridges.remove(bridge);
        Set<Bridge> bridges = new HashSet<>();
        for (Bridge cur: getBridges()) {
            if (cur.getNodeId() == bridgeId)
                continue;
            bridges.add(cur);
        }
        setBridges(bridges);
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
    public void clearTopologyForBridge(Integer bridgeid) {
        Bridge bridge = getBridge(bridgeid);
        if (bridge == null) {
            return;
        }

        if (bridge.isNewTopology()) {
            return;
        }

        Set<Bridge> notnew = new HashSet<>();
        for (Bridge cbridge : getBridges()) {
            if (cbridge.isNewTopology()) {
                continue;
            }
            notnew.add(cbridge);
        }

        if (notnew.size() == 1) {
            clearTopology();
            return;
        }

        SharedSegment topsegment = null;
        if (bridge.isRootBridge()) {
            for (SharedSegment segment : getSharedSegments(bridge.getNodeId())) {
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
                Bridge newRootBridge = getBridge(newRootId);
                if (newRootBridge == null)
                    continue;
                topsegment = getSharedSegment(newRootId,newRootBridge.getRootPort());
                hierarchySetUp(newRootBridge);
                break;
            }
        } else {
            topsegment = getSharedSegment(bridge.getNodeId(), bridge.getRootPort());
        }

        if (topsegment == null ) {
            return;
        }

        BridgePort toberemoved = topsegment.getBridgePort(bridge.getNodeId());
        cleanForwarders(bridge.getNodeId());
        bridge.setRootPort(null);
        if (toberemoved == null) {
            return;
        } else {
            topsegment.getBridgePortsOnSegment().remove(toberemoved);
        }

        List<SharedSegment> topology = new ArrayList<>();

        for (SharedSegment segment : getSharedSegments()) {
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

        setTopology(topology);
        //assigning again the forwarders to segment if is the case
        Map<String, Set<BridgePort>> forwardermap = new HashMap<>();
        for (BridgePortWithMacs forwarder: getForwarding()) {
            for (String mac: forwarder.getMacs()) {
                if (!forwardermap.containsKey(mac)) {
                    forwardermap.put(mac, new HashSet<>());
                }
                forwardermap.get(mac).add(forwarder.getPort());
            }
        }

        for (String mac: forwardermap.keySet()) {
            SharedSegment first = getSharedSegment(forwardermap.get(mac).iterator().next());
            if (first == null) {
                continue;
            }
            if (forwardermap.get(mac).containsAll(first.getBridgePortsOnSegment())) {
                first.getMacsOnSegment().add(mac);
            }
        }
        cleanForwarders();
    }

    public boolean loadTopologyEntry(SharedSegment segment) {
        for (BridgePort port : segment.getBridgePortsOnSegment()) {
            for ( Bridge bridge : getBridges() ) {
                if ( port.getNodeId().intValue() == bridge.getNodeId().intValue()) {
                    getSharedSegments().add(segment);
                    return true;
                }
            }
        }
        return false;
    }

    public void hierarchySetUp(Bridge root) {
        if (root == null || getBridge(root.getNodeId()) == null) {
            return;
        }
        if (root.isRootBridge()) {
            return;
        }
        root.setRootBridge();
        if (getBridges().size() == 1) {
            return;
        }
        for (SharedSegment segment : getSharedSegments(root.getNodeId())) {
            segment.setDesignatedBridge(root.getNodeId());
            hierarchySetUpGo(segment, root.getNodeId(), 0);
        }
    }

    private void hierarchySetUpGo(SharedSegment segment, Integer rootid, int level) {
        if (segment == null) {
            return;
        }
        level++;
        if (level == maxlevel) {
            return;
        }
        for (Integer bridgeid : segment.getBridgeIdsOnSegment()) {
            if (bridgeid.intValue() == rootid.intValue())
                continue;
            Bridge bridge = getBridge(bridgeid);
            if (bridge == null)
                return;
            bridge.setRootPort(segment.getBridgePort(bridgeid).getBridgePort());
            for (SharedSegment s2 : getSharedSegments(bridgeid)) {
                if (s2.getDesignatedBridge() != null && s2.getDesignatedBridge().intValue() == rootid.intValue())
                    continue;
                s2.setDesignatedBridge(bridgeid);
                hierarchySetUpGo(s2,bridgeid,level);
            }
        }
    }

    public void addforwarders(BridgeForwardingTable bridgeFT) {
        Set<String> macs = new HashSet<>(getMacsOnSegments());
        cleanForwarders(bridgeFT.getNodeId());
        for (String forward :  bridgeFT.getMactoport().keySet()) {
            if (macs.contains(forward)) {
                continue;
            }
            addForwarding(bridgeFT.getMactoport().get(forward), forward);
        }
    }

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
