/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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
 * <p>DbVlanEntry class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class DbVlanEntry
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

    public static final int VLAN_TYPE_UNKNOWN = 0;
    public static final int VLAN_TYPE_ETHERNET = 1;
    public static final int VLAN_TYPE_FDDI = 2;
    public static final int VLAN_TYPE_TOKEN_RING = 3;
    public static final int VLAN_TYPE_FDDINET = 4;
    public static final int VLAN_TYPE_TRNET = 5;
    public static final int VLAN_TYPE_DEPRECATED = 6;

    public static final int VLAN_STATUS_UNKNOWN = 0;
    public static final int VLAN_STATUS_OPERATIONAL = 1;
    public static final int VLAN_STATUS_SUSPENDED = 2;
    public static final int VLAN_STATUS_MTU_TOO_BIG_FOR_DEVICE = 3;
    public static final int VLAN_STATUS_MTU_TOO_BIG_FOR_TRUNK = 4;

	/**
	 * The node identifier
	 */
    int     m_nodeId;

    /**
     * The vlan identifier to be referred to in a unique fashion.
     */
    int     m_vlanId;

    /**
     * The name of the VLAN
     */
    String m_vlanname;
    
    /**
     * Indicates what type of vlan is this:
     * '1' ethernet
     * '2' FDDI
     * '3' TokenRing
     * '4' FDDINet
     * '5' TRNet
     * '6' Deprecated
     * 
     */
    int     m_vlantype = VLAN_TYPE_UNKNOWN;

    /**
     * 
     * An indication of what is the Vlan Status:
     * '1' operational
     * '2' suspendid
     * '3' mtuTooBigForDevice
     * '4' mtuTooBigForTrunk
     * 
     */
    int     m_vlanstatus = VLAN_STATUS_UNKNOWN;

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
		private static final String SQL_LOAD_STPNODE = "SELECT vlanname,vlantype,vlanstatus," +
				"status,lastPollTime FROM vlan WHERE nodeid = ? AND vlanid = ? ";

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
		private static final int CHANGED_VLANNAME = 1 << 0;

		private static final int CHANGED_VLANTYPE = 1 << 1;

		private static final int CHANGED_VLANSTATUS = 1 << 2;

		private static final int CHANGED_STATUS = 1 << 3;

		private static final int CHANGED_POLLTIME = 1 << 4;

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
						"The VLAN record already exists in the database");

			// first extract the next node identifier
			//
			StringBuffer names = new StringBuffer(
					"INSERT INTO vlan (nodeid,vlanid");
			StringBuffer values = new StringBuffer("?,?");

			values.append(",?");
			names.append(",vlanname");

			values.append(",?");
			names.append(",vlantype");
		
			values.append(",?");
			names.append(",vlanstatus");

			values.append(",?");
			names.append(",status");

			values.append(",?");
			names.append(",lastpolltime");

			names.append(") VALUES (").append(values).append(')');

			LogUtils.debugf(this, "DbVlanEntry.insert: SQL insert statment = %s", names.toString());

			final DBUtils d = new DBUtils(getClass());
			
			try {
                PreparedStatement stmt = c.prepareStatement(names.toString());
                d.watch(stmt);

                int ndx = 1;
                stmt.setInt(ndx++, m_nodeId);
                stmt.setInt(ndx++, m_vlanId);
            	stmt.setString(ndx++, m_vlanname);
            	stmt.setInt(ndx++, m_vlantype);
            	stmt.setInt(ndx++, m_vlanstatus);
            	stmt.setString(ndx++, new String(new char[] { m_status }));
            	stmt.setTimestamp(ndx++, m_lastPollTime);

                // Run the insert
                //
                int rc = stmt.executeUpdate();
                LogUtils.debugf(this, "DbVlanEntry.insert: row %d", rc);
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
			StringBuffer sqlText = new StringBuffer("UPDATE vlan SET ");

			char comma = ' ';


			if ((m_changed & CHANGED_VLANNAME) == CHANGED_VLANNAME) {
				sqlText.append(comma).append("vlanname = ?");
				comma = ',';
			}

			if ((m_changed & CHANGED_VLANTYPE) == CHANGED_VLANTYPE) {
				sqlText.append(comma).append("vlantype = ?");
				comma = ',';
			}
			
			if ((m_changed & CHANGED_VLANSTATUS) == CHANGED_VLANSTATUS) {
				sqlText.append(comma).append("vlanstatus = ?");
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

			sqlText.append(" WHERE nodeid = ? AND vlanid = ? ");

			LogUtils.debugf(this, "DbVlanEntry.update: SQL insert statment = %s", sqlText.toString());

			final DBUtils d = new DBUtils(getClass());
			try {
                PreparedStatement stmt = c.prepareStatement(sqlText.toString());
                d.watch(stmt);

                int ndx = 1;

                if ((m_changed & CHANGED_VLANNAME) == CHANGED_VLANNAME)
                	stmt.setString(ndx++, m_vlanname);

                if ((m_changed & CHANGED_VLANTYPE) == CHANGED_VLANTYPE)
                	stmt.setInt(ndx++, m_vlantype);
                
                if ((m_changed & CHANGED_VLANSTATUS) == CHANGED_VLANSTATUS) 
                	stmt.setInt(ndx++, m_vlanstatus);

                if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
                	stmt.setString(ndx++, new String(new char[] { m_status }));

                if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
                	stmt.setTimestamp(ndx++, m_lastPollTime);
                }

                stmt.setInt(ndx++, m_nodeId);
                stmt.setInt(ndx++, m_vlanId);

                // Run the insert
                //
                int rc = stmt.executeUpdate();
                LogUtils.debugf(this, "DbVlanEntry.update: row %d", rc);
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
				throw new IllegalStateException(
						"The record does not exists in the database");

			final DBUtils d = new DBUtils(getClass());
            PreparedStatement stmt = null;
			
			try {
                stmt = c.prepareStatement(SQL_LOAD_STPNODE);
                d.watch(stmt);
                stmt.setInt(1, m_nodeId);
                stmt.setInt(2, m_vlanId);

                // Run the select
                //
                ResultSet rset = stmt.executeQuery();
                d.watch(rset);
                if (!rset.next()) {
                    LogUtils.debugf(this, "DbVlanEntry.load: no result found");
                	return false;
                }

                // extract the values.
                //
                int ndx = 1;

                // get the vlan name
                //
                m_vlanname = rset.getString(ndx++);
                
                // get the vlan type
                //
                m_vlantype = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_vlantype = VLAN_TYPE_UNKNOWN;

                // get the vlan status
                //
                m_vlanstatus = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_vlanstatus = -1;

                String str = rset.getString(ndx++);
                if (str != null && !rset.wasNull())
                	m_status = str.charAt(0);
                else
                	m_status = STATUS_UNKNOWN;

                m_lastPollTime = rset.getTimestamp(ndx++);

                rset.close();
                stmt.close();
			} finally {
			    d.cleanUp();
            }

			// clear the mask and mark as backed
			// by the database
			//
			LogUtils.debugf(this, "DbVlanEntry.load: result found");
			m_changed = 0;
			return true;
		}

        DbVlanEntry(int nodeId,int vlanid, boolean exists)
        {
                m_nodeId = nodeId;
                m_vlanId = vlanid;
                m_fromDb = exists;
				m_vlantype = -1;
				m_vlanstatus = -1;
                m_vlanname = "default";
        }

        static DbVlanEntry create(int nodeId,int vlanid) {
        	return new DbVlanEntry(nodeId,vlanid, false);
        }
		/**
		 * <p>get_nodeId</p>
		 *
		 * @return a int.
		 */
        protected int get_nodeId() {
			return m_nodeId;
		}

		/**
		 * <p>getVlanId</p>
		 *
		 * @return a int.
		 */
        protected int getVlanId() {
			return m_vlanId;
		}

		/**
		 * <p>getVlanName</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
        protected String getVlanName() {
			return m_vlanname;
		}

		protected void setVlanName(String vlanname) {
			m_vlanname = vlanname;
			m_changed |= CHANGED_VLANNAME;
		}

		protected boolean hasBaseVlanNameChanged() {
			if ((m_changed & CHANGED_VLANNAME) == CHANGED_VLANNAME)
				return true;
			else
				return false;
		}

		boolean updateVlanName(final String vlanname) {
		    if (m_vlanname == null || !m_vlanname.equals(vlanname)) {
		        setVlanName(vlanname);
		        return true;
		    } else {
		        return false;
		    }
		}

		/**
		 * <p>getVlanType</p>
		 *
		 * @return a int.
		 */
		protected int getVlanType() {
			return m_vlantype;
		}

		protected void setVlanType(int vlantype) {
			m_vlantype = vlantype;
			m_changed |= CHANGED_VLANTYPE;
		}

		protected boolean hasBaseTypeChanged() {
			if ((m_changed & CHANGED_VLANTYPE) == CHANGED_VLANTYPE)
				return true;
			else
				return false;
		}

		boolean updateVlanType(int vlantype) {
			if (vlantype != m_vlantype) {
				setVlanType(vlantype);
				return true;
			} else
				return false;
		}

		/**
		 * <p>getVlanStatus</p>
		 *
		 * @return a int.
		 */
		protected int getVlanStatus() {
			return m_vlanstatus;
		}

		protected void setVlanStatus(int vlanstatus) {
			m_vlanstatus = vlanstatus;
			m_changed |= CHANGED_VLANSTATUS;
		}

		protected boolean hasBaseStatusChanged() {
			if ((m_changed & CHANGED_VLANSTATUS) == CHANGED_VLANSTATUS)
				return true;
			else
				return false;
		}

		boolean updateVlanStatus(int vlanstatus) {
			if (vlanstatus != m_vlanstatus) {
				setVlanStatus(vlanstatus);
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
		static DbVlanEntry get(int nid, int basevlan) throws SQLException {
			Connection db = null;
			try {
				db = DataSourceFactory.getInstance().getConnection();
				return get(db, nid, basevlan);
			} finally {
				try {
					if (db != null)
						db.close();
				} catch (SQLException e) {
				    LogUtils.warnf(DbVlanEntry.class, e, "Exception closing JDBC connection");
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
		static DbVlanEntry get(Connection db, int nid, int basevlan)
				throws SQLException {
			DbVlanEntry entry = new DbVlanEntry(nid, basevlan,true);
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
		        .append("vlanId", m_vlanId)
		        .append("vlanName", m_vlanname)
		        .append("vlanType", m_vlantype)
		        .append("vlanStatus", m_vlanstatus)
		        .append("status", m_status)
		        .append("lastPollTime", m_lastPollTime)
		        .toString();
		}

}
