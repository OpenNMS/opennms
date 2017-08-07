/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.mock;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;

/**
 *
 * <p>
 * Once loaded or create, the class tracks any changes and will write those
 * changes to the database whenever the <code>store</code> method is invoked.
 * If a database connection is not passed to the store method, then a temporary
 * one is allocated to write the results.
 * </p>
 *
 * <p>
 * NOTE: if the connection is passed in and is not in auto commit mode, then the
 * caller must call <code>commit</code> to inform the database that the
 * transaction is complete.
 * 
 * @deprecated Objects like this that control their own data access are deprecated in favor
 * of the JAXB beans (opennms-model) and DAO objects (opennms-dao).
 *
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 */
public final class DbIpInterfaceEntry {

    /** Constant <code>SNMP_UNKNOWN=' '</code> */
    private final static char SNMP_UNKNOWN = ' ';

    /** Constant <code>STATE_UNKNOWN=' '</code> */
    private final static char STATE_UNKNOWN = ' ';

    /**
     * The SQL statement used to read a node from the database. This record is
     * keyed by the node identifier and distributed poller name.
     */
    private static final String SQL_LOAD_REC = "SELECT ifIndex, ipHostname, isManaged, ipStatus, ipLastCapsdPoll, isSnmpPrimary FROM ipInterface WHERE nodeID = ? AND ipAddr = ? AND isManaged != 'D'";

    /**
     * This is the SQL statement used to load a record when the ifIndex is
     * involved as part of the key lookup. This is mainly used when a node has
     * multiple <em>unnamed</em> interfaces (i.e. 0.0.0.0) but each has a
     * different ifIndex.
     */
    private static final String SQL_LOAD_REC_IFINDEX = "SELECT ifIndex, ipHostname, isManaged, ipStatus, ipLastCapsdPoll, isSnmpPrimary FROM ipInterface WHERE nodeID = ? AND ipAddr = ? AND ifIndex = ? AND isManaged != 'D'";

    /**
     * True if this recored was loaded from the database. False if it's new.
     */
    private boolean m_fromDb;

    /**
     * The node identifier
     */
    private long m_nodeId;

    /**
     * The IP address.
     */
    private InetAddress m_ipAddr;

    /**
     * The SNMP ifIndex
     */
    private int m_ifIndex;

    /**
     * The hostname string, if any
     */
    private String m_hostname;

    /**
     * The status of the interface
     */
    private int m_status;

    /**
     * The managed status, if any
     */
    private char m_managedState;

    /**
     * The last time the interface was checked.
     */
    private Timestamp m_lastPoll;

    /**
     * The SNMP primary status.
     */
    private char m_primaryState;

    /**
     * The bit map used to determine which elements have changed since the
     * record was created.
     */
    private int m_changed;

    // Indicates that the ifIndex is to be used as database key for this entry
    private boolean m_useIfIndexAsKey;

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
        if (!m_fromDb) {
            throw new IllegalStateException("The record does not exists in the database");
        }

        // create the Prepared statement and then start setting the query values
        PreparedStatement stmt = null;
        ResultSet rset = null;
        final DBUtils d = new DBUtils(getClass());
  
        try {
            if (m_useIfIndexAsKey) {
                stmt = c.prepareStatement(SQL_LOAD_REC_IFINDEX);
                d.watch(stmt);
                stmt.setLong(1, m_nodeId);
                stmt.setString(2, InetAddressUtils.str(m_ipAddr));
                stmt.setInt(3, m_ifIndex);
            } else {
                stmt = c.prepareStatement(SQL_LOAD_REC);
                d.watch(stmt);
                stmt.setLong(1, m_nodeId);
                stmt.setString(2, InetAddressUtils.str(m_ipAddr));
            }

            // Execute the query
            rset = stmt.executeQuery();
            d.watch(rset);
            if (!rset.next()) {
                return false;
            }

            // extract the values
            int ndx = 1;

            // get the ifIndex
            m_ifIndex = rset.getInt(ndx++);
            if (rset.wasNull()) {
                m_ifIndex = -1;
            }

            // get the host name
            m_hostname = rset.getString(ndx++);
            if (rset.wasNull()) {
                m_hostname = null;
            }

            // get the managed status
            String str = rset.getString(ndx++);
            if (str != null && rset.wasNull() == false) {
                m_managedState = str.charAt(0);
            } else {
                m_managedState = STATE_UNKNOWN;
            }

            // get the status
            m_status = rset.getInt(ndx++);
            if (rset.wasNull()) {
                m_status = -1;
            }

            // get the time
            m_lastPoll = rset.getTimestamp(ndx++);

            // get the SNMP primary state
            str = rset.getString(ndx++);
            if (str != null && rset.wasNull() == false) {
                m_primaryState = str.charAt(0);
            } else {
                m_primaryState = STATE_UNKNOWN;
            }
        } finally {
            d.cleanUp();
        }

        // clear the mask and mark as backed by the database
        m_changed = 0;
        return true;
    }

    /**
     * Constructs a new interface.
     * 
     * @param nid
     *            The node identifier.
     * @param address
     *            The target interface address.
     * @param exists
     *            True if the interface already exists.
     * 
     */
    private DbIpInterfaceEntry(long nid, InetAddress address, boolean exists) {
        m_fromDb = exists;
        m_nodeId = nid;
        m_ipAddr = address;
        m_ifIndex = -1;
        m_managedState = STATE_UNKNOWN;
        m_status = -1;
        m_lastPoll = null;
        m_primaryState = SNMP_UNKNOWN;
        m_changed = 0;
        m_useIfIndexAsKey = false;
    }

    /**
     * Constructs a new interface, this constructor will only work for entries
     * loaded from the database!
     * 
     * @param nid
     *            The node identifier.
     * @param address
     *            The target interface address.
     * @param ifIndex
     *            The target ifIndex of the node/address pair
     * @param exists
     *            True if the interface already exists.
     * 
     */
    private DbIpInterfaceEntry(long nid, InetAddress address, int ifIndex, boolean exists) {
        m_fromDb = exists;
        m_nodeId = nid;
        m_ipAddr = address;
        m_ifIndex = ifIndex;
        m_status = -1;
        m_lastPoll = null;
        m_managedState = STATE_UNKNOWN;
        m_primaryState = SNMP_UNKNOWN;
        m_changed = 0;
        m_useIfIndexAsKey = true;
    }

    /**
     * Returns the current ifIndex
     */
    public int getIfIndex() {
        return m_ifIndex;
    }

    /**
     * Retrieves a current record from the database based upon the key fields of
     * <em>nodeID</em> and <em>ipAddr</em>. If the record cannot be found
     * then a null reference is returnd.
     * 
     * @param db
     *            The databse connection used to load the entry.
     * @param nid
     *            The node id key
     * @param addr
     *            The internet address.
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbIpInterfaceEntry get(Connection db, long nid, InetAddress addr)
    throws SQLException {
        DbIpInterfaceEntry entry = new DbIpInterfaceEntry(nid, addr, true);
        if (!entry.load(db)) {
            entry = null;
        }
        return entry;
    }

    /**
     * Retrieves a current record from the database based upon the key fields of
     * <em>nodeID</em> and <em>ipAddr</em>. If the record cannot be found
     * then a null reference is returned.
     * 
     * @param db
     *            The database connection used to load the entry.
     * @param nid
     *            The node id key
     * @param addr
     *            The IP address.
     * @param ifIndex
     *            The interface index.
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbIpInterfaceEntry get(Connection db, long nid, InetAddress addr, int ifIndex) throws SQLException {
        DbIpInterfaceEntry entry = new DbIpInterfaceEntry(nid, addr, ifIndex, true);
        if (!entry.load(db)) {
            entry = null;
        }
        return entry;
    }

    /**
     * Creates a string that displays the internal contents of the record. This
     * is mainly just used for debug output since the format is ad-hoc.
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        String sep = System.getProperty("line.separator");
        final StringBuilder buf = new StringBuilder();

        buf.append("from database      = ").append(m_fromDb).append(sep);
        buf.append("node identifier    = ").append(m_nodeId).append(sep);
        buf.append("IP Address         = ").append(InetAddressUtils.str(m_ipAddr)).append(sep);
        buf.append("interface index    = ").append(m_ifIndex).append(sep);
        buf.append("last poll time     = ").append(m_lastPoll).append(sep);
        buf.append("hostname           = ").append(m_hostname).append(sep);
        buf.append("status             = ").append(m_status).append(sep);
        buf.append("isManaged          = ").append(m_managedState).append(sep);
        buf.append("isSnmpPrimary      = ").append(m_primaryState).append(sep);
        buf.append("field change map   = 0x").append(Integer.toHexString(m_changed)).append(sep);
        return buf.toString();
    }
}
