//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.discovery;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.utils.IPSorter;

/**
 * This class represents a singular instance that is used
 * to check address to determine their pollablility. If the
 * address has already been discovered or is part of the
 * exclude list then the manager can be used to check.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 *
 */
final class DiscoveredIPMgr
{
	/**
	 * The SQL statement used to extract the list of currently known
	 * IP addresses from the IP Interface table.
	 */
	private final static String IP_LOAD_SQL = "SELECT ipAddr FROM ipInterface where isManaged!='D'";

	/**
	 * The set of all discovered addresses
	 */
	private static Set		m_discovered = new TreeSet(AddrComparator.comparator);

	/**
	 * The list of all excluded ranges.
	 */
	private static ExcludeRange[]	m_excluded;

	/**
	 * The list of specific addresses.  Specific addresses take precedence
	 * over the exclude ranges.
	 */
	private static List		m_specifics;
	
	/**
	 * This class implements the {@link java.util.Comparator Comparator} interface
	 * and provides the infomation required for a Tree to be sorted. This implementation
	 * only works with objects of type {@link java.net.InetAddress InetAddress}.
	 *
	 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
	 * @author <a href="http://www.opennms.org/">OpenNMS</a>
	 *
	 */
	static final class AddrComparator
		implements Comparator
	{
		/**
		 * Singular instance of the comparator that can
		 * be used in multiple trees.
		 */
		static final AddrComparator comparator = new AddrComparator();

		/**
		 * The compare method returns the difference between to objects.
		 * This method expects both instances to be instances of
		 * {@link java.net.InetAddress InetAddress} or <code>null</code>.
		 *
		 * @param a	an instance of an InetAddress.
		 * @param b 	an instance of an InetAddress.
		 *
		 * @return less than zero if <tt>a</tt> &lt; <tt>b</tt>, zero if <tt>a</tt> ==
		 * 	<tt>b</tt>, or greater than zero if <tt>a</tt> &gt; <tt>b</tt>.
		 */
		public int compare(Object a, Object b)
		{
			int rc = 0;
			if(a != null && b == null)
			{
				rc = -1;
			}
			else if(a == null && b != null)
			{
				rc = 1;
			}
			else if(a != null) // and  b != null
			{
				byte[] ax = ((InetAddress)a).getAddress();
				byte[] bx = ((InetAddress)b).getAddress();

				for(int x = 0; x < ax.length && rc == 0; x++)
					rc = (ax[x] < 0 ? 256 + ax[x] : ax[x]) 
					   - (bx[x] < 0 ? 256 + bx[x] : bx[x]);
			}
			return rc;
		}
	}

	/**
	 * Default construct for the instance. This constructor
	 * always throws an exception to the caller.
	 *
	 * @throws java.lang.UnsupportedOperationException Always thrown.
	 *
	 */
	private DiscoveredIPMgr()
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
				m_discovered.clear();
				while(rs.next())
				{
					String ipstr = rs.getString(1);
					try
					{
						m_discovered.add(InetAddress.getByName(ipstr));
					}
					catch(UnknownHostException uhE)
					{
						// log?
					}
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
	 * Set the exclusion list used to determine if an IP Address
	 * has been excluded.
	 *
	 * @param ranges	The list of excluded ranges
	 *
	 */
	static synchronized void setExclusionList(ExcludeRange[] ranges)
	{
		m_excluded = ranges;
	}

	/**
	 * Set the specifics list used to determine if an IP Address
	 * has been excluded (& not specifically included).
	 *
	 * @param specifics	The list of specific addresses
	 *
	 */
	static synchronized void setSpecificsList(List specifics)
	{
		m_specifics = specifics;
	}
	
	/**
	 * Returns true if the node has been discovered and
	 * added to the discovered IP manager.
	 *
	 * @param addr	The IP Address to query.
	 *
	 * @return True if the address is known to the manager.
	 *
	 */
	static synchronized boolean isDiscovered(InetAddress addr)
	{
		return m_discovered.contains(addr);
	}

	/**
	 * Returns true if the node has been discovered and
	 * added to the discovered IP manager. If the address
	 * cannot be converted to an {@link java.net.InetAddress
	 * InetAddress} instance then an exception is generated.
	 *
	 * @param ipAddr	The IP Address to query.
	 *
	 * @return True if the address is known to the manager.
	 *
	 * @throws java.io.UnknownHostException Thrown if the 
	 * 	address name could not be converted.
	 *
	 */
	static boolean isDiscovered(String ipAddr)
		throws UnknownHostException
	{
		return isDiscovered(InetAddress.getByName(ipAddr));
	}

	/**
	 * Returns true if the passed address is included in the
	 * range of addresses to be skipped and is not contained 
	 * within the specifcs address list (which takes precedence
	 * over the exclude ranges.
	 *
	 * @param addr	The address to check for exclusion
	 *
	 * @return True if the address is excluded.
	 *
	 */
	static synchronized boolean isExcluded(InetAddress addr)
	{
		boolean rc = false;
		if(m_excluded != null)
		{
			long laddr = 0;
			byte[] octets = addr.getAddress();
			for(int x = 0; x < octets.length; x++)
				laddr = (laddr << 8)
				      | (octets[x] < 0 ? 256L + octets[x] : octets[x] + 0L);

			for(int x = 0; !rc && x < m_excluded.length; x++)
			{
				try
				{
					InetAddress abegin = InetAddress.getByName(m_excluded[x].getBegin());
					InetAddress aend   = InetAddress.getByName(m_excluded[x].getEnd());
					long begin = IPSorter.convertToLong(abegin.getAddress());
					long end   = IPSorter.convertToLong(aend.getAddress());
					if(begin <= laddr && laddr <= end)
					{
						// Ok, the address is excluded by this range...now
						// check to see if the address is contained within
						// the list of specifics.  If not then return true.
						boolean inSpecifics = false;
						Iterator iter = m_specifics.iterator();
						while (iter.hasNext())
						{
							IPPollAddress tmp = (IPPollAddress)iter.next();
							InetAddress specific = tmp.getAddress();
							if (specific.equals(addr))
							{
								inSpecifics = true;
								break;
							}
						}		
						
						if (!inSpecifics)
						{
							rc = true;
							break;
						}
					}
				}
				catch(UnknownHostException ex)
				{
					Category log = ThreadCategory.getInstance(DiscoveredIPMgr.class);
					log.error("DiscoveredIPMgr.isExcluded: failed to convert exclusion address to InetAddress", ex);
				}
			}
		}
		
		return rc;
	}

	/**
	 * Returns true if the passed address is included in the
	 * range of addresses to be skipped.
	 *
	 * @param ipAddr The address to check for exclusion
	 *
	 * @return True if the address is excluded.
	 *
	 * @throws java.net.UnknownHostException Thrown if the string
	 *	address could not be converted to an InetAddress.
	 */
	static boolean isExcluded(String ipAddr)
		throws UnknownHostException
	{
		return isExcluded(InetAddress.getByName(ipAddr));
	}

	/**
	 * Returns true if the passed address is either excluded
	 * or has already been discovered.
	 *
	 * @param addr	The address to check
	 *
	 * @return True if the address has been discovered or
	 * 	is excluded.
	 *
	 * @throws java.net.UnknownHostException Thrown if the address
	 * 	cannot be converted to an InetAddress.
	 */
	static synchronized boolean isDiscoveredOrExcluded(InetAddress addr)
	{
		return isDiscovered(addr) || isExcluded(addr);
	}

	/**
	 * Returns true if the passed address is either excluded
	 * or has already been discovered.
	 *
	 * @param addr	The address to check
	 *
	 * @return True if the address has been discovered or
	 * 	is excluded.
	 *
	 * @throws java.net.UnknownHostException Thrown if the address
	 * 	cannot be converted to an InetAddress.
	 */
	static boolean isDiscoveredOrExcluded(String addr)
		throws UnknownHostException
	{
		return isDiscoveredOrExcluded(InetAddress.getByName(addr));
	}

	/**
	 * Adds a new address to the list of discovered address.
	 *
	 * @param addr	The address to add to the discovered set.
	 *
	 * @return True if the address was not already discovered.
	 */
	static synchronized boolean addDiscovered(InetAddress addr)
	{
		return m_discovered.add(addr);
	}

	/**
	 * Adds a new address to the list of discovered address.
	 *
	 * @param addr	The address to add to the discovered set.
	 *
	 * @return True if the address was not already discovered.
	 *
	 * @throws java.net.UnknownHost Thrown if the address cannot
	 * 	be converted.
	 */
	static boolean addDiscovered(String addr)
		throws UnknownHostException
	{
		return addDiscovered(InetAddress.getByName(addr));
	}

	/**
	 * Removes an address from the list of discovered address.
	 *
	 * @param addr	The address to remove from the discovered set.
	 *
	 * @return True if the address was already discovered.
	 */
	static synchronized boolean removeDiscovered(InetAddress addr)
	{
		return m_discovered.remove(addr);
	}
	
	/**
	 * Removes an address from the list of discovered address.
	 *
	 * @param addr	The address to remove from the discovered set.
	 *
	 * @return True if the address was already discovered.
	 *
	 * @throws java.net.UnknownHost Thrown if the address cannot
	 * 	be converted.
	 */
	static boolean removeDiscovered(String addr)
		throws UnknownHostException
	{
		return removeDiscovered(InetAddress.getByName(addr));
	}
	
} // end DiscoveredIPMgr
