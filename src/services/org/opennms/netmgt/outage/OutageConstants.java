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
// 2003 Nov 11: Merged changes from Rackspace project
// 2003 Jan 08: Changed "= null" to "is null" to work with Posgres 7.2
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
// Tab Size = 8
//

package org.opennms.netmgt.outage;

/**
 * This class is a repository for constant, static information concerning the
 * Outage Manager.
 * 
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 */
public final class OutageConstants {
    /**
     * The SQL statement necessary to read service id and service name into map.
     */
    public final static String SQL_DB_SVC_TABLE_READ = "SELECT serviceID, serviceName FROM service";

    /**
     * The SQL statement that is used to close open outages for services that
     * are currently unmanaged
     */
    public static final String DB_CLOSE_OUTAGES_FOR_UNMANAGED_SERVICES = "UPDATE outages set ifregainedservice = ? where outageid in (select outages.outageid from outages, ifservices where ((outages.nodeid = ifservices.nodeid) AND (outages.ipaddr = ifservices.ipaddr) AND (outages.serviceid = ifservices.serviceid) AND ((ifservices.status = 'F') OR (ifservices.status = 'U')) AND (outages.ifregainedservice IS NULL)))";

    /**
     * The sql statement that is used to close open outages for interfaces that
     * are currently unmanaged
     */
    public static final String DB_CLOSE_OUTAGES_FOR_UNMANAGED_INTERFACES = "UPDATE outages set ifregainedservice = ? where outageid in (select outages.outageid from outages, ipinterface where ((outages.nodeid = ipinterface.nodeid) AND (outages.ipaddr = ipinterface.ipaddr) AND ((ipinterface.ismanaged = 'F') OR (ipinterface.ismanaged = 'U')) AND (outages.ifregainedservice IS NULL)))";

    /**
     * The sql statement that is used to get serviceID for a service name
     */
    public static final String DB_GET_SVC_ID = "SELECT serviceid from service where (servicename= ?)";

    /**
     * The sql statement that is used to see if there is an 'open' record for a
     * nodeid/ip/service
     */
    public static final String DB_COUNT_OPEN_OUTAGES_FOR_SVC = "SELECT count(*) from outages where (nodeid = ? AND ipAddr = ? AND serviceID = ? AND (ifRegainedService IS NULL))";

    /**
     * The sql statement that is used to see if there is an 'open' record for a
     * nodeid/ip
     */
    public static final String DB_COUNT_OPEN_OUTAGES_FOR_INTERFACE = "SELECT count(*) from outages where (nodeid = ? AND ipAddr = ? AND (ifRegainedService IS NULL))";

    /**
     * The sql statement that is used to see if there is an 'open' record for a
     * nodeid
     */
    public static final String DB_COUNT_OPEN_OUTAGES_FOR_NODE = "SELECT count(*) from outages where (nodeid = ? AND (ifRegainedService IS NULL))";

    /**
     * The sql statement that is used to insert an new outage entry
     */
    public static final String DB_INS_NEW_OUTAGE = "INSERT INTO outages (outageID, svcLostEventID, nodeID, ipAddr, serviceID, ifLostService) VALUES (?, ?, ?, ?, ?, ?)";

    /**
     * The sql statement that is used to update the 'regained time' in an open
     * entry
     */
    public static final String DB_UPDATE_OUTAGE_FOR_SERVICE = "UPDATE outages set svcRegainedEventID=?, ifRegainedService=? where (nodeid = ? AND ipAddr = ? AND serviceID = ? and (ifRegainedService IS NULL))";

    /**
     * The sql statement used to get all SNMP/SNMPv2 entries for a node from the
     * ifServices that are currently active
     */
    public static final String DB_GET_SNMP_SERVICE = "SELECT ipaddr, serviceid, status FROM ifservices WHERE nodeid = ? AND (serviceID = ? OR serviceID = ?) AND (status ='A')";

    /**
     * The sql statement used to flag an entry from the ifServices table as
     * deleted based on a node and the service being SNMP or SNMPv2
     */
    public static final String DB_DELETE_SNMP_SERVICE = "UPDATE ifservices SET status = 'D' WHERE nodeid = ? AND (serviceID = ? OR serviceID = ?)";

    /**
     * The sql statement used to clear snmp related data for a nodeid
     * 
     * Note: this statement is got depreciated after OpenNMS 1.1.3
     */
    public static final String DB_CLEAR_NODE_SNMP_INFO = "UPDATE node set nodesysoid=null, nodesysname=null, nodesysdescription=null, nodesyslocation=null, nodesyscontact=null WHERE nodeid = ?";

    /**
     * The sql statement used to clear snmp related data for a nodeid
     */
    public static final String DB_CLEAR_INTERFACE_SNMP_INFO = "UPDATE ipinterface set ifIndex=null, ipstatus=null, issnmpprimary='N' WHERE nodeid = ?";

    /**
     * The sql statement used to delete all entries from the snmpInterface table
     * for a node
     */
    public static final String DB_DELETE_SNMP_INTERFACE = "DELETE FROM snmpinterface WHERE nodeid = ?";

    /**
     * The sql statement used to determine if there are any remaining entries in
     * the 'ifservices' table for a specific nodeID/ipaddr pair following the
     * deletion of a service entry.
     */
    public static final String DB_GET_SERVICE_COUNT = "SELECT COUNT(*) FROM ifservices WHERE nodeid = ? AND ipaddr = ? AND status = 'A'";

    /**
     * The sql statement used to flag an entry from the ipInterface table as
     * deleted based on a node/interface pair
     */
    public static final String DB_DELETE_INTERFACE = "UPDATE ipinterface SET isManaged = 'D' WHERE nodeid = ? AND ipAddr = ?";

    /**
     * The sql statement used to flag all interface entries from the
     * 'ipInterface' table as deleted for a specific node (called when there are
     * only unmanaged interfaces left on the node)
     */
    public static final String DB_DELETE_ALL_INTERFACES = "UPDATE ipinterface SET isManaged = 'D' WHERE nodeid = ?";

    /**
     * The sql statement used to flag an entry from the node table as deleted
     * based on a node identifier
     */
    public static final String DB_DELETE_NODE = "UPDATE node SET nodeType = 'D' WHERE nodeid = ?";

    /**
     * The sql statement used to reparent outage table entries...used when a
     * 'interfaceReparented' event is received.
     */
    public static final String DB_REPARENT_OUTAGES = "UPDATE outages SET nodeid=? WHERE nodeid=? AND ipaddr=?";

    /**
     * The sql statement used to get all active service entries for a nodeid/ip
     */
    public static final String DB_GET_ACTIVE_SERVICES_FOR_INTERFACE = "SELECT serviceid FROM ifservices WHERE nodeid=? AND ipaddr=? AND status='A'";

    /**
     * The sql statement used to get all active service entries for a nodeid
     */
    public static final String DB_GET_ACTIVE_SERVICES_FOR_NODE = "SELECT ipAddr, serviceid FROM ifservices WHERE nodeid=? AND status='A'";

    /**
     * The sql statement used to close all open outages for a nodeid/ip
     */
    public static final String DB_UPDATE_OUTAGES_FOR_INTERFACE = "UPDATE outages set svcRegainedEventID=?, ifRegainedService=? where (nodeid = ? AND ipAddr = ? and (ifRegainedService IS NULL))";

    /**
     * The sql statement used to close all open outages for a nodeid
     */
    public static final String DB_UPDATE_OUTAGES_FOR_NODE = "UPDATE outages set svcRegainedEventID=?, ifRegainedService=? where (nodeid = ? and (ifRegainedService IS NULL))";

    /**
     * The sql statement used to record an event cache hit
     */
    public static final String DB_INS_CACHE_HIT = "INSERT INTO outages (outageID, svcLostEventID, nodeID, ipAddr, serviceID, ifLostService, svcRegainedEventID, ifRegainedService) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * The sql statement used to count if there are other interfaces exist on
     * the node except the interface to delete.
     */
    public static final String DB_COUNT_REMAIN_INTERFACES_ON_NODE = "SELECT count(*) FROM ipinterface " + "WHERE nodeid = ? AND ipAddr != ? " + "AND isManaged != 'D'";

    /**
     * The sql statement used to count if there are other services exist on the
     * interface except the service to delete.
     */
    public static final String DB_COUNT_REMAIN_SERVICES_ON_INTERFACE = "SELECT count(*) FROM ifservices " + "WHERE nodeid = ? AND ipAddr = ? " + "AND serviceID != ? AND status != 'D'";

    /**
     * The sql statement used to delete a service from the ifServices table
     * based on a node/interface/service tuple
     */
    public static final String DB_DELETE_SERVICE = "DELETE FROM ifservices WHERE nodeid = ? AND ipAddr = ? AND serviceID = ?";

    /**
     * The sql statement used to delete usersNotified records from the database
     * based on a node/interface/service tuple
     */
    public static final String DB_DELETE_USERSNOTIFIED_ON_SERVICE = "DELETE FROM usersNotified WHERE notifyID " + "IN (SELECT notifyID FROM notifications " + "    WHERE nodeid = ? AND interfaceid = ? " + "    AND serviceID = ?)";

    /**
     * The sql statement used to delete notifications from the database based on
     * a node/interface/service tuple
     */
    public static final String DB_DELETE_NOTIFICATIONS_ON_SERVICE = "DELETE FROM notifications " + "WHERE nodeid = ? AND interfaceid = ? AND serviceID = ?";

    /**
     * The sql statement used to delete all outages from the database based on a
     * node/interface/service tuple
     */
    public static final String DB_DELETE_OUTAGES_ON_SERVICE = "DELETE FROM outages " + "WHERE nodeid = ? AND ipAddr = ? AND serviceID = ?";

    /**
     * The sql statement used to delete all events from the database based on a
     * node/interface/service tuple
     */
    public static final String DB_DELETE_EVENTS_ON_SERVICE = "DELETE FROM events " + "WHERE nodeid = ? AND ipAddr = ? AND serviceID = ?";

    /**
     * The sql statement used to query if an interface is the SNMP primary
     * interface for a nodeid
     */
    public static final String DB_QUERY_PRIMARY_INTERFACE = "SELECT isSnmpPrimary FROM ipinterface " + "WHERE nodeid = ? AND ipAddr = ?";

    /**
     * The sql statement used to retrieve the nodeLabel for a nodeid
     */
    public static final String DB_GET_NODE_LABEL = "SELECT nodelabel FROM NODE WHERE nodeid = ?";
}
