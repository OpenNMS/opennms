//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// 2005 Jan 03: Added support for lame SNMP hosts
// 2003 Oct 20: Added minval and maxval code for mibObj RRDs
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.utils.AlphaNumeric;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.xml.event.Event;

/**
 * <P>
 * The SnmpCollector class ...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
final class SnmpCollector implements ServiceCollector {
    private final class IfNumberTracker extends SingleInstanceTracker {
        int m_ifNumber = -1;
        private IfNumberTracker() {
            super(SnmpObjId.get(INTERFACES_IFNUMBER), SnmpInstId.INST_ZERO);
        }

        protected void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
            m_ifNumber = val.toInt();
        }
        
        public int getIfNumber() {
            return m_ifNumber;
        }
    }

    /**
     * Name of monitored service.
     */
    private static final String SERVICE_NAME = "SNMP";

    /**
     * SQL statement to retrieve interface's 'ipinterface' table information.
     */
    private static final String SQL_GET_NODEID = "SELECT nodeid,ifindex,issnmpprimary FROM ipinterface WHERE ipaddr=? AND ismanaged!='D'";

    /**
     * SQL statement to retrieve interface's 'issnmpprimary' table information.
     */
    private static final String SQL_GET_ISSNMPPRIMARY = "SELECT ifindex,issnmpprimary FROM ipinterface WHERE nodeid=?";

    /**
     * /** SQL statement to retrieve node's system object id.
     */
    private static final String SQL_GET_NODESYSOID = "SELECT nodesysoid FROM node WHERE nodeid=? AND nodetype!='D'";

    /**
     * SQL statement to check for SNMPv2 for a node
     */
    private static final String SQL_CHECK_SNMPV2 = "SELECT ifservices.serviceid FROM service, ifservices WHERE servicename='SNMPv2' AND ifservices.serviceid = service.serviceid AND nodeid=?";

    /**
     * SQL statement to fetch the ifIndex, ifName, and ifDescr values for all
     * interfaces associated with a node
     */
    private static final String SQL_GET_SNMP_INFO = "SELECT DISTINCT snmpifindex, snmpiftype, snmpifname, snmpifdescr,snmpphysaddr " + "FROM snmpinterface, ipinterface " + "WHERE ipinterface.nodeid=snmpinterface.nodeid " + "AND ifindex = snmpifindex " + "AND ipinterface.nodeid=? " + "AND (ipinterface.ismanaged!='D')";

    /**
     * Default object to collect if "oid" property not available.
     */
    private static final String DEFAULT_OBJECT_IDENTIFIER = ".1.3.6.1.2.1.1.2"; // MIB-II
                                                                                // System
                                                                                // Object
                                                                                // Id

    /**
     * Object identifier used to retrieve interface count.
     */
    private static final String INTERFACES_IFNUMBER = ".1.3.6.1.2.1.2.1"; // MIB-II
                                                                            // interfaces.ifNumber

    /**
     * Valid values for the 'snmpStorageFlag' attribute in datacollection-config
     * xml file.
     * 
     * "primary" = only primary SNMP interface should be collected and stored
     * "all" = all primary SNMP interfaces should be collected and stored
     */
    private static String SNMP_STORAGE_PRIMARY = "primary";

    private static String SNMP_STORAGE_ALL = "all";

    private static String SNMP_STORAGE_SELECT = "select";

    /**
     * This defines the default maximum number of variables the collector is
     * permitted to pack into a single outgoing PDU. This value is intentionally
     * kept relatively small in order to communicate successfully with the
     * largest possible number of agents.
     */
    private static int DEFAULT_MAX_VARS_PER_PDU = 30;

    /**
     * Max number of variables permitted in a single outgoing SNMP PDU request..
     */
    private int m_maxVarsPerPdu;

    /**
     * Path to SNMP RRD file repository.
     */
    private String m_rrdPath;

    /**
     * Local host name
     */
    private String m_host;

    /* -------------------------------------------------------------- */
    /* Attribute key names */
    /* -------------------------------------------------------------- */

    /**
     * Interface attribute key used to store the interface's JoeSNMP SnmpPeer
     * object.
     */
    static final String SNMP_PEER_KEY = "org.opennms.netmgt.collectd.SnmpCollector.SnmpPeer";

    /**
     * Interface attribute key used to store the number of interfaces configured
     * on the remote host.
     */
    static final String INTERFACE_COUNT_KEY = "org.opennms.netmgt.collectd.SnmpCollector.ifCount";

    /**
     * Interface attribute key used to store the map of IfInfo objects which
     * hold data about each interface on a particular node.
     */
    static String IF_MAP_KEY = "org.opennms.netmgt.collectd.SnmpCollector.ifMap";

    /**
     * Interface attribute key used to store a NodeInfo object which holds data
     * about the node being polled.
     */
    static String NODE_INFO_KEY = "org.opennms.netmgt.collectd.SnmpCollector.nodeInfo";

    /**
     * Interface attribute key used to store the data collection scheme to be
     * followed. Two possible values: SNMP_STORAGE_PRIMARY = "primary"
     * SNMP_STORAGE_ALL = "all" SNMP_STORAGE_SELECT = "select"
     */
    static String SNMP_STORAGE_KEY = "org.opennms.netmgt.collectd.SnmpCollector.snmpStorage";

    /**
     * Interface attribute key used to store configured value for the maximum
     * number of variables permitted in a single outgoing SNMP PDU request.
     */
    static String MAX_VARS_PER_PDU_STORAGE_KEY = "org.opennms.netmgt.collectd.SnmpCollector.maxVarsPerPdu";

    /**
     * <P>
     * Returns the name of the service that the plug-in collects ("SNMP").
     * </P>
     * 
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return SERVICE_NAME;
    }

    /**
     * <P>
     * Initialize the service collector.
     * </P>
     * 
     * <P>
     * During initialization the SNMP collector:
     *  - Initializes various configuration factories. - Verifies access to the
     * database - Verifies access to RRD file repository - Verifies access to
     * JNI RRD shared library - Determines if SNMP to be stored for only the
     * node'sprimary interface or for all interfaces.
     * </P>
     * 
     * @param parameters
     *            Not currently used.
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     * 
     */
    public void initialize(Map parameters) {
        // Log4j category
        //
        Category log = log();

        // Get local host name (used when generating threshold events)
        //
        try {
            m_host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            if (log.isEnabledFor(Priority.WARN))
                log.warn("initialize: Unable to resolve local host name.", e);
            m_host = "unresolved.host";
        }

        // Initialize the SnmpPeerFactory
        //
        try {
            SnmpPeerFactory.init();
        } catch (MarshalException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        // Initialize the DataCollectionConfigFactory
        //
        try {
            DataCollectionConfigFactory.reload();

        } catch (MarshalException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed to load data collection configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed to load data collection configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed to load data collection configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        // Make sure we can connect to the database
        //
        java.sql.Connection ctest = null;
        try {
            DatabaseConnectionFactory.init();
            ctest = DatabaseConnectionFactory.getInstance().getConnection();
        } catch (IOException ie) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: IOException getting database connection", ie);
            throw new UndeclaredThrowableException(ie);
        } catch (MarshalException me) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Marshall Exception getting database connection", me);
            throw new UndeclaredThrowableException(me);
        } catch (ValidationException ve) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Validation Exception getting database connection", ve);
            throw new UndeclaredThrowableException(ve);
        } catch (SQLException sqlE) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed getting connection to the database.", sqlE);
            throw new UndeclaredThrowableException(sqlE);
        } catch (ClassNotFoundException cnfE) {
            if (log.isEnabledFor(Priority.FATAL))
                log.fatal("initialize: Failed loading database driver.", cnfE);
            throw new UndeclaredThrowableException(cnfE);
        } finally {
            if (ctest != null) {
                try {
                    ctest.close();
                } catch (Throwable t) {
                    if (log.isEnabledFor(Priority.WARN))
                        log.warn("initialize: an exception occured while closing the JDBC connection", t);
                }
            }
        }

        // Get path to RRD repository
        //
        m_rrdPath = DataCollectionConfigFactory.getInstance().getRrdRepository();
        if (m_rrdPath == null)
            throw new RuntimeException("Configuration error, failed to retrieve path to RRD repository.");

        // TODO: make a path utils class that has the below in it
        // Strip the File.separator char off of the end of the path
        if (m_rrdPath.endsWith(File.separator)) {
            m_rrdPath = m_rrdPath.substring(0, (m_rrdPath.length() - File.separator.length()));
        }
        if (log.isDebugEnabled())
            log.debug("initialize: SNMP RRD file repository path: " + m_rrdPath);

        // If the RRD file repository directory does NOT already exist, create
        // it.
        //
        File f = new File(m_rrdPath);
        if (!f.isDirectory())
            if (!f.mkdirs())
                throw new RuntimeException("Unable to create RRD file repository, path: " + m_rrdPath);

        try {
            RrdUtils.initialize();
        } catch (RrdException e) {
            if (log.isEnabledFor(Priority.ERROR))
                log.error("initialize: Unable to initialize RrdUtils", e);
            throw new RuntimeException("Unable to initialize RrdUtils", e);
        }

        // Save local reference to singleton instance
        //
        // m_rrdInterface = org.opennms.netmgt.rrd.Interface.getInstance();
        if (log.isDebugEnabled())
            log.debug("initialize: successfully instantiated JNI interface to RRD...");

        return;
    }

    /**
     * Responsible for freeing up any resources held by the collector.
     */
    public void release() {
        // Nothing to release...
    }

    /**
     * Responsible for performing all necessary initialization for the specified
     * interface in preparation for data collection.
     * 
     * @param iface
     *            Network interface to be prepped for collection.
     * @param parameters
     *            Key/value pairs associated with the package to which the
     *            interface belongs..
     * 
     */
    public void initialize(NetworkInterface iface, Map parameters) {
        Category log = log();

        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");

        InetAddress ipAddr = getInetAddress(iface);

        String collectionName = getCollectionName(parameters);

        // Determine if data to be collected for all interfaces or only
        // for the primary SNMP interface
        //
        String storageFlag = DataCollectionConfigFactory.getInstance().getSnmpStorageFlag(collectionName);
        if (storageFlag == null) {
            if (log.isEnabledFor(Priority.WARN))
                log.warn("initialize: Configuration error, failed to retrieve SNMP storage flag for collection: " + collectionName);
            storageFlag = SNMP_STORAGE_PRIMARY;
        }

        // Add the SNMP storage value as an attribute of the interface
        //
        iface.setAttribute(SNMP_STORAGE_KEY, storageFlag);
        if (log.isDebugEnabled())
            log.debug("initialize: SNMP storage flag: '" + storageFlag + "'");

        // Retrieve configured value for max number of vars per PDU
        //
        int maxVarsPerPdu = DataCollectionConfigFactory.getInstance().getMaxVarsPerPdu(collectionName);
        if (maxVarsPerPdu == -1) {
            if (log.isEnabledFor(Priority.WARN))
                log.warn("initialize: Configuration error, failed to retrieve max vars per pdu from collection: " + collectionName);
            maxVarsPerPdu = DEFAULT_MAX_VARS_PER_PDU;
        } else if (maxVarsPerPdu == 0) {
            // Special case, zero indicates "no limit" on number of
            // vars in a single PDU...so set maxVarsPerPdu to maximum
            // integer value: Integer.MAX_VALUE. This is a lot
            // easier than building in special logic to handle a
            // value of zero. Doubt anyone will attempt to collect
            // over 2 billion oids.
            //
            maxVarsPerPdu = Integer.MAX_VALUE;
        }

        // Add max vars per pdu value as an attribute of the interface
        //
        iface.setAttribute(MAX_VARS_PER_PDU_STORAGE_KEY, new Integer(maxVarsPerPdu));
        if (log.isDebugEnabled())
            log.debug("initialize: maxVarsPerPdu=" + maxVarsPerPdu);

        // Get database connection in order to retrieve the nodeid,
        // ifIndex and sysoid information from the database for this interface.
        //
        java.sql.Connection dbConn = null;
        try {
            dbConn = DatabaseConnectionFactory.getInstance().getConnection();
        } catch (SQLException sqlE) {
            if (log.isEnabledFor(Priority.ERROR))
                log.error("initialize: Failed getting connection to the database.", sqlE);
            throw new UndeclaredThrowableException(sqlE);
        }

        int nodeID = -1;
        int primaryIfIndex = -1;
        char isSnmpPrimary = DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE;
        boolean snmpv2Supported = false;
        String sysoid = null;

        Map ifMap = new TreeMap();
        NodeInfo nodeInfo = null;

        // All database calls wrapped in try/finally block so we make
        // certain that the connection will be closed when we are
        // finished.
        //
        try {
            // Prepare & execute the SQL statement to get the 'nodeid' from the
            // ipInterface table 'nodeid' will be used to retrieve the node's
            // system object id from the node table.
            // In addition to nodeid, the interface's ifIndex and isSnmpPrimary
            // fields are also retrieved.
            //
            PreparedStatement stmt = null;
            try {
                stmt = dbConn.prepareStatement(SQL_GET_NODEID);
                stmt.setString(1, ipAddr.getHostAddress()); // interface address
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    nodeID = rs.getInt(1);
                    if (rs.wasNull())
                        nodeID = -1;
                    primaryIfIndex = rs.getInt(2);
                    if (rs.wasNull())
                        primaryIfIndex = -1;
                    String str = rs.getString(3);
                    if (str != null)
                        isSnmpPrimary = str.charAt(0);
                } else {
                    nodeID = -1;
                    primaryIfIndex = -1;
                    isSnmpPrimary = DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE;
                }

                rs.close();
            } catch (SQLException sqle) {
                if (log.isDebugEnabled())
                    log.debug("initialize: SQL exception!!", sqle);
                throw new RuntimeException("SQL exception while attempting to retrieve node id for interface " + ipAddr.getHostAddress());
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                    // Ignore
                }
            }

            if (log.isDebugEnabled())
                log.debug("initialize: db retrieval info: nodeid = " + nodeID + ", address = " + ipAddr.getHostAddress() + ", primaryIfIndex = " + primaryIfIndex + ", isSnmpPrimary = " + isSnmpPrimary);

            // RuntimeException is thrown if any of the following are true:
            // - node id is invalid
            // - primaryIfIndex is invalid
            // - Interface is not the primary SNMP interface for the node
            //
            if (nodeID == -1)
                throw new RuntimeException("Unable to retrieve node id for interface " + ipAddr.getHostAddress());

            if (primaryIfIndex == -1)
                // allow this for nodes without ipAddrTables
                // throw new RuntimeException("Unable to retrieve ifIndex for interface " + ipAddr.getHostAddress());
                if (log.isDebugEnabled())
                    log.debug("initialize: db retrieval info: node " + nodeID + " does not have a legitimate primaryIfIndex. Assume node does not supply ipAddrTable and continue...");

            if (isSnmpPrimary != DbIpInterfaceEntry.SNMP_PRIMARY)
                throw new RuntimeException("Interface " + ipAddr.getHostAddress() + " is not the primary SNMP interface for nodeid " + nodeID);

            // Prepare & execute the SQL statement to get the node's
            // system object id (sysoid)
            //
            try {
                stmt = dbConn.prepareStatement(SQL_GET_NODESYSOID);
                stmt.setInt(1, nodeID); // node ID
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    sysoid = rs.getString(1);
                } else {
                    sysoid = null;
                }
                rs.close();
            } catch (SQLException sqle) {
                if (log.isDebugEnabled())
                    log.debug("initialize: SQL exception retrieving the node id", sqle);
                throw new RuntimeException("SQL exception while attempting to retrieve interface's node id");
            } catch (NullPointerException npe) {
                // Thrown by ResultSet.getString() if database query did not
                // return anything
                if (log.isDebugEnabled())
                    log.debug("initialize: NullPointerException", npe);
                sysoid = null;
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                    if (log.isInfoEnabled())
                        log.info("initialize: an error occured trying to close an SQL statement", e);
                }
            }

            if (sysoid == null)
                throw new RuntimeException("System Object ID for interface " + ipAddr.getHostAddress() + " does not exist in the database.");

            // Our implmentation requires that all sysObjectID's must have a
            // leading period ('.').
            // Add the leading period if it is not present.
            //
            if (!sysoid.startsWith(".")) {
                String period = ".";
                period.concat(sysoid);
                sysoid = period;
            }

            // Create the NodeInfo obect for this node
            //
            nodeInfo = new NodeInfo(nodeID, primaryIfIndex);

            // Retrieve list of mib objects to be collected from the
            // remote agent which are to be stored in the node-level RRD file.
            // These objects pertain to the node itself not any individual
            // interfaces.
            List oidList = DataCollectionConfigFactory.getInstance().getMibObjectList(collectionName, sysoid, ipAddr.getHostAddress(), -1);
            nodeInfo.setOidList(oidList);
            List dsList = buildDataSourceList(collectionName, oidList);
            nodeInfo.setDsList(dsList);

            // Add the NodeInfo object as an attribute of the interface
            //
            iface.setAttribute(NODE_INFO_KEY, nodeInfo);

            // Prepare & execute the SQL statement for retrieving the SNMP
            // version of node
            //		
            try {
                stmt = dbConn.prepareStatement(SQL_CHECK_SNMPV2);
                stmt.setInt(1, nodeID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    rs.getInt(1);
                    snmpv2Supported = true;
                } else {
                    snmpv2Supported = false;
                }
                rs.close();
            } catch (SQLException sqle) {
                if (log.isDebugEnabled())
                    log.debug("initialize: SQL exception!!", sqle);
                throw new RuntimeException("SQL exception while attempting to retrieve snmp version information");
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                    if (log.isInfoEnabled())
                        log.info("initialize: an error occured while closing an SQL statement", e);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("initialize: address = " + ipAddr.getHostAddress() + ", nodeid = " + nodeID + ", primaryIfIndex = " + primaryIfIndex + ", isSnmpPrimary = " + isSnmpPrimary + ", SNMPversion = " + (snmpv2Supported ? "SNMPv2" : "SNMPv1"));
            }

            // Build object list for each interface
            //
            // Prepare & execute the SQL statement to retrieve all ifIndex,
            // ifType, ifName, ifDescr, & physAddr values for all valid
            // interfaces for
            // the specified node.
            //
            // For each interface retrieved from the database:
            //
            // 1. Determine the MIB objects to be collected for the
            // interface as well as the corresponding RRD data source list.
            // 2. Create the RRD file to hold data retrieved for the interface.
            // 3. Add the interface to the interface map for retrieval during
            // the poll.
            PreparedStatement stmt1 = null;
            try {
                stmt = dbConn.prepareStatement(SQL_GET_SNMP_INFO);
                stmt.setInt(1, nodeID);
                ResultSet rs = stmt.executeQuery();

                // The following code does a database lookup on the ipinterface
                // table
                // and builds a Map of ifIndex and issnmpprimary values. The
                // issnmpprimary
                // value can then be checked to see if SNMP collection needs to
                // be done on it.

                stmt1 = dbConn.prepareStatement(SQL_GET_ISSNMPPRIMARY);
                stmt1.setInt(1, nodeID); // interface address
                ResultSet rs1 = stmt1.executeQuery();

                if (log.isDebugEnabled())
                    log.debug("initialize: Attempting to get issnmpprimary information for node: " + nodeID);

                HashMap snmppriMap = new HashMap();

                while (rs1.next()) {
                    String snmppriIfIndex = rs1.getString(1);
                    String snmppriCollType = rs1.getString(2);

                    String currSNMPPriValue = (String) snmppriMap.get(snmppriIfIndex);

                    if (currSNMPPriValue == null)
                        snmppriMap.put(snmppriIfIndex, snmppriCollType);
                    else if (currSNMPPriValue.equals("P"))
                        continue;
                    else if (currSNMPPriValue.equals("S") && snmppriCollType.equals("P"))
                        snmppriMap.put(snmppriIfIndex, snmppriCollType);
                    else if (currSNMPPriValue.equals("C") && (snmppriCollType.equals("P") || snmppriCollType.equals("S")))
                        snmppriMap.put(snmppriIfIndex, snmppriCollType);
                    else
                        snmppriMap.put(snmppriIfIndex, snmppriCollType);
                }
                rs1.close();

                while (rs.next()) {
                    // Extract retrieved database values from
                    // the result set
                    int index = rs.getInt(1);
                    int type = rs.getInt(2);
                    String name = rs.getString(3);
                    String descr = rs.getString(4);
                    String physAddr = rs.getString(5);
                    if (log.isDebugEnabled())
                        log.debug("initialize: snmpifindex = " + index + ", snmpifname = " + name + ", snmpifdescr = " + descr + ", snmpphysaddr = -" + physAddr + "-");

                    // Determine the label for this interface.
                    // The label will be used to create the RRD file
                    // name which holds SNMP data retreived from the
                    // remote agent.
                    //
                    // If available ifName is used to generate the label
                    // since it is guaranteed to be unique. Otherwise
                    // ifDescr is used. In either case, all non
                    // alpha numeric characters are converted to
                    // underscores to ensure that the resuling string
                    // will make a decent file name and that RRD
                    // won't have any problems using it
                    //
                    String label = null;
                    if (name != null) {
                        label = AlphaNumeric.parseAndReplace(name, '_');
                    } else if (descr != null) {
                        label = AlphaNumeric.parseAndReplace(descr, '_');
                    } else {
                        log.warn("Interface (ifIndex/nodeId=" + index + "/" + nodeID + ") has no ifName and no ifDescr...setting to label to 'no_ifLabel'.");
                        label = "no_ifLabel";
                    }

                    // In order to assure the uniqueness of the
                    // RRD file names we now append the MAC/physical
                    // address to the end of label if it is available.
                    // 
                    if (physAddr != null) {
                        physAddr = AlphaNumeric.parseAndTrim(physAddr);
                        if (physAddr.length() == 12) {
                            label = label + "-" + physAddr;
                        } else {
                            if (log.isDebugEnabled())
                                log.debug("initialize: physical address len is NOT 12, physAddr=" + physAddr);
                        }
                    }

                    if (log.isDebugEnabled())
                        log.debug("initialize: ifLabel = '" + label + "'");

                    // Create new IfInfo object
                    //

                    String collType = (String) snmppriMap.get(rs.getString(1));

                    IfInfo ifInfo = new IfInfo(index, type, label, collType);

                    if (index == primaryIfIndex) {
                        ifInfo.setIsPrimary(true);
                    } else {
                        ifInfo.setIsPrimary(false);
                    }

                    // Retrieve list of mib objects to be collected from
                    // the remote agent for this interface.
                    //
                    oidList = DataCollectionConfigFactory.getInstance().getMibObjectList(collectionName, sysoid, ipAddr.getHostAddress(), type);

                    // Now build a list of RRD data source objects from
                    // the list of mib objects
                    //
                    dsList = buildDataSourceList(collectionName, oidList);

                    // Set MIB object and data source lists in IfInfo object
                    //
                    ifInfo.setOidList(oidList);
                    ifInfo.setDsList(dsList);

                    // Add the new IfInfo object to the interface map keyed by
                    // interface index
                    //
                    ifMap.put(new Integer(index), ifInfo);
                }
                rs.close();
            } catch (SQLException sqle) {
                if (log.isDebugEnabled())
                    log.debug("initialize: SQL exception!!", sqle);
                throw new RuntimeException("SQL exception while attempting to retrieve snmp interface info");
            } catch (NullPointerException npe) {
                // Thrown by ResultSet.getString() if database query did not
                // return anything
                //
                if (log.isDebugEnabled())
                    log.debug("initialize: NullPointerException", npe);
                throw new RuntimeException("NullPointerException while attempting to retrieve snmp interface info");
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                    if (log.isInfoEnabled())
                        log.info("initialize: an error occured trying to close an SQL statement", e);
                }
            }

            // Verify that we did find at least one eligible interface for the
            // node
            //
            if (ifMap.size() < 1)
                throw new RuntimeException("Failed to retrieve any eligible interfaces for node " + nodeID + " from the database.");

            // Add the ifMap object as an attribute of the interface
            //
            iface.setAttribute(IF_MAP_KEY, ifMap);

            // Verify that there is something to collect from this
            // primary SMP interface. If no node objects and no
            // interface objects then throw exception
            //
            if (nodeInfo.getOidList().isEmpty()) {
                boolean hasInterfaceOids = false;
                Iterator iter = ifMap.values().iterator();
                while (iter.hasNext() && !hasInterfaceOids) {
                    IfInfo ifInfo = (IfInfo) iter.next();
                    if (!ifInfo.getOidList().isEmpty())
                        hasInterfaceOids = true;
                }

                if (!hasInterfaceOids) {
                    throw new RuntimeException("collection '" + collectionName + "' defines nothing to collect for " + ipAddr.getHostAddress());
                }
            }
        } finally {
            // Done with the database so close the connection
            try {
                dbConn.close();
            } catch (SQLException sqle) {
                if (log.isEnabledFor(Priority.INFO))
                    log.info("initialize: SQLException while closing database connection", sqle);
            }
        }

        if (log.isDebugEnabled())
            log.debug("initialize: address = " + ipAddr.getHostAddress() + ", nodeID = " + nodeID + ", ifIndex = " + primaryIfIndex + ", sysoid = " + sysoid);

        // Instantiate new SnmpPeer object for this interface
        //

        if (log.isDebugEnabled())
            log.debug("initialize: initialization completed for " + ipAddr.getHostAddress());
        return;
    }

    /**
     * Responsible for releasing any resources associated with the specified
     * interface.
     * 
     * @param iface
     *            Network interface to be released.
     */
    public void release(NetworkInterface iface) {
        // Nothing to release...
    }

    /**
     * Perform data collection.
     * 
     * @param iface
     *            Network interface to be data collected.
     * @param eproxy
     *            Eventy proxy for sending events.
     * @param parameters
     *            Key/value pairs from the package to which the interface
     *            belongs.
     */
    public int collect(NetworkInterface iface, EventProxy eproxy, Map parameters) {
        try {
            
            // -----------------------------------------------------------
            // 
            // Collect node and interface MIB data from the remote agent
            //
            // -----------------------------------------------------------
            SnmpNodeCollector nodeCollector = null;
            // construct the nodeCollector
            if (!getNodeInfo(iface).getOidList().isEmpty()) {
                nodeCollector = new SnmpNodeCollector(getInetAddress(iface), getNodeInfo(iface).getOidList());
            }
            
            IfNumberTracker ifNumber = null;
            SnmpIfCollector ifCollector = null;
            // construct the ifCollector
            if (hasInterfaceOids(iface)) {
                ifCollector = new SnmpIfCollector(getInetAddress(iface), getIfMap(iface));
                ifNumber = new IfNumberTracker();
            } 
            

            collectData(iface, ifNumber, nodeCollector, ifCollector);
            
            if (hasInterfaceOids(iface)) {
                int savedIfCount = getSavedIfCount(iface);
                
                int ifCount = ifNumber.getIfNumber();
                
                saveIfCount(iface, ifCount);
                
                log().debug("collect: interface: " + getHostAddress(iface) + " ifCount: " + ifCount + " savedIfCount: " + savedIfCount);
                
                // If saved interface count differs from the newly retreived
                // interface count the following must occur:
                // 
                // 1. generate forceRescan event so Capsd will rescan the
                // node, update the database, and generate the appropriate
                // events back to the poller.
                // 
                if (savedIfCount != -1) {
                    if (ifCount != savedIfCount) {
                        log().info("Number of interfaces on primary SNMP interface " + getHostAddress(iface) + " has changed, generating 'ForceRescan' event. ");
                        generateForceRescanEvent(getHostAddress(iface), eproxy);
                    }
                }
            }
            
            // Update RRD with values retrieved in SNMP collection
            boolean rrdError = updateRRDs(getCollectionName(parameters), iface, nodeCollector, ifCollector);
            
            if (rrdError) {
                log().warn("collect: RRD error during update for " + getHostAddress(iface));
            }
            
            //
            // return the status of the collection
            //
            return COLLECTION_SUCCEEDED;
        } catch (CollectionError e) {
            if (e.getCause() == null)
                log().error(e.getMessage());
            else
                log().error(e.getMessage(), e.getCause());
            return COLLECTION_FAILED;
        } catch (CollectionWarning e) {
            if (e.getCause() == null)
                log().warn(e.getMessage());
            else
                log().warn(e.getMessage(), e.getCause());
            return COLLECTION_FAILED;
        } catch (Throwable t) {
            log().error("Unexpected error during node SNMP collection for " + getHostAddress(iface), t);
            return COLLECTION_FAILED;
        }
    }

    private void collectData(NetworkInterface iface, CollectionTracker ifNumber, SnmpNodeCollector nodeCollector, SnmpIfCollector ifCollector) throws CollectionWarning {
        try {
            InetAddress address = getInetAddress(iface);
            List trackers = new ArrayList(3);
            if (ifNumber != null) trackers.add(ifNumber);
            if (nodeCollector != null) trackers.add(nodeCollector);
            if (ifCollector != null) trackers.add(ifCollector);
            
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
            
            // now collect the data
            SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "SnmpCollectors for "+address.getHostAddress(), (CollectionTracker[]) trackers.toArray(new CollectionTracker[trackers.size()]));
            walker.start();

            if (log().isDebugEnabled())
                log().debug("collect: successfully instantiated SnmpNodeCollector() for " + getHostAddress(iface));
            
            // wait for collection to finish
            walker.waitFor();
            
            if (log().isDebugEnabled())
                log().debug("collect: node SNMP query for address " + getHostAddress(iface) + " complete.");

            // Was the node collection successful?
            //
            if (walker.failed()) {
                // Log error and return COLLECTION_FAILED
                //
                throw new CollectionWarning("collect: collection failed for " + getHostAddress(iface));
                
            }
            setMaxVarsPdu(iface, walker.getMaxVarsPerPdu());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CollectionWarning("collect: Collection of node SNMP data for interface " + getHostAddress(iface) + " interrupted!", e);
        }
    }
    
    private void setMaxVarsPdu(NetworkInterface iface, int maxVarsPerPdu) {
        iface.setAttribute(MAX_VARS_PER_PDU_STORAGE_KEY, new Integer(maxVarsPerPdu));
    }

    private void saveIfCount(NetworkInterface iface, int ifCount) {
        // Add the interface count to the interface's attributes for
        // retrieval during poll()
        iface.setAttribute(INTERFACE_COUNT_KEY, new Integer(ifCount));
    }

    private int getSavedIfCount(NetworkInterface iface) {
        int savedIfCount = -1;
        Integer tmp = (Integer) iface.getAttribute(INTERFACE_COUNT_KEY);
        if (tmp != null)
            savedIfCount = tmp.intValue();
        return savedIfCount;
    }

    private boolean hasInterfaceOids(NetworkInterface iface) throws CollectionError {
        boolean hasInterfaceOids = false;
        Iterator iter = getIfMap(iface).values().iterator();
        while (iter.hasNext() && !hasInterfaceOids) {
            IfInfo ifInfo = (IfInfo) iter.next();
            if (ifInfo.getType() < 1)
            		continue;
            if (!ifInfo.getOidList().isEmpty())
                hasInterfaceOids = true;
        }
        return hasInterfaceOids;
    }

    private Map getIfMap(NetworkInterface iface) throws CollectionError {
        Map ifMap = (Map) iface.getAttribute(IF_MAP_KEY);
        if (ifMap == null)
            throw new CollectionError("Interface map not available for interface " + getHostAddress(iface));
        return ifMap;
    }

    private NodeInfo getNodeInfo(NetworkInterface iface) throws CollectionError {
        NodeInfo nodeInfo = (NodeInfo) iface.getAttribute(NODE_INFO_KEY);
        if (nodeInfo == null)
            throw new CollectionError("Node info not available for interface " + getHostAddress(iface));
        return nodeInfo;
    }

    private String getHostAddress(NetworkInterface iface) {
        return getInetAddress(iface).getHostAddress();
    }

    private int getMaxVarsPerPdu(NetworkInterface iface) {
        int maxVarsPerPdu = ((Integer) iface.getAttribute(MAX_VARS_PER_PDU_STORAGE_KEY)).intValue();
        return maxVarsPerPdu;
    }

    private InetAddress getInetAddress(NetworkInterface iface) {
        InetAddress ipaddr = (InetAddress) iface.getAddress();
        return ipaddr;
    }

    private String getCollectionName(Map parameters) {
        String collectionName = ParameterMap.getKeyedString(parameters, "collection", "default");
        return collectionName;
    }

    /**
     * This method is responsible for building an RRDTool style 'update' command
     * which is issued via the RRD JNI interface in order to push the latest
     * SNMP-collected values into the interface's RRD database.
     * 
     * @param collectionName
     *            SNMP data Collection name from 'datacollection-config.xml'
     * @param iface
     *            NetworkInterface object of the interface currently being
     *            polled
     * @param nodeCollector
     *            Node level MIB data collected via SNMP for the polled
     *            interface
     * @param ifCollector
     *            Interface level MIB data collected via SNMP for the polled
     *            interface
     * @throws CollectionError 
     * 
     * @exception RuntimeException
     *                Thrown if the data source list for the interface is null.
     */
    private boolean updateRRDs(String collectionName, NetworkInterface iface, SnmpNodeCollector nodeCollector, SnmpIfCollector ifCollector) throws CollectionError {
        // Log4j category
        //
        InetAddress ipaddr = getInetAddress(iface);
        
        // Retrieve SNMP storage attribute
        String snmpStorage = getSnmpStorage(iface);
        
        // Get primary interface index from NodeInfo object
        NodeInfo nodeInfo = getNodeInfo(iface);
        int primaryIfIndex = nodeInfo.getPrimarySnmpIfIndex();
        
        // Retrieve interface map attribute
        //
        Map ifMap = getIfMap(iface);
        
        // Write relevant collected SNMP statistics to RRD database
        // 
        // First the node level RRD info will be updated.
        // Secondly the interface level RRD info will be updated.
        //
        boolean rrdError = false;
        
        // -----------------------------------------------------------
        // Node data
        // -----------------------------------------------------------
        if (nodeCollector != null) {
            log().debug("updateRRDs: processing node-level collection...");
            
            // Build path to node RRD repository. createRRD() will make the
            // appropriate directories if they do not already exist.
            //
            String nodeRepository = m_rrdPath + File.separator + String.valueOf(nodeInfo.getNodeId());
            
            SNMPCollectorEntry nodeEntry = nodeCollector.getEntry();
            
            // Iterate over the node datasource list and issue RRD update
            // commands to update each datasource which has a corresponding
            // value in the collected SNMP data
            //
            Iterator iter = nodeInfo.getDsList().iterator();
            while (iter.hasNext()) {
                DataSource ds = (DataSource) iter.next();
                
                try {
                    
                    String dsVal = getRRDValue(ds, nodeEntry);
                    if (dsVal == null) {
                        // Do nothing, no update is necessary
                        if (log().isDebugEnabled())
                            log().debug("updateRRDs: Skipping update, no data retrieved for nodeId: " + nodeInfo.getNodeId() + " datasource: " + ds.getName());
                    } else {
                        //createRRD(collectionName, ipaddr, nodeRepository, ds);
                        if(ds.performUpdate(collectionName, ipaddr.getHostAddress(), nodeRepository, ds.getName(), dsVal)) {
                            log().warn("updateRRDs: ds.performUpdate() failed for node: " + nodeInfo.getNodeId() + " datasource: " + ds.getName());
                            rrdError = true;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log().warn("getRRDValue: " + e.getMessage());
                    // Set rrdError flag
                    rrdError = true;
                    log().warn("updateRRDs: call to getRRDValue() failed for node: " + nodeInfo.getNodeId() + " datasource: " + ds.getName());
                }
                
            } // end while(more datasources)
        } // end if(nodeCollector != null)
        
        // -----------------------------------------------------------
        // Interface-specific data
        // -----------------------------------------------------------
        
        if (ifCollector != null) {
            // Retrieve list of SNMP collector entries generated for the
            // remote node's interfaces.
            //
            List snmpCollectorEntries = ifCollector.getEntries();
            if (snmpCollectorEntries == null || snmpCollectorEntries.size() == 0) {
                log().warn("updateRRDs:  No data retrieved for the interface " + ipaddr.getHostAddress());
            }
            
            // Iterate over the SNMP collector entries
            //
            Iterator iter = snmpCollectorEntries.iterator();
            while (iter.hasNext()) {
                SNMPCollectorEntry ifEntry = (SNMPCollectorEntry) iter.next();
                
                int ifIndex = ifEntry.getIfIndex().intValue();
                
                
                
                // Are we storing SNMP data for all interfaces or primary
                // interface only?
                // If only storing for primary interface only proceed if current
                // ifIndex is equal to the ifIndex of the primary SNMP interface
                if (snmpStorage.equals(SNMP_STORAGE_PRIMARY)) {
                    if (ifIndex != primaryIfIndex) {
                        if (log().isDebugEnabled())
                            log().debug("updateRRDs: only storing SNMP data for primary interface (" + primaryIfIndex + "), skipping ifIndex: " + ifIndex);
                        continue;
                    }
                }
                
                // Use ifIndex to lookup the IfInfo object from the interface
                // map
                //
                IfInfo ifInfo = (IfInfo) ifMap.get(new Integer(ifIndex));
                if (ifInfo == null) {
                    // no data needed for this interface
                    continue;
                }
                
                if (ifInfo.getCollType() == null){
                    log().warn("updateRRDs: No SNMP info for ifIndex: " + ifIndex);
                    continue;
                }
                
                if (snmpStorage.equals(SNMP_STORAGE_SELECT)) {
                    if (ifInfo.getCollType() == null || ifInfo.getCollType().equals("N")) {
                        if (log().isDebugEnabled())
                            log().debug("updateRRDs: selectively storing SNMP data for primary interface (" + primaryIfIndex + "), skipping ifIndex: " + ifIndex + " because collType = "+ifInfo.getCollType());
                        continue;
                    }
                }
                
                if (ifInfo.getDsList() == null)
                    throw new RuntimeException("Data Source list not available for primary IP addr " + ipaddr.getHostAddress() + " and ifIndex " + ifInfo.getIndex());
                
                // Iterate over the interface datasource list and issue RRD
                // update
                // commands to update each datasource which has a corresponding
                // value in the collected SNMP data
                //
                Iterator i = ifInfo.getDsList().iterator();
                while (i.hasNext()) {
                    DataSource ds = (DataSource) i.next();
                    
                    // Build path to interface RRD repository. createRRD() will
                    // make the
                    // appropriate directories if they do not already exist.
                    //
                    String ifRepository = m_rrdPath + File.separator + String.valueOf(nodeInfo.getNodeId()) + File.separator + ifInfo.getLabel();
                    
                    try {
                        
                        String dsVal = getRRDValue(ds, ifEntry);
                        
                        // Build RRD update command
                        //
                        if (dsVal == null) {
                            // Do nothing, no update is necessary
                            if (log().isDebugEnabled())
                                log().debug("updateRRDs: Skipping update, no data retrieved for node/ifindex: " + nodeInfo.getNodeId() + "/" + ifIndex + " datasource: " + ds.getName());
                        } else {
                            // Call createRRD() to create RRD if it doesn't
                            // already exist
                            //
                            //createRRD(collectionName, ipaddr, ifRepository, ds);
                            if(ds.performUpdate(collectionName, ipaddr.getHostAddress(), ifRepository, ds.getName(), dsVal)) {
                                log().warn("updateRRDs: ds.performUpdate() failed for node/ifindex: " + nodeInfo.getNodeId() + "/" + ifIndex + " datasource: " + ds.getName());
                                rrdError = true;
                            }
                            
                        }
                    } catch (IllegalArgumentException e) {
                        log().warn("buildRRDUpdateCmd: " + e.getMessage());
                        // Set rrdError flag
                        rrdError = true;
                        log().warn("updateRRDs: call to buildRRDUpdateCmd() failed for node/ifindex: " + nodeInfo.getNodeId() + "/" + ifIndex + " datasource: " + ds.getName());
                    }
                    
                } // end while(more datasources)
            } // end while(more SNMP collector entries)
        } // end if(ifCollector != null)
        return rrdError;
    }

    private String getSnmpStorage(NetworkInterface iface) {
        String snmpStorage = (String) iface.getAttribute(SNMP_STORAGE_KEY);
        return snmpStorage;
    }
    
    /**
     * @param ds
     * @param collectorEntry
     * @param log
     * @param dsVal
     * @return
     * @throws Exception
     */
    public String getRRDValue(DataSource ds, SNMPCollectorEntry collectorEntry) throws IllegalArgumentException {
        Category log = log();
        String dsVal = null;

        // Make sure we have an actual object id value.
        if (ds.getOid() == null)
            return null;

        String instance = null;
        if (ds.getInstance().equals(MibObject.INSTANCE_IFINDEX))
            instance = collectorEntry.getIfIndex().toString();
        else
            instance = ds.getInstance();

        String fullOid = SnmpObjId.get(ds.getOid(), instance).toString();

        SnmpValue snmpVar = collectorEntry.getValue(fullOid);
        if (snmpVar == null)
            // No value retrieved matching this oid
            return null;

        if (log.isDebugEnabled())
            log.debug("issueRRDUpdate: name:oid:value -  " + ds.getName() + ":" + fullOid + ":" + snmpVar.toString());
	
	return ds.getStorableValue(snmpVar);
    }

    /**
     * This method is responsible for building a list of RRDDataSource objects
     * from the provided list of MibObject objects.
     * 
     * @param collectionName
     *            Collection name
     * @param oidList
     *            List of MibObject objects defining the oid's to be collected
     *            via SNMP.
     * 
     * @return list of RRDDataSource objects
     */
    private List buildDataSourceList(String collectionName, List oidList) {
        // Log4j category
        //
        Category log = log();

        // Retrieve the RRD expansion data source list which contains all
        // the expansion data source's. Use this list as a basis
        // for building a data source list for the current interface.
        //
        List dsList = new LinkedList();

        // Loop through the MIB object list to be collected for this interface
        // and add a corresponding RRD data source object. In this manner
        // each interface will have RRD files create which reflect only the data
        // sources pertinent to it.
        //
        Iterator o = oidList.iterator();
        while (o.hasNext()) {
            MibObject obj = (MibObject) o.next();
	    DataSource ds = DataSource.dataSourceForMibObject(obj, collectionName);
	    if(ds!=null) {
		// Add the new data source to the list
		dsList.add(ds);
	    } else if(log.isEnabledFor(Priority.WARN)) {
		log.warn("buildDataSourceList: Data type '" + obj.getType() + "' not supported.");
		log.warn("buildDataSourceList: MIB object '" + obj.getAlias() + "' will not be mapped to a data source.");
	    }

        }

        return dsList;
    }

    /**
     * This method is responsible for building a Capsd forceRescan event object
     * and sending it out over the EventProxy.
     * 
     * @param ifAddress
     *            interface address to which this event pertains
     * @param eventProxy
     *            proxy over which an event may be sent to eventd
     */
    private void generateForceRescanEvent(String ifAddress, EventProxy eventProxy) {
        // Log4j category
        //
        Category log = log();

        if (log.isDebugEnabled())
            log.debug("generateForceRescanEvent: interface = " + ifAddress);

        // create the event to be sent
        Event newEvent = new Event();

        newEvent.setUei(EventConstants.FORCE_RESCAN_EVENT_UEI);

        newEvent.setSource("SNMPServiceMonitor");

        newEvent.setInterface(ifAddress);

        newEvent.setService(SERVICE_NAME);

        if (m_host != null)
            newEvent.setHost(m_host);

        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Send event via EventProxy
        try {
            eventProxy.send(newEvent);
        } catch (EventProxyException e) {
            if (log.isEnabledFor(Priority.ERROR))
                log.error("generateForceRescanEvent: Unable to send forceRescan event.", e);
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(SnmpCollector.class);
    }
}
