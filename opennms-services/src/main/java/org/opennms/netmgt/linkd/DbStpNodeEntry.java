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
public class DbStpNodeEntry
{
	/**
	 * The character returned if the entry is active
	 */

	static final char STATUS_ACTIVE = 'A';

	/**
	 * The character returned if the entry is not active
	 * means last polled
	 */

	static final char STATUS_NOT_POLLED = 'N';

	/**
	 * It stats that node is deleted
	 * The character returned if the node is deleted
	 */
	static final char STATUS_DELETE = 'D';

	/**
	 * The character returned if the entry type is unset/unknown.
	 */

	static final char STATUS_UNKNOWN = 'K';

	/**
	 * the bridge type
	 */

	static final int BASE_TYPE_UNKNOWN = 1;
	static final int BASE_TYPE_TRASPARENT_ONLY = 2;
	static final int BASE_TYPE_SOURCEROUTE_ONLY = 3;
	static final int BASE_TYPE_SRT = 4;

	/**
	 * the STP Protocol Specification
	 */

	static final int STP_UNKNOWN = 1;
	static final int STP_DECLB100 = 2;
	static final int STP_IEEE8011D = 3;

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
						"The record already exists in the database");

			ThreadCategory log = ThreadCategory.getInstance(getClass());

			// first extract the next node identifier
			//
			StringBuffer names = new StringBuffer(
					"INSERT INTO StpNode (nodeid,basevlan");
			StringBuffer values = new StringBuffer("?,?");

			if ((m_changed & CHANGED_BRIDGEADDR) == CHANGED_BRIDGEADDR) {
				values.append(",?");
				names.append(",baseBridgeAddress");
			}

			if ((m_changed & CHANGED_NUMPORTS) == CHANGED_NUMPORTS) {
				values.append(",?");
				names.append(",baseNumPorts");
			}

			if ((m_changed & CHANGED_BASETYPE) == CHANGED_BASETYPE) {
				values.append(",?");
				names.append(",basetype");
			}
			
			if ((m_changed & CHANGED_STPPROTSPEC) == CHANGED_STPPROTSPEC) {
				values.append(",?");
				names.append(",stpProtocolSpecification");
			}

			if ((m_changed & CHANGED_STPPRIORITY) == CHANGED_STPPRIORITY) {
				values.append(",?");
				names.append(",stpPriority");
			}

			if ((m_changed & CHANGED_STPDESROOT) == CHANGED_STPDESROOT) {
				values.append(",?");
				names.append(",stpdesignatedroot");
			}

			if ((m_changed & CHANGED_STPROOTCOST) == CHANGED_STPROOTCOST) {
				values.append(",?");
				names.append(",stprootcost");
			}

			if ((m_changed & CHANGED_STPROOTPORT) == CHANGED_STPROOTPORT) {
				values.append(",?");
				names.append(",stprootport");
			}

			if ((m_changed & CHANGED_VLANNAME) == CHANGED_VLANNAME) {
				values.append(",?");
				names.append(",basevlanname");
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
			log.debug("DbStpNodeEntry.insert: SQL insert statment = " + names.toString());

			final DBUtils d = new DBUtils(getClass());
			
			try {
                PreparedStatement stmt = c.prepareStatement(names.toString());
                d.watch(stmt);

                int ndx = 1;
                stmt.setInt(ndx++, m_nodeId);
                stmt.setInt(ndx++, m_basevlan);

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

                // Run the insert
                //
                int rc = stmt.executeUpdate();
                if (log.isDebugEnabled())
                	log.debug("StpNodeEntry.insert: row " + rc);
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

			ThreadCategory log = ThreadCategory.getInstance(getClass());

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

			if (log.isDebugEnabled())
				log.debug("DbStpNodeEntry.update: SQL insert statment = " + sqlText.toString());

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
                if (log.isDebugEnabled())
                	log.debug("StpNodeEntry.update: row " + rc);
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
				throw new IllegalStateException("The record does not exists in the database");

			ThreadCategory log = ThreadCategory.getInstance(getClass());

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
                	if (log.isDebugEnabled())
                		log.debug("StpNodeEntry.load: no result found");
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
                	m_basetype = -1;

                m_stpprotocolspecification = rset.getInt(ndx++);
                if (rset.wasNull())
                	m_stpprotocolspecification = -1;
                
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
			if (log.isDebugEnabled())
				log.debug("StpNodeEntry.load: result found");
			m_changed = 0;
			return true;
		}

		/**
		 * Default constructor. 
		 *
		 */
		DbStpNodeEntry() {
			throw new UnsupportedOperationException(
					"Default constructor not supported!");
		}

        DbStpNodeEntry(int nodeId,int basevlan, boolean exists)
        {
                m_nodeId = nodeId;
                m_fromDb = exists;
                m_basenumports = -1;
				m_basetype = -1;
				m_stpprotocolspecification = -1;
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
		 * @return
		 */
		public int get_nodeId() {
			return m_nodeId;
		}

		/**
		 * @return
		 */
		public int get_basevlan() {
			return m_basevlan;
		}

		/**
		 * @return
		 */
		public String get_basevlanname() {
			return m_basevlanname;
		}

		void set_basevlanname(String basevlanname) {
			m_basevlanname = basevlanname;
			m_changed |= CHANGED_VLANNAME;
		}

		boolean hasBaseVlanNameChanged() {
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
		 * @return
		 */
		public String get_basebridgeaddress() {
			return m_basebridgeaddress;
		}

		void set_basebridgeaddress(String basebridgeaddress) {
			m_basebridgeaddress = basebridgeaddress;
			m_changed |= CHANGED_BRIDGEADDR;
		}

		boolean hasBaseBridgeAddressChanged() {
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
		 * @return
		 */
		public int get_basenumports() {
			return m_basenumports;
		}

		void set_basenumports(int basenumports) {
			m_basenumports = basenumports;
			m_changed |= CHANGED_NUMPORTS;
		}

		boolean hasBaseNumPortsChanged() {
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
		 * @return
		 */
		public int get_basetype() {
			return m_basetype;
		}

		void set_basetype(int basetype) {
			if (basetype == BASE_TYPE_SRT || basetype == BASE_TYPE_TRASPARENT_ONLY || basetype == BASE_TYPE_SOURCEROUTE_ONLY)
			m_basetype = basetype;
			else 
				m_basetype = BASE_TYPE_UNKNOWN;
			m_changed |= CHANGED_BASETYPE;
		}

		boolean hasBaseTypeChanged() {
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
		 * @return
		 */
		public String get_stpdesignatedroot() {
			return m_stpdesignatedroot;
		}

		void set_stpdesignatedroot(String stpdesignatedroot) {
			m_stpdesignatedroot = stpdesignatedroot;
			m_changed |= CHANGED_STPDESROOT;
		}

		boolean hasStpDesignatedRootChanged() {
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
		 * @return
		 */
		public int get_stppriority() {
			return m_stppriority;
		}

		void set_stppriority(int stppriority) {
			m_stppriority = stppriority;
			m_changed |= CHANGED_STPPRIORITY;
		}

		boolean hasStpPriorityChanged() {
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
		 * @return
		 */
		public int get_stpprotocolspecification() {
			return m_stpprotocolspecification;
		}

		void set_stpprotocolspecification(int stpprotocolspecification) {
			if (stpprotocolspecification == STP_DECLB100|| stpprotocolspecification == STP_IEEE8011D)
			m_stpprotocolspecification = stpprotocolspecification;
			else 
				m_stpprotocolspecification = STP_UNKNOWN;
			m_changed |= CHANGED_STPPROTSPEC;
		}

		boolean hasStpProtocolSpecificationChanged() {
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
		 * @return
		 */
		public int get_stprootcost() {
			return m_stprootcost;
		}

		void set_stprootcost(int stprootcost) {
			m_stprootcost = stprootcost;
			m_changed |= CHANGED_STPROOTCOST;
		}

		boolean hasStpRootCostChanged() {
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
		 * @return
		 */
		public int get_stprootport() {
			return m_stprootport;
		}
		
		void set_stprootport(int stprootport) {
			m_stprootport = stprootport;
			m_changed |= CHANGED_STPROOTPORT;
		}

		boolean hasStpRootPortChanged() {
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
					ThreadCategory.getInstance(DbStpNodeEntry.class).warn(
							"Exception closing JDBC connection", e);
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

		public String toString() {
			String sep = System.getProperty("line.separator");
			StringBuffer buf = new StringBuffer();

			buf.append("from db = ").append(m_fromDb).append(sep);
			buf.append("node id = ").append(m_nodeId).append(sep);
			buf.append("base vlan index = ").append(m_basevlan).append(sep);
			buf.append("base bridge address = ").append(m_basebridgeaddress).append(sep);
			buf.append("base number of ports = ").append(m_basenumports).append(sep);
			buf.append("base bridge type id = ").append(m_basetype).append(sep);
			buf.append("stp protocol specification id = ").append(m_stpprotocolspecification).append(sep);
			buf.append("stp bridge priority = ").append(m_stppriority).append(sep);
			buf.append("stp designated root = ").append(m_stpdesignatedroot).append(sep);
			buf.append("stp root cost = ").append(m_stprootcost).append(sep);
			buf.append("stp root port = ").append(m_stprootport).append(sep);
			buf.append("base vlan name = ").append(m_basevlanname).append(sep);
			buf.append("status = ").append(m_status).append(sep);
			buf.append("last poll time = ").append(m_lastPollTime).append(sep);
			return buf.toString();

		}

}
