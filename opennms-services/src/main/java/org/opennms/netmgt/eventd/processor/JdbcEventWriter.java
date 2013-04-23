/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.eventd.EventdConstants;
import org.opennms.netmgt.eventd.db.AutoAction;
import org.opennms.netmgt.eventd.db.OperatorAction;
import org.opennms.netmgt.eventd.db.SnmpInfo;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.Constants;
import org.opennms.netmgt.model.events.EventProcessor;
import org.opennms.netmgt.model.events.Parameter;
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
 * While loading multiple values of the same element into a single DB column, the
 * multiple values are delimited by MULTIPLE_VAL_DELIM.
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
 * @deprecated Replace with a Hibernate implementation. See bug NMS-3033. Actually
 * it doesn't have any details. :P
 * http://issues.opennms.org:8280/browse/NMS-3033
 *
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public final class JdbcEventWriter extends AbstractJdbcPersister implements EventProcessor, InitializingBean {
    /**
     * {@inheritDoc}
     *
     * The method that inserts the event into the database
     */
    public void process(final Header eventHeader, final Event event) throws SQLException, DataAccessException {
        if (!checkEventSanityAndDoWeProcess(event, "JdbcEventWriter")) {
            return;
        }

        LogUtils.debugf(this, "JdbcEventWriter: processing %s nodeid: %d ipaddr: %s serviceid: %s time: %s", event.getUei(), event.getNodeid(), event.getInterface(), event.getService(), event.getTime());

        final Connection connection = getDataSource().getConnection();

        try {
            connection.setAutoCommit(false);

            try {
                insertEvent(eventHeader, event, connection);

                connection.commit();
            } catch (final SQLException e) {
                LogUtils.warnf(this, e, "Error inserting event into the datastore.");
                try {
                    connection.rollback();
                } catch (final Throwable e2) {
                    LogUtils.warnf(this, e2, "Rollback of transaction failed.");
                }

                throw e;
            } catch (final DataAccessException e) {
                LogUtils.warnf(this, e, "Error inserting event into the datastore.");
                try {
                    connection.rollback();
                } catch (final Throwable e2) {
                    LogUtils.warnf(this, e2, "Rollback of transaction failed.");
                }

                throw e;
            }
        } finally {
            try {
                connection.close();
            } catch (final SQLException e) {
                LogUtils.warnf(this, e, "SQLException while closing database connection.");
            }
        }

        LogUtils.debugf(this, "EventWriter finished for : %s", event.getUei());
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
    private void insertEvent(final Header eventHeader, final Event event, final Connection connection) throws SQLException {
        // Execute the statement to get the next event id
        final int eventID = getNextId();

        LogUtils.debugf(this, "DBID: %d", eventID);

        synchronized (event) {
            event.setDbid(eventID);
        }
        final DBUtils d = new DBUtils(getClass());

        try {
            final PreparedStatement insStmt = connection.prepareStatement(EventdConstants.SQL_DB_INS_EVENT);
            d.watch(insStmt);

            // eventID
            insStmt.setInt(1, eventID);

            // eventUEI
            insStmt.setString(2, Constants.format(event.getUei(), EVENT_UEI_FIELD_SIZE));

            // nodeID
            final Long nodeid = event.getNodeid();
            set(insStmt, 3, event.hasNodeid() ? nodeid.intValue() : -1);

            // eventTime
            insStmt.setTimestamp(4, getEventTime(event));

            // Resolve the event host to a hostname using the ipInterface table
            String hostname = getEventHost(event);

            // eventHost
            set(insStmt, 5, Constants.format(hostname, EVENT_HOST_FIELD_SIZE));

            // ipAddr
            set(insStmt, 6, Constants.format(event.getInterface(), EVENT_INTERFACE_FIELD_SIZE));

            // eventDpName
            String dpName = "localhost";
            if (eventHeader != null && eventHeader.getDpName() != null) {
                dpName = Constants.format(eventHeader.getDpName(), EVENT_DPNAME_FIELD_SIZE);
            } else if (event.getDistPoller() != null) {
                dpName = Constants.format(event.getDistPoller(), EVENT_DPNAME_FIELD_SIZE);
            }
            insStmt.setString(7, dpName);

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
            final String parametersString=Parameter.format(event);
            set(insStmt, 11, Constants.format(parametersString, 0));

            // eventCreateTime
            final Timestamp eventCreateTime = new Timestamp(System.currentTimeMillis());
            insStmt.setTimestamp(12, eventCreateTime);

            // eventDescr
            set(insStmt, 13, Constants.format(event.getDescr(), 0));

            // eventLoggroup
            set(insStmt, 14, (event.getLoggroupCount() > 0) ? Constants.format(event.getLoggroup(), EVENT_LOGGRP_FIELD_SIZE) : null);

            // eventLogMsg
            // eventLog
            // eventDisplay
            if (event.getLogmsg() != null) {
                // set log message
                set(insStmt, 15, Constants.format(event.getLogmsg().getContent(), 0));
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
            set(insStmt, 18, OnmsSeverity.get(event.getSeverity()).getId());

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
                final List<Operaction> a = new ArrayList<Operaction>();
                final List<String> b = new ArrayList<String>();

                for (final Operaction eoa : event.getOperactionCollection()) {
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
                set(insStmt, 28, event.getTticket().getState().equals("on") ? 1 : 0);
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

        LogUtils.debugf(this, "SUCCESSFULLY added %s related  data into the EVENTS table.", event.getUei());
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
    
    String getHostName(final int nodeId, final String hostip) throws SQLException {
        try {
            final String hostname = new SimpleJdbcTemplate(getDataSource()).queryForObject(EventdConstants.SQL_DB_HOSTIP_TO_HOSTNAME, String.class, new Object[] { nodeId, hostip });
            return (hostname != null) ? hostname : hostip;
        } catch (final EmptyResultDataAccessException e) {
            return hostip;
        }
    }

    /**
     * @param event
     * @param log
     * @return
     */
    private int getEventServiceId(final Event event) {
        if (event.getService() == null) {
            return -1;
        }
        
        try {
            return getServiceID(event.getService());
        } catch (final Throwable t) {
            LogUtils.warnf(this, t, "Error converting service name \"%s\" to an integer identifier, storing -1.", event.getService());
            return -1;
        }
    }

    /**
     * <p>getEventHost</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param connection a {@link java.sql.Connection} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getEventHost(final Event event) {
        if (event.getHost() == null) {
            return null;
        }
        
        // If the event doesn't have a node ID, we can't lookup the IP address and be sure we have the right one since we don't know what node it is on
        if (!event.hasNodeid()) {
            return event.getHost();
        }
        
        try {
            return getHostName(event.getNodeid().intValue(), event.getHost());
        } catch (final Throwable t) {
            LogUtils.warnf(this, t, "Error converting host IP \"%s\" to a hostname, storing the IP.", event.getHost());
            return event.getHost();
        }
    }
}
