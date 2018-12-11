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


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink;

public interface BridgeTopologyService extends TopologyService {

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
