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

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.BridgeBridgeLink;





/**
 * <p>BridgeBridgeLinkDao interface.</p>
 */
public interface BridgeBridgeLinkDao extends OnmsDao<BridgeBridgeLink, Integer> {
    
    public List<BridgeBridgeLink> findByNodeId(Integer id);

    public List<BridgeBridgeLink> findByDesignatedNodeId(Integer id);

    public BridgeBridgeLink getByNodeIdBridgePort(Integer id, Integer port);

    public BridgeBridgeLink getByNodeIdBridgePortIfIndex(Integer id, Integer ifindex);

    public List<BridgeBridgeLink> getByDesignatedNodeIdBridgePort(Integer id, Integer port);

    public List<BridgeBridgeLink> getByDesignatedNodeIdBridgePortIfIndex(Integer id, Integer ifindex);

    void deleteByNodeIdOlderThen(Integer nodeiId, Date now);

    void deleteByDesignatedNodeIdOlderThen(Integer nodeiId, Date now);
    
    void deleteByNodeId(Integer nodeiId);

    void deleteByDesignatedNodeId(Integer nodeiId);

}
