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
package org.opennms.netmgt.dao;

import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * <p>TransactionAwareEventForwarder class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class TransactionAwareEventForwarder implements EventForwarder, InitializingBean {

    public static class PendingEventsSynchronization extends TransactionSynchronizationAdapter {

        private PendingEventsHolder m_eventsHolder;
        private EventForwarder m_eventForwarder;

        public PendingEventsSynchronization(PendingEventsHolder eventsHolder,
                                            EventForwarder eventForwarder) {
            m_eventsHolder = eventsHolder;
            m_eventForwarder = eventForwarder;
        }

        @Override
        public void afterCommit() {
            if (!m_eventsHolder.hasPendingEvents()) {
                return;
            }

            List<Log> pendingEvents = m_eventsHolder.consumePendingEvents();
            for (Log events : pendingEvents) {
                m_eventForwarder.sendNow(events);
            }
        }

        @Override
        public void afterCompletion(int status) {
            if (TransactionSynchronizationManager.hasResource(m_eventForwarder)) {
                TransactionSynchronizationManager
                        .unbindResource(m_eventForwarder);
            }
        }

    }

    public static class PendingEventsHolder extends ResourceHolderSupport {

        private List<Log> m_pendingEvents = null;

        public PendingEventsHolder(List<Log> pendingEvents) {
            m_pendingEvents = pendingEvents;
        }

        public synchronized List<Log> consumePendingEvents() {
            List<Log> pendingEvents = m_pendingEvents;
            m_pendingEvents = null;
            return pendingEvents;
        }

        public List<Log> getPendingEvents() {
            return m_pendingEvents;
        }

        @Override
        public void clear() {
            m_pendingEvents = null;
        }

        public boolean hasPendingEvents() {
            return m_pendingEvents != null;
        }

        public void setPendingEventsList(List<Log> pendingEvents) {
            m_pendingEvents = pendingEvents;
        }

    }

    private EventForwarder m_eventForwarder;


    public TransactionAwareEventForwarder() {
    }

    public TransactionAwareEventForwarder(EventForwarder forwarder) throws Exception {
        setEventForwarder(forwarder);
        afterPropertiesSet();
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_eventForwarder != null,
                "eventForwarder property must be set");
    }

    /** {@inheritDoc} */
    @Override
    public void sendNow(Event event) {
        Log eventLog = new Log();
        Events events = new Events();
        eventLog.setEvents(events);
        events.addEvent(event);
        sendNow(eventLog);
    }

    @Override
    public void sendNow(Log eventLog) {
        List<Log> pendingEvents = requestPendingEventsList();

        pendingEvents.add(eventLog);

        releasePendingEventsList(pendingEvents);
    }

    @Override
    public void sendNowSync(Event event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendNowSync(Log eventLog) {
        throw new UnsupportedOperationException();
    }

    public List<Log> requestPendingEventsList() {
        PendingEventsHolder eventsHolder = (PendingEventsHolder) TransactionSynchronizationManager
                .getResource(m_eventForwarder);
        if (eventsHolder != null
                && (eventsHolder.hasPendingEvents() || eventsHolder
                        .isSynchronizedWithTransaction())) {
            eventsHolder.requested();
            if (!eventsHolder.hasPendingEvents()) {
                eventsHolder.setPendingEventsList(new LinkedList<Log>());
            }
            return eventsHolder.getPendingEvents();
        }

        List<Log> pendingEvents = new LinkedList<>();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            PendingEventsHolder holderToUse = eventsHolder;
            if (holderToUse == null) {
                holderToUse = new PendingEventsHolder(pendingEvents);
            } else {
                holderToUse.setPendingEventsList(pendingEvents);
            }
            holderToUse.requested();
            TransactionSynchronizationManager
                    .registerSynchronization(new PendingEventsSynchronization(
                            holderToUse, m_eventForwarder));
            holderToUse.setSynchronizedWithTransaction(true);
            if (holderToUse != eventsHolder) {
                TransactionSynchronizationManager.bindResource(
                        m_eventForwarder, holderToUse);
            }
        }

        return pendingEvents;
    }

    public void releasePendingEventsList(List<Log> pendingEvents) {
        if (pendingEvents == null) {
            return;

        }

        PendingEventsHolder eventsHolder = (PendingEventsHolder) TransactionSynchronizationManager
                .getResource(m_eventForwarder);
        if (eventsHolder != null
                && eventHolderHolds(eventsHolder, pendingEvents)) {
            // It's the transactional Connection: Don't close it.
            eventsHolder.released();
        } else {
            for (Log log : pendingEvents) {
                m_eventForwarder.sendNow(log);
            }
        }

    }

    private boolean eventHolderHolds(PendingEventsHolder eventsHolder, List<Log> passedInEvents) {
        if (!eventsHolder.hasPendingEvents()) {
            return false;
        }
        return (eventsHolder.getPendingEvents() == passedInEvents);
    }

}
