/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.api;

import java.net.InetAddress;

import org.opennms.netmgt.model.OnmsIpInterface;

/**
 * Used to create {@link CollectionAgent}s for a given IP interface.
 *
 * @author jwhite
 */
public interface CollectionAgentFactory {

    CollectionAgent createCollectionAgent(OnmsIpInterface ipIf);

    CollectionAgent createCollectionAgent(String nodeCriteria, InetAddress ipAddr);

    /**
     * Create a collection agent for the given IP interface, and
     * optionally override the node's location.
     *
     * Overriding the node's location is strictly used for testing
     * (i.e. via the collection:collect) command in the Karaf console
     * and is not used in normal operations.
     *
     * @param nodeCriteria node id or fs:fid
     * @param ipAddr ip address associated with the node
     * @param location <b>null</b> if the nodes existing location should be used
     * @return the {@link CollectionAgent}
     */
    CollectionAgent createCollectionAgentAndOverrideLocation(String nodeCriteria, InetAddress ipAddr, String location);

}
