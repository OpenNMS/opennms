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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.dao.api.EventExpander;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.events.api.EventWriter;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.eventconf.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class MockEventIpcManager implements EventForwarder, EventProxy, EventIpcManager, EventIpcBroadcaster {
    private static final Logger LOG = LoggerFactory.getLogger(MockEventIpcManager.class);

    static class ListenerKeeper {
    	final EventListener m_listener;
    	final Set<String> m_ueiList;

        ListenerKeeper(final EventListener listener, final Set<String> ueiList) {
            m_listener = listener;
            m_ueiList = ueiList;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(27, 31)
                .append(m_listener)
                .append(m_ueiList)
                .toHashCode();
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) return false;
            if (o instanceof ListenerKeeper) {
                final ListenerKeeper keeper = (ListenerKeeper) o;
                return m_listener.equals(keeper.m_listener) && (m_ueiList == null ? keeper.m_ueiList == null : m_ueiList.equals(keeper.m_ueiList));
            }
            return false;
        }

        private boolean eventMatches(final Event e) {
            if (m_ueiList == null)
                return true;
            return m_ueiList.contains(e.getUei());
        }

        public void sendEventIfAppropriate(final Event e) {
            if (eventMatches(e)) {
                m_listener.onEvent(e);
            }
        }
    }

    /**
     * This class implements {@link EventConfDao} but every call returns null.
     */
    public static class EmptyEventConfDao implements EventConfDao {
        @Override
        public void addEvent(final org.opennms.netmgt.xml.eventconf.Event event) {}

        @Override
        public void addEventToProgrammaticStore(final org.opennms.netmgt.xml.eventconf.Event event) {}

        @Override
        public org.opennms.netmgt.xml.eventconf.Event findByEvent(final Event matchingEvent) {
            return null;
        }

        @Override
        public org.opennms.netmgt.xml.eventconf.Event findByUei(final String uei) {
            return null;
        }

        @Override
        public String getEventLabel(final String uei) {
            return null;
        }

        @Override
        public Map<String, String> getEventLabels() {
            return null;
        }

        @Override
        public List<String> getEventUEIs() {
            return null;
        }

        @Override
        public List<org.opennms.netmgt.xml.eventconf.Event> getEvents(final String uei) {
            return null;
        }

        @Override
        public List<org.opennms.netmgt.xml.eventconf.Event> getEventsByLabel() {
            return null;
        }

        @Override
        public boolean isSecureTag(final String tag) {
            return false;
        }

        @Override
        public void reload() throws DataAccessException {}

        @Override
        public boolean removeEventFromProgrammaticStore(final org.opennms.netmgt.xml.eventconf.Event event) {
            return false;
        }

        @Override
        public void saveCurrent() {}

        @Override
        public Events getRootEvents() {
            return null;
        }
    }

    public static interface SendNowHook {
        public void beforeBroadcast(Event event);

        public void afterBroadcast(Event event);

        void finishProcessingEvents();
    }

    private EventAnticipator m_anticipator;
    
    private EventWriter m_eventWriter = new EventWriter() {
        @Override
        public void writeEvent(final Event e) {
            
        }
    };

    private List<ListenerKeeper> m_listeners = new ArrayList<ListenerKeeper>();

    private int m_pendingEvents;

    private volatile int m_eventDelay = 20;

    private boolean m_synchronous = true;
    
    private ScheduledExecutorService m_scheduler = null;

	private EventExpander m_expander = null;

	private SendNowHook m_sendNowHook;

	private int m_numSchedulerThreads = 1;

    public MockEventIpcManager() {
        m_anticipator = new EventAnticipator();
    }
    
    @Override
    public void addEventListener(final EventListener listener) {
        m_listeners.add(new ListenerKeeper(listener, null));
    }

    @Override
    public void addEventListener(final EventListener listener, final Collection<String> ueis) {
        m_listeners.add(new ListenerKeeper(listener, new HashSet<String>(ueis)));
    }

    @Override
    public void addEventListener(final EventListener listener, final String uei) {
        m_listeners.add(new ListenerKeeper(listener, Collections.singleton(uei)));
    }

    @Override
    public void broadcastNow(final Event event) {
    	
    	LOG.debug("Sending: {}", new EventWrapper(event));
        final List<ListenerKeeper> listeners = new ArrayList<ListenerKeeper>(m_listeners);
        for (final ListenerKeeper k : listeners) {
            k.sendEventIfAppropriate(event);
        }
    }
    
    public void setEventWriter(final EventWriter eventWriter) {
        m_eventWriter = eventWriter;
    }

    public EventAnticipator getEventAnticipator() {
        return m_anticipator;
    }
    
    public void setEventExpander(final EventExpander expander) {
        m_expander  = expander;
    }

    public void setEventAnticipator(final EventAnticipator anticipator) {
        m_anticipator = anticipator;
    }

    @Override
    public void removeEventListener(final EventListener listener) {
        m_listeners.remove(new ListenerKeeper(listener, null));
    }

    @Override
    public void removeEventListener(final EventListener listener, final Collection<String> ueis) {
        m_listeners.remove(new ListenerKeeper(listener, new HashSet<String>(ueis)));
    }

    @Override
    public void removeEventListener(final EventListener listener, final String uei) {
        m_listeners.remove(new ListenerKeeper(listener, Collections.singleton(uei)));
    }
    
    public synchronized void setEventDelay(final int millis) {
        m_eventDelay  = millis;
    }

    /**
     * @param event
     */
    public void sendEventToListeners(final Event event) {
        m_eventWriter.writeEvent(event);
        broadcastNow(event);
    }

    public void setSynchronous(final boolean syncState) {
        m_synchronous = syncState;
    }
    
    public boolean isSynchronous() {
        return m_synchronous;
    }

    public void setSendNowHook(SendNowHook hook) {
        m_sendNowHook = hook;
    }

    public SendNowHook getSendNowHook() {
        return m_sendNowHook;
    }

    public void setNumSchedulerThreads(int numThreads) {
        m_numSchedulerThreads = numThreads;
    }

    public int getNumSchedulerTheads() {
        return m_numSchedulerThreads;
    }

    @Override
    public synchronized void sendNow(final Event event) {
        // Expand the event parms
        if (m_expander != null) {
            m_expander.expandEvent(event);
        }
        m_pendingEvents++;
        LOG.debug("StartEvent processing ({} remaining)", m_pendingEvents);
        LOG.debug("Received: {}", new EventWrapper(event));
        m_anticipator.eventReceived(event);

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    // Allows external classes to alter the timing/order of events
                    if (m_sendNowHook != null) {
                        m_sendNowHook.beforeBroadcast(event);
                    }

                    m_eventWriter.writeEvent(event);
                    broadcastNow(event);
                    m_anticipator.eventProcessed(event);
                } finally {
                    synchronized(MockEventIpcManager.this) {
                        m_pendingEvents--;
                        LOG.debug("Finished processing event ({} remaining)", m_pendingEvents);
                        MockEventIpcManager.this.notifyAll();
                    }

                    // Allows external classes to alter the timing/order of events
                    if (m_sendNowHook != null) {
                        m_sendNowHook.afterBroadcast(event);
                    }
                }
            }
        };
        
        if (isSynchronous()) {
            r.run();
        } else {
            getScheduler().schedule(r, m_eventDelay, TimeUnit.MILLISECONDS);
        }
    }

    ScheduledExecutorService getScheduler() {
        if (m_scheduler == null) {
            m_scheduler = Executors.newScheduledThreadPool(getNumSchedulerTheads(),
                new LogPreservingThreadFactory(getClass().getSimpleName(), getNumSchedulerTheads())
            );
        }
        return m_scheduler;
    }

    @Override
    public void sendNow(final Log eventLog) {
        for (final Event event : eventLog.getEvents().getEventCollection()) {
            sendNow(event);
        }
    }

    /**
     * 
     */
    public synchronized void finishProcessingEvents() {
        // Allow the hook to unblock any pending events
        if (m_sendNowHook != null) {
            m_sendNowHook.finishProcessingEvents();
        }

        while (m_pendingEvents > 0) {
        	LOG.debug("Waiting for event processing: ({} remaining)", m_pendingEvents);
            try {
                wait();
            } catch (final InterruptedException e) {
                // Do nothing
            }
        }
    }

    public EventdConfig getEventdConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setEventdConfig(final EventdConfig eventdConfig) {
        // TODO Auto-generated method stub
    }

    public void setDataSource(final DataSource instance) {
        // TODO Auto-generated method stub
        
    }
    
    
    

    public void reset() {
        m_listeners = new ArrayList<ListenerKeeper>();
        m_anticipator.reset();
    }

    @Override
    public void send(final Event event) throws EventProxyException {
        sendNow(event);
    }

    @Override
    public void send(final Log eventLog) throws EventProxyException {
        sendNow(eventLog);
    }

}
