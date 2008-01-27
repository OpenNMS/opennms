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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

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
final class EventWriter extends Persist implements InitializingBean {
    private String m_getNextEventIdStr;

    public EventWriter() {
        super();
    }
    
    @Override
    public void afterPropertiesSet() throws SQLException {
        super.afterPropertiesSet();
        
        Assert.state(m_getNextEventIdStr != null, "property getNextEventIdStr must be set");
        
        // prepare the SQL statement
        m_getHostNameStmt = m_dsConn.prepareStatement(EventdConstants.SQL_DB_HOSTIP_TO_HOSTNAME);
        m_getNextIdStmt = m_dsConn.prepareStatement(getGetNextEventIdStr());
        m_insStmt = m_dsConn.prepareStatement(EventdConstants.SQL_DB_INS_EVENT);

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
    public void persistEvent(Header eventHeader, Event event) throws SQLException {
        if (event != null) {
            // Check value of <logmsg> attribute 'dest', if set to
            // "donotpersist" then simply return, the uei is not to be
            // persisted to the database
            String logdest = event.getLogmsg().getDest();
            if (logdest.equals("donotpersist")) {
                log().debug("EventWriter: uei '" + event.getUei() + "' marked as 'doNotPersist'.");
                return;
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("EventWriter dbRun for : " + event.getUei() + " nodeid: " + event.getNodeid() + " ipaddr: " + event.getInterface() + " serviceid: " + event.getService());
                }
            }

            try {
                insertEvent(eventHeader, event);

                // commit
                m_dsConn.commit();
            } catch (SQLException e) {
                log().warn("Error inserting event into the datastore", e);
                try {
                    m_dsConn.rollback();
                } catch (Exception e2) {
                    log().warn("Rollback of transaction failed!", e2);
                }

                throw e;
            }

            if (log().isDebugEnabled()) {
                log().debug("EventWriter finished for : " + event.getUei());
            }
        }
    }

    public String getGetNextEventIdStr() {
        return m_getNextEventIdStr;
    }

    public void setGetNextEventIdStr(String getNextEventIdStr) {
        m_getNextEventIdStr = getNextEventIdStr;
    }
}
