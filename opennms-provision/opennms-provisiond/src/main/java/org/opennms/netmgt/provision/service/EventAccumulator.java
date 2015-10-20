/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EventAccumulator implements EventForwarder {
    private static final Logger LOG = LoggerFactory.getLogger(EventAccumulator.class);

    private final EventForwarder m_eventForwarder;
    private final List<Event> m_events = new ArrayList<>();

    public EventAccumulator(final EventForwarder forwarder) {
        m_eventForwarder = forwarder;
    }

    @Override
    public synchronized void sendNow(final Event event) {
        m_events.add(event);
    }

    @Override
    public synchronized void sendNow(final Log log) {
        if (log != null && log.getEvents() != null && log.getEvents().getEventCount() > 0) {
            for (final Event e : log.getEvents().getEventCollection()) {
                m_events.add(e);
            }
        }
    }
    
    public synchronized void flush() {
        LOG.debug("flush(): sending {} events: {}", m_events.size(), m_events);
        for (final Event e : m_events) {
            m_eventForwarder.sendNow(e);
        }
        m_events.clear();
    }
}