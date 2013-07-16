/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xmlrpcd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.xmlrpcd.SubscribedEvent;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class BroadcastEventProcessor implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);

    /**
     * The location where incoming events of interest are enqueued
     */
    private final FifoQueue<Event> m_eventQ;

    /**
     * The maximam size of the event queue.
     */
    private final int m_maxQSize;

    /**
     * Subscribed events for this listener
     */
    private final HashSet<String> m_events;

    /**
     * Suffix used to create a unique name for event registration
     */
    private final String m_nameSuffix;

    /**
     * Create message selector to set to the subscription
     */
    BroadcastEventProcessor(final String nameSuffix, final FifoQueue<Event> eventQ, final int maxQSize, final List<SubscribedEvent> eventList) {
        m_nameSuffix = nameSuffix;

        // Create the selector for the ueis this service is interested in
        final List<String> ueiList = new ArrayList<String>();

        for (final SubscribedEvent event : eventList) {
            ueiList.add(event.getUei());
        }

        m_eventQ = eventQ;
        m_maxQSize = maxQSize;
        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(this, ueiList);

        m_events = new HashSet<String>();
        m_events.addAll(ueiList);
    }

    /**
     * Unsubscribe from eventd
     */
    public void close() {
        EventIpcManagerFactory.getIpcManager().removeEventListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing. Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each UEI.
     */
    @Override
    public void onEvent(final Event event) {
    	final String eventUei = event.getUei();
        if (eventUei == null) {
            return;
        }

        LOG.debug("Received event: {}", eventUei);

        try {
            if (m_events.contains(eventUei)) {
                if (m_eventQ.size() >= m_maxQSize) {
                    m_eventQ.remove(1000);

                    LOG.debug("Event {} removed from event queue", eventUei);
                }

                m_eventQ.add(event);

                LOG.debug("Event {} added to event queue", eventUei);
            }
        } catch (final InterruptedException ex) {
		LOG.error("Failed to process event", ex);
            return;
        } catch (final FifoQueueException ex) {
            LOG.error("Failed to process event", ex);
            return;
        } catch (final Throwable t) {
            LOG.error("Failed to process event", t);
            return;
        }
    }

    /**
     * Return an id for this event listener
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "Xmlrpcd:BroadcastEventProcessor_" + m_nameSuffix;
    }
}
