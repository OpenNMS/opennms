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
package org.opennms.netmgt.eventd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.opennms.netmgt.eventd.db.Constants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
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
final class AlarmWriter extends Persist {
    private String m_getNextAlarmIdStr;

    /**
     * Constructor
     * @param connectionFactory 
     */
    public AlarmWriter() {
        super();
    }
    
    @Override
    public void afterPropertiesSet() throws SQLException {
        super.afterPropertiesSet();
        
        Assert.state(m_getNextAlarmIdStr != null, "property getNextAlarmIdStr must be set");

        // prepare the SQL statement
        m_getHostNameStmt = m_dsConn.prepareStatement(EventdConstants.SQL_DB_HOSTIP_TO_HOSTNAME);
        m_getNextIdStmt = m_dsConn.prepareStatement(getGetNextAlarmIdStr());
        m_insStmt = m_dsConn.prepareStatement(EventdConstants.SQL_DB_ALARM_INS_EVENT);
        m_reductionQuery = m_dsConn.prepareStatement(EventdConstants.SQL_DB_ALARM_REDUCTION_QUERY);
        m_upDateStmt = m_dsConn.prepareStatement(EventdConstants.SQL_DB_ALARM_UPDATE_EVENT);
        m_updateEventStmt = m_dsConn.prepareStatement(EventdConstants.SQL_DB_UPDATE_EVENT_WITH_ALARM_ID);

        // XXX any reason why we can't do this in our super's afterPropertiesSet()?
        // set the database for rollback support
        try {
            m_dsConn.setAutoCommit(false);
        } catch (SQLException e) {
            log().warn("Unable to set auto commit mode: " + e, e);
        }
    }
    
    public void close() {
        try {
            m_getHostNameStmt.close();
            m_getNextIdStmt.close();
            m_insStmt.close();
            m_reductionQuery.close();
            m_upDateStmt.close();
            m_updateEventStmt.close();
        } catch (SQLException e) {
            log().warn("SQLException while closing prepared statements: " + e, e);
        } finally {
            super.close();
        }
    }


    /**
     * The method that inserts the event into the database
     * 
     * @param eventHeader
     *            the event header
     * @param event
     *            the actual event to be inserted
     */
    public void persistAlarm(Header eventHeader, Event event) throws SQLException {
        if (event != null) {
            // Check value of <logmsg> attribute 'dest', if set to
            // "donotpersist" then simply return, the uei is not to be
            // persisted to the database
            String logdest = event.getLogmsg().getDest();
            if (logdest.equals("donotpersist") || event.getAlarmData() == null) {
                log().debug("AlarmWriter: uei '" + event.getUei() + "' marked as 'doNotPersist' or reductionKey is null.");
                return;
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("AlarmWriter dbRun for : " + event.getUei() + " nodeid: " + event.getNodeid() + " ipaddr: " + event.getInterface() + " serviceid: " + event.getService());
                }
            }

            /*
             * Try twice incase the transaction fails.  This could happen if 2 or more threads query the db
             * at the same time and determine that insert needs to happen.  One insert will complete the other
             * will fail.  The next time through the loop, the alarm will be reduced with an update. 
             */
            int attempt = 1;
            boolean notUpdated = true;
            while (attempt <= 2 && notUpdated) {
                try {
                    insertOrUpdateAlarm(eventHeader, event);
                    m_dsConn.commit();
                    notUpdated = false;
                } catch (SQLException e) {
                    try {
                        m_dsConn.rollback();
                        m_dsConn.setAutoCommit(false);
                    } catch (Exception e2) {
                        log().warn("Rollback of transaction failed!", e2);
                    }
                    if (attempt > 1) {
                        log().warn("Error in attempt: "+attempt+" inserting alarm into the datastore", e);
                        throw e;
                    } else {
                        log().info("Retrying insertOrUpdate statement after first attempt: "+ e.getMessage());
                    }
                }
                attempt++;
            }

            if (log().isDebugEnabled()) {
                log().debug("AlarmWriter finished for : " + event.getUei());
            }
        }
    }

    public void insertOrUpdateAlarm(Header eventHeader, Event event) throws SQLException {
        int alarmId = isReductionNeeded(eventHeader, event);
        if (alarmId != -1) {
            if (log().isDebugEnabled()) {
                log().debug("Reducing event for: " + event.getDbid() + ": " + event.getUei());
            }
            updateAlarm(eventHeader, event, alarmId);
            
            // This removes all previous events that have been reduced.
            if (log().isDebugEnabled()) {
                log().debug("insertOrUpdate: auto-clean is: " + event.getAlarmData().getAutoClean());
            }
            if (event.getAlarmData().getAutoClean() == true) {
                log().debug("insertOrUpdate: deleting previous events");
                cleanPreviousEvents(alarmId, event.getDbid());
            }
            
        } else {
            if (log().isDebugEnabled()) {
                log().debug("Not reducing event for: " + event.getDbid() + ": " + event.getUei());
            }
            insertAlarm(eventHeader, event);
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
    protected void insertAlarm(Header eventHeader, Event event) throws SQLException {
        int alarmID = -1;
        alarmID = getNextId();
        if (log().isDebugEnabled()) {
            log().debug("AlarmWriter: DBID: " + alarmID);
        }
    
        //Column 1, alarmId
        m_insStmt.setInt(1, alarmID);
        
        //Column 2, eventUie
        m_insStmt.setString(2, Constants.format(event.getUei(), EVENT_UEI_FIELD_SIZE));
        
        //Column 3, dpName
        m_insStmt.setString(3, (eventHeader != null) ? Constants.format(eventHeader.getDpName(), EVENT_DPNAME_FIELD_SIZE) : "undefined");
        
        // Column 4, nodeID
        int nodeid = (int) event.getNodeid();
        m_insStmt.setObject(4, event.hasNodeid() ? new Integer(nodeid) : null);
        
        // Column 5, ipaddr
        m_insStmt.setString(5, event.getInterface());
        
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
        m_insStmt.setObject(6, (svcId == -1 ? null : new Integer(svcId)));
    
        //Column 7, reductionKey
        m_insStmt.setString(7, event.getAlarmData().getReductionKey());
        
        //Column 8, alarmType
        m_insStmt.setInt(8, event.getAlarmData().getAlarmType());
        
        //Column 9, counter
        m_insStmt.setInt(9, 1);
        
        //Column 10, serverity
        set(m_insStmt, 10, Constants.getSeverity(event.getSeverity()));
    
        //Column 11, lastEventId
        m_insStmt.setInt(11, event.getDbid());
        
        //Column 12, firstEventTime
        //Column 13, lastEventTime
        Timestamp eventTime = getEventTime(event);
        m_insStmt.setTimestamp(12, eventTime);
        m_insStmt.setTimestamp(13, eventTime);
        
        //Column 14, description
        set(m_insStmt, 14, Constants.format(event.getDescr(), EVENT_DESCR_FIELD_SIZE));
    
        //Column 15, logMsg
        if (event.getLogmsg() != null) {
            // set log message
            set(m_insStmt, 15, Constants.format(event.getLogmsg().getContent(), EVENT_LOGMSG_FIELD_SIZE));
        } else {
            m_insStmt.setNull(15, Types.VARCHAR);
        }
    
        //Column 16, operInstruct
        set(m_insStmt, 16, Constants.format(event.getOperinstruct(), EVENT_OPERINSTRUCT_FIELD_SIZE));
        
        //Column 17, tticketId
        //Column 18, tticketState
        if (event.getTticket() != null) {
            set(m_insStmt, 17, Constants.format(event.getTticket().getContent(), EVENT_TTICKET_FIELD_SIZE));
            int ttstate = 0;
            if (event.getTticket().getState().equals("on")) {
                ttstate = 1;
            }
            set(m_insStmt, 18, ttstate);
        } else {
            m_insStmt.setNull(17, Types.VARCHAR);
            m_insStmt.setNull(18, Types.INTEGER);
        }
    
        //Column 19, mouseOverText
        set(m_insStmt, 19, Constants.format(event.getMouseovertext(), EVENT_MOUSEOVERTEXT_FIELD_SIZE));
    
        //Column 20, suppressedUntil
        set(m_insStmt, 20, eventTime);
        
        //Column 21, suppressedUser
        m_insStmt.setString(21, null);
        
        //Column 22, suppressedTime
        set(m_insStmt, 22, eventTime);
        
        //Column 23, alarmAckUser
        m_insStmt.setString(23, null);
        
        //Column 24, alarmAckTime
        m_insStmt.setTimestamp(24, null);
        
        //Column 25, clearUie
        //Column 26, x733AlarmType
        //Column 27, x733ProbableCause
        //Column 28, clearKey
        if (event.getAlarmData() == null) {
            m_insStmt.setString(25, null);
            m_insStmt.setString(26, null);
            m_insStmt.setInt(27, -1);
            m_insStmt.setString(28, null);
        } else {
            m_insStmt.setString(25, Constants.format(event.getAlarmData().getClearUei(), EVENT_UEI_FIELD_SIZE));
            m_insStmt.setString(26, Constants.format(event.getAlarmData().getX733AlarmType(), EVENT_X733_ALARMTYPE_SIZE));
            set(m_insStmt, 27, event.getAlarmData().getX733ProbableCause());
            set(m_insStmt, 28, event.getAlarmData().getClearKey());
        }
        
        if (log().isDebugEnabled()) {
            log().debug("m_insStmt is: " + m_insStmt.toString());
        }
        
        m_insStmt.executeUpdate();
        
        m_updateEventStmt.setInt(1, alarmID);
        m_updateEventStmt.setInt(2, event.getDbid());
        m_updateEventStmt.executeUpdate();
    
        if (log().isDebugEnabled()) {
            log().debug("SUCCESSFULLY added " + event.getUei() + " related  data into the ALARMS table");
        }
    
    }

    public String getGetNextAlarmIdStr() {
        return m_getNextAlarmIdStr;
    }

    public void setGetNextAlarmIdStr(String getNextAlarmIdStr) {
        m_getNextAlarmIdStr = getNextAlarmIdStr;
    }

    protected void updateAlarm(Header eventHeader, Event event, int alarmId) throws SQLException {
        m_upDateStmt.setInt(1, event.getDbid());
        m_upDateStmt.setTimestamp(2, getEventTime(event));
        set(m_upDateStmt, 3, Constants.format(event.getLogmsg().getContent(), EVENT_LOGMSG_FIELD_SIZE));
        m_upDateStmt.setString(4, event.getAlarmData().getReductionKey());
    
        if (log().isDebugEnabled()) {
            log().debug("Persist.updateAlarm: reducing event: " + event.getDbid() +  " into alarm");
        }
        
        m_upDateStmt.executeUpdate();
    
        m_updateEventStmt.setInt(1, alarmId);
        m_updateEventStmt.setInt(2, event.getDbid());
        m_updateEventStmt.executeUpdate();
    }

    protected int isReductionNeeded(Header eventHeader, Event event) throws SQLException {
        if (log().isDebugEnabled()) {
            log().debug("Persist.isReductionNeeded: reductionKey: " + event.getAlarmData().getReductionKey());
        }
    
        m_reductionQuery.setString(1, event.getAlarmData().getReductionKey());
    
        ResultSet rs = null;
        int alarmId;
        try {
            rs = m_reductionQuery.executeQuery();
            alarmId = -1;
            while (rs.next()) {
                alarmId = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            rs.close();
        }
        return alarmId;
    }

    protected void cleanPreviousEvents(int alarmId, int eventId) {
        PreparedStatement stmt = null;
        try {
            stmt = m_dsConn.prepareStatement("DELETE FROM events WHERE alarmId = ? AND eventId != ?");
            stmt.setInt(1, alarmId);
            stmt.setInt(2, eventId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log().error("cleanPreviousEvents: Couldn't remove old events: " + e, e);
        }
    
        try {
            stmt.close();
        } catch (SQLException e) {
            log().error("cleanPreviousEvents: Couldn't close statement: " + e, e);
        }
    }
}
