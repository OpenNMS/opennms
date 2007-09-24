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
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;

/**
 * The EventHandler is the Runnable that essentially does all the work on an
 * incoming event.
 * 
 * Operations done on an incoming event are -looking up an eventconf entry,
 * expanding event parms, adding the event to the database and sending the event
 * to interested listeners.
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
final class EventHandler implements Runnable {
    /**
     * log of events
     */
    private Log m_eventLog;

    /**
     * SQL string to get the next value from the database sequence
     */
    private String m_getNextEventIdStr;

    private String m_getNextAlarmIdStr;

    /**
     * Constructor for the eventhandler
     * @param connectionFactory 
     * 
     * @param eventLog
     *            events to be processed
     * @param getNextEventId
     *            the sql statement to get next event id from sequence
     */
    public EventHandler(Log eventLog, String getNextEventId, String getNextAlarmIdStr) {
        m_eventLog = eventLog;
        m_getNextEventIdStr = getNextEventId;
        m_getNextAlarmIdStr = getNextAlarmIdStr;
    }

    /**
     * Process the received events. For each event, use the EventExpander to
     * look up matching eventconf entry and load info from that match, expand
     * event parms, add event to database and send event to appropriate
     * listeners.
     */
    public void run() {
        // check to see if the event log is hooked up
        //
        if (m_eventLog == null)
            return;

        // open up a logger
        //
        Category log = ThreadCategory.getInstance(getClass());

        Events events = m_eventLog.getEvents();
        if (events == null || events.getEventCount() <= 0) {
            // no events to process
            return;
        }

        // create an EventWriters
        EventWriter eventWriter = null;
        AlarmWriter alarmWriter = null;
        try {
            try {
                eventWriter = new EventWriter(m_getNextEventIdStr);
                alarmWriter = new AlarmWriter(m_getNextAlarmIdStr);
            } catch (Throwable t) {
                log.warn("Exception creating EventWriter", t);
                log.warn("Event(s) CANNOT be inserted into the database");

                return;
            }

            Enumeration<Event> en = events.enumerateEvent();
            while (en.hasMoreElements()) {
                Event event = en.nextElement();

                if (log.isDebugEnabled()) {
                    // print out the eui, source, and other
                    // important aspects
                    //
                    String uuid = event.getUuid();
                    log.debug("Event {");
                    log.debug("  uuid  = " + (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                    log.debug("  uei   = " + event.getUei());
                    log.debug("  src   = " + event.getSource());
                    log.debug("  iface = " + event.getInterface());
                    log.debug("  time  = " + event.getTime());
                    Parm[] parms = (event.getParms() == null ? null : event.getParms().getParm());
                    if (parms != null) {
                        log.debug("  parms {");
                        for (int x = 0; x < parms.length; x++) {
                            if ((parms[x].getParmName() != null) && (parms[x].getValue().getContent() != null)) {
                                log.debug("    (" + parms[x].getParmName().trim() + ", " + parms[x].getValue().getContent().trim() + ")");
                            }
                        }
                        log.debug("  }");
                    }
                    log.debug("}");
                }

                // look up eventconf match and expand event
                EventExpander.expandEvent(event);
                try {
                    // add to database
                    eventWriter.persistEvent(m_eventLog.getHeader(), event);
                    // send event to interested listeners
                    EventIpcManagerFactory.getIpcManager().broadcastNow(event);

                    alarmWriter.persistAlarm(m_eventLog.getHeader(), event);
                } catch (SQLException sqle) {
                    log.warn("Unable to add event to database", sqle);
                } catch (Throwable t) {
                    log.warn("Unknown exception processing event", t);
                }
            }
        } finally {
            
            // close database related stuff in the eventwriter
            if (eventWriter != null) eventWriter.close();
            if (alarmWriter != null) alarmWriter.close();
        }
    }

}
