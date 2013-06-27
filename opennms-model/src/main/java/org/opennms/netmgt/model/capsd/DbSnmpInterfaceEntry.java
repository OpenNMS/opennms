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
import java.sql.Types;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
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
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class DbSnmpInterfaceEntry {
	
	private static final Logger LOG = LoggerFactory.getLogger(DbSnmpInterfaceEntry.class);

    /**
     * The SQL statement used to read a node from the database. This record is
     * keyed by the node identifier and the ifIndex.
     */
    private static final String SQL_LOAD_REC = "SELECT "
        + "snmpIpAdEntNetMask, snmpPhysAddr, snmpIfDescr, snmpIfType, "
        + "snmpIfName, snmpIfSpeed, snmpIfAdminStatus, snmpIfOperStatus, "
        + "snmpIfAlias, snmpCollect FROM snmpInterface WHERE nodeID = ? AND snmpIfIndex = ?";

    /**
     * True if this recored was loaded from the database. False if it's new.
     */
    private boolean m_fromDb;

    /**
     * The node identifier
     */
    private long m_nodeId;

    /**
     * The SNMP ifIndex
     */
    private int m_ifIndex;

    private InetAddress m_netmask;

    private String m_physAddr;

    private String m_ifDescription;

    private int m_ifType;

    private String m_ifName;

    private String m_ifAlias;

    private long m_ifSpeed;

    private int m_ifAdminStatus;

    private int m_ifOperStatus;
    
    private String m_collect;

    /**
     * The bit map used to determine which elements have changed since the
     * record was created.
     */
    private int m_changed;

    // Mask fields
    // private static final int CHANGED_IFADDRESS = 1 << 0;

    private static final int CHANGED_NETMASK = 1 << 1;

    private static final int CHANGED_PHYSADDR = 1 << 2;

    private static final int CHANGED_DESCRIPTION = 1 << 3;

    private static final int CHANGED_IFTYPE = 1 << 4;

    private static final int CHANGED_IFNAME = 1 << 5;

    private static final int CHANGED_IFSPEED = 1 << 6;

    private static final int CHANGED_IFADMINSTATUS = 1 << 7;

    private static final int CHANGED_IFOPERSTATUS = 1 << 8;
    
    private static final int CHANGED_IFALIAS = 1 << 9;
    
    private static final int CHANGED_COLLECT = 1 << 10;

    /**
     * Inserts the new interface into the ipInterface table of the OpenNMS
     * database.
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
        StringBuffer names = new StringBuffer("INSERT INTO snmpInterface (nodeID,snmpIfIndex");
        StringBuffer values = new StringBuffer("?,?");

        if ((m_changed & CHANGED_NETMASK) == CHANGED_NETMASK) {
            values.append(",?");
            names.append(",snmpIpAdEntNetMask");
        }

        if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR) {
            values.append(",?");
            names.append(",snmpPhysAddr");
        }

        if ((m_changed & CHANGED_DESCRIPTION) == CHANGED_DESCRIPTION) {
            values.append(",?");
            names.append(",snmpIfDescr");
        }

        if ((m_changed & CHANGED_IFTYPE) == CHANGED_IFTYPE) {
            values.append(",?");
            names.append(",snmpIfType");
        }

        if ((m_changed & CHANGED_IFNAME) == CHANGED_IFNAME) {
            values.append(",?");
            names.append(",snmpIfName");
        }

        if ((m_changed & CHANGED_IFSPEED) == CHANGED_IFSPEED) {
            values.append(",?");
            names.append(",snmpIfSpeed");
        }

        if ((m_changed & CHANGED_IFADMINSTATUS) == CHANGED_IFADMINSTATUS) {
            values.append(",?");
            names.append(",snmpIfAdminStatus");
        }

        if ((m_changed & CHANGED_IFOPERSTATUS) == CHANGED_IFOPERSTATUS) {
            values.append(",?");
            names.append(",snmpIfOperStatus");
        }
        
        if ((m_changed & CHANGED_IFALIAS) == CHANGED_IFALIAS) {
            values.append(",?");
            names.append(",snmpIfAlias");
        }

        if ((m_changed & CHANGED_COLLECT) == CHANGED_COLLECT) {
            values.append(",?");
            names.append(",snmpCollect");
        }

        names.append(") VALUES (").append(values).append(')');
        LOG.debug("DbSnmpInterfaceEntry.insert: SQL insert statment = {}", names.toString());

        // create the Prepared statement and then start setting the result values
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            stmt = c.prepareStatement(names.toString());
            d.watch(stmt);
            names = null;

            int ndx = 1;
            stmt.setLong(ndx++, m_nodeId);
            stmt.setInt(ndx++, m_ifIndex);

            if ((m_changed & CHANGED_NETMASK) == CHANGED_NETMASK) {
                stmt.setString(ndx++, InetAddressUtils.str(m_netmask));
            }

            if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR) {
                stmt.setString(ndx++, m_physAddr);
            }

            if ((m_changed & CHANGED_DESCRIPTION) == CHANGED_DESCRIPTION) {
                stmt.setString(ndx++, m_ifDescription);
            }

            if ((m_changed & CHANGED_IFTYPE) == CHANGED_IFTYPE) {
                stmt.setInt(ndx++, m_ifType);
            }

            if ((m_changed & CHANGED_IFNAME) == CHANGED_IFNAME) {
                stmt.setString(ndx++, m_ifName);
            }

            if ((m_changed & CHANGED_IFSPEED) == CHANGED_IFSPEED) {
                stmt.setLong(ndx++, m_ifSpeed);
            }

            if ((m_changed & CHANGED_IFADMINSTATUS) == CHANGED_IFADMINSTATUS) {
                stmt.setInt(ndx++, m_ifAdminStatus);
            }

            if ((m_changed & CHANGED_IFOPERSTATUS) == CHANGED_IFOPERSTATUS) {
                stmt.setInt(ndx++, m_ifOperStatus);
            }
            
            if ((m_changed & CHANGED_IFALIAS) == CHANGED_IFALIAS) {
                stmt.setString(ndx++, m_ifAlias);
            }
            if ((m_changed & CHANGED_COLLECT) == CHANGED_COLLECT) {
                stmt.setString(ndx++, m_collect);
            }

            // Run the insert
            int rc = stmt.executeUpdate();
            LOG.debug("DbSnmpInterfaceEntry.insert: SQL update result = {}", rc);
        } catch (SQLException e) {
            LOG.error("Insertion of snmpinterface entry failed: nodeid = {}, ifIndex = {}", m_nodeId, m_ifIndex);
            throw e;
        } catch (RuntimeException e) {
            LOG.error("Insertion of snmpinterface entry failed: nodeid = {}, ifIndex = {}", m_nodeId, m_ifIndex);
            throw e;
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
        StringBuffer sqlText = new StringBuffer("UPDATE snmpInterface SET ");

        char comma = ' ';

        if ((m_changed & CHANGED_NETMASK) == CHANGED_NETMASK) {
            sqlText.append(comma).append("snmpIpAdEntNetMask = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR) {
            sqlText.append(comma).append("snmpPhysAddr = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_DESCRIPTION) == CHANGED_DESCRIPTION) {
            sqlText.append(comma).append("snmpIfDescr = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_IFTYPE) == CHANGED_IFTYPE) {
            sqlText.append(comma).append("snmpIfType = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_IFNAME) == CHANGED_IFNAME) {
            sqlText.append(comma).append("snmpIfName = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_IFSPEED) == CHANGED_IFSPEED) {
            sqlText.append(comma).append("snmpIfSpeed = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_IFADMINSTATUS) == CHANGED_IFADMINSTATUS) {
            sqlText.append(comma).append("snmpIfAdminStatus = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_IFOPERSTATUS) == CHANGED_IFOPERSTATUS) {
            sqlText.append(comma).append("snmpIfOperStatus = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_IFALIAS) == CHANGED_IFALIAS) {
            sqlText.append(comma).append("snmpIfAlias = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_COLLECT) == CHANGED_COLLECT) {
            sqlText.append(comma).append("snmpCollect = ?");
            comma = ',';
        }

        sqlText.append(" WHERE nodeID = ? AND snmpIfIndex = ? ");

        LOG.debug("DbSnmpInterfaceEntry.update: SQL update statment = {}", sqlText.toString());

        // create the Prepared statement and then start setting the result values
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            stmt = c.prepareStatement(sqlText.toString());
            d.watch(stmt);
            sqlText = null;

            int ndx = 1;

            if ((m_changed & CHANGED_NETMASK) == CHANGED_NETMASK) {
                if (m_netmask == null) {
                    stmt.setNull(ndx++, Types.VARCHAR);
                } else {
                    stmt.setString(ndx++, InetAddressUtils.str(m_netmask));
                }
            }

            if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR) {
                if (m_physAddr == null) {
                    stmt.setNull(ndx++, Types.CHAR);
                } else {
                    stmt.setString(ndx++, m_physAddr);
                }
            }

            if ((m_changed & CHANGED_DESCRIPTION) == CHANGED_DESCRIPTION) {
                if (m_ifDescription == null) {
                    stmt.setNull(ndx++, Types.VARCHAR);
                } else {
                    stmt.setString(ndx++, m_ifDescription);
                }
            }

            if ((m_changed & CHANGED_IFTYPE) == CHANGED_IFTYPE) {
                if (m_ifType == -1) {
                    stmt.setNull(ndx++, Types.INTEGER);
                } else {
                    stmt.setInt(ndx++, m_ifType);
                }
            }

            if ((m_changed & CHANGED_IFNAME) == CHANGED_IFNAME) {
                if (m_ifName == null) {
                    stmt.setNull(ndx++, Types.VARCHAR);
                } else {
                    stmt.setString(ndx++, m_ifName);
                }
            }

            if ((m_changed & CHANGED_IFSPEED) == CHANGED_IFSPEED) {
                if (m_ifSpeed == -1L) {
                    stmt.setNull(ndx++, Types.INTEGER);
                } else {
                    stmt.setLong(ndx++, m_ifSpeed);
                }
            }

            if ((m_changed & CHANGED_IFADMINSTATUS) == CHANGED_IFADMINSTATUS) {
                if (m_ifAdminStatus == -1) {
                    stmt.setNull(ndx++, Types.INTEGER);
                } else {
                    stmt.setInt(ndx++, m_ifAdminStatus);
                }
            }

            if ((m_changed & CHANGED_IFOPERSTATUS) == CHANGED_IFOPERSTATUS) {
                if (m_ifOperStatus == -1) {
                    stmt.setNull(ndx++, Types.INTEGER);
                } else {
                    stmt.setInt(ndx++, m_ifOperStatus);
                }
            }

            if ((m_changed & CHANGED_IFALIAS) == CHANGED_IFALIAS) {
                if (m_ifAlias == null) {
                    stmt.setNull(ndx++, Types.VARCHAR);
                } else {
                    stmt.setString(ndx++, m_ifAlias);
                }
            }

            if ((m_changed & CHANGED_COLLECT) == CHANGED_COLLECT) {
                if (m_collect == null) {
                    stmt.setNull(ndx++, Types.VARCHAR);
                } else {
                    stmt.setString(ndx++, m_collect);
                }
            }

            stmt.setLong(ndx++, m_nodeId);

            if (m_ifIndex == -1) {
                stmt.setNull(ndx++, Types.INTEGER);
            } else {
                stmt.setInt(ndx++, m_ifIndex);
            }

            // Run the update
            int rc = stmt.executeUpdate();
            LOG.debug("DbSnmpInterfaceEntry.update: update result = {}", rc);
        } catch (SQLException e) {
            LOG.error("Update of snmpinterface entry failed: nodeid = {}, ifIndex = {}", m_nodeId, m_ifIndex);
            throw e;
        } catch (RuntimeException e) {
            LOG.error("Update of snmpinterface entry failed: nodeid = {}, ifIndex = {}", m_nodeId, m_ifIndex);
            throw e;
        } finally {
            d.cleanUp();
        }
        stmt.close();

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
            stmt = c.prepareStatement(SQL_LOAD_REC);
            d.watch(stmt);
            stmt.setLong(1, m_nodeId);
            stmt.setInt(2, m_ifIndex);

            // Run the query
            rset = stmt.executeQuery();
            d.watch(rset);
            if (!rset.next()) {
                return false;
            }

            // extract the values
            int ndx = 1;

            // get the netmask
            String str = rset.getString(ndx++);
            if (str != null && !rset.wasNull()) {
            	m_netmask = InetAddressUtils.addr(str);
            }

            // get the physical address
            m_physAddr = rset.getString(ndx++);
            if (rset.wasNull()) {
                m_physAddr = null;
            }

            // get the description
            m_ifDescription = rset.getString(ndx++);
            if (rset.wasNull()) {
                m_ifDescription = null;
            }

            // get the type
            m_ifType = rset.getInt(ndx++);
            if (rset.wasNull()) {
                m_ifIndex = -1;
            }

            // get the name
            m_ifName = rset.getString(ndx++);
            if (rset.wasNull()) {
                m_ifName = null;
            }

            // get the speed
            m_ifSpeed = rset.getLong(ndx++);
            if (rset.wasNull()) { 
                m_ifSpeed = -1L;
            }

            // get the admin status
            m_ifAdminStatus = rset.getInt(ndx++);
            if (rset.wasNull()) {
                m_ifAdminStatus = -1;
            }

            // get the operational status
            m_ifOperStatus = rset.getInt(ndx++);
            if (rset.wasNull()) {
                m_ifOperStatus = -1;
            }
            
            // get the alias
            m_ifAlias = rset.getString(ndx++);
            if (rset.wasNull()) {
                m_ifAlias = null;
            }

            // get the collect flag
            m_collect = rset.getString(ndx++);
            if (rset.wasNull()) {
                m_collect = null;
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
    private DbSnmpInterfaceEntry() {
        throw new UnsupportedOperationException("Default constructor not supported!");
    }

    /**
     * Constructs a new interface.
     * 
     * @param nodeId
     *            The node identifier.
     * @param ifIndex
     *            The interface index to load
     * 
     */
    private DbSnmpInterfaceEntry(long nodeId, int ifIndex) {
        this(nodeId, ifIndex, true);
    }

    /**
     * Constructs a new interface.
     * 
     * @param nodeId
     *            The node identifier.
     * @param ifIndex
     *            The interface index to load
     * @param exists
     *            True if the interface already exists.
     * 
     */
    private DbSnmpInterfaceEntry(long nodeId, int ifIndex, boolean exists) {
        m_fromDb = exists;
        m_nodeId = nodeId;
        m_ifIndex = ifIndex;
        m_netmask = null;
        m_physAddr = null;
        m_ifDescription = null;
        m_ifType = -1;
        m_ifName = null;
        m_ifSpeed = -1L;
        m_ifAdminStatus = -1;
        m_ifOperStatus = -1;
        m_ifAlias = null;
        m_collect = null;
        m_changed = 0;
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

    public InetAddress getNetmask() {
        return m_netmask;
    }

    public void setNetmask(InetAddress mask) {
        m_netmask = mask;
        m_changed |= CHANGED_NETMASK;
    }

    public boolean hasNetmaskChanged() {
        if ((m_changed & CHANGED_NETMASK) == CHANGED_NETMASK) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateNetmask(InetAddress newNetmask) {
        if (newNetmask == null || newNetmask.equals(m_netmask)) {
            return false;
        } else {
            setNetmask(newNetmask);
            return true;
        }
    }

    public String getPhysicalAddress() {
        return m_physAddr;
    }

    public void setPhysicalAddress(String addr) {
        m_physAddr = addr;
        m_changed |= CHANGED_PHYSADDR;
    }

    public boolean hasPhysicalAddressChanged() {
        if ((m_changed & CHANGED_PHYSADDR) == CHANGED_PHYSADDR) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updatePhysicalAddress(String newPhysAddr) {
        if (newPhysAddr == null || newPhysAddr.equals(m_physAddr)) {
            return false;
        } else {
            setPhysicalAddress(newPhysAddr);
            return true;
        }
    }

    public String getDescription() {
        return m_ifDescription;
    }

    public void setDescription(String descr) {
        m_ifDescription = descr;
        m_changed |= CHANGED_DESCRIPTION;
    }

    public boolean hasDescriptionChanged() {
        if ((m_changed & CHANGED_DESCRIPTION) == CHANGED_DESCRIPTION) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateDescription(String newIfDescription) {
        if (newIfDescription == null
                || newIfDescription.equals(m_ifDescription)) {
            return false;
        } else {
            setDescription(newIfDescription);
            return true;
        }
    }

    public String getName() {
        return m_ifName;
    }

    public void setName(String name) {
        m_ifName = name;
        m_changed |= CHANGED_IFNAME;
    }

    public boolean hasNameChanged() {
        if ((m_changed & CHANGED_IFNAME) == CHANGED_IFNAME) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateName(String newIfName) {
        if (newIfName == null || newIfName.equals(m_ifName)) {
            return false;
        } else {
            setName(newIfName);
            return true;
        }
    }

    public int getType() {
        return m_ifType;
    }

    public void setType(int type) {
        m_ifType = type;
        m_changed |= CHANGED_IFTYPE;
    }

    public boolean hasTypeChanged() {
        if ((m_changed & CHANGED_IFTYPE) == CHANGED_IFTYPE) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateType(int newIfType) {
        if (newIfType == -1 || newIfType == m_ifType) {
            return false;
        } else {
            setType(newIfType);
            return true;
        }
    }

    public long getSpeed() {
        return m_ifSpeed;
    }

    public void setSpeed(long speed) {
        m_ifSpeed = speed;
        m_changed |= CHANGED_IFSPEED;
    }

    public boolean hasSpeedChanged() {
        if ((m_changed & CHANGED_IFSPEED) == CHANGED_IFSPEED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateSpeed(long newIfSpeed) {
        if (newIfSpeed == -1L || newIfSpeed == m_ifSpeed) {
            return false;
        } else {
            setSpeed(newIfSpeed);
            return true;
        }
    }

    public int getAdminStatus() {
        return m_ifAdminStatus;
    }

    public void setAdminStatus(int status) {
        m_ifAdminStatus = status;
        m_changed |= CHANGED_IFADMINSTATUS;
    }

    public boolean hasAdminStatusChanged() {
        if ((m_changed & CHANGED_IFADMINSTATUS) == CHANGED_IFADMINSTATUS) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateAdminStatus(int newIfAdminStatus) {
        if (newIfAdminStatus == -1 || newIfAdminStatus == m_ifAdminStatus) {
            return false;
        } else {
            setAdminStatus(newIfAdminStatus);
            return true;
        }
    }

    public int getOperationalStatus() {
        return m_ifOperStatus;
    }

    public void setOperationalStatus(int status) {
        m_ifOperStatus = status;
        m_changed |= CHANGED_IFOPERSTATUS;
    }

    public boolean hasOperationalStatusChanged() {
        if ((m_changed & CHANGED_IFOPERSTATUS) == CHANGED_IFOPERSTATUS) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateOperationalStatus(int newIfOperStatus) {
        if (newIfOperStatus == -1 || newIfOperStatus == m_ifOperStatus) {
            return false;
        } else {
            setOperationalStatus(newIfOperStatus);
            return true;
        }
    }
    
    public String getAlias() {
        return m_ifAlias;
    }

    public void setAlias(String alias) {
        m_ifAlias = alias;
        m_changed |= CHANGED_IFALIAS;
    }

    public boolean hasAliasChanged() {
        if ((m_changed & CHANGED_IFALIAS) == CHANGED_IFALIAS) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateAlias(String newIfAlias) {
        if (newIfAlias == null || newIfAlias.equals(m_ifAlias)) {
            return false;
        } else {
            setAlias(newIfAlias);
            return true;
        }
    }

    public String getCollect() {
        return m_collect;
    }

    public void setCollect(String collect) {
        m_collect = collect;
        m_changed |= CHANGED_COLLECT;
    }

    public boolean hasCollectChanged() {
        return ((m_changed & CHANGED_COLLECT) == CHANGED_COLLECT);
    }

    public boolean updateCollect(String newCollect) {
        if (newCollect == null || newCollect.equals(m_collect)) {
            return false;
        } else {
            setCollect(newCollect);
            return true;
        }
    }

    
    /**
     * Updates the interface information in the configured database. If the
     * interface does not exist the a new row in the table is created. If the
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
     * interface does not exist the a new row in the table is created. If the
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

    /**
     * Creates a new entry. The entry is created in memory, but is not written
     * to the database until the first call to <code>store</code>.
     * 
     * @param nid
     *            The node id of the interface.
     * @param ifIndex
     *            The ifIndex of the interface
     * 
     * @return A new interface record.
     */
    public static DbSnmpInterfaceEntry create(int nid, int ifIndex) {
        return new DbSnmpInterfaceEntry(nid, ifIndex, false);
    }

    /**
     * Retrieves a current record from the database based upon the key fields of
     * <em>nodeID</em> and <em>ifindex</em>. If the record cannot be found
     * then a null reference is returned.
     * 
     * @param nodeId
     *            The node id key
     * @param ifIndex
     *            the interface index.
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbSnmpInterfaceEntry get(long nodeId, int ifIndex) throws SQLException {
        Connection db = null;
        try {
            db = DataSourceFactory.getInstance().getConnection();
            return get(db, nodeId, ifIndex);
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
     * <em>nodeID</em> and <em>ifIndex</em>. If the record cannot be found
     * then a null reference is returned.
     * 
     * @param db
     *            The database connection used to load the entry.
     * @param nodeId
     *            The node id key
     * @param ifIndex
     *            The interface index.
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbSnmpInterfaceEntry get(Connection db, long nodeId, int ifIndex)
    throws SQLException {
        DbSnmpInterfaceEntry entry = new DbSnmpInterfaceEntry(nodeId, ifIndex);
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

        buf.append("from database   = ").append(m_fromDb).append(sep);
        buf.append("node identifier = ").append(m_nodeId).append(sep);
        buf.append("IP Netmask      = ").append(InetAddressUtils.str(m_netmask)).append(sep);
        buf.append("MAC             = ").append(m_physAddr).append(sep);
        buf.append("ifIndex         = ").append(m_ifIndex).append(sep);
        buf.append("ifDescr         = ").append(m_ifDescription).append(sep);
        buf.append("ifType          = ").append(m_ifType).append(sep);
        buf.append("ifName          = ").append(m_ifName).append(sep);
        buf.append("ifSpeed         = ").append(m_ifSpeed).append(sep);
        buf.append("ifAdminStatus   = ").append(m_ifAdminStatus).append(sep);
        buf.append("ifOperStatus    = ").append(m_ifOperStatus).append(sep);
        buf.append("ifAlias         = ").append(m_ifAlias).append(sep);
        buf.append("collect         = ").append(m_collect).append(sep);
        return buf.toString();
    }

    /**
     * For debugging only
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        try {
            Integer temp = new Integer(args[1]);
            DbSnmpInterfaceEntry entry =
                DbSnmpInterfaceEntry.get(Integer.parseInt(args[0]),
                                         temp.intValue());
            System.out.println(entry.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
