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
 * <p>
 * This class is used to model a row of the <em>node</em> table from the
 * OpenNMS database. The node table is indexed by the elements <em>dpNode</em>
 * and <em>nodeID</em>. When a new element is created using the
 * <code>create</code> call a node id will be automatically defined. If the
 * name of the distribute poller is not passed to the create method, the it also
 * is assigned a default value.
 * </p>
 * 
 * <p>
 * Once loaded or create, the class tracks any changes and will write those
 * changes to the database whenever the <code>store</code> method is invoked.
 * If a database conneciton is not passed to the store method, then a temporary
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
 * 
 */
public final class DbNodeEntry {
	
	private static final Logger LOG = LoggerFactory.getLogger(DbNodeEntry.class);

    /**
     * The character returned if the node is active
     */
    public static final char NODE_TYPE_ACTIVE = 'A';

    /**
     * The character returned if the node is deleted
     */
    public static final char NODE_TYPE_DELETED = 'D';

    /**
     * The character returned if the node type is unset/unknown.
     */
    public static final char NODE_TYPE_UNKNOWN = ' ';

    /**
     * Label source set by user
     */
    public static final char LABEL_SOURCE_USER = 'U';

    /**
     * Label source set by netbios
     */
    public static final char LABEL_SOURCE_NETBIOS = 'N';

    /**
     * Label source set by hostname
     */
    public static final char LABEL_SOURCE_HOSTNAME = 'H';

    /**
     * Label source set by SNMP sysname
     */
    public static final char LABEL_SOURCE_SYSNAME = 'S';

    /**
     * Label source set by IP Address
     */
    public static final char LABEL_SOURCE_ADDRESS = 'A';

    /**
     * Label source unset/unknown
     */
    public static final char LABEL_SOURCE_UNKNOWN = ' ';

    /**
     * The default distributed poller name to use if one is not supplied
     */
    private static final String DEFAULT_DP_NAME = "localhost";

    /**
     * The SQL text used to extract the next sequence id for the node table.
     */
    // private static final String SQL_NEXT_NID = "SELECT NEXTVAL('nodeNxtId')";
    private static final String SQL_NEXT_NID = System.getProperty("opennms.db.nextNodeId", "SELECT NEXTVAL('nodeNxtId')");
    
    /**
     * The SQL statement used to read a node from the database. This record is
     * keyed by the node identifier and distributed poller name.
     */
    private static final String SQL_LOAD_REC = "SELECT nodeCreateTime, nodeParentID, nodeType, nodeSysOID, nodeSysName, nodeSysDescription, nodeSysLocation, nodeSysContact, nodeLabel, nodeLabelSource, nodeNetBIOSName, nodeDomainName, operatingSystem, lastCapsdPoll, foreignSource, foreignId FROM node WHERE nodeID = ? AND dpName = ? AND nodeType != 'D'";

    /**
     * The SQL statement used to read the list of IP Addresses associated with
     * this node.
     */
    private static final String SQL_LOAD_IF_LIST = "SELECT ipAddr, ifIndex FROM ipInterface WHERE nodeID = ? AND isManaged != 'D'";

    /**
     * The SQL statement used to read the list of managed IP Addresses
     * associated with this node.
     */
    private static final String SQL_LOAD_MANAGED_IF_LIST = "SELECT ipAddr, ifIndex FROM ipInterface WHERE nodeID = ? AND isManaged = 'M'";

    /**
     * The SQL statement used to read the list of SNMP interface entries for
     * this particular node.
     */
    private static final String SQL_LOAD_SNMP_LIST = "SELECT ipAddr, snmpIfIndex FROM snmpInterface WHERE nodeID = ?";

    /**
     * True if this recored was loaded from the database. False if it's new.
     */
    private boolean m_fromDb;

    /**
     * The node identifier
     */
    private int m_nodeId;

    /**
     * The name of the distributed poller
     */
    private String m_dpName;

    /**
     * The date the record was created, if any
     */
    private Timestamp m_createTime;

    /**
     * The parent identifier, if any
     */
    private int m_parentId;

    /**
     * The type of node, active or deleted.
     */
    private char m_type;

    /**
     * SNMP system object identifier
     */
    private String m_sysoid;

    /**
     * SNMP system name
     */
    private String m_sysname;

    /**
     * SNMP system description
     */
    private String m_sysdescr;

    /**
     * SNMP system location
     */
    private String m_syslocation;

    /**
     * SNMP system contact
     */
    private String m_syscontact;

    /**
     * The node's label
     */
    private String m_label;

    /**
     * Source of the label
     */
    private char m_labelSource;

    /**
     * The netbios name
     */
    private String m_nbName;

    /**
     * The netbios domain name
     */
    private String m_nbDomainName;

    /**
     * The operating system
     */
    private String m_os;

    /**
     * The last time the node was scanned.
     */
    private Timestamp m_lastPoll;
    
    /**
     * The foreignSource for the node.
     */
    private String m_foreignSource;
    
    /**
     * The foreignId for the node;
     */
    private String m_foreignId;

    /**
     * The bit map used to determine which elements have changed since the
     * record was created.
     */
    private int m_changed;

    // Mask fields
    //
    private static final int CHANGED_CREATE_TIME = 1 << 0;

    private static final int CHANGED_PARENT_ID = 1 << 1;

    private static final int CHANGED_TYPE = 1 << 2;

    private static final int CHANGED_SYSOID = 1 << 3;

    private static final int CHANGED_SYSNAME = 1 << 4;

    private static final int CHANGED_SYSLOC = 1 << 5;

    private static final int CHANGED_SYSCONTACT = 1 << 6;

    private static final int CHANGED_LABEL = 1 << 7;

    private static final int CHANGED_LABEL_SOURCE = 1 << 8;

    private static final int CHANGED_NETBIOS_NAME = 1 << 9;

    private static final int CHANGED_DOMAIN_NAME = 1 << 10;

    private static final int CHANGED_OS = 1 << 11;

    private static final int CHANGED_DPNAME = 1 << 12;

    private static final int CHANGED_SYSDESCR = 1 << 13;

    private static final int CHANGED_POLLTIME = 1 << 14;

    private static final int CHANGED_FOREIGN_SOURCE = 1 << 15;

    private static final int CHANGED_FOREIGN_ID = 1 << 16;

    /**
     * Inserts the new node into the node table of the OpenNMS databasee.
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

        // Get the next node identifier
        PreparedStatement stmt = null;
        ResultSet rset = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = c.prepareStatement(SQL_NEXT_NID);
            d.watch(stmt);
            rset = stmt.executeQuery();
            d.watch(rset);
            rset.next();
            m_nodeId = rset.getInt(1);

            // first extract the next node identifier
            //
            StringBuffer names = new StringBuffer("INSERT INTO node (nodeID,dpName");
            StringBuffer values = new StringBuffer("?");

            if ((m_changed & CHANGED_DPNAME) == CHANGED_DPNAME) {
                values.append(",?");
            } else {
                values.append(",'").append(DEFAULT_DP_NAME).append("'");
            }

            if ((m_changed & CHANGED_PARENT_ID) == CHANGED_PARENT_ID) {
                values.append(",?");
                names.append(",nodeParentID");
            }

            if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE) {
                values.append(",?");
                names.append(",nodeType");
            }

            if ((m_changed & CHANGED_CREATE_TIME) == CHANGED_CREATE_TIME) {
                values.append(",?");
                names.append(",nodeCreateTime");
            } else {
                values.append(",?");
                names.append(",nodeCreateTime");
                m_createTime = new Timestamp(new Date().getTime());
                m_changed |= CHANGED_CREATE_TIME;
            }

            if ((m_changed & CHANGED_SYSOID) == CHANGED_SYSOID) {
                values.append(",?");
                names.append(",nodeSysOID");
            }

            if ((m_changed & CHANGED_SYSNAME) == CHANGED_SYSNAME) {
                values.append(",?");
                names.append(",nodeSysName");
            }

            if ((m_changed & CHANGED_SYSDESCR) == CHANGED_SYSDESCR) {
                values.append(",?");
                names.append(",nodeSysDescription");
            }

            if ((m_changed & CHANGED_SYSLOC) == CHANGED_SYSLOC) {
                values.append(",?");
                names.append(",nodeSysLocation");
            }

            if ((m_changed & CHANGED_SYSCONTACT) == CHANGED_SYSCONTACT) {
                values.append(",?");
                names.append(",nodeSysContact");
            }

            if ((m_changed & CHANGED_LABEL) == CHANGED_LABEL) {
                values.append(",?");
                names.append(",nodeLabel");
            }

            if ((m_changed & CHANGED_LABEL_SOURCE) == CHANGED_LABEL_SOURCE) {
                values.append(",?");
                names.append(",nodeLabelSource");
            }

            if ((m_changed & CHANGED_NETBIOS_NAME) == CHANGED_NETBIOS_NAME) {
                values.append(",?");
                names.append(",nodeNetBIOSName");
            }

            if ((m_changed & CHANGED_DOMAIN_NAME) == CHANGED_DOMAIN_NAME) {
                values.append(",?");
                names.append(",nodeDomainName");
            }

            if ((m_changed & CHANGED_OS) == CHANGED_OS) {
                values.append(",?");
                names.append(",operatingSystem");
            }

            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
                values.append(",?");
                names.append(",lastCapsdPoll");
            }

            if ((m_changed & CHANGED_FOREIGN_SOURCE) == CHANGED_FOREIGN_SOURCE) {
                values.append(",?");
                names.append(",foreignSource");
            }
            if ((m_changed & CHANGED_FOREIGN_ID) == CHANGED_FOREIGN_ID) {
                values.append(",?");
                names.append(",foreignId");
            }

            names.append(") VALUES (").append(values).append(')');
            LOG.debug("DbNodeEntry.insert: SQL insert statment = {}", names.toString());

            // create the Prepared statment and then
            // start setting the result values
            //
            stmt = c.prepareStatement(names.toString());
            d.watch(stmt);
            names = null;

            int ndx = 1;
            stmt.setInt(ndx++, m_nodeId);
            if ((m_changed & CHANGED_DPNAME) == CHANGED_DPNAME)
                stmt.setString(ndx++, m_dpName);

            if ((m_changed & CHANGED_PARENT_ID) == CHANGED_PARENT_ID)
                if (m_parentId == -1)
                    stmt.setNull(ndx++, Types.INTEGER);
                else
                    stmt.setInt(ndx++, m_parentId);

            if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE)
                stmt.setString(ndx++, new String(new char[] { m_type }));

            if ((m_changed & CHANGED_CREATE_TIME) == CHANGED_CREATE_TIME) {
                stmt.setTimestamp(ndx++, m_createTime);
            }

            if ((m_changed & CHANGED_SYSOID) == CHANGED_SYSOID)
                stmt.setString(ndx++, m_sysoid);

            if ((m_changed & CHANGED_SYSNAME) == CHANGED_SYSNAME)
                stmt.setString(ndx++, m_sysname);

            if ((m_changed & CHANGED_SYSDESCR) == CHANGED_SYSDESCR)
                stmt.setString(ndx++, m_sysdescr);

            if ((m_changed & CHANGED_SYSLOC) == CHANGED_SYSLOC)
                stmt.setString(ndx++, m_syslocation);

            if ((m_changed & CHANGED_SYSCONTACT) == CHANGED_SYSCONTACT)
                stmt.setString(ndx++, m_syscontact);

            if ((m_changed & CHANGED_LABEL) == CHANGED_LABEL)
                stmt.setString(ndx++, m_label);

            if ((m_changed & CHANGED_LABEL_SOURCE) == CHANGED_LABEL_SOURCE)
                stmt.setString(ndx++, new String(new char[] { m_labelSource }));

            if ((m_changed & CHANGED_NETBIOS_NAME) == CHANGED_NETBIOS_NAME)
                stmt.setString(ndx++, m_nbName);

            if ((m_changed & CHANGED_DOMAIN_NAME) == CHANGED_DOMAIN_NAME)
                stmt.setString(ndx++, m_nbDomainName);

            if ((m_changed & CHANGED_OS) == CHANGED_OS)
                stmt.setString(ndx++, m_os);

            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
                stmt.setTimestamp(ndx++, m_lastPoll);
            }

            if ((m_changed & CHANGED_FOREIGN_SOURCE) == CHANGED_FOREIGN_SOURCE)
                stmt.setString(ndx++, m_foreignSource);

            if ((m_changed & CHANGED_FOREIGN_ID) == CHANGED_FOREIGN_ID)
                stmt.setString(ndx++, m_foreignId);

                LOG.debug("nodeid='{}' nodetype='{}' createTime='{}' lastPoll='{}' dpName='{}' sysname='{}' sysoid='{}' sysdescr='{}' syslocation='{}' syscontact='{}' label='{}' labelsource='{}' netbios='{}' domain='{}' os='{}'", m_nodeId, new String(new char[] { m_type }), m_createTime, m_lastPoll, m_dpName, m_sysname, m_sysoid, m_sysdescr, m_syslocation, m_syscontact, m_label, new String(new char[] { m_labelSource }), m_nbName, m_nbDomainName, m_os); 

            // Run the insert
            //
            int rc = stmt.executeUpdate();
            LOG.debug("DbNodeEntry.insert: SQL update result = {}", rc);

            // Insert a null entry into the asset table

            createAssetNodeEntry(c, m_nodeId);

            // clear the mask and mark as backed
            // by the database
            //
            m_fromDb = true;
            m_changed = 0;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Updates an existing record in the OpenNMS node table.
     * 
     * @param c
     *            The connection used for the update.
     * 
     * @throws java.sql.SQLException
     *             Thrown if an error occurs with the connection
     */
    private void update(Connection c) throws SQLException {
        if (!m_fromDb)
            throw new IllegalStateException("The record does not exists in the database");

        // first extract the next node identifier
        //
        StringBuffer sqlText = new StringBuffer("UPDATE node SET ");

        char comma = ' ';
        if ((m_changed & CHANGED_PARENT_ID) == CHANGED_PARENT_ID) {
            sqlText.append(comma).append("nodeParentID = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE) {
            sqlText.append(comma).append("nodeType = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_CREATE_TIME) == CHANGED_CREATE_TIME) {
            sqlText.append(comma).append("nodeCreateTime = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_SYSOID) == CHANGED_SYSOID) {
            sqlText.append(comma).append("nodeSysOID = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_SYSNAME) == CHANGED_SYSNAME) {
            sqlText.append(comma).append("nodeSysName = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_SYSDESCR) == CHANGED_SYSDESCR) {
            sqlText.append(comma).append("nodeSysDescription = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_SYSLOC) == CHANGED_SYSLOC) {
            sqlText.append(comma).append("nodeSysLocation = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_SYSCONTACT) == CHANGED_SYSCONTACT) {
            sqlText.append(comma).append("nodeSysContact = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_LABEL) == CHANGED_LABEL) {
            sqlText.append(comma).append("nodeLabel = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_LABEL_SOURCE) == CHANGED_LABEL_SOURCE) {
            sqlText.append(comma).append("nodeLabelSource = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_NETBIOS_NAME) == CHANGED_NETBIOS_NAME) {
            sqlText.append(comma).append("nodeNetBIOSName = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_DOMAIN_NAME) == CHANGED_DOMAIN_NAME) {
            sqlText.append(comma).append("nodeDomainName = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_OS) == CHANGED_OS) {
            sqlText.append(comma).append("operatingSystem = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
            sqlText.append(comma).append("lastCapsdPoll = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_FOREIGN_SOURCE) == CHANGED_FOREIGN_SOURCE) {
            sqlText.append(comma).append("foreignSource = ?");
            comma = ',';
        }

        if ((m_changed & CHANGED_FOREIGN_ID) == CHANGED_FOREIGN_ID) {
            sqlText.append(comma).append("foreignId = ?");
            comma = ',';
        }

        sqlText.append(" WHERE nodeID = ? AND dpName = ?");

        LOG.debug("DbNodeEntry.update: SQL update statment = {}", sqlText.toString());

        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = c.prepareStatement(sqlText.toString());
            d.watch(stmt);
            
            sqlText = null;

            int ndx = 1;
            if ((m_changed & CHANGED_PARENT_ID) == CHANGED_PARENT_ID)
                if (m_parentId == -1)
                    stmt.setNull(ndx++, Types.INTEGER);
                else
                    stmt.setInt(ndx++, m_parentId);

            if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE)
                stmt.setString(ndx++, new String(new char[] { m_type }));

            if ((m_changed & CHANGED_CREATE_TIME) == CHANGED_CREATE_TIME) {
                if (m_createTime == null) {
                    stmt.setNull(ndx++, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(ndx++, m_createTime);
                }
            }

            if ((m_changed & CHANGED_SYSOID) == CHANGED_SYSOID) {
                if (m_sysoid == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_sysoid);
            }

            if ((m_changed & CHANGED_SYSNAME) == CHANGED_SYSNAME) {
                if (m_sysname == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_sysname);
            }

            if ((m_changed & CHANGED_SYSDESCR) == CHANGED_SYSDESCR) {
                if (m_sysdescr == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_sysdescr);
            }

            if ((m_changed & CHANGED_SYSLOC) == CHANGED_SYSLOC) {
                if (m_syslocation == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_syslocation);
            }

            if ((m_changed & CHANGED_SYSCONTACT) == CHANGED_SYSCONTACT) {
                if (m_syscontact == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_syscontact);
            }

            if ((m_changed & CHANGED_LABEL) == CHANGED_LABEL) {
                if (m_label == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_label);
            }

            if ((m_changed & CHANGED_LABEL_SOURCE) == CHANGED_LABEL_SOURCE) {
                stmt.setString(ndx++, new String(new char[] { m_labelSource }));
            }

            if ((m_changed & CHANGED_NETBIOS_NAME) == CHANGED_NETBIOS_NAME) {
                if (m_nbName == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_nbName);
            }

            if ((m_changed & CHANGED_DOMAIN_NAME) == CHANGED_DOMAIN_NAME) {
                if (m_nbDomainName == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_nbDomainName);
            }

            if ((m_changed & CHANGED_OS) == CHANGED_OS) {
                if (m_os == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_os);
            }

            if ((m_changed & CHANGED_POLLTIME) == CHANGED_POLLTIME) {
                if (m_lastPoll != null) {
                    stmt.setTimestamp(ndx++, m_lastPoll);
                } else
                    stmt.setNull(ndx++, Types.TIMESTAMP);
            }

            if ((m_changed & CHANGED_FOREIGN_SOURCE) == CHANGED_FOREIGN_SOURCE) {
                if (m_foreignSource == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_foreignSource);
            }

            if ((m_changed & CHANGED_FOREIGN_ID) == CHANGED_FOREIGN_ID) {
                if (m_foreignId == null)
                    stmt.setNull(ndx++, Types.VARCHAR);
                else
                    stmt.setString(ndx++, m_foreignId);
            }

            stmt.setInt(ndx++, m_nodeId);
            stmt.setString(ndx++, m_dpName);

            // Run the insert
            //
            int rc = stmt.executeUpdate();
            LOG.debug("DbNodeEntry.update: update result = {}", rc);

            // clear the mask and mark as backed
            // by the database
            //
            m_changed = 0;
        } finally {
            d.cleanUp();
        }
    }

    /**
     * Load the current node from the database. If the node was modified, the
     * modifications are lost. The nodeid and dpName must be set prior to this
     * call.
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

        // create the Prepared statement and then
        // start setting the result values
        //
        PreparedStatement stmt = null;
        ResultSet rset = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = c.prepareStatement(SQL_LOAD_REC);
            d.watch(stmt);
            stmt.setInt(1, m_nodeId);
            stmt.setString(2, m_dpName);

            rset = stmt.executeQuery();
            d.watch(rset);
            if (!rset.next()) {
                return false;
            }

            // extract the values.
            //
            int ndx = 1;

            // get the time
            //
            m_createTime = rset.getTimestamp(ndx++);

            // the parent id
            //
            m_parentId = rset.getInt(ndx++);
            if (rset.wasNull())
                m_parentId = -1;

            // the node type
            //
            String str = rset.getString(ndx++);
            if (str != null && !rset.wasNull())
                m_type = str.charAt(0);
            else
                m_type = NODE_TYPE_UNKNOWN;

            // the sysoid
            //
            m_sysoid = rset.getString(ndx++);
            if (rset.wasNull())
                m_sysoid = null;

            // the sysname
            //
            m_sysname = rset.getString(ndx++);
            if (rset.wasNull())
                m_sysname = null;

            // the sys description
            //
            m_sysdescr = rset.getString(ndx++);
            if (rset.wasNull())
                m_sysdescr = null;

            // the system location
            //
            m_syslocation = rset.getString(ndx++);
            if (rset.wasNull())
                m_syslocation = null;

            // the system contact
            //
            m_syscontact = rset.getString(ndx++);
            if (rset.wasNull())
                m_syscontact = null;

            // the node label
            //
            m_label = rset.getString(ndx++);
            if (rset.wasNull())
                m_label = null;

            // the label type
            //
            str = rset.getString(ndx++);
            if (rset.wasNull() || str == null)
                m_labelSource = LABEL_SOURCE_UNKNOWN;
            else
                m_labelSource = str.charAt(0);

            // the netbios name
            //
            m_nbName = rset.getString(ndx++);
            if (rset.wasNull())
                m_nbName = null;

            // the domain name
            //
            m_nbDomainName = rset.getString(ndx++);
            if (rset.wasNull())
                m_nbDomainName = null;

            // the operating system
            //
            m_os = rset.getString(ndx++);
            if (rset.wasNull())
                m_os = null;

            // get the last poll time
            //
            m_lastPoll = rset.getTimestamp(ndx++);
            
            m_foreignSource = rset.getString(ndx++);
            
            m_foreignId = rset.getString(ndx++);
        } finally {
            d.cleanUp();
        }

        // clear the mask and mark as backed by the database
        m_changed = 0;
        return true;
    }

    /**
     * Default constructor. Constructs an empty node entry with no data. The
     * default distributed poller name is used internally.
     * 
     */
    private DbNodeEntry() {
        this(DEFAULT_DP_NAME);
    }

    /**
     * Constructs a new node entry with no data, except that the distributed
     * poller name is set to the passed string.
     * 
     * @param poller
     *            The poller name.
     * 
     */
    private DbNodeEntry(String poller) {
        m_fromDb = false;
        m_nodeId = -1;
        m_dpName = poller;
        m_createTime = null;
        m_parentId = -1;
        m_type = NODE_TYPE_UNKNOWN;
        m_sysoid = null;
        m_sysname = null;
        m_sysdescr = null;
        m_syslocation = null;
        m_syscontact = null;
        m_label = null;
        m_labelSource = LABEL_SOURCE_UNKNOWN;
        m_nbName = null;
        m_nbDomainName = null;
        m_os = null;
        m_lastPoll = null;
        m_foreignSource = null;
        m_foreignId = null;
        m_changed = 0;
    }

    /**
     * Constructs a new entry with the specific node identifier. Once set the
     * node identifier is non-mutable. If this constructor is used the record
     * must already exists in the database.
     * 
     * @param nid
     *            The node identifier.
     * 
     */
    private DbNodeEntry(int nid) {
        this(DEFAULT_DP_NAME);
        m_fromDb = true;
        m_nodeId = nid;
        m_dpName = DEFAULT_DP_NAME;
    }

    /**
     * Constructs a new entry with the specific node identifier. Once set the
     * node identifier is non-mutable. If this constructor is used the record
     * must already exists in the database.
     * 
     * @param nid
     *            The node identifier.
     * @param dpName
     *            The name of the distributed poller.
     * 
     */
    private DbNodeEntry(int nid, String dpName) {
        this(dpName);
        m_fromDb = true;
        m_nodeId = nid;
        m_dpName = dpName;
    }

    /**
     * Returns the node entry's unique identifier. This is a non-mutable
     * element. If the record does not yet exist in the database then a -1 is
     * returned.
     * 
     */
public     int getNodeId() {
        return m_nodeId;
    }

    /**
     * Returns the name of the distributed poller for the entry. This is a
     * non-mutable element of the record.
     * 
     */
public     String getDistributedPollerName() {
        return m_dpName;
    }

    /**
     * Gets the creation time of the record.
     */
    public Timestamp getCreationTime() {
        return m_createTime;
    }

    /**
     * Gets the creation time of the record.
     */
    public String getFormattedCreationTime() {
        String result = null;
        if (m_createTime != null) {
            result = m_createTime.toString();
        }
        return result;
    }

    /**
     * Sets the current creation time.
     * 
     * @param time
     *            The creation time.
     * 
     */
    public void setCreationTime(String time) throws ParseException {
        if (time == null) {
            m_createTime = null;
        } else {
            Date tmpDate = EventConstants.parseToDate(time);
            m_createTime = new Timestamp(tmpDate.getTime());
        }
        m_changed |= CHANGED_CREATE_TIME;
    }

    /**
     * Sets the current creation time.
     * 
     * @param time
     *            The creation time.
     * 
     */
    public void setCreationTime(Date time) {
        m_createTime = new Timestamp(time.getTime());
        m_changed |= CHANGED_CREATE_TIME;
    }

    /**
     * Sets the current creation time.
     * 
     * @param time
     *            The creation time.
     * 
     */
    public void setCreationTime(Timestamp time) {
        m_createTime = time;
        m_changed |= CHANGED_CREATE_TIME;
    }

    /**
     * Returns true if the entry has a parent identity.
     * 
     */
    public boolean hasParentId() {
        return m_parentId != -1;
    }

    /**
     * Returns the id of the parent.
     */
    public int getParentId() {
        return m_parentId;
    }

    /**
     * Sets the id of the parent.
     * 
     * @param id
     *            The new parent id.
     */
    public void setParentId(int id) {
        m_parentId = id;
        m_changed |= CHANGED_PARENT_ID;
    }

    public boolean hasParentIdChanged() {
        if ((m_changed & CHANGED_PARENT_ID) == CHANGED_PARENT_ID)
            return true;
        else
            return false;
    }

    public boolean updateParentId(int newparentId) {
        if (newparentId != m_parentId) {
            setParentId(newparentId);
            return true;
        } else
            return false;
    }

    /**
     * Returns the current node type
     */
    public char getNodeType() {
        return m_type;
    }

    /**
     * Sets the node type
     * 
     * @param type
     *            The new node type.
     * 
     */
    public void setNodeType(char type) {
        m_type = type;
        m_changed |= CHANGED_TYPE;
    }

    public boolean hasNodeTypeChanged() {
        if ((m_changed & CHANGED_TYPE) == CHANGED_TYPE)
            return true;
        else
            return false;
    }

    public boolean updateNodeType(char newtype) {
        if (newtype != m_type) {
            setNodeType(newtype);
            return true;
        } else
            return false;
    }

    /**
     * Returns the current SNMP system object id, if any.
     */
    public String getSystemOID() {
        return m_sysoid;
    }

    /**
     * Sets the current SNMP system object id.
     */
    public void setSystemOID(String oid) {
        m_sysoid = oid;
        m_changed |= CHANGED_SYSOID;
    }

    public boolean hasSystemOIDChanged() {
        if ((m_changed & CHANGED_SYSOID) == CHANGED_SYSOID)
            return true;
        else
            return false;
    }

    /**
     * Update the value of sysoid associated with this node entry.
     * 
     */
    public boolean updateSystemOID(String newsysoid) {
        if (newsysoid == null || newsysoid.equals(m_sysoid))
            return false;
        else {
            setSystemOID(newsysoid);
            return true;
        }
    }

    /**
     * Gets the current system name
     */
    public String getSystemName() {
        return m_sysname;
    }

    /**
     * Sets the current system name.
     * 
     * @param name
     *            The new system name
     */
    public void setSystemName(String name) {
        m_sysname = name;
        m_changed |= CHANGED_SYSNAME;
    }

    public boolean hasSystemNameChanged() {
        if ((m_changed & CHANGED_SYSNAME) == CHANGED_SYSNAME)
            return true;
        else
            return false;
    }

    public boolean updateSystemName(String newsysname) {
        if (newsysname == null || newsysname.equals(m_sysname))
            return false;
        else {
            setSystemName(newsysname);
            return true;
        }
    }

    /**
     * Returns the current system description
     */
    public String getSystemDescription() {
        return m_sysdescr;
    }

    /**
     * Sets the current system description
     * 
     * @param descr
     *            The new system description.
     */
    public void setSystemDescription(String descr) {
        m_sysdescr = descr;
        m_changed |= CHANGED_SYSDESCR;
    }

    public boolean hasSystemDescriptionChanged() {
        if ((m_changed & CHANGED_SYSDESCR) == CHANGED_SYSDESCR)
            return true;
        else
            return false;
    }

    public boolean updateSystemDescription(String newsysdescr) {
        if (newsysdescr == null || newsysdescr.equals(m_sysdescr))
            return false;
        else {
            setSystemDescription(newsysdescr);
            return true;
        }
    }

    /**
     * Returns the current system location
     */
    public String getSystemLocation() {
        return m_syslocation;
    }

    /**
     * Sets the current system location.
     * 
     * @param loc
     *            The new location
     */
    public void setSystemLocation(String loc) {
        m_syslocation = loc;
        m_changed |= CHANGED_SYSLOC;
    }

    public boolean hasSystemLocationChanged() {
        if ((m_changed & CHANGED_SYSLOC) == CHANGED_SYSLOC)
            return true;
        else
            return false;
    }

    public boolean updateSystemLocation(String newsyslocation) {
        if (newsyslocation == null || newsyslocation.equals(m_syslocation))
            return false;
        else {
            setSystemLocation(newsyslocation);
            return true;
        }
    }

    /**
     * Returns the current system contact.
     */
    public String getSystemContact() {
        return m_syscontact;
    }

    /**
     * Sets the current system contact.
     * 
     * @param contact
     *            The new system contact
     */
    public void setSystemContact(String contact) {
        m_syscontact = contact;
        m_changed |= CHANGED_SYSCONTACT;
    }

    public boolean hasSystemContactChanged() {
        if ((m_changed & CHANGED_SYSCONTACT) == CHANGED_SYSCONTACT)
            return true;
        else
            return false;
    }

    public boolean updateSystemContact(String newsyscontact) {
        if (newsyscontact == null || newsyscontact.equals(m_syscontact))
            return false;
        else {
            setSystemContact(newsyscontact);
            return true;
        }
    }

    /**
     * Returns the entry's label.
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * Sets the current label for the entry.
     * 
     * @param label
     *            The new label.
     */
    public void setLabel(String label) {
        m_label = label;
        m_changed |= CHANGED_LABEL;
    }

    public boolean hasLabelChanged() {
        if ((m_changed & CHANGED_LABEL) == CHANGED_LABEL)
            return true;
        else
            return false;
    }

    public boolean updateLabel(String newlabel) {
        boolean doUpdate = false;
        if (newlabel != null && m_label != null) {
            if (!newlabel.equals(m_label))
                doUpdate = true;
        } else if (newlabel == null && m_label == null) {
            // do nothing
        } else
            // one is null the other isn't, do the update
            doUpdate = true;

        if (doUpdate) {
            setLabel(newlabel);
            return true;
        } else
            return false;
    }

    /**
     * Returns the current label source.
     */
    public char getLabelSource() {
        return m_labelSource;
    }

    /**
     * Sets the source of the node's label.
     * 
     * @param src
     *            The new label source.
     */
    public void setLabelSource(char src) {
        m_labelSource = src;
        m_changed |= CHANGED_LABEL_SOURCE;
    }

    public boolean hasLabelSourceChanged() {
        if ((m_changed & CHANGED_LABEL_SOURCE) == CHANGED_LABEL_SOURCE)
            return true;
        else
            return false;
    }

    public boolean updateLabelSource(char newlabelSource) {
        if (newlabelSource != m_labelSource) {
            setLabelSource(newlabelSource);
            return true;
        } else
            return false;
    }

    /**
     * Returns the current NetBIOS name.
     */
    public String getNetBIOSName() {
        return m_nbName;
    }

    /**
     * Sets the current NetBIOS name.
     * 
     * @param name
     *            The new NetBIOS name.
     */
    public void setNetBIOSName(String name) {
        if (name != null) {
            m_nbName = name.toUpperCase();
        } else {
            m_nbName = null;
        }
        m_changed |= CHANGED_NETBIOS_NAME;
    }

    public boolean hasNetBIOSNameChanged() {
        if ((m_changed & CHANGED_NETBIOS_NAME) == CHANGED_NETBIOS_NAME)
            return true;
        else
            return false;
    }

    public boolean updateNetBIOSName(String newnbName) {
        boolean doUpdate = false;
        if (newnbName != null && m_nbName != null) {
            if (!newnbName.toUpperCase().equals(m_nbName))
                doUpdate = true;
        } else if (newnbName == null && m_nbName == null) {
            // do nothing
        } else
            // one is null the other isn't, do the update
            doUpdate = true;

        if (doUpdate) {
            setNetBIOSName(newnbName);
            return true;
        } else
            return false;
    }

    /**
     * Returns the current domain name.
     */
    public String getDomainName() {
        return m_nbDomainName;
    }

    /**
     * Sets the current domain name.
     * 
     * @param domain
     *            The new domain name.
     */
    public void setDomainName(String domain) {
        m_nbDomainName = domain;
        m_changed |= CHANGED_DOMAIN_NAME;
    }

    public boolean hasDomainNameChanged() {
        if ((m_changed & CHANGED_DOMAIN_NAME) == CHANGED_DOMAIN_NAME)
            return true;
        else
            return false;
    }

    public boolean updateDomainName(String domain) {
        boolean doUpdate = false;
        if (domain != null && m_nbDomainName != null) {
            if (!domain.equals(m_nbDomainName))
                doUpdate = true;
        } else if (domain == null && m_nbDomainName == null) {
            // do nothing
        } else
            // one is null the other isn't, do the update
            doUpdate = true;

        if (doUpdate) {
            setDomainName(domain);
            return true;
        } else
            return false;
    }

    /**
     * Returns the current operating system string
     */
    public String getOS() {
        return m_os;
    }

    /**
     * Sets the current operating system string.
     * 
     * @param os
     *            The OS string
     */
    public void setOS(String os) {
        m_os = os;
        m_changed |= CHANGED_OS;
    }

    public boolean hasOSChanged() {
        if ((m_changed & CHANGED_NETBIOS_NAME) == CHANGED_NETBIOS_NAME)
            return true;
        else
            return false;
    }

    public boolean updateOS(String newos) {
        boolean doUpdate = false;
        if (newos != null && m_os != null) {
            if (!newos.equals(m_os))
                doUpdate = true;
        } else if (newos == null && m_os == null) {
            // do nothing
        } else
            // one is null the other isn't, do the update
            doUpdate = true;

        if (doUpdate) {
            setOS(newos);
            return true;
        } else
            return false;
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
     * Sets the last poll time.
     * 
     * @param time
     *            The last poll time.
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
     * Sets the last poll time.
     * 
     * @param time
     *            The last poll time.
     * 
     */
    public void setLastPoll(Date time) {
        m_lastPoll = new Timestamp(time.getTime());
        m_changed |= CHANGED_POLLTIME;
    }

    /**
     * Sets the last poll time.
     * 
     * @param time
     *            The last poll time.
     * 
     */
    public void setLastPoll(Timestamp time) {
        m_lastPoll = time;
        m_changed |= CHANGED_POLLTIME;
    }
    
    /**
     * Returns the current foreignSource.
     */
    public String getForeignSource() {
        return m_foreignSource;
    }

    /**
     * Sets the current NetBIOS name.
     * 
     * @param name
     *            The new NetBIOS name.
     */
    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
        m_changed |= CHANGED_FOREIGN_SOURCE;
    }

    public boolean hasForeignSource() {
        if ((m_changed & CHANGED_FOREIGN_SOURCE) == CHANGED_FOREIGN_SOURCE)
            return true;
        else
            return false;
    }

    public boolean updateForeignSource(String newForeignSource) {
        boolean doUpdate = false;
        if (newForeignSource != null && m_foreignSource != null) {
            if (!newForeignSource.toUpperCase().equals(m_foreignSource))
                doUpdate = true;
        } else if (newForeignSource == null && m_foreignSource == null) {
            // do nothing
        } else
            // one is null the other isn't, do the update
            doUpdate = true;

        if (doUpdate) {
            setForeignSource(newForeignSource);
            return true;
        } else
            return false;
    }

    
    /**
     * Returns the current foreignSource.
     */
    public String getForeignId() {
        return m_foreignId;
    }

    /**
     * Sets the current NetBIOS name.
     * 
     * @param name
     *            The new NetBIOS name.
     */
    public void setForeignId(String foreignId) {
        m_foreignId = foreignId;
        m_changed |= CHANGED_FOREIGN_ID;
    }

    public boolean hasForeignId() {
        if ((m_changed & CHANGED_FOREIGN_ID) == CHANGED_FOREIGN_ID)
            return true;
        else
            return false;
    }

    public boolean updateForeignId(String newForeignId) {
        boolean doUpdate = false;
        if (newForeignId != null && m_foreignId != null) {
            if (!newForeignId.toUpperCase().equals(m_foreignId))
                doUpdate = true;
        } else if (newForeignId == null && m_foreignId == null) {
            // do nothing
        } else
            // one is null the other isn't, do the update
            doUpdate = true;

        if (doUpdate) {
            setForeignId(newForeignId);
            return true;
        } else
            return false;
    }
    

    /**
     * Updates the node information in the configured database. If the node does
     * not exist the a new row in the table is created. If the element already
     * exists then it's current row is updated as needed based upon the current
     * changes to the node.
     * 
     */
    public void store() throws SQLException {
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
                    LOG.warn("Exception closing JDBC connection", e);
                }
            }
        }
        return;
    }

    /**
     * Updates the node information in the configured database. If the node does
     * not exist the a new row in the table is created. If the element already
     * exists then it's current row is updated as needed based upon the current
     * changes to the node.
     * 
     * @param db
     *            The database connection used to write the record.
     */
    public void store(Connection db) throws SQLException {
        LOG.debug("DbNodeEntry: changed map = 0x{}", Integer.toHexString(m_changed));
        if (m_changed != 0 || m_fromDb == false) {
            if (m_fromDb)
                update(db);
            else
                insert(db);
        }
    }

    public DbIpInterfaceEntry[] getInterfaces() throws SQLException {
        DbIpInterfaceEntry[] entries = null;

        Connection db = null;
        try {
            db = DataSourceFactory.getInstance().getConnection();
            entries = getInterfaces(db);
        } finally {
            try {
                if (db != null)
                    db.close();
            } catch (SQLException e) {
                LOG.warn("Exception closing JDBC connection", e);
            }
        }

        return entries;
    }

    public DbIpInterfaceEntry[] getInterfaces(Connection db) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rset = null;
        final DBUtils d = new DBUtils(getClass());
        
        List<DbIpInterfaceEntry> l;
        try {
            stmt = db.prepareStatement(SQL_LOAD_IF_LIST);
            d.watch(stmt);
            stmt.setInt(1, m_nodeId);

            rset = stmt.executeQuery();
            d.watch(rset);
            l = new ArrayList<DbIpInterfaceEntry>();

            while (rset.next()) {
                String saddr = rset.getString(1);
                int ifIndex = rset.getInt(2);
                if (rset.wasNull())
                    ifIndex = -1;

                InetAddress addr = null;
                addr = InetAddressUtils.addr(saddr);
                if (addr == null) continue;

                DbIpInterfaceEntry entry = null;
                if (ifIndex == -1)
                    entry = DbIpInterfaceEntry.get(db, m_nodeId, addr);
                else
                    entry = DbIpInterfaceEntry.get(db, m_nodeId, addr, ifIndex);

                if (entry != null)
                    l.add(entry);
            }
        } finally {
            d.cleanUp();
        }

        DbIpInterfaceEntry[] entries = new DbIpInterfaceEntry[l.size()];
        return l.toArray(entries);
    }

    public DbIpInterfaceEntry[] getManagedInterfaces() throws SQLException {
        DbIpInterfaceEntry[] entries = null;

        Connection db = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            db = DataSourceFactory.getInstance().getConnection();
            d.watch(db);
            entries = getManagedInterfaces(db);
        } finally {
            d.cleanUp();
        }

        return entries;
    }

    public DbIpInterfaceEntry[] getManagedInterfaces(Connection db) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rset = null;
        final DBUtils d = new DBUtils(getClass());
        
        List<DbIpInterfaceEntry> l;
        try {
            stmt = db.prepareStatement(SQL_LOAD_MANAGED_IF_LIST);
            d.watch(stmt);
            stmt.setInt(1, m_nodeId);

            rset = stmt.executeQuery();
            d.watch(rset);
            l = new ArrayList<DbIpInterfaceEntry>();

            while (rset.next()) {
                String saddr = rset.getString(1);
                int ifIndex = rset.getInt(2);
                if (rset.wasNull())
                    ifIndex = -1;

                InetAddress addr = null;
                addr = InetAddressUtils.addr(saddr);
                if (addr == null) continue;
                
                DbIpInterfaceEntry entry = null;
                if (ifIndex == -1)
                    entry = DbIpInterfaceEntry.get(db, m_nodeId, addr);
                else
                    entry = DbIpInterfaceEntry.get(db, m_nodeId, addr, ifIndex);

                if (entry != null)
                    l.add(entry);
            }
        } finally {
            d.cleanUp();
        }

        DbIpInterfaceEntry[] entries = new DbIpInterfaceEntry[l.size()];
        return l.toArray(entries);
    }

    public static DbIpInterfaceEntry getPrimarySnmpInterface(DbIpInterfaceEntry[] ipInterfaces) {
        if (ipInterfaces == null)
            return null;

        for (int i = 0; i < ipInterfaces.length; i++)
            if (ipInterfaces[i].getPrimaryState() == DbIpInterfaceEntry.SNMP_PRIMARY)
                return ipInterfaces[i];

        return null;
    }

    public DbSnmpInterfaceEntry[] getSnmpInterfaces() throws SQLException {
        DbSnmpInterfaceEntry[] entries = null;

        Connection db = null;
        try {
            db = DataSourceFactory.getInstance().getConnection();
            entries = getSnmpInterfaces(db);
        } finally {
            try {
                if (db != null)
                    db.close();
            } catch (SQLException e) {
                LOG.warn("Exception closing JDBC connection", e);
            }
        }

        return entries;
    }

    public DbSnmpInterfaceEntry[] getSnmpInterfaces(Connection db) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rset = null;
        final DBUtils d = new DBUtils(getClass());

        List<DbSnmpInterfaceEntry> l;
        try {
            stmt = db.prepareStatement(SQL_LOAD_SNMP_LIST);
            d.watch(stmt);
            stmt.setInt(1, m_nodeId);

            rset = stmt.executeQuery();
            d.watch(rset);
            l = new ArrayList<DbSnmpInterfaceEntry>();

            while (rset.next()) {
                String saddr = rset.getString(1);
                int ifIndex = rset.getInt(2);
                if (rset.wasNull())
                    ifIndex = -1;

                if (InetAddressUtils.addr(saddr) == null) continue;

                DbSnmpInterfaceEntry entry = null;
                if (ifIndex != -1) {
                    entry = DbSnmpInterfaceEntry.get(db, m_nodeId, ifIndex);

                    if (entry != null)
                        l.add(entry);
                }
            }
        } finally {
            d.cleanUp();
        }

        DbSnmpInterfaceEntry[] entries = new DbSnmpInterfaceEntry[l.size()];
        return l.toArray(entries);
    }

    /**
     * Creates a new entry. The entry is created in memory, but is not written
     * to the database until the first call to <code>store</code>.
     * 
     * @return A new node record.
     */
    public static DbNodeEntry create() {
        return new DbNodeEntry(DEFAULT_DP_NAME);
    }

    /**
     * Creates a new entry. The entry is created in memory, but is not written
     * to the database until the first call to <code>store</code>.
     * 
     * @param poller
     *            The name of the distributed poller to use.
     * 
     * @return A new node record.
     */
    public static DbNodeEntry create(String poller) {
        if (poller == null)
            poller = DEFAULT_DP_NAME;

        return new DbNodeEntry(poller);
    }

    /**
     * Clones an existing entry.
     * 
     * @param entry
     *            The entry to be cloned
     * 
     * @return a new DbNodeEntry identical to the original
     */
    public static DbNodeEntry clone(DbNodeEntry entry) {
        DbNodeEntry clonedEntry = create();
        clonedEntry.m_fromDb = entry.m_fromDb;
        clonedEntry.m_nodeId = entry.m_nodeId;
        clonedEntry.m_createTime = entry.m_createTime;
        clonedEntry.m_parentId = entry.m_parentId;
        clonedEntry.m_type = entry.m_type;
        clonedEntry.m_sysoid = entry.m_sysoid;
        clonedEntry.m_sysname = entry.m_sysname;
        clonedEntry.m_syslocation = entry.m_syslocation;
        clonedEntry.m_sysdescr = entry.m_sysdescr;
        clonedEntry.m_syscontact = entry.m_syscontact;
        clonedEntry.m_label = entry.m_label;
        clonedEntry.m_labelSource = entry.m_labelSource;
        clonedEntry.m_nbName = entry.m_nbName;
        clonedEntry.m_nbDomainName = entry.m_nbDomainName;
        clonedEntry.m_os = entry.m_os;
        clonedEntry.m_lastPoll = entry.m_lastPoll;
        clonedEntry.m_changed = entry.m_changed;
        return clonedEntry;
    }

    /**
     * Retreives a current record from the database based upon the key fields of
     * <em>nodeID</em> and <em>dpName</em>. If the record cannot be found
     * then a null reference is returnd.
     * 
     * @param nid
     *            The node id key
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbNodeEntry get(int nid) throws SQLException {
        return get(nid, DEFAULT_DP_NAME);
    }

    /**
     * Retreives a current record from the database based upon the key fields of
     * <em>nodeID</em> and <em>dpName</em>. If the record cannot be found
     * then a null reference is returnd.
     * 
     * @param nid
     *            The node id key
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbNodeEntry get(int nid, String dpName) throws SQLException {
        Connection db = null;
        try {
            db = DataSourceFactory.getInstance().getConnection();
            return get(db, nid, dpName);
        } finally {
            try {
                if (db != null)
                    db.close();
            } catch (SQLException e) {
                LOG.warn("Exception closing JDBC connection", e);
            }
        }
    }

    /**
     * Retrieves a current record from the database based upon the key fields of
     * <em>nodeID</em> and <em>dpName</em>. If the record cannot be found
     * then a null reference is returned.
     * 
     * @param db
     *            The database connection used to load the entry.
     * @param nid
     *            The node id key
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbNodeEntry get(Connection db, int nid) throws SQLException {
        return get(db, nid, DEFAULT_DP_NAME);
    }

    /**
     * Retrieves a current record from the database based upon the key fields of
     * <em>nodeID</em> and <em>dpName</em>. If the record cannot be found
     * then a null reference is returned.
     * 
     * @param db
     *            The database connection used to load the entry.
     * @param nid
     *            The node id key
     * @param dpName
     *            The distribute poller name key
     * 
     * @return The loaded entry or null if one could not be found.
     * 
     */
    public static DbNodeEntry get(Connection db, int nid, String dpName) throws SQLException {
        DbNodeEntry entry = new DbNodeEntry(nid, dpName);
        if (!entry.load(db))
            entry = null;
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

        buf.append("from database            = ").append(m_fromDb).append(sep);
        buf.append("node identifier          = ").append(m_nodeId).append(sep);
        buf.append("distributed poller       = ").append(m_dpName).append(sep);
        buf.append("creation time            = ").append(m_createTime).append(sep);
        buf.append("parent identfier         = ").append(m_parentId).append(sep);
        buf.append("node type                = ").append(m_type).append(sep);
        buf.append("snmp system oid          = ").append(m_sysoid).append(sep);
        buf.append("snmp system name         = ").append(m_sysname).append(sep);
        buf.append("snmp system description  = ").append(m_sysdescr).append(sep);
        buf.append("snmp system location     = ").append(m_syslocation).append(sep);
        buf.append("snmp system contact      = ").append(m_syscontact).append(sep);
        buf.append("label                    = ").append(m_label).append(sep);
        buf.append("label source             = ").append(m_labelSource).append(sep);
        buf.append("NetBIOS                  = ").append(m_nbName).append(sep);
        buf.append("Domain                   = ").append(m_nbDomainName).append(sep);
        buf.append("Operating System         = ").append(m_os).append(sep);
        buf.append("last poll time           = ").append(m_lastPoll).append(sep);
        return buf.toString();
    }

    /**
     * Creates a null entry for a nodeid into the assets table
     *
     * @param conn a {@link java.sql.Connection} object.
     * @param nodeid a int.
     * @throws java.sql.SQLException if any.
     */
    public void createAssetNodeEntry(Connection conn, int nodeid) throws SQLException {

        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        
        try {
            stmt = conn.prepareStatement("INSERT INTO ASSETS (nodeID,category,userLastModified,lastModifiedDate,displayCategory,notifyCategory,pollerCategory,thresholdCategory) values(?,?,?,?,?,?,?,?)");
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            stmt.setString(2, "Unspecified");
            stmt.setString(3, "");
            stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
            stmt.setString(5, "");
            stmt.setString(6, "");
            stmt.setString(7, "");
            stmt.setString(8, "");

            stmt.execute();
        } finally {
            d.cleanUp();
        }
    }


}
