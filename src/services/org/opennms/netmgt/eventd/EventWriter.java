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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;

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
final class EventWriter extends PersistEvents {
    /**
     * Constructor
     */
    public EventWriter(String getNextEventIdStr) throws SQLException {
        super(getNextEventIdStr);
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
            Category log = ThreadCategory.getInstance(EventWriter.class);

            // Check value of <logmsg> attribute 'dest', if set to
            // "donotpersist" then simply return, the uei is not to be
            // persisted to the database
            String logdest = event.getLogmsg().getDest();
            if (logdest.equals("donotpersist")) {
                log.debug("EventWriter: uei '" + event.getUei() + "' marked as 'doNotPersist'.");
                return;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("EventWriter dbRun for : " + event.getUei() + " nodeid: " + event.getNodeid() + " ipaddr: " + event.getInterface() + " serviceid: " + event.getService());
                }
            }

            try {
                add(eventHeader, event);

                // commit
                m_dbConn.commit();
            } catch (SQLException e) {
                log.warn("Error inserting event into the datastore", e);
                try {
                    m_dbConn.rollback();
                } catch (Exception e2) {
                    log.warn("Rollback of transaction failed!", e2);
                }

                throw e;
            }

            if (log.isDebugEnabled())
                log.debug("EventWriter finished for : " + event.getUei());
        }
    }
}
