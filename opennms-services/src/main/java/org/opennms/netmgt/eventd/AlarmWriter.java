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
// Tab Size = 8
//

package org.opennms.netmgt.eventd;

import java.sql.SQLException;

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

    public String getGetNextAlarmIdStr() {
        return m_getNextAlarmIdStr;
    }

    public void setGetNextAlarmIdStr(String getNextAlarmIdStr) {
        m_getNextAlarmIdStr = getNextAlarmIdStr;
    }
}
