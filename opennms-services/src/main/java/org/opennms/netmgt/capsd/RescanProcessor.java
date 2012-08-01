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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.DBUtils;
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

/**
 * This class is designed to rescan all the managed interfaces for a specified
 * node, update the database based on the information collected, and generate
 * events necessary to notify the other OpenNMS services. The constructor takes
 * an integer which is the node identifier of the node to be rescanned. .
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:jamesz@opennms.org">James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class RescanProcessor implements Runnable {
    private static final InetAddress ZERO_ZERO_ZERO_ZERO = addr("0.0.0.0");

	/**
     * SQL statement for retrieving the 'nodetype' field of the node table for
     * the specified nodeid. Used to determine if the node is active ('A') or
     * been marked as deleted ('D').
     */
    final static String SQL_DB_RETRIEVE_NODE_TYPE = "SELECT nodetype FROM node WHERE nodeID=?";

    /**
     * SQL statement used to retrieve other nodeIds that have the same
     * ipinterface as the updating node.
     */
    final static String SQL_DB_RETRIEVE_OTHER_NODES = "SELECT ipinterface.nodeid FROM ipinterface, node WHERE ipinterface.ismanaged != 'D' AND ipinterface.ipaddr = ? AND ipinterface.nodeid = node.nodeid AND node.foreignsource IS NULL AND ipinterface.nodeid !=? ";

    /**
     * SQL statements used to reparent an interface and its associated services
     * under a new parent nodeid
     */
    final static String SQL_DB_REPARENT_IP_INTERFACE_LOOKUP = "SELECT ipaddr, ifindex FROM ipinterface " + "WHERE nodeID=? AND ipaddr=? AND isManaged!='D'";

    final static String SQL_DB_REPARENT_IP_INTERFACE_DELETE = "DELETE FROM ipinterface " + "WHERE nodeID=? AND ipaddr=?";

    final static String SQL_DB_REPARENT_IP_INTERFACE = "UPDATE ipinterface SET nodeID=? WHERE nodeID=? AND ipaddr=? AND isManaged!='D'";

    final static String SQL_DB_REPARENT_SNMP_IF_LOOKUP = "SELECT id FROM snmpinterface WHERE nodeID=? AND snmpifindex=?";

    final static String SQL_DB_REPARENT_SNMP_IF_DELETE = "DELETE FROM snmpinterface " + "WHERE nodeID=? AND snmpifindex=?";

    final static String SQL_DB_REPARENT_SNMP_INTERFACE = "UPDATE snmpinterface SET nodeID=? WHERE nodeID=? AND snmpifindex=?";

    final static String SQL_DB_REPARENT_IF_SERVICES_LOOKUP = "SELECT serviceid FROM ifservices " + "WHERE nodeID=? AND ipaddr=? AND ifindex = ? " + "AND status!='D'";

    final static String SQL_DB_REPARENT_IF_SERVICES_DELETE = "DELETE FROM ifservices WHERE nodeID=? AND ipaddr=? ";

    final static String SQL_DB_REPARENT_IF_SERVICES = "UPDATE ifservices SET nodeID=? WHERE nodeID=? AND ipaddr=? AND status!='D'";

    /**
     * SQL statements used to clear up ipinterface table, ifservices table and
     * snmpinterface table when delete a duplicate node.
     */
    final static String SQL_DB_DELETE_DUP_INTERFACE = "DELETE FROM ipinterface WHERE nodeID=?";

    final static String SQL_DB_DELETE_DUP_SERVICES = "DELETE FROM ifservices WHERE nodeid=?";

    final static String SQL_DB_DELETE_DUP_SNMPINTERFACE = "DELETE FROM snmpinterface WHERE nodeid =?";

    /**
     * SQL statement used to retrieve service IDs so that service name can be
     * determined from ID, and map of service names
     */
    private final static String SQL_RETRIEVE_SERVICE_IDS = "SELECT serviceid,servicename  FROM service";

    /**
     * SQL statement used to update ipinterface.ismanaged
     * when rescan discovers new interface
     */
    private final static String SQL_DB_UPDATE_ISMANAGED = "UPDATE ipinterface SET ismanaged=? WHERE nodeID=? AND ipaddr=? AND isManaged!='D'";

    /**
     * Indicates if the rescan is in response to a forceRescan event.
     */
    private boolean m_forceRescan;

    /**
     * Event list...during the rescan significant database changes cause events
     * (interfaceReparented, nodeGainedService, and others) to be created and
     * added to the event list. The last thing the rescan process does is send
     * the events out.
     */
    private final List<Event> m_eventList = new ArrayList<Event>();

    /**
     * Set during the rescan to true if any of the ifIndex values associated
     * with a node's interface's were modified as a result of the scan.
     */
    private boolean m_ifIndexOnNodeChangedFlag;

    /**
     * Set during the rescan to true if a new interface is added to the
     * snmpInterfaces table or if any key fields of the node's snmpIntefaces
     * table were modified (such as ifIndex or ifType)
     */
    private boolean m_snmpIfTableChangedFlag;

    private CapsdDbSyncer m_capsdDbSyncer;

    private PluginManager m_pluginManager;

    private int m_nodeId;
    
    private static Set<Integer> m_queuedRescanTracker;

    /**
     * Constructor.
     * 
     * @param nodeInfo
     *            Scheduler.NodeInfo object containing the nodeid of the node to
     *            be rescanned.
     * @param forceRescan
     *            True if a forced rescan is to be performed (all interfaces not
     *            just managed interfaces scanned), false otherwise.
     */
    RescanProcessor(Scheduler.NodeInfo nodeInfo, boolean forceRescan, CapsdDbSyncer capsdDbSyncer, PluginManager pluginManager) {
        this(nodeInfo.getNodeId(), forceRescan, capsdDbSyncer, pluginManager);
    }
    
    /**
     * <p>Constructor for RescanProcessor.</p>
     *
     * @param nodeId a int.
     * @param forceRescan a boolean.
     * @param capsdDbSyncer a {@link org.opennms.netmgt.capsd.CapsdDbSyncer} object.
     * @param pluginManager a {@link org.opennms.netmgt.capsd.PluginManager} object.
     */
    public RescanProcessor(int nodeId, boolean forceRescan, CapsdDbSyncer capsdDbSyncer, PluginManager pluginManager) {
        m_nodeId = nodeId;
        m_forceRescan = forceRescan;
        m_capsdDbSyncer = capsdDbSyncer;
        m_pluginManager = pluginManager;
        
        // Add the node ID of the node to be rescanned to the Set that tracks
        // rescan requests
        synchronized (m_queuedRescanTracker) {
            m_queuedRescanTracker.add(nodeId);
        }
    }

    /**
     * This method is responsible for updating the node table using the most
     * recent data collected from the node's managed interfaces.
     * 
     * @param dbc
     *            Database connection
     * @param now
     *            Date object representing the time of the rescan.
     * @param dbNodeEntry
     *            DbNodeEntry object representing existing values in the
     *            database
     * @param currPrimarySnmpIf
     *            Primary SNMP interface address based on latest collection
     * @param dbIpInterfaces
     *            Array of DbIpInterfaceEntry objects representing all the
     *            interfaces retrieved from the database for this node.
     * @param collectorMap
     *            Map of IfCollector objects...one per managed interface.
     * 
     * @return DbNodeEntry object representing the updated values from the node
     *         table in the database.
     * 
     * @throws SQLException
     *             if there is a problem updating the node table.
     */
    private DbNodeEntry updateNode(Connection dbc, Date now, DbNodeEntry dbNodeEntry, InetAddress currPrimarySnmpIf, DbIpInterfaceEntry[] dbIpInterfaces, Map<String, IfCollector> collectorMap) throws SQLException {
        if (log().isDebugEnabled()) {
            log().debug("updateNode: updating node id " + dbNodeEntry.getNodeId());
        }

        /*
         * Clone the existing dbNodeEntry so we have all the original
         * values of the 'node' table fields in case we need to generate
         * 'node***Changed' events following the update.
         */
        DbNodeEntry originalDbNodeEntry = DbNodeEntry.clone(dbNodeEntry);

        /*
         * Create node which represents the most recently retrieved
         * information in the collector for this node
         */
        DbNodeEntry currNodeEntry = DbNodeEntry.create();
        currNodeEntry.setNodeType(DbNodeEntry.NODE_TYPE_ACTIVE);

        // Set node label and SMB info based on latest collection
        setNodeLabelAndSmbInfo(collectorMap, dbNodeEntry, currNodeEntry, currPrimarySnmpIf);

        // Set SNMP info
        if (currPrimarySnmpIf != null) {
            /*
             * We prefer to use the collector for the primary SNMP interface
             * to update SNMP data in the node table. However a collector
             * for the primary SNMP interface may not exist in the map if
             * a node has only recently had SNMP support enabled or if the
             * new primary SNMP interface was only recently added to the
             * node. At any rate if it exists use it, if not use the
             * first collector which supports SNMP.
             */
            final String addr = str(currPrimarySnmpIf);
			IfCollector primaryIfc = addr == null? null : collectorMap.get(addr);
            if (primaryIfc == null) {
                for (IfCollector tmp : collectorMap.values()) {
                    if (tmp.getSnmpCollector() != null) {
                        primaryIfc = tmp;
                        break;
                    }
                }
            }

            /*
             * Sanity check...should always have a primary interface
             * collector at this point
             */
            if (primaryIfc == null) {
                log().error("updateNode: failed to determine primary interface collector for node " + dbNodeEntry.getNodeId());
                throw new RuntimeException("Update node failed for node " + dbNodeEntry.getNodeId() + ", unable to determine primary interface collector.");
            }

            IfSnmpCollector snmpc = primaryIfc.getSnmpCollector();
            if (snmpc != null && snmpc.hasSystemGroup()) {
                SystemGroup sysgrp = snmpc.getSystemGroup();

                // sysObjectId
                currNodeEntry.setSystemOID(sysgrp.getSysObjectID());

                // sysName
                String sysName = sysgrp.getSysName();
                if (sysName != null && sysName.length() > 0) {
                    currNodeEntry.setSystemName(sysName);
                }

                // sysDescription
                String sysDescr = sysgrp.getSysDescr();
                if (sysDescr != null && sysDescr.length() > 0) {
                    currNodeEntry.setSystemDescription(sysDescr);
                }

                // sysLocation
                String sysLocation = sysgrp.getSysLocation();
                if (sysLocation != null && sysLocation.length() > 0) {
                    currNodeEntry.setSystemLocation(sysLocation);
                }

                // sysContact
                String sysContact = sysgrp.getSysContact();
                if (sysContact != null && sysContact.length() > 0) {
                    currNodeEntry.setSystemContact(sysContact);
                }
            }
        }

        /*
         * Currently, we do not use the ParentId except in mapping.
         * Unfortunately, this is never
         * set in the currNodeEntry so it gets reset here. As a workaround,
         * setting it to the old value.
         */

        currNodeEntry.updateParentId(dbNodeEntry.getParentId());

        // Update any fields which have changed
        if (log().isDebugEnabled()) {
            log().debug("updateNode: -------dumping old node-------: " + dbNodeEntry);
            log().debug("updateNode: -------dumping new node-------: " + currNodeEntry);
        }
        dbNodeEntry.updateParentId(currNodeEntry.getParentId());
        dbNodeEntry.updateNodeType(currNodeEntry.getNodeType());
        dbNodeEntry.updateSystemOID(currNodeEntry.getSystemOID());
        dbNodeEntry.updateSystemName(currNodeEntry.getSystemName());
        dbNodeEntry.updateSystemDescription(currNodeEntry.getSystemDescription());
        dbNodeEntry.updateSystemLocation(currNodeEntry.getSystemLocation());
        dbNodeEntry.updateSystemContact(currNodeEntry.getSystemContact());
        dbNodeEntry.updateNetBIOSName(currNodeEntry.getNetBIOSName());
        dbNodeEntry.updateDomainName(currNodeEntry.getDomainName());
        dbNodeEntry.updateOS(currNodeEntry.getOS());
        dbNodeEntry.setLastPoll(now);

        /*
         * Only update node label/source if original node entry is
         * not set to user-defined.
         */
        if (dbNodeEntry.getLabelSource() != DbNodeEntry.LABEL_SOURCE_USER) {
            dbNodeEntry.updateLabel(currNodeEntry.getLabel());
            dbNodeEntry.updateLabelSource(currNodeEntry.getLabelSource());
        }

        // Set event flags
        boolean nodeLabelChangedFlag = false;
        boolean nodeInfoChangedFlag = false;

        if (dbNodeEntry.hasLabelChanged() || dbNodeEntry.hasLabelSourceChanged()) {
            nodeLabelChangedFlag = true;
        }

        if (dbNodeEntry.hasSystemOIDChanged() || dbNodeEntry.hasSystemNameChanged() || dbNodeEntry.hasSystemDescriptionChanged() || dbNodeEntry.hasSystemLocationChanged() || dbNodeEntry.hasSystemContactChanged() || dbNodeEntry.hasNetBIOSNameChanged() || dbNodeEntry.hasDomainNameChanged() || dbNodeEntry.hasOSChanged()) {
            nodeInfoChangedFlag = true;
        }

        // Call store to update the database
        dbNodeEntry.store(dbc);

        // Create nodeLabelChanged event if necessary
        if (nodeLabelChangedFlag) {
            m_eventList.add(createNodeLabelChangedEvent(dbNodeEntry, originalDbNodeEntry));
        }

        // Create nodeInfoChangedEvent if necessary
        if (nodeInfoChangedFlag) {
            m_eventList.add(createNodeInfoChangedEvent(dbNodeEntry, originalDbNodeEntry));
        }

        return dbNodeEntry;
    }

	/**
     * This method is responsible for updating all of the interface's associated
     * with a node.
     * 
     * @param dbc
     *            Database connection.
     * @param now
     *            Date/time to be associated with the update.
     * @param node
     *            Node entry for the node being rescanned
     * @param collectorMap
     *            Map of IfCollector objects associated with the node.
     * @param doesSnmp
     *            Indicates that the interface does support SNMP
     * 
     * @throws SQLException
     *             if there is a problem updating the ipInterface table.
     */
    private void updateInterfaces(Connection dbc, Date now, DbNodeEntry node,
            Map<String, IfCollector> collectorMap, boolean doesSnmp)
    throws SQLException {
        /*
         * make sure we have a current PackageIpListMap
         * this was getting done once for each managed ip
         * interface in updateInterfaceInfo. Seems more
         * efficient to just do it once here, and then later
         * for new interfaces (which aren't in the DB yet at
         * this point) in updateInterfaceInfo.
         */
        log().debug("updateInterfaces: Rebuilding PackageIpListMap");
        PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();
        pollerCfgFactory.rebuildPackageIpListMap();

        /*
         * List of update interfaces
         * This list is maintained so that for nodes with multiple
         * interfaces which support SNMP, interfaces are not updated
         * more than once.
         */
        List<InetAddress> updatedIfList = new ArrayList<InetAddress>();

        IfSnmpCollector snmpCollector = null;

        if (doesSnmp) {
            /*
             * Reset modification flags. These flags are set by
             * the updateInterface() method when changes have been
             * detected which warrant further action (such as
             * generating an event).
             */
            m_ifIndexOnNodeChangedFlag = false;
            m_snmpIfTableChangedFlag = false;

            /*
             * Determine if any of the interface collector objects have
             * an SNMP collector associated with them. If so, use the first
             * interface with SNMP data collected to update all SNMP-found
             * interfaces.
             */
            IfCollector collectorWithSnmp = null;
            for (IfCollector tmp : collectorMap.values()) {
                if (tmp.getSnmpCollector() != null) {
                    collectorWithSnmp = tmp;
                    break;
                }
            }

            if (collectorWithSnmp != null) {
                snmpCollector = collectorWithSnmp.getSnmpCollector();

                updateInterface(dbc, now, node, collectorWithSnmp.getTarget(),
                                collectorWithSnmp.getTarget(),
                                collectorWithSnmp.getSupportedProtocols(),
                                snmpCollector, doesSnmp);
                updatedIfList.add(collectorWithSnmp.getTarget());

                // Update subtargets
                if (collectorWithSnmp.hasAdditionalTargets()) {
                    Map<InetAddress, List<SupportedProtocol>> subTargets = collectorWithSnmp.getAdditionalTargets();
                    for(InetAddress subIf : subTargets.keySet()) {
                        updateInterface(dbc, now, node,
                                        collectorWithSnmp.getTarget(), subIf,
                                        subTargets.get(subIf),
                                        snmpCollector, doesSnmp);
                        updatedIfList.add(subIf);
                    }
                }

                // Add any new non-IP interfaces
                if (collectorWithSnmp.hasNonIpInterfaces()) {
                    for(Integer ifIndex : collectorWithSnmp.getNonIpInterfaces()) {

                        updateNonIpInterface(dbc, now, node, ifIndex.intValue(),
                                             snmpCollector);
                    }
                }
            }
        }

        /*
         * Majority of interfaces should have been updated by this
         * point (provided the node supports SNMP). Only non-SNMP
         * interfaces and those associated with the node via
         * SMB (NetBIOS name) should remain. Loop through collector
         * map and update any remaining interfaces. Use the
         * updatedIfList object to filter out any interfaces which
         * have already been updated
         */

        // Iterate over interfaces from collection map
        for (IfCollector ifc : collectorMap.values()) {
            // Update target
            InetAddress ifaddr = ifc.getTarget();
            if (!updatedIfList.contains(ifaddr)) {
                updateInterface(dbc, now, node, ifc.getTarget(), ifaddr,
                                ifc.getSupportedProtocols(), snmpCollector,
                                doesSnmp);
                updatedIfList.add(ifaddr);
            }

            // Update subtargets
            if (ifc.hasAdditionalTargets()) {
                Map<InetAddress, List<SupportedProtocol>> subTargets = ifc.getAdditionalTargets();
                for(InetAddress subIf : subTargets.keySet()) {

                    if (!updatedIfList.contains(subIf)) {
                        updateInterface(dbc, now, node, ifc.getTarget(), subIf,
                                        subTargets.get(subIf),
                                        snmpCollector, doesSnmp);
                        updatedIfList.add(subIf);
                    }
                }
            }
        }
    }

    /**
     * This method is responsible for updating the ipInterface table entry for a
     * specific interface.
     * 
     * @param dbc
     *            Database Connection
     * @param now
     *            Date/time to be associated with the update.
     * @param node
     *            Node entry for the node being rescanned
     * @param ifIndex
     *            Interface index of non-IP interface to update
     * @param snmpc
     *            SNMP collector or null if SNMP not supported.
     * 
     * @throws SQLException
     *             if there is a problem updating the ipInterface table.
     */
    private void updateNonIpInterface(Connection dbc, Date now,
            DbNodeEntry node, int ifIndex, IfSnmpCollector snmpc)
    throws SQLException {
        if (log().isDebugEnabled()) {
            log().debug("updateNonIpInterface: node= " + node.getNodeId()
                      + " ifIndex= " + ifIndex);
        }

        // Sanity Check
        if (snmpc == null || snmpc.failed()) {
            return;
        }

        // Construct InetAddress object for "0.0.0.0" address
        InetAddress ifAddr = null;
        ifAddr = ZERO_ZERO_ZERO_ZERO;
        if (ifAddr == null) return;
        
        updateSnmpInfoForNonIpInterface(dbc, node, ifIndex, snmpc, ifAddr);
    }

    /**
     * SnmpInterface table updates for non-IP interface.
     */
    private void updateSnmpInfoForNonIpInterface(Connection dbc,
            DbNodeEntry node, int ifIndex, IfSnmpCollector snmpc,
            InetAddress ifAddr) throws SQLException {
        if (log().isDebugEnabled()) {
            log().debug("updateNonIpInterface: updating non-IP SNMP interface "
                      + "with nodeId=" + node.getNodeId() + " and ifIndex="
                      + ifIndex);
        }

        // Create and load SNMP Interface entry from the database
        boolean newSnmpIfTableEntry = false;
        DbSnmpInterfaceEntry dbSnmpIfEntry = DbSnmpInterfaceEntry.get(dbc,node.getNodeId(), ifIndex);
        if (dbSnmpIfEntry == null) {
            /*
             * SNMP Interface not found with this nodeId & ifIndex, create new
             * interface
             */
            if (log().isDebugEnabled()) {
                log().debug("updateNonIpInterface: non-IP SNMP interface with "
                          + "ifIndex " + ifIndex+ " not in database, creating "
                          + "new snmpInterface object.");
            }
            dbSnmpIfEntry = DbSnmpInterfaceEntry.create(node.getNodeId(), ifIndex);
            newSnmpIfTableEntry = true;
        }

        IfTableEntry ifte = findEntryByIfIndex(ifIndex, snmpc);
        IfXTableEntry ifxte = findXEntryByIfIndex(ifIndex, snmpc);

        /*
         * Make sure we have a valid IfTableEntry object and update
         * any values which have changed
         */
        if (ifte != null) {
            // index
            // dbSnmpIfEntry.updateIfIndex(ifIndex);

            /*
             * netmask
             *
             * NOTE: non-IP interfaces don't have netmasks so skip
             */

            updateType(ifte, dbSnmpIfEntry);
            updateDescription(ifIndex, ifte, dbSnmpIfEntry);
            updatePhysicalAddress(ifIndex, ifte, dbSnmpIfEntry);
            updateSpeed(ifIndex, ifte, ifxte, dbSnmpIfEntry);
            updateAdminStatus(ifte, dbSnmpIfEntry);
            updateOperationalStatus(ifte, dbSnmpIfEntry);
            updateName(ifIndex, snmpc, dbSnmpIfEntry);
            updateAlias(ifIndex, snmpc, dbSnmpIfEntry);
        } // end if valid ifTable entry

        /*
         * If this is a new interface or if any of the following
         * key fields have changed set the m_snmpIfTableChangedFlag
         * variable to TRUE. This will potentially trigger an event
         * which will cause the poller to reinitialize the primary
         * SNMP interface for the node.
         */
        // dbSnmpIfEntry.hasIfIndexChanged() ||
        if (!m_snmpIfTableChangedFlag
                && newSnmpIfTableEntry
                || dbSnmpIfEntry.hasTypeChanged()
                || dbSnmpIfEntry.hasNameChanged()
                || dbSnmpIfEntry.hasDescriptionChanged()
                || dbSnmpIfEntry.hasPhysicalAddressChanged()
                || dbSnmpIfEntry.hasAliasChanged()) {
            m_snmpIfTableChangedFlag = true;
        }

        // Update the database
        dbSnmpIfEntry.store(dbc);
    }

    /**
     * Find the ifTable entry for this interface.
     */
    private static IfTableEntry findEntryByIfIndex(int ifIndex, IfSnmpCollector snmpc) {
        
        if (snmpc.hasIfTable()) {
            return snmpc.getIfTable().getEntry(ifIndex);
        }
        
        return null;

    }

    /**
     * Find the ifXTable entry for this interface.
     */
    private static IfXTableEntry findXEntryByIfIndex(int ifIndex, IfSnmpCollector snmpc) {
        if (snmpc.hasIfXTable()) {
            return snmpc.getIfXTable().getEntry(ifIndex);
        }
        
        return null;
    }

    private static void updateAlias(int ifIndex, IfSnmpCollector snmpc, DbSnmpInterfaceEntry dbSnmpIfEntry) {
        // alias (from interface extensions table)
        String ifAlias = snmpc.getIfAlias(ifIndex);
        if (ifAlias != null) {
            dbSnmpIfEntry.updateAlias(ifAlias);
        } else {
            dbSnmpIfEntry.updateAlias("");
        }
    }

    private static void updateName(int ifIndex, IfSnmpCollector snmpc, DbSnmpInterfaceEntry dbSnmpIfEntry) {
        // name (from interface extensions table)
        String ifName = snmpc.getIfName(ifIndex);
        if (ifName != null && ifName.length() > 0) {
            dbSnmpIfEntry.updateName(ifName);
        }
    }

    private static void updateOperationalStatus(IfTableEntry ifte, DbSnmpInterfaceEntry dbSnmpIfEntry) {
        Integer sint = ifte.getIfOperStatus();
        if (sint == null) {
            dbSnmpIfEntry.updateOperationalStatus(0);
        } else {
            dbSnmpIfEntry.updateOperationalStatus(sint.intValue());
        }
    }

    private static void updateAdminStatus(IfTableEntry ifte, DbSnmpInterfaceEntry dbSnmpIfEntry) {
        Integer sint = ifte.getIfAdminStatus();
        if (sint == null) {
            dbSnmpIfEntry.updateAdminStatus(0);
        } else {
            dbSnmpIfEntry.updateAdminStatus(sint.intValue());
        }
    }

    private static void updateType(IfTableEntry ifte, DbSnmpInterfaceEntry dbSnmpIfEntry) {
        Integer sint = ifte.getIfType();
        if (sint == null) {
            dbSnmpIfEntry.updateType(0);
        } else {
            dbSnmpIfEntry.updateType(sint.intValue());
        }
    }

    private static void updateDescription(int ifIndex, IfTableEntry ifte, DbSnmpInterfaceEntry dbSnmpIfEntry) {
        String str = ifte.getIfDescr();
        if (log().isDebugEnabled()) {
            log().debug("updateNonIpInterface: ifIndex: " + ifIndex + " has ifDescription: " + str);
        }
        if (str != null && str.length() > 0) {
            dbSnmpIfEntry.updateDescription(str);
        }
    }

    private static void updatePhysicalAddress(int ifIndex, IfTableEntry ifte, DbSnmpInterfaceEntry dbSnmpIfEntry) {
        String physAddr = ifte.getPhysAddr();

        if (log().isDebugEnabled()) {
            log().debug("updateNonIpInterface: ifIndex: " + ifIndex + " has physical address '" + physAddr + "'");
        }

        if (physAddr != null && physAddr.length() == 12) {
            dbSnmpIfEntry.updatePhysicalAddress(physAddr);
        }
    }

    static void updateSpeed(int ifIndex, IfTableEntry ifte, IfXTableEntry ifxte, DbSnmpInterfaceEntry dbSnmpIfEntry) {
        
        
        long speed = 0;
        try {
            speed = getInterfaceSpeed(ifte, ifxte);
        } catch (Throwable e) {
            log().warn("updateNonIpInterface: ifSpeed '" + ifte.getDisplayString(IfTableEntry.IF_SPEED) + "' for ifIndex " + ifIndex + " is invalid, inserting 0: " + e, e);
            speed = 0;
        }

        dbSnmpIfEntry.updateSpeed(speed);

    }
    
    private static long getInterfaceSpeed(IfTableEntry ifte, IfXTableEntry ifxte) {
        if (ifxte != null && ifxte.getIfHighSpeed() != null && ifxte.getIfHighSpeed() > 4294) {
            return ifxte.getIfHighSpeed() * 1000000L; 
        }
        
        if (ifte != null && ifte.getIfSpeed() != null) {
            return ifte.getIfSpeed();
        }

        return 0;
    }



    private static ThreadCategory log() {
        return ThreadCategory.getInstance(RescanProcessor.class);
    }

    /**
     * This method is responsible for updating the ipInterface table entry for a
     * specific interface.
     * 
     * @param dbc
     *            Database Connection
     * @param now
     *            Date/time to be associated with the update.
     * @param node
     *            Node entry for the node being rescanned
     * @param target
     *            Target interface (from IfCollector.getTarget())
     * @param ifaddr
     *            Interface being updated.
     * @param protocols
     *            Protocols supported by the interface.
     * @param snmpc
     *            SNMP collector or null if SNMP not supported.
     * @param doesSnmp
     *            Indicates that the interface supports SNMP
     * 
     * @throws SQLException
     *             if there is a problem updating the ipInterface table.
     */
    private void updateInterface(Connection dbc, Date now, DbNodeEntry node,
            InetAddress target, InetAddress ifaddr, List<SupportedProtocol> protocols,
            IfSnmpCollector snmpc, boolean doesSnmp) throws SQLException {
        /*
         * Reparenting
         *
         * This sub-interface was not previously associated with this node. If
         * the sub-interface is already associated with another node we must do
         * one of the following:
         *
         * 1. If the target interface (the one being rescanned) appears to be an
         * interface alias all of the interfaces under the sub-interface's node
         * will be reparented under the nodeid of the target interface.
         *
         * 2. If however the interface is not an alias, only the sub-interface
         * will be reparented under the nodeid of the interface being rescanned.
         *
         * In the reparenting process, the database ipinterface, snmpinterface
         * and ifservices table entries associated with the reparented interface
         * will be "updated" to reflect the new nodeid. If the old node has
         * no remaining interfaces following the reparenting it will be marked
         * as deleted.
         */

        /*
         * Special case: Need to skip interface reparenting for '0.0.0.0'
         * interfaces as well as loopback interfaces ('127.*.*.*').
         */
        final String ifaddrString = str(ifaddr);
		if (log().isDebugEnabled()) {
            log().debug("updateInterface: updating interface "
                      + ifaddrString + "(targetIf="
                      + str(target) + ")");
            if (doesSnmp) {
                log().debug("updateInterface: the SNMP collection passed in is "
                          + "collected via "
                          + (snmpc ==  null ? "No SnmpCollection passed in (snmpc == null)" : str(snmpc.getCollectorTargetAddress())));
            }
        }

        boolean reparentFlag = false;
        boolean newIpIfEntry = false;
        int ifIndex = -1;

        DbIpInterfaceEntry dbIpIfEntry =
            DbIpInterfaceEntry.get(dbc,node.getNodeId(), ifaddr);

        if (doesSnmp && snmpc != null && snmpc.hasIpAddrTable()) {
            // Attempt to load IP Interface entry from the database
            ifIndex = snmpc.getIfIndex(ifaddr);
            if (log().isDebugEnabled()) {
                log().debug("updateInterface: interface = "
                          + ifaddrString + " ifIndex = " + ifIndex
                          + ". Checking for this address on other nodes.");
            }

            /*
             * the updating interface may have already existed in the
             * ipinterface table with different
             * nodeIds. If it exist in a different node, verify if all the
             * interfaces on that node
             * are contained in the snmpc of the updating interface. If they
             * are, reparent all
             * the interfaces on that node to the node of the updating
             * interface, otherwise, just add
             * the interface to the updating node.
             */
            // Verify that SNMP collection contains ipAddrTable entries
            IpAddrTable ipAddrTable = null;
            ipAddrTable = snmpc.getIpAddrTable();

            if (ipAddrTable == null) {
                log().error("updateInterface: null ipAddrTable in the SNMP "
                          + "collection");
            } else {
                if (ifaddrString.equals("0.0.0.0") || ifaddr.isLoopbackAddress()) {
                    if (log().isDebugEnabled()) {
                        log().debug("updateInterface: Skipping address from "
                                  + "snmpc ipAddrTable "
                                  + ifaddrString);
                    }
                } else {
                    if (log().isDebugEnabled()) {
                        log().debug("updateInterface: Checking address from "
                                  + "snmpc ipAddrTable "
                                  + ifaddrString);
                    }
                    
                    PreparedStatement stmt = null;
                    final DBUtils d = new DBUtils(RescanProcessor.class);
                    try {
                        stmt = dbc.prepareStatement(SQL_DB_RETRIEVE_OTHER_NODES);
                        d.watch(stmt);
                        stmt.setString(1, ifaddrString);
                        stmt.setInt(2, node.getNodeId());
                        
                        ResultSet rs = stmt.executeQuery();
                        d.watch(rs);
                        while (rs.next()) {
                            int existingNodeId = rs.getInt(1);
                            if (log().isDebugEnabled()) {
                                log().debug("updateInterface: ckecking for "
                                          + ifaddrString
                                          + " on existing nodeid  "
                                          + existingNodeId);
                            }
                            
                            DbNodeEntry suspectNodeEntry = DbNodeEntry.get(dbc, existingNodeId);
                            if (suspectNodeEntry == null) {
                                // This can happen if a node has been deleted.
                                continue;
                            }
                            
                            /*
                             * Retrieve list of interfaces associated with the
                             * old node
                             */
                            DbIpInterfaceEntry[] tmpIfArray = suspectNodeEntry.getInterfaces(dbc);
                            
                            /*
                             * Verify if the suspectNodeEntry is a duplicate
                             * node
                             */
                            if (areDbInterfacesInSnmpCollection(tmpIfArray, snmpc)) {
                                /*
                                 * Reparent each interface under the targets'
                                 * nodeid
                                 */
                                for (int i = 0; i < tmpIfArray.length; i++) {
                                    InetAddress addr = tmpIfArray[i].getIfAddress();
                                    int index = snmpc.getIfIndex(addr);
                                    
                                    // Skip non-IP or loopback interfaces
                                    final String addrString = str(addr);
									if (addrString == null || addrString.equals("0.0.0.0") || addr.isLoopbackAddress()) {
                                        continue;
                                    }
                                    
                                    if (log().isDebugEnabled()) {
                                        log().debug("updateInterface: "
                                                  + "reparenting interface "
                                                  + addrString
                                                  + " under node: "
                                                  + node.getNodeId()
                                                  + " from existing node: "
                                                  + existingNodeId);
                                    }
                                    
                                    reparentInterface(dbc, addr, index, node.getNodeId(), existingNodeId);
                                    
                                    // Create interfaceReparented event
                                    createInterfaceReparentedEvent(node, existingNodeId, addr);
                                }
                                
                                if (log().isDebugEnabled()) {
                                    log().debug("updateInterface: interface "
                                              + ifaddrString
                                              + " is added to node: "
                                              + node.getNodeId()
                                              + " by reparenting from existing "
                                              + "node: " + existingNodeId);
                                }
                                dbIpIfEntry = DbIpInterfaceEntry.get(dbc, node.getNodeId(), ifaddr);
                                reparentFlag = true;
                                
                                // delete duplicate node after reparenting.
                                deleteDuplicateNode(dbc, suspectNodeEntry);
                                createDuplicateNodeDeletedEvent(suspectNodeEntry);
                            }
                        }
                    }
                    
                    catch (SQLException e) {
                        log().error("SQLException while updating interface: " + ifaddrString + " on nodeid: " + node.getNodeId());
                        throw e;
                    } finally {
                        d.cleanUp();
                    }
                }
            }
        }

        /*
         * if no reparenting occurred on the updating interface, add it to the
         * updating node.
         */
        if (dbIpIfEntry == null) {
            /*
             * Interface not found with this nodeId so create new interface
             * entry
             */
            if (log().isDebugEnabled()) {
                log().debug("updateInterface: interface " + ifaddr + " ifIndex "
                          + ifIndex + " not in database under nodeid "
                          + node.getNodeId()
                          + ", creating new interface object.");
            }

            /*
             * If doesSnmp is set to true, the dbIpIfEntry must not be stored
             * to the database until the corresponding DbSnmpInterfaceEntry is
             * stored.
             */
            if (ifIndex == -1 && !doesSnmp) {
                dbIpIfEntry = DbIpInterfaceEntry.create(node.getNodeId(),
                                                        ifaddr);
            } else {
                dbIpIfEntry = DbIpInterfaceEntry.create(node.getNodeId(),
                                                        ifaddr, ifIndex);
                /*
                 * XXX uh, what????? - dj@opennms.org
                 * This wasn't getting done for some reason, so do it explicitly
                 */
                dbIpIfEntry.setIfIndex(ifIndex);
            }

            if (isDuplicateInterface(dbc, ifaddr, node.getNodeId())) {
                m_eventList.add(createDuplicateIpAddressEvent(dbIpIfEntry));
            }
            newIpIfEntry = true;
        }
        
        DbIpInterfaceEntry currIpIfEntry =
            getNewDbIpInterfaceEntry(node, snmpc, doesSnmp, ifaddr);

        /*
         * XXX Note that updateSnmpInfo only gets called if doesSnmp is
         * true, but a new dbIpIfEntry with an ifIndex might have been
         * create()ed above if ifIndex != -1 || doesSnmp.  This might be
         * a problem if doesSnmp is false but ifIndex != -1, as the ipInterface
         * entry will point an snmpInterface entry that might not exist.
         */
        if (doesSnmp && snmpc != null) {
            // update SNMP info if available
            updateSnmpInfo(dbc, node, snmpc, currIpIfEntry.getIfAddress(),
                           currIpIfEntry.getIfIndex());
        }

        // update ipinterface for the updating interface
        updateInterfaceInfo(dbc, now, node, dbIpIfEntry, currIpIfEntry,
                            newIpIfEntry, reparentFlag);

        // update IfServices for the updating interface
        updateServiceInfo(dbc, node, dbIpIfEntry, newIpIfEntry, protocols);

    }

    /**
     * This method is responsible to delete any interface associated with the
     * duplicate node, delete any entry left in ifservices table and
     * snmpinterface table for the duplicate node, and make the node as
     * 'deleted'.
     * 
     * @param dbc
     *            Database Connection
     * @param duplicateNode
     *            Duplicate node to delete.
     * 
     */
    private static void deleteDuplicateNode(Connection dbc, DbNodeEntry duplicateNode) throws SQLException {

        final DBUtils d = new DBUtils(RescanProcessor.class);
        try {
            PreparedStatement ifStmt = dbc.prepareStatement(SQL_DB_DELETE_DUP_INTERFACE);
            d.watch(ifStmt);
            PreparedStatement svcStmt = dbc.prepareStatement(SQL_DB_DELETE_DUP_SERVICES);
            d.watch(svcStmt);
            PreparedStatement snmpStmt = dbc.prepareStatement(SQL_DB_DELETE_DUP_SNMPINTERFACE);
            d.watch(snmpStmt);
            ifStmt.setInt(1, duplicateNode.getNodeId());
            svcStmt.setInt(1, duplicateNode.getNodeId());
            snmpStmt.setInt(1, duplicateNode.getNodeId());

            ifStmt.executeUpdate();
            svcStmt.executeUpdate();
            snmpStmt.executeUpdate();

            duplicateNode.setNodeType(DbNodeEntry.NODE_TYPE_DELETED);
            duplicateNode.store(dbc);
        } catch (SQLException sqlE) {
            log().error("deleteDuplicateNode  SQLException while deleting duplicate node: " + duplicateNode.getNodeId());
            throw sqlE;
        } finally {
            d.cleanUp();
        }

    }

    /**
     * This method verify if an ipaddress is existing in other node except in
     * the updating node.
     * 
     * @param dbc
     *            Database Connection
     * @param ifaddr
     *            Ip address being verified.
     * @param nodeId
     *            Node Id of the node being rescanned
     * 
     */
    private static boolean isDuplicateInterface(Connection dbc, InetAddress ifaddr, int nodeId) throws SQLException {

        boolean duplicate = false;

        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(RescanProcessor.class);
        try {
            stmt = dbc.prepareStatement(SQL_DB_RETRIEVE_OTHER_NODES);
            d.watch(stmt);
            stmt.setString(1, str(ifaddr));
            stmt.setInt(2, nodeId);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                duplicate = true;
            }
            return duplicate;
        } catch (SQLException sqlE) {
            log().error("isDuplicateInterface: SQLException while updating interface: " + str(ifaddr) + " on nodeid: " + nodeId);
            throw sqlE;
        } finally {
            d.cleanUp();
        }

    }

    /**
     * This method is responsible for updating the ipinterface table entry for a
     * specific interface.
     * 
     * @param dbc
     *            Database Connection.
     * @param now
     *            Date/time to be associated with the update.
     * @param node
     *            Node entry for the node being rescanned.
     * @param dbIpIfEntry
     *            interface entry of the updating interface.
     * @param snmpc
     *            SNMP collector or null if SNMP not supported.
     * @param isNewIpEntry
     *            if dbIpIfEntry is a new entry.
     * @param isReparented
     *            if dbIpIfEntry is reparented.
     * @param doesSnmp
     *            if node supports SNMP.
     * 
     * @throws SQLException
     *             if there is a problem updating the ipinterface table.
     */
    private void updateInterfaceInfo(Connection dbc, Date now, DbNodeEntry node,
            DbIpInterfaceEntry dbIpIfEntry, DbIpInterfaceEntry currIpIfEntry,
            boolean isNewIpEntry, boolean isReparented)
    throws SQLException {
        PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();

        InetAddress ifaddr = dbIpIfEntry.getIfAddress();

        /*
         * Clone the existing database entry so we have access to the values
         * of the database fields associated with the interface in the event
         * that something has changed.
         */
        DbIpInterfaceEntry originalIpIfEntry = DbIpInterfaceEntry.clone(dbIpIfEntry);

        // Update any fields which have changed
        dbIpIfEntry.setLastPoll(now);
        dbIpIfEntry.updateHostname(currIpIfEntry.getHostname());
        dbIpIfEntry.updateManagedState(currIpIfEntry.getManagedState());
        dbIpIfEntry.updateStatus(currIpIfEntry.getStatus());
        dbIpIfEntry.updatePrimaryState(currIpIfEntry.getPrimaryState());
        
        /*
         * XXX Note: the ifIndex will not be updated if updateIfIndex(-1)
         * is called.  In other words, an ifIndex of a value other than -1
         * (non-null in the database) will never change to -1 (which is null
         * in the database) by calling updateIfIndex.  setIfIndex does work,
         * however if m_useIfIndexAsKey is set in the DbIpInterfaceEntry,
         * no entries (or at least not the right entry) will be updated
         * because the WHERE clause for the UPDATE will be referring to the
         * *new* ifIndex.
         */
        dbIpIfEntry.updateIfIndex(currIpIfEntry.getIfIndex());

        /*
         * Set event flags
         * NOTE: Must set these flags prior to call to
         * DbIpInterfaceEntry.store()
         * method which will cause the change map to be cleared.
         */
        boolean ifIndexChangedFlag = false;
        boolean ipHostnameChangedFlag = false;

        if (dbIpIfEntry.hasIfIndexChanged()) {
            ifIndexChangedFlag = true;
        }

        if (dbIpIfEntry.hasHostnameChanged()) {
            ipHostnameChangedFlag = true;
        }

        // Update the database
        dbIpIfEntry.store(dbc);

        /*
         * If the interface was not already in the database under
         * the node being rescanned or some other node send a
         * nodeGainedInterface event.
         */
        if (isNewIpEntry && !isReparented) {
            createNodeGainedInterfaceEvent(dbIpIfEntry);
        }

        // InterfaceIndexChanged event
        if (log().isDebugEnabled()) {
            log().debug("updateInterfaceInfo: ifIndex changed: "
                      + ifIndexChangedFlag);
        }
        if (ifIndexChangedFlag) {
            m_eventList.add(createInterfaceIndexChangedEvent(dbIpIfEntry, originalIpIfEntry));
            m_ifIndexOnNodeChangedFlag = true;
        }

        // IPHostNameChanged event
        if (log().isDebugEnabled()) {
            log().debug("updateInterfaceInfo: hostname changed: "
                      + ipHostnameChangedFlag);
        }
        if (ipHostnameChangedFlag) {
            m_eventList.add(createIpHostNameChangedEvent(dbIpIfEntry, originalIpIfEntry));
        }
        
        if (isNewIpEntry) {
            /*
             * If it's new, the packageIpListMap needs to be rebuilt,
             * polling status rechecked, and ismanaged updated if necessary
             */
            boolean ipToBePolled = false;
            final String ifaddrString = str(ifaddr);
			log().debug("updateInterfaceInfo: rebuilding PackageIpListMap for "
                      + "new interface " + ifaddrString);
            PollerConfigFactory.getInstance().rebuildPackageIpListMap();
            org.opennms.netmgt.config.poller.Package ipPkg = ifaddrString == null? null : pollerCfgFactory.getFirstPackageMatch(ifaddrString);
            if (ipPkg != null) {
                ipToBePolled = true;
            }
            if (log().isDebugEnabled()) {
                log().debug("updateInterfaceInfo: interface " + ifaddrString + " to be polled: " + ipToBePolled);
            }
            if (ipToBePolled) {
                final DBUtils d = new DBUtils(RescanProcessor.class);
                try {
                    PreparedStatement stmt = dbc.prepareStatement(SQL_DB_UPDATE_ISMANAGED);
                    d.watch(stmt);
                    stmt.setString(1, new String(new char[] { DbIpInterfaceEntry.STATE_MANAGED }));
                    stmt.setLong(2, dbIpIfEntry.getNodeId());
                    stmt.setString(3, ifaddrString);
                    stmt.executeUpdate();
                    if (log().isDebugEnabled()) {
                        log().debug("updateInterfaceInfo: updated managed state "
                                  + "for new interface "
                                  + ifaddrString + " on node "
                                  + dbIpIfEntry.getNodeId() + " to managed");
                    }
                } finally {
                    d.cleanUp();
                }
            }
        }
    }

    /**
     * Create IP interface entry representing latest information
     * retrieved for the interface via the collector.  If doesSnmp is set to
     * <i>true</i>, this entry must <b>not</b> be stored to the database until
     * the corresponding DbSnmpInterfaceEntry is stored.
     */
    private static DbIpInterfaceEntry getNewDbIpInterfaceEntry(DbNodeEntry node,
            IfSnmpCollector snmpc, boolean doesSnmp,
            InetAddress ifaddr) {
        CapsdConfig cFactory = CapsdConfigFactory.getInstance();
        PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();
        
        int ifIndex = -1;
        
        DbIpInterfaceEntry currIpIfEntry;
        final String ifaddrString = str(ifaddr);
		if (doesSnmp) {
            if (snmpc != null && snmpc.hasIpAddrTable()) {
                ifIndex = snmpc.getIfIndex(ifaddr);
            }
            if (ifIndex == -1) {
                if (log().isDebugEnabled()) {
                    log().debug("updateInterfaceInfo: interface "
                              + ifaddrString
                              + " has no valid ifIndex. Assuming this is a "
                              + "lame SNMP host with no ipAddrTable");
                }
                ifIndex = CapsdConfig.LAME_SNMP_HOST_IFINDEX;
            }
            currIpIfEntry = DbIpInterfaceEntry.create(node.getNodeId(), ifaddr, ifIndex);
        } else {
            currIpIfEntry = DbIpInterfaceEntry.create(node.getNodeId(), ifaddr);
        }

        // Hostname
        currIpIfEntry.setHostname(ifaddr.getHostName());

        /*
         * Managed state
         * NOTE: (reference internal bug# 201)
         * If the ip is 'managed', it might still be 'not polled' based
         * on the poller configuration.
         *
         * Try to avoid re-evaluating the ip against filters for
         * each service, try to get the first package here and use
         * that for service evaluation
         *
         * At this point IF the ip is already in the database, package filter
         * evaluation should go through OK. New interfaces will be dealt with
         * later
         */
        org.opennms.netmgt.config.poller.Package ipPkg = null;

        if (cFactory.isAddressUnmanaged(ifaddr)) {
            currIpIfEntry.setManagedState(DbIpInterfaceEntry.STATE_UNMANAGED);
        } else {
            boolean ipToBePolled = false;
            ipPkg = ifaddrString == null? null : pollerCfgFactory.getFirstPackageMatch(ifaddrString);
            if (ipPkg != null) {
                ipToBePolled = true;
            }

            if (ipToBePolled) {
                currIpIfEntry.setManagedState(DbIpInterfaceEntry.STATE_MANAGED);
            } else {
                currIpIfEntry.setManagedState(DbIpInterfaceEntry.STATE_NOT_POLLED);
            }

            if (log().isDebugEnabled()) {
                log().debug("updateInterfaceInfo: interface "
                          + ifaddrString + " to be polled = "
                          + ipToBePolled);
            }
        }

        /*
         * If SNMP data collection is available set SNMP Primary state
         * as well as ifIndex and ifStatus.
         *
         * For all interfaces simply set 'isSnmpPrimary' field to
         * not eligible for now. Following the interface updates
         * the primary and secondary SNMP interfaces will be
         * determined and the value of the 'isSnmpPrimary' field
         * set accordingly for each interface. The old primary
         * interface should have already been saved for future
         * reference.
         */
        if (doesSnmp && snmpc != null && snmpc.hasIpAddrTable()) {
            if (ifIndex != -1) {
                if (snmpc.hasIfTable()) {
                    int status = snmpc.getAdminStatus(ifIndex);
                    currIpIfEntry.setStatus(status);
                }
            } else {
                // No ifIndex found
                log().debug("updateInterfaceInfo:  No ifIndex found for "
                          + ifaddrString
                          + ". Not eligible for primary SNMP interface.");
            }
            currIpIfEntry.setPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);
        } else if (doesSnmp) {
            currIpIfEntry.setPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);
        }
        return currIpIfEntry;
    }

    /**
     * This method is responsible for updating the ifservices table entry for a
     * specific interface.
     * 
     * @param dbc
     *            Database Connection.
     * @param node
     *            Node entry for the node being rescanned.
     * @param dbIpIfEntry
     *            interface entry of the updating interface.
     * @param isNewIpEntry
     *            if the dbIpIfEntry is a new entry.
     * @param protocols
     *            Protocols supported by the interface.
     * 
     * @throws SQLException
     *             if there is a problem updating the ifservices table.
     */
    private void updateServiceInfo(Connection dbc, DbNodeEntry node, DbIpInterfaceEntry dbIpIfEntry, boolean isNewIpEntry, List<SupportedProtocol> protocols) throws SQLException {
        CapsdConfig cFactory = CapsdConfigFactory.getInstance();
        PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();
        org.opennms.netmgt.config.poller.Package ipPkg = null;

        InetAddress ifaddr = dbIpIfEntry.getIfAddress();

        // Retrieve from the database the interface's service list
        DbIfServiceEntry[] dbSupportedServices = dbIpIfEntry.getServices(dbc);

        int ifIndex = dbIpIfEntry.getIfIndex();

        if (log().isDebugEnabled()) {
            if (ifIndex == -1) {
                log().debug("updateServiceInfo: Retrieving interface's service list from database for host " + dbIpIfEntry.getHostname());
            } else {
                log().debug("updateServiceInfo: Retrieving interface's service list from database for host " + dbIpIfEntry.getHostname() + " ifindex " + ifIndex);
            }
        }
        
        /*
         * add newly supported protocols
         *		
         * NOTE!!!!!: (reference internal bug# 201)
         * If the ip is 'managed', the service can still be 'not polled'
         * based on the poller configuration - at this point the ip is already
         * in the database, so package filter evaluation should go through OK
         */
        if (log().isDebugEnabled()) {
            log().debug("updateServiceInfo: Checking for new services on host "
                      + dbIpIfEntry.getHostname());
        }

        Iterator<SupportedProtocol> iproto = protocols.iterator();
        while (iproto.hasNext()) {
            SupportedProtocol p = iproto.next();
            Number sid = m_capsdDbSyncer.getServiceId(p.getProtocolName());

            /*
             * Only adding newly supported services so check against the service
             * list retrieved from the database
             */
            boolean found = false;
            for (int i = 0; i < dbSupportedServices.length && !found; i++) {
                if (dbSupportedServices[i].getServiceId() == sid.intValue()) {
                    found = true;
                }
            }

            if (!found) {
                DbIfServiceEntry ifSvcEntry = DbIfServiceEntry.create(node.getNodeId(), ifaddr, sid.intValue());

                // now fill in the entry
                final String ifaddrString = str(ifaddr);
				if (cFactory.isAddressUnmanaged(ifaddr)) {
                    ifSvcEntry.setStatus(DbIfServiceEntry.STATUS_UNMANAGED);
                } else {
                    ipPkg = ifaddrString == null? null : pollerCfgFactory.getFirstPackageMatch(ifaddrString);
                    if (ipPkg == null) {
                    	ifSvcEntry.setStatus(DbIfServiceEntry.STATUS_NOT_POLLED);
                    } else if (isServicePolledLocally(ifaddrString, p.getProtocolName(), ipPkg)) {
                        ifSvcEntry.setStatus(DbIfServiceEntry.STATUS_ACTIVE);
                    } else if (isServicePolled(ifaddrString, p.getProtocolName(), ipPkg)) {
                        ifSvcEntry.setStatus(DbIpInterfaceEntry.STATE_REMOTE);
                    } else {
                        ifSvcEntry.setStatus(DbIfServiceEntry.STATUS_NOT_POLLED);
                    }
                }

                /*
                 * Set qualifier if available. Currently the qualifier field
                 * is used to store the port at which the protocol was found.
                 */
                if (p.getQualifiers() != null && p.getQualifiers().get("port") != null) {
                    try {
                        Integer port = (Integer) p.getQualifiers().get("port");
                        if (log().isDebugEnabled()) {
                            log().debug("updateServiceInfo: got a port qualifier: " + port + " for service: " + p.getProtocolName());
                        }
                        ifSvcEntry.setQualifier(port.toString());
                    } catch (ClassCastException ccE) {
                        // Do nothing
                    }
                }

                ifSvcEntry.setSource(DbIfServiceEntry.SOURCE_PLUGIN);
                ifSvcEntry.setNotify(DbIfServiceEntry.NOTIFY_ON);

                if (ifIndex != -1) {
                    ifSvcEntry.setIfIndex(ifIndex);
                }

                ifSvcEntry.store();

                if (log().isDebugEnabled()) {
                    log().debug("updateIfServices: update service: " + p.getProtocolName() + " for interface:" + ifaddrString + " on node:" + node.getNodeId());
                }

                // Generate nodeGainedService event
                m_eventList.add(createNodeGainedServiceEvent(node, dbIpIfEntry, p.getProtocolName()));

                /*
                 * If this interface already existed in the database and SNMP
                 * service has been gained then create interfaceSupportsSNMP
                 * event
                 */
                if (!isNewIpEntry && p.getProtocolName().equalsIgnoreCase("SNMP")) {
                    m_eventList.add(createInterfaceSupportsSNMPEvent(dbIpIfEntry));
                }
            }
            // Update the supported services list
            dbSupportedServices = dbIpIfEntry.getServices(dbc);
        } // end while(more protocols)
        
        if (m_forceRescan) {
            updateServicesOnForcedRescan(node, dbIpIfEntry, dbSupportedServices);
        }
    }

    private static boolean isServicePolled(String ifAddr, String svcName, org.opennms.netmgt.config.poller.Package ipPkg) {
        boolean svcToBePolled = false;
        if (ipPkg != null) {
            svcToBePolled = PollerConfigFactory.getInstance().isPolled(svcName, ipPkg);
            if (!svcToBePolled)
                svcToBePolled = PollerConfigFactory.getInstance().isPolled(ifAddr, svcName);
        }
        return svcToBePolled;
    }

    private static boolean isServicePolledLocally(String ifAddr, String svcName, org.opennms.netmgt.config.poller.Package ipPkg) {
        boolean svcToBePolled = false;
        if (ipPkg != null && !ipPkg.getRemote()) {
            svcToBePolled = PollerConfigFactory.getInstance().isPolled(svcName, ipPkg);
            if (!svcToBePolled)
                svcToBePolled = PollerConfigFactory.getInstance().isPolledLocally(ifAddr, svcName);
        }
        return svcToBePolled;
    }

    /**
     * This method is responsible for updating the status of services for an
     * interface during a forced rescan
     * 
     * @param node
     *            Node entry for the node being rescanned
     * @param dbIpIfEntry
     *            interface entry of the updating interface
     * @param dbSupportedServices
     *            services on the updating interface
     * 
     * @throws SQLException
     *             if there is a problem updating the snmpInterface table.
     */
    private void updateServicesOnForcedRescan(DbNodeEntry node, DbIpInterfaceEntry dbIpIfEntry, DbIfServiceEntry[] dbSupportedServices) throws SQLException {
        /*
         * Now process previously existing protocols to update polling status.
         * Additional checks on forced rescan for existing services go here.
         * Specifically, has service been forced managed/unmanaged or has
         * polling status changed?
         */

        PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();
        CapsdConfig cFactory = CapsdConfigFactory.getInstance();
        InetAddress ifaddr = dbIpIfEntry.getIfAddress();
        org.opennms.netmgt.config.poller.Package ipPkg = null;

        boolean ipToBePolled = false;
        final String ifaddrString = str(ifaddr);
        ipPkg = ifaddrString == null? null : pollerCfgFactory.getFirstPackageMatch(ifaddrString);
        if (ipPkg != null) {
            ipToBePolled = true;
        }

        if (log().isDebugEnabled()) {
            log().debug("updateServicesOnForcedRescan: Checking status of existing services on host " + ifaddr);
        }

        // Get service names from database
        java.sql.Connection ctest = null;
        ResultSet rs = null;
        Map<Integer, String> serviceNames = new HashMap<Integer, String>();
        final DBUtils d = new DBUtils(RescanProcessor.class);
        try {
            ctest = DataSourceFactory.getInstance().getConnection();
            d.watch(ctest);
            PreparedStatement loadStmt = ctest.prepareStatement(SQL_RETRIEVE_SERVICE_IDS);
            d.watch(loadStmt);

            // go ahead and load the service table
            rs = loadStmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                Integer id = new Integer(rs.getInt(1));
                String name = rs.getString(2);
                serviceNames.put(id, name);
            }
        } catch (Throwable t) {
            log().error("Error reading services table", t);
        } finally {
            d.cleanUp();
        }

        for (int i = 0; i < dbSupportedServices.length; i++) {
            Integer id = dbSupportedServices[i].getServiceId();
            String sn = (serviceNames.get(id)).toString();

            DbIfServiceEntry ifSvcEntry = DbIfServiceEntry.get(node.getNodeId(), ifaddr, dbSupportedServices[i].getServiceId());
            if (log().isDebugEnabled()) {
                log().debug("updateServicesOnForcedRescan: old status for nodeId " + node.getNodeId() + ", ifaddr " + ifaddr + ", serviceId " + dbSupportedServices[i].getServiceId() + " = " + ifSvcEntry.getStatus());
            }

            // now fill in the entry

            boolean svcChangeToActive = false;
            boolean svcChangeToNotPolled = false;
            boolean svcChangeToForced = false;
            if (!cFactory.isAddressUnmanaged(ifaddr)) {
                boolean svcToBePolled = false;
                if (ipToBePolled) {
                    if (ipPkg == null) {
                        ipPkg = pollerCfgFactory.getFirstPackageMatch(ifaddrString);
                    }
                    if (ipPkg != null) {
                        if (log().isDebugEnabled()) {
                            log().debug("updateServicesOnForcedRescan: Is service to be polled for package = " + ipPkg.getName() + ", service = " + sn);
                        }
                        svcToBePolled = pollerCfgFactory.isPolled(sn, ipPkg);
                        if (!svcToBePolled) {
                            if (log().isDebugEnabled()) {
                                log().debug("updateServicesOnForcedRescan: Is service to be polled for ifaddr = " + ifaddrString + ", service = " + sn);
                            }
                            svcToBePolled = pollerCfgFactory.isPolled(ifaddrString, sn);
                        }
                        if (!svcToBePolled) {
                            log().debug("updateServicesOnForcedRescan: Service not to be polled");
                        }
                    } else {
                        log().debug("updateServicesOnForcedRescan: No poller package found");
                    }
                } else {
                    log().debug("updateServicesOnForcedRescan: Service not polled because interface is not polled");
                        
                }

                if (ifSvcEntry.getStatus() == DbIfServiceEntry.STATUS_FORCED) {
                    if (svcToBePolled) {
                        // Do nothing
                        log().debug("updateServicesOnForcedRescan: status = FORCED. No action taken.");
                    } else {
                        // change the status to "N"
                        ifSvcEntry.updateStatus(DbIfServiceEntry.STATUS_NOT_POLLED);
                        svcChangeToNotPolled = true;
                        log().debug("updateServicesOnForcedRescan: status = FORCED. Changed to NOT_POLLED");
                    }
                } else if (ifSvcEntry.getStatus() == DbIfServiceEntry.STATUS_SUSPEND) {
                    if (svcToBePolled) {
                        // change the status to "F"
                        ifSvcEntry.updateStatus(DbIfServiceEntry.STATUS_FORCED);
                        svcChangeToForced = true;
                        log().debug("updateServicesOnForcedRescan: status = SUSPEND. Changed to FORCED");
                    } else {
                        // change the status to "N"
                        ifSvcEntry.updateStatus(DbIfServiceEntry.STATUS_NOT_POLLED);
                        svcChangeToNotPolled = true;
                        log().debug("updateServicesOnForcedRescan: status = SUSPEND. Changed to NOT_POLLED");
                    }
                } else if (ifSvcEntry.getStatus() == DbIfServiceEntry.STATUS_RESUME) {
                    if (svcToBePolled) {
                        // change the status to "A"
                        ifSvcEntry.updateStatus(DbIfServiceEntry.STATUS_ACTIVE);
                        svcChangeToActive = true;
                        log().debug("updateServicesOnForcedRescan: status = RESUME. Changed to ACTIVE");
                    } else {
                        // change the status to "N"
                        ifSvcEntry.updateStatus(DbIfServiceEntry.STATUS_NOT_POLLED);
                        svcChangeToNotPolled = true;
                        log().debug("updateServicesOnForcedRescan: status = RESUME. Changed to NOT_POLLED");
                    }
                } else if (svcToBePolled && ifSvcEntry.getStatus() != DbIfServiceEntry.STATUS_ACTIVE) {
                    // set the status to "A"
                    ifSvcEntry.updateStatus(DbIfServiceEntry.STATUS_ACTIVE);
                    svcChangeToActive = true;
                    log().debug("updateServicesOnForcedRescan: New status = ACTIVE");
                } else if (!svcToBePolled && ifSvcEntry.getStatus() == DbIfServiceEntry.STATUS_ACTIVE) {
                    // set the status to "N"
                    ifSvcEntry.updateStatus(DbIfServiceEntry.STATUS_NOT_POLLED);
                    svcChangeToNotPolled = true;
                    log().debug("updateServicesOnForcedRescan: New status = NOT_POLLED");
                } else {
                    log().debug("updateServicesOnForcedRescan: Status Unchanged");
                }
            }

            if (svcChangeToActive) {
                ifSvcEntry.store();
                m_eventList.add(createResumePollingServiceEvent(node, dbIpIfEntry, sn));
            } else if (svcChangeToNotPolled || svcChangeToForced) {
                ifSvcEntry.store();
                m_eventList.add(createSuspendPollingServiceEvent(node, dbIpIfEntry, sn));
            }
        }
    }

    /**
     * This method is responsible for updating the snmpInterface table entry for
     * a specific interface.
     * 
     * @param dbc
     *            Database Connection
     * @param node
     *            Node entry for the node being rescanned
     * @param dbIpIfEntry
     *            interface entry of the updating interface
     * @param snmpc
     *            SNMP collector or null if SNMP not supported.
     * 
     * @throws SQLException
     *             if there is a problem updating the snmpInterface table.
     */
    private void updateSnmpInfo(Connection dbc, DbNodeEntry node,
            IfSnmpCollector snmpc, InetAddress ifaddr, int ifIndex)
    throws SQLException {
        /*
         * If SNMP info is available update the snmpInterface table entry with
         * anything that has changed.
         */		
        if (snmpc != null && !snmpc.failed() && ifIndex != -1) {
            if (log().isDebugEnabled()) {
                log().debug("updateSnmpInfo: updating SNMP interface for "
                          + "nodeId/ifIndex="
                          + node.getNodeId() + "/" + ifIndex);
            }

            // Create and load SNMP Interface entry from the database
            boolean newSnmpIfTableEntry = false;
            DbSnmpInterfaceEntry dbSnmpIfEntry = 
                DbSnmpInterfaceEntry.get(dbc, node.getNodeId(), ifIndex);
            if (dbSnmpIfEntry == null) {
                /*
                 * SNMP Interface not found with this nodeId, create new
                 * interface
                 */
                if (log().isDebugEnabled()) {
                    log().debug("updateSnmpInfo: SNMP interface index " + ifIndex
                              + " not in database, creating new interface "
                              + "object.");
                }
                dbSnmpIfEntry = DbSnmpInterfaceEntry.create(node.getNodeId(),
                                                            ifIndex);
                newSnmpIfTableEntry = true;
            }

            /*
             * Create SNMP interface entry representing latest information
             * retrieved for the interface via the collector
             */
            DbSnmpInterfaceEntry currSnmpIfEntry =
                DbSnmpInterfaceEntry.create(node.getNodeId(), ifIndex);

            // Find the ifTable entry for this interface
            IfTable ift = snmpc.getIfTable();
            IfTableEntry ifte = null;
            for (IfTableEntry current : ift) {
                // index
                Integer sint = current.getIfIndex();
                if (sint != null) {
                    if (ifIndex == sint.intValue()) {
                        ifte = current;
                        break;
                    }
                }
            }

            // Make sure we have a valid IfTableEntry object
            if (ifte == null
                    && ifIndex == CapsdConfig.LAME_SNMP_HOST_IFINDEX) {
                 if (log().isDebugEnabled()) {
                    log().debug("updateSnmpInfo: interface "
                              + str(snmpc.getCollectorTargetAddress())
                              + " appears to be a lame SNMP host");
                 }
            } else if (ifte != null) {
                /*
                 * IP address and netmask
                 *
                 * WARNING: IfSnmpCollector.getIfAddressAndMask() ONLY returns
                 * the FIRST IP address and mask for a given interface as
                 * specified in the ipAddrTable.
                 */
                InetAddress[] aaddrs = snmpc.getIfAddressAndMask(ifIndex);


                if (aaddrs == null) {
                    // disable collection on interface with no ip address by default
                    currSnmpIfEntry.setCollect("N");
                } else {
                    // mark the interface is collection enable
                    currSnmpIfEntry.setCollect("C");

                    // netmask
                    if (aaddrs[1] != null) {
                        if (log().isDebugEnabled()) {
                            log().debug("updateSnmpInfo: interface "
                                        + str(aaddrs[0])
                                        + " has netmask: "
                                        + str(aaddrs[1]));
                        }
                        currSnmpIfEntry.setNetmask(aaddrs[1]);
                    }
                    
                } 

                // type
                Integer sint = ifte.getIfType();
                currSnmpIfEntry.setType(sint.intValue());

                // description
                String str = ifte.getIfDescr();
                if (log().isDebugEnabled()) {
                    log().debug("updateSnmpInfo: " + ifaddr
                              + " has ifDescription: " + str);
                }
                if (str != null && str.length() > 0) {
                    currSnmpIfEntry.setDescription(str);
                }

                String physAddr = ifte.getPhysAddr();

                if (log().isDebugEnabled()) {
                    log().debug("updateSnmpInfo: " + ifaddr
                              + " has phys address: -" + physAddr + "-");
                }

                if (physAddr != null && physAddr.length() == 12) {
                    currSnmpIfEntry.setPhysicalAddress(physAddr);
                }

                // speed
                Long speed = snmpc.getInterfaceSpeed(ifIndex);

                //set the default speed to 10MB if not retrievable.
                currSnmpIfEntry.setSpeed((speed == null
                        ? 10000000L : speed.longValue())); 

                // admin status
                sint = ifte.getIfAdminStatus();
                currSnmpIfEntry.setAdminStatus(sint == null ? 0 : sint.intValue());

                // oper status
                sint = ifte.getIfOperStatus();
                currSnmpIfEntry.setOperationalStatus(sint == null ? 0 : sint.intValue());

                // name (from interface extensions table)
                String ifName = snmpc.getIfName(ifIndex);
                if (ifName != null && ifName.length() > 0) {
                    currSnmpIfEntry.setName(ifName);
                }

                // alias (from interface extensions table)
                String ifAlias = snmpc.getIfAlias(ifIndex);
                if (ifAlias != null) {
                    currSnmpIfEntry.setAlias(ifAlias);
                } else {
                    currSnmpIfEntry.setAlias("");
                }		    

            } // end if valid ifTable entry

            // Update any fields which have changed
            // dbSnmpIfEntry.updateIfIndex(currSnmpIfEntry.getIfIndex());
            dbSnmpIfEntry.updateNetmask(currSnmpIfEntry.getNetmask());
            dbSnmpIfEntry.updatePhysicalAddress(currSnmpIfEntry.getPhysicalAddress());
            dbSnmpIfEntry.updateDescription(currSnmpIfEntry.getDescription());
            dbSnmpIfEntry.updateName(currSnmpIfEntry.getName());
            dbSnmpIfEntry.updateType(currSnmpIfEntry.getType());
            dbSnmpIfEntry.updateSpeed(currSnmpIfEntry.getSpeed());
            dbSnmpIfEntry.updateAdminStatus(currSnmpIfEntry.getAdminStatus());
            dbSnmpIfEntry.updateOperationalStatus(currSnmpIfEntry.getOperationalStatus());
            dbSnmpIfEntry.updateAlias(currSnmpIfEntry.getAlias());
            dbSnmpIfEntry.updateCollect(currSnmpIfEntry.getCollect());

            /*
             * If this is a new interface or if any of the following
             * key fields have changed set the m_snmpIfTableChangedFlag
             * variable to TRUE. This will potentially trigger an event
             * which will cause the poller to reinitialize the primary
             * SNMP interface for the node.
             */
            // dbSnmpIfEntry.hasIfIndexChanged() ||
            if (!m_snmpIfTableChangedFlag && newSnmpIfTableEntry
                    || dbSnmpIfEntry.hasTypeChanged()
                    || dbSnmpIfEntry.hasNameChanged()
                    || dbSnmpIfEntry.hasDescriptionChanged()
                    || dbSnmpIfEntry.hasPhysicalAddressChanged()
                    || dbSnmpIfEntry.hasAliasChanged()) {
                m_snmpIfTableChangedFlag = true;
            }

            // Update the database
            dbSnmpIfEntry.store(dbc);

            // end if complete snmp info available
        } else if (snmpc != null && snmpc.hasIpAddrTable() && ifIndex != -1) {
            if (log().isDebugEnabled()) {
                log().debug("updateSnmpInfo: updating SNMP interface for "
                          + "nodeId/ifIndex/ipAddr="
                          + node.getNodeId() + "/" + ifIndex + "/" + ifaddr
                          + " based on ipAddrTable only - No ifTable "
                          + "available");
            }

            // Create and load SNMP Interface entry from the database
            DbSnmpInterfaceEntry dbSnmpIfEntry =
                DbSnmpInterfaceEntry.get(dbc, node.getNodeId(), ifIndex);
            if (dbSnmpIfEntry == null) {
                /*
                 * SNMP Interface not found with this nodeId, create new
                 * interface
                 */
                if (log().isDebugEnabled()) {
                    log().debug("updateSnmpInfo: SNMP interface index " + ifIndex
                              + " not in database, creating new interface "
                              + "object.");
                }
                dbSnmpIfEntry = DbSnmpInterfaceEntry.create(node.getNodeId(),
                                                            ifIndex);
            }

            /*
             * Create SNMP interface entry representing latest information
             * retrieved for the interface via the collector
             */
            DbSnmpInterfaceEntry.create(node.getNodeId(), ifIndex);

            // Update the database
            dbSnmpIfEntry.store(dbc);
            // end if partial snmp info available
        } else if (snmpc != null) {
            // allow for lame snmp hosts with no ipAddrTable
            ifIndex = CapsdConfig.LAME_SNMP_HOST_IFINDEX;
            if (log().isDebugEnabled()) {
                log().debug("updateSnmpInfo: updating SNMP interface for "
                          + "nodeId/ipAddr=" + node.getNodeId() + "/" + ifaddr
                          + " based on ip address only - No ipAddrTable "
                          + "available");
            }

            // Create and load SNMP Interface entry from the database

            DbSnmpInterfaceEntry dbSnmpIfEntry =
                DbSnmpInterfaceEntry.get(dbc, node.getNodeId(), ifIndex);
            if (dbSnmpIfEntry == null) {
                /*
                 * SNMP Interface not found with this nodeId, create new
                 * interface
                 */
                if (log().isDebugEnabled()) {
                    log().debug("updateSnmpInfo: SNMP interface index " + ifIndex
                              + " not in database, creating new interface "
                              + "object.");
                }
                dbSnmpIfEntry = DbSnmpInterfaceEntry.create(node.getNodeId(),
                                                            ifIndex);

            }

            /*
             * Create SNMP interface entry representing latest information
             * retrieved for the interface via the collector
             */
            DbSnmpInterfaceEntry.create(node.getNodeId(), ifIndex);

            // Update the database
            dbSnmpIfEntry.store(dbc);
        }
    }

	/**
     * This method is responsible for reparenting an interface's database table
     * entries under its new node identifier. The following tables are updated:
     * 
     * ipInterface snmpInterface ifServices
     * 
     * @param dbc
     *            Database connection
     * @param ifAddr
     *            Interface to be reparented.
     * @param newNodeId
     *            Interface's new node identifier
     * @param oldNodeId
     *            Interfaces' old node identifier
     * 
     * @throws SQLException
     *             if a database error occurs during reparenting.
     */
    private static void reparentInterface(Connection dbc, InetAddress ifAddr, int ifIndex, int newNodeId, int oldNodeId) throws SQLException {
        String ipaddr = str(ifAddr);
        final DBUtils d = new DBUtils(RescanProcessor.class);

        try {
            PreparedStatement ifLookupStmt = dbc.prepareStatement(SQL_DB_REPARENT_IP_INTERFACE_LOOKUP);
            d.watch(ifLookupStmt);
            PreparedStatement ifDeleteStmt = dbc.prepareStatement(SQL_DB_REPARENT_IP_INTERFACE_DELETE);
            d.watch(ifDeleteStmt);
            PreparedStatement ipInterfaceStmt = dbc.prepareStatement(SQL_DB_REPARENT_IP_INTERFACE);
            d.watch(ipInterfaceStmt);
            PreparedStatement snmpIfLookupStmt = dbc.prepareStatement(SQL_DB_REPARENT_SNMP_IF_LOOKUP);
            d.watch(snmpIfLookupStmt);
            PreparedStatement snmpIfDeleteStmt = dbc.prepareStatement(SQL_DB_REPARENT_SNMP_IF_DELETE);
            d.watch(snmpIfDeleteStmt);
            PreparedStatement snmpInterfaceStmt = dbc.prepareStatement(SQL_DB_REPARENT_SNMP_INTERFACE);
            d.watch(snmpInterfaceStmt);
            PreparedStatement ifServicesLookupStmt = dbc.prepareStatement(SQL_DB_REPARENT_IF_SERVICES_LOOKUP);
            d.watch(ifServicesLookupStmt);
            PreparedStatement ifServicesDeleteStmt = dbc.prepareStatement(SQL_DB_REPARENT_IF_SERVICES_DELETE);
            d.watch(ifServicesDeleteStmt);
            PreparedStatement ifServicesStmt = dbc.prepareStatement(SQL_DB_REPARENT_IF_SERVICES);
            d.watch(ifServicesStmt);

            if (log().isDebugEnabled()) {
                log().debug("reparentInterface: reparenting address/ifIndex/nodeID: " + ipaddr + "/" + ifIndex + "/" + newNodeId);
            }


            /*
             * SNMP interface
             *
             * NOTE: Only reparent SNMP interfaces if we have valid ifIndex
             */
            if (ifIndex < 1) {
                log().debug("reparentInterface: don't have a valid ifIndex, skipping snmpInterface table reparenting.");
            } else {
                /*
                 * NOTE: Now that the snmpInterface table is uniquely keyed
                 * by nodeId and ifIndex we must only reparent the
                 * old entry if there isn't already an entry with
                 * the same nodeid/ifindex pairing. If it can't
                 * be reparented it will be deleted.
                 */

                /*
                 * Look for matching nodeid/ifindex for the entry to be
                 * reparented
                 */
                boolean alreadyExists = false;
                snmpIfLookupStmt.setInt(1, newNodeId);
                snmpIfLookupStmt.setInt(2, ifIndex);
                ResultSet rs = snmpIfLookupStmt.executeQuery();
                d.watch(rs);
                if (rs.next()) {
                    /*
                     * Looks like we got a match so just delete
                     * the entry from the old node
                     */
                    if (log().isDebugEnabled()) {
                        log().debug("reparentInterface: interface with ifindex " + ifIndex + " already exists under new node " + newNodeId + " in snmpinterface table, deleting from under old node " + oldNodeId);
                    }
                    alreadyExists = true;

                    snmpIfDeleteStmt.setInt(1, oldNodeId);
                    snmpIfDeleteStmt.setInt(2, ifIndex);

                    snmpIfDeleteStmt.executeUpdate();
                }

                if (alreadyExists == false) {
                    /*
                     * Update the 'snmpinterface' table entry so that this
                     * interface's nodeID is set to the value of reparentNodeID
                     */
                    if (log().isDebugEnabled()) {
                        log().debug("reparentInterface: interface with ifindex " + ifIndex + " does not yet exist under new node " + newNodeId + " in snmpinterface table, reparenting.");
                    }
                    
                    snmpInterfaceStmt.setInt(1, newNodeId);
                    snmpInterfaceStmt.setInt(2, oldNodeId);
                    snmpInterfaceStmt.setInt(3, ifIndex);

                    // execute and log
                    snmpInterfaceStmt.executeUpdate();
                }
            }

            // Look for matching nodeid/ifindex for the entry to be reparented
            boolean ifAlreadyExists = false;
            ifLookupStmt.setInt(1, newNodeId);
            ifLookupStmt.setString(2, ipaddr);
            ResultSet rs = ifLookupStmt.executeQuery();
            d.watch(rs);
            if (rs.next()) {
                /*
                 * Looks like we got a match so just delete
                 * the entry from the old node
                 */
                if (log().isDebugEnabled()) {
                    log().debug("reparentInterface: interface with ifindex " + ifIndex + " already exists under new node " + newNodeId + " in ipinterface table, deleting from under old node " + oldNodeId);
                }
                ifAlreadyExists = true;

                ifDeleteStmt.setInt(1, oldNodeId);
                ifDeleteStmt.setString(2, ipaddr);

                ifDeleteStmt.executeUpdate();
            }

            if (ifAlreadyExists == false) {
                /*
                 * Update the 'ipinterface' table entry so that this
                 * interface's nodeID is set to the value of reparentNodeID
                 */
                if (log().isDebugEnabled()) {
                    log().debug("reparentInterface: interface with ifindex " + ifIndex + " does not yet exist under new node " + newNodeId + " in ipinterface table, reparenting.");
                }

                ipInterfaceStmt.setInt(1, newNodeId);
                ipInterfaceStmt.setInt(2, oldNodeId);
                ipInterfaceStmt.setString(3, ipaddr);

                // execute and log
                ipInterfaceStmt.executeUpdate();
            }
            
            // Look for matching nodeid/ifindex for the entry to be reparented
            boolean ifsAlreadyExists = false;
            ifServicesLookupStmt.setInt(1, newNodeId);
            ifServicesLookupStmt.setString(2, ipaddr);
            ifServicesLookupStmt.setInt(3, ifIndex);
            rs = ifServicesLookupStmt.executeQuery();
            d.watch(rs);
            if (rs.next()) {
                /*
                 * Looks like we got a match so just delete
                 * the entry from the old node
                 */
                if (log().isDebugEnabled()) {
                    log().debug("reparentInterface: interface with ifindex " + ifIndex + " already exists under new node " + newNodeId + " in ifservices table, deleting from under old node " + oldNodeId);
                }
                ifsAlreadyExists = true;

                ifServicesDeleteStmt.setInt(1, oldNodeId);
                ifServicesDeleteStmt.setString(2, ipaddr);

                ifServicesDeleteStmt.executeUpdate();
            }

            if (ifsAlreadyExists == false) {
                /*
                 * Update the 'snmpinterface' table entry so that this
                 * interface's nodeID is set to the value of reparentNodeID
                 */
                if (log().isDebugEnabled()) {
                    log().debug("reparentInterface: interface with ifindex " + ifIndex + " does not yet exist under new node " + newNodeId + " in ifservices table, reparenting.");
                }

                /*
                 * Update the 'nodeID' field of all 'ifservices' table entries
                 * for the reparented interfaces.
                 */
                ifServicesStmt.setInt(1, newNodeId);
                ifServicesStmt.setInt(2, oldNodeId);
                ifServicesStmt.setString(3, ipaddr);

                // execute and log
                ifServicesStmt.executeUpdate();
            }

            if (log().isDebugEnabled()) {
                log().debug("reparentInterface: reparented " + ipaddr + " : ifIndex: " + ifIndex + " : oldNodeID: " + oldNodeId + " newNodeID: " + newNodeId);
            }
        } catch (SQLException sqlE) {
            log().error("SQLException while reparenting addr/ifindex/nodeid " + ipaddr + "/" + ifIndex + "/" + oldNodeId);
            throw sqlE;
        } finally {
            d.cleanUp();
        }
    }

    /**
     * Builds a list of InetAddress objects representing each of the interfaces
     * from the collector map object which support SNMP and have a valid ifIndex
     * and have an IfType of loopback.
     * 
     * This is part of a feature to choose a non 127.*.*.* loopback address as
     * the primary SNMP interface.
     * 
     * @param collectorMap
     *            Map of IfCollector objects containing data collected from all
     *            of the node's interfaces.
     * @param snmpc
     *            Reference to SNMP collection object
     * 
     * @return List of InetAddress objects.
     */
    private static List<InetAddress> buildLBSnmpAddressList(Map<String, IfCollector> collectorMap, IfSnmpCollector snmpc) {
        List<InetAddress> addresses = new ArrayList<InetAddress>();

        // Verify that we have SNMP info
        if (snmpc == null) {
            log().debug("buildLBSnmpAddressList: no SNMP info available...");
            return addresses;
        }
        if (!snmpc.hasIfTable()) {
            log().debug("buildLBSnmpAddressList: no SNMP ifTable available...");
            return addresses;
        }

        /*
         * To be eligible to be the primary SNMP interface for a node:
         * 
         * 1. The interface must support SNMP
         * 2. The interface must have a valid ifIndex.
         */
        Collection<IfCollector> values = collectorMap.values();
        Iterator<IfCollector> iter = values.iterator();
        while (iter.hasNext()) {
            IfCollector ifc = iter.next();

            // Add eligible target.
            InetAddress ifaddr = ifc.getTarget();

            if (addresses.contains(ifaddr) == false) {
                if (SuspectEventProcessor.supportsSnmp(ifc.getSupportedProtocols()) && SuspectEventProcessor.hasIfIndex(ifaddr, snmpc) && SuspectEventProcessor.getIfType(ifaddr, snmpc) == 24) {
                    if (log().isDebugEnabled()) {
                        log().debug("buildLBSnmpAddressList: adding target interface " + str(ifaddr) + " temporarily marked as primary!");
                    }
                    addresses.add(ifaddr);
                }
            }

            // Now go through list of sub-targets
            if (ifc.hasAdditionalTargets()) {
                Map<InetAddress, List<SupportedProtocol>> subTargets = ifc.getAdditionalTargets();
                for(InetAddress xifaddr : subTargets.keySet()) {

                    if (addresses.contains(xifaddr) == false) {
                        if (SuspectEventProcessor.supportsSnmp(subTargets.get(xifaddr)) && SuspectEventProcessor.hasIfIndex(xifaddr, snmpc) && SuspectEventProcessor.getIfType(xifaddr, snmpc) == 24) {
                            if (log().isDebugEnabled()) {
                                log().debug("buildLBSnmpAddressList: adding subtarget interface " + str(xifaddr) + " temporarily marked as primary!");
                            }
                            addresses.add(xifaddr);
                        }
                    }
                }
            }
        }

        return addresses;
    }

    /**
     * Builds a list of InetAddress objects representing each of the interfaces
     * from the collector map object which support SNMP and have a valid
     * ifIndex.
     * 
     * @param collectorMap
     *            Map of IfCollector objects containing data collected from all
     *            of the node's interfaces.
     * @param snmpc
     *            Reference to SNMP collection object
     * 
     * @return List of InetAddress objects.
     */
    private static List<InetAddress> buildSnmpAddressList(Map<String, IfCollector> collectorMap, IfSnmpCollector snmpc) {
        List<InetAddress> addresses = new ArrayList<InetAddress>();

        // Verify that we have SNMP info
        if (snmpc == null) {
            log().debug("buildSnmpAddressList: no SNMP info available...");
            return addresses;
        }

        /*
         * To be eligible to be the primary SNMP interface for a node:
         * 
         * 1. The interface must support SNMP
         * 2. The interface must have a valid ifIndex.
         */
        Collection<IfCollector> values = collectorMap.values();
        Iterator<IfCollector> iter = values.iterator();
        while (iter.hasNext()) {
            IfCollector ifc = iter.next();

            // Add eligible target.
            InetAddress ifaddr = ifc.getTarget();

            if (addresses.contains(ifaddr) == false) {
                if (SuspectEventProcessor.supportsSnmp(ifc.getSupportedProtocols()) && SuspectEventProcessor.hasIfIndex(ifaddr, snmpc)) {
                    if (log().isDebugEnabled()) {
                        log().debug("buildSnmpAddressList: adding target interface " + str(ifaddr) + " temporarily marked as primary!");
                    }
                    addresses.add(ifaddr);
                }
            }

            // Now go through list of sub-targets
            if (ifc.hasAdditionalTargets()) {
                Map<InetAddress, List<SupportedProtocol>> subTargets = ifc.getAdditionalTargets();
                
                for(InetAddress xifaddr : subTargets.keySet()) {
                    // Add eligible subtargets.
                    if (addresses.contains(xifaddr) == false) {
                        if (SuspectEventProcessor.supportsSnmp(subTargets.get(xifaddr)) && SuspectEventProcessor.hasIfIndex(xifaddr, snmpc)) {
                            if (log().isDebugEnabled()) {
                                log().debug("buildSnmpAddressList: adding subtarget interface " + str(xifaddr) + " temporarily marked as primary!");
                            }
                            addresses.add(xifaddr);
                        }
                    }
                }
            }
        }

        return addresses;
    }

    /**
     * This method is responsible for determining the primary IP interface for
     * the node being rescanned.
     * 
     * @param collectorMap
     *            Map of IfCollector objects containing data collected from all
     *            of the node's interfaces.
     * 
     * @return InetAddress The primary IP interface for the node or null if a
     *         primary interface for the node could not be determined.
     */
    private static InetAddress determinePrimaryIpInterface(Map<String, IfCollector> collectorMap) {
        Collection<IfCollector> values = collectorMap.values();
        Iterator<IfCollector> iter = values.iterator();
        InetAddress primaryIf = null;
        while (iter.hasNext()) {
            IfCollector ifc = iter.next();
            InetAddress currIf = ifc.getTarget();

            if (primaryIf == null) {
                primaryIf = currIf;
                continue;
            } else {
                // Test the target interface of the collector first.
                primaryIf = SuspectEventProcessor.compareAndSelectPrimary(currIf, primaryIf);

                // Now test each of the collected subtargets
                if (ifc.hasAdditionalTargets()) {
                    Map<InetAddress, List<SupportedProtocol>> subTargets = ifc.getAdditionalTargets();
                    Iterator<InetAddress> siter = subTargets.keySet().iterator();

                    while (siter.hasNext()) {
                        currIf = siter.next();
                        primaryIf = SuspectEventProcessor.compareAndSelectPrimary(currIf, primaryIf);
                    }
                }
            }
        }

        if (log().isDebugEnabled()) {
            if (primaryIf != null) {
                log().debug("determinePrimaryIpInterface: selected primary interface: " + str(primaryIf));
            } else {
                log().debug("determinePrimaryIpInterface: no primary interface found");
            }
        }
        return primaryIf;
    }

    /**
     * Primarily, this method is responsible for assigning the node's nodeLabel
     * value using information collected from the node's various interfaces.
     * Additionally, if the node talks NetBIOS/SMB, then the node's NetBIOS name
     * and operating system fields are assigned.
     * 
     * @param collectorMap
     *            Map of IfCollector objects, one per interface.
     * @param dbNodeEntry
     *            Node entry, as it exists in the database.
     * @param currNodeEntry
     *            Current node entry, as collected during the current rescan.
     * @param currPrimarySnmpIf
     *            Primary SNMP interface, as determined from the collection
     *            retrieved during the current rescan.
     */
    private static void setNodeLabelAndSmbInfo(Map<String, IfCollector> collectorMap, DbNodeEntry dbNodeEntry, DbNodeEntry currNodeEntry, InetAddress currPrimarySnmpIf) {
        boolean labelSet = false;

        /*
         * We are going to change the order in which labels are assigned.
         * First, we check DNS - the hostname of the primary interface.
         * Then we check SMB - next SNMP sysName - and finally IP address
         * This is different then in 1.0 - when SMB came first.
         */

        InetAddress primaryIf = null;

        if (!labelSet) {
            /*
             * If no label is set, attempt to get the hostname for the primary
             * SNMP interface.
             * Note: this was wrong prior to 1.0.1 - the method
             * determinePrimaryIpInterface
             * would return the lowest numbered interface, not necessarily the
             * primary SNMP interface.
             */
            if (currPrimarySnmpIf != null) {
                primaryIf = currPrimarySnmpIf;
            } else {
                primaryIf = determinePrimaryIpInterface(collectorMap);
            }
            if (primaryIf == null) {
                log().error("setNodeLabelAndSmbInfo: failed to find primary interface...");
            } else {
                String hostName = primaryIf.getHostName();
                if (!hostName.equals(str(primaryIf))) {
                    labelSet = true;

                    currNodeEntry.setLabel(hostName);
                    currNodeEntry.setLabelSource(DbNodeEntry.LABEL_SOURCE_HOSTNAME);
                }
            }
        }

        IfSmbCollector savedSmbcRef = null;

        // Does the node entry in database have a NetBIOS name?
        if (dbNodeEntry.getNetBIOSName() != null) {
            /*
             * Yes it does, so search through collected info for all
             * interfaces and see if any have a NetBIOS name
             * which matches the existing one in the database
             */
            Collection<IfCollector> values = collectorMap.values();
            Iterator<IfCollector> iter = values.iterator();
            while (iter.hasNext() && !labelSet) {
                IfCollector ifc = iter.next();
                IfSmbCollector smbc = ifc.getSmbCollector();
                if (smbc != null) {
                    if (smbc.getNbtName() != null) {
                        /*
                         * Save reference to first IfSmbCollector object
                         * for future use.
                         */
                        savedSmbcRef = smbc;

                        String netbiosName = smbc.getNbtName().toUpperCase();
                        if (netbiosName.equals(dbNodeEntry.getNetBIOSName())) {
                            // Found a match.
                            labelSet = true;

                            currNodeEntry.setLabel(netbiosName);
                            currNodeEntry.setLabelSource(DbNodeEntry.LABEL_SOURCE_NETBIOS);
                            currNodeEntry.setNetBIOSName(netbiosName);

                            if (smbc.getDomainName() != null) {
                                currNodeEntry.setDomainName(smbc.getDomainName());
                            }
                        }
                    }
                }
            }
        } else {
            /*
             * No it does not, attempt to find an interface
             * collector that does have a NetBIOS name and
             * save a reference to that collector
             */
            Collection<IfCollector> values = collectorMap.values();
            Iterator<IfCollector> iter = values.iterator();
            while (iter.hasNext()) {
                IfCollector ifc = iter.next();
                IfSmbCollector smbc = ifc.getSmbCollector();
                if (smbc != null && smbc.getNbtName() != null) {
                    savedSmbcRef = smbc;
                }
            }
        }

        /*
         * If node label has not yet been set and SMB info is available
         * use that info to set the node label and NetBIOS name
         */
        if (!labelSet && savedSmbcRef != null) {
            labelSet = true;

            currNodeEntry.setLabel(savedSmbcRef.getNbtName());
            currNodeEntry.setLabelSource(DbNodeEntry.LABEL_SOURCE_NETBIOS);
            currNodeEntry.setNetBIOSName(currNodeEntry.getLabel());

            if (savedSmbcRef.getDomainName() != null) {
                currNodeEntry.setDomainName(savedSmbcRef.getDomainName());
            }
        }

        /*
         * If we get this far no IP hostname or SMB info was available. Next we
         * want to use MIB-II sysName for the node label. The primary SNMP
         * interface has already been determined so use it if available.
         */
        if (!labelSet && currPrimarySnmpIf != null) {
            /*
             * We prefer to use the collector for the primary SNMP interface
             * however a collector for the primary SNMP interface may not exist
             * in the map if a node has only recently had SNMP support enabled
             * or if the new primary SNMP interface was only recently added to
             * the node. At any rate if it exists use it, if not use the
             * first collector which supports SNMP.
             */
            final String currPrimarySnmpAddress = str(currPrimarySnmpIf);
			IfCollector ifc = currPrimarySnmpAddress == null? null : collectorMap.get(currPrimarySnmpAddress);
            if (ifc == null) {
                Collection<IfCollector> collectors = collectorMap.values();
                Iterator<IfCollector> iter = collectors.iterator();
                while (iter.hasNext()) {
                    ifc = iter.next();
                    if (ifc.getSnmpCollector() != null) {
                        break;
                    }
                }
            }

            // Sanity check
            if (ifc == null || ifc.getSnmpCollector() == null) {
                log().warn("setNodeLabelAndSmbInfo: primary SNMP interface set to " + currPrimarySnmpAddress + " but no SNMP collector found.");
            } else {
                IfSnmpCollector snmpc = ifc.getSnmpCollector();
                SystemGroup sysgrp = snmpc.getSystemGroup();

                String str = sysgrp.getSysName();
                if (str != null && str.length() > 0) {
                    labelSet = true;
                    currNodeEntry.setLabel(str);
                    currNodeEntry.setLabelSource(DbNodeEntry.LABEL_SOURCE_SYSNAME);
                }
            }
        }

        if (!labelSet) {
            /*
             * If we get this far no SNMP info was available so we will default
             * to the IP address of the primary interface.
             */
            if (primaryIf != null) {
                currNodeEntry.setLabel(str(primaryIf));
                currNodeEntry.setLabelSource(DbNodeEntry.LABEL_SOURCE_ADDRESS);
            } else {
                /*
                 * If all else fails, just use the current values from
                 * the database.
                 */
                currNodeEntry.setLabel(dbNodeEntry.getLabel());
                currNodeEntry.setLabelSource(dbNodeEntry.getLabelSource());
            }
        }
    }

    /**
     * Utility method used to determine if the specified node has been marked as
     * deleted in the node table.
     * 
     * @param dbc
     *            Database connection.
     * @param nodeId
     *            Node identifier to check
     * 
     * @return TRUE if node has been marked as deleted, FALSE otherwise.
     */
    private static boolean isNodeDeleted(Connection dbc, int nodeId) throws SQLException {
        boolean nodeDeleted = false;

        /*
         * Prepare & execute the SQL statement to retrieve the 'nodetype' field
         * from the node table for the specified nodeid.
         */
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(RescanProcessor.class);
        try {
            stmt = dbc.prepareStatement(SQL_DB_RETRIEVE_NODE_TYPE);
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            rs.next();
            String nodeTypeStr = rs.getString(1);
            if (!rs.wasNull()) {
                char nodeType = nodeTypeStr.charAt(0);
                if (nodeType == DbNodeEntry.NODE_TYPE_DELETED) {
                    nodeDeleted = true;
                }
            }
        } finally {
            d.cleanUp();
        }

        return nodeDeleted;
    }

    /**
     * This method is used to verify if each interface on a node stored in the
     * database is in the specified SNMP data collection.
     * 
     * @param dbInterfaces
     *            the ipInterfaces on a node stored in the database
     * @param snmpc
     *            IfSnmpCollector object containing SNMP collected ipAddrTable
     *            information.
     * 
     * @return True if each ipInterface is contained in the ipAddrTable of the
     *         specified SNMP collection.
     * 
     */
    private static boolean areDbInterfacesInSnmpCollection(DbIpInterfaceEntry[] dbInterfaces, IfSnmpCollector snmpc) {
        // Sanity check...null parms?
        if (dbInterfaces == null || snmpc == null) {
            log().error("areDbInterfacesInSnmpCollection: empty dbInterfaces or IfSnmpCollector.");
            return false;
        }

        // SNMP collection successful?
        if (!snmpc.hasIpAddrTable()) {
            log().error("areDbInterfacesInSnmpCollection: SNMP Collector failed.");
            return false;
        }

        // Verify that SNMP collection contains ipAddrTable entries
        IpAddrTable ipAddrTable = null;

        if (snmpc.hasIpAddrTable()) {
            ipAddrTable = snmpc.getIpAddrTable();
        }

        if (ipAddrTable == null) {
            log().error("areDbInterfacesInSnmpCollection: null ipAddrTable in the SNMP collection");
            return false;
        }

        List<InetAddress> ipAddrList = ipAddrTable.getIpAddresses();

        /*
         * Loop through the interface table entries until there are no more
         * entries or we've found a match
         */
        for (final DbIpInterfaceEntry dbInterface : dbInterfaces) {
            InetAddress ipaddr = dbInterface.getIfAddress();
            
            // Skip non-IP or loopback interfaces
            final String ipaddrString = str(ipaddr);
			if (ipaddrString == null || ipaddrString.equals("0.0.0.0") || ipaddr.isLoopbackAddress()) {
                continue;
            }

            boolean found = false;
            for (final InetAddress addr : ipAddrList) {
                // Skip non-IP or loopback interfaces
                final String addrString = str(addr);
				if (addrString == null || addrString.equals("0.0.0.0") || addr.isLoopbackAddress()) {
                    continue;
                }

                if (ipaddrString.equals(addrString)) {
                    found = true;
                    if (log().isDebugEnabled()) {
                        log().debug("areDbInterfacesInSnmpCollection: found match for ipaddress: " + ipaddrString);
                    }
                    break;
                }
            }
            
            if (!found) {
                if (log().isDebugEnabled()) {
                    log().debug("areDbInterfacesInSnmpCollection: ipaddress : " + ipaddrString + " not in the SNMP collection. SNMP collection may not be usable.");
                }
                return false;
            }
        }

        return true;
    }

    /**
     * This is where all the work of the class is done.
     */
    public void run() {
        // perform rescan of the node
        DbNodeEntry dbNodeEntry = getNode();
        
        if (dbNodeEntry == null) {
            return;
        }
        
        if (dbNodeEntry.getForeignSource() != null) {
            log().info("Skipping rescan of node "+getNodeId()+" since it was imported with foreign source "+dbNodeEntry.getForeignSource());
            return;
        }
        
        if (log().isDebugEnabled()) {
            log().debug("start rescanning node: " + getNodeId());
        }

        DbIpInterfaceEntry[] dbInterfaces = getInterfaces(dbNodeEntry);
        
        if (dbInterfaces == null) {
            log().debug("no interfaces found in the database to rescan for node: "
                      + getNodeId());
            return;
        }

        // this indicates whether or not we found an iface the responds to snmp
        boolean doesSnmp = true;
        
        IpAddrTable ipAddTable = null;
        List<InetAddress> prevAddrList = null;
        boolean snmpcAgree = false;
        boolean gotSnmpc = false;
        Map<String, IfCollector> collectorMap =
            new HashMap<String, IfCollector>();
        Map<String, IfCollector> nonSnmpCollectorMap =
            new HashMap<String, IfCollector>();
        Set<InetAddress> probedAddrs = new HashSet<InetAddress>();

        boolean gotSnmpCollection = false;
        DbIpInterfaceEntry oldPrimarySnmpInterface =
            DbNodeEntry.getPrimarySnmpInterface(dbInterfaces);
        if (oldPrimarySnmpInterface != null) {
            gotSnmpCollection =
                scanPrimarySnmpInterface(oldPrimarySnmpInterface, collectorMap,
                                         probedAddrs);
        }

        if (!gotSnmpCollection) {
            /*
             * Run collector for each retrieved interface and add result
             * to a collector map.
             */
            for (int i = 0; i < dbInterfaces.length; i++) {
                log().info("run: Running collector for interface "+i+" of "+dbInterfaces.length);
                final InetAddress ifaddr = dbInterfaces[i].getIfAddress();
                final String ifaddrString = str(ifaddr);

                /*
                 * collect the information from the interface.
                 * NOTE: skip '127.*.*.*' and '0.0.0.0' addresses.
                 */
				if (!scannableInterface(dbInterfaces, ifaddr)) {
                    log().debug("run: skipping scan of address: "+ifaddrString);
                    continue;
                }

				if (ifaddrString == null) {
					log().debug("run: unable to scan inet address: " + ifaddr);
					continue;
				}
				
                if (log().isDebugEnabled()) {
                    log().debug("running collection for " + ifaddrString);
                }

                IfCollector collector = new IfCollector(m_pluginManager, ifaddr, true, probedAddrs);
                collector.run();

                IfSnmpCollector snmpc = collector.getSnmpCollector();
                if (snmpc != null) {
                    gotSnmpc = true;
                }
                if (snmpc != null && snmpc.hasIpAddrTable()
                        && snmpc.getIfIndex(snmpc.getCollectorTargetAddress()) != -1) {
                    if (areDbInterfacesInSnmpCollection(dbInterfaces, snmpc)) {
                        collectorMap.put(ifaddrString, collector);
                        gotSnmpCollection = true;
                        if (log().isDebugEnabled()) {
                            log().debug("SNMP data collected via "
                                      + ifaddrString);
                            log().debug("Adding " + ifaddrString
                                      + " to collectorMap for node: "
                                      + getNodeId());
                        }
                        snmpcAgree = false;
                        break;
                    } else if (ipAddTable == null) {
                        snmpcAgree = true;
                        collectorMap.put(ifaddrString, collector);
                        ipAddTable = snmpc.getIpAddrTable();
                        prevAddrList = ipAddTable.getIpAddresses();

                        if (log().isDebugEnabled()) {
                            log().debug("SNMP data collected via "
                                      + ifaddrString
                                      + " does not agree with database.  "
                                      + "Tentatively adding to the "
                                      + "collectorMap and continuing");
                            for(InetAddress a : prevAddrList) {
                                log().debug("IP address in list = " + a);
                            }
                        }
                    } else if (ipAddTable != null && snmpcAgree == true) {
                        ipAddTable = snmpc.getIpAddrTable();
                        List<InetAddress> addrList = ipAddTable.getIpAddresses();

                        boolean listMatch = true;
                        String jstring = null;
                        String kstring = null;
                        Iterator<InetAddress> j = prevAddrList.iterator();
                        Iterator<InetAddress> k = addrList.iterator();
                        while (j.hasNext()) {
                            jstring = j.next().toString();
                            if (k.hasNext()) {
                                kstring = k.next().toString();
                                if (jstring.equals(kstring)) {
                                    if (log().isDebugEnabled()) {
                                        log().debug(jstring + " = " + kstring);
                                    }
                                } else {
                                    if (log().isDebugEnabled()) {
                                        log().debug(jstring + " != " + kstring);
                                    }
                                    listMatch = false;
                                }
                            } else {
                                listMatch = false;
                            }
                        }
                        if (k.hasNext()) {
                            listMatch = false;
                        }
                        if (listMatch) {
                            log().debug("Current and previous address lists match");
                        } else {
                            log().debug("Current and previous address lists "
                                      + "DO NOT match");
                            snmpcAgree = false;
                        }
                        collector.deleteSnmpCollector();
                    }
                    if (snmpcAgree == false) {
                        if (log().isDebugEnabled()) {
                            log().debug("SNMP data collected via "
                                      + ifaddrString
                                      + " does not agree with database or with "
                                      + "other interface(s) on this node.");
                        }
                    }
                } else {
                    /*
                     * Build a non-SNMP collectorMap, skipping 127.*.*.*
                     * and 0.0.0.0
                     */
                    nonSnmpCollectorMap.put(ifaddrString, collector);
                    if (log().isDebugEnabled()) {
                        log().debug("Adding " + ifaddrString
                                  + " to nonSnmpCollectorMap for node: "
                                  + getNodeId());
                    }
                }
            }
        }

        if (!gotSnmpCollection && snmpcAgree == false) {
            /*
             * We didn't get a collection from a primary snmp interface,
             * and we didn't get a collection that agrees with the db, and
             * multiple interface collections don't agree with each other.
             * First check for lame SNMP host, otherwise use the
             * nonSnmpCollectorMap and set doesSnmp = false
             */
            collectorMap = nonSnmpCollectorMap;
            if (nonSnmpCollectorMap.size() == 1 && gotSnmpc) {
                doesSnmp = true;
                if (log().isDebugEnabled()) {
                    log().debug("node " + getNodeId()
                              + " appears to be a lame SNMP host... "
                              + "Proceeding");
                }
            } else {
                doesSnmp = false;
                if (log().isDebugEnabled()) {
                    if (gotSnmpc == false) {
                        log().debug("Could not collect SNMP data for node: "
                                  + getNodeId());
                    } else {
                        log().debug("Not using SNMP data for node: "
                                  + getNodeId() + ".  "
                                  + "Collection does not agree with database.");
                    }
                }
            }
        } else if (snmpcAgree == true) {
            /*
             * We didn't get a collection from a primary snmp interface,
             * and we didn't get a collection that agrees with the db, but
             * all collections we DID get agree with each other.
             * May want to create an event here
             */
            if (log().isDebugEnabled()) {
                log().debug("SNMP collection for node: "
                          + getNodeId()
                          + " does not agree with database, but there is no "
                          + "conflict among the interfaces on this node which "
                          + "respond to SNMP. Proceeding...");
            }
            m_eventList.add(createSnmpConflictsWithDbEvent(dbNodeEntry));
        }

        // Update the database
        Date now = null;
        Connection dbc = null;
        boolean updateCompleted = false;
        try {
            /*
             * Synchronize on the Capsd sync lock so we can check if
             * the interface is already in the database and perform
             * the necessary inserts in one atomic operation
             *	
             * The SuspectEventProcessor class is also synchronizing on this
             * lock prior to performing database inserts or updates.
             */
            log().debug("Waiting for capsd dbLock to process "
                      + getNodeId());
            synchronized (Capsd.getDbSyncLock()) {
                log().debug("Got capsd dbLock. processing "
                          + getNodeId());
                // Get database connection
                dbc = DataSourceFactory.getInstance().getConnection();

                /*
                 * There is a slight possibility that the node being rescanned
                 * has been deleted (due to reparenting) by another thread
                 * between the time this rescan was started and the database
                 * sync lock was grabbed. Verify that the current nodeid is
                 * still valid (ie, not deleted) before continuing.
                 */
                if (!isNodeDeleted(dbc, getNodeId())) {
                    // Update interface information
                    now = new Date();
                    updateInterfaces(dbc, now, dbNodeEntry, collectorMap,
                                     doesSnmp);
                    
                    if (doesSnmp) {
                        InetAddress oldPriIf = null;
                        if (oldPrimarySnmpInterface != null) {
                            oldPriIf = oldPrimarySnmpInterface.getIfAddress();
                        }
                        InetAddress newSnmpPrimaryIf =
                            updatePrimarySnmpInterface(dbc, dbNodeEntry,
                                                       collectorMap, oldPriIf);

                            updateNode(dbc, now, dbNodeEntry, newSnmpPrimaryIf,
                                       dbInterfaces, collectorMap);
                    }
                    updateCompleted = true;
                    m_eventList.add(createRescanCompletedEvent(dbNodeEntry));
                }
            }
        } catch (Throwable t) {
            log().error("Error updating records for node ID " + getNodeId() + ": " + t, t);
        } finally {
            // Finished with the database connection, close it.
            try {
                if (dbc != null) {
                    dbc.close();
                }
            } catch (SQLException e) {
                log().error("Error closing connection: " + e, e);
            }
            
            // Remove the node we just scanned from the tracker set
            synchronized (m_queuedRescanTracker) {
                m_queuedRescanTracker.remove(getNodeId());
            }
        }

        // Send events associcatd with the rescan
        if (updateCompleted) {
            // Send all events created during rescan process to eventd
            for (Event event : m_eventList) {
                try {
                    EventIpcManagerFactory.getIpcManager().sendNow(event);
                } catch (Throwable t) {
                    log().warn("run: unexpected throwable exception caught "
                             + "while sending event: " + t, t);
                }
            }
        }

        if (log().isDebugEnabled()) {
            log().debug((m_forceRescan ? "Forced r" : "R") + "escan "
                      + "for node w/ nodeid " + getNodeId()
                      + " completed.");
        }
    }

    /**
     * <p>scannableInterface</p>
     *
     * @param dbInterfaces an array of {@link org.opennms.netmgt.capsd.DbIpInterfaceEntry} objects.
     * @param ifaddr a {@link java.net.InetAddress} object.
     * @return a boolean.
     */
    protected static boolean scannableInterface(final DbIpInterfaceEntry[] dbInterfaces, final InetAddress ifaddr) {
        final String ifaddrString = str(ifaddr);
        if (ifaddrString == null) return false;

        final boolean localHostAddress = (ifaddr.isLoopbackAddress() && dbInterfaces.length > 1);
        final boolean nonIpAddress = ifaddrString.equals("0.0.0.0");
        final boolean scannable = !localHostAddress && !nonIpAddress;
        return scannable;
    }

    private int getNodeId() {
        return m_nodeId;
    }

    private boolean scanPrimarySnmpInterface(DbIpInterfaceEntry oldPrimarySnmpInterface, Map<String, IfCollector> collectorMap, Set<InetAddress> probedAddrs) {
        boolean gotSnmpCollection = false;

        /*
         * Run collector for DB primary snmp interface and add result
         * to a collector map.
         */
        final InetAddress ifaddr = oldPrimarySnmpInterface.getIfAddress();
        final String ifaddrString = str(ifaddr);

        if (ifaddrString == null) {
        	log().info("old primary SNMP interface has an invalid address: " + ifaddr);
        	return false;
        }

		if (log().isDebugEnabled()) {
            log().debug("running collection for DB primary SNMP interface " + ifaddrString);
        }
        IfCollector collector = new IfCollector(m_pluginManager, ifaddr, true, probedAddrs);
        collector.run();
        IfSnmpCollector snmpc = collector.getSnmpCollector();
        if (snmpc == null) {
            log().debug("SNMP Collector from DB primary SNMP interface is null");
        } else {
            gotSnmpCollection = true;
            collectorMap.put(ifaddrString, collector);
            if (log().isDebugEnabled()) {
                log().debug("SNMP data collected from DB primary SNMP interface " + ifaddrString);
            }
            if (!snmpc.hasIfTable()) {
                log().debug("SNMP Collector has no IfTable");
            }
            if (!snmpc.hasIpAddrTable() || snmpc.getIfIndex(snmpc.getCollectorTargetAddress()) == -1) {
                log().debug("SNMP Collector has no IpAddrTable. Assume its a lame SNMP host.");
            }
        }
        return gotSnmpCollection;
    }

    private InetAddress updatePrimarySnmpInterface(Connection dbc, DbNodeEntry dbNodeEntry, Map<String, IfCollector> collectorMap, InetAddress oldPriIf) throws SQLException {
        /*
         * Now that all interfaces have been added to the
         * database we can update the 'primarySnmpInterface'
         * field of the ipInterface table. Necessary because
         * the IP address must already be in the database
         * to evaluate against a filter rule.
         *
         * First create a list of eligible loopback interfaces
         * and a list of all eligible interfaces. Test in the
         * following order:
         * 
         * 1) strict = true (interface must be part of a Collectd
         * package) and loopback.
         * 
         * 2) strict = true and all eligible interfaces.
         * strict = false and loopback.
         * 
         * 4) strict = false and all eligible interfaces.
         */
        boolean strict = true;
        CollectdConfigFactory.getInstance().rebuildPackageIpListMap();
        IfSnmpCollector snmpc = findSnmpCollector(collectorMap);
        List<InetAddress> snmpLBAddresses = buildLBSnmpAddressList(collectorMap, snmpc);
        List<InetAddress> snmpAddresses = buildSnmpAddressList(collectorMap, snmpc);
        
        // first set the value of issnmpprimary for secondaries
        Iterator<InetAddress> iter = snmpAddresses.iterator();
        while(iter.hasNext()) {
            InetAddress addr = iter.next();
            final String addrString = str(addr);
			if (CollectdConfigFactory.getInstance().isServiceCollectionEnabled(addrString, "SNMP")) {
                final DBUtils d = new DBUtils(RescanProcessor.class);
                try {
                    PreparedStatement stmt = dbc.prepareStatement("UPDATE ipInterface SET isSnmpPrimary='S' WHERE nodeId=? AND ipAddr=? AND isManaged!='D'");
                    d.watch(stmt);
                    stmt.setInt(1, dbNodeEntry.getNodeId());
                    stmt.setString(2, addrString);
                    stmt.executeUpdate();
                    log().debug("updatePrimarySnmpInterface: updated " + addrString + " to secondary.");
                } finally {
                    d.cleanUp();
                }
            }
        }
        
        InetAddress newSnmpPrimaryIf = CapsdConfigFactory.getInstance().determinePrimarySnmpInterface(snmpLBAddresses, strict);
        String psiType = ConfigFileConstants.getFileName(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME) + " loopback addresses";

        if (newSnmpPrimaryIf == null) {
            newSnmpPrimaryIf = CapsdConfigFactory.getInstance().determinePrimarySnmpInterface(snmpAddresses, strict);
            psiType = ConfigFileConstants.getFileName(ConfigFileConstants.COLLECTD_CONFIG_FILE_NAME) + " addresses";
        }

        strict = false;
        if (newSnmpPrimaryIf == null) {
            newSnmpPrimaryIf = CapsdConfigFactory.getInstance().determinePrimarySnmpInterface(snmpLBAddresses, strict);
            psiType = "DB loopback addresses";
        }

        if (newSnmpPrimaryIf == null) {
            newSnmpPrimaryIf = CapsdConfigFactory.getInstance().determinePrimarySnmpInterface(snmpAddresses, strict);
            psiType = "DB addresses";
        }

        if (newSnmpPrimaryIf == null) {
            newSnmpPrimaryIf = snmpc.getCollectorTargetAddress();
            psiType = "SNMP collector target address";
        }

        if (newSnmpPrimaryIf != null) {
            if (log().isDebugEnabled()) {
                log().debug("updatePrimarySnmpInterface: primary SNMP interface is: " + newSnmpPrimaryIf + ", selected from " + psiType);
            }
            SuspectEventProcessor.setPrimarySnmpInterface(dbc, dbNodeEntry, newSnmpPrimaryIf, oldPriIf);
        } else {
            log().debug("SuspectEventProcessor: Unable to determine a primary SNMP interface");
        }   
        
        /*
         * Now that we've identified the new primary SNMP
         * interface we can determine if it is necessary to
         * generate certain SNMP data collection related
         * events
         */
        generateSnmpDataCollectionEvents(dbNodeEntry, oldPriIf, newSnmpPrimaryIf);
        return newSnmpPrimaryIf;
    }

    /**
     * @param collectorMap
     * @return
     */
    private static IfSnmpCollector findSnmpCollector(Map<String, IfCollector> collectorMap) {
        for (Iterator<IfCollector> iter = collectorMap.values().iterator(); iter.hasNext();) {
            IfCollector collector = iter.next();
            if (collector.hasSnmpCollection()) {
                return collector.getSnmpCollector();
            }
        }
        return null;
    }

    private DbIpInterfaceEntry[] getInterfaces(DbNodeEntry dbNodeEntry) {
        /*
         * If this is a forced rescan then retrieve all the interfaces
         * associated with this node and perform collections against them.
         * Otherwise, this is a regularly scheduled rescan, only the
         * node's managed interfaces are to be retrieved and collected.
         */ 
        DbIpInterfaceEntry[] dbInterfaces = null;

        /*
         * Retrieve list of interfaces associated with this nodeID
         * from the database
         */
        if (log().isDebugEnabled()) {
            log().debug("retrieving managed interfaces for node: " + getNodeId());
        }
        
        try {
            dbInterfaces = (m_forceRescan ? dbNodeEntry.getInterfaces() : dbNodeEntry.getManagedInterfaces());
        } catch (NullPointerException npE) {
            log().error("RescanProcessor: Null pointer when retrieving "+(m_forceRescan ? "" : "managed")+" interfaces for node " + getNodeId(), npE);
            log().error("Rescan failed for node w/ nodeid " + getNodeId());
        } catch (SQLException sqlE) {
            log().error("RescanProcessor: unable to load interface info for nodeId " + getNodeId() + " from the database.", sqlE);
            log().error("Rescan failed for node w/ nodeid " + getNodeId());
        }
        return dbInterfaces;
    }

    private DbNodeEntry getNode() {
        DbNodeEntry dbNodeEntry = null;

        /*
         * Get DbNodeEntry object which represents this node and
         * load it from the database
         */
        try {
            dbNodeEntry = DbNodeEntry.get(getNodeId());
        } catch (SQLException e) {
            log().error("RescanProcessor: unable to load node info for nodeId "
                      + getNodeId() + " from the database.", e);
            log().error("Rescan failed for node w/ nodeid "
                      + getNodeId());
        }
        return dbNodeEntry;
    }

    /**
     * Determines if any SNMP data collection related events need to be
     * generated based upon the results of the current rescan. If necessary will
     * generate one of the following events: 'reinitializePrimarySnmpInterface'
     * 'primarySnmpInterfaceChanged'
     * 
     * @param nodeEntry
     *            DbNodeEntry object of the node being rescanned.
     * @param oldPriIf
     *            Previous primary SNMP interface (from the DB).
     * @param primarySnmpIf
     *            Primary SNMP interface as determined by the current rescan.
     */
    private void generateSnmpDataCollectionEvents(DbNodeEntry nodeEntry, InetAddress oldPriIf, InetAddress primarySnmpIf) {
        /*
         * NOTE: If SNMP service was not previously supported on this node
         * then oldPriIf will be null. If this is the case
         * then no need to generate primarySnmpInterfaceChanged event,
         * the nodeGainedService event generated due to the addition of
         * SNMP is sufficient.
         */
        boolean reInit = true;
        if (oldPriIf == null && primarySnmpIf != null) {
            reInit = false;
            log().debug("generateSnmpDataCollectionEvents: Either SNMP support was recently enabled on this node, or node doesn't support ipAddrTable MIB.");
            m_eventList.add(createPrimarySnmpInterfaceChangedEvent(nodeEntry.getNodeId(), primarySnmpIf, null));
        } else {
            /*
             * A PrimarySnmpInterfaceChanged event is generated if the scan
             * found a different primary SNMP interface than what is stored
             * in the database.
             */
            if (primarySnmpIf != null && !oldPriIf.equals(primarySnmpIf)) {
                if (log().isDebugEnabled()) {
                    log().debug("generateSnmpDataCollectionEvents: primary SNMP interface has changed.  Was: " + str(oldPriIf) + " Is: " + str(primarySnmpIf));
                }
                m_eventList.add(createPrimarySnmpInterfaceChangedEvent(nodeEntry.getNodeId(), primarySnmpIf, oldPriIf));
                reInit = false;
            }
        }

        /*
         * An interface map is built by the SNMP poller when the primary
         * SNMP interface is initialized by the service monitor. This map
         * is used to associate each interface on the node with its
         * ifIndex and ifLabel for purposes of performing data collection
         * and storage. If an ifIndex has changed for one or more
         * interfaces or if a new interface was added to the node then
         * the primary SNMP interface must be reinitialized so that this
         * interface map can be rebuilt with the new information.
         */
        if (reInit && (m_ifIndexOnNodeChangedFlag || m_snmpIfTableChangedFlag)) {
            if (log().isDebugEnabled()) {
                log().debug("generateSnmpDataCollectionEvents: Generating reinitializeSnmpInterface event for interface " + str(primarySnmpIf));
            }
            m_eventList.add(createReinitializePrimarySnmpInterfaceEvent(nodeEntry.getNodeId(), primarySnmpIf));
        }
    }
    
    private static EventBuilder eventBuilder(String uei) {
        return new EventBuilder(uei, "OpenNMS.Capsd").setHost(Capsd.getLocalHostAddress());
    }
    
    private static EventBuilder nodeEventBuilder(String uei, long nodeId) {
        return eventBuilder(uei).setNodeid(nodeId);
    }

    private static EventBuilder interfaceEventBuilder(String uei, long nodeId, String ipAddr) {
        return eventBuilder(uei).setNodeid(nodeId).setInterface(addr(ipAddr));
    }
    private static EventBuilder serviceEventBuilder(String uei, long nodeId, String ipAddr, String svc) {
        return eventBuilder(uei).setNodeid(nodeId).setInterface(addr(ipAddr)).setService(svc);
    }

    /**
     * This method is responsible for generating a nodeLabelChanged event and
     * adding it to the event list.
     * 
     * @param updatedEntry
     *            Updated node entry object
     * @param originalEntry
     *            Original node entry object
     */
    private static Event createNodeLabelChangedEvent(DbNodeEntry updatedEntry, DbNodeEntry originalEntry) {
        if (log().isDebugEnabled()) {
            log().debug("createNodeLabelChangedEvent: nodeId: " + updatedEntry.getNodeId() + " oldLabel: '" + originalEntry.getLabel() + "' oldSource: '" + originalEntry.getLabelSource() + "' newLabel: '" + updatedEntry.getLabel() + "' newLabelSource: '" + updatedEntry.getLabelSource() + "'");
        }

        EventBuilder bldr = nodeEventBuilder(EventConstants.NODE_LABEL_CHANGED_EVENT_UEI, updatedEntry.getNodeId());

        if (originalEntry.getLabel() != null) {
            bldr.addParam(EventConstants.PARM_OLD_NODE_LABEL, originalEntry.getLabel());
            bldr.addParam(EventConstants.PARM_OLD_NODE_LABEL_SOURCE, originalEntry.getLabelSource());
        }

        if (updatedEntry.getLabel() != null) {
            bldr.addParam(EventConstants.PARM_NEW_NODE_LABEL, updatedEntry.getLabel());
            bldr.addParam(EventConstants.PARM_NEW_NODE_LABEL_SOURCE, updatedEntry.getLabelSource());
        }

        if (log().isDebugEnabled()) {
            log().debug("createNodeLabelChangedEvent: successfully created nodeLabelChanged event for nodeid: " + updatedEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a nodeInfoChanged event and
     * adding it to the event list.
     * 
     * @param updatedEntry
     *            Updated node entry object
     * @param originalEntry
     *            Original node entry object
     */
    private static Event createNodeInfoChangedEvent(DbNodeEntry updatedEntry, DbNodeEntry originalEntry) {
        if (log().isDebugEnabled()) {
            log().debug("createNodeInfoChangedEvent: nodeId: " + updatedEntry.getNodeId());
        }

        EventBuilder bldr = nodeEventBuilder(EventConstants.NODE_INFO_CHANGED_EVENT_UEI, updatedEntry.getNodeId());

        // SysOID
        if (updatedEntry.getSystemOID() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSOID, updatedEntry.getSystemOID());
        }

        // SysName
        if (updatedEntry.getSystemName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSNAME, updatedEntry.getSystemName());
        }

        // SysDescription
        if (updatedEntry.getSystemDescription() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSDESCRIPTION, updatedEntry.getSystemDescription());
        }

        // SysLocation
        if (updatedEntry.getSystemLocation() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSLOCATION, updatedEntry.getSystemLocation());
        }

        // SysContact
        if (updatedEntry.getSystemContact() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSCONTACT, updatedEntry.getSystemContact());
        }

        // NetBIOS name
        if (updatedEntry.getNetBIOSName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_NETBIOS_NAME, updatedEntry.getNetBIOSName());
        }

        // Domain name
        if (updatedEntry.getDomainName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_DOMAIN_NAME, updatedEntry.getDomainName());
        }

        // Operating System
        if (updatedEntry.getOS() != null) {
            bldr.addParam(EventConstants.PARM_NODE_OPERATING_SYSTEM, updatedEntry.getOS());
        }

        if (log().isDebugEnabled()) {
            log().debug("createNodeInfoChangedEvent: successfully created nodeInfoChanged event for nodeid: " + updatedEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a primarySnmpInterfaceChanged
     * event and adding it to the event list.
     * 
     * @param nodeId
     *            Nodeid of node being rescanned.
     * @param newPrimaryIf
     *            new primary SNMP interface address
     * @param oldPrimaryIf
     *            old primary SNMP interface address
     * @return 
     */
    private static Event createPrimarySnmpInterfaceChangedEvent(final int nodeId, final InetAddress newPrimaryIf, final InetAddress oldPrimaryIf) {
        String oldPrimaryAddr = null;
        if (oldPrimaryIf != null) {
            oldPrimaryAddr = str(oldPrimaryIf);
        }

        String newPrimaryAddr = null;
        if (newPrimaryIf != null) {
            newPrimaryAddr = str(newPrimaryIf);
        }

        if (log().isDebugEnabled()) {
            log().debug("createPrimarySnmpInterfaceChangedEvent: nodeId: " + nodeId + "oldPrimarySnmpIf: '" + oldPrimaryAddr + "' newPrimarySnmpIf: '" + newPrimaryAddr + "'");
        }

        final EventBuilder bldr = serviceEventBuilder(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI, nodeId, newPrimaryAddr, "SNMP");

        if (oldPrimaryAddr != null) {
            bldr.addParam(EventConstants.PARM_OLD_PRIMARY_SNMP_ADDRESS, oldPrimaryAddr);
        }

        if (newPrimaryAddr != null) {
            bldr.addParam(EventConstants.PARM_NEW_PRIMARY_SNMP_ADDRESS, newPrimaryAddr);
        }

        if (log().isDebugEnabled()) {
            log().debug("createPrimarySnmpInterfaceChangedEvent: successfully created primarySnmpInterfaceChanged event for nodeid: " + nodeId);
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a interfaceIndexChanged event
     * and adding it to the event list.
     * 
     * @param updatedEntry
     *            updated IP interface database entry
     * @param originalEntry
     *            original IP interface database entry
     * @return 
     */
    private static Event createInterfaceIndexChangedEvent(final DbIpInterfaceEntry updatedEntry, final DbIpInterfaceEntry originalEntry) {
        if (log().isDebugEnabled()) {
            log().debug("createInterfaceIndexChangedEvent: nodeId: " + updatedEntry.getNodeId() + " oldIfIndex: " + originalEntry.getIfIndex() + " newIfIndex: " + updatedEntry.getIfIndex());
        }

        EventBuilder bldr = interfaceEventBuilder(EventConstants.INTERFACE_INDEX_CHANGED_EVENT_UEI, updatedEntry.getNodeId(), str(updatedEntry.getIfAddress()));

        bldr.addParam(EventConstants.PARM_OLD_IFINDEX, originalEntry.getIfIndex());
        bldr.addParam(EventConstants.PARM_NEW_IFINDEX, updatedEntry.getIfIndex());

        if (log().isDebugEnabled()) {
            log().debug("createInterfaceIndexChangedEvent: successfully created interfaceIndexChanged event for nodeid: " + updatedEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating an ipHostNameChanged event and
     * adding it to the event list.
     * 
     * @param updatedEntry
     *            updated IP interface database entry
     * @param originalEntry
     *            original IP interface database entry
     * @return 
     */
    private static Event createIpHostNameChangedEvent(final DbIpInterfaceEntry updatedEntry, final DbIpInterfaceEntry originalEntry) {
        if (log().isDebugEnabled()) {
            log().debug("createIpHostNameChangedEvent: nodeId: " + updatedEntry.getNodeId() + " oldHostName: " + originalEntry.getHostname() + " newHostName: " + updatedEntry.getHostname());
        }

        EventBuilder bldr = interfaceEventBuilder(EventConstants.INTERFACE_IP_HOSTNAME_CHANGED_EVENT_UEI, updatedEntry.getNodeId(), str(updatedEntry.getIfAddress()));
        
        // Add old IP Hostname
        if (originalEntry.getHostname() != null) {
            bldr.addParam(EventConstants.PARM_OLD_IP_HOSTNAME, originalEntry.getHostname());
        }

        // Add new IP Hostname
        if (updatedEntry.getHostname() != null) {
            bldr.addParam(EventConstants.PARM_IP_HOSTNAME, updatedEntry.getHostname());
        }

        if (log().isDebugEnabled()) {
            log().debug("createIpHostNameChangedEvent: successfully created ipHostNameChanged event for nodeid: " + updatedEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a interfaceReparented event and
     * adding it to the event list.
     * 
     * @param newNode
     *            Entry of node under which the interface was added.
     * @param oldNodeId
     *            Node identifier of node from which the interface was removed.
     * @param reparentedIf
     *            Reparented interface
     * @return 
     */
    private static Event createInterfaceReparentedEvent(final DbNodeEntry newNode, final int oldNodeId, final InetAddress reparentedIf) {
        final String reparentedAddress = str(reparentedIf);
		if (log().isDebugEnabled()) {
            log().debug("createInterfaceReparentedEvent: ifAddr: " + reparentedAddress + " oldNodeId: " + oldNodeId + " newNodeId: " + newNode.getNodeId());
        }

        
        EventBuilder bldr = interfaceEventBuilder(EventConstants.INTERFACE_REPARENTED_EVENT_UEI, newNode.getNodeId(), reparentedAddress);

        // Add old node id
        bldr.addParam(EventConstants.PARM_OLD_NODEID, oldNodeId);

        // Add new node id
        bldr.addParam(EventConstants.PARM_NEW_NODEID, newNode.getNodeId());

        // Add ip host name
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, reparentedIf.getHostName());

        // Add node label and node label source
        if (newNode.getLabel() != null) {
            bldr.addParam(EventConstants.PARM_NODE_LABEL, newNode.getLabel());
            bldr.addParam(EventConstants.PARM_NODE_LABEL_SOURCE, newNode.getLabelSource());
        }

        // Add nodeSysName
        if (newNode.getSystemName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSNAME, newNode.getSystemName());
        }

        // Add nodeSysDescription
        if (newNode.getSystemDescription() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSDESCRIPTION, newNode.getSystemDescription());
        }

        if (log().isDebugEnabled()) {
            log().debug("createInterfaceReparentedEvent: successfully created interfaceReparented event for nodeid/interface: " + newNode.getNodeId() + "/" + reparentedAddress);
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a duplicateNodeDeleted event
     * and adding it to the event list.
     * 
     * @param deletedNode
     *            Entry of duplciate node which was deleted.
     * @return 
     */
    private static Event createDuplicateNodeDeletedEvent(DbNodeEntry deletedNode) {
        if (log().isDebugEnabled()) {
            log().debug("createDuplicateNodeDeletedEvent: delete nodeid: " + deletedNode.getNodeId());
        }

        EventBuilder bldr = nodeEventBuilder(EventConstants.DUP_NODE_DELETED_EVENT_UEI, deletedNode.getNodeId());

        if (log().isDebugEnabled()) {
            log().debug("createDuplicateNodeDeletedEvent: successfully created duplicateNodeDeleted event for nodeid: " + deletedNode.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a nodeGainedInterface event and
     * adding it to the event list.
     * 
     * @param ifEntry
     *            Entry of new interface.
     * @return 
     */
    private static Event createNodeGainedInterfaceEvent(DbIpInterfaceEntry ifEntry) {
        final String ifAddress = str(ifEntry.getIfAddress());
		if (log().isDebugEnabled()) {
            log().debug("createNodeGainedInterfaceEvent: nodeId: " + ifEntry.getNodeId() + " interface: " + ifAddress);
        }

        EventBuilder bldr = interfaceEventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, ifEntry.getNodeId(), ifAddress);

        // Add ip host name
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ifEntry.getHostname() == null ? "" : ifEntry.getHostname());

        // Add discovery method
        bldr.addParam(EventConstants.PARM_METHOD, "icmp");

        if (log().isDebugEnabled()) {
            log().debug("createNodeGainedInterfaceEvent: successfully created nodeGainedInterface event for nodeid: " + ifEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

	/**
     * This method is responsible for generating a duplicateIpAddress event and
     * adding it to the event list.
     * 
     * @param ifEntry
     *            Entry of new interface.
	 * @return 
     */
    private static Event createDuplicateIpAddressEvent(final DbIpInterfaceEntry ifEntry) {
        final String ifAddress = str(ifEntry.getIfAddress());

        if (log().isDebugEnabled()) {
            log().debug("createDuplicateIpAddressEvent: nodeId: " + ifEntry.getNodeId() + " interface: " + ifAddress);
        }

        EventBuilder bldr = interfaceEventBuilder(EventConstants.DUPLICATE_IPINTERFACE_EVENT_UEI, ifEntry.getNodeId(), ifAddress);

        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ifEntry.getHostname() == null ? "" : ifEntry.getHostname());

        // Add discovery method
        bldr.addParam(EventConstants.PARM_METHOD, "icmp");

        if (log().isDebugEnabled()) {
            log().debug("createDuplicateIpAddressEvent: successfully created duplicateIpAddress event for nodeid: " + ifEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a nodeGainedService event and
     * adding it to the event list.
     * 
     * @param nodeEntry
     *            Entry of node which has gained a service
     * @param ifEntry
     *            Entry of interface which has gained a service
     * @param svcName
     *            Service name
     * @return 
     */
    private static Event createNodeGainedServiceEvent(final DbNodeEntry nodeEntry, final DbIpInterfaceEntry ifEntry, final String svcName) {
        final String ifAddress = str(ifEntry.getIfAddress());

        if (log().isDebugEnabled()) {
            log().debug("createNodeGainedServiceEvent: nodeId: " + ifEntry.getNodeId() + " interface: " + ifAddress + " service: " + svcName);
        }

        EventBuilder bldr = serviceEventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, ifEntry.getNodeId(), ifAddress, svcName);

        // Add ip host name
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ifEntry.getHostname() == null ? "" : ifEntry.getHostname());

        // Add nodeSysName
        if (nodeEntry.getSystemName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSNAME, nodeEntry.getSystemName());
        }

        // Add nodeSysDescription
        if (nodeEntry.getSystemDescription() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSDESCRIPTION, nodeEntry.getSystemDescription());
        }

        if (log().isDebugEnabled()) {
            log().debug("createNodeGainedServiceEvent: successfully created nodeGainedService event for nodeid: " + ifEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a suspendPollingService event
     * and adding it to the event list.
     * 
     * @param nodeEntry
     *            Entry of node for which a service is to be polled
     * @param ifEntry
     *            Entry of interface which a service is to be polled
     * @param svcName
     *            Service name
     * @return 
     */
    private static Event createSuspendPollingServiceEvent(final DbNodeEntry nodeEntry, final DbIpInterfaceEntry ifEntry, final String svcName) {
        final String ifAddress = str(ifEntry.getIfAddress());

        final EventBuilder bldr = serviceEventBuilder(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI, ifEntry.getNodeId(), ifAddress, svcName);

        // Add ip host name
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ifEntry.getHostname() == null ? "" : ifEntry.getHostname());
        
        // Add nodeSysName
        if (nodeEntry.getSystemName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSNAME, nodeEntry.getSystemName());
        }

        // Add nodeSysDescription
        if (nodeEntry.getSystemDescription() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSDESCRIPTION, nodeEntry.getSystemDescription());
        }

        if (log().isDebugEnabled()) {
            log().debug("suspendPollingServiceEvent: Created suspendPollingService event for nodeid: " + ifEntry.getNodeId() + " interface: " + ifAddress + " service: " + svcName);
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a resumePollingService event
     * and adding it to the event list.
     * 
     * @param nodeEntry
     *            Entry of node for which a service is to be polled
     * @param ifEntry
     *            Entry of interface which a service is to be polled
     * @param svcName
     *            Service name
     * @return 
     */
    private static Event createResumePollingServiceEvent(final DbNodeEntry nodeEntry, final DbIpInterfaceEntry ifEntry, final String svcName) {
        final String ifAddress = str(ifEntry.getIfAddress());
        
        final EventBuilder bldr = serviceEventBuilder(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI, ifEntry.getNodeId(), ifAddress, svcName);

        // Add ip host name
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ifEntry.getHostname() == null ? "" : ifEntry.getHostname());

        // Add nodeSysName
        if (nodeEntry.getSystemName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSNAME, nodeEntry.getSystemName());
        }

        // Add nodeSysDescription
        if (nodeEntry.getSystemDescription() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSDESCRIPTION, nodeEntry.getSystemDescription());
        }

        if (log().isDebugEnabled()) {
            log().debug("resumePollingServiceEvent: Created resumePollingService event for nodeid: " + ifEntry.getNodeId() + " interface: " + ifAddress + " service: " + svcName);
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a snmpConflictsWithDb event and
     * adding it to the event list.
     *
     * @param nodeEntry Entry of node for which a conflict exits
     * @return 
     */
    private static Event createSnmpConflictsWithDbEvent(DbNodeEntry nodeEntry) {
        
        EventBuilder bldr = nodeEventBuilder(EventConstants.SNMP_CONFLICTS_WITH_DB_EVENT_UEI, nodeEntry.getNodeId());

        // Add node label
        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeEntry.getLabel() == null ? "" : nodeEntry.getLabel());

        // Add nodeSysName
        if (nodeEntry.getSystemName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSNAME, nodeEntry.getSystemName());
        }

        // Add nodeSysDescription
        if (nodeEntry.getSystemDescription() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSDESCRIPTION, nodeEntry.getSystemDescription());
        }

        if (log().isDebugEnabled()) {
            log().debug("snmpConflictsWithDbEvent: Created snmpConflictsWithDbEvent for nodeid: " + nodeEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a rescanCompleted event and
     * adding it to the event list.
     *
     * @param nodeEntry Entry of node which was rescanned
     * @return 
     */
    private static Event createRescanCompletedEvent(DbNodeEntry nodeEntry) {
        
        EventBuilder bldr = nodeEventBuilder(EventConstants.RESCAN_COMPLETED_EVENT_UEI, nodeEntry.getNodeId());

        // Add node label
        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeEntry.getLabel() == null ? "" : nodeEntry.getLabel());

        if (log().isDebugEnabled()) {
            log().debug("rescanCompletedEvent: Created rescanCompletedEvent for nodeid: " + nodeEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a interfaceSupportsSNMPEvent
     * event and adding it to the event list.
     * 
     * @param ifEntry
     *            Entry of interface which has gained a service
     * @return 
     */
    private static Event createInterfaceSupportsSNMPEvent(final DbIpInterfaceEntry ifEntry) {
        final String ifAddress = str(ifEntry.getIfAddress());

        if (log().isDebugEnabled()) {
            log().debug("createInterfaceSupportsSNMPEvent: nodeId: " + ifEntry.getNodeId() + " interface: " + ifAddress);
        }
        
        final EventBuilder bldr = interfaceEventBuilder(EventConstants.INTERFACE_SUPPORTS_SNMP_EVENT_UEI, ifEntry.getNodeId(), ifAddress);

        if (log().isDebugEnabled()) {
            log().debug("interfaceSupportsSNMPEvent: successfully created interfaceSupportsSNMPEvent event for nodeid: " + ifEntry.getNodeId());
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a
     * reinitializePrimarySnmpInterface event and adding it to the event list.
     * 
     * @param nodeId
     *            Nodeid of node being rescanned.
     * @param primarySnmpIf
     *            Primary SNMP interface address.
     * @return 
     */
    private static Event createReinitializePrimarySnmpInterfaceEvent(final int nodeId, final InetAddress primarySnmpIf) {
        final String primaryAddress = str(primarySnmpIf);
        
		if (log().isDebugEnabled()) {
            log().debug("reinitializePrimarySnmpInterface: nodeId: " + nodeId + " interface: " + primaryAddress);
        }

        EventBuilder bldr = interfaceEventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, nodeId, primaryAddress);

        if (log().isDebugEnabled()) {
            log().debug("createReinitializePrimarySnmpInterfaceEvent: successfully created reinitializePrimarySnmpInterface event for interface: " + primaryAddress);
        }

        // Add event to the list of events to be sent out.
        return bldr.getEvent();
    }
    
    
    /**
     * Responsible for setting the Set used to track rescans that
     * are already enqueued for processing.  Should be called once by Capsd
     * at startup.
     *
     * @param queuedRescanTracker
     *          The synchronized Set to use
     */
    public static synchronized void setQueuedRescansTracker(Set<Integer> queuedRescanTracker) {
        m_queuedRescanTracker = Collections.synchronizedSet(queuedRescanTracker);
    }
    
    /**
     * Is a rescan already enqueued for a given node ID?
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @return a boolean.
     */
    public static boolean isRescanQueuedForNode(Integer nodeId) {
        synchronized(m_queuedRescanTracker) {
            return (m_queuedRescanTracker.contains(nodeId));
        }
    }

} // end class
