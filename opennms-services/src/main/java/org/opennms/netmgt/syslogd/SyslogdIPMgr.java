//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.syslogd;

import org.opennms.netmgt.config.DataSourceFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a singular instance that is used to map trap IP
 * addresses to known nodes.
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class SyslogdIPMgr {
    /**
     * The SQL statement used to extract the list of currently known IP
     * addresses and their node IDs from the IP Interface table.
     */
    private final static String IP_LOAD_SQL = "SELECT ipAddr, nodeid FROM ipInterface";

    /**
     * A Map of IP addresses and node IDs
     */
    private static Map<String,Long> m_knownips = new HashMap<String,Long>();

    /**
     * Default construct for the instance. This constructor always throws an
     * exception to the caller.
     *
     * @throws java.lang.UnsupportedOperationException
     *                               Always thrown.
     *                               <p/>
     *                               Clears and synchronizes the internal known IP address cache with the
     *                               current information contained in the database. To synchronize the cache
     *                               the method opens a new connection to the database, loads the address,
     *                               and then closes it's connection.
     * @throws java.sql.SQLException Thrown if the connection cannot be created or a database
     *                               error occurs.
     *                               <p/>
     *                               Clears and synchronizes the internal known IP address cache with the
     *                               current information contained in the database. To synchronize the cache
     *                               the method opens a new connection to the database, loads the address,
     *                               and then closes it's connection.
     * @throws java.sql.SQLException Thrown if the connection cannot be created or a database
     *                               error occurs.
     */

    // WTF, This is a straight Cut'n paste from TRAPD
    // private SyslodIPMgr() {
    // throw new UnsupportedOperationException("Construction is not
    // supported");
    // }
    /**
     * Clears and synchronizes the internal known IP address cache with the
     * current information contained in the database. To synchronize the cache
     * the method opens a new connection to the database, loads the address,
     * and then closes it's connection.
     *
     * @throws java.sql.SQLException
     *             Thrown if the connection cannot be created or a database
     *             error occurs.
     */
    static synchronized void dataSourceSync() throws SQLException {
        java.sql.Connection c = null;
        try {
            // Get database connection
            c = DataSourceFactory.getInstance().getConnection();

            // Run with it
            //
            // c.setReadOnly(true);

            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery(IP_LOAD_SQL);

            if (rs != null) {
                m_knownips.clear();
                while (rs.next()) {
                    String ipstr = rs.getString(1);
                    long ipnodeid = rs.getLong(2);
                    m_knownips.put(ipstr, ipnodeid);
                }
                rs.close();
            }

            s.close();
        } finally {
            try {
                if (c != null)
                    c.close();
            } catch (SQLException sqlE) {
            }
        }
    }

    /**
     * Returns the nodeid for the IP Address
     *
     * @param addr The IP Address to query.
     * @return The node ID of the IP Address if known.
     */
    static synchronized long getNodeId(String addr) {
        if (addr == null)
            return -1;
        return longValue((Long) m_knownips.get(addr));
    }

    /**
     * Sets the IP Address and Node ID in the Map.
     *
     * @param addr   The IP Address to add.
     * @param nodeid The Node ID to add.
     * @return The nodeid if it existed in the map.
     */
    static synchronized long setNodeId(String addr, long nodeid) {
        if (addr == null || nodeid == -1)
            return -1;

        return longValue((Long) m_knownips.put(addr, nodeid));
    }

    /**
     * Removes an address from the node ID map.
     *
     * @param addr The address to remove from the node ID map.
     * @return The nodeid that was in the map.
     */
    static long removeNodeId(String addr) {
        if (addr == null)
            return -1;
        return longValue((Long) m_knownips.remove(addr));
    }

    private static long longValue(Long result) {
        return (result == null ? -1 : result);
    }

} // end SyslodIPMgr
