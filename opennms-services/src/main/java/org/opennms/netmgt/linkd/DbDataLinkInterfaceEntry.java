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

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public final class DbDataLinkInterfaceEntry
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
	 * The node identifier
	 */
	int     m_nodeId;

        /**
         * SNMP index of interface connected to the link on the node, 
         * is "-1" if it doesn't support SNMP 
         */
        int     m_ifindex;

        /**
         * Unique integer identifier for parent linking node
         */
        int     m_nodeparentid;
        
        /**
         * SNMP interface index on the parent node.
         */
        int     m_parentifindex;
		
		
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
		private static final String SQL_LOAD_DATALINKINTERFACE = "SELECT nodeparentid,parentIfIndex,status,lastpolltime FROM datalinkinterface WHERE nodeid = ? AND ifindex = ? ";

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
		private static final int CHANGED_PARENTNODEID = 1 << 0;

		private static final int CHANGED_PARENTIFINDEX = 1 << 1;

		private static final int CHANGED_STATUS = 1 << 2;

		private static final int CHANGED_POLLTIME = 1 << 3;

		/**
		 * Inserts the new row into the DataLinkInterface table
		 * of the OpenNMS database.
		 *
		 * @param c	The connection to the database.
		 *
		 * @throws java.sql.SQLException Thrown if an error occurs
		 * 	with the connection
		 */
		private void insert(Connection c) throws SQLException {
			if (m_fromDb)
				throw new IllegalStateException("The data link interface record already exists in the database");

			// first extract the next node identifier
			//
			String queryString = "INSERT INTO DataLinkInterface (nodeid,ifindex,nodeparentid,parentIfIndex,status,lastpolltime) VALUES (?,?,?,?,?,?)";
			
			// create the Prepared statment and then
			// start setting the result values
			//

			PreparedStatement stmt;
	        final DBUtils d = new DBUtils(getClass());
            try {
                stmt = c.prepareStatement(queryString);
                d.watch(stmt);
                queryString = null;

                int ndx = 1;
                
                stmt.setInt(ndx++, m_nodeId);

                stmt.setInt(ndx++, m_ifindex);

                stmt.setInt(ndx++, m_nodeparentid);

                stmt.setInt(ndx++, m_parentifindex);

                stmt.setString(ndx++, new String(new char[] { m_status }));

                stmt.setTimestamp(ndx++, m_lastPollTime);

                // Run the insert
                //
                //int rc = 
                stmt.executeUpdate();
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
		 * Updates an existing record in the OpenNMS DataLinkInterface table.
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
			StringBuffer sqlText = new StringBuffer("UPDATE DataLinkInterface SET ");

			char comma = ' ';

			if ((m_changed & CHANGED_PARENTNODEID) == CHANGED_PARENTNODEID) {
				sqlText.append(comma).append("nodeparentid = ?");
				comma = ',';
			}

			if ((m_changed & CHANGED_PARENTIFINDEX) == CHANGED_PARENTIFINDEX) {
				sqlText.append(comma).append("parentIfIndex = ?");
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

			sqlText.append(" WHERE nodeid = ? AND ifindex = ? ");

			// create the Prepared statment and then
			// start setting the result values
			//
			PreparedStatement stmt;
	        final DBUtils d = new DBUtils(getClass());
            try {
                stmt = c.prepareStatement(sqlText.toString());
                d.watch(stmt);

                int ndx = 1;

                if ((m_changed & CHANGED_PARENTNODEID) == CHANGED_PARENTNODEID)
                	stmt.setInt(ndx++, m_nodeparentid);

                if ((m_changed & CHANGED_PARENTIFINDEX) == CHANGED_PARENTIFINDEX)
                	stmt.setInt(ndx++, m_parentifindex);

                if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS)
                	stmt.setString(ndx++, new String(new char[] { m_status }));

                if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
                	stmt.setTimestamp(ndx++, m_lastPollTime);
                }

                stmt.setInt(ndx++, m_nodeId);
                stmt.setInt(ndx++, m_ifindex);

                // Run the insert
                //
                //int rc = 
                stmt.executeUpdate();
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
		 * and ifindex must be set prior to this call.
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

			PreparedStatement stmt = null;
            ResultSet rset;
            final DBUtils d = new DBUtils(getClass());
            try {
                stmt = c.prepareStatement(SQL_LOAD_DATALINKINTERFACE);
                d.watch(stmt);
                stmt.setInt(1, m_nodeId);
                stmt.setInt(2, m_ifindex);

                rset = stmt.executeQuery();
                d.watch(rset);
                if (!rset.next()) {
                    LogUtils.debugf(this, "DataLinkInterfaceEntry.load: no result found");
                	return false;
                }

                // extract the values.
                //
                int ndx = 1;

                // get the mac address
                //
                m_nodeparentid = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_nodeparentid = -1;

                // get the source node ifindex
                //
                m_parentifindex = rset.getInt(ndx++);
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
            LogUtils.debugf(this, "DataInterfaceEntry.load: result found");
			m_changed = 0;
			return true;
		}

        DbDataLinkInterfaceEntry(int nodeId, int ifindex, boolean exists)
        {
                m_nodeId = nodeId;
				m_ifindex = ifindex;
                m_fromDb = exists;
                m_nodeparentid = -1;
				m_parentifindex = -1;
        }

        DbDataLinkInterfaceEntry(int nodeId, boolean exists)
        {
                m_nodeId = nodeId;
				m_ifindex = -1;
                m_fromDb = exists;
                m_nodeparentid = -1;
				m_parentifindex = -1;
        }

    	static DbDataLinkInterfaceEntry create(int nodeid, int ifindex) {
    		return new DbDataLinkInterfaceEntry(nodeid, ifindex, false);
    	}

    	static DbDataLinkInterfaceEntry create(int nodeid) {
    		return new DbDataLinkInterfaceEntry(nodeid,false);
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
		 * <p>get_ifindex</p>
		 *
		 * @return a int.
		 */
		public int get_ifindex() {
			return m_ifindex;
		}

		/**
		 * <p>get_nodeparentid</p>
		 *
		 * @return a int.
		 */
		public int get_nodeparentid() {
			return m_nodeparentid;
		}

		void set_nodeparentid(int nodeparentid) {
			m_nodeparentid = nodeparentid;
			m_changed |= CHANGED_PARENTNODEID;
		}

		boolean hasNodeParentIdChanged() {
			if ((m_changed & CHANGED_PARENTNODEID) == CHANGED_PARENTNODEID)
				return true;
			else
				return false;
		}

		boolean updateNodeParentId(int nodeparentid) {
			if (nodeparentid != m_nodeparentid) {
				set_nodeparentid(nodeparentid);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_parentifindex</p>
		 *
		 * @return a int.
		 */
		public int get_parentifindex() {
			return m_parentifindex;
		}

		void set_parentifindex(int parentifindex) {
			m_parentifindex = parentifindex;
			m_changed |= CHANGED_PARENTIFINDEX;
		}

		boolean hasParentIfIndexChanged() {
			if ((m_changed & CHANGED_PARENTIFINDEX) == CHANGED_PARENTIFINDEX)
				return true;
			else
				return false;
		}

		boolean updateParentIfIndex(int parentifindex) {
			if (parentifindex != m_parentifindex) {
				set_parentifindex(parentifindex);
				return true;
			} else
				return false;
		}

		/**
		 * <p>get_status</p>
		 *
		 * @return a char.
		 */
		public char get_status() {
			return m_status;
		}
		
		void set_status(char status) {
			if (status == STATUS_ACTIVE || status == STATUS_NOT_POLLED
					|| status == STATUS_DELETED)
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
		 * @param time	The last poll time.
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
		 * @param time	The last poll time.
		 *
		 */
		void set_lastpolltime(Date time) {
			m_lastPollTime = new Timestamp(time.getTime());
			m_changed |= CHANGED_POLLTIME;
		}

		/**
		 * Sets the last poll time.
		 *
		 * @param time	The last poll time.
		 *
		 */
		void set_lastpolltime(Timestamp time) {
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
		 * key fields of <em>nodeID</em> and <em>ifindex</em>. If the
		 * record cannot be found then a null reference is returnd.
		 *
		 * @param nid	The node id key
		 * @param ifindex The interface snmp index
		 *
		 * @return The loaded entry or null if one could not be found.
		 *
		 */
		static DbDataLinkInterfaceEntry get(int nid, int ifindex) throws SQLException {
			Connection db = null;
			try {
				db = DataSourceFactory.getInstance().getConnection();
				return get(db, nid, ifindex);
			} finally {
				try {
					if (db != null)
						db.close();
				} catch (SQLException e) {
                    LogUtils.warnf(DbDataLinkInterfaceEntry.class, e, "Exception closing JDBC connection");
				}
			}
		}

		/**
		 * Retreives a current record from the database based upon the
		 * key fields of <em>nodeID</em> and <em>ifindex</em>. If the
		 * record cannot be found then a null reference is returnd.
		 *
		 * @param db	The databse connection used to load the entry.
		 * @param nid	The node id key
		 * @param ifindex The interface snmp index
		 *
		 * @return The loaded entry or null if one could not be found.
		 *
		 */
		static DbDataLinkInterfaceEntry get(Connection db, int nid, int ifindex)
				throws SQLException {
			DbDataLinkInterfaceEntry entry = new DbDataLinkInterfaceEntry(nid, ifindex,true);
			if (!entry.load(db))
				entry = null;
			return entry;
		}
		
		/**
		 * Retreives a current record from the database based upon the
		 * key field of <em>nodeID</em>. If the
		 * record cannot be found then a null reference is returnd.
		 *
		 * @param nid	The node id key
		 *
		 * @return The loaded entry or null if one could not be found.
		 *
		 */
		static DbDataLinkInterfaceEntry get(int nid) throws SQLException {
			Connection db = null;
			try {
				db = DataSourceFactory.getInstance().getConnection();
				return get(db, nid);
			} finally {
				try {
					if (db != null)
						db.close();
				} catch (SQLException e) {
                    LogUtils.warnf(DbDataLinkInterfaceEntry.class, e, "Exception closing JDBC connection");
				}
			}
		}

		/**
		 * Retreives a current record from the database based upon the
		 * key fields of <em>nodeID</em>. If the
		 * record cannot be found then a null reference is returnd.
		 *
		 * @param db	The databse connection used to load the entry.
		 * @param nid	The node id key
		 *
		 * @return The loaded entry or null if one could not be found.
		 *
		 */
		static DbDataLinkInterfaceEntry get(Connection db, int nid)
				throws SQLException {
			DbDataLinkInterfaceEntry entry = new DbDataLinkInterfaceEntry(nid,true);
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
			String sep = System.getProperty("line.separator");
			StringBuffer buf = new StringBuffer();

			buf.append("from db = ").append(m_fromDb).append(sep);
			buf.append("node id = ").append(m_nodeId).append(sep);
			buf.append("ifindex = ").append(m_ifindex).append(sep);
			buf.append("node parent id = ").append(m_nodeparentid).append(sep);
			buf.append("parent interface ifindex = ").append(m_parentifindex).append(sep);
			buf.append("status = ").append(m_status).append(sep);
			buf.append("last poll time = ").append(m_lastPollTime).append(sep);
			return buf.toString();

		}

}
