/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.topology.LinkableNode;
import org.opennms.netmgt.model.topology.LinkableSnmpNode;

/**
 * <p>QueryManager interface.</p>
 *
 * @author antonio
 * @version $Id: $
 */
public interface QueryManager {

    /**
     * <p>getSnmpNodeList</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    List<LinkableSnmpNode> getSnmpNodeList();

    /**
     * <p>getSnmpNode</p>
     *
     * @param nodeid a int.
     * @return a {@link org.opennms.netmgt.model.topology.LinkableNode} object.
     * @throws java.sql.SQLException if any.
     */
    LinkableSnmpNode getSnmpNode(int nodeid);

    /**
     * <p>updateDeletedNodes</p>
     *
     * @throws java.sql.SQLException if any.
     */
    void updateDeletedNodes();

    /**
     * <p>storeSnmpCollection</p>
     *
     * @param node a {@link org.opennms.netmgt.model.topology.LinkableNode} object.
     * @param snmpColl a {@link org.opennms.netmgt.linkd.SnmpCollection} object.
     * @return a {@link org.opennms.netmgt.model.topology.LinkableNode} object.
     * @throws java.sql.SQLException if any.
     */
    LinkableNode storeSnmpCollection(LinkableNode node, SnmpCollection snmpColl, Linkd linkd);
    
    /**
     * <p>storeDiscoveryLink</p>
     *
     * @param discoveryLink a {@link org.opennms.netmgt.linkd.DiscoveryLink} object.
     * @throws java.sql.SQLException if any.
     */
    void storeDiscoveryLink(DiscoveryLink discoveryLink);
    
    /**
     * <p>update</p>
     *
     * @param nodeid a int.
     * @param action a char.
     * @throws java.sql.SQLException if any.
     */
    void update(int nodeid, StatusType action,Set<String> activePackages);
    
    /**
     * <p>updateForInterface</p>
     *
     * @param nodeid a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @param action a char.
     * @throws java.sql.SQLException if any.
     */
    void updateForInterface(int nodeid, String ipAddr, int ifIndex, StatusType action,Set<String> activePackages);
    
}
