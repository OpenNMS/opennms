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
// 2008 Jan 26: Use setter injection for EventWriter and AlarmWriter.
//              Add dependency injection for DataSource and
//              EventdServiceManager - dj@opennms.org
// 2008 Jan 06: Format code a bit, dependency inject EventExpander,
//              and create log() method. - dj@opennms.org
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

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

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
final class EventHandler implements Runnable, InitializingBean {
    /**
     * log of events
     */
    private Log m_eventLog;

    /**
     * SQL string to get the next value from the database sequence
     */
    private String m_getNextEventIdStr;

    private String m_getNextAlarmIdStr;

    private EventExpander m_eventExpander;

    private EventdServiceManager m_eventdServiceManager;

    private DataSource m_dataSource;

    /**
     * Constructor for the eventhandler
     * @param eventLog
     *            events to be processed
     * @param getNextEventId
     *            the sql statement to get next event id from sequence
     * @param eventExpander TODO
     * @param dataSource TODO
     * @param connectionFactory 
     */
    public EventHandler(Log eventLog, String getNextEventId, String getNextAlarmIdStr, EventExpander eventExpander, EventdServiceManager eventdServiceManager, DataSource dataSource) {
        m_eventLog = eventLog;
        m_getNextEventIdStr = getNextEventId;
        m_getNextAlarmIdStr = getNextAlarmIdStr;
        m_eventExpander = eventExpander;
        m_eventdServiceManager = eventdServiceManager;
        m_dataSource = dataSource;
        
        afterPropertiesSet();
    }

    /**
     * Process the received events. For each event, use the EventExpander to
     * look up matching eventconf entry and load info from that match, expand
     * event parms, add event to database and send event to appropriate
     * listeners.
     */
    public void run() {
        // check to see if the event log is hooked up
        if (m_eventLog == null) {
            return;
        }

        Events events = m_eventLog.getEvents();
        if (events == null || events.getEventCount() <= 0) {
            // no events to process
            return;
        }

        // create an EventWriters
        EventWriter eventWriter = null;
        try {
            AlarmWriter alarmWriter = null;
            try {
                try {
                    eventWriter = new EventWriter();
                    eventWriter.setDataSource(m_dataSource);
                    eventWriter.setEventdServiceManager(m_eventdServiceManager);
                    eventWriter.setGetNextEventIdStr(m_getNextEventIdStr);
                    eventWriter.afterPropertiesSet();
                    
                    alarmWriter = new AlarmWriter();
                    alarmWriter.setDataSource(m_dataSource);
                    alarmWriter.setEventdServiceManager(m_eventdServiceManager);
                    alarmWriter.setGetNextAlarmIdStr(m_getNextAlarmIdStr);
                    alarmWriter.afterPropertiesSet();
                } catch (Throwable t) {
                    log().warn("Exception creating EventWriter", t);
                    log().warn("Event(s) CANNOT be inserted into the database");

                    return;
                }

                Enumeration<Event> en = events.enumerateEvent();
                while (en.hasMoreElements()) {
                    Event event = en.nextElement();

                    if (log().isDebugEnabled()) {
                        // print out the eui, source, and other
                        // important aspects
                        //
                        String uuid = event.getUuid();
                        log().debug("Event {");
                        log().debug("  uuid  = " + (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                        log().debug("  uei   = " + event.getUei());
                        log().debug("  src   = " + event.getSource());
                        log().debug("  iface = " + event.getInterface());
                        log().debug("  time  = " + event.getTime());
                        Parm[] parms = (event.getParms() == null ? null : event.getParms().getParm());
                        if (parms != null) {
                            log().debug("  parms {");
                            for (int x = 0; x < parms.length; x++) {
                                if ((parms[x].getParmName() != null) && (parms[x].getValue().getContent() != null)) {
                                    log().debug("    (" + parms[x].getParmName().trim() + ", " + parms[x].getValue().getContent().trim() + ")");
                                }
                            }
                            log().debug("  }");
                        }
                        log().debug("}");
                    }

                    // look up eventconf match and expand event
                    m_eventExpander.expandEvent(event);
                    try {
                        // add to database
                        eventWriter.persistEvent(m_eventLog.getHeader(), event);
                        // send event to interested listeners
                        EventIpcManagerFactory.getIpcManager().broadcastNow(event);

                        alarmWriter.persistAlarm(m_eventLog.getHeader(), event);
                    } catch (SQLException sqle) {
                        log().warn("Unable to add event to database", sqle);
                    } catch (Throwable t) {
                        log().warn("Unknown exception processing event", t);
                    }
                }
            } finally {
                if (alarmWriter != null) alarmWriter.close();
            }
        } finally {
            
            // close database related stuff in the eventwriter
            if (eventWriter != null) eventWriter.close();
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void afterPropertiesSet() throws IllegalStateException {
        Assert.state(m_eventLog != null, "property eventLog must be set");
        Assert.state(m_getNextEventIdStr != null, "property getNextEventId must be set");
        Assert.state(m_getNextAlarmIdStr != null, "property getNextAlarmIdStr must be set");
        Assert.state(m_eventExpander != null, "property eventExpander must be set");
        Assert.state(m_eventdServiceManager != null, "property eventdServiceManager must be set");
        Assert.state(m_dataSource != null, "property dataSource must be set");
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }
}
