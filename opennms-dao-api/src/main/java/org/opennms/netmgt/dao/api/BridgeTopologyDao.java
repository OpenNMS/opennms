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

package org.opennms.netmgt.dao.api;


import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;

public interface BridgeTopologyDao {
        
    
    // clear topology for all BroadcastDomain that are empty
    void clean();
    // update broadcastdomain
    void save(BroadcastDomain domain);
    // Load the topology from the scratch
    void load(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao);
    
    List<SharedSegment> getBridgeNodeSharedSegments(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao, int nodeid);

    SharedSegment getHostNodeSharedSegment(BridgeBridgeLinkDao bridgeBridgeLinkDao, BridgeMacLinkDao bridgeMacLinkDao, String mac);
    void delete(BroadcastDomain domain);

    // getting BroacastDomain for calculation
    BroadcastDomain get(int nodeid);
    
    Set<BroadcastDomain> getAll();

    Set<BroadcastDomain> getAllPersisted(BridgeBridgeLinkDao bridgeBridgeLinkDao, BridgeMacLinkDao bridgeMacLinkDao);

}
