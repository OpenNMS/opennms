/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.opennms.core.utils.DBUtils;

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

    static final int LAME_SNMP_HOST_IFINDEX = -100;

    /**
     * The SQL statement used to read a node from the database. This record is
     * keyed by the node identifier and the ifIndex.
     */
    private static final String SQL_LOAD_REC = "SELECT "
        + "snmpPhysAddr, snmpIfDescr, snmpIfType, "
        + "snmpIfName, snmpIfSpeed, snmpIfAdminStatus, snmpIfOperStatus, "
        + "snmpIfAlias, snmpCollect FROM snmpInterface WHERE nodeID = ? AND snmpIfIndex = ?";

    /**
     * True if this record was loaded from the database. False if it's new.
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
        
        return true;
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
        m_physAddr = null;
        m_ifDescription = null;
        m_ifType = -1;
        m_ifName = null;
        m_ifSpeed = -1L;
        m_ifAdminStatus = -1;
        m_ifOperStatus = -1;
        m_ifAlias = null;
        m_collect = null;
    }

    public String getAlias() {
        return m_ifAlias;
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
        final StringBuilder buf = new StringBuilder();

        buf.append("from database   = ").append(m_fromDb).append(sep);
        buf.append("node identifier = ").append(m_nodeId).append(sep);
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
}
