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
// 2008 Feb 02: Rename EventHandler to DefaultEventHandlerImpl. - dj@opennms.org
// 2008 Jan 27: Refactor event processing to iterate over an injected
//              list of EventProcessors instead of doing specific work
//              with EventExpander, EventWriter, EventIpcManager, and
//              AlarmWriter. - dj@opennms.org
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
package org.opennms.netmgt.eventd;

import java.sql.SQLException;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.processor.EventProcessor;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The EventHandler builds Runnables that essentially do all the work on an
 * incoming event.
 *
 * Operations done on an incoming event are handled by the List of injected
 * EventProcessors, in the order in which they are given in the list.  If any
 * of them throw an exception, futher processing of that event Log is stopped.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public final class DefaultEventHandlerImpl implements InitializingBean, EventHandler {
    private List<EventProcessor> m_eventProcessors;

    /**
     * <p>Constructor for DefaultEventHandlerImpl.</p>
     */
    public DefaultEventHandlerImpl() {
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventHandler#createRunnable(org.opennms.netmgt.xml.event.Log)
     */
    /** {@inheritDoc} */
    public EventHandlerRunnable createRunnable(Log eventLog) {
        return new EventHandlerRunnable(eventLog);
    }

    public class EventHandlerRunnable implements Runnable {
        /**
         * log of events
         */
        private Log m_eventLog;

        public EventHandlerRunnable(Log eventLog) {
            Assert.notNull(eventLog, "eventLog argument must not be null");
            
            m_eventLog = eventLog;
        }
        
        /**
         * Process the received events. For each event, use the EventExpander to
         * look up matching eventconf entry and load info from that match, expand
         * event parms, add event to database and send event to appropriate
         * listeners.
         */
        public void run() {
            Events events = m_eventLog.getEvents();
            if (events == null || events.getEventCount() <= 0) {
                // no events to process
                return;
            }

            for (Event event : events.getEventCollection()) {
                if (log().isDebugEnabled()) {
                    // Log the eui, source, and other important aspects
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
                        for (Parm parm : parms) {
                            if ((parm.getParmName() != null) && (parm.getValue().getContent() != null)) {
                                log().debug("    (" + parm.getParmName().trim() + ", " + parm.getValue().getContent().trim() + ")");
                            }
                        }
                        log().debug("  }");
                    }
                    log().debug("}");
                }

                for (EventProcessor eventProcessor : m_eventProcessors) {
                    try {
                        eventProcessor.process(m_eventLog.getHeader(), event);
                    } catch (SQLException e) {
                        log().warn("Unable to process event using processor " + eventProcessor + "; not processing with any later processors.  Exception: " + e, e);
                        break;
                    } catch (Throwable t) {
                        log().warn("Unknown exception processing event with processor " + eventProcessor + "; not processing with any later processors.  Exception: " + t, t);
                        break;
                    }
                }
            }
        }

    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    public void afterPropertiesSet() throws IllegalStateException {
        Assert.state(m_eventProcessors != null, "property eventPersisters must be set");
    }

    /**
     * <p>getEventProcessors</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<EventProcessor> getEventProcessors() {
        return m_eventProcessors;
    }

    /**
     * <p>setEventProcessors</p>
     *
     * @param eventProcessors a {@link java.util.List} object.
     */
    public void setEventProcessors(List<EventProcessor> eventProcessors) {
        m_eventProcessors = eventProcessors;
    }
}
