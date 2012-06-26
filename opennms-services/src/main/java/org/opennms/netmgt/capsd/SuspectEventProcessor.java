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

package org.opennms.netmgt.capsd;

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.IfCollector.SupportedProtocol;
import org.opennms.netmgt.capsd.snmp.IfTable;
import org.opennms.netmgt.capsd.snmp.IfTableEntry;
import org.opennms.netmgt.capsd.snmp.IfXTableEntry;
import org.opennms.netmgt.capsd.snmp.IpAddrTable;
import org.opennms.netmgt.capsd.snmp.SystemGroup;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.capsd.DbIfServiceEntry;
import org.opennms.netmgt.model.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.model.capsd.DbNodeEntry;
import org.opennms.netmgt.model.capsd.DbSnmpInterfaceEntry;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;

/**
 * This class is designed to scan/capability check a suspect interface, update
 * the database based on the information collected from the device, and
 * generate events necessary to notify the other OpenNMS services. The
 * constructor takes a string which is the IP address of the interface to be
 * scanned.
 * 
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class SuspectEventProcessor implements Runnable {
    private static final String EVENT_SOURCE = "OpenNMS.Capsd";

    /**
     * SQL statement to retrieve the node identifier for a given IP address
     */
    private static String SQL_RETRIEVE_INTERFACE_NODEID_PREFIX = "SELECT nodeId FROM ipinterface WHERE ";

    /**
     * SQL statement to retrieve the ipaddresses for a given node ID
     */
    private final static String SQL_RETRIEVE_IPINTERFACES_ON_NODEID = "SELECT ipaddr FROM ipinterface WHERE nodeid = ? and ismanaged != 'D'";

    /**
     * IP address of new suspect interface
     */
    String m_suspectIf;

    private CapsdDbSyncer m_capsdDbSyncer;

    private PluginManager m_pluginManager;
    
    private static Set<String> m_queuedSuspectTracker;

    /**
     * Constructor.
     * @param capsdDbSyncer for querying the database
     * @param pluginManager for accessing plugins
     * @param ifAddress
     *            Suspect interface address.
     */
    SuspectEventProcessor(CapsdDbSyncer capsdDbSyncer, PluginManager pluginManager, String ifAddress) {
        Assert.notNull(capsdDbSyncer, "The capsdDbSyncer argument cannot be null");
        Assert.notNull(pluginManager, "The pluginManager argument cannot be null");
        Assert.notNull(ifAddress, "The ifAddress argument cannot be null");

        m_capsdDbSyncer = capsdDbSyncer;
        m_pluginManager = pluginManager;
        m_suspectIf = ifAddress;
        
        // Add the interface address to the Set that tracks suspect
        // scans in the queue
        synchronized (m_queuedSuspectTracker) {
        	m_queuedSuspectTracker.add(ifAddress);
        }
    }

    /**
     * This method is responsible for determining if a node already exists in
     * the database for the current interface. If the IfCollector object
     * contains a valid SNMP collection, an attempt will be made to look up in
     * the database each interface contained in the SNMP collection's ifTable.
     * If an interface is found to already exist in the database a DbNodeEntry
     * object will be created from it and returned. If the IfCollector object
     * does not contain a valid SNMP collection or if none of the interfaces
     * exist in the database null is returned.
     * 
     * @param dbc
     *            Connection to the database.
     * @param collector
     *            Interface collector object
     * @return dbNodeEntry Returns null if a node does not already exist in
     *         the database, otherwise returns the DbNodeEntry object for the
     *         node under which the current interface/IP address should be
     *         added.
     * @throws SQLException
     *             Thrown if an error occurs retrieving the parent nodeid from
     *             the database.
     */
    private DbNodeEntry getExistingNodeEntry(java.sql.Connection dbc,
            IfCollector collector) throws SQLException {
        if (log().isDebugEnabled())
            log().debug("getExistingNodeEntry: checking for current target: "
                    + collector.getTarget());

        // Do we have any additional interface information collected via SNMP?
        // If not simply return, there is nothing to check
        if (!collector.hasSnmpCollection()
                || collector.getSnmpCollector().failed())
            return null;

        // Next verify that ifTable and ipAddrTable entries were collected
        IfSnmpCollector snmpc = collector.getSnmpCollector();
        IfTable ifTable = null;
        IpAddrTable ipAddrTable = null;

        if (snmpc.hasIfTable())
            ifTable = snmpc.getIfTable();

        if (snmpc.hasIpAddrTable())
            ipAddrTable = snmpc.getIpAddrTable();

        if (ifTable == null || ipAddrTable == null)
            return null;

        // SQL statement prefix
        StringBuffer sqlBuffer = new StringBuffer(SQL_RETRIEVE_INTERFACE_NODEID_PREFIX);
        boolean firstAddress = true;

        // Loop through the interface table entries and see if any already
        // exist in the database.
        List<String> ipaddrsOfNewNode = new ArrayList<String>();
        List<String> ipaddrsOfOldNode = new ArrayList<String>();

        for (IfTableEntry ifEntry : ifTable) {

            if (ifEntry.getIfIndex() == null) {
                log().debug("getExistingNodeEntry:  Breaking from loop");
                break;
            }

            //
            // Get ifIndex
            //
            int ifIndex = ifEntry.getIfIndex().intValue();


            //
            // Get ALL IP Addresses for this ifIndex
            //
            List<InetAddress> ipAddrs = ipAddrTable.getIpAddresses(ifIndex);
            if (log().isDebugEnabled())
                log().debug("getExistingNodeEntry: number of interfaces retrieved for ifIndex "
                        + ifIndex + " is: " + ipAddrs.size());

            // Iterate over IP address list and add each to the sql buffer
            //
            for(InetAddress ipAddress : ipAddrs) {

                // 
                // Skip interface if no IP address or if IP address is
                // "0.0.0.0"
                // or if this interface is of type loopback
                if (ipAddress == null
                        || str(ipAddress).equals("0.0.0.0")
                        || ipAddress.isLoopbackAddress())
                    continue;

                if (firstAddress) {
                    sqlBuffer.append("ipaddr='").append(
                                                        str(ipAddress)).append(
                                                                                           "'");
                    firstAddress = false;
                } else
                    sqlBuffer.append(" OR ipaddr='").append(
                                                            str(ipAddress)).append(
                                                                                               "'");

                ipaddrsOfNewNode.add(str(ipAddress));
            }
        } // end while

        // Make sure we added at least one address to the SQL query
        //
        if (firstAddress)
            return null;

        // Prepare the db statement in advance
        //
        if (log().isDebugEnabled())
            log().debug("getExistingNodeEntry: issuing SQL command: "
                    + sqlBuffer.toString());

        int nodeID = -1;
        PreparedStatement stmt;
        final DBUtils d = new DBUtils(getClass());

        try {
            stmt = dbc.prepareStatement(sqlBuffer.toString());
            d.watch(stmt);

            // Do any of the IP addrs already exist in the database under another node?
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            if (rs.next()) {
                nodeID = rs.getInt(1);
                if (log().isDebugEnabled())
                    log().debug("getExistingNodeEntry: target "
                            + str(collector.getTarget()) + nodeID);
                rs = null;
            }
        } finally {
            d.cleanUp();
        }

        if (nodeID == -1)
            return null;

        try {
            stmt = dbc.prepareStatement(SQL_RETRIEVE_IPINTERFACES_ON_NODEID);
            d.watch(stmt);
            stmt.setInt(1, nodeID);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                String ipaddr = rs.getString(1);
                if (!ipaddr.equals("0.0.0.0"))
                    ipaddrsOfOldNode.add(ipaddr);
            }
        } finally {
            d.cleanUp();
        }

        if (ipaddrsOfNewNode.containsAll(ipaddrsOfOldNode)) {
            if (log().isDebugEnabled())
                log().debug("getExistingNodeEntry: found one of the addrs under existing node: " + nodeID);
            return DbNodeEntry.get(nodeID);
        } else {
            String dupIpaddr = getDuplicateIpaddress(ipaddrsOfOldNode, ipaddrsOfNewNode);
            createAndSendDuplicateIpaddressEvent(nodeID, dupIpaddr);
            return null;
        }
    }

	/**
     * This method is used to verify if there is a same ipaddress existing in
     * two sets of ipaddresses, and return the first ipaddress that is the
     * same in both sets as a string.
     * 
     * @param ipListA
     *            a collection of ip addresses.
     * @param ipListB
     *            a collection of ip addresses.
     * @return the first ipaddress exists in both ipaddress lists.
     */
    private String getDuplicateIpaddress(List<String> ipListA, List<String> ipListB) {
        if (ipListA == null || ipListB == null)
            return null;

        String ipaddr = null;
        Iterator<String> iter = ipListA.iterator();
        while (iter.hasNext()) {
            ipaddr = iter.next();
            if (ipListB.contains(ipaddr)) {
                if (log().isDebugEnabled())
                    log().debug("getDuplicateIpaddress: get duplicate ip address: "
                            + ipaddr);
                break;
            } else
                ipaddr = null;
        }
        return ipaddr;
    }

    /**
     * This method is responsble for inserting a new node into the node table.
     * 
     * @param dbc
     *            Database connection.
     * @param ifaddr
     *            Suspect interface
     * @param collector
     *            Interface collector containing SMB and SNMP info collected
     *            from the remote device.
     * @return DbNodeEntry object associated with the newly inserted node
     *         table entry.
     * @throws SQLException
     *             if an error occurs inserting the new node.
     */
    private DbNodeEntry createNode(Connection dbc, InetAddress ifaddr,
            IfCollector collector) throws SQLException {
        // Determine primary interface for the node. Primary interface
        // is needed for determining the node label.
        //
        InetAddress primaryIf = determinePrimaryInterface(collector);

        // Get Snmp and Smb collector objects
        //
        IfSnmpCollector snmpc = collector.getSnmpCollector();
        IfSmbCollector smbc = collector.getSmbCollector();

        // First create a node entry for the new interface
        //
        DbNodeEntry entryNode = DbNodeEntry.create();

        // fill in the node information
        //
        Date now = new Date();
        entryNode.setCreationTime(now);
        entryNode.setLastPoll(now);
        entryNode.setNodeType(DbNodeEntry.NODE_TYPE_ACTIVE);
        entryNode.setLabel(primaryIf.getHostName());
        if (entryNode.getLabel().equals(str(primaryIf)))
            entryNode.setLabelSource(DbNodeEntry.LABEL_SOURCE_ADDRESS);
        else
            entryNode.setLabelSource(DbNodeEntry.LABEL_SOURCE_HOSTNAME);

        if (snmpc != null) {
            if (snmpc.hasSystemGroup()) {
                SystemGroup sysgrp = snmpc.getSystemGroup();

                // sysObjectId
                String sysObjectId = sysgrp.getSysObjectID();
                if (sysObjectId != null)
                    entryNode.setSystemOID(sysObjectId);
                else
                    log().warn("SuspectEventProcessor: "
                            + str(ifaddr)
                            + " has NO sysObjectId!!!!");

                // sysName
                String str = sysgrp.getSysName();
                if (log().isDebugEnabled())
                    log().debug("SuspectEventProcessor: "
                            + str(ifaddr) + " has sysName: "
                            + str);

                if (str != null && str.length() > 0) {
                    entryNode.setSystemName(str);

                    // Hostname takes precedence over sysName so only replace
                    // label if
                    // hostname was not available.
                    if (entryNode.getLabelSource() == DbNodeEntry.LABEL_SOURCE_ADDRESS) {
                        entryNode.setLabel(str);
                        entryNode.setLabelSource(DbNodeEntry.LABEL_SOURCE_SYSNAME);
                    }
                }

                // sysDescription
                str = sysgrp.getSysDescr();
                if (log().isDebugEnabled())
                    log().debug("SuspectEventProcessor: "
                            + str(ifaddr)
                            + " has sysDescription: " + str);
                if (str != null && str.length() > 0)
                    entryNode.setSystemDescription(str);

                // sysLocation
                str = sysgrp.getSysLocation();
                if (log().isDebugEnabled())
                    log().debug("SuspectEventProcessor: "
                            + str(ifaddr) + " has sysLocation: "
                            + str);
                if (str != null && str.length() > 0)
                    entryNode.setSystemLocation(str);

                // sysContact
                str = sysgrp.getSysContact();
                if (log().isDebugEnabled())
                    log().debug("SuspectEventProcessor: "
                            + str(ifaddr) + " has sysContact: "
                            + str);
                if (str != null && str.length() > 0)
                    entryNode.setSystemContact(str);
            }
        }

        // check for SMB information
        //
        if (smbc != null) {
            // Netbios Name and Domain
            // Note: only override if the label source is not HOSTNAME
            if (smbc.getNbtName() != null
                    && entryNode.getLabelSource() != DbNodeEntry.LABEL_SOURCE_HOSTNAME) {
                entryNode.setLabel(smbc.getNbtName());
                entryNode.setLabelSource(DbNodeEntry.LABEL_SOURCE_NETBIOS);
                entryNode.setNetBIOSName(entryNode.getLabel());
                if (smbc.getDomainName() != null) {
                    entryNode.setDomainName(smbc.getDomainName());
                }
            }
        }

        entryNode.store(dbc);
        return entryNode;
    }

    /**
     * This method is responsible for inserting new entries into the
     * ipInterface table for each interface found to be associated with the
     * suspect interface during the capabilities scan.
     * 
     * @param dbc
     *            Database connection.
     * @param node
     *            DbNodeEntry object representing the suspect interface's
     *            parent node table entry
     * @param useExistingNode
     *            False if a new node was created for the suspect interface.
     *            True if an existing node entry was found under which the the
     *            suspect interface is to be added.
     * @param ifaddr
     *            Suspect interface
     * @param collector
     *            Interface collector containing SMB and SNMP info collected
     *            from the remote device.
     * @throws SQLException
     *             if an error occurs adding interfaces to the ipInterface
     *             table.
     */
    private void addInterfaces(Connection dbc, DbNodeEntry node,
            boolean useExistingNode, InetAddress ifaddr, IfCollector collector)
            throws SQLException {
        CapsdConfig cFactory = CapsdConfigFactory.getInstance();

        Date now = new Date();

        int nodeId = node.getNodeId();

        DbIpInterfaceEntry ipIfEntry = DbIpInterfaceEntry.create(nodeId,
                                                                 ifaddr);
        ipIfEntry.setLastPoll(now);
        ipIfEntry.setHostname(ifaddr.getHostName());

        /*
         * NOTE: (reference internal bug# 201) If the ip is 'managed', it
         * might still be 'not polled' based on the poller configuration The
         * package filter evaluation requires that the ip be in the database -
         * at this point the ip is NOT in db, so insert as active and update
         * afterward Try to avoid re-evaluating the ip against filters for
         * each service, try to get the first package here and use that for
         * service evaluation
         */
        boolean addrUnmanaged = cFactory.isAddressUnmanaged(ifaddr);
        if (addrUnmanaged) {
            log().debug("addInterfaces: " + ifaddr + " is unmanaged");
            ipIfEntry.setManagedState(DbIpInterfaceEntry.STATE_UNMANAGED);
        } else {
            log().debug("addInterfaces: " + ifaddr + " is managed");
            ipIfEntry.setManagedState(DbIpInterfaceEntry.STATE_MANAGED);
        }

        ipIfEntry.setPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);

        ipIfEntry.store(dbc);

        // now update if necessary
        org.opennms.netmgt.config.poller.Package ipPkg =
            getPackageForNewInterface(dbc, ifaddr, ipIfEntry, addrUnmanaged);
        
        int ifIndex = addSnmpInterfaces(dbc, ifaddr, nodeId, collector,
                                        ipIfEntry);

        // Add supported protocols
        addSupportedProtocols(node, ifaddr,
                              collector.getSupportedProtocols(),
                              addrUnmanaged, ifIndex, ipPkg);

        /*
         * If the useExistingNode flag is true, then we're done. The interface
         * is most likely an alias and the subinterfaces collected via SNMP
         * should already be in the database.
         */
        if (useExistingNode == true) {
            return;
        }

        getSubInterfacesForNewInterface(dbc, node, ifaddr, collector, now,
                                        nodeId, ifIndex);
    }

    private int addSnmpInterfaces(Connection dbc, InetAddress ifaddr,
            int nodeId, IfCollector collector, DbIpInterfaceEntry ipIfEntry)
    throws SQLException {
        boolean addedSnmpInterfaceEntry =
            addIfTableSnmpInterfaces(dbc, ifaddr, nodeId, collector);
        
        int ifIndex = getIfIndexForNewInterface(dbc, ifaddr, collector,
                                                ipIfEntry);
        
        if (ifIndex == CapsdConfig.LAME_SNMP_HOST_IFINDEX
                || !addedSnmpInterfaceEntry) {
            DbSnmpInterfaceEntry snmpEntry =
                DbSnmpInterfaceEntry.create(nodeId, ifIndex);
            snmpEntry.store(dbc);
        }

        if (log().isDebugEnabled()) {
            log().debug("SuspectEventProcessor: setting ifindex for "
                    + nodeId + "/" + ifaddr + " to " + ifIndex);
        }
        
        ipIfEntry.setIfIndex(ifIndex);
        ipIfEntry.store(dbc);
        
        return ifIndex;
    }

    private org.opennms.netmgt.config.poller.Package getPackageForNewInterface(
            Connection dbc, InetAddress ifaddr, DbIpInterfaceEntry ipIfEntry,
            boolean addrUnmanaged) throws SQLException {
        if (addrUnmanaged) {
            return null;
        }

        PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();

        org.opennms.netmgt.config.poller.Package ipPkg = null;

        /*
         * The newly discoveried IP addr is not in the Package IPList Mapping
         * yet, so rebuild the list.
         */
        pollerCfgFactory.rebuildPackageIpListMap();

        boolean ipToBePolled = false;
        ipPkg = pollerCfgFactory.getFirstPackageMatch(str(ifaddr));
        if (ipPkg != null) {
            ipToBePolled = true;
        }

        if (log().isDebugEnabled()) {
            log().debug("addInterfaces: " + ifaddr + " is to be polled = "
                    + ipToBePolled);
        }

        if (!ipToBePolled) {
            // update ismanaged to 'N' in ipinterface
            ipIfEntry.setManagedState(DbIpInterfaceEntry.STATE_NOT_POLLED);
            ipIfEntry.store(dbc);
        }

        return ipPkg;
    }

    private int getIfIndexForNewInterface(Connection dbc, InetAddress ifaddr,
            IfCollector collector, DbIpInterfaceEntry ipIfEntry)
            throws SQLException {
        if (!collector.hasSnmpCollection()) {
            return -1;
        }

        IfSnmpCollector snmpc = collector.getSnmpCollector();

        int ifIndex = -1;

        /*
         * Just set primary state to not eligible for now. The primary SNMP
         * interface won't be selected until after all interfaces have been
         * inserted into the database. This is because the interface must
         * already be in the database for filter rule evaluation to succeed.
         */
        ipIfEntry.setPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);

        if (snmpc.hasIpAddrTable()
                && (ifIndex = snmpc.getIfIndex(ifaddr)) != -1) {
            if (snmpc.hasIfTable()) {
                int status = snmpc.getAdminStatus(ifIndex);
                if (status != -1) {
                    ipIfEntry.setStatus(status);
                }
            }
        } else {
            /*
             * Address does not have a valid ifIndex associated with it Assume
             * there is no ipAddrTable and set ifIndex equal to
             * CapsdConfigFactory.LAME_SNMP_HOST_IFINDEX
             */
            ifIndex = CapsdConfig.LAME_SNMP_HOST_IFINDEX;
            if (log().isDebugEnabled()) {
                log().debug("SuspectEventProcessor: no valid ifIndex for "
                        + ifaddr + " Assume this is a lame SNMP host");
            }
        }
        ipIfEntry.store(dbc);

        return ifIndex;
    }

    private void getSubInterfacesForNewInterface(Connection dbc,
            DbNodeEntry node, InetAddress ifaddr, IfCollector collector,
            Date now, int nodeId, int ifIndex) throws SQLException {
        if (!collector.hasSnmpCollection()) {
            return;
        }

        CapsdConfig cFactory = CapsdConfigFactory.getInstance();
        PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();
        
        IfSnmpCollector snmpc = collector.getSnmpCollector();

        // Made it this far...lets add the IP sub-interfaces
        addSubIpInterfaces(dbc, node, collector, now, nodeId, cFactory, pollerCfgFactory,
                           snmpc);
        
    }


    private void addSubIpInterfaces(Connection dbc, DbNodeEntry node,
            IfCollector collector, Date now, int nodeId, CapsdConfig cFactory,
            PollerConfig pollerCfgFactory, IfSnmpCollector snmpc) throws SQLException {
        
        if (!snmpc.hasIpAddrTable()) {
            return;
        }
        
        Map<InetAddress, List<SupportedProtocol>> extraTargets = collector.getAdditionalTargets();
        for(InetAddress xifaddr : extraTargets.keySet()) {

            if (log().isDebugEnabled()) {
                log().debug("addInterfaces: adding interface "
                        + str(xifaddr));
            }

            DbIpInterfaceEntry xipIfEntry = DbIpInterfaceEntry.create(nodeId,
                                                                      xifaddr);
            xipIfEntry.setLastPoll(now);
            xipIfEntry.setHostname(xifaddr.getHostName());

            /*
             * NOTE: (reference internal bug# 201) If the ip is 'managed', it
             * might still be 'not polled' based on the poller configuration.
             * The package filter evaluation requires that the ip be in the
             * database - at this point the ip is NOT in db, so insert as
             * active and update afterward. Try to avoid re-evaluating the ip
             * against filters for each service, try to get the first package
             * here and use that for service evaluation.
             */
            boolean xaddrUnmanaged = cFactory.isAddressUnmanaged(xifaddr);
            if (xaddrUnmanaged) {
                xipIfEntry.setManagedState(DbIpInterfaceEntry.STATE_UNMANAGED);
            } else {
                xipIfEntry.setManagedState(DbIpInterfaceEntry.STATE_MANAGED);
            }

            /*
             * Just set primary state to not eligible for now. The primary
             * SNMP interface won't be selected until after all interfaces
             * have been inserted into the database. This is because the
             * interface must already be in the database for filter rule
             * evaluation to succeed.
             */
            xipIfEntry.setPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);
            int xifIndex = -1;
            if ((xifIndex = snmpc.getIfIndex(xifaddr)) != -1) {
                /*
                 * XXX I'm not sure if it is always safe to call setIfIndex
                 * here.  We should only do it if an snmpInterface entry
                 * was previously created for this ifIndex.  It was likely done
                 * by addSnmpInterfaces, but I have't checked to make sure that
                 * all cases are covered. - dj@opennms.org 
                 */
                xipIfEntry.setIfIndex(xifIndex);
                int status = snmpc.getAdminStatus(xifIndex);
                if (status != -1) {
                    xipIfEntry.setStatus(status);
                }

                if (!supportsSnmp(extraTargets.get(xifaddr))) {
                    log().debug("addInterfaces: Interface doesn't support SNMP. "
                            + str(xifaddr)
                            + " set to not eligible");
                }
            } else {
                /*
                 * No ifIndex found so set primary state to NOT_ELIGIBLE
                 */
                log().debug("addInterfaces: No ifIndex found. "
                        + str(xifaddr) + " set to not eligible");
            }

            xipIfEntry.store(dbc);

            // now update if necessary
            org.opennms.netmgt.config.poller.Package xipPkg = null;
            if (!xaddrUnmanaged) {
                // The newly discoveried IP addr is not in the Package
                // IPList
                // Mapping yet, so rebuild the list.
                //
                PollerConfigFactory.getInstance().rebuildPackageIpListMap();

                boolean xipToBePolled = false;
                xipPkg = pollerCfgFactory.getFirstPackageMatch(str(xifaddr));
                if (xipPkg != null) {
                    xipToBePolled = true;
                }

                if (!xipToBePolled) {
                    // update ismanaged to 'N' in ipinterface
                    xipIfEntry.setManagedState(DbIpInterfaceEntry.STATE_NOT_POLLED);
                    xipIfEntry.store(dbc);
                }
            }

            // add the supported protocols
            addSupportedProtocols(node, xifaddr,
                                  extraTargets.get(xifaddr),
                                  xaddrUnmanaged, xifIndex, xipPkg);
        }
    }
    
    private boolean addIfTableSnmpInterfaces(Connection dbc, InetAddress ifaddr,
            int nodeId, IfCollector collector)
            throws SQLException {
        if (!collector.hasSnmpCollection()) {
            return false;
        }
        
        IfSnmpCollector snmpc = collector.getSnmpCollector();

        if (!snmpc.hasIfTable()) {
            return false;
        }

        boolean addedSnmpInterfaceEntry = false;

        for (IfTableEntry ifte : snmpc.getIfTable()) {
            // index
            if (ifte.getIfIndex() == null) {
                continue;
            }
            final int xifIndex = ifte.getIfIndex().intValue();

            /*
             * address WARNING: IfSnmpCollector.getIfAddressAndMask() ONLY
             * returns the FIRST IP address and mask for a given interface as
             * specified in the ipAddrTable.
             */
            InetAddress[] addrs = null;
            if (snmpc.hasIpAddrTable()) {
                addrs = snmpc.getIfAddressAndMask(xifIndex);
            }

            // At some point back in the day this was done with ifType
            // Skip loopback interfaces
            if (addrs != null && addrs[0].isLoopbackAddress()) {
                continue;
            }

            final DbSnmpInterfaceEntry snmpEntry =
                DbSnmpInterfaceEntry.create(nodeId, xifIndex);

            if (addrs == null) {
                // No IP associated with the interface
                snmpEntry.setCollect("N");

            } else {
                // IP address
                if (addrs[0].equals(ifaddr)) {
                    addedSnmpInterfaceEntry = true;
                }

                // netmask
                if (addrs[1] != null) {
                    snmpEntry.setNetmask(addrs[1]);
                }
                
                snmpEntry.setCollect("C");
            }

            // description
            final String str = ifte.getIfDescr();
            if (log().isDebugEnabled() && addrs != null) {
                log().debug("SuspectEventProcessor: "
                        + str(addrs[0]) + " has ifDescription: "
                        + str);
            }
            if (str != null && str.length() > 0) {
                snmpEntry.setDescription(str);
            }

            // physical address
            String physAddr = null;
            try {
                physAddr = ifte.getPhysAddr();
                if (log().isDebugEnabled() && addrs != null) {
                    log().debug("SuspectEventProcessor: "
                            + str(addrs[0])
                            + " has physical address: -" + physAddr + "-");
                }
            } catch (IllegalArgumentException iae) {
                physAddr = null;
                if (log().isDebugEnabled() && addrs != null) {
                    log().debug("ifPhysAddress." + ifte.getIfIndex() + " on node "
                               + nodeId + " / " + str(addrs[0])
                               + " could not be converted to a hex string (not a PhysAddr / OCTET STRING?), setting to null.");
                }
                StringBuffer errMsg = new StringBuffer("SNMP agent bug on node ");
                errMsg.append(nodeId).append(" / ").append(str(ifaddr));
                errMsg.append(": wrong type for physical address (see bug 2740). ");
                errMsg.append("Working around, but expect trouble with this node.");
                log().warn(errMsg.toString());
            }
            if (physAddr != null && physAddr.length() == 12) {
                snmpEntry.setPhysicalAddress(physAddr);
            }

            if (ifte.getIfType() == null) {
                snmpEntry.setType(0);
            } else {
                snmpEntry.setType(ifte.getIfType().intValue());
            }
            
            
            IfXTableEntry ifxte = snmpc.hasIfXTable() ? snmpc.getIfXTable().getEntry(xifIndex) : null;
            
            long speed = getInterfaceSpeed(ifte, ifxte);

            // speed
            snmpEntry.setSpeed(speed);


            // admin status
            if (ifte.getIfAdminStatus() == null) {
                snmpEntry.setAdminStatus(0);
            } else {
                snmpEntry.setAdminStatus(ifte.getIfAdminStatus().intValue());
            }

            // oper status
            if (ifte.getIfOperStatus() == null) {
                snmpEntry.setOperationalStatus(0);
            } else {
                snmpEntry.setOperationalStatus(ifte.getIfOperStatus().intValue());
            }

            // name (from interface extensions table)
            String ifName = snmpc.getIfName(xifIndex);
            if (ifName != null && ifName.length() > 0) {
                snmpEntry.setName(ifName);
            }

            // alias (from interface extensions table)
            final String ifAlias = snmpc.getIfAlias(xifIndex);
            if (ifAlias != null && ifAlias.length() > 0) {
                snmpEntry.setAlias(ifAlias);
            }

            snmpEntry.store(dbc);
        }
        return addedSnmpInterfaceEntry;
    }

    private long getInterfaceSpeed(IfTableEntry ifte, IfXTableEntry ifxte) {
        if (ifxte != null && ifxte.getIfHighSpeed() != null && ifxte.getIfHighSpeed() > 4294) {
            return ifxte.getIfHighSpeed() * 1000000L; 
        }
        
        if (ifte != null && ifte.getIfSpeed() != null) {
            return ifte.getIfSpeed();
        }

        return 0;
    }

    /**
     * Responsible for iterating inserting an entry into the ifServices table
     * for each protocol supported by the interface.
     * 
     * @param node
     *            Node entry
     * @param ifaddr
     *            Interface address
     * @param protocols
     *            List of supported protocols
     * @param addrUnmanaged
     *            Boolean flag indicating if interface is managed or unmanaged
     *            according to the Capsd configuration.
     * @param ifIndex
     *            Interface index or -1 if index is not known
     * @param ipPkg
     *            Poller package to which the interface belongs
     * @throws SQLException
     *             if an error occurs adding interfaces to the ipInterface
     *             table.
     */
    private void addSupportedProtocols(DbNodeEntry node, InetAddress ifaddr,
            List<SupportedProtocol> protocols, boolean addrUnmanaged, int ifIndex,
            org.opennms.netmgt.config.poller.Package ipPkg)
            throws SQLException {
        if (str(ifaddr).equals("0.0.0.0")) {
            log().debug("addSupportedProtocols: node "
                    + node.getNodeId()
                    + ": Cant add ip services for non-ip interface. Just return.");
            return;
        }

        // add the supported protocols
        //
        // NOTE!!!!!: (reference internal bug# 201)
        // If the ip is 'managed', the service can still be 'not polled'
        // based on the poller configuration - at this point the ip is already
        // in the database, so package filter evaluation should go through OK
        //
        for(SupportedProtocol p : protocols) {

            Number sid = m_capsdDbSyncer.getServiceId(p.getProtocolName());

            DbIfServiceEntry ifSvcEntry = DbIfServiceEntry.create(
                                                                  node.getNodeId(),
                                                                  ifaddr,
                                                                  sid.intValue());

            // now fill in the entry
            //
            if (addrUnmanaged)
                ifSvcEntry.setStatus(DbIfServiceEntry.STATUS_UNMANAGED);
            else {
                if (isServicePolledLocally(str(ifaddr), p.getProtocolName(), ipPkg)) {
                    ifSvcEntry.setStatus(DbIfServiceEntry.STATUS_ACTIVE);
                } else if (isServicePolled(str(ifaddr), p.getProtocolName(), ipPkg)) {
                    ifSvcEntry.setStatus(DbIpInterfaceEntry.STATE_REMOTE);
                } else {
                    ifSvcEntry.setStatus(DbIfServiceEntry.STATUS_NOT_POLLED);
                }
            }

            // Set qualifier if available. Currently the qualifier field
            // is used to store the port at which the protocol was found.
            //
            if (p.getQualifiers() != null
                    && p.getQualifiers().get("port") != null) {
                try {
                    Integer port = (Integer) p.getQualifiers().get("port");
                    ifSvcEntry.setQualifier(port.toString());
                } catch (ClassCastException ccE) {
                    // Do nothing
                }
            }

            ifSvcEntry.setSource(DbIfServiceEntry.SOURCE_PLUGIN);
            ifSvcEntry.setNotify(DbIfServiceEntry.NOTIFY_ON);
            if (ifIndex != -1)
                ifSvcEntry.setIfIndex(ifIndex);
            ifSvcEntry.store();
        }
    }

    private boolean isServicePolled(String ifAddr, String svcName, org.opennms.netmgt.config.poller.Package ipPkg) {
        boolean svcToBePolled = false;
        if (ipPkg != null) {
            svcToBePolled = PollerConfigFactory.getInstance().isPolled(svcName, ipPkg);
            if (!svcToBePolled)
                svcToBePolled = PollerConfigFactory.getInstance().isPolled(ifAddr, svcName);
        }
        return svcToBePolled;
    }

    private boolean isServicePolledLocally(String ifAddr, String svcName, org.opennms.netmgt.config.poller.Package ipPkg) {
        boolean svcToBePolled = false;
        if (ipPkg != null && !ipPkg.getRemote()) {
            svcToBePolled = PollerConfigFactory.getInstance().isPolled(svcName, ipPkg);
            if (!svcToBePolled)
                svcToBePolled = PollerConfigFactory.getInstance().isPolledLocally(ifAddr, svcName);
        }
        return svcToBePolled;
    }

    /**
     * Utility method which checks the provided list of supported protocols to
     * determine if the SNMP service is present.
     * 
     * @param supportedProtocols
     *            List of supported protocol objects.
     * @return TRUE if service "SNMP" is present in the list, FALSE otherwise
     */
    static boolean supportsSnmp(List<SupportedProtocol> supportedProtocols) {
        for(SupportedProtocol p : supportedProtocols) {
            if (p.getProtocolName().equals("SNMP"))
                return true;
        }
        return false;
    }

    /**
     * Utility method which determines if the passed IfSnmpCollector object
     * contains an ifIndex value for the passed IP address.
     * 
     * @param ipaddr
     *            IP address
     * @param snmpc
     *            SNMP collection
     * @return TRUE if an ifIndex value was found in the SNMP collection for
     *         the provided IP address, FALSE otherwise.
     */
    static boolean hasIfIndex(InetAddress ipaddr, IfSnmpCollector snmpc) {
        int ifIndex = -1;
        if (snmpc.hasIpAddrTable())
            ifIndex = snmpc.getIfIndex(ipaddr);

        if (log().isDebugEnabled())
            log().debug("hasIfIndex: ipAddress: " + str(ipaddr)
                    + " has ifIndex: " + ifIndex);
        if (ifIndex == -1)
            return false;
        else
            return true;
    }

    /**
     * Utility method which determines returns the ifType for the passed IP
     * address.
     * 
     * @param ipaddr
     *            IP address
     * @param snmpc
     *            SNMP collection
     * @return TRUE if an ifIndex value was found in the SNMP collection for
     *         the provided IP address, FALSE otherwise.
     */
    static int getIfType(InetAddress ipaddr, IfSnmpCollector snmpc) {
        int ifIndex = snmpc.getIfIndex(ipaddr);
        int ifType = snmpc.getIfType(ifIndex);

        if (log().isDebugEnabled())
            log().debug("getIfType: ipAddress: " + str(ipaddr)
                    + " has ifIndex: " + ifIndex + " and ifType: " + ifType);
        return ifType;
    }

    /**
     * Utility method which compares two InetAddress objects based on the
     * provided method (MIN/MAX) and returns the InetAddress which is to be
     * considered the primary interface. NOTE: In order for an interface to be
     * considered primary it must be managed. This method will return null if
     * the 'oldPrimary' address is null and the 'currentIf' address is
     * unmanaged.
     * 
     * @param currentIf
     *            Interface with which to compare the 'oldPrimary' address.
     * @param oldPrimary
     *            Primary interface to be compared against the 'currentIf'
     *            address.
     * @param method
     *            Comparison method to be used (either "min" or "max")
     * @return InetAddress object of the primary interface based on the
     *         provided method or null if neither address is eligible to be
     *         primary.
     */
    static InetAddress compareAndSelectPrimary(InetAddress currentIf, InetAddress oldPrimary) {
        InetAddress newPrimary = null;
        if (oldPrimary == null) {
            if (!CapsdConfigFactory.getInstance().isAddressUnmanaged(currentIf)) {
                return currentIf;
            }
            else {
                return oldPrimary;
            }
        }

        byte[] current = currentIf.getAddress();
        byte[] primary = oldPrimary.getAddress();

        // Smallest address wins
        if (new ByteArrayComparator().compare(current, primary) < 0) {
            // Replace the primary interface with the current
            // interface only if the current interface is managed!
            if (!CapsdConfigFactory.getInstance().isAddressUnmanaged(currentIf)) {
                newPrimary = currentIf;
            }
        }   

        if (newPrimary != null) {
            return newPrimary;
        }
        else {
            return oldPrimary;
        }
    }

    /**
     * Builds a list of InetAddress objects representing each of the
     * interfaces from the IfCollector object which support SNMP and have a
     * valid ifIndex and is a loopback interface. This is in order to allow a
     * non-127.*.*.* loopback address to be chosen as the primary SNMP
     * interface.
     * 
     * @param collector
     *            IfCollector object containing SNMP and SMB info.
     * @return List of InetAddress objects.
     */
    private List<InetAddress> buildLBSnmpAddressList(IfCollector collector) {
        List<InetAddress> addresses = new ArrayList<InetAddress>();

        // Verify that SNMP info is available
        if (collector.getSnmpCollector() == null) {
            if (log().isDebugEnabled())
                log().debug("buildLBSnmpAddressList: no SNMP info for "
                        + collector.getTarget());
            return addresses;
        }

        // Verify that both the ifTable and ipAddrTable were
        // successfully collected.
        IfSnmpCollector snmpc = collector.getSnmpCollector();
        if (!snmpc.hasIfTable() || !snmpc.hasIpAddrTable()) {
            log().info("buildLBSnmpAddressList: missing SNMP info for "
                    + collector.getTarget());
            return addresses;
        }

        // To be eligible to be the primary SNMP interface for a node:
        //
        // 1. The interface must support SNMP
        // 2. The interface must have a valid ifIndex
        //

        // Add eligible target.
        //
        InetAddress ipAddr = collector.getTarget();
        if (supportsSnmp(collector.getSupportedProtocols())
                && hasIfIndex(ipAddr, snmpc)
                && getIfType(ipAddr, snmpc) == 24) {
            if (log().isDebugEnabled())
                log().debug("buildLBSnmpAddressList: adding target interface "
                        + str(ipAddr)
                        + " temporarily marked as primary!");
            addresses.add(ipAddr);
        }

        // Add eligible subtargets.
        //
        if (collector.hasAdditionalTargets()) {
            Map<InetAddress, List<SupportedProtocol>> extraTargets = collector.getAdditionalTargets();
            for(InetAddress currIf : extraTargets.keySet()) {
                // Test current subtarget.
                // 
                if (supportsSnmp(extraTargets.get(currIf))
                        && getIfType(currIf, snmpc) == 24) {
                    if (log().isDebugEnabled())
                        log().debug("buildLBSnmpAddressList: adding subtarget interface "
                                + str(currIf)
                                + " temporarily marked as primary!");
                    addresses.add(currIf);
                }
            } // end while()
        } // end if()

        return addresses;
    }

    /**
     * Builds a list of InetAddress objects representing each of the
     * interfaces from the IfCollector object which support SNMP and have a
     * valid ifIndex.
     * 
     * @param collector
     *            IfCollector object containing SNMP and SMB info.
     * @return List of InetAddress objects.
     */
    private List<InetAddress> buildSnmpAddressList(IfCollector collector) {
        List<InetAddress> addresses = new ArrayList<InetAddress>();

        // Verify that SNMP info is available
        if (collector.getSnmpCollector() == null) {
            if (log().isDebugEnabled())
                log().debug("buildSnmpAddressList: no SNMP info for "
                        + collector.getTarget());
            return addresses;
        }

        // Verify that both the ifTable and ipAddrTable were
        // successfully collected.
        IfSnmpCollector snmpc = collector.getSnmpCollector();
        if (!snmpc.hasIfTable() || !snmpc.hasIpAddrTable()) {
            log().info("buildSnmpAddressList: missing SNMP info for "
                    + collector.getTarget());
            return addresses;
        }

        // To be eligible to be the primary SNMP interface for a node:
        //
        // 1. The interface must support SNMP
        // 2. The interface must have a valid ifIndex
        //

        // Add eligible target.
        //
        InetAddress ipAddr = collector.getTarget();
        if (supportsSnmp(collector.getSupportedProtocols())
                && hasIfIndex(ipAddr, snmpc)) {
            if (log().isDebugEnabled())
                log().debug("buildSnmpAddressList: adding target interface "
                        + str(ipAddr)
                        + " temporarily marked as primary!");
            addresses.add(ipAddr);
        }

        // Add eligible subtargets.
        //
        if (collector.hasAdditionalTargets()) {
            Map<InetAddress, List<SupportedProtocol>> extraTargets = collector.getAdditionalTargets();
            for(InetAddress currIf : extraTargets.keySet()) {

                // Test current subtarget.
                // 
                if (supportsSnmp(extraTargets.get(currIf))
                        && hasIfIndex(currIf, snmpc)) {
                    if (log().isDebugEnabled())
                        log().debug("buildSnmpAddressList: adding subtarget interface "
                                + str(currIf)
                                + " temporarily marked as primary!");
                    addresses.add(currIf);
                }
            } // end while()
        } // end if()

        return addresses;
    }

    /**
     * This method is responsbile for determining the node's primary IP
     * interface from among all the node's IP interfaces.
     * 
     * @param collector
     *            IfCollector object containing SNMP and SMB info.
     * @return InetAddress object of the primary SNMP interface or null if
     *         none of the node's interfaces are eligible.
     */
    private InetAddress determinePrimaryInterface(IfCollector collector) {
        InetAddress primaryIf = null;

        // For now hard-coding primary interface address selection method to
        // MIN

        // Initially set the target interface as primary
        primaryIf = collector.getTarget();

        // Next the subtargets will be tested. If is managed and
        // has a smaller numeric IP address then it will in turn be
        // set as the primary interface.
        if (collector.hasAdditionalTargets()) {
            Map<InetAddress, List<SupportedProtocol>> extraTargets = collector.getAdditionalTargets();
            for(InetAddress currIf : extraTargets.keySet()) {
                primaryIf = compareAndSelectPrimary(currIf, primaryIf);
            } // end while()
        } // end if (Collector.hasAdditionalTargets())

        if (log().isDebugEnabled())
            if (primaryIf != null)
                log().debug("determinePrimaryInterface: selected primary interface: "
                        + str(primaryIf));
            else
                log().debug("determinePrimaryInterface: no primary interface found");
        return primaryIf;
    }

    /**
     * This is where all the work of the class is done.
     */
    public void run() {

        // Convert interface InetAddress object
        //
        InetAddress ifaddr = null;
        ifaddr = addr(m_suspectIf);
        if (ifaddr == null) {
            log().warn(
                     "SuspectEventProcessor: Failed to convert interface address "
                             + m_suspectIf + " to InetAddress");
            return;
        }

        // collect the information
        //
        if (log().isDebugEnabled())
            log().debug("SuspectEventProcessor: running collection for "
                    + str(ifaddr));

        IfCollector collector = new IfCollector(m_pluginManager, ifaddr, true);
        collector.run();

        // Track changes to primary SNMP interface
        InetAddress oldSnmpPrimaryIf = null;
        InetAddress newSnmpPrimaryIf = null;

        // Update the database
        //
        boolean updateCompleted = false;
        boolean useExistingNode = false;
        DbNodeEntry entryNode = null;
        try {
            // Synchronize on the Capsd sync lock so we can check if
            // the interface is already in the database and perform
            // the necessary inserts in one atomic operation
            //	
            // The RescanProcessor class is also synchronizing on this
            // lock prior to performing database inserts or updates.
            Connection dbc = null;
            synchronized (Capsd.getDbSyncLock()) {
                // Get database connection
                //
                try {
                    dbc = DataSourceFactory.getInstance().getConnection();

                    // Only add the node/interface to the database if
                    // it isn't already in the database
                    if (!m_capsdDbSyncer.isInterfaceInDB(dbc, ifaddr)) {
                        // Using the interface collector object determine
                        // if this interface belongs under a node already
                        // in the database.
                        //
                        entryNode = getExistingNodeEntry(dbc, collector);

                        if (entryNode == null) {
                            // Create a node entry for the new interface
                            //
                            entryNode = createNode(dbc, ifaddr, collector);
                        } else {
                            // Will use existing node entry
                            //
                            useExistingNode = true;
                        }

                        // Get old primary SNMP interface(s) (if one or more
                        // exists)
                        //
                        List<InetAddress> oldPriIfs = getPrimarySnmpInterfaceFromDb(dbc,
                                                                       entryNode);

                        // Add interfaces
                        //
                        addInterfaces(dbc, entryNode, useExistingNode,
                                      ifaddr, collector);

                        // Now that all interfaces have been added to the
                        // database we can update the 'primarySnmpInterface'
                        // field of the ipInterface table. Necessary because
                        // the IP address must already be in the database
                        // to evaluate against a filter rule.
                        //
                        // Determine primary SNMP interface from the lists of
                        // possible addresses
                        // in this order: loopback interfaces in
                        // collectd-configuration.xml,
                        // other interfaces in collectd-configuration.xml,
                        // loopback interfaces,
                        // other interfaces
                        //
                        boolean strict = true;
                        CollectdConfigFactory.getInstance().rebuildPackageIpListMap();
                        List<InetAddress> lbAddressList = buildLBSnmpAddressList(collector);
                        List<InetAddress> addressList = buildSnmpAddressList(collector);
                        // first set the value of issnmpprimary for
                        // secondaries
                        Iterator<InetAddress> iter = addressList.iterator();
                        while (iter.hasNext()) {
                            InetAddress addr = iter.next();
                            if (CollectdConfigFactory.getInstance().isServiceCollectionEnabled(str(addr), "SNMP")) {
                                final DBUtils d = new DBUtils(getClass());
                                try {
                                    PreparedStatement stmt = dbc.prepareStatement("UPDATE ipInterface SET isSnmpPrimary='S' WHERE nodeId=? AND ipAddr=? AND isManaged!='D'");
                                    d.watch(stmt);
                                    stmt.setInt(1, entryNode.getNodeId());
                                    stmt.setString(2, str(addr));

                                    stmt.executeUpdate();
                                    log().debug("updated " + str(addr) + " to secondary.");
                                } finally {
                                    d.cleanUp();
                                }
                            }
                        }
                        String psiType = null;
                        if (lbAddressList != null) {
                            newSnmpPrimaryIf = CapsdConfigFactory.getInstance().determinePrimarySnmpInterface(lbAddressList, strict);
                            psiType = ConfigFileConstants.getFileName(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME) + " loopback addresses";
                        }
                        if (newSnmpPrimaryIf == null) {
                            newSnmpPrimaryIf = CapsdConfigFactory.getInstance().determinePrimarySnmpInterface(addressList, strict);
                            psiType = ConfigFileConstants.getFileName(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME) + " addresses";
                        }
                        strict = false;
                        if ((newSnmpPrimaryIf == null) && (lbAddressList != null)) {
                            newSnmpPrimaryIf = CapsdConfigFactory.getInstance().determinePrimarySnmpInterface(lbAddressList, strict);
                            psiType = "DB loopback addresses";
                        }
                        if (newSnmpPrimaryIf == null) {
                            newSnmpPrimaryIf = CapsdConfigFactory.getInstance().determinePrimarySnmpInterface(addressList, strict);
                            psiType = "DB addresses";
                        }
                        if (collector.hasSnmpCollection() && newSnmpPrimaryIf == null) {
                            newSnmpPrimaryIf = ifaddr;
                            psiType = "New suspect ip address";
                        }

                        if (log().isDebugEnabled()) {
                            if (newSnmpPrimaryIf == null) {
                                log().debug("No primary SNMP interface found");
                            } else {
                                log().debug("primary SNMP interface is: "
                                        + newSnmpPrimaryIf
                                        + ", selected from " + psiType);
                            }
                        }
                        // iterate over list of old primaries. There should
                        // only be
                        // one or none, but in case there are more, this will
                        // clear
                        // out the extras.
                        Iterator<InetAddress> opiter = oldPriIfs.iterator();
                        if (opiter.hasNext()) {
                            while (opiter.hasNext()) {
                                setPrimarySnmpInterface(
                                                        dbc,
                                                        entryNode,
                                                        newSnmpPrimaryIf,
                                                        opiter.next());
                            }
                        } else {
                            setPrimarySnmpInterface(dbc, entryNode,
                                                    newSnmpPrimaryIf, null);
                        }
                        // Update
                        updateCompleted = true;
                    }
                } finally {
                    if (dbc != null) {
                        try {
                            dbc.close();
                        } catch (SQLException e) {
                            if (log().isInfoEnabled())
                                log().info(
                                         "run: an sql exception occured closing the database connection",
                                         e);
                        }
                    }
                    dbc = null;
                }
            }

        } // end try
        catch (Throwable t) {
            log().error("Error writing records", t);
        }
        finally {
        	// remove the interface we've just scanned from the tracker set
        	synchronized(m_queuedSuspectTracker) {
        		m_queuedSuspectTracker.remove(str(ifaddr));
        	}
        }

        // Send events
        //
        if (updateCompleted) {
            if (!useExistingNode)
                createAndSendNodeAddedEvent(entryNode);

            sendInterfaceEvents(entryNode, useExistingNode, ifaddr, collector);

            if (useExistingNode) {
                generateSnmpDataCollectionEvents(entryNode, oldSnmpPrimaryIf,
                                                 newSnmpPrimaryIf);
            }

        }

        // send suspectScanCompleted event regardless of scan outcome
    	if (log().isDebugEnabled()) {
    		log().debug("sendInterfaceEvents: sending suspect scan completed event for " + str(ifaddr));
    		log().debug("SuspectEventProcessor for " + m_suspectIf + " completed.");
    	}
    	createAndSendSuspectScanCompletedEvent(ifaddr);
    } // end run

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(SuspectEventProcessor.class);
    }

    /**
     * Returns a list of InetAddress object(s) of the primary SNMP
     * interface(s) (if one or more exists).
     * 
     * @param dbc
     *            Database connection.
     * @param node
     *            DbNodeEntry object representing the interface's parent node
     *            table entry
     * @throws SQLException
     *             if an error occurs updating the ipInterface table
     * @return List of Old SNMP primary interface addresses (usually just
     *         one).
     */
    List<InetAddress> getPrimarySnmpInterfaceFromDb(Connection dbc, DbNodeEntry node)
            throws SQLException {
        List<InetAddress> priSnmpAddrs = new ArrayList<InetAddress>();

        log().debug("getPrimarySnmpInterfaceFromDb: retrieving primary SNMP interface(s) from DB for node "
                + node.getNodeId());
        InetAddress oldPrimarySnmpIf = null;

        final DBUtils d = new DBUtils(getClass());
        try {
            PreparedStatement stmt = dbc.prepareStatement("SELECT ipAddr FROM ipInterface WHERE nodeId=? AND isSnmpPrimary='P' AND isManaged!='D'");
            d.watch(stmt);
            stmt.setInt(1, node.getNodeId());

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                String oldPrimaryAddr = rs.getString(1);
                log().debug("getPrimarySnmpInterfaceFromDb: String oldPrimaryAddr = " + oldPrimaryAddr);
                if (oldPrimaryAddr != null) {
                    oldPrimarySnmpIf = addr(oldPrimaryAddr);
                    log().debug("getPrimarySnmpInterfaceFromDb: old primary SNMP interface is " + oldPrimaryAddr);
                    priSnmpAddrs.add(oldPrimarySnmpIf);
                }
            }
        } catch (SQLException sqlE) {
            log().warn("getPrimarySnmpInterfaceFromDb: Exception: " + sqlE);
            throw sqlE;
        } finally {
            d.cleanUp();
        }

        return priSnmpAddrs;
    }

    /**
     * Responsible for setting the value of the 'isSnmpPrimary' field of the
     * ipInterface table to 'P' (Primary) for the primary SNMP interface
     * address.
     * 
     * @param dbc
     *            Database connection.
     * @param node
     *            DbNodeEntry object representing the suspect interface's
     *            parent node table entry
     * @param newPrimarySnmpIf
     *            New primary SNMP interface.
     * @param oldPrimarySnmpIf
     *            Old primary SNMP interface.
     * @throws SQLException
     *             if an error occurs updating the ipInterface table
     */
    static void setPrimarySnmpInterface(Connection dbc, DbNodeEntry node, InetAddress newPrimarySnmpIf, InetAddress oldPrimarySnmpIf)
            throws SQLException {
        if (newPrimarySnmpIf == null) {
            if (log().isDebugEnabled())
                log().debug("setPrimarySnmpInterface: newSnmpPrimary is null, nothing to set, returning.");
            return;
        } else {
            if (log().isDebugEnabled())
                log().debug("setPrimarySnmpInterface: newSnmpPrimary = "
                        + newPrimarySnmpIf);
        }

        // Verify that old and new primary interfaces are different
        //
        if (oldPrimarySnmpIf != null
                && oldPrimarySnmpIf.equals(newPrimarySnmpIf)) {
            // Old and new primary interfaces are the same
            if (log().isDebugEnabled())
                log().debug("setPrimarySnmpInterface: Old and new primary interfaces are the same");
        }

        // Set primary SNMP interface 'isSnmpPrimary' field to 'P' for primary
        //
        if (newPrimarySnmpIf != null) {
            if (log().isDebugEnabled())
                log().debug("setPrimarySnmpInterface: Updating primary SNMP interface "
                        + str(newPrimarySnmpIf));

            // Update the appropriate entry in the 'ipInterface' table
            //

            final DBUtils d = new DBUtils(SuspectEventProcessor.class);
            try {
                PreparedStatement stmt = dbc.prepareStatement("UPDATE ipInterface SET isSnmpPrimary='P' WHERE nodeId=? AND ipaddr=? AND isManaged!='D'");
                d.watch(stmt);
                stmt.setInt(1, node.getNodeId());
                stmt.setString(2, str(newPrimarySnmpIf));

                stmt.executeUpdate();
                if (log().isDebugEnabled())
                    log().debug("setPrimarySnmpInterface: Completed update of new primary interface to PRIMARY.");
            } finally {
                d.cleanUp();
            }
        }
    }
    
    /**
     * Responsible for setting the Set used to track suspect scans that
     * are already enqueued for processing.  Should be called once by Capsd
     * at startup.
     *
     * @param queuedSuspectTracker a {@link java.util.Set} object.
     */
    public static synchronized void setQueuedSuspectsTracker(Set<String> queuedSuspectTracker) {
    	m_queuedSuspectTracker = Collections.synchronizedSet(queuedSuspectTracker);
    }
    
    /**
     * Is a suspect scan already enqueued for a given IP address?
     *
     * @param ipAddr
     * 			The IP address of interest
     * @return a boolean.
     */
    public static boolean isScanQueuedForAddress(String ipAddr) {
    	synchronized(m_queuedSuspectTracker) {
    		return (m_queuedSuspectTracker.contains(ipAddr));
    	}
    }

    /**
     * Determines if any SNMP data collection related events need to be
     * generated based upon the results of the current rescan. If necessary
     * will generate one of the following events:
     * 'reinitializePrimarySnmpInterface' 'primarySnmpInterfaceChanged'
     * 
     * @param nodeEntry
     *            DbNodeEntry object of the node being rescanned.
     * @param oldPrimary
     *            Old primary SNMP interface
     * @param newPrimary
     *            New primary SNMP interface
     */
    private void generateSnmpDataCollectionEvents(DbNodeEntry nodeEntry,
            InetAddress oldPrimary, InetAddress newPrimary) {
        // Sanity check -- should not happen
        if (oldPrimary == null && newPrimary == null) {
            log().warn("generateSnmpDataCollectionEvents: both old and new primary SNMP interface vars are null!");
        }

        // Sanity check -- should not happen
        else if (oldPrimary != null && newPrimary == null) {
            log().warn("generateSnmpDataCollectionEvents: old primary ("
                    + str(oldPrimary)
                    + ") is not null but new primary is null!");
        }

        // Just added the primary SNMP interface to the node, the
        // nodeGainedService
        // event already generated is sufficient to start SNMP data
        // collection...no
        // additional events are required.
        else if (oldPrimary == null && newPrimary != null) {
            if (log().isDebugEnabled())
                log().debug("generateSnmpDataCollectionEvents: identified "
                        + str(newPrimary)
                        + " as the primary SNMP interface for node "
                        + nodeEntry.getNodeId());
        }

        // A PrimarySnmpInterfaceChanged event is generated if the scan
        // found a different primary SNMP interface than what is stored
        // in the database.
        //
        else if (!oldPrimary.equals(newPrimary)) {
            if (log().isDebugEnabled()) {
                log().debug("generateSnmpDataCollectionEvents: primary SNMP interface has changed.  Was: "
                        + str(oldPrimary)
                        + " Is: "
                        + str(newPrimary));
            }

            createAndSendPrimarySnmpInterfaceChangedEvent(
                                                          nodeEntry.getNodeId(),
                                                          newPrimary,
                                                          oldPrimary);
        }

        // The primary SNMP interface did not change but the Capsd scan just
        // added
        // an interface to the node so we need to update the interface
        // map which is maintained in memory for the purpose of doing
        // SNMP data collection. Therefore we generate a
        // reinitializePrimarySnmpInterface event so that this map
        // can be refreshed based on the most up to date information
        // in the database.
        else {
            if (log().isDebugEnabled())
                log().debug("generateSnmpDataCollectionEvents: Generating reinitializeSnmpInterface event for interface "
                        + str(newPrimary));
            createAndSendReinitializePrimarySnmpInterfaceEvent(
                                                               nodeEntry.getNodeId(),
                                                               newPrimary);
        }
    }

    /**
     * This method is responsible for generating a primarySnmpInterfaceChanged
     * event and sending it to eventd..
     * 
     * @param nodeId
     *            Nodeid of node being rescanned.
     * @param newPrimaryIf
     *            new primary SNMP interface address
     * @param oldPrimaryIf
     *            old primary SNMP interface address
     */
    private void createAndSendPrimarySnmpInterfaceChangedEvent(int nodeId,
            InetAddress newPrimaryIf, InetAddress oldPrimaryIf) {
        
        
        if (log().isDebugEnabled())
            log().debug("createAndSendPrimarySnmpInterfaceChangedEvent: nodeId: "
                    + nodeId
                    + " oldPrimarySnmpIf: '"
                    + str(oldPrimaryIf)
                    + "' newPrimarySnmpIf: '" 
                    + str(newPrimaryIf) 
                    + "'");

        
        EventBuilder bldr = createEventBuilder(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI);
        bldr.setNodeid(nodeId);
        bldr.setInterface(newPrimaryIf);
        bldr.setService("SNMP");
        
        if (str(oldPrimaryIf) != null) {
            bldr.addParam(EventConstants.PARM_OLD_PRIMARY_SNMP_ADDRESS, str(oldPrimaryIf));
        }
        
        if (str(newPrimaryIf) != null) {
            bldr.addParam(EventConstants.PARM_NEW_PRIMARY_SNMP_ADDRESS, str(newPrimaryIf));
        }

        sendEvent(bldr.getEvent());

    }

    /**
     * This method is responsible for generating a
     * reinitializePrimarySnmpInterface event and sending it to eventd.
     * 
     * @param nodeId
     *            Nodeid of node being rescanned.
     * @param primarySnmpIf
     *            Primary SNMP interface address.
     */
    private void createAndSendReinitializePrimarySnmpInterfaceEvent(int nodeId, InetAddress primarySnmpIf) {
        if (log().isDebugEnabled())
            log().debug("reinitializePrimarySnmpInterface: nodeId: " + nodeId
                    + " interface: " + str(primarySnmpIf));

        EventBuilder bldr = createEventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI);
        bldr.setNodeid(nodeId);
        bldr.setInterface(primarySnmpIf);
        
        sendEvent(bldr.getEvent());

    }

    /**
     * This method is responsible for creating all the necessary
     * interface-level events for the node and sending them to Eventd.
     * 
     * @param node
     *            DbNodeEntry object for the parent node.
     * @param useExistingNode
     *            TRUE if existing node was used, FALSE if new node was
     *            created.
     * @param ifaddr
     *            Target interface address
     * @param collector
     *            Interface collector containing SNMP and SMB info.
     */
    private void sendInterfaceEvents(DbNodeEntry node,
            boolean useExistingNode, InetAddress ifaddr, IfCollector collector) {
        // nodeGainedInterface
        //
        if (log().isDebugEnabled())
            log().debug("sendInterfaceEvents: sending node gained interface event for "
                    + str(ifaddr));

        createAndSendNodeGainedInterfaceEvent(node.getNodeId(), ifaddr);

        // nodeGainedService
        //
        log().debug("sendInterfaceEvents: processing supported services for "
                + str(ifaddr));
        for(SupportedProtocol p : collector.getSupportedProtocols()) {
            if (log().isDebugEnabled())
                log().debug("sendInterfaceEvents: sending event for service: "
                        + p.getProtocolName());
            createAndSendNodeGainedServiceEvent(node, ifaddr,
                                                p.getProtocolName(), null);
        }

        // If the useExistingNode flag is set to TRUE we're done, none of the
        // sub-targets should have been added.
        //
        if (useExistingNode)
            return;

        // If SNMP info available send events for sub-targets
        //
        if (collector.hasSnmpCollection()
                && !collector.getSnmpCollector().failed()) {
            Map<InetAddress, List<SupportedProtocol>> extraTargets = collector.getAdditionalTargets();
            for(InetAddress xifaddr : extraTargets.keySet()) {

                // nodeGainedInterface
                //
                createAndSendNodeGainedInterfaceEvent(node.getNodeId(),
                                                      xifaddr);

                // nodeGainedService
                //
                List<SupportedProtocol> supportedProtocols = extraTargets.get(xifaddr);
                log().debug("interface " + xifaddr + " supports "
                        + supportedProtocols.size() + " protocols.");
                if (supportedProtocols != null) {
                    for(SupportedProtocol p : supportedProtocols) {
                        createAndSendNodeGainedServiceEvent(
                                                            node,
                                                            xifaddr,
                                                            p.getProtocolName(),
                                                            null);
                    }
                }
            }
        }
    }

    /**
     * This method is responsible for creating and sending a 'nodeAdded' event
     * to Eventd
     * 
     * @param nodeEntry
     *            DbNodeEntry object for the newly created node.
     */
    private void createAndSendNodeAddedEvent(DbNodeEntry nodeEntry) {
        
        EventBuilder bldr = createEventBuilder(EventConstants.NODE_ADDED_EVENT_UEI);
        bldr.setNodeid(nodeEntry.getNodeId());
        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeEntry.getLabel());
        bldr.addParam(EventConstants.PARM_NODE_LABEL_SOURCE, nodeEntry.getLabelSource());
        bldr.addParam(EventConstants.PARM_METHOD, "icmp");
        
        sendEvent(bldr.getEvent());

    }

    private EventBuilder createEventBuilder(String uei) {
        EventBuilder bldr = new EventBuilder(uei, EVENT_SOURCE);
        bldr.setHost(Capsd.getLocalHostAddress());
        return bldr;
    }

    private void sendEvent(Event newEvent) {
        // Send event to Eventd
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(newEvent);

            if (log().isDebugEnabled())
                log().debug("sendEvent: successfully sent: "+toString(newEvent));
        } catch (Throwable t) {
            log().warn("run: unexpected throwable exception caught during send to middleware", t);
        }
    }

    private String toString(Event e) {
        StringBuilder buf = new StringBuilder();
        buf.append("Event uei: ").append(e.getUei());
        buf.append(" For ").append(e.getNodeid()).append('/').append(e.getInterface()).append('/').append(e.getService());
        return buf.toString();
    }

    /**
     * This method is responsible for creating and sending a
     * 'duplicateIPAddress' event to Eventd
     * 
     * @param nodeId
     *            Interface's parent node identifier.
     * @param ipAddr
     *            Interface's IP address
     */
    private void createAndSendDuplicateIpaddressEvent(int nodeId, String ipAddr) {
        // create the event to be sent
        
        EventBuilder bldr = createEventBuilder(EventConstants.DUPLICATE_IPINTERFACE_EVENT_UEI);
        bldr.setNodeid(nodeId);
        bldr.setInterface(addr(ipAddr));
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, getHostName(ipAddr));
        bldr.addParam(EventConstants.PARM_METHOD, "icmp");
        
        sendEvent(bldr.getEvent());

    }

    private String getHostName(String ipAddr) {
        String hostName = InetAddressUtils.normalize(ipAddr);
        return hostName == null? "" : hostName;
    }

    /**
     * This method is responsible for creating and sending a
     * 'nodeGainedInterface' event to Eventd
     * 
     * @param nodeId
     *            Interface's parent node identifier.
     * @param ipAddr
     *            Interface's IP address
     */
    private void createAndSendNodeGainedInterfaceEvent(int nodeId, InetAddress ipAddr) {
        
        EventBuilder bldr = createEventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI);
        bldr.setNodeid(nodeId);
        bldr.setInterface(ipAddr);
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ipAddr.getHostName());
        bldr.addParam(EventConstants.PARM_METHOD, "icmp");

        sendEvent(bldr.getEvent());

    }

    /**
     * This method is responsible for creating and sending a
     * 'nodeGainedService' event to Eventd
     * 
     * @param nodeEntry
     *            Interface's parent node identifier.
     * @param ipAddr
     *            Interface's IP address
     * @param svcName
     *            Service name
     * @param qualifier
     *            Service qualifier (typically the port on which the service
     *            was found)
     */
    private void createAndSendNodeGainedServiceEvent(DbNodeEntry nodeEntry, InetAddress ipAddr, String svcName, String qualifier) {
        
        EventBuilder bldr = createEventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);
        bldr.setNodeid(nodeEntry.getNodeId());
        bldr.setInterface(ipAddr);
        bldr.setService(svcName);
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ipAddr.getHostName());
        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeEntry.getLabel());
        bldr.addParam(EventConstants.PARM_NODE_LABEL_SOURCE, nodeEntry.getLabelSource());

        // Add qualifier (if available)
        if (qualifier != null && qualifier.length() > 0) {
            bldr.addParam(EventConstants.PARM_QUALIFIER, qualifier);
        }
        
        // Add sysName (if available)
        if (nodeEntry.getSystemName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSNAME, nodeEntry.getSystemName());
        }

        // Add sysDescr (if available)
        if (nodeEntry.getSystemDescription() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSDESCRIPTION, nodeEntry.getSystemDescription());
        }

        sendEvent(bldr.getEvent());

    }
    
    /**
     * This method is responsible for creating and sending a
     * 'suspectScanCompleted' event to Eventd
     * 
     * @param ipAddr
     * 			IP address of the interface for which the suspect scan has completed
     */
    private void createAndSendSuspectScanCompletedEvent(InetAddress ipAddr) {
    	EventBuilder bldr = createEventBuilder(EventConstants.SUSPECT_SCAN_COMPLETED_EVENT_UEI);
    	bldr.setInterface(ipAddr);
    	bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ipAddr.getHostName());
    	sendEvent(bldr.getEvent());
    }

} // end class
