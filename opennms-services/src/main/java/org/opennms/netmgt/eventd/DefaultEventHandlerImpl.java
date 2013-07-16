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

package org.opennms.netmgt.eventd;

import java.util.List;

import org.opennms.netmgt.model.events.EventProcessor;
import org.opennms.netmgt.model.events.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
public final class DefaultEventHandlerImpl implements InitializingBean, EventHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventHandlerImpl.class);
    
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
    @Override
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
        @Override
        public void run() {
            Events events = m_eventLog.getEvents();
            if (events == null || events.getEventCount() <= 0) {
                // no events to process
                return;
            }

            for (final Event event : events.getEventCollection()) {
                if (LOG.isDebugEnabled()) {
                    // Log the uei, source, and other important aspects
                    final String uuid = event.getUuid();
                    LOG.debug("Event {");
                    LOG.debug("  uuid  = {}", (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                    LOG.debug("  uei   = {}", event.getUei());
                    LOG.debug("  src   = {}", event.getSource());
                    LOG.debug("  iface = {}", event.getInterface());
                    LOG.debug("  time  = {}", event.getTime());
                    if (event.getParmCollection().size() > 0) {
                        LOG.debug("  parms {");
                        for (final Parm parm : event.getParmCollection()) {
                            if ((parm.getParmName() != null) && (parm.getValue().getContent() != null)) {
                                LOG.debug("    ({}, {})", parm.getParmName().trim(), parm.getValue().getContent().trim());
                            }
                        }
                        LOG.debug("  }");
                    }
                    LOG.debug("}");
                }

                for (final EventProcessor eventProcessor : m_eventProcessors) {
                    try {
                        eventProcessor.process(m_eventLog.getHeader(), event);
                    } catch (EventProcessorException e) {
                        LOG.warn("Unable to process event using processor {}; not processing with any later processors.", eventProcessor, e);
                        break;
                    } catch (Throwable t) {
                        LOG.warn("Unknown exception processing event with processor {}; not processing with any later processors.", eventProcessor, t);
                        break;
                    }
                }
            }
        }

    }

    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
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
