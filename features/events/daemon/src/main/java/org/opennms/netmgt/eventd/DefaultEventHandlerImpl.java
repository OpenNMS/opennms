/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.eventd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.events.api.EventHandler;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * The EventHandler builds Runnables that essentially do all the work on an
 * incoming event.
 *
 * Operations done on an incoming event are handled by the List of injected
 * EventProcessors, in the order in which they are given in the list.  If any
 * of them throw an exception, further processing of that event Log is stopped.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public final class DefaultEventHandlerImpl implements InitializingBean, EventHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventHandlerImpl.class);
    
    private List<EventProcessor> m_eventProcessors;

    private boolean m_logEventSummaries;

    private final Timer processTimer;

    private final Histogram logSizes;

    private NodeDao m_nodeDao;

    /**
     * <p>Constructor for DefaultEventHandlerImpl.</p>
     */
    public DefaultEventHandlerImpl(MetricRegistry registry) {
        processTimer = Objects.requireNonNull(registry).timer("eventlogs.process");
        logSizes = registry.histogram("eventlogs.sizes");
    }

    @Override
    public EventHandlerRunnable createRunnable(Log eventLog) {
        return new EventHandlerRunnable(eventLog, false);
    }

    @Override
    public EventHandlerRunnable createRunnable(Log eventLog, boolean synchronous) {
        return new EventHandlerRunnable(eventLog, synchronous);
    }

    private class EventHandlerRunnable implements Runnable {
        /**
         * log of events
         */
        private final Log m_eventLog;

        private final boolean m_synchronous;

        public EventHandlerRunnable(Log eventLog, boolean synchronous) {
            Assert.notNull(eventLog, "eventLog argument must not be null");
            
            m_eventLog = eventLog;
            m_synchronous = synchronous;
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
                if (event.getNodeid() == 0) {
                    final Parm foreignSource = event.getParm("_foreignSource");
                    if (foreignSource != null && foreignSource.getValue() != null) {
                        final Parm foreignId = event.getParm("_foreignId");
                        if (foreignId != null && foreignId.getValue() != null) {
                            final OnmsNode node = getNodeDao().findByForeignId(foreignSource.getValue().getContent(), foreignId.getValue().getContent());
                            if (node != null) {
                                event.setNodeid(node.getId().longValue());
                            } else {
                                LOG.warn("Can't find node associated with foreignSource {} and foreignId {}", foreignSource, foreignId);
                            }
                        }
                    }
                }

                if (LOG.isInfoEnabled() && getLogEventSummaries()) {
                    LOG.info("Received event: UEI={}, src={}, iface={}, svc={}, time={}, parms={}", event.getUei(), event.getSource(), event.getInterface(), event.getService(), event.getTime(), getPrettyParms(event));
                }

                if (LOG.isDebugEnabled()) {
                    // Log the uei, source, and other important aspects
                    final String uuid = event.getUuid();
                    LOG.debug("Event {");
                    LOG.debug("  uuid  = {}", (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                    LOG.debug("  uei   = {}", event.getUei());
                    LOG.debug("  src   = {}", event.getSource());
                    LOG.debug("  iface = {}", event.getInterface());
                    LOG.debug("  svc   = {}", event.getService());
                    LOG.debug("  time  = {}", event.getTime());
                    // NMS-8413: I'm seeing a ConcurrentModificationException in the logs here,
                    // copy the parm collection to avoid this
                    List<Parm> parms = new ArrayList<>(event.getParmCollection());
                    if (parms.size() > 0) {
                        LOG.debug("  parms {");
                        for (final Parm parm : parms) {
                            if ((parm.getParmName() != null) && (parm.getValue().getContent() != null)) {
                                LOG.debug("    ({}, {})", parm.getParmName().trim(), parm.getValue().getContent().trim());
                            }
                        }
                        LOG.debug("  }");
                    }
                    LOG.debug("}");
                }
            }

            try (Timer.Context context = processTimer.time()) {
                for (final EventProcessor eventProcessor : m_eventProcessors) {
                    try {
                        eventProcessor.process(m_eventLog, m_synchronous);
                        logSizes.update(events.getEventCount());
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

    private static List<String> getPrettyParms(final Event event) {
        final List<String> parms = new ArrayList<>();
        for (final Parm p : event.getParmCollection()) {
            parms.add(p.getParmName() + "=" + p.getValue().getContent());
        }
        return parms;
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
    
    public boolean getLogEventSummaries() {
        return m_logEventSummaries;
    }
    
    public void setLogEventSummaries(final boolean logEventSummaries) {
        m_logEventSummaries = logEventSummaries;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
}
