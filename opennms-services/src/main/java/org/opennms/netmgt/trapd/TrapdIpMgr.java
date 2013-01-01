/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.trapd;

import java.sql.SQLException;

/**
 * <p>TrapdIpMgr interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface TrapdIpMgr {

    /**
     * Clears and synchronizes the internal known IP address cache with the
     * current information contained in the database. To synchronize the cache
     * the method opens a new connection to the database, loads the address, and
     * then closes it's connection.
     *
     * @throws java.sql.SQLException
     *             Thrown if the connection cannot be created or a database
     *             error occurs.
     */
    public abstract void dataSourceSync() throws SQLException;

    /**
     * Returns the nodeid for the IP Address
     *
     * @param addr
     *            The IP Address to query.
     * @return The node ID of the IP Address if known.
     */
    public abstract long getNodeId(String addr);

    /**
     * Sets the IP Address and Node ID in the Map.
     *
     * @param addr
     *            The IP Address to add.
     * @param nodeid
     *            The Node ID to add.
     * @return The nodeid if it existed in the map.
     */
    public abstract long setNodeId(String addr, long nodeid);

    /**
     * Removes an address from the node ID map.
     *
     * @param addr
     *            The address to remove from the node ID map.
     * @return The nodeid that was in the map.
     */
    public abstract long removeNodeId(String addr);

    /**
     * <p>clearKnownIpsMap</p>
     */
    public abstract void clearKnownIpsMap();

}
