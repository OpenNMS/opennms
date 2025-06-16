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


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink;

public interface BridgeTopologyService extends TopologyService {

    static SharedSegment createSharedSegmentFromBridgeMacLink(BridgeMacLink link) {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(getBridgePortFromBridgeMacLink(link));
        segment.getMacsOnSegment().add(link.getMacAddress());
        segment.setDesignatedBridge(link.getNode().getId());
        segment.setCreateTime(link.getBridgeMacLinkCreateTime());
        segment.setLastPollTime(link.getBridgeMacLinkLastPollTime());
        return segment;
    }

    static SharedSegment createSharedSegmentFromBridgeBridgeLink(BridgeBridgeLink link) {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(getBridgePortFromBridgeBridgeLink(link));
        segment.getBridgePortsOnSegment().add(getBridgePortFromDesignatedBridgeBridgeLink(link));
        segment.setDesignatedBridge(link.getDesignatedNode().getId());
        segment.setCreateTime(link.getBridgeBridgeLinkCreateTime());
        segment.setLastPollTime(link.getBridgeBridgeLinkLastPollTime());
        return segment;
    }

    static BridgePort getBridgePortFromBridgeMacLink(BridgeMacLink link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getNode().getId());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setVlan(link.getVlan());
        return bp;
    }

    static BridgePort getBridgePortFromBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getNode().getId());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setVlan(link.getVlan());
        return bp;
    }

    static BridgePort getBridgePortFromDesignatedBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getDesignatedNode().getId());
        bp.setBridgePort(link.getDesignatedPort());
        bp.setBridgePortIfIndex(link.getDesignatedPortIfIndex());
        bp.setVlan(link.getDesignatedVlan());
        return bp;
    }

    Set<String> getBridgeIdentifiers(Bridge bridge);

    String getBridgeDesignatedIdentifier(Bridge bridge);
    // this indicates the total size of in memory bft
    boolean collectBft(int nodeid, int maxsize);

    void collectedBft(int nodeid);
    // Load the topology from the scratch
    void load();
    
    List<SharedSegment> getSharedSegments(int nodeid);

    SharedSegment getSharedSegment(String mac);
    
    void delete(int nodeid);
    
    BroadcastDomain reconcile(BroadcastDomain domain,int nodeid) ;

    void reconcile(int nodeId, Date now);

    void store(int nodeId, BridgeElement bridge);

    void store(int nodeId, BridgeStpLink link);

    void store(int nodeId, List<BridgeForwardingTableEntry> bft);
    
    void store(BroadcastDomain domain, Date now);
    
    void add(BroadcastDomain domain);
        
    void updateBridgeOnDomain(BroadcastDomain domain,Integer nodeid);

    Set<BroadcastDomain> findAll();
    
    BroadcastDomain getBroadcastDomain(int nodeId);
    
    Map<Integer, Set<BridgeForwardingTableEntry>> getUpdateBftMap();
    
    Set<BridgeForwardingTableEntry> useBridgeTopologyUpdateBFT(int nodeid);
    
    List<TopologyShared> match();
    
    List<MacPort> getMacPorts();

    void deletePersistedData();
    
}
