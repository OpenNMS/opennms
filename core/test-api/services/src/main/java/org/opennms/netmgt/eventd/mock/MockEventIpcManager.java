/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.mock;

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
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.config.EventExpander;
import org.opennms.netmgt.config.EventdConfigManager;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventIpcBroadcaster;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventIpcManagerProxy;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.model.events.EventWriter;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class MockEventIpcManager implements EventForwarder, EventProxy, EventIpcManager, EventIpcBroadcaster, InitializingBean {

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
    private static class EmptyEventConfDao implements EventConfDao {
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

    private EventIpcManagerProxy m_proxy;

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
        LogUtils.debugf(this, "Sending: %s", new EventWrapper(event));
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
    
    @Override
    public synchronized void sendNow(final Event event) {
        // Expand the event parms
        final EventExpander expander = new EventExpander();
        expander.setEventConfDao(new EmptyEventConfDao());
        expander.expandEvent(event);
        m_pendingEvents++;
        LogUtils.debugf(this, "StartEvent processing (%d remaining)", m_pendingEvents);
        LogUtils.debugf(this, "Received: ", new EventWrapper(event));
        m_anticipator.eventReceived(event);

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    m_eventWriter.writeEvent(event);
                    broadcastNow(event);
                    m_anticipator.eventProcessed(event);
                } finally {
                    synchronized(MockEventIpcManager.this) {
                        m_pendingEvents--;
                        LogUtils.debugf(this, "Finished processing event (%d remaining)", m_pendingEvents);
                        MockEventIpcManager.this.notifyAll();
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
            m_scheduler = Executors.newSingleThreadScheduledExecutor(
                new LogPreservingThreadFactory(getClass().getSimpleName(), 1, false)
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
        while (m_pendingEvents > 0) {
            LogUtils.debugf(this, "Waiting for event processing: (%d remaining)", m_pendingEvents);
            try {
                wait();
            } catch (final InterruptedException e) {
                // Do nothing
            }
        }
    }

    public EventdConfigManager getEventdConfigMgr() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setEventdConfigMgr(final EventdConfigManager eventdConfigMgr) {
        // TODO Auto-generated method stub
        
    }

    public void setDataSource(final DataSource instance) {
        // TODO Auto-generated method stub
        
    }
    
    
    

    public void reset() {
        m_listeners = new ArrayList<ListenerKeeper>();
        m_anticipator.reset();
    }

    public void setEventIpcManagerProxy(final EventIpcManagerProxy proxy) {
        m_proxy = proxy;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_proxy, "expected to have proxy set");
        m_proxy.setDelegate(this);
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
