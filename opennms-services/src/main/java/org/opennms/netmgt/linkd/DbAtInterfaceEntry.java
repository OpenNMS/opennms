/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.linkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public final class DbAtInterfaceEntry {
	/**
	 * The character returned if the entry is active
	 */
	public static final char STATUS_ACTIVE = 'A';

	/**
	 * The character returned if the entry is not active
	 * means last polled
	 */
	public static final char STATUS_NOT_POLLED = 'N';

	/**
	 * It stats that node is deleted
	 * The character returned if the node is deleted
	 */
	public static final char STATUS_DELETED = 'D';

	/**
	 * The character returned if the entry type is unset/unknown.
	 */
	public static final char STATUS_UNKNOWN = 'K';

	/**
	 * The node identifier
	 */
	private final int m_nodeId;

	/**
	 * The ip address 
	 */
	private final InetAddress m_ipaddr;

	/**
	 * The mac address  
	 */
	String m_physaddr;

	/**
	 * The nodeid identifier of the node 
	 * from whom this information was learned
	 */
	int m_sourcenodeid;

	/**
	 * The ifindex on source node 
	 * from whom this information was learned
	 */
	int m_ifindex;

	/**
	 * The Status of
	 * this information
	 */
	char m_status = STATUS_UNKNOWN;

	/**
	 * The Time when
	 * this information was learned
	 */
	Timestamp m_lastPollTime;

	/**
	 * the sql statement to load data from database
	 */
	private static final String SQL_LOAD_ATINTERFACE = "SELECT atphysaddr,sourceNodeid,ifindex,status,lastpolltime FROM atinterface WHERE nodeid = ? AND ipaddr = ? ";

	/**
	 * True if this recored was loaded from the database.
	 * False if it's new.
	 */
	private boolean m_fromDb;

	/**
	 * The bit map used to determine which elements have
	 * changed since the record was created.
	 */
	private int m_changed;
	
	// Mask fields
	//
	private static final int CHANGED_PHYSADDR = 1 << 0;

	private static final int CHANGED_SOURCE = 1 << 1;

	private static final int CHANGED_IFINDEX = 1 << 2;

	private static final int CHANGED_STATUS = 1 << 3;

	private static final int CHANGED_POLLTIME = 1 << 4;

	/**
	 * Inserts the new row into the AtInterface table
	 * of the OpenNMS databasee.
	 *
	 * @param c	The connection to the database.
	 *
	 * @throws java.sql.SQLException Thrown if an error occurs
	 * 	with the connection
	 */
	private void insert(Connection c) throws SQLException {
		if (m_fromDb)
			throw new IllegalStateException(
					"The ARP interface record already exists in the database");

		// first extract the next node identifier
		//
		StringBuffer names = new StringBuffer(
				"INSERT INTO AtInterface (nodeid,ipaddr");
		StringBuffer values = new StringBuffer("?,?");

		if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR) {
			values.append(",?");
			names.append(",atphysaddr");
		}

		if ((m_changed & CHANGED_SOURCE) == CHANGED_SOURCE) {
			values.append(",?");
			names.append(",sourceNodeid");
		}

		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
			values.append(",?");
			names.append(",ifindex");
		}

		if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS) {
			values.append(",?");
			names.append(",status");
		}

		if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
			values.append(",?");
			names.append(",lastpolltime");
		}

		names.append(") VALUES (").append(values).append(')');
		
		LogUtils.debugf(this, "AtInterfaceEntry.insert: SQL insert statment = " + names.toString());

		// create the Prepared statment and then
		// start setting the result values
		//

		PreparedStatement stmt;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = c.prepareStatement(names.toString());
            d.watch(stmt);

            int ndx = 1;
            stmt.setInt(ndx++, m_nodeId);
            stmt.setString(ndx++, str(m_ipaddr));

            if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR)
            	stmt.setString(ndx++, m_physaddr);

            if ((m_changed & CHANGED_SOURCE) == CHANGED_SOURCE)
            	stmt.setInt(ndx++, m_sourcenodeid);

            if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
            	stmt.setInt(ndx++, m_ifindex);

            if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
            	stmt.setString(ndx++, new String(new char[] { m_status }));

            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
            	stmt.setTimestamp(ndx++, m_lastPollTime);
            }

            // Run the insert
            //
            int rc = stmt.executeUpdate();
            
            LogUtils.debugf(this, "AtInterfaceEntry.insert: row " + rc);
        } finally {
            d.cleanUp();
        }
		
		// clear the mask and mark as backed
		// by the database
		//
		m_fromDb = true;
		m_changed = 0;
	}

	/** 
	 * Updates an existing record in the OpenNMS AtInterface table.
	 * 
	 * @param c	The connection used for the update.
	 *
	 * @throws java.sql.SQLException Thrown if an error occurs
	 * 	with the connection
	 */
	private void update(Connection c) throws SQLException {
		if (!m_fromDb)
			throw new IllegalStateException(
					"The record does not exists in the database");

		// first extract the next node identifier
		//
		StringBuffer sqlText = new StringBuffer("UPDATE AtInterface SET ");

		char comma = ' ';

		if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR) {
			sqlText.append(comma).append("atphysaddr = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_SOURCE) == CHANGED_SOURCE) {
			sqlText.append(comma).append("sourcenodeid = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
			sqlText.append(comma).append("ifindex = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS) {
			sqlText.append(comma).append("status = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
			sqlText.append(comma).append("lastpolltime = ?");
			comma = ',';
		}

		sqlText.append(" WHERE nodeid = ? AND ipaddr = ? ");

		LogUtils.debugf(this, "AtInterfaceEntry.update: SQL insert statment = " + sqlText.toString());

		PreparedStatement stmt;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = c.prepareStatement(sqlText.toString());
            d.watch(stmt);

            int ndx = 1;

            if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR)
            	stmt.setString(ndx++, m_physaddr);

            if ((m_changed & CHANGED_SOURCE) == CHANGED_SOURCE)
            	stmt.setInt(ndx++, m_sourcenodeid);

            if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
            	stmt.setInt(ndx++, m_ifindex);

            if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
            	stmt.setString(ndx++, new String(new char[] { m_status }));

            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
            	stmt.setTimestamp(ndx++, m_lastPollTime);
            }

            stmt.setInt(ndx++, m_nodeId);
            stmt.setString(ndx++, str(m_ipaddr));

            // Run the insert
            //
            int rc = stmt.executeUpdate();
            LogUtils.debugf(this, "AtInterfaceEntry.update: row " + rc);
        } finally {
            d.cleanUp();
        }

		// clear the mask and mark as backed
		// by the database
		//
		m_changed = 0;
	}

	/**
	 * Load the current interface from the database. If the interface
	 * was modified, the modifications are lost. The nodeid
	 * and ip address must be set prior to this call.
	 *
	 * @param c	The connection used to load the data.
	 *
	 * @throws java.sql.SQLException Thrown if an error occurs
	 * 	with the connection
	 */
	private boolean load(Connection c) throws SQLException {
		if (!m_fromDb)
			throw new IllegalStateException(
					"The record does not exists in the database");

		// create the Prepared statment and then
		// start setting the result values
		//
		PreparedStatement stmt = null;
		// Run the select
        		//
        ResultSet rset;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = c.prepareStatement(SQL_LOAD_ATINTERFACE);
            d.watch(stmt);
            stmt.setInt(1, m_nodeId);
            stmt.setString(2, str(m_ipaddr));

            rset = stmt.executeQuery();
            d.watch(rset);
            if (!rset.next()) {
                LogUtils.debugf(this, "AtInterfaceEntry.load: no result found");
            	return false;
            }

            // extract the values.
            //
            int ndx = 1;

            // get the mac address
            //
            m_physaddr = rset.getString(ndx++);
            if (rset.wasNull())
            	m_physaddr = null;

            // get the source node id
            //
            m_sourcenodeid = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_sourcenodeid = -1;

            // get the source node ifindex
            //
            m_ifindex = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_ifindex = -1;

            // the entry status
            //
            String str = rset.getString(ndx++);
            if (str != null && !rset.wasNull())
            	m_status = str.charAt(0);
            else
            	m_status = STATUS_UNKNOWN;

            m_lastPollTime = rset.getTimestamp(ndx++);
        } finally {
            d.cleanUp();
        }

		// clear the mask and mark as backed
		// by the database
		//
		m_changed = 0;
		LogUtils.debugf(this, "AtInterfaceEntry.load: result found");
		return true;
	}

	private DbAtInterfaceEntry(int nodeId, InetAddress ipaddr, boolean exists) {
		m_nodeId = nodeId;
		m_fromDb = exists;
		m_sourcenodeid = -1;
		m_ifindex = -1;
		m_ipaddr = ipaddr;
		m_physaddr = null;
	}

	static DbAtInterfaceEntry create(int nodeid, InetAddress ipaddr) {
		return new DbAtInterfaceEntry(nodeid, ipaddr, false);
	}

	/**
	 * @return
	 */
	protected int get_nodeId() {
		return m_nodeId;
	}

	/**
	 * @return
	 */
	protected InetAddress get_ipaddr() {
		return m_ipaddr;
	}

	/**
	 * @return
	 */
	protected String get_physaddr() {
		return m_physaddr;
	}

	protected void set_physaddr(String macaddr) {
		m_physaddr = macaddr;
		m_changed |= CHANGED_PHYSADDR;
	}

	protected boolean hasAtPhysAddrChanged() {
		if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR)
			return true;
		else
			return false;
	}

	boolean updateAtPhysAddr(final String macaddr) {
		if (m_physaddr == null || !m_physaddr.equals(macaddr)) {
			set_physaddr(macaddr);
			return true;
		} else
			return false;
	}

	protected int get_sourcenodeid() {
		return m_sourcenodeid;
	}

	protected void set_sourcenodeid(int sourcenode) {
		m_sourcenodeid = sourcenode;
		m_changed |= CHANGED_SOURCE;
	}

	protected boolean hasSourceNodeIdChanged() {
		if ((m_changed & CHANGED_SOURCE) == CHANGED_SOURCE)
			return true;
		else
			return false;
	}

	boolean updateSourceNodeId(int sourcenodeid) {
		if (sourcenodeid != m_sourcenodeid) {
			set_sourcenodeid(sourcenodeid);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	protected int get_ifindex() {
		return m_ifindex;
	}

	protected void set_ifindex(int ifindex) {
		m_ifindex = ifindex;
		m_changed |= CHANGED_IFINDEX;
	}

	protected boolean hasIfIndexChanged() {
		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
			return true;
		else
			return false;
	}

	boolean updateIfIndex(int ifindex) {
		if (ifindex != m_ifindex) {
			set_ifindex(ifindex);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	protected char get_status() {
		return m_status;
	}

	protected void set_status(char status) {
		if (status == STATUS_ACTIVE || status == STATUS_NOT_POLLED
				|| status == STATUS_DELETED)
			m_status = status;
		m_changed |= CHANGED_STATUS;
	}

	protected boolean hasStatusChanged() {
		if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
			return true;
		else
			return false;
	}

	boolean updateStatus(char status) {
		if (status != m_status) {
			set_status(status);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	protected Timestamp get_lastpolltime() {
		return m_lastPollTime;
	}

	/**
	 * Gets the last poll time of the record
	 */
	String getLastPollTimeString() {
		String result = null;
		if (m_lastPollTime != null) {
			result = m_lastPollTime.toString();
		}
		return result;
	}

	/**
	 * Sets the last poll time.
	 *
	 * @param time	The last poll time.
	 *
	 */
	protected void set_lastpolltime(String time) throws ParseException {
		if (time == null) {
			m_lastPollTime = null;
		} else {
			Date tmpDate = EventConstants.parseToDate(time);
			m_lastPollTime = new Timestamp(tmpDate.getTime());
		}
		m_changed |= CHANGED_POLLTIME;
	}

	/**
	 * Sets the last poll time.
	 *
	 * @param time	The last poll time.
	 *
	 */
	protected void set_lastpolltime(Date time) {
		m_lastPollTime = new Timestamp(time.getTime());
		m_changed |= CHANGED_POLLTIME;
	}

	/**
	 * Sets the last poll time.
	 *
	 * @param time	The last poll time.
	 *
	 */
	protected void set_lastpolltime(Timestamp time) {
		m_lastPollTime = time;
		m_changed |= CHANGED_POLLTIME;
	}

	/**
	 * Updates the interface information in the configured database. If the 
	 * interface does not exist the a new row in the table is created. If the
	 * element already exists then it's current row is updated as 
	 * needed based upon the current changes to the node.
	 */
	void store() throws SQLException {
		if (m_changed != 0 || m_fromDb == false) {
			Connection db = null;
			try {
				db = DataSourceFactory.getInstance().getConnection();
				store(db);
				if (db.getAutoCommit() == false)
					db.commit();
			} finally {
				try {
					if (db != null)
						db.close();
				} catch (SQLException e) {
				    LogUtils.warnf(this, e, "Exception closing JDBC connection");
				}
			}
		}
		return;
	}

	/**
	 * Updates the interface information in the configured database. If the 
	 * atinterface does not exist the a new row in the table is created. If the
	 * element already exists then it's current row is updated as 
	 * needed based upon the current changes to the node.
	 *
	 * @param db	The database connection used to write the record.
	 */
	void store(Connection db) throws SQLException {
		if (m_changed != 0 || m_fromDb == false) {
			if (m_fromDb)
				update(db);
			else
				insert(db);
		}
	}

	/**
	 * Retreives a current record from the database based upon the
	 * key fields of <em>nodeID</em> and <em>ipaddr</em>. If the
	 * record cannot be found then a null reference is returnd.
	 *
	 * @param nid	The node id key
	 * @param ipaddr The ip address
	 *
	 * @return The loaded entry or null if one could not be found.
	 *
	 */
	static DbAtInterfaceEntry get(int nid, InetAddress ipaddr) throws SQLException {
		Connection db = null;
		try {
			db = DataSourceFactory.getInstance().getConnection();
			return get(db, nid, ipaddr);
		} finally {
			try {
				if (db != null)
					db.close();
			} catch (SQLException e) {
                LogUtils.warnf(DbAtInterfaceEntry.class, e, "Exception closing JDBC connection");
			}
		}
	}

	/**
	 * Retrieves a current record from the database based upon the
	 * key fields of <em>nodeID</em> and <em>ipaddr</em>. If the
	 * record cannot be found then a null reference is returned.
	 *
	 * @param db	The database connection used to load the entry.
	 * @param nid	The node id key
	 * @param ipaddr The ipaddress
	 *
	 * @return The loaded entry or null if one could not be found.
	 *
	 */
	static DbAtInterfaceEntry get(Connection db, int nid, InetAddress ipaddr)
			throws SQLException {
		DbAtInterfaceEntry entry = new DbAtInterfaceEntry(nid, ipaddr,true);
		if (!entry.load(db))
			entry = null;
		return entry;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("fromDB?", m_fromDb)
			.append("nodeID", m_nodeId)
			.append("ipAddress", m_ipaddr)
			.append("physAddress", m_physaddr)
			.append("sourceNodeID", m_sourcenodeid)
			.append("ifIndex", m_ifindex)
			.append("status", m_status)
			.append("lastPollTime", m_lastPollTime)
			.toString();
	}

}
