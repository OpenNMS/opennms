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
// 2008 Feb 10: Pull common event checks into checkEventSanityAndDoWeProcess in
//              AbstractJdbcPersister. - dj@opennms.org
// 2008 Feb 06: Fix bugs from bug #2247. - dj@opennms.org
// 2008 Jan 28: Catch EmptyResultDataAccessException in getHostName(String).
//              Thanks for the catch, jeffg! - dj@opennms.org
// 2008 Jan 27: Make thread-safe. - dj@opennms.org
// 2008 Jan 27: Push methods in Persist that are only used by a single
//              subclass into that subclass.
// 2008 Jan 26: Dependency injection using setter injection instead of
//              constructor injection and implement InitializingBean.
//              Move some common setters and initializion into Persist.
//              Implement log method. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.eventd.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.eventd.EventdConstants;
import org.opennms.netmgt.eventd.db.AutoAction;
import org.opennms.netmgt.eventd.db.Constants;
import org.opennms.netmgt.eventd.db.OperatorAction;
import org.opennms.netmgt.eventd.db.Parameter;
import org.opennms.netmgt.eventd.db.SnmpInfo;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Operaction;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * EventWriter loads the information in each 'Event' into the database.
 * 
 * While loading mutiple values of the same element into a single DB column, the
 * mutiple values are delimited by MULTIPLE_VAL_DELIM.
 * 
 * When an element and its attribute are loaded into a single DB column, the
 * value and the attribute are separated by a DB_ATTRIB_DELIM.
 * 
 * When using delimiters to append values, if the values already have the
 * delimiter, the delimiter in the value is escaped as in URLs.
 * 
 * Values for the ' <parms>' block are loaded with each parm name and parm value
 * delimited with the NAME_VAL_DELIM.
 * 
 * @see org.opennms.netmgt.eventd.db.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.eventd.db.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.eventd.db.Constants#NAME_VAL_DELIM
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public final class JdbcEventWriter extends AbstractJdbcPersister implements EventProcessor, InitializingBean {
    /**
     * The method that inserts the event into the database
     * 
     * @param eventHeader
     *            the event header
     * @param event
     *            the actual event to be inserted
     */
    public void process(Header eventHeader, Event event) throws SQLException, DataAccessException {
        if (!checkEventSanityAndDoWeProcess(event, "JdbcEventWriter")) {
            return;
        }
            
        if (log().isDebugEnabled()) {
            log().debug("JdbcEventWriter: processing " + event.getUei() + " nodeid: " + event.getNodeid() + " ipaddr: " + event.getInterface() + " serviceid: " + event.getService());
        }

        Connection connection = getDataSource().getConnection();

        try {
            connection.setAutoCommit(false);

            try {
                insertEvent(eventHeader, event, connection);

                connection.commit();
            } catch (SQLException e) {
                log().warn("JdbcEventWriter: Error inserting event into the datastore: " + e, e);
                try {
                    connection.rollback();
                } catch (Exception e2) {
                    log().warn("JdbcEventWriter: Rollback of transaction failed: " + e2, e2);
                }

                throw e;
            } catch (DataAccessException e) {
                log().warn("JdbcEventWriter: Error inserting event into the datastore: " + e, e);
                try {
                    connection.rollback();
                } catch (Exception e2) {
                    log().warn("JdbcEventWriter: Rollback of transaction failed: " + e2, e2);
                }

                throw e;
            }
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log().warn("JdbcEventWriter: SQLException while closing database connection: " + e, e);
            }
        }

        if (log().isDebugEnabled()) {
            log().debug("JdbcEventWriter: EventWriter finished for : " + event.getUei());
        }
    }

    /**
     * Insert values into the EVENTS table
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the event to the
     *                database.
     * @exception java.lang.NullPointerException
     *                Thrown if a required resource cannot be found in the
     *                properties file.
     */
    private void insertEvent(Header eventHeader, Event event, Connection connection) throws SQLException {
        // Execute the statement to get the next event id
        int eventID = getNextId();

        if (log().isDebugEnabled()) {
            log().debug("EventWriter: DBID: " + eventID);
        }

        synchronized (event) {
            event.setDbid(eventID);
        }
        final DBUtils d = new DBUtils(getClass());

        try {
            PreparedStatement insStmt = connection.prepareStatement(EventdConstants.SQL_DB_INS_EVENT);
            d.watch(insStmt);

            // eventID
            insStmt.setInt(1, eventID);

            // eventUEI
            insStmt.setString(2, Constants.format(event.getUei(), EVENT_UEI_FIELD_SIZE));

            // nodeID
            int nodeid = (int) event.getNodeid();
            set(insStmt, 3, event.hasNodeid() ? nodeid : -1);

            // eventTime
            insStmt.setTimestamp(4, getEventTime(event));

            // Resolve the event host to a hostname using the ipInterface table
            String hostname = getEventHost(event, connection);

            // eventHost
            set(insStmt, 5, Constants.format(hostname, EVENT_HOST_FIELD_SIZE));

            // ipAddr
            set(insStmt, 6, Constants.format(event.getInterface(), EVENT_INTERFACE_FIELD_SIZE));

            // eventDpName
            insStmt.setString(7, (eventHeader != null) ? Constants.format(eventHeader.getDpName(), EVENT_DPNAME_FIELD_SIZE) : "undefined");

            // eventSnmpHost
            set(insStmt, 8, Constants.format(event.getSnmphost(), EVENT_SNMPHOST_FIELD_SIZE));

            // service identifier - convert the service name to a service id
            set(insStmt, 9, getEventServiceId(event));

            // eventSnmp
            if (event.getSnmp() != null) {
                insStmt.setString(10, SnmpInfo.format(event.getSnmp(), EVENT_SNMP_FIELD_SIZE));
            } else {
                insStmt.setNull(10, Types.VARCHAR);
            }

            // eventParms

            // Replace any null bytes with a space, otherwise postgres will complain about encoding in UNICODE 
            String parametersString=(event.getParms() != null) ? Parameter.format(event.getParms()) : null;
            if (parametersString != null) {
                parametersString=parametersString.replace((char)0, ' ');
            }

            set(insStmt, 11, parametersString);

            // grab the ifIndex out of the parms if it is defined   
            if (event.hasIfIndex()) {
                if (event.getParms() != null) {
                    Parameter.format(event.getParms());
                }
            }

            // eventCreateTime
            Timestamp eventCreateTime = new Timestamp(System.currentTimeMillis());
            insStmt.setTimestamp(12, eventCreateTime);

            // eventDescr
            set(insStmt, 13, Constants.format(event.getDescr(), EVENT_DESCR_FIELD_SIZE));

            // eventLoggroup
            set(insStmt, 14, (event.getLoggroupCount() > 0) ? Constants.format(event.getLoggroup(), EVENT_LOGGRP_FIELD_SIZE) : null);

            // eventLogMsg
            // eventLog
            // eventDisplay
            if (event.getLogmsg() != null) {
                // set log message
                set(insStmt, 15, Constants.format(event.getLogmsg().getContent(), EVENT_LOGMSG_FIELD_SIZE));
                String logdest = event.getLogmsg().getDest();
                if (logdest.equals("logndisplay")) {
                    // if 'logndisplay' set both log and display column to yes
                    set(insStmt, 16, MSG_YES);
                    set(insStmt, 17, MSG_YES);
                } else if (logdest.equals("logonly")) {
                    // if 'logonly' set log column to true
                    set(insStmt, 16, MSG_YES);
                    set(insStmt, 17, MSG_NO);
                } else if (logdest.equals("displayonly")) {
                    // if 'displayonly' set display column to true
                    set(insStmt, 16, MSG_NO);
                    set(insStmt, 17, MSG_YES);
                } else if (logdest.equals("suppress")) {
                    // if 'suppress' set both log and display to false
                    set(insStmt, 16, MSG_NO);
                    set(insStmt, 17, MSG_NO);
                }
            } else {
                insStmt.setNull(15, Types.VARCHAR);

                /*
                 * If this is an event that had no match in the event conf
                 * mark it as to be logged and displayed so that there
                 * are no events that slip through the system
                 * without the user knowing about them
                 */
                set(insStmt, 17, MSG_YES);
            }

            // eventSeverity
            set(insStmt, 18, Constants.getSeverity(event.getSeverity()));

            // eventPathOutage
            set(insStmt, 19, (event.getPathoutage() != null) ? Constants.format(event.getPathoutage(), EVENT_PATHOUTAGE_FIELD_SIZE) : null);

            // eventCorrelation
            set(insStmt, 20, (event.getCorrelation() != null) ? org.opennms.netmgt.eventd.db.Correlation.format(event.getCorrelation(), EVENT_CORRELATION_FIELD_SIZE) : null);

            // eventSuppressedCount
            insStmt.setNull(21, Types.INTEGER);

            // eventOperInstruct
            set(insStmt, 22, Constants.format(event.getOperinstruct(), EVENT_OPERINSTRUCT_FIELD_SIZE));

            // eventAutoAction
            set(insStmt, 23, (event.getAutoactionCount() > 0) ? AutoAction.format(event.getAutoaction(), EVENT_AUTOACTION_FIELD_SIZE) : null);

            // eventOperAction / eventOperActionMenuText
            if (event.getOperactionCount() > 0) {
                List<Operaction> a = new ArrayList<Operaction>();
                List<String> b = new ArrayList<String>();

                for (Operaction eoa : event.getOperactionCollection()) {
                    a.add(eoa);
                    b.add(eoa.getMenutext());
                }

                set(insStmt, 24, OperatorAction.format(a, EVENT_OPERACTION_FIELD_SIZE));
                set(insStmt, 25, Constants.format(b, EVENT_OPERACTION_MENU_FIELD_SIZE));
            } else {
                insStmt.setNull(24, Types.VARCHAR);
                insStmt.setNull(25, Types.VARCHAR);
            }

            // eventNotification, this column no longer needed
            insStmt.setNull(26, Types.VARCHAR);

            // eventTroubleTicket / eventTroubleTicket state
            if (event.getTticket() != null) {
                set(insStmt, 27, Constants.format(event.getTticket().getContent(), EVENT_TTICKET_FIELD_SIZE));
                int ttstate = 0;
                if (event.getTticket().getState().equals("on")) {
                    ttstate = 1;
                }

                set(insStmt, 28, ttstate);
            } else {
                insStmt.setNull(27, Types.VARCHAR);
                insStmt.setNull(28, Types.INTEGER);
            }

            // eventForward
            set(insStmt, 29, (event.getForwardCount() > 0) ? org.opennms.netmgt.eventd.db.Forward.format(event.getForward(), EVENT_FORWARD_FIELD_SIZE) : null);

            // event mouseOverText
            set(insStmt, 30, Constants.format(event.getMouseovertext(), EVENT_MOUSEOVERTEXT_FIELD_SIZE));

            // eventAckUser
            if (event.getAutoacknowledge() != null && event.getAutoacknowledge().getState().equals("on")) {
                set(insStmt, 31, Constants.format(event.getAutoacknowledge().getContent(), EVENT_ACKUSER_FIELD_SIZE));

                // eventAckTime - if autoacknowledge is present,
                // set time to event create time
                set(insStmt, 32, eventCreateTime);
            } else {
                insStmt.setNull(31, Types.INTEGER);
                insStmt.setNull(32, Types.TIMESTAMP);
            }

            // eventSource
            set(insStmt, 33, Constants.format(event.getSource(), EVENT_SOURCE_FIELD_SIZE));

            // ifindex
            if (event.hasIfIndex()) {
                set(insStmt, 34, event.getIfIndex());
            } else {
                insStmt.setNull(34, Types.INTEGER);
            }
            
            // execute
            insStmt.executeUpdate();
        } finally {
            d.cleanUp();
        }

        if (log().isDebugEnabled()) {
            log().debug("SUCCESSFULLY added " + event.getUei() + " related  data into the EVENTS table");
        }
    }


    /**
     * This method is used to convert the event host into a hostname id by
     * performing a lookup in the database. If the conversion is successful then
     * the corresponding hosname will be returned to the caller.
     * @param nodeId TODO
     * @param hostip
     *            The event host
     * 
     * @return The hostname
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error accessing the stored data or
     *                the SQL text is malformed.
     * 
     * @see EventdConstants#SQL_DB_HOSTIP_TO_HOSTNAME
     * 
     */
    // FIXME: This uses JdbcTemplate and not the passed in connection
    String getHostName(int nodeId, String hostip, Connection connection) throws SQLException {
        try {
            String hostname = new SimpleJdbcTemplate(getDataSource()).queryForObject(EventdConstants.SQL_DB_HOSTIP_TO_HOSTNAME, String.class, new Object[] { nodeId, hostip });
            return (hostname != null) ? hostname : hostip;
        } catch (EmptyResultDataAccessException e) {
            return hostip;
        }
    }

    /**
     * @param event
     * @param log
     * @return
     */
    private int getEventServiceId(Event event) {
        if (event.getService() == null) {
            return -1;
        }
        
        try {
            return getServiceID(event.getService());
        } catch (Throwable t) {
            log().warn("EventWriter.add: Error converting service name \"" + event.getService() + "\" to an integer identifier, storing -1.  Error: " + t, t);
            return -1;
        }
    }

    /**
     * @param event
     * @return
     */
    protected String getEventHost(Event event, Connection connection) {
        if (event.getHost() == null) {
            return null;
        }
        
        // If the event doesn't have a node ID, we can't lookup the IP address and be sure we have the right one since we don't know what node it is on
        if (!event.hasNodeid()) {
            return event.getHost();
        }
        
        try {
            return getHostName((int) event.getNodeid(), event.getHost(), connection);
        } catch (Throwable t) {
            log().warn("EventWriter.add: Error converting host IP \"" + event.getHost() + "\" to a hostname, storing the IP.  Error: " + t, t);
            return event.getHost();
        }
    }
}
