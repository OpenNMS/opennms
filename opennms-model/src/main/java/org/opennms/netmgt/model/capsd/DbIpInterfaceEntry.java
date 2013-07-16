/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.capsd;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger LOG = LoggerFactory.getLogger(DbIpInterfaceEntry.class);

    /** Constant <code>SNMP_PRIMARY='P'</code> */
    public final static char SNMP_PRIMARY = 'P';

    /** Constant <code>SNMP_SECONDARY='S'</code> */
    public final static char SNMP_SECONDARY = 'S';

    /** Constant <code>SNMP_NOT_ELIGIBLE='N'</code> */
    public final static char SNMP_NOT_ELIGIBLE = 'N';

    /** Constant <code>SNMP_UNKNOWN=' '</code> */
    public final static char SNMP_UNKNOWN = ' ';

    /** Constant <code>STATE_MANAGED='M'</code> */
    public final static char STATE_MANAGED = 'M';

    /** Constant <code>STATE_UNMANAGED='U'</code> */
    public final static char STATE_UNMANAGED = 'U';

    /** Constant <code>STATE_ALIAS='A'</code> */
    public final static char STATE_ALIAS = 'A';

    /** Constant <code>STATE_DELETED='D'</code> */
    public final static char STATE_DELETED = 'D';

    /** Constant <code>STATE_FORCED='F'</code> */
    public final static char STATE_FORCED = 'F';

    /** Constant <code>STATE_NOT_POLLED='N'</code> */
    public final static char STATE_NOT_POLLED = 'N';
    
    /** Constant <code>STATE_REMOTE='X'</code> */
    public final static char STATE_REMOTE = 'X';

    /** Constant <code>STATE_UNKNOWN=' '</code> */
    public final static char STATE_UNKNOWN = ' ';

    /**
     * The SQL statement used to read a node from the database. This record is
     * keyed by the node identifier and distributed poller name.
     */
    private static final String SQL_LOAD_REC = "SELECT ifIndex, ipHostname, isManaged, ipStatus, ipLastCapsdPoll, isSnmpPrimary FROM ipInterface WHERE nodeID = ? AND ipAddr = ? AND isManaged != 'D'";

    /**
     * This is the SQL statement used to load the list of service identifiers
     * associated with this interface. Once the identifiers are known then a
     * list of DbIfServiceEntry(s) can be returned to the caller.
     */
    private static final String SQL_LOAD_IFSVC_LIST = "SELECT serviceID FROM ifServices WHERE nodeID = ? AND ipAddr = ? AND status != 'D'";

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
    private int m_originalIfIndex;

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

    // Mask fields
    //
    private static final int CHANGED_IFINDEX = 1 << 0;

    private static final int CHANGED_HOSTNAME = 1 << 1;

    private static final int CHANGED_MANAGED = 1 << 2;

    private static final int CHANGED_STATUS = 1 << 3;

    private static final int CHANGED_POLLTIME = 1 << 4;

    private static final int CHANGED_PRIMARY = 1 << 5;

    // Indicates that the ifIndex is to be used as database key for this entry
    private boolean m_useIfIndexAsKey;

    /**
     * Inserts the new interface into the ipInterface table of the OpenNMS
     * databasee.
     * 
     * @param c
     *            The connection to the database.
     * 
     * @throws java.sql.SQLException
     *             Thrown if an error occurs with the connection
     */
    private void insert(Connection c) throws SQLException {
        if (m_fromDb) {
            throw new IllegalStateException("The record already exists in the database");
        }

        // first extract the next node identifier
        StringBuffer names = new StringBuffer("INSERT INTO ipInterface (nodeID,ipAddr");
        StringBuffer values = new StringBuffer("?,?");

        if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
            values.append(",?");
            names.append(",ifIndex");
        }

        if ((m_changed & CHANGED_HOSTNAME) == CHANGED_HOSTNAME) {
            values.append(",?");
            names.append(",ipHostname");
        }

        if ((m_changed & CHANGED_MANAGED) == CHANGED_MANAGED) {
            values.append(",?");
            names.append(",isManaged");
        }

        if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS) {
            values.append(",?");
            names.append(",ipStatus");
        }

        if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
            values.append(",?");
            names.append(",ipLastCapsdPoll");
        }

        if ((m_changed & CHANGED_PRIMARY) == CHANGED_PRIMARY) {
            values.append(",?");
            names.append(",isSnmpPrimary");
        }

        names.append(") VALUES (").append(values).append(')');
            LOG.debug("DbIpInterfaceEntry.insert: SQL insert statement for interface [{},{}] = {}", m_nodeId, m_ipAddr, names.toString());

        // create the Prepared statement and then start setting the result values
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            stmt = c.prepareStatement(names.toString());
            d.watch(stmt);
            names = null;
            int ndx = 1;
            stmt.setLong(ndx++, m_nodeId);
            stmt.setString(ndx++, InetAddressUtils.str(m_ipAddr));
            if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
                stmt.setInt(ndx++, m_ifIndex);
            }
            if ((m_changed & CHANGED_HOSTNAME) == CHANGED_HOSTNAME) {
                stmt.setString(ndx++, m_hostname);
            }
            if ((m_changed & CHANGED_MANAGED) == CHANGED_MANAGED) {
                stmt.setString(ndx++,
                               new String(new char[] { m_managedState }));
            }
            if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS) {
                stmt.setInt(ndx++, m_status);
            }
            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
                stmt.setTimestamp(ndx++, m_lastPoll);
            }
            if ((m_changed & CHANGED_PRIMARY) == CHANGED_PRIMARY) {
                stmt.setString(ndx++,
                               new String(new char[] { m_primaryState }));
            }
            // Run the insert
            int rc = stmt.executeUpdate();
            LOG.debug("DbIpInterfaceEntry.insert: SQL update result = {}", rc);
        } finally {
            d.cleanUp();
        }

        // clear the mask and mark as backed by the database
        m_fromDb = true;
        m_changed = 0;
    }
    
    /**
     * Updates an existing record in the OpenNMS ipInterface table.
     * 
     * @param c
     *            The connection used for the update.
     * 
     * @throws java.sql.SQLException
     *             Thrown if an error occurs with the connection
     */
    private void update(Connection c) throws SQLException {
        if (!m_fromDb) {
            throw new IllegalStateException("The record does not exists in the database");
        }

        // first extract the next node identifier
        StringBuffer sqlText = new StringBuffer("UPDATE ipInterface SET ");

        char comma = ' ';
        if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
            sqlText.append(comma).append("ifIndex = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_HOSTNAME) == CHANGED_HOSTNAME) {
            sqlText.append(comma).append("ipHostname = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_MANAGED) == CHANGED_MANAGED) {
            sqlText.append(comma).append("isManaged = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS) {
            sqlText.append(comma).append("ipStatus = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
            sqlText.append(comma).append("ipLastCapsdPoll = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_PRIMARY) == CHANGED_PRIMARY) {
            sqlText.append(comma).append("isSnmpPrimary = ?");
            comma = ',';
        }

        if (m_useIfIndexAsKey) {
            sqlText.append(" WHERE nodeID = ? AND ipAddr = ? AND ifIndex = ?");
        } else {
            sqlText.append(" WHERE nodeID = ? AND ipAddr = ?");
        }

        if ((m_changed & CHANGED_PRIMARY) == CHANGED_PRIMARY && m_primaryState == 'N') {
            sqlText.append(" AND isSnmpPrimary != 'C'");
        }

        sqlText.append(" AND isManaged <> 'D'");

        LOG.debug("DbIpInterfaceEntry.update: SQL update statment = {}", sqlText.toString());

        // create the Prepared statement and then start setting the result values
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            stmt = c.prepareStatement(sqlText.toString());
            d.watch(stmt);
            sqlText = null;
            int ndx = 1;
            if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
                if (m_ifIndex == -1) {
                    stmt.setNull(ndx++, Types.INTEGER);
                } else {
                    stmt.setInt(ndx++, m_ifIndex);
                }
            }
            if ((m_changed & CHANGED_HOSTNAME) == CHANGED_HOSTNAME) {
                if (m_hostname != null) {
                    stmt.setString(ndx++, m_hostname);
                } else {
                    stmt.setNull(ndx++, Types.VARCHAR);
                }
            }
            if ((m_changed & CHANGED_MANAGED) == CHANGED_MANAGED) {
                if (m_managedState == STATE_UNKNOWN) {
                    stmt.setString(ndx++, "N");
                } else {
                    stmt.setString(ndx++,
                                   new String(new char[] { m_managedState }));
                }
            }
            if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS) {
                if (m_status == -1) {
                    stmt.setNull(ndx++, Types.INTEGER);
                } else {
                    stmt.setInt(ndx++, m_status);
                }
            }
            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
                if (m_lastPoll != null) {
                    stmt.setTimestamp(ndx++, m_lastPoll);
                } else {
                    stmt.setNull(ndx++, Types.TIMESTAMP);
                }
            }
            if ((m_changed & CHANGED_PRIMARY) == CHANGED_PRIMARY) {
                if (m_primaryState == SNMP_UNKNOWN) {
                    stmt.setNull(ndx++, Types.CHAR);
                } else {
                    stmt.setString(ndx++,
                                   new String(new char[] { m_primaryState }));
                }
            }
            stmt.setLong(ndx++, m_nodeId);
            stmt.setString(ndx++, InetAddressUtils.str(m_ipAddr));
            if (m_useIfIndexAsKey) {
                if (m_ifIndex == -1) {
                    stmt.setNull(ndx++, Types.INTEGER);
                } else {
                    stmt.setInt(ndx++, m_originalIfIndex);
                }
            }
            // Run the insert
            int rc = stmt.executeUpdate();
            LOG.debug("DbIpInterfaceEntry.update: update result = {}", rc);
        } finally {
            d.cleanUp();
        }

        // clear the mask and mark as backed by the database
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
     * Default constructor.
     * 
     */
    private DbIpInterfaceEntry() {
        throw new UnsupportedOperationException("Default constructor not supported!");
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
        m_originalIfIndex = -1;
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
        m_originalIfIndex = ifIndex;
        m_status = -1;
        m_lastPoll = null;
        m_managedState = STATE_UNKNOWN;
        m_primaryState = SNMP_UNKNOWN;
        m_changed = 0;
        m_useIfIndexAsKey = true;
    }

    /**
     * Returns the node entry's unique identifier. This is a non-mutable
     * element. If the record does not yet exist in the database then a -1 is
     * returned.
     * 
     */
    public long getNodeId() {
        return m_nodeId;
    }

    /**
     * Returns the name of the distributed poller for the entry. This is a
     * non-mutable element of the record.
     * 
     */
    public InetAddress getIfAddress() {
        return m_ipAddr;
    }

    /**
     * Gets the last poll time of the record
     */
    public String getLastPollString() {
        String result = null;
        if (m_lastPoll != null) {
            result = m_lastPoll.toString();
        }
        return result;
    }

    /**
     * Gets the last poll time of the record
     */
    public Timestamp getLastPoll() {
        return m_lastPoll;
    }

    /**
     * Sets the current creation time.
     * 
     * @param time
     *            The creation time.
     * 
     */
    public void setLastPoll(String time) throws ParseException {
        if (time == null) {
            m_lastPoll = null;
        } else {
            Date tmpDate = EventConstants.parseToDate(time);
            m_lastPoll = new Timestamp(tmpDate.getTime());
        }
        m_changed |= CHANGED_POLLTIME;
    }

    /**
     * Sets the current creation time.
     * 
     * @param time
     *            The creation time.
     * 
     */
    public void setLastPoll(Date time) {
        m_lastPoll = new Timestamp(time.getTime());
        m_changed |= CHANGED_POLLTIME;
    }

    /**
     * Sets the current creation time.
     * 
     * @param time
     *            The creation time.
     * 
     */
    public void setLastPoll(Timestamp time) {
        m_lastPoll = time;
        m_changed |= CHANGED_POLLTIME;
    }

    /**
     * Returns true if the ifIndex is defined.
     */
    public boolean hasIfIndex() {
        return m_ifIndex != -1;
    }

    /**
     * Returns the current ifIndex
     */
    public int getIfIndex() {
        return m_ifIndex;
    }

    /**
     * Sets the ifIndex value
     * 
     * @param ndx
     *            The new ifIndex.
     */
    public void setIfIndex(int ndx) {
        m_ifIndex = ndx;
        m_changed |= CHANGED_IFINDEX;
    }

    public boolean hasIfIndexChanged() {
        if ((m_changed & CHANGED_IFINDEX) == CHANGED_IFINDEX) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateIfIndex(int newIfIndex) {
        if (newIfIndex != -1 && newIfIndex != m_ifIndex) {
            setIfIndex(newIfIndex);
            return true;
        }
        return false;
    }

    /**
     * Returns the current hostname.
     */
    public String getHostname() {
        return m_hostname;
    }

    /**
     * Sets the current hostname.
     * 
     * @param name
     *            The new hostname
     */
    public void setHostname(String name) {
        m_hostname = name;
        m_changed |= CHANGED_HOSTNAME;
    }

    public boolean hasHostnameChanged() {
        if ((m_changed & CHANGED_HOSTNAME) == CHANGED_HOSTNAME) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateHostname(String newHostname) {
        boolean doUpdate = false;
        if (newHostname != null && m_hostname != null) {
            if (!newHostname.equals(m_hostname)) {
                doUpdate = true;
            }
        } else if (newHostname == null && m_hostname == null) {
            // do nothing
        } else {
            // one is null the other isn't, do the update
            doUpdate = true;
        }

        if (doUpdate) {
            setHostname(newHostname);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the current managed state of the interface
     */
    public char getManagedState() {
        return m_managedState;
    }

    /**
     * Sets the managed state of the instance.
     * 
     * @param state
     *            The new managed state
     */
    public void setManagedState(char state) {
        m_managedState = state;
        m_changed |= CHANGED_MANAGED;
    }

    public boolean hasManagedStateChanged() {
        if ((m_changed & CHANGED_MANAGED) == CHANGED_MANAGED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateManagedState(char newManagedState) {
        if (newManagedState != m_managedState) {
            setManagedState(newManagedState);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the current operational status field
     */
    public int getStatus() {
        return m_status;
    }

    /**
     * Sets the current status of the interface
     * 
     * @param status
     *            The new status.
     * 
     */
    public void setStatus(int status) {
        m_status = status;
        m_changed |= CHANGED_STATUS;
    }

    public boolean hasStatusChanged() {
        if ((m_changed & CHANGED_STATUS) == CHANGED_STATUS) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateStatus(int newStatus) {
        if (newStatus != -1 && newStatus != m_status) {
            setStatus(newStatus);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the current primary state.
     */
    public char getPrimaryState() {
        return m_primaryState;
    }

    /**
     * Sets the new primary state.
     * 
     * @param state
     *            The new primary state.
     */
    public void setPrimaryState(char state) {
        m_primaryState = state;
        m_changed |= CHANGED_PRIMARY;
    }

    public boolean hasPrimaryStateChanged() {
        if ((m_changed & CHANGED_PRIMARY) == CHANGED_PRIMARY) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updatePrimaryState(char newPrimaryState) {
        if (newPrimaryState != SNMP_UNKNOWN && newPrimaryState != m_primaryState) {
            setPrimaryState(newPrimaryState);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates the interface information in the configured database. If the
     * interfaca does not exist the a new row in the table is created. If the
     * element already exists then it's current row is updated as needed based
     * upon the current changes to the node.
     */
    public void store() throws SQLException {
        if (m_changed != 0 || m_fromDb == false) {
            Connection db = null;
            try {
                db = DataSourceFactory.getInstance().getConnection();
                store(db);
                if (db.getAutoCommit() == false) {
                    db.commit();
                }
            } finally {
                try {
                    if (db != null) {
                        db.close();
                    }
                } catch (SQLException e) {
                    LOG.warn("Exception closing JDBC connection", e);
                }
            }
        }
        return;
    }

    /**
     * Updates the interface information in the configured database. If the
     * interfaca does not exist the a new row in the table is created. If the
     * element already exists then it's current row is updated as needed based
     * upon the current changes to the node.
     * 
     * @param db
     *            The database connection used to write the record.
     */
    public void store(Connection db) throws SQLException {
        if (m_changed != 0 || m_fromDb == false) {
            if (m_fromDb) {
                update(db);
            } else {
                insert(db);
            }
        }
    }

    public DbIfServiceEntry[] getServices() throws SQLException {
        DbIfServiceEntry[] entries = null;

        Connection db = null;
        try {
            db = DataSourceFactory.getInstance().getConnection();
            entries = getServices(db);
        } finally {
            try {
                if (db != null) {
                    db.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing JDBC connection", e);
            }
        }

        return entries;
    }

    public DbIfServiceEntry[] getServices(Connection db) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rset = null;
        final DBUtils d = new DBUtils(getClass());
        List<DbIfServiceEntry> l;

        try {
            stmt = db.prepareStatement(SQL_LOAD_IFSVC_LIST);
            d.watch(stmt);
            stmt.setLong(1, m_nodeId);
            stmt.setString(2, InetAddressUtils.str(m_ipAddr));

            rset = stmt.executeQuery();
            d.watch(rset);
            l = new ArrayList<DbIfServiceEntry>();

            while (rset.next()) {
                int sid = rset.getInt(1);
                DbIfServiceEntry entry = DbIfServiceEntry.get(db, m_nodeId, m_ipAddr, sid);
                if (entry != null) {
                    l.add(entry);
                }
            }
        } finally {
            d.cleanUp();
        }

        DbIfServiceEntry[] entries = new DbIfServiceEntry[l.size()];
        return l.toArray(entries);
    }

    /**
     * Creates a new entry. The entry is created in memory, but is not written
     * to the database until the first call to <code>store</code>.
     * 
     * @param address
     *            The address of the interface.
     * @param l
     *            The node id of the interface.
     * 
     * @return A new interface record.
     */
    public static DbIpInterfaceEntry create(long l, InetAddress address) {
        return new DbIpInterfaceEntry(l, address, false);
    }

    /**
     * Creates a new entry. The entry is created in memory, but is not written
     * to the database until the first call to <code>store</code>.
     * 
     * @param address
     *            The address of the interface.
     * @param nid
     *            The node id of the interface.
     * @param ifIndex
     *            The ifindex of the interface.
     * 
     * @return A new interface record.
     */
    public static DbIpInterfaceEntry create(int nid, InetAddress address, int ifIndex) {
        return new DbIpInterfaceEntry(nid, address, ifIndex, false);
    }

    /**
     * Clones an existing entry.
     * 
     * @param entry
     *            The entry to be cloned
     * 
     * @return a new DbIpInterfaceEntry identical to the original
     */
    public static DbIpInterfaceEntry clone(DbIpInterfaceEntry entry) {
        DbIpInterfaceEntry clonedEntry = create(entry.getNodeId(), entry.getIfAddress());
        clonedEntry.m_fromDb = entry.m_fromDb;
        clonedEntry.m_ifIndex = entry.m_ifIndex;
        clonedEntry.m_managedState = entry.m_managedState;
        clonedEntry.m_status = entry.m_status;
        clonedEntry.m_lastPoll = entry.m_lastPoll;
        clonedEntry.m_primaryState = entry.m_primaryState;
        clonedEntry.m_changed = entry.m_changed;
        return clonedEntry;
    }

    /**
     * Retrieves a current record from the database based upon the key fields of
     * <em>nodeID</em> and <em>ipAddr</em>. If the record cannot be found
     * then a null reference is returned.
     * 
     * @param nid
     *            The node id key
     * @param addr
     *            The ip address.
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbIpInterfaceEntry get(int nid, InetAddress addr) throws SQLException {
        Connection db = null;
        try {
            db = DataSourceFactory.getInstance().getConnection();
            return get(db, nid, addr);
        } finally {
            try {
                if (db != null) {
                    db.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing JDBC connection", e);
            }
        }
    }

    /**
     * Retrieves a current record from the database based upon the key fields of
     * <em>nodeID</em> and <em>ipAddr</em>. If the record cannot be found
     * then a null reference is returned.
     * 
     * @param nid
     *            The node id key
     * @param addr
     *            The ip address.
     * @param ifIndex
     *            The interface index.
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbIpInterfaceEntry get(int nid, InetAddress addr, int ifIndex) throws SQLException {
        Connection db = null;
        try {
            db = DataSourceFactory.getInstance().getConnection();
            return get(db, nid, addr, ifIndex);
        } finally {
            try {
                if (db != null) {
                    db.close();
                }
            } catch (SQLException e) {
                LOG.warn("Exception closing JDBC connection", e);
            }
        }
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
        StringBuffer buf = new StringBuffer();

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

    /**
     * For debugging only
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        try {
            DbIpInterfaceEntry entry = DbIpInterfaceEntry.get(Integer.parseInt(args[0]), InetAddressUtils.addr(args[1]));
            System.out.println(entry.toString());

            DbIfServiceEntry[] services = entry.getServices();
            for (int i = 0; i < services.length; i++) {
                System.out.println(services[i].toString());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
