//
// Copyright (C) 2003 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
//
// Tab Size = 8
//
package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.MissingResourceException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xml.sax.InputSource;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.IPSorter;

import org.opennms.netmgt.config.DatabaseConnectionFactory;

// Castor generated discovery configuration classes.
import org.opennms.netmgt.config.discovery.ExcludeRange;

/**
 * This class represents a singular instance that is used
 * to map trap IP addresses to known nodes.
 *
 * @author <a href="mailto:weave@opennms.org">Brian Weaver</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 *
 */
final class TrapdIPMgr
{
	/**
	 * The SQL statement used to extract the list of currently known
	 * IP addresses and their node IDs from the IP Interface table.
	 */
	private final static String IP_LOAD_SQL = "SELECT ipAddr, nodeid FROM ipInterface";

	/**
	 * A Map of IP addresses and node IDs
	 */
	private static Map		m_knownips = new HashMap();

	/**
	 * Default construct for the instance. This constructor
	 * always throws an exception to the caller.
	 *
	 * @throws java.lang.UnsupportedOperationException Always thrown.
	 *
	 */
	private TrapdIPMgr()
	{
		throw new UnsupportedOperationException("Construction is not supported");
	}

	/** 
	 * Clears and synchronizes the internal known IP address
	 * cache with the current information contained in the
	 * database. To synchronize the cache the method opens a new
	 * connection to the database, loads the address, and then
	 * closes it's connection.
	 *
	 * @throws java.sql.SQLException Thrown if the connection cannot be
	 * 	created or a database error occurs.
	 *
	 */
	static synchronized void dataSourceSync()
		throws SQLException
	{
		java.sql.Connection c = null;
		try
		{
			// Get database connection
			c = DatabaseConnectionFactory.getInstance().getConnection();
			
			// Run with it
			//
			//c.setReadOnly(true);

			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(IP_LOAD_SQL);

			if(rs != null)
			{
				m_knownips.clear();
				while(rs.next())
				{
					String ipstr = rs.getString(1);
					String ipnodeid = rs.getString(2);
					m_knownips.put(ipstr, ipnodeid);
				}
				rs.close();
			}

			s.close();
		}
		finally
		{
			try
			{
				if(c != null)
					c.close();
			}
			catch(SQLException sqlE) { }
		}
	}

	
	/**
	 * Returns the nodeid for the IP Address
	 *
	 * @param addr	The IP Address to query.
	 *
	 * @return The node ID of the IP Address if known.
	 *
	 */
	static synchronized String getNodeId(String addr)
	{
		Object m_nodeid = m_knownips.get(addr);
		if (m_nodeid == null)
			return null;
		else
			return m_knownips.get(addr).toString();
	}

	/**
	 * Sets the IP Address and Node ID in the Map.
	 *
	 * @param addr		The IP Address to add.
	 * @param nodeid	The Node ID to add.
	 *
	 * @return The nodeid if it existed in the map.
	 *
	 */
	static synchronized String setNodeId(String addr, String nodeid)
	{
                if (addr == null || nodeid == null)
                        return null;
                else
			return m_knownips.put(addr, nodeid).toString();
	}

	/**
	 * Removes an address from the node ID map.
	 *
	 * @param addr	The address to remove from the node ID map.
	 *
	 * @return The nodeid that was in the map.
	 *
	 */
	static String removeNodeId(String addr)
	{
                if (m_knownips.get(addr) == null)
                        return null;
                else
			return m_knownips.remove(addr).toString();
	}
	
} // end TrapdIPMgr
