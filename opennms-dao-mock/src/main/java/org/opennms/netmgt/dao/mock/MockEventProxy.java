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

package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class MockEventProxy implements EventProxy {
    private static final Logger LOG = LoggerFactory.getLogger(MockEventProxy.class);
    private List<Event> m_events = new ArrayList<Event>();

    @Override
    public void send(final Event event) throws EventProxyException {
        LOG.debug("Received event: {}", event);
        m_events.add(event);
    }

    @Override
    public void send(final Log eventLog) throws EventProxyException {
        if (eventLog.getEvents() != null) {
            final List<Event> events = eventLog.getEvents().getEventCollection();
            LOG.debug("Received events: {}", events);
            m_events.addAll(events);
        }
    }

    public void resetEvents() {
        m_events.clear();
    }

    public List<Event> getEvents() {
        return m_events;
    }
}
