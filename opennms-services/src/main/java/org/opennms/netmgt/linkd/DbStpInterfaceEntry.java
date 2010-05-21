/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 26, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.linkd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class DbStpInterfaceEntry {

	/**
	 * The character returned if the entry is active
	 */

	static final char STATUS_ACTIVE = 'A';

	/**
	 * The character returned if the entry is not active means last polled
	 */

	static final char STATUS_NOT_POLLED = 'N';

	/**
	 * It stats that node is deleted The character returned if the node is
	 * deleted
	 */
	static final char STATUS_DELETE = 'D';

	/**
	 * The character returned if the entry type is unset/unknown.
	 */

	static final char STATUS_UNKNOWN = 'K';

	/**
	 * the STP Bridge Port State
	 */

	static final int STP_PORT_DISABLED = 1;

	static final int STP_PORT_BLOCKING = 2;

	static final int STP_PORT_LISTENING = 3;

	static final int STP_PORT_LEARNING = 4;

	static final int STP_PORT_FORWARDING = 5;

	static final int STP_PORT_BROKEN = 6;

	/**
	 * The node identifier
	 */

	int m_nodeId;

	/**
	 * bridge port number identifier
	 */
	int m_bridgeport;

	/**
	 * interface ifindex corresponding to bridge port number
	 */

	int m_ifindex;

	/**
	 * integer that reflect thestp staus of the bridge port '1' disabled '2'
	 * blocking '3' listening '4' learning '5' forwarding '6' broken
	 */

	int m_stpportstate;

	/**
	 * The contribution of this port to the path cost of paths towards the
	 * spanning tree root which include this port.
	 */

	int m_stpportpathcost;

	/**
	 * the unique Bridge Identifier of the Bridge recorded as the Root in the
	 * Configuration BPDUs transmitted by the Designated Bridge for the segment
	 * to which the port is attached.
	 */

	String m_stpportdesignatedroot;

	/**
	 * The path cost of the Designated Port of the segment connected to this
	 * port. This value is compared to the Root Path Cost field in received
	 * bridge PDUs.
	 */

	int m_stpportdesignatedcost;

	/**
	 * The Bridge Identifier of the bridge which this port considers to be the
	 * Designated Bridge for this port's segment.
	 */

	String m_stpportdesignatedbridge;

	/**
	 * The Port Identifier of the port on the Designated Bridge for this port's
	 * segment.
	 */

	String m_stpportdesignatedport;

	/**
	 * Unique integer identifier VLAN for which this info is valid
	 */

	int m_stpportvlan;

	/**
	 * The Status of this information
	 */

	char m_status = STATUS_UNKNOWN;

	/**
	 * The Time when this information was learned
	 */

	Timestamp m_lastPollTime;

	/**
	 * the sql statement to load data from database
	 */
	private static final String SQL_LOAD_STPINTERFACE = "SELECT ifindex,stpportstate,stpportpathcost,stpportdesignatedroot,stpportdesignatedcost,stpportdesignatedbridge,stpportdesignatedport,status,lastPollTime FROM stpinterface WHERE nodeid = ? AND bridgeport = ? AND stpvlan = ? ";

	/**
	 * True if this recored was loaded from the database. False if it's new.
	 */
	private boolean m_fromDb;

	/**
	 * The bit map used to determine which elements have changed since the
	 * record was created.
	 */
	private int m_changed;

	// Mask fields
	//

	private static final int CHANGED_IFINDEX = 1 << 0;

	private static final int CHANGED_STP_PORT_STATE = 1 << 1;

	private static final int CHANGED_STP_PORT_PATH_COST = 1 << 2;

	private static final int CHANGED_STP_PORT_DES_ROOT = 1 << 3;

	private static final int CHANGED_STP_PORT_DES_COST = 1 << 4;

	private static final int CHANGED_STP_PORT_DES_BRIDGE = 1 << 5;

	private static final int CHANGED_STP_PORT_DES_PORT = 1 << 6;

	private static final int CHANGED_STATUS = 1 << 7;

	private static final int CHANGED_POLLTIME = 1 << 8;

	/**
	 * Inserts the new row into the StpInterface table of the OpenNMS database.
	 * 
	 * @param c
	 *            The connection to the database.
	 * 
	 * @throws java.sql.SQLException
	 *             Thrown if an error occurs with the connection
	 */
	private void insert(Connection c) throws SQLException {
		if (m_fromDb)
			throw new IllegalStateException("The record already exists in the database");

		ThreadCategory log = ThreadCategory.getInstance(getClass());

		// first extract the next node identifier
		//
		StringBuffer names = new StringBuffer(
				"INSERT INTO StpInterface (nodeid,bridgeport,stpvlan");
		StringBuffer values = new StringBuffer("?,?,?");

		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
			values.append(",?");
			names.append(",ifindex");
		}

		if ((m_changed & CHANGED_STP_PORT_STATE) == CHANGED_STP_PORT_STATE) {
			values.append(",?");
			names.append(",stpportstate");
		}

		if ((m_changed & CHANGED_STP_PORT_PATH_COST) == CHANGED_STP_PORT_PATH_COST) {
			values.append(",?");
			names.append(",stpportpathcost");
		}

		if ((m_changed & CHANGED_STP_PORT_DES_ROOT) == CHANGED_STP_PORT_DES_ROOT) {
			values.append(",?");
			names.append(",stpportdesignatedroot");
		}

		if ((m_changed & CHANGED_STP_PORT_DES_COST) == CHANGED_STP_PORT_DES_COST) {
			values.append(",?");
			names.append(",stpportdesignatedcost");
		}

		if ((m_changed & CHANGED_STP_PORT_DES_BRIDGE) == CHANGED_STP_PORT_DES_BRIDGE) {
			values.append(",?");
			names.append(",stpportdesignatedbridge");
		}

		if ((m_changed & CHANGED_STP_PORT_DES_PORT) == CHANGED_STP_PORT_DES_PORT) {
			values.append(",?");
			names.append(",stpportdesignatedport");
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

		if (log.isDebugEnabled())
			log.debug("StpInterfaceEntry.insert: SQL insert statment = " + names.toString());


		DBUtils d = new DBUtils(getClass());

		try {
            PreparedStatement stmt = c.prepareStatement(names.toString());
            d.watch(stmt);

            int ndx = 1;
            stmt.setInt(ndx++, m_nodeId);
            stmt.setInt(ndx++, m_bridgeport);
            stmt.setInt(ndx++, m_stpportvlan);

            if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
            	stmt.setInt(ndx++, m_ifindex);

            if ((m_changed & CHANGED_STP_PORT_STATE) == CHANGED_STP_PORT_STATE)
            	stmt.setInt(ndx++, m_stpportstate);

            if ((m_changed & CHANGED_STP_PORT_PATH_COST) == CHANGED_STP_PORT_PATH_COST)
            	stmt.setInt(ndx++, m_stpportpathcost);

            if ((m_changed & CHANGED_STP_PORT_DES_ROOT) == CHANGED_STP_PORT_DES_ROOT)
            	stmt.setString(ndx++, m_stpportdesignatedroot);

            if ((m_changed & CHANGED_STP_PORT_DES_COST) == CHANGED_STP_PORT_DES_COST)
            	stmt.setInt(ndx++, m_stpportdesignatedcost);

            if ((m_changed & CHANGED_STP_PORT_DES_BRIDGE) == CHANGED_STP_PORT_DES_BRIDGE)
            	stmt.setString(ndx++, m_stpportdesignatedbridge);

            if ((m_changed & CHANGED_STP_PORT_DES_PORT) == CHANGED_STP_PORT_DES_PORT)
            	stmt.setString(ndx++, m_stpportdesignatedport);

            if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
            	stmt.setString(ndx++, new String(new char[] { m_status }));

            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
            	stmt.setTimestamp(ndx++, m_lastPollTime);
            }

            // Run the insert
            //
            int rc = stmt.executeUpdate();
            if (log.isDebugEnabled())
            	log.debug("StpInterfaceEntry.insert: row " + rc);
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
	 * Updates an existing record in the OpenNMS StpNode table.
	 * 
	 * @param c
	 *            The connection used for the update.
	 * 
	 * @throws java.sql.SQLException
	 *             Thrown if an error occurs with the connection
	 */
	private void update(Connection c) throws SQLException {
		if (!m_fromDb)
			throw new IllegalStateException(
					"The record does not exists in the database");

		ThreadCategory log = ThreadCategory.getInstance(getClass());

		// first extract the next node identifier
		//
		StringBuffer sqlText = new StringBuffer("UPDATE StpInterface SET ");

		char comma = ' ';

		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
			sqlText.append(comma).append("ifindex = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_STP_PORT_STATE) == CHANGED_STP_PORT_STATE) {
			sqlText.append(comma).append("stpportstate = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_STP_PORT_PATH_COST) == CHANGED_STP_PORT_PATH_COST) {
			sqlText.append(comma).append("stpportpathcost = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_STP_PORT_DES_ROOT) == CHANGED_STP_PORT_DES_ROOT) {
			sqlText.append(comma).append("stpportdesignatedroot = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_STP_PORT_DES_COST) == CHANGED_STP_PORT_DES_COST) {
			sqlText.append(comma).append("stpportdesignatedcost = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_STP_PORT_DES_BRIDGE) == CHANGED_STP_PORT_DES_BRIDGE) {
			sqlText.append(comma).append("stpportdesignatedbridge = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_STP_PORT_DES_PORT) == CHANGED_STP_PORT_DES_PORT) {
			sqlText.append(comma).append(
					"stpportdesignatedport = ?");
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

		sqlText.append(" WHERE nodeid = ? AND bridgeport = ? AND stpvlan = ? ");

		if (log.isDebugEnabled())
			log.debug("StpInterfaceEntry.update: SQL statement " + sqlText.toString());

		DBUtils d = new DBUtils(getClass());

		try {
            PreparedStatement stmt = c.prepareStatement(sqlText.toString());
            d.watch(stmt);
            
            int ndx = 1;

            if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
            	stmt.setInt(ndx++, m_ifindex);

            if ((m_changed & CHANGED_STP_PORT_STATE) == CHANGED_STP_PORT_STATE)
            	stmt.setInt(ndx++, m_stpportstate);

            if ((m_changed & CHANGED_STP_PORT_PATH_COST) == CHANGED_STP_PORT_PATH_COST)
            	stmt.setInt(ndx++, m_stpportpathcost);

            if ((m_changed & CHANGED_STP_PORT_DES_ROOT) == CHANGED_STP_PORT_DES_ROOT)
            	stmt.setString(ndx++, m_stpportdesignatedroot);

            if ((m_changed & CHANGED_STP_PORT_DES_COST) == CHANGED_STP_PORT_DES_COST)
            	stmt.setInt(ndx++, m_stpportdesignatedcost);

            if ((m_changed & CHANGED_STP_PORT_DES_BRIDGE) == CHANGED_STP_PORT_DES_BRIDGE)
            	stmt.setString(ndx++, m_stpportdesignatedbridge);

            if ((m_changed & CHANGED_STP_PORT_DES_PORT) == CHANGED_STP_PORT_DES_PORT)
            	stmt.setString(ndx++, m_stpportdesignatedport);

            if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
            	stmt.setString(ndx++, new String(new char[] { m_status }));

            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
            	stmt.setTimestamp(ndx++, m_lastPollTime);
            }

            stmt.setInt(ndx++, m_nodeId);
            stmt.setInt(ndx++, m_bridgeport);
            stmt.setInt(ndx++, m_stpportvlan);

            // Run the insert
            //
            int rc = stmt.executeUpdate();
            if (log.isDebugEnabled())
            	log.debug("StpInterfaceEntry.update: row " + rc);
            stmt.close();
		} finally {
		    d.cleanUp();
        }

		// clear the mask and mark as backed
		// by the database
		//
		m_changed = 0;
	}

	/**
	 * Load the current interface from the database. If the interface was
	 * modified, the modifications are lost. The nodeid and ip address must be
	 * set prior to this call.
	 * 
	 * @param c
	 *            The connection used to load the data.
	 * 
	 * @throws java.sql.SQLException
	 *             Thrown if an error occurs with the connection
	 */
	private boolean load(Connection c) throws SQLException {
		if (!m_fromDb)
			throw new IllegalStateException("The record does not exists in the database");

		ThreadCategory log = ThreadCategory.getInstance(getClass());

		// create the Prepared statment and then
		// start setting the result values
		//
		PreparedStatement stmt = null;
		DBUtils d = new DBUtils(getClass());
		
		try {
            stmt = c.prepareStatement(SQL_LOAD_STPINTERFACE);
            d.watch(stmt);
            stmt.setInt(1, m_nodeId);
            stmt.setInt(2, m_bridgeport);
            stmt.setInt(3, m_stpportvlan);

            // Run the select
            ResultSet rset = stmt.executeQuery();
            d.watch(rset);
            if (!rset.next()) {
            	if (log.isDebugEnabled())
            		log.debug("StpInterfaceEntry.load: no result found");
            	return false;
            }

            // extract the values.
            //
            int ndx = 1;

            // get the base bridge address
            //
            m_ifindex = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_ifindex = -1;

            m_stpportstate = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_stpportstate = -1;

            m_stpportpathcost = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_stpportpathcost = -1;

            m_stpportdesignatedroot = rset.getString(ndx++);
            if (rset.wasNull())
            	m_stpportdesignatedroot = null;

            m_stpportdesignatedcost = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_stpportdesignatedcost = -1;

            m_stpportdesignatedbridge = rset.getString(ndx++);
            if (rset.wasNull())
            	m_stpportdesignatedbridge = null;

            m_stpportdesignatedport = rset.getString(ndx++);
            if (rset.wasNull())
            	m_stpportdesignatedport = null;

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
		if (log.isDebugEnabled())
			log.debug("StpInterfaceEntry.load: result found");
		m_changed = 0;
		return true;
	}

	/**
	 * Default constructor.
	 *  
	 */

	DbStpInterfaceEntry() {
		throw new UnsupportedOperationException(
				"Default constructor not supported!");
	}

	DbStpInterfaceEntry(int nodeId, int bridgeport, int stpvlan, boolean exists) {
		m_nodeId = nodeId;
		m_fromDb = exists;
		m_bridgeport = bridgeport;
		m_ifindex = -1;
		m_stpportstate = -1;
		m_stpportpathcost = -1;
		m_stpportdesignatedcost = -1;
		m_stpportvlan = stpvlan;
		m_stpportdesignatedbridge = null;
		m_stpportdesignatedroot = null;
		m_stpportdesignatedport = null;
	}

	static DbStpInterfaceEntry create(int nodeId, int bridgeport, int vlan) {
		return new DbStpInterfaceEntry(nodeId, bridgeport, vlan, false);
	}

	/**
	 * @return
	 */
	public int get_nodeId() {
		return m_nodeId;
	}

	/**
	 * @return
	 */
	public int get_bridgeport() {
		return m_bridgeport;
	}

	/**
	 * @return
	 */
	public int get_stpvlan() {
		return m_stpportvlan;
	}

	/**
	 * @return
	 */
	public int get_ifindex() {
		return m_ifindex;
	}

	void set_ifindex(int index) {
		m_ifindex = index;
		m_changed |= CHANGED_IFINDEX;
	}

	boolean hasIfIndexChanged() {
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
	public int get_stpportstate() {
		return m_stpportstate;
	}

	void set_stpportstate(int stpportstate) {
		if (stpportstate == STP_PORT_BLOCKING
				|| stpportstate == STP_PORT_BROKEN
				|| stpportstate == STP_PORT_DISABLED
				|| stpportstate == STP_PORT_FORWARDING
				|| stpportstate == STP_PORT_LEARNING
				|| stpportstate == STP_PORT_LISTENING)
			m_stpportstate = stpportstate;
		else
			m_stpportstate = -1;
		m_changed |= CHANGED_STP_PORT_STATE;
	}

	boolean hasStpPortStateChanged() {
		if ((m_changed & CHANGED_STP_PORT_STATE) == CHANGED_STP_PORT_STATE)
			return true;
		else
			return false;
	}

	boolean updateStpPortState(int stpportstate) {
		if (stpportstate != m_stpportstate) {
			set_stpportstate(stpportstate);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_stpportpathcost() {
		return m_stpportpathcost;
	}

	void set_stpportpathcost(int stpportpathcost) {
		m_stpportpathcost = stpportpathcost;
		m_changed |= CHANGED_STP_PORT_PATH_COST;
	}

	boolean hasStpPortPathCostChanged() {
		if ((m_changed & CHANGED_STP_PORT_PATH_COST) == CHANGED_STP_PORT_PATH_COST)
			return true;
		else
			return false;
	}

	boolean updateStpPortPathCost(int stpportpathcost) {
		if (stpportpathcost != m_stpportpathcost) {
			set_stpportpathcost(stpportpathcost);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public String get_stpportdesignatedroot() {
		return m_stpportdesignatedroot;
	}

	void set_stpportdesignatedroot(String stpportdesignatedroot) {
		m_stpportdesignatedroot = stpportdesignatedroot;
		m_changed |= CHANGED_STP_PORT_DES_ROOT;
	}

	boolean hasStpPortDesignatedRootChanged() {
		if ((m_changed & CHANGED_STP_PORT_DES_ROOT) == CHANGED_STP_PORT_DES_ROOT)
			return true;
		else
			return false;
	}

	boolean updateStpportDesignatedRoot(String stpportdesignatedroot) {
		if (stpportdesignatedroot != m_stpportdesignatedroot) {
			set_stpportdesignatedroot(stpportdesignatedroot);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public int get_stpportdesignatedcost() {
		return m_stpportdesignatedcost;
	}

	void set_stpportdesignatedcost(int stpportdesignatedcost) {
		m_stpportdesignatedcost = stpportdesignatedcost;
		m_changed |= CHANGED_STP_PORT_DES_COST;
	}

	boolean hasStpPortDesgnatedCostChanged() {
		if ((m_changed & CHANGED_STP_PORT_DES_COST) == CHANGED_STP_PORT_DES_COST)
			return true;
		else
			return false;
	}

	boolean updateStpPortDesignatedCost(int stpportdesignatedcost) {
		if (stpportdesignatedcost != m_stpportdesignatedcost) {
			set_stpportdesignatedcost(stpportdesignatedcost);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public String get_stpportdesignatedbridge() {
		return m_stpportdesignatedbridge;
	}

	void set_stpportdesignatedbridge(String stpportdesignatedbridge) {
		m_stpportdesignatedbridge = stpportdesignatedbridge;
		m_changed |= CHANGED_STP_PORT_DES_BRIDGE;
	}

	boolean hasStpPortDesignatedBridgeChanged() {
		if ((m_changed & CHANGED_STP_PORT_DES_BRIDGE) == CHANGED_STP_PORT_DES_BRIDGE)
			return true;
		else
			return false;
	}

	boolean updateStpportDesignatedBridge(String stpportdesignatedbridge) {
		if (stpportdesignatedbridge != m_stpportdesignatedbridge) {
			set_stpportdesignatedbridge(stpportdesignatedbridge);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	public String get_stpdesignatedport() {
		return m_stpportdesignatedport;
	}

	void set_stpportdesignatedport(String stpportdesignatedport) {
		m_stpportdesignatedport = stpportdesignatedport;
		m_changed |= CHANGED_STP_PORT_DES_PORT;
	}

	boolean hasStpPortDesignatedPortChanged() {
		if ((m_changed & CHANGED_STP_PORT_DES_PORT) == CHANGED_STP_PORT_DES_PORT)
			return true;
		else
			return false;
	}

	boolean updateStpportDesignatedPort(String stpportdesignatedport) {
		if (stpportdesignatedport != m_stpportdesignatedport) {
			set_stpportdesignatedport(stpportdesignatedport);
			return true;
		} else
			return false;
	}

	/**
	 * @return
	 */
	char get_status() {
		return m_status;
	}

	void set_status(char status) {
		if (status == STATUS_ACTIVE || status == STATUS_NOT_POLLED
				|| status == STATUS_DELETE)
			m_status = status;
		m_changed |= CHANGED_STATUS;
	}

	boolean hasStatusChanged() {
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
	Timestamp get_lastpolltime() {
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
	 * @param time
	 *            The last poll time.
	 *  
	 */
	void set_lastpolltime(String time) throws ParseException {
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
	 * @param time
	 *            The last poll time.
	 *  
	 */
	void set_lastpolltime(Date time) {
		m_lastPollTime = new Timestamp(time.getTime());
		m_changed |= CHANGED_POLLTIME;
	}

	/**
	 * Sets the last poll time.
	 * 
	 * @param time
	 *            The last poll time.
	 *  
	 */
	void set_lastpolltime(Timestamp time) {
		m_lastPollTime = time;
		m_changed |= CHANGED_POLLTIME;
	}

	/**
	 * Updates the interface information in the configured database. If the
	 * interface does not exist the a new row in the table is created. If the
	 * element already exists then it's current row is updated as needed based
	 * upon the current changes to the node.
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
					ThreadCategory.getInstance(getClass()).warn(
							"Exception closing JDBC connection", e);
				}
			}
		}
		return;
	}

	/**
	 * Updates the interface information in the configured database. If the
	 * stpnode row does not exist the a new row in the table is created. If the
	 * element already exists then it's current row is updated as needed based
	 * upon the current changes to the node.
	 * 
	 * @param db
	 *            The database connection used to write the record.
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
	 * Retreives a current record from the database based upon the key fields of
	 * <em>nodeID</em>,<em>bridgeport</em> and <em>spvlan</em>. If the
	 * record cannot be found then a null reference is returnd.
	 * 
	 * @param nid
	 *            The node id key
	 * @param bridgeport
	 *            the bridge port identifier
	 * @param stpvlan
	 *            The vlan index
	 * 
	 * @return The loaded entry or null if one could not be found.
	 *  
	 */
	static DbStpInterfaceEntry get(int nid, int bridgeport, int vlan)
			throws SQLException {
		Connection db = null;
		try {
			db = DataSourceFactory.getInstance().getConnection();
			return get(db, nid, bridgeport, vlan);
		} finally {
			try {
				if (db != null)
					db.close();
			} catch (SQLException e) {
				ThreadCategory.getInstance(DbStpNodeEntry.class).warn(
						"Exception closing JDBC connection", e);
			}
		}
	}

	/**
	 * Retreives a current record from the database based upon the key fields of
	 * <em>nodeID</em>,<em>bridgeport</em> and <em>spvlan</em>. If the
	 * record cannot be found then a null reference is returnd.
	 * 
	 * @param db
	 *            The databse connection used to load the entry.
	 * @param nid
	 *            The node id key
	 * @param bridgeport
	 *            the bridge port identifier
	 * @param stpvlan
	 *            The vlan index
	 * 
	 * @return The loaded entry or null if one could not be found.
	 *  
	 */
	static DbStpInterfaceEntry get(Connection db, int nid, int bridgeport,
			int vlan) throws SQLException {
		DbStpInterfaceEntry entry = new DbStpInterfaceEntry(nid, bridgeport,
				vlan, true);
		if (!entry.load(db))
			entry = null;
		return entry;
	}

	public String toString() {
		String sep = System.getProperty("line.separator");
		StringBuffer buf = new StringBuffer();

		buf.append("from db = ").append(m_fromDb).append(sep);
		buf.append("node id = ").append(m_nodeId).append(sep);
		buf.append("bridge port= ").append(m_bridgeport).append(sep);
		buf.append("ifindex = ").append(m_ifindex).append(sep);
		buf.append("stp vlan index = ").append(m_stpportvlan).append(sep);
		buf.append("stp port state= ").append(m_stpportstate).append(sep);
		buf.append("stp port path cost = ").append(m_stpportpathcost).append(
				sep);
		buf.append("stp port designatd root = ")
				.append(m_stpportdesignatedroot).append(sep);
		buf.append("stp port designated cost = ").append(
				m_stpportdesignatedcost).append(sep);
		buf.append("stp port designated bridge  = ").append(
				m_stpportdesignatedbridge).append(sep);
		buf.append("stp port designated port = ").append(
				m_stpportdesignatedport).append(sep);
		buf.append("status = ").append(m_status).append(sep);
		buf.append("last poll time = ").append(m_lastPollTime).append(sep);
		return buf.toString();
	}

}