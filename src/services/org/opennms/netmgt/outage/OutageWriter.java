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
// 2003 Nov 10: Removed event cache calls - too many issues - set outage writer threads to 1
// 2003 Jan 31: Cleaned up some unused imports. 
// 2003 Jan 08: Changed SQL "= null" to "is null" to work with Postgres 7.2
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * When a 'nodeLostService' is received, it is made sure that there is no 'open'
 * outage record in the 'outages' table for this nodeid/ipaddr/serviceid - i.e
 * that there is not already a record for this n/i/s where the 'lostService'
 * time is known and the 'regainedService' time is NULL - if there is, the
 * current 'lostService' event is ignored else a new outage is created.
 * 
 * The 'interfaceDown' is similar to the 'nodeLostService' except that it acts
 * relevant to a nodeid/ipaddr combination and a 'nodeDown' acts on a nodeid.
 * 
 * When a 'nodeRegainedService' is received and there is an 'open' outage for
 * the nodeid/ipaddr/serviceid, the outage is cleared. If not, the event is
 * placed in the event cache in case a race condition has occurred that puts the
 * "up" event in before the "down" event. (currently inactive).
 * 
 * The 'interfaceUp' is similar to the 'nodeRegainedService' except that it acts
 * relevant to a nodeid/ipaddr combination and a 'nodeUp' acts on a nodeid.
 * 
 * When a 'deleteService' is received, the appropriate entry is marked for
 * deletion is the 'ifservices' table - if this entry is the only entry for a
 * node/ip combination, the corresponding entry in the 'ipinterface' table is
 * marked for deletion and this is then cascaded to the node table All deletions
 * are followed by an appropriate event(serviceDeleted or interfaceDeleted or..)
 * being generated and sent to eventd.
 * 
 * When an 'interfaceReparented' event is received, 'outages' table entries
 * associated with the old nodeid/interface pairing are changed so that those
 * outage entries will be associated with the new nodeid/interface pairing.
 * 
 * The nodeLostService, interfaceDown, nodeDown, nodeUp, interfaceUp,
 * nodeRegainedService, deleteService events update the svcLostEventID and the
 * svcRegainedEventID fields as approppriate. The interfaceReparented event has
 * no impact on these eventid reference fields.
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public final class OutageWriter implements Runnable {
    private static final String SNMP_SVC = "SNMP";

    private static final String SNMPV2_SVC = "SNMPv2";

    /**
     * The event from which data is to be read.
     */
    private Event m_event;

    // Control whether or not an event is generated following
    // database modifications to notify other OpenNMS processes
    private boolean m_generateNodeDeletedEvent;

    private OutageManager m_outageMgr;

    /**
     * A class to hold SNMP/SNMPv2 entries for a node from the ifservices table.
     * A list of this class is maintained on SNMP delete so as to be able to
     * generate a series of serviceDeleted for all entries marked as 'D'
     */
    private static class IfSvcSnmpEntry {
        private long m_nodeID;

        private String m_ipAddr;

        private String m_svcName;

        IfSvcSnmpEntry(long nodeid, String ip, String sname) {
            m_nodeID = nodeid;
            m_ipAddr = ip;
            m_svcName = sname;
        }

        long getNodeID() {
            return m_nodeID;
        }

        String getIP() {
            return m_ipAddr;
        }

        String getService() {
            return m_svcName;
        }
    }

    /**
     * Convert event time into timestamp
     */
    private java.sql.Timestamp convertEventTimeIntoTimestamp(String eventTime) {
        java.sql.Timestamp timestamp = null;
        try {
            java.util.Date date = EventConstants.parseToDate(eventTime);
            timestamp = new java.sql.Timestamp(date.getTime());
        } catch (ParseException e) {
            ThreadCategory.getInstance(OutageWriter.class).warn("Failed to convert event time " + eventTime + " to timestamp.", e);

            timestamp = new java.sql.Timestamp((new java.util.Date()).getTime());
        }
        return timestamp;
    }

    /**
     * <P>
     * This method is used to convert the service name into a service id. It
     * first looks up the information from a service map in OutagesManager and
     * if no match is found, by performing a lookup in the database. If the
     * conversion is successful then the corresponding integer identifier will
     * be returned to the caller.
     * </P>
     * 
     * @param name
     *            The name of the service
     * 
     * @return The integer identifier for the service name.
     * 
     * @throws java.sql.SQLException
     *             if there is an error accessing the stored data, the SQL text
     *             is malformed, or the result cannot be obtained.
     * 
     * @see org.opennms.netmgt.outage.OutageConstants#DB_GET_SVC_ID
     *      DB_GET_SVC_ID
     */
    private long getServiceID(String name) throws SQLException {
        //
        // Check the name to make sure that it is not null
        //
        if (name == null)
            throw new NullPointerException("The service name was null");

        // ask OutageManager
        //
        long id = m_outageMgr.getServiceID(name);
        if (id != -1)
            return id;

        //
        // talk to the database and get the identifer
        //
        Connection dbConn = null;
        try {
            dbConn = getConnection();

            // SQL statement to get service id for a servicename from the
            // service table
            PreparedStatement serviceStmt = dbConn.prepareStatement(OutageConstants.DB_GET_SVC_ID);

            serviceStmt.setString(1, name);
            ResultSet rset = serviceStmt.executeQuery();
            if (rset.next()) {
                id = rset.getLong(1);
            }

            // close result set
            rset.close();

            // close statement
            if (serviceStmt != null)
                serviceStmt.close();
        } finally {
            try {
                if (dbConn != null)
                    dbConn.close();
            } catch (SQLException e) {
                ThreadCategory.getInstance(getClass()).warn("Exception closing JDBC connection", e);
            }
        }

        // Record the new find
        //
        if (id != -1)
            m_outageMgr.addServiceMapping(name, id);

        //
        // return the id to the caller
        //
        return id;
    }

    /**
     * Handles node lost service events. Record the 'nodeLostService' event in
     * the outages table - create a new outage entry if the service is not
     * already down.
     */
    private void handleNodeLostService(long eventID, String eventTime, BasicService svc) {
        Category log = ThreadCategory.getInstance(OutageWriter.class);

        if (eventID == -1 || !svc.isValid()) {
            log.warn(EventConstants.NODE_LOST_SERVICE_EVENT_UEI + " ignored - info incomplete - eventid/nodeid/ip/svc: " + eventID + "/" + svc);
            return;
        }

        // check that there is no 'open' entry already
        Connection dbConn = null;

        try {
            dbConn = getConnection();
            // check that there is no 'open' entry already
            if (svc.openOutageExists(dbConn)) {
                log.warn("\'" + EventConstants.NODE_LOST_SERVICE_EVENT_UEI + "\' for " + svc + " ignored - table already  has an open record ");
            } else {
                // Set the database commit mode
                dbConn.setAutoCommit(false);

                PreparedStatement newOutageWriter = null;
                if (log.isDebugEnabled())
                    log.debug("handleNodeLostService: creating new outage entry...");
                newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);
                newOutageWriter.setLong(1, getNextOutageId(dbConn));
                newOutageWriter.setLong(2, eventID);
                newOutageWriter.setLong(3, svc.getNodeId());
                newOutageWriter.setString(4, svc.getIpAddr());
                newOutageWriter.setLong(5, svc.getServiceId());
                newOutageWriter.setTimestamp(6, convertEventTimeIntoTimestamp(eventTime));


                // execute
                newOutageWriter.executeUpdate();

                // close statement
                newOutageWriter.close();

                // commit work
                DbUtil.commit(log, dbConn, "nodeLostService : " + svc + " recorded in DB", "nodeLostService could not be recorded  for nodeid/ipAddr/service: " + svc);

            }
        } catch (SQLException sqle) {
            DbUtil.rollback(log, dbConn, "SQL exception while handling \'nodeLostService\'", sqle);
        } finally {
            DbUtil.close(log, dbConn);
        }

    }

    private long getNextOutageId(Connection dbConn) throws SQLException {
        long outageID = -1;

        PreparedStatement getNextOutageIdStmt = dbConn.prepareStatement(m_outageMgr.getGetNextOutageID());

        // Execute the statement to get the next outage id from the
        // sequence
        //
        ResultSet seqRS = getNextOutageIdStmt.executeQuery();
        if (seqRS.next()) {
            outageID = seqRS.getLong(1);
        }
        seqRS.close();
        return outageID;
    }

    /**
     * Handles interface down events. Record the 'interfaceDown' event in the
     * outages table - create a new outage entry for each active service of the
     * nodeid/ip if service not already down.
     */
    private void handleInterfaceDown(long eventID, String eventTime, BasicInterface iface) {
        Category log = ThreadCategory.getInstance(OutageWriter.class);
        
        if (eventID == -1 || !iface.isValid()) {
            log.warn(EventConstants.INTERFACE_DOWN_EVENT_UEI + " ignored - info incomplete - eventid/nodeid/ip: " + eventID + "/" + iface);
            return;
        }

        Connection dbConn = null;
        try {
            dbConn = getConnection();

            // Set the database commit mode
            dbConn.setAutoCommit(false);

            // Prepare SQL statement used to get active services for the
            // nodeid/ip
            PreparedStatement activeSvcsStmt = dbConn.prepareStatement(OutageConstants.DB_GET_ACTIVE_SERVICES_FOR_INTERFACE);

            // Prepare SQL statement used to see if there is already an
            // 'open' record for the node/ip/svc combination
            PreparedStatement openStmt = dbConn.prepareStatement(OutageConstants.DB_OPEN_RECORD);
            // Prepare statement to insert a new outage table entry
            PreparedStatement newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);

            // Prepare statement to insert a new outage table entry
            newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);

            if (log.isDebugEnabled())
                log.debug("handleInterfaceDown: creating new outage entries...");

            activeSvcsStmt.setLong(1, iface.getNodeId());
            activeSvcsStmt.setString(2, iface.getIpAddr());
            ResultSet activeSvcsRS = activeSvcsStmt.executeQuery();
            while (activeSvcsRS.next()) {
                BasicService svc = new BasicService(iface, activeSvcsRS.getLong(1));
                if (svc.openOutageExists(dbConn)) {
                    if (log.isDebugEnabled())
                        log.debug("handleInterfaceDown: " + svc + " already down");
                } else {
                    newOutageWriter.setLong(1, getNextOutageId(dbConn));
                    newOutageWriter.setLong(2, eventID);
                    newOutageWriter.setLong(3, svc.getNodeId());
                    newOutageWriter.setString(4, svc.getIpAddr());
                    newOutageWriter.setLong(5, svc.getServiceId());
                    newOutageWriter.setTimestamp(6, convertEventTimeIntoTimestamp(eventTime));

                    // execute update
                    newOutageWriter.executeUpdate();

                    if (log.isDebugEnabled())
                        log.debug("handleInterfaceDown: Recording new outage for " + svc);
                }
            }
            // close result set
            activeSvcsRS.close();

            // commit work
            DbUtil.commit(log, dbConn, "Outage recorded for all active services for " + iface, "interfaceDown could not be recorded  for nodeid/ipAddr: " + iface);

            // close statements
            activeSvcsStmt.close();
            openStmt.close();
            newOutageWriter.close();
        } catch (SQLException sqle) {
            DbUtil.rollback(log, dbConn, "SQL exception while handling \'interfaceDown\'", sqle);
        } finally {
            DbUtil.close(log, dbConn);
        }
    }

    /**
     * Handles node down events. Record the 'nodeDown' event in the outages
     * table - create a new outage entry for each active service of the nodeid
     * if service is not already down.
     * @param node TODO
     */
    private void handleNodeDown(long eventID, String eventTime, BasicNode node) {
        Category log = ThreadCategory.getInstance(OutageWriter.class);
        
        if (eventID == -1 || !node.isValid()) {
            log.warn(EventConstants.NODE_DOWN_EVENT_UEI + " ignored - info incomplete - eventid/nodeid: " + eventID + "/" + node);
            return;
        }

        Connection dbConn = null;
        try {
            dbConn = getConnection();

            // Set the database commit mode
            dbConn.setAutoCommit(false);

            // Prepare SQL statement used to get active services for the nodeid
            PreparedStatement activeSvcsStmt = dbConn.prepareStatement(OutageConstants.DB_GET_ACTIVE_SERVICES_FOR_NODE);

            // Prepare SQL statement used to see if there is already an
            // 'open' record for the node/ip/svc combination
            PreparedStatement openStmt = dbConn.prepareStatement(OutageConstants.DB_OPEN_RECORD);
            // Prepare statement to insert a new outage table entry
            PreparedStatement newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);

            // Prepare statement to insert a new outage table entry
            newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);

            if (log.isDebugEnabled())
                log.debug("handleNodeDown: creating new outage entries...");

            // Get all active services for the nodeid
            activeSvcsStmt.setLong(1, node.getNodeId());
            ResultSet activeSvcsRS = activeSvcsStmt.executeQuery();
            while (activeSvcsRS.next()) {
                BasicService svc = new BasicService(node.getNodeId(), activeSvcsRS.getString(1), activeSvcsRS.getLong(2));
                if (svc.openOutageExists(dbConn)) {
                    if (log.isDebugEnabled())
                        log.debug("handleNodeDown: " + svc + " already down");
                } else {
                    // set parms
                    newOutageWriter.setLong(1, getNextOutageId(dbConn));
                    newOutageWriter.setLong(2, eventID);
                    newOutageWriter.setLong(3, svc.getNodeId());
                    newOutageWriter.setString(4, svc.getIpAddr());
                    newOutageWriter.setLong(5, svc.getServiceId());
                    newOutageWriter.setTimestamp(6, convertEventTimeIntoTimestamp(eventTime));

                    // execute update
                    newOutageWriter.executeUpdate();

                    if (log.isDebugEnabled())
                        log.debug("handleNodeDown: Recording outage for " + svc);
                }

            }
            // close result set
            activeSvcsRS.close();

            // commit work
            DbUtil.commit(log, dbConn, "Outage recorded for all active services for " + node, "nodeDown could not be recorded  for nodeId: " + node);

            // close statements
            activeSvcsStmt.close();
            openStmt.close();
            newOutageWriter.close();
        } catch (SQLException sqle) {
            DbUtil.rollback(log, dbConn, "SQL exception while handling \'nodeDown\'", sqle);
        } finally {
            DbUtil.close(log, dbConn);
        }
    }

    /**
     * Handle node up events. Record the 'nodeUp' event in the outages table -
     * close all open outage entries for the nodeid in the outages table.
     */
    private void handleNodeUp(long eventID, String eventTime, BasicNode node) {
        Category log = ThreadCategory.getInstance(OutageWriter.class);

        if (eventID == -1 || !node.isValid()) {
            log.warn(EventConstants.NODE_UP_EVENT_UEI + " ignored - info incomplete - eventid/nodeid: " + eventID + "/" + node);
            return;
        }

        Connection dbConn = null;
        try {
            dbConn = getConnection();

            int count = 0;

            if (node.openOutageExists(dbConn)) {

                // Set the database commit mode
                dbConn.setAutoCommit(false);
                
                // Prepare SQL statement used to update the 'regained time' for
                // all open outage entries for the nodeid
                PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGES_FOR_NODE);
                outageUpdater.setLong(1, eventID);
                outageUpdater.setTimestamp(2, convertEventTimeIntoTimestamp(eventTime));
                outageUpdater.setLong(3, node.getNodeId());
                count = outageUpdater.executeUpdate();

                // close statement
                outageUpdater.close();
            } else {
                // Outage table does not have an open record.
                log.warn("\'" + EventConstants.NODE_UP_EVENT_UEI + "\' for " + node + " no open record.");
            }

            // commit work
            DbUtil.commit(log, dbConn, "nodeUp closed " + count + " outages for nodeid " + node + " in DB", "nodeUp could not be recorded  for nodeId: " + node);

        } catch (SQLException se) {
            DbUtil.rollback(log, dbConn, "SQL exception while handling \'nodeRegainedService\'", se);
        } finally {
            DbUtil.close(log, dbConn);
        }
    }

    /**
     * Handles interface up events. Record the 'interfaceUp' event in the
     * outages table - close all open outage entries for the nodeid/ip in the
     * outages table.
     */
    private void handleInterfaceUp(long eventID, String eventTime, BasicInterface iface) {
        Category log = ThreadCategory.getInstance(OutageWriter.class);

        if (eventID == -1 || !iface.isValid()) {
            log.warn(EventConstants.INTERFACE_UP_EVENT_UEI + " ignored - info incomplete - eventid/nodeid/ipAddr: " + eventID + "/" + iface);
            return;
        }

        Connection dbConn = null;
        try {
            dbConn = getConnection();

            if (iface.openOutageExists(dbConn)) {
                // Set the database commit mode
                dbConn.setAutoCommit(false);
                
                // Prepare SQL statement used to update the 'regained time' for
                // all open outage entries for the nodeid/ipaddr
                PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGES_FOR_INTERFACE);
                outageUpdater.setLong(1, eventID);
                outageUpdater.setTimestamp(2, convertEventTimeIntoTimestamp(eventTime));
                outageUpdater.setLong(3, iface.getNodeId());
                outageUpdater.setString(4, iface.getIpAddr());
                int count = outageUpdater.executeUpdate();

                // close statement
                outageUpdater.close();

                // commit work
                DbUtil.commit(log, dbConn, "handleInterfaceUp: interfaceUp closed " + count + " outages for nodeid/ip " + iface + " in DB", "interfaceUp could not be recorded for nodeId/ipaddr: " + iface);
            } else {
                log.warn("\'" + EventConstants.INTERFACE_UP_EVENT_UEI + "\' for " + iface + " ignored.");
            }
        } catch (SQLException se) {
            DbUtil.rollback(log, dbConn, "SQL exception while handling \'interfaceUp\'", se);
        } finally {
            DbUtil.close(log, dbConn);
        }
    }

    /**
     * Hanlde node regained service events. Record the 'nodeRegainedService'
     * event in the outages table - close the outage entry in the table if the
     * service is currently down.
     * @param svc TODO
     */
    private void handleNodeRegainedService(long eventID, String eventTime, BasicService svc) {
        Category log = ThreadCategory.getInstance(OutageWriter.class);

        if (eventID == -1 || !svc.isValid()) {
            log.warn(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI + " ignored - info incomplete - eventid/nodeid/ip/svc: " + eventID + "/" + svc);
            return;
        }

        Connection dbConn = null;
        try {
            dbConn = getConnection();

            if (svc.openOutageExists(dbConn)) {
                // Set the database commit mode
                dbConn.setAutoCommit(false);

                // Prepare SQL statement used to update the 'regained time' in
                // an open entry
                PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGE_FOR_SERVICE);
                outageUpdater.setLong(1, eventID);
                outageUpdater.setTimestamp(2, convertEventTimeIntoTimestamp(eventTime));
                outageUpdater.setLong(3, svc.getNodeId());
                outageUpdater.setString(4, svc.getIpAddr());
                outageUpdater.setLong(5, svc.getServiceId());
                outageUpdater.executeUpdate();

                // close statement
                outageUpdater.close();

                // commit work
                DbUtil.commit(log, dbConn, "nodeRegainedService: closed outage for nodeid/ip/service " + svc + " in DB", "nodeRegainedService could not be recorded  for nodeId/ipAddr/service: " + svc);
            } else {
                // Outage table does not have an open record.
                log.warn("\'" + EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI + "\' for " + svc + " does not have open record.");
            }
        } catch (SQLException se) {
            DbUtil.rollback(log, dbConn, "SQL exception while handling \'nodeRegainedService\'", se);
        } finally {
            DbUtil.close(log, dbConn);
        }
    }

    /**
     * <p>
     * Record the 'interfaceReparented' event in the outages table.
     * Change'outages' table entries associated with the old nodeid/interface
     * pairing so that those outage entries will be associated with the new
     * nodeid/interface pairing.
     * </p>
     * 
     * <p>
     * <strong>Note: </strong>This event has no impact on the event id reference
     * fields
     * </p>
     */
    private void handleInterfaceReparented(String ipAddr, Parms eventParms) {
        Category log = ThreadCategory.getInstance(OutageWriter.class);

        if (log.isDebugEnabled())
            log.debug("interfaceReparented event received...");

        if (ipAddr == null || eventParms == null) {
            log.warn(EventConstants.INTERFACE_REPARENTED_EVENT_UEI + " ignored - info incomplete - ip/parms: " + ipAddr + "/" + eventParms);
            return;
        }

        long oldNodeId = -1;
        long newNodeId = -1;

        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        Enumeration parmEnum = eventParms.enumerateParm();
        while (parmEnum.hasMoreElements()) {
            Parm parm = (Parm) parmEnum.nextElement();
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null)
                continue;
            else
                parmContent = parmValue.getContent();

            // old nodeid
            if (parmName.equals(EventConstants.PARM_OLD_NODEID)) {
                try {
                    oldNodeId = Integer.valueOf(parmContent).intValue();
                } catch (NumberFormatException nfe) {
                    log.warn("Parameter " + EventConstants.PARM_OLD_NODEID + " cannot be non-numeric");
                    oldNodeId = -1;
                }

            }

            // new nodeid
            else if (parmName.equals(EventConstants.PARM_NEW_NODEID)) {
                try {
                    newNodeId = Integer.valueOf(parmContent).intValue();
                } catch (NumberFormatException nfe) {
                    log.warn("Parameter " + EventConstants.PARM_NEW_NODEID + " cannot be non-numeric");
                    newNodeId = -1;
                }
            }
        }

        if (newNodeId == -1 || oldNodeId == -1) {
            log.warn("Unable to process 'interfaceReparented' event, invalid event parm.");
            return;
        }

        BasicInterface iface = new BasicInterface(oldNodeId, ipAddr);
        Connection dbConn = null;
        try {
            dbConn = getConnection();

            // Set the database commit mode
            dbConn.setAutoCommit(false);

            // Issue SQL update to change the 'outages' table entries
            // associated with the old nodeid/interface pairing
            // so that those outage entries will be associated with
            // the new nodeid/interface pairing.

            // Prepare SQL statement used to reparent outage table entries -
            // used when a 'interfaceReparented' event is received
            PreparedStatement reparentOutagesStmt = dbConn.prepareStatement(OutageConstants.DB_REPARENT_OUTAGES);
            reparentOutagesStmt.setLong(1, newNodeId);
            reparentOutagesStmt.setLong(2, iface.getNodeId());
            reparentOutagesStmt.setString(3, iface.getIpAddr());
            int count = reparentOutagesStmt.executeUpdate();

            // commit work
            String s = "Reparented " + count + " outages - ip: " + iface.getIpAddr() + " reparented from " + iface.getNodeId() + " to " + newNodeId;
            String f = "reparent outages failed for newNodeId/ipAddr: " + newNodeId + "/" + iface.getIpAddr();
            DbUtil.commit(log, dbConn, s, f);

            // close statement
            reparentOutagesStmt.close();

        } catch (SQLException se) {
            DbUtil.rollback(log, dbConn, "SQL exception while handling \'interfaceReparented\'", se);
        } finally {
            DbUtil.close(log, dbConn);
        }
    }

    private Connection getConnection() throws SQLException {
        return m_outageMgr.getConnection();
    }

    /**
     * This method creates an event for the passed parameters.
     * 
     * @param uei
     *            Event to generate and send
     * @param eventDate
     *            Time to be set for the event
     * @param nodeID
     *            Node identifier associated with this event
     * @param ipAddr
     *            Interface address associated with this event
     * @param serviceName
     *            Service name associated with this event
     */
    private Event createEvent(String uei, java.util.Date eventDate, long nodeID, String ipAddr, String serviceName) {
        // build event to send
        Event newEvent = new Event();

        newEvent.setUei(uei);
        newEvent.setSource("OutageManager");

        // Convert integer nodeID to String
        newEvent.setNodeid(nodeID);

        if (ipAddr != null)
            newEvent.setInterface(ipAddr);

        if (serviceName != null)
            newEvent.setService(serviceName);

        newEvent.setTime(EventConstants.formatToString(eventDate));

        return newEvent;
    }

    /**
     * Process an event. Read the event UEI, nodeid, interface and service -
     * depending on the UEI, read event parms, if necessary, and process as
     * appropriate.
     */
    private void processEvent() {
        Category log = ThreadCategory.getInstance(OutageWriter.class);

        if (m_event == null) {
            if (log.isDebugEnabled())
                log.debug("Event is null, nothing to process");
            return;
        }

        if (log.isDebugEnabled())
            log.debug("About to process event: " + m_event.getUei());

        //
        // Check to make sure the event has a uei
        //
        String uei = m_event.getUei();
        if (uei == null) {
            // should only get registered events
            if (log.isDebugEnabled())
                log.debug("Event received with null UEI, ignoring event");
            return;
        }

        // get eventid
        long eventID = -1;
        if (m_event.hasDbid())
            eventID = m_event.getDbid();

        // convert the node id
        long nodeID = -1;
        if (m_event.hasNodeid())
            nodeID = m_event.getNodeid();

        String ipAddr = m_event.getInterface();
        String service = m_event.getService();
        String eventTime = m_event.getTime();

        if (log.isDebugEnabled())
            log.debug("processEvent: Event\nuei\t\t" + uei + "\neventid\t\t" + eventID + "\nnodeid\t\t" + nodeID + "\nipaddr\t\t" + ipAddr + "\nservice\t\t" + service + "\neventtime\t" + (eventTime != null ? eventTime : "<null>"));

        // get service id for the service name
        long serviceID = -1;
        if (service != null) {
            try {
                serviceID = getServiceID(service);
            } catch (SQLException sqlE) {
                log.warn("Error converting service name \"" + service + "\" to an integer identifier, storing -1", sqlE);
            }
        }

        //
        // Check for any of the following UEIs:
        //
        // nodeLostService
        // interfaceDown
        // nodeDown
        // nodeUp
        // interfaceUp
        // nodeRegainedService
        // deleteService
        // interfaceReparented
        //
        if (uei.equals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)) {
            handleNodeLostService(eventID, eventTime, new BasicService(nodeID, ipAddr, serviceID));
        } else if (uei.equals(EventConstants.INTERFACE_DOWN_EVENT_UEI)) {
            handleInterfaceDown(eventID, eventTime, new BasicInterface(nodeID, ipAddr));
        } else if (uei.equals(EventConstants.NODE_DOWN_EVENT_UEI)) {
            handleNodeDown(eventID, eventTime, new BasicNode(nodeID));
        } else if (uei.equals(EventConstants.NODE_UP_EVENT_UEI)) {
            handleNodeUp(eventID, eventTime, new BasicNode(nodeID));
        } else if (uei.equals(EventConstants.INTERFACE_UP_EVENT_UEI)) {
            handleInterfaceUp(eventID, eventTime, new BasicInterface(nodeID, ipAddr));
        } else if (uei.equals(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)) {
            handleNodeRegainedService(eventID, eventTime, new BasicService(nodeID, ipAddr, serviceID));
        } else if (uei.equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI)) {
            handleInterfaceReparented(ipAddr, m_event.getParms());
        }
    }

    /**
     * The constructor.
     * @param mgr
     * 
     * @param event
     *            the event for this outage writer.
     */
    public OutageWriter(OutageManager mgr, Event event) {
        m_outageMgr = mgr;
        m_event = event;
    }

    /**
     * Process the event depending on the UEI.
     */
    public void run() {
        try {
            processEvent();
        } catch (Throwable t) {
            Category log = ThreadCategory.getInstance(OutageWriter.class);
            log.warn("Unexpected exception processing event", t);
        }
    }
}
