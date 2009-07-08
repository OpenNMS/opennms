/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class ThresholdingEventProxy implements EventProxy {

    private List<Event> m_events;
    
    public ThresholdingEventProxy() {
        m_events = new LinkedList<Event>();
    }
    
    public void send(Event event) throws EventProxyException {
        add(event);
    }

    public void send(Log eventLog) throws EventProxyException {
        for (Event e : eventLog.getEvents().getEventCollection()) {
            add(e);
        }
    }
    
    public void add(Event event) {
        m_events.add(event);
    }

    public void add(List<Event> events) {
        m_events.addAll(events);
    }

    public void removeAllEvents() {
        m_events.clear();
    }
    
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
            } catch (Exception e) {
                log().info("sendAllEvents: Failed sending threshold events: " + e, e);
            }
            removeAllEvents();
        }
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
