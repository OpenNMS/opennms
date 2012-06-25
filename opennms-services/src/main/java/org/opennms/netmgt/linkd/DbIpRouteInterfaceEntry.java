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
 */
public final class DbIpRouteInterfaceEntry {
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
	 * Integer representing route type
	 */
	public static final int ROUTE_TYPE_OTHER = 1;

	public static final int ROUTE_TYPE_INVALID = 2;

	public static final int ROUTE_TYPE_DIRECT = 3;

	public static final int ROUTE_TYPE_INDIRECT = 4;

	/**
	 * The node identifier
	 */

	int m_nodeId;

	/**
	 * The port index on which this route is routed
	 */

	int m_routeifindex;

	/**
	 * The route metrics
	 */

	int m_routemetric1;

	int m_routemetric2;

	int m_routemetric3;

	int m_routemetric4;

	int m_routemetric5;

	/**
	 * The route type
	 */

	int m_routetype = ROUTE_TYPE_OTHER;

	/**
	 * The routing mechanism via which this route was learned
	 */
	int m_routeproto;

	/**
	 *  The destination IP address of this route. 
	 * An entry with a value of 0.0.0.0 is considered a default route.
	 */

	String m_routedest;

	/**
	 * Indicate the mask to be logical-ANDed with the
	 * destination address before being compared to the
	 * value in the ipRouteDest field.
	 --#  routeifIndex      : The index value which uniquely identifies the
	 --#                      local interface through which the next hop of this
	 --#                      route should be reached. 
	 */
	String m_routemask;

	/**
	 * The IP address of the next hop of this route.
	 * (In the case of a route bound to an interface
	 * which is realized via a broadcast media, the value
	 * of this field is the agent's IP address on that
	 * interface.)
	 */
	String m_routenexthop;

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
	private static final String SQL_LOAD_IPROUTEINTERFACE = "SELECT routeMask,routeNextHop,routeifindex,routemetric1,routemetric2,routemetric3,routemetric4,routemetric5,routetype,routeproto,status,lastpolltime FROM iprouteinterface WHERE nodeid = ? AND routeDest = ? ";

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
	private static final int CHANGED_MASK = 1 << 0;

	private static final int CHANGED_NXT_HOP = 1 << 1;

	private static final int CHANGED_IFINDEX = 1 << 2;

	private static final int CHANGED_METRIC1 = 1 << 3;

	private static final int CHANGED_METRIC2 = 1 << 4;

	private static final int CHANGED_METRIC3 = 1 << 5;

	private static final int CHANGED_METRIC4 = 1 << 6;

	private static final int CHANGED_METRIC5 = 1 << 7;

	private static final int CHANGED_TYPE = 1 << 8;

	private static final int CHANGED_PROTO = 1 << 9;

	private static final int CHANGED_STATUS = 1 << 10;

	private static final int CHANGED_POLLTIME = 1 << 11;

	/**
	 * Inserts the new row into the IpRouteInterface table
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
					"The IP route interface record already exists in the database");

		// first extract the next node identifier
		//
		StringBuffer names = new StringBuffer("INSERT INTO IpRouteInterface (nodeid,routeDest");
		StringBuffer values = new StringBuffer("?,?");

		values.append(",?");
		names.append(",routeMask");

		values.append(",?");
		names.append(",routeNextHop");

		values.append(",?");
		names.append(",routeifindex");

		values.append(",?");
		names.append(",routemetric1");

		values.append(",?");
		names.append(",routemetric2");

		values.append(",?");
		names.append(",routemetric3");

		values.append(",?");
		names.append(",routemetric4");

		values.append(",?");
		names.append(",routemetric5");

		values.append(",?");
		names.append(",routetype");

		values.append(",?");
		names.append(",routeproto");

		values.append(",?");
		names.append(",status");

		values.append(",?");
		names.append(",lastpolltime");

		names.append(") VALUES (").append(values).append(')');

		LogUtils.debugf(this, "IpRouteInterfaceEntry.insert: SQL insert statment = %s", names.toString());

		// create the Prepared statment and then
		// start setting the result values
		DBUtils d = new DBUtils(getClass());
		
		try {
            PreparedStatement stmt = c.prepareStatement(names.toString());
            d.watch(stmt);

            int ndx = 1;
            stmt.setInt(ndx++, m_nodeId);
            stmt.setString(ndx++, m_routedest);
        	stmt.setString(ndx++, m_routemask);
        	stmt.setString(ndx++, m_routenexthop);
        	stmt.setInt(ndx++, m_routeifindex);
        	stmt.setInt(ndx++, m_routemetric1);
        	stmt.setInt(ndx++, m_routemetric2);
        	stmt.setInt(ndx++, m_routemetric3);
        	stmt.setInt(ndx++, m_routemetric4);
        	stmt.setInt(ndx++, m_routemetric5);
        	stmt.setInt(ndx++, m_routetype);
        	stmt.setInt(ndx++, m_routeproto);
        	stmt.setString(ndx++, new String(new char[] { m_status }));
        	stmt.setTimestamp(ndx++, m_lastPollTime);
            
            // Run the insert
            //
            int rc = stmt.executeUpdate();
            LogUtils.debugf(this, "IpRouteInterfaceEntry.insert: row %d", rc);
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
		StringBuffer sqlText = new StringBuffer("UPDATE IpRouteInterface SET ");

		char comma = ' ';

		if ((m_changed & CHANGED_MASK) == CHANGED_MASK) {
			sqlText.append(comma).append("routeMask = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_NXT_HOP) == CHANGED_NXT_HOP) {
			sqlText.append(comma).append("routeNextHop = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
			sqlText.append(comma).append("routeifindex = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC1) == CHANGED_METRIC1) {
			sqlText.append(comma).append("routemetric1 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC2) == CHANGED_METRIC2) {
			sqlText.append(comma).append("routemetric2 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC3) == CHANGED_METRIC3) {
			sqlText.append(comma).append("routemetric3 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC4) == CHANGED_METRIC4) {
			sqlText.append(comma).append("routemetric4 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_METRIC5) == CHANGED_METRIC5) {
			sqlText.append(comma).append("routemetric5 = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE) {
			sqlText.append(comma).append("routetype = ?");
			comma = ',';
		}

		if ((m_changed & CHANGED_PROTO) == CHANGED_PROTO) {
			sqlText.append(comma).append("routeproto = ?");
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

		sqlText.append(" WHERE nodeid = ? AND routeDest = ? ");

		LogUtils.debugf(this, "IpRouteInterfaceEntry.update: SQL insert statment = %s", sqlText.toString());
		
		// create the Prepared statment and then
		// start setting the result values
		//
		DBUtils d = new DBUtils(getClass());
		
		try {
            PreparedStatement stmt = c.prepareStatement(sqlText.toString());
            d.watch(stmt);

            int ndx = 1;

            if ((m_changed & CHANGED_MASK) == CHANGED_MASK)
            	stmt.setString(ndx++, m_routemask);

            if ((m_changed & CHANGED_NXT_HOP) == CHANGED_NXT_HOP)
            	stmt.setString(ndx++, m_routenexthop);

            if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
            	stmt.setInt(ndx++, m_routeifindex);

            if ((m_changed & CHANGED_METRIC1) == CHANGED_METRIC1)
            	stmt.setInt(ndx++, m_routemetric1);

            if ((m_changed & CHANGED_METRIC2) == CHANGED_METRIC2)
            	stmt.setInt(ndx++, m_routemetric2);

            if ((m_changed & CHANGED_METRIC3) == CHANGED_METRIC3)
            	stmt.setInt(ndx++, m_routemetric3);

            if ((m_changed & CHANGED_METRIC4) == CHANGED_METRIC4)
            	stmt.setInt(ndx++, m_routemetric4);

            if ((m_changed & CHANGED_METRIC5) == CHANGED_METRIC5)
            	stmt.setInt(ndx++, m_routemetric5);

            if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE)
            	stmt.setInt(ndx++, m_routetype);

            if ((m_changed & CHANGED_PROTO) == CHANGED_PROTO)
            	stmt.setInt(ndx++, m_routeproto);

            if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
            	stmt.setString(ndx++, new String(new char[] { m_status }));

            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
            	stmt.setTimestamp(ndx++, m_lastPollTime);
            }

            stmt.setInt(ndx++, m_nodeId);
            stmt.setString(ndx++, m_routedest);

            // Run the insert
            //
            int rc = stmt.executeUpdate();
            LogUtils.debugf(this, "IpRouteInterfaceEntry.update: row %d", rc);
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
			throw new IllegalStateException("The record does not exists in the database");

		// create the Prepared statment and then
		// start setting the result values
		//
		PreparedStatement stmt = null;
		DBUtils d = new DBUtils(getClass());
		
		// Run the select
        		//
        ResultSet rset;
        try {
            stmt = c.prepareStatement(SQL_LOAD_IPROUTEINTERFACE);
            d.watch(stmt);
            stmt.setInt(1, m_nodeId);
            stmt.setString(2, m_routedest);

            rset = stmt.executeQuery();
            d.watch(rset);
            if (!rset.next()) {
                LogUtils.debugf(this, "IpRouteInterfaceEntry.load: no result found");
            	return false;
            }

            // extract the values.
            //
            int ndx = 1;

            // get the route netmask
            //
            m_routemask = rset.getString(ndx++);
            if (rset.wasNull())
            	m_routemask = null;

            // get the next hop ip address
            //
            m_routenexthop = rset.getString(ndx++);
            if (rset.wasNull())
            	m_routenexthop = null;

            // get the interface ifindex for routing info
            //
            m_routeifindex = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_routeifindex = -1;
            // get the metrics
            m_routemetric1 = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_routemetric1 = -1;

            m_routemetric2 = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_routemetric2 = -1;

            m_routemetric3 = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_routemetric3 = -1;
            
            m_routemetric4 = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_routemetric4 = -1;

            m_routemetric5 = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_routemetric5 = -1;
            
            m_routetype = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_routetype = ROUTE_TYPE_OTHER;
            
            m_routeproto = rset.getInt(ndx++);
            if (rset.wasNull())
            	m_routeproto = -1;
            
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
        LogUtils.debugf(this, "IpRouteInterfaceEntry.load: result found");
		m_changed = 0;
		return true;
	}

	DbIpRouteInterfaceEntry(int nodeId, String routedest, boolean exists) {
		m_nodeId = nodeId;
		m_fromDb = exists;
		m_routeifindex = -1;
		m_routemetric1 = -1;
		m_routemetric2 = -1;
		m_routemetric3 = -1;
		m_routemetric4 = -1;
		m_routemetric5 = -1;
		m_routetype = ROUTE_TYPE_OTHER;
		m_routeproto = -1;
		m_routenexthop = null;
		m_routedest = routedest;
		m_routemask = null;
	}

	static DbIpRouteInterfaceEntry create(int nodeid, String routedest) {
		return new DbIpRouteInterfaceEntry(nodeid, routedest, false);
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
	protected String get_routedest() {
		return m_routedest;
	}

	/**
	 * @return
	 */
	protected String get_routemask() {
		return m_routemask;
	}

	protected void set_routemask(String routemask) {
		m_routemask = routemask;
		m_changed |= CHANGED_MASK;
	}

	protected boolean hasRouteMaskChanged() {
		if ((m_changed & CHANGED_MASK) == CHANGED_MASK)
			return true;
		else
			return false;
	}

	boolean updateRouteMask(final String routemask) {
		if (m_routemask == null || !m_routemask.equals(routemask)) {
			set_routemask(routemask);
			return true;
		} else
			return false;
	}

	/**
	 * <p>get_routenexthop</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected String get_routenexthop() {
		return m_routenexthop;
	}

	protected void set_routenexthop(String routenexthop) {
		m_routenexthop = routenexthop;
		m_changed |= CHANGED_NXT_HOP;
	}

	protected boolean hasRouteNextHopChanged() {
		if ((m_changed & CHANGED_NXT_HOP) == CHANGED_NXT_HOP)
			return true;
		else
			return false;
	}

	boolean updateRouteNextHop(final String routenexthop) {
		if (m_routenexthop == null || !m_routenexthop.equals(routenexthop)) {
			set_routenexthop(routenexthop);
			return true;
		} else
			return false;
	}

	/**
	 * <p>get_ifindex</p>
	 *
	 * @return a int.
	 */
	protected int get_ifindex() {
		return m_routeifindex;
	}

	protected void set_ifindex(int ifindex) {
		m_routeifindex = ifindex;
		m_changed |= CHANGED_IFINDEX;
	}

	protected boolean hasIfIndexChanged() {
		if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX)
			return true;
		else
			return false;
	}

	boolean updateIfIndex(int ifindex) {
		if (ifindex != m_routeifindex) {
			set_ifindex(ifindex);
			return true;
		} else
			return false;
	}

	/**
	 * <p>get_routemetric1</p>
	 *
	 * @return a int.
	 */
	protected int get_routemetric1() {
		return m_routemetric1;
	}

	protected void set_routemetric1(int routemetric) {
		m_routemetric1 = routemetric;
		m_changed |= CHANGED_METRIC1;
	}

	protected boolean hasRouteMetric1Changed() {
		if ((m_changed & CHANGED_METRIC1) == CHANGED_METRIC1)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric1(int routemetric) {
		if (routemetric != m_routemetric1) {
			set_routemetric1(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * <p>get_routemetric2</p>
	 *
	 * @return a int.
	 */
	protected int get_routemetric2() {
		return m_routemetric2;
	}

	protected void set_routemetric2(int routemetric) {
		m_routemetric2 = routemetric;
		m_changed |= CHANGED_METRIC2;
	}

	protected boolean hasRouteMetric2Changed() {
		if ((m_changed & CHANGED_METRIC2) == CHANGED_METRIC2)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric2(int routemetric) {
		if (routemetric != m_routemetric2) {
			set_routemetric2(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * <p>get_routemetric3</p>
	 *
	 * @return a int.
	 */
	protected int get_routemetric3() {
		return m_routemetric3;
	}

	protected void set_routemetric3(int routemetric) {
		m_routemetric3 = routemetric;
		m_changed |= CHANGED_METRIC3;
	}

	protected boolean hasRouteMetric3Changed() {
		if ((m_changed & CHANGED_METRIC3) == CHANGED_METRIC3)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric3(int routemetric) {
		if (routemetric != m_routemetric3) {
			set_routemetric3(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * <p>get_routemetric4</p>
	 *
	 * @return a int.
	 */
	protected int get_routemetric4() {
		return m_routemetric4;
	}

	protected void set_routemetric4(int routemetric) {
		m_routemetric4 = routemetric;
		m_changed |= CHANGED_METRIC4;
	}

	protected boolean hasRouteMetric4Changed() {
		if ((m_changed & CHANGED_METRIC4) == CHANGED_METRIC4)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric4(int routemetric) {
		if (routemetric != m_routemetric4) {
			set_routemetric4(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * <p>get_routemetric5</p>
	 *
	 * @return a int.
	 */
	protected int get_routemetric5() {
		return m_routemetric5;
	}

	protected void set_routemetric5(int routemetric) {
		m_routemetric5 = routemetric;
		m_changed |= CHANGED_METRIC5;
	}

	protected boolean hasRouteMetric5Changed() {
		if ((m_changed & CHANGED_METRIC5) == CHANGED_METRIC5)
			return true;
		else
			return false;
	}

	boolean updateRouteMetric5(int routemetric) {
		if (routemetric != m_routemetric5) {
			set_routemetric5(routemetric);
			return true;
		} else
			return false;
	}

	/**
	 * <p>get_routetype</p>
	 *
	 * @return a int.
	 */
	protected int get_routetype() {
		return m_routetype;
	}

	protected void set_routetype(int routetype) {
		if (routetype == ROUTE_TYPE_OTHER || routetype == ROUTE_TYPE_INVALID
				|| routetype == ROUTE_TYPE_DIRECT
				|| routetype == ROUTE_TYPE_INDIRECT)
			m_routetype = routetype;
		m_changed |= CHANGED_TYPE;
	}

	protected boolean hasRouteTypeChanged() {
		if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE)
			return true;
		else
			return false;
	}

	boolean updateRouteType(int routetype) {
		if (routetype != m_routetype) {
			set_routetype(routetype);
			return true;
		} else
			return false;
	}

	/**
	 * <p>get_routeproto</p>
	 *
	 * @return a int.
	 */
	protected int get_routeproto() {
		return m_routeproto;
	}

	protected void set_routeproto(int routeproto) {
		m_routeproto = routeproto;
		m_changed |= CHANGED_PROTO;
	}

	protected boolean hasRouteProtoChanged() {
		if ((m_changed & CHANGED_PROTO) == CHANGED_PROTO)
			return true;
		else
			return false;
	}

	boolean updateRouteProto(int routeproto) {
		if (routeproto != m_routeproto) {
			set_routeproto(routeproto);
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
	static DbIpRouteInterfaceEntry get(int nid, String routedest)
			throws SQLException {
		Connection db = null;
		try {
			db = DataSourceFactory.getInstance().getConnection();
			return get(db, nid, routedest);
		} finally {
			try {
				if (db != null)
					db.close();
			} catch (SQLException e) {
                LogUtils.warnf(DbIpRouteInterfaceEntry.class, e, "Exception closing JDBC connection");
			}
		}
	}

	/**
	 * Retreives a current record from the database based upon the
	 * key fields of <em>nodeID</em> and <em>routedest</em>. If the
	 * record cannot be found then a null reference is returnd.
	 *
	 * @param db	The database connection used to load the entry.
	 * @param nid	The node id key
	 * @param routedest The ip route destination address
	 *
	 * @return The loaded entry or null if one could not be found.
	 *
	 */
	static DbIpRouteInterfaceEntry get(Connection db, int nid, String routedest)
			throws SQLException {
		DbIpRouteInterfaceEntry entry = new DbIpRouteInterfaceEntry(nid,
				routedest,true);
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
        .append("db", m_fromDb)
        .append("nodeId", m_nodeId)
        .append("routeDestination", m_routedest)
        .append("routeMask", m_routemask)
        .append("routeNextHop", m_routenexthop)
        .append("ifIndex", m_routeifindex)
        .append("routeMetric1", m_routemetric1)
        .append("routeMetric2", m_routemetric2)
        .append("routeMetric3", m_routemetric3)
        .append("routeMetric4", m_routemetric4)
        .append("routeMetric5", m_routemetric5)
        .append("routeType", m_routetype)
        .append("routeProtocol", m_routeproto)
        .append("status", m_status)
        .append("lastPollTime", m_lastPollTime)
        .toString();
	}

}
