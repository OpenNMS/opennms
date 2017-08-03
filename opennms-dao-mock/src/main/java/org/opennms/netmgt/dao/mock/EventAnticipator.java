/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author brozow
 */
public class EventAnticipator implements EventListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventAnticipator.class);
    
    boolean m_discardUnanticipated = false;

    /**
     * This collection contains events that are expected to be received during the
     * given unit test.
     */
    final List<EventWrapper> m_anticipatedEvents = new ArrayList<>();
    
    /**
     * This collection contains events that have been received during the unit test.
     * These events are removed from {@link #m_anticipatedEvents} as they are received.
     */
    final List<Event> m_anticipatedEventsReceived = new ArrayList<>();

    /**
     * This list contains events that were received during the test duration but were not
     * in the {@link #m_anticipatedEvents} list. The {@link #m_unanticipatedEvents} list is 
     * only populated if {@link #m_discardUnanticipated} is set to <code>false</code>.
     */
    final List<Event> m_unanticipatedEvents = new ArrayList<>();

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

    private synchronized void saveUnanticipatedEvent(Event event) {
        if (!m_discardUnanticipated) {
            m_unanticipatedEvents.add(event);
        }
    }

    public synchronized List<Event> getAnticipatedEvents() {
        return Collections.unmodifiableList(
            m_anticipatedEvents.stream().map(EventWrapper::getEvent).collect(Collectors.toList())
        );
    }

    public synchronized List<Event> getAnticipatedEventsReceived() {
        return Collections.unmodifiableList(m_anticipatedEventsReceived);
    }

    /**
     * @return
     */
    public synchronized List<Event> getUnanticipatedEvents() {
        return Collections.unmodifiableList(m_unanticipatedEvents);
    }

    public synchronized void reset() {
        resetAnticipated();
        resetUnanticipated();
    }

    public synchronized void resetUnanticipated() {
        m_unanticipatedEvents.clear();
    }

    public synchronized void resetAnticipated() {
        m_anticipatedEvents.clear();
        m_anticipatedEventsReceived.clear();
    }

    /**
     * @param i
     * @return
     */
    public synchronized Collection<Event> waitForAnticipated(long millis) {
        long waitTime = millis;
        long last = System.currentTimeMillis();
        long now = last;
        while (waitTime > 0) {
            if (m_anticipatedEvents.isEmpty())
                return new ArrayList<Event>(0);
            try {
                wait(waitTime);
            } catch (InterruptedException e) {
            	LOG.error("interrupted while waiting for anticipated events", e);
            }
            now = System.currentTimeMillis();
            waitTime -= (now - last);
            last = now;
        }
        return getAnticipatedEvents();
    }

    /**
     * @param event
     */
    public void eventProcessed(Event event) {
    }

    public synchronized void verifyAnticipated(long wait,
            long sleepMiddle,
            long sleepAfter,
            int anticipatedSize,
            int unanticipatedSize) {

        final StringBuilder problems = new StringBuilder();

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
        
        if (sleepAfter > 0) {
            try {
                Thread.sleep(sleepAfter);
            } catch (InterruptedException e) {
            }
        }

        if (unanticipatedSize >= 0 && getUnanticipatedEvents().size() != unanticipatedSize) {
            problems.append(getUnanticipatedEvents().size() +
                    " unanticipated events received (expected " +
                    unanticipatedSize + "):\n");
            problems.append(listEvents("\t", getUnanticipatedEvents()));
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
        final StringBuilder b = new StringBuilder();

        for (final Event event : events) {
            b.append(prefix);
            b.append(event.getUei() + " / " + event.getNodeid() + " / " + event.getInterface() + " / " + event.getService());
            b.append("\n");
        }

        return b.toString();
    }

    @Override
    public String getName() {
        return "eventAnticipator";
    }

    @Override
    public void onEvent(Event e) {
        eventReceived(e);
    }

}
