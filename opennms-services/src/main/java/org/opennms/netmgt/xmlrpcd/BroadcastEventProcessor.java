/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 13, 2004
 *
 * 2008 Jun 14: Java 5 generics, code formatting, and log(). - dj@opennms.org
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.xmlrpcd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
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
    BroadcastEventProcessor(String nameSuffix, FifoQueue<Event> eventQ, int maxQSize, List<SubscribedEvent> eventList) {
        m_nameSuffix = nameSuffix;

        // Create the selector for the ueis this service is interested in
        List<String> ueiList = new ArrayList<String>();

        for (SubscribedEvent event : eventList) {
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
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing. Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each UEI.
     * 
     * @param event
     *            The event
     * 
     */
    public void onEvent(Event event) {
        String eventUei = event.getUei();
        if (eventUei == null) {
            return;
        }

        if (log().isDebugEnabled()) {
            log().debug("Received event: " + eventUei);
        }

        try {
            if (m_events.contains(eventUei)) {
                if (m_eventQ.size() >= m_maxQSize) {
                    m_eventQ.remove(1000);

                    if (log().isDebugEnabled()) {
                        log().debug("Event " + eventUei + " removed from event queue");
                    }
                }

                m_eventQ.add(event);

                if (log().isDebugEnabled())
                    log().debug("Event " + eventUei + " added to event queue");
            }
        } catch (InterruptedException ex) {
            log().error("Failed to process event", ex);
            return;
        } catch (FifoQueueException ex) {
            log().error("Failed to process event", ex);
            return;
        } catch (Throwable t) {
            log().error("Failed to process event", t);
            return;
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * Return an id for this event listener
     */
    public String getName() {
        return "Xmlrpcd:BroadcastEventProcessor_" + m_nameSuffix;
    }
}
