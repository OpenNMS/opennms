/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ThresholdingEventProxy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdingEventProxy implements EventProxy {
    
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingEventProxy.class);

    private List<Event> m_events;
    
    /**
     * <p>Constructor for ThresholdingEventProxy.</p>
     */
    public ThresholdingEventProxy() {
        m_events = new LinkedList<>();
    }
    
    /** {@inheritDoc} */
    @Override
    public void send(Event event) throws EventProxyException {
        add(event);
    }

    /**
     * <p>send</p>
     *
     * @param eventLog a {@link org.opennms.netmgt.xml.event.Log} object.
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    @Override
    public void send(Log eventLog) throws EventProxyException {
        for (Event e : eventLog.getEvents().getEventCollection()) {
            add(e);
        }
    }
    
    /**
     * <p>add</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void add(Event event) {
        m_events.add(event);
    }

    /**
     * <p>add</p>
     *
     * @param events a {@link java.util.List} object.
     */
    public void add(List<Event> events) {
        m_events.addAll(events);
    }

    /**
     * <p>removeAllEvents</p>
     */
    public void removeAllEvents() {
        m_events.clear();
    }
    
    /**
     * <p>sendAllEvents</p>
     */
    public void sendAllEvents() {
        if (m_events.size() > 0) {
            try {
                Log log = new Log();
                Events events = new Events();
                for (Event e : m_events) {
                    events.addEvent(e);
                }
                log.setEvents(events);
                EventIpcManagerFactory.getIpcManager().sendNow(log);
            } catch (Throwable e) {
                LOG.info("sendAllEvents: Failed sending threshold events", e);
            }
            removeAllEvents();
        }
    }

}
