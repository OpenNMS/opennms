//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 09: Eliminate warnings, use Java 5 generics and loops. - dj@opennms.org
// 2007 Aug 24: Add the ability to reset either of the anticipated
//              and unanticipated lists individually. - dj@opennms.org
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

import junit.framework.Assert;

import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author brozow
 */
public class EventAnticipator implements EventListener {
    
    boolean m_discardUnanticipated = false;

    /**
     * This collection contains events that are expected to be received during the
     * given unit test.
     */
    final List<EventWrapper> m_anticipatedEvents = new ArrayList<EventWrapper>();
    
    /**
     * This collection contains events that have been received during the unit test.
     * These events are removed from {@link #m_anticipatedEvents} as they are received.
     */
    final List<Event> m_anticipatedEventsReceived = new ArrayList<Event>();

    /**
     * This list contains events that were received during the test duration but were not
     * in the {@link #m_anticipatedEvents} list. The {@link #m_unanticipatedEvents} list is 
     * only populated if {@link #m_discardUnanticipated} is set to <code>false</code>.
     */
    final List<Event> m_unanticipatedEvents = new ArrayList<Event>();

    /**
     */
    public EventAnticipator() {
    }
    

    /**
     * @return the discardUnanticipated
     */
    public boolean isDiscardUnanticipated() {
        return m_discardUnanticipated;
    }


    /**
     * @param discardUnanticipated the discardUnanticipated to set
     */
    public void setDiscardUnanticipated(boolean discardUnanticipated) {
        m_discardUnanticipated = discardUnanticipated;
    }


    /**
     * @param event
     * 
     */
    public void anticipateEvent(Event event) {
        anticipateEvent(event, false);
    }
    
    public synchronized void anticipateEvent(Event event, boolean checkUnanticipatedList) {
        EventWrapper w = new EventWrapper(event);
        if (checkUnanticipatedList) {
            for(Iterator<Event> it = m_unanticipatedEvents.iterator(); it.hasNext(); ) {
                Event unE = it.next();
                EventWrapper unW = new EventWrapper(unE);
                if (unW.equals(w)) {
                    it.remove();
                    notifyAll();
                    return;
                }
            }
        } 
        m_anticipatedEvents.add(w);
        notifyAll();
    }

    /**
     * @param event
     */
    public synchronized void eventReceived(Event event) {
        EventWrapper w = new EventWrapper(event);
        if (m_anticipatedEvents.contains(w)) {
            m_anticipatedEvents.remove(w);
            m_anticipatedEventsReceived.add(event);
            notifyAll();
        } else {
            saveUnanticipatedEvent(event);
        }
    }

    private void saveUnanticipatedEvent(Event event) {
        if (!m_discardUnanticipated) {
            m_unanticipatedEvents.add(event);
        }
    }

    public synchronized Collection<Event> getAnticipatedEvents() {
        List<Event> events = new ArrayList<Event>(m_anticipatedEvents.size());
        for (EventWrapper w : m_anticipatedEvents) {
            events.add(w.getEvent());
        }
        return events;
    }
    
    public synchronized List<Event> getAnticipatedEventsRecieved() {
        return new ArrayList<Event>(m_anticipatedEventsReceived);
    }

    public void reset() {
        resetAnticipated();
        resetUnanticipated();
    }

    public void resetUnanticipated() {
        m_unanticipatedEvents.clear();
    }

    public void resetAnticipated() {
        m_anticipatedEvents.clear();
        m_anticipatedEventsReceived.clear();
    }

    /**
     * @return
     */
    public Collection<Event> unanticipatedEvents() {
        return Collections.unmodifiableCollection(m_unanticipatedEvents);
    }

    /**
     * @param i
     * @return
     */
    public synchronized Collection<Event> waitForAnticipated(long millis) {
        long waitTime = millis;
        long start = System.currentTimeMillis();
        long now = start;
        while (waitTime > 0) {
            if (m_anticipatedEvents.isEmpty())
                return new ArrayList<Event>(0);
            try {
                wait(waitTime);
            } catch (InterruptedException e) {
            }
            now = System.currentTimeMillis();
            waitTime -= (now - start);
        }
        return getAnticipatedEvents();
    }

    /**
     * @param event
     */
    public void eventProcessed(Event event) {
    }

    public void verifyAnticipated(long wait,
            long sleepMiddle,
            long sleepAfter,
            int anticipatedSize,
            int unanticipatedSize) {

        StringBuffer problems = new StringBuffer();

        Collection<Event> missingEvents = waitForAnticipated(wait);

        if (sleepMiddle > 0) {
            try {
                Thread.sleep(sleepMiddle);
            } catch (InterruptedException e) {
            }
        }

        if (anticipatedSize >= 0 && missingEvents.size() != anticipatedSize) {
            problems.append(missingEvents.size() +
                    " expected events still outstanding (expected " +
                    anticipatedSize + "):\n");
            problems.append(listEvents("\t", missingEvents));
        }
        if (unanticipatedSize >= 0 && unanticipatedEvents().size() != unanticipatedSize) {
            problems.append(unanticipatedEvents().size() +
                    " unanticipated events received (expected " +
                    unanticipatedSize + "):\n");
            problems.append(listEvents("\t", unanticipatedEvents()));
        }

        if (problems.length() > 0) {
            problems.deleteCharAt(problems.length() - 1);
            Assert.fail(problems.toString());
        }
    }
    
    public void verifyAnticipated() {
        verifyAnticipated(0, 0, 0, 0, 0);
    }

    private static String listEvents(String prefix, Collection<Event> events) {
        StringBuffer b = new StringBuffer();

        for (Event event : events) {
            b.append(prefix);
            b.append(event.getUei() + " / " + event.getNodeid() + " / " + event.getInterface() + " / " + event.getService());
            b.append("\n");
        }

        return b.toString();
    }

    public String getName() {
        return "eventAnticipator";
    }

    public void onEvent(Event e) {
        eventReceived(e);
    }

}
