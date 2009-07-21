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
// 2008 Jan 28: Add a few tests to check that the Event is valid. - dj@opennms.org
// 2008 Jan 28: Use EmptyResultDataAccessException instead of
//              IncorrectResultSizeDataAccessException. - dj@opennms.org
// 2008 Jan 27: Make thread-safe and use Spring's SQL exception translation
//              to help us only retry the alarm insertion when we see a
//              data integrity violation (violation of a uniqueness
//              constraint). - dj@opennms.org
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

import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.eventd.EventdConstants;
import org.opennms.netmgt.eventd.db.Constants;
import org.opennms.netmgt.eventd.db.Parameter;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.util.Assert;

/**
 * AlarmWriter writes events classified as alarms to the database.
 * Alarms are deduplicated using:
 * Uei, dpname, nodeid, serviceid, reductionKey
 * 
 * The reductionKey is a string attribute created by the user
 * for a UEI defined in eventConf.  Can be a literal or more likely
 * a tokenized string such as %interface%. 
 *  
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
@Deprecated
public final class JdbcAlarmWriter extends AbstractJdbcPersister implements EventProcessor, InitializingBean {
    /**
     * The method that inserts the event into the database
     * 
     * @param eventHeader
     *            the event header
     * @param event
     *            the actual event to be inserted
     */
    public void process(Header eventHeader, Event event) throws SQLException {
        if (!checkEventSanityAndDoWeProcess(event, "JdbcAlarmWriter")) {
            return;
        }
        
        if (event.getAlarmData() == null) {
            if (log().isDebugEnabled()) {
                log().debug("JdbcAlarmWriter: uei '" + event.getUei() + "' does not have alarm data; not processing into an alarm.");
            }
            return;
        }

        Assert.isTrue(event.getDbid() > 0, "event does not have a dbid");
        
        if (log().isDebugEnabled()) {
            log().debug("JdbcAlarmWriter dbRun for : " + event.getUei() + " nodeid: " + event.getNodeid() + " ipaddr: " + event.getInterface() + " serviceid: " + event.getService());
        }

        /*
         * Try twice incase the transaction fails.  This could happen if 2 or more threads query the db
         * at the same time and determine that insert needs to happen.  One insert will complete the other
         * will fail.  The next time through the loop, the alarm will be reduced with an update. 
         */
        boolean updated = false;
        for (int attempt = 1; attempt <= 2 && !updated; attempt++) {
            Connection connection = getDataSource().getConnection();

            try {
                connection.setAutoCommit(false);

                int alarmId = isReductionNeeded(eventHeader, event, connection);
                if (alarmId != -1) {
                    if (log().isDebugEnabled()) {
                        log().debug("JdbcAlarmWriter: Reducing event for " + event.getDbid() + " with UEI " + event.getUei());
                    }

                    updateAlarm(eventHeader, event, alarmId, connection);

                    if (event.getAlarmData().getAutoClean() == true) {
                        log().debug("JdbcAlarmWriter: deleting previous events for alarm " + alarmId);
                        cleanPreviousEvents(alarmId, event.getDbid(), connection);
                    }

                    updated = true;
                } else {
                    if (log().isDebugEnabled()) {
                        log().debug("JdbcAlarmWriter: Inserting new alarm (not reducing) for event " + event.getDbid() + " with UEI " + event.getUei());
                    }

                    try {
                        insertAlarm(eventHeader, event, connection);
                        updated = true;
                    } catch (DataIntegrityViolationException e) {
                        if (attempt > 1) {
                            log().error("JdbcAlarmWriter: Error in attempt: "+attempt+" inserting alarm for event " + event.getDbid() + " into the datastore: " + e, e);
                            throw e;
                        } else {
                            log().info("JdbcAlarmWriter: Retrying processing of alarm for event " + event.getDbid() + " after first attempt: " + e.getClass() + ": " + e.getMessage());
                        }
                    } 
                }
            } finally {
                if (updated) {
                    try {
                        connection.commit();
                    } catch (SQLException e) {
                        log().error("JdbcAlarmWriter: Commit of transaction failed: " + e, e);
                    }
                } else {
                    try {
                        connection.rollback();
                    } catch (SQLException e) {
                        log().error("JdbcAlarmWriter: Rollback of transaction failed: " + e, e);
                    }
                }

                connection.close();
            }
        }

        if (log().isDebugEnabled()) {
            log().debug("AlarmWriter finished for event " + event.getDbid() + " with UEI " + event.getUei());
        }
    }

    /**
     * Insert values into the ALARMS table
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the event to the
     *                database.
     * @exception java.lang.NullPointerException
     *                Thrown if a required resource cannot be found in the
     *                properties file.
     */
    private void insertAlarm(Header eventHeader, Event event, Connection connection) throws SQLException {
        int alarmID = -1;
        alarmID = getNextId();
        if (log().isDebugEnabled()) {
            log().debug("AlarmWriter: DBID: " + alarmID);
        }

        final DBUtils d = new DBUtils(getClass());
        try {
            PreparedStatement insStmt = connection.prepareStatement(EventdConstants.SQL_DB_ALARM_INS_EVENT);
            d.watch(insStmt);

            //Column 1, alarmId
            insStmt.setInt(1, alarmID);

            //Column 2, eventUie
            insStmt.setString(2, Constants.format(event.getUei(), EVENT_UEI_FIELD_SIZE));

            //Column 3, dpName
            insStmt.setString(3, (eventHeader != null) ? Constants.format(eventHeader.getDpName(), EVENT_DPNAME_FIELD_SIZE) : "undefined");

            // Column 4, nodeID
            int nodeid = (int) event.getNodeid();
            insStmt.setObject(4, event.hasNodeid() ? new Integer(nodeid) : null);

            // Column 5, ipaddr
            insStmt.setString(5, event.getInterface());

            //Column 6, serviceId
            //
            // convert the service name to a service id
            //
            int svcId = -1;
            if (event.getService() != null) {
                try {
                    svcId = getServiceID(event.getService());
                } catch (SQLException e) {
                    log().warn("insertAlarm: Error converting service name \"" + event.getService() + "\" to an integer identifier, storing -1: " + e, e);
                }
            }
            insStmt.setObject(6, (svcId == -1 ? null : new Integer(svcId)));

            //Column 7, reductionKey
            insStmt.setString(7, event.getAlarmData().getReductionKey());

            //Column 8, alarmType
            insStmt.setInt(8, event.getAlarmData().getAlarmType());

            //Column 9, counter
            insStmt.setInt(9, 1);

            //Column 10, serverity
            set(insStmt, 10, Constants.getSeverity(event.getSeverity()));

            //Column 11, lastEventId
            insStmt.setInt(11, event.getDbid());

            //Column 12, firstEventTime
            //Column 13, lastEventTime
            Timestamp eventTime = getEventTime(event);
            insStmt.setTimestamp(12, eventTime);
            insStmt.setTimestamp(13, eventTime);

            //Column 14, description
            set(insStmt, 14, Constants.format(event.getDescr(), EVENT_DESCR_FIELD_SIZE));

            //Column 15, logMsg
            if (event.getLogmsg() != null) {
                // set log message
                set(insStmt, 15, Constants.format(event.getLogmsg().getContent(), EVENT_LOGMSG_FIELD_SIZE));
            } else {
                insStmt.setNull(15, Types.VARCHAR);
            }

            //Column 16, operInstruct
            set(insStmt, 16, Constants.format(event.getOperinstruct(), EVENT_OPERINSTRUCT_FIELD_SIZE));

            //Column 17, tticketId
            //Column 18, tticketState
            if (event.getTticket() != null) {
                set(insStmt, 17, Constants.format(event.getTticket().getContent(), EVENT_TTICKET_FIELD_SIZE));
                int ttstate = 0;
                if (event.getTticket().getState().equals("on")) {
                    ttstate = 1;
                }
                set(insStmt, 18, ttstate);
            } else {
                insStmt.setNull(17, Types.VARCHAR);
                insStmt.setNull(18, Types.INTEGER);
            }

            //Column 19, mouseOverText
            set(insStmt, 19, Constants.format(event.getMouseovertext(), EVENT_MOUSEOVERTEXT_FIELD_SIZE));

            //Column 20, suppressedUntil
            set(insStmt, 20, eventTime);

            //Column 21, suppressedUser
            insStmt.setString(21, null);

            //Column 22, suppressedTime
            set(insStmt, 22, eventTime);

            //Column 23, alarmAckUser
            insStmt.setString(23, null);

            //Column 24, alarmAckTime
            insStmt.setTimestamp(24, null);

            //Column 25, clearUie
            //Column 26, x733AlarmType
            //Column 27, x733ProbableCause
            //Column 28, clearKey
            if (event.getAlarmData() == null) {
                insStmt.setString(25, null);
                insStmt.setString(26, null);
                insStmt.setInt(27, -1);
                insStmt.setString(28, null);
            } else {
                insStmt.setString(25, Constants.format(event.getAlarmData().getClearUei(), EVENT_UEI_FIELD_SIZE));
                insStmt.setString(26, Constants.format(event.getAlarmData().getX733AlarmType(), EVENT_X733_ALARMTYPE_SIZE));
                set(insStmt, 27, event.getAlarmData().getX733ProbableCause());
                set(insStmt, 28, event.getAlarmData().getClearKey());
            }
            
            // Column 29 ifindex
            if (event.hasIfIndex()) {
                set(insStmt, 29, event.getIfIndex());
            } else {
                insStmt.setNull(29, Types.INTEGER);
            }


            if (log().isDebugEnabled()) {
                log().debug("m_insStmt is: " + insStmt.toString());
            }
            
            // Column 30 eventParms

            // Replace any null bytes with a space, otherwise postgres will complain about encoding in UNICODE 
            String parametersString=(event.getParms() != null) ? Parameter.format(event.getParms()) : null;
            if (parametersString != null) {
                parametersString=parametersString.replace((char)0, ' ');
            }

            set(insStmt, 30, parametersString);
            insStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLErrorCodeSQLExceptionTranslator().translate("foo", "bar", e);
        } finally {
            d.cleanUp();
        }
        
        updateEventForAlarm(event, alarmID, connection);
    
        if (log().isDebugEnabled()) {
            log().debug("SUCCESSFULLY added " + event.getUei() + " related  data into the ALARMS table");
        }
    
    }

    private void updateAlarm(Header eventHeader, Event event, int alarmId, Connection connection) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
            PreparedStatement upDateStmt = connection.prepareStatement(EventdConstants.SQL_DB_ALARM_UPDATE_EVENT);
            d.watch(upDateStmt);
            
            upDateStmt.setInt(1, event.getDbid());
            upDateStmt.setTimestamp(2, getEventTime(event));
            set(upDateStmt, 3, Constants.format(event.getLogmsg().getContent(), EVENT_LOGMSG_FIELD_SIZE));
            upDateStmt.setString(4, event.getAlarmData().getReductionKey());

            if (log().isDebugEnabled()) {
                log().debug("Persist.updateAlarm: reducing event " + event.getDbid() +  " into alarm " + alarmId);
            }

            upDateStmt.executeUpdate();
        } finally {
            d.cleanUp();
        }

        updateEventForAlarm(event, alarmId, connection);
    }

    private void updateEventForAlarm(Event event, int alarmID, Connection connection) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
            PreparedStatement updateEventStmt = connection.prepareStatement(EventdConstants.SQL_DB_UPDATE_EVENT_WITH_ALARM_ID);
            d.watch(updateEventStmt);
            
            updateEventStmt.setInt(1, alarmID);
            updateEventStmt.setInt(2, event.getDbid());
            updateEventStmt.executeUpdate();
        } catch (SQLException e) {
            log().warn("Failed to update event " + event.getDbid() + " for alarm " + alarmID + ": " + e, e);
            throw e;
        } finally {
            d.cleanUp();
        }
    }

    // FIXME: This uses JdbcTemplate and not the passed in Connection
    private int isReductionNeeded(Header eventHeader, Event event, Connection connection) throws SQLException {
        try {
            int alarmId = new JdbcTemplate(getDataSource()).queryForInt(EventdConstants.SQL_DB_ALARM_REDUCTION_QUERY, new Object[] { event.getAlarmData().getReductionKey() });

            if (log().isDebugEnabled()) {
                log().debug("Persist.isReductionNeeded: yes for reductionKey: " + event.getAlarmData().getReductionKey());
            }
            
            return alarmId;
        } catch (EmptyResultDataAccessException e) {
            if (log().isDebugEnabled()) {
                log().debug("Persist.isReductionNeeded: no for reductionKey: " + event.getAlarmData().getReductionKey());
            }
                
            return -1;
        }
    }

    private void cleanPreviousEvents(int alarmId, int eventId, Connection connection) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = connection.prepareStatement("DELETE FROM events WHERE alarmId = ? AND eventId != ?");
            d.watch(stmt);
            stmt.setInt(1, alarmId);
            stmt.setInt(2, eventId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log().error("cleanPreviousEvents: Couldn't remove old events: " + e, e);
        } finally {
            d.cleanUp();
        }
    }
}
