//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.xml.event.Event;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class EventAnticipator {

    /**
     * Need this class because Event doesn't properly implement hashCode
     */
    static class EventWrapper {
        Event m_event;

        EventWrapper(Event event) {
            m_event = event;
        }

        public boolean equals(Object o) {
            EventWrapper w = (EventWrapper) o;
            return MockUtil.eventsMatch(m_event, w.m_event);
        }

        public Event getEvent() {
            return m_event;
        }

        public int hashCode() {
            return m_event.getUei().hashCode();
        }
    }

    List m_anticipatedEvents = new ArrayList();

    List m_unanticipatedEvents = new ArrayList();

    /**
     */
    public EventAnticipator() {
    }

    /**
     * @param service
     */
    public void add(MockElement element) {
        // TODO Auto-generated method stub

    }

    /**
     * @param event
     * 
     */
    public void anticipateEvent(Event event) {
        m_anticipatedEvents.add(new EventWrapper(event));
    }

    /**
     * @param event
     */
    public synchronized void eventReceived(Event event) {
        EventWrapper w = new EventWrapper(event);
        if (m_anticipatedEvents.contains(w)) {
            m_anticipatedEvents.remove(w);
            notifyAll();
        } else {
            m_unanticipatedEvents.add(event);
        }
    }

    public synchronized Collection getAnticipatedEvents() {
        List events = new ArrayList(m_anticipatedEvents.size());
        Iterator it = m_anticipatedEvents.iterator();
        while (it.hasNext()) {
            EventWrapper w = (EventWrapper) it.next();
            events.add(w.getEvent());
        }
        return events;
    }

    /**
     * @param service
     */
    public void remove(MockElement element) {
        // TODO Auto-generated method stub

    }

    public void reset() {
        m_anticipatedEvents = new ArrayList();
        m_unanticipatedEvents = new ArrayList();
    }

    /**
     * @return
     */
    public Collection unanticipatedEvents() {
        return Collections.unmodifiableCollection(m_unanticipatedEvents);
    }

    /**
     * @param i
     * @return
     */
    public synchronized Collection waitForAnticipated(long millis) {
        long waitTime = millis;
        long start = System.currentTimeMillis();
        long now = start;
        while (waitTime > 0) {
            if (m_anticipatedEvents.isEmpty())
                return Collections.EMPTY_LIST;
            try {
                wait(waitTime);
            } catch (InterruptedException e) {
            }
            now = System.currentTimeMillis();
            waitTime -= (now - start);
        }
        return getAnticipatedEvents();
    }

}
