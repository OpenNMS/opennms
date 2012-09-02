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
 * <p>DbStpNodeEntry class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class DbStpNodeEntry
{
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
	 * the bridge type
	 */

	public static final int BASE_TYPE_UNKNOWN = 1;
	public static final int BASE_TYPE_TRANSPARENT_ONLY = 2;
	public static final int BASE_TYPE_SOURCEROUTE_ONLY = 3;
	public static final int BASE_TYPE_SRT = 4;

	/**
	 * the STP Protocol Specification
	 */

	public static final int STP_UNKNOWN = 1;
	public static final int STP_DECLB100 = 2;
	public static final int STP_IEEE8011D = 3;

	/**
	 * The node identifier
	 */

        int     m_nodeId;

        /**
         * The MAC address used by this bridge when it must
         * be referred to in a unique fashion.
         */
        String  m_basebridgeaddress;


		/**
		 * The number of ports controlled by the bridge entity.
		 */

        int     m_basenumports;

        /**
         * Indicates what type of bridging this bridge can perform.
         * '1' unknown
         * '2' transparent-only
         * '3' sourceroute-only
         * '4' srt
         */

        int     m_basetype;

		/**
		 * An indication of what version of the Spanning Tree Protocol is being run. 
		 *	'1' unknown
		 *	'2' decLb100
		 *	'3' ieee8011d
		 */  

        int     m_stpprotocolspecification;

		/** 
		 *  The value of the write-able portion of the Bridge
		 *   ID, i.e., the first two octets of the (8 octet
		 *   long) Bridge ID. The other (last) 6 octets of the
		 *   Bridge ID are given by the value of dot1dBaseBridgeAddress.
		*/

        int     m_stppriority;

        /**
         * The bridge identifier of the root of the spanning
         * tree as determined by the Spanning Tree Protocol
         * as executed by this node. 
         */
		String  m_stpdesignatedroot;

		/**
		 * The cost of the path to the root as seen from this bridge.
		 */
		int     m_stprootcost;

		/**
		 * The port number of the port which offers the
		 * lowest cost path from this bridge to the root bridge.
		 */
		int     m_stprootport;
		
		/**
		 * Unique integer identifier VLAN for which this info is valid
		 */

		int     m_basevlan;
		
		/**
		 * The VLAN name
		 */
		
		String  m_basevlanname;
		
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
		private static final String SQL_LOAD_STPNODE = "SELECT baseBridgeAddress,baseNumPorts,basetype,stpProtocolSpecification," +
				"stpPriority,stpdesignatedroot,stprootcost,stprootport,basevlanname,status,lastPollTime FROM stpnode WHERE nodeid = ? AND basevlan = ? ";

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
		private static final int CHANGED_BRIDGEADDR = 1 << 0;

		private static final int CHANGED_NUMPORTS = 1 << 1;

		private static final int CHANGED_BASETYPE = 1 << 2;

		private static final int CHANGED_STPPROTSPEC = 1 << 3;
		
		private static final int CHANGED_STPPRIORITY = 1 << 4;
		
		private static final int CHANGED_STPDESROOT = 1 << 5;

		private static final int CHANGED_STPROOTCOST = 1 << 6;

		private static final int CHANGED_STPROOTPORT = 1 << 7;

		private static final int CHANGED_VLANNAME = 1 << 8;

		private static final int CHANGED_STATUS = 1 << 9;

		private static final int CHANGED_POLLTIME = 1 << 10;

		/**
		 * Inserts the new row into the StpNode table
		 * of the OpenNMS database.
		 *
		 * @param c	The connection to the database.
		 *
		 * @throws java.sql.SQLException Thrown if an error occurs
		 * 	with the connection
		 */
		private void insert(Connection c) throws SQLException {
			if (m_fromDb)
				throw new IllegalStateException(
						"The STP node record already exists in the database");

			// first extract the next node identifier
			//
			StringBuffer names = new StringBuffer("INSERT INTO StpNode (nodeid,basevlan");
			StringBuffer values = new StringBuffer("?,?");

			values.append(",?");
			names.append(",baseBridgeAddress");

			values.append(",?");
			names.append(",baseNumPorts");

			values.append(",?");
			names.append(",basetype");
		
			values.append(",?");
			names.append(",stpProtocolSpecification");

			values.append(",?");
			names.append(",stpPriority");

			values.append(",?");
			names.append(",stpdesignatedroot");

			values.append(",?");
			names.append(",stprootcost");

			values.append(",?");
			names.append(",stprootport");

			values.append(",?");
			names.append(",basevlanname");

			values.append(",?");
			names.append(",status");

			values.append(",?");
			names.append(",lastpolltime");

			names.append(") VALUES (").append(values).append(')');

			LogUtils.debugf(this, "DbStpNodeEntry.insert: SQL insert statment = %s", names.toString());

			final DBUtils d = new DBUtils(getClass());
			
			try {
                PreparedStatement stmt = c.prepareStatement(names.toString());
                d.watch(stmt);

                int ndx = 1;
                stmt.setInt(ndx++, m_nodeId);
                stmt.setInt(ndx++, m_basevlan);
            	stmt.setString(ndx++, m_basebridgeaddress);
            	stmt.setInt(ndx++, m_basenumports);
            	stmt.setInt(ndx++, m_basetype);
            	stmt.setInt(ndx++, m_stpprotocolspecification);
            	stmt.setInt(ndx++, m_stppriority);
            	stmt.setString(ndx++, m_stpdesignatedroot);
            	stmt.setInt(ndx++, m_stprootcost);
            	stmt.setInt(ndx++, m_stprootport);
            	stmt.setString(ndx++, m_basevlanname);
            	stmt.setString(ndx++, new String(new char[] { m_status }));
            	stmt.setTimestamp(ndx++, m_lastPollTime);

                // Run the insert
                //
                int rc = stmt.executeUpdate();
                LogUtils.debugf(this, "StpNodeEntry.insert: row %d", rc);
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
			StringBuffer sqlText = new StringBuffer("UPDATE StpNode SET ");

			char comma = ' ';


			if ((m_changed & CHANGED_BRIDGEADDR) == CHANGED_BRIDGEADDR) {
				sqlText.append(comma).append("baseBridgeAddress = ?");
				comma = ',';
			}

			if ((m_changed & CHANGED_NUMPORTS) == CHANGED_NUMPORTS) {
				sqlText.append(comma).append("baseNumPorts = ?");
				comma = ',';
			}

			if ((m_changed & CHANGED_BASETYPE) == CHANGED_BASETYPE) {
				sqlText.append(comma).append("basetype = ?");
				comma = ',';
			}
			
			if ((m_changed & CHANGED_STPPROTSPEC) == CHANGED_STPPROTSPEC) {
				sqlText.append(comma).append("stpProtocolSpecification = ?");
				comma = ',';
			}

			if ((m_changed & CHANGED_STPPRIORITY) == CHANGED_STPPRIORITY) {
				sqlText.append(comma).append("stpPriority = ?");
				comma = ',';
			}

			if ((m_changed & CHANGED_STPDESROOT) == CHANGED_STPDESROOT) {
				sqlText.append(comma).append("stpdesignatedroot = ?");
				comma = ',';
			}

			if ((m_changed & CHANGED_STPROOTCOST) == CHANGED_STPROOTCOST) {
				sqlText.append(comma).append("stprootcost = ?");
				comma = ',';
			}

			if ((m_changed & CHANGED_STPROOTPORT) == CHANGED_STPROOTPORT) {
				sqlText.append(comma).append("stprootport = ?");
				comma = ',';
			}

			if ((m_changed & CHANGED_VLANNAME) == CHANGED_VLANNAME) {
				sqlText.append(comma).append("basevlanname = ?");
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

			sqlText.append(" WHERE nodeid = ? AND basevlan = ? ");

			LogUtils.debugf(this, "DbStpNodeEntry.update: SQL insert statment = %s", sqlText.toString());

			final DBUtils d = new DBUtils(getClass());
			try {
                PreparedStatement stmt = c.prepareStatement(sqlText.toString());
                d.watch(stmt);

                int ndx = 1;

                if ((m_changed & CHANGED_BRIDGEADDR) == CHANGED_BRIDGEADDR)
                	stmt.setString(ndx++, m_basebridgeaddress);

                if ((m_changed & CHANGED_NUMPORTS) == CHANGED_NUMPORTS)
                	stmt.setInt(ndx++, m_basenumports);

                if ((m_changed & CHANGED_BASETYPE) == CHANGED_BASETYPE)
                	stmt.setInt(ndx++, m_basetype);
                
                if ((m_changed & CHANGED_STPPROTSPEC) == CHANGED_STPPROTSPEC) 
                	stmt.setInt(ndx++, m_stpprotocolspecification);

                if ((m_changed & CHANGED_STPPRIORITY) == CHANGED_STPPRIORITY) 
                	stmt.setInt(ndx++, m_stppriority);

                if ((m_changed & CHANGED_STPDESROOT) == CHANGED_STPDESROOT)
                	stmt.setString(ndx++, m_stpdesignatedroot);

                if ((m_changed & CHANGED_STPROOTCOST) == CHANGED_STPROOTCOST)
                	stmt.setInt(ndx++, m_stprootcost);

                if ((m_changed & CHANGED_STPROOTPORT) == CHANGED_STPROOTPORT) 
                	stmt.setInt(ndx++, m_stprootport);
                
                if ((m_changed & CHANGED_VLANNAME) == CHANGED_VLANNAME) 
                	stmt.setString(ndx++, m_basevlanname);

                if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
                	stmt.setString(ndx++, new String(new char[] { m_status }));

                if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
                	stmt.setTimestamp(ndx++, m_lastPollTime);
                }

                stmt.setInt(ndx++, m_nodeId);
                stmt.setInt(ndx++, m_basevlan);

                // Run the insert
                //
                int rc = stmt.executeUpdate();
                LogUtils.debugf(this, "StpNodeEntry.update: row %d", rc);
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
				throw new IllegalStateException("The STP node record does not exist in the database");

			final DBUtils d = new DBUtils(getClass());
			try {
                PreparedStatement stmt = null;
                stmt = c.prepareStatement(SQL_LOAD_STPNODE);
                d.watch(stmt);
                stmt.setInt(1, m_nodeId);
                stmt.setInt(2, m_basevlan);

                // Run the select
                //
                ResultSet rset = stmt.executeQuery();
                d.watch(rset);
                if (!rset.next()) {
                    LogUtils.debugf(this, "StpNodeEntry.load: no result found");
                	return false;
                }

                // extract the values.
                //
                int ndx = 1;

                // get the base bridge address
                //
                m_basebridgeaddress = rset.getString(ndx++);
                if (rset.wasNull())
                	m_basebridgeaddress = null;

                // get base bridge port numbers
                //
                m_basenumports = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_basenumports = -1;

                // get the base type
                //
                m_basetype = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_basetype = BASE_TYPE_UNKNOWN;

                m_stpprotocolspecification = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_stpprotocolspecification = STP_UNKNOWN;
                
                m_stppriority = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_stppriority = -1;
                
                m_stpdesignatedroot = rset.getString(ndx++);
                if (rset.wasNull())
                	m_stpdesignatedroot= null;
                
                m_stprootcost = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_stprootcost = -1;

                m_stprootport = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_stprootport = -1;

                m_basevlanname = rset.getString(ndx++);
                if (rset.wasNull())
                	m_basevlanname = null;

                String str = rset.getString(ndx++);
                if (str != null && !rset.wasNull())
                	m_status = str.charAt(0);
                else
                	m_status = STATUS_UNKNOWN;

                m_lastPollTime = rset.getTimestamp(ndx++);
                stmt.close();
			} finally {
			    d.cleanUp();
            }

			// clear the mask and mark as backed
			// by the database
			//
			LogUtils.debugf(this, "StpNodeEntry.load: result found");
			m_changed = 0;
			return true;
		}

        DbStpNodeEntry(int nodeId, int basevlan, boolean exists)
        {
                m_nodeId = nodeId;
                m_fromDb = exists;
                m_basenumports = -1;
				m_basetype = BASE_TYPE_UNKNOWN;
				m_stpprotocolspecification = STP_UNKNOWN;
				m_stppriority = -1;
				m_stprootcost = -1;
				m_stprootport = -1;
				m_basevlan = basevlan;
                m_basebridgeaddress = null;
                m_stpdesignatedroot = null;
                m_basevlanname = null;
        }

        static DbStpNodeEntry create(int nodeId,int basevlan) {
        	return new DbStpNodeEntry(nodeId,basevlan, false);
        }
		/**
		 * <p>get_nodeId</p>
		 *
		 * @return a int.
		 */
		public int get_nodeId() {
			return m_nodeId;
		}

		/**
		 * <p>get_basevlan</p>
		 *
		 * @return a int.
		 */
		public int get_basevlan() {
			return m_basevlan;
		}

		/**
		 * <p>get_basevlanname</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_basevlanname() {
			return m_basevlanname;
		}

		protected void set_basevlanname(String basevlanname) {
			m_basevlanname = basevlanname;
			m_changed |= CHANGED_VLANNAME;
		}

		protected boolean hasBaseVlanNameChanged() {
			if ((m_changed & CHANGED_VLANNAME) == CHANGED_VLANNAME)
				return true;
			else
				return false;
		}

		boolean updateBaseVlanName(final String basevlanname) {
			if (m_basevlanname == null || !m_basevlanname.equals(basevlanname)) {
				set_basevlanname(basevlanname);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_basebridgeaddress</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_basebridgeaddress() {
			return m_basebridgeaddress;
		}

		protected void set_basebridgeaddress(String basebridgeaddress) {
			m_basebridgeaddress = basebridgeaddress;
			m_changed |= CHANGED_BRIDGEADDR;
		}

		protected boolean hasBaseBridgeAddressChanged() {
			if ((m_changed & CHANGED_BRIDGEADDR) == CHANGED_BRIDGEADDR)
				return true;
			else
				return false;
		}

		boolean updateBaseBridgeAddress(final String basebridgeaddress) {
			if (m_basebridgeaddress == null || !m_basebridgeaddress.equals(basebridgeaddress)) {
				set_basebridgeaddress(basebridgeaddress);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_basenumports</p>
		 *
		 * @return a int.
		 */
		public int get_basenumports() {
			return m_basenumports;
		}

		protected void set_basenumports(int basenumports) {
			m_basenumports = basenumports;
			m_changed |= CHANGED_NUMPORTS;
		}

		protected boolean hasBaseNumPortsChanged() {
			if ((m_changed & CHANGED_NUMPORTS) == CHANGED_NUMPORTS)
				return true;
			else
				return false;
		}

		boolean updateBaseNumPorts(int basenumports) {
			if (basenumports != m_basenumports) {
				set_basenumports(basenumports);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_basetype</p>
		 *
		 * @return a int.
		 */
		public int get_basetype() {
			return m_basetype;
		}

		protected void set_basetype(int basetype) {
			if (basetype == BASE_TYPE_SRT || basetype == BASE_TYPE_TRANSPARENT_ONLY || basetype == BASE_TYPE_SOURCEROUTE_ONLY)
			m_basetype = basetype;
			else 
				m_basetype = BASE_TYPE_UNKNOWN;
			m_changed |= CHANGED_BASETYPE;
		}

		protected boolean hasBaseTypeChanged() {
			if ((m_changed & CHANGED_BASETYPE) == CHANGED_BASETYPE)
				return true;
			else
				return false;
		}

		boolean updateBaseType(int basetype) {
			if (basetype != m_basetype) {
				set_basetype(basetype);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_stpdesignatedroot</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String get_stpdesignatedroot() {
			return m_stpdesignatedroot;
		}

		protected void set_stpdesignatedroot(String stpdesignatedroot) {
			m_stpdesignatedroot = stpdesignatedroot;
			m_changed |= CHANGED_STPDESROOT;
		}

		protected boolean hasStpDesignatedRootChanged() {
			if ((m_changed & CHANGED_STPDESROOT) == CHANGED_STPDESROOT)
				return true;
			else
				return false;
		}

		boolean updateStpDesignatedRoot(final String stpdesignatedroot) {
			if (m_stpdesignatedroot == null || !m_stpdesignatedroot.equals(stpdesignatedroot)) {
				set_stpdesignatedroot(stpdesignatedroot);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_stppriority</p>
		 *
		 * @return a int.
		 */
		public int get_stppriority() {
			return m_stppriority;
		}

		protected void set_stppriority(int stppriority) {
			m_stppriority = stppriority;
			m_changed |= CHANGED_STPPRIORITY;
		}

		protected boolean hasStpPriorityChanged() {
			if ((m_changed & CHANGED_STPPRIORITY) == CHANGED_STPPRIORITY)
				return true;
			else
				return false;
		}

		boolean updateStpPriority(int stppriority) {
			if (stppriority != m_stppriority) {
				set_stppriority(stppriority);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_stpprotocolspecification</p>
		 *
		 * @return a int.
		 */
		public int get_stpprotocolspecification() {
			return m_stpprotocolspecification;
		}

		protected void set_stpprotocolspecification(int stpprotocolspecification) {
			if (stpprotocolspecification == STP_DECLB100|| stpprotocolspecification == STP_IEEE8011D)
			m_stpprotocolspecification = stpprotocolspecification;
			else 
				m_stpprotocolspecification = STP_UNKNOWN;
			m_changed |= CHANGED_STPPROTSPEC;
		}

		protected boolean hasStpProtocolSpecificationChanged() {
			if ((m_changed & CHANGED_STPPROTSPEC) == CHANGED_STPPROTSPEC)
				return true;
			else
				return false;
		}

		boolean updateStpProtocolSpecification(int stpprotocolspecification) {
			if (stpprotocolspecification != m_stpprotocolspecification) {
				set_stpprotocolspecification(stpprotocolspecification);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_stprootcost</p>
		 *
		 * @return a int.
		 */
		public int get_stprootcost() {
			return m_stprootcost;
		}

		protected void set_stprootcost(int stprootcost) {
			m_stprootcost = stprootcost;
			m_changed |= CHANGED_STPROOTCOST;
		}

		protected boolean hasStpRootCostChanged() {
			if ((m_changed & CHANGED_STPROOTCOST) == CHANGED_STPROOTCOST)
				return true;
			else
				return false;
		}

		boolean updateStpRootCost(int stprootcost) {
			if (stprootcost != m_stprootcost) {
				set_stprootcost(stprootcost);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_stprootport</p>
		 *
		 * @return a int.
		 */
		public int get_stprootport() {
			return m_stprootport;
		}
		
		protected void set_stprootport(int stprootport) {
			m_stprootport = stprootport;
			m_changed |= CHANGED_STPROOTPORT;
		}

		protected boolean hasStpRootPortChanged() {
			if ((m_changed & CHANGED_STPROOTPORT) == CHANGED_STPROOTPORT)
				return true;
			else
				return false;
		}

		boolean updateStpRootPort(int stprootport) {
			if (stprootport != m_stprootport) {
				set_stprootport(stprootport);
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
		 * stpnode row does not exist the a new row in the table is created. If the
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
		 * key fields of <em>nodeID</em> and <em>basevlan</em>. If the
		 * record cannot be found then a null reference is returnd.
		 *
		 * @param nid	The node id key
		 * @param basevlan The vlan index
		 *
		 * @return The loaded entry or null if one could not be found.
		 *
		 */
		static DbStpNodeEntry get(int nid, int basevlan) throws SQLException {
			Connection db = null;
			try {
				db = DataSourceFactory.getInstance().getConnection();
				return get(db, nid, basevlan);
			} finally {
				try {
					if (db != null)
						db.close();
				} catch (SQLException e) {
                    LogUtils.warnf(DbStpNodeEntry.class, e, "Exception closing JDBC connection");
				}
			}
		}

		/**
		 * Retreives a current record from the database based upon the
		 * key fields of <em>nodeID</em> and <em>basevlan</em>. If the
		 * record cannot be found then a null reference is returnd.
		 *
		 * @param db	The databse connection used to load the entry.
		 * @param nid	The node id key
		 * @param basevan  The vlan index
		 *
		 * @return The loaded entry or null if one could not be found.
		 *
		 */
		static DbStpNodeEntry get(Connection db, int nid, int basevlan)
				throws SQLException {
			DbStpNodeEntry entry = new DbStpNodeEntry(nid, basevlan,true);
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
			    .append("baseVlanId", m_basevlan)
                .append("baseVlanName", m_basevlanname)
			    .append("baseBridgeAddress", m_basebridgeaddress)
			    .append("baseNumPorts", m_basenumports)
			    .append("baseBridgeType", m_basetype)
			    .append("stpProtocolId", m_stpprotocolspecification)
			    .append("stpBridgePriority", m_stppriority)
			    .append("stpDesignatedRoot", m_stpdesignatedroot)
			    .append("stpRootCost", m_stprootcost)
			    .append("stpRootPort", m_stprootport)
			    .append("status", m_status)
			    .append("lastPollTime", m_lastPollTime)
			    .toString();
		}

}
