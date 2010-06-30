//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 26: Rename TrapdIPMgr to JdbcTrapdIpMgr and create an interface for the key methods, TrapdIpMgr. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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
