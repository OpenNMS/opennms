/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
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
    public void onEvent(IEvent e) {
        eventReceived(Event.copyFrom(e));
    }

}
