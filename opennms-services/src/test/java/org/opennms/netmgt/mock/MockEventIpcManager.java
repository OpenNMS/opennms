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
// 2008 Feb 05: Java 5 generics, some code formatting. - dj@opennms.org
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.config.EventdConfigManager;
import org.opennms.netmgt.eventd.EventIpcBroadcaster;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerProxy;
import org.opennms.netmgt.eventd.processor.EventExpander;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class MockEventIpcManager implements EventIpcManager, EventIpcBroadcaster, InitializingBean {

    static class ListenerKeeper {
        EventListener m_listener;

        Set m_ueiList;

        ListenerKeeper(EventListener listener, Set ueiList) {
            m_listener = listener;
            m_ueiList = ueiList;
        }

        public boolean equals(Object o) {
            if (o instanceof ListenerKeeper) {
                ListenerKeeper keeper = (ListenerKeeper) o;
                return m_listener.equals(keeper.m_listener) && (m_ueiList == null ? keeper.m_ueiList == null : m_ueiList.equals(keeper.m_ueiList));
            }
            return false;
        }

        private boolean eventMatches(Event e) {
            if (m_ueiList == null)
                return true;
            return m_ueiList.contains(e.getUei());
        }

        public void sendEventIfAppropriate(Event e) {
            if (eventMatches(e)) {
                m_listener.onEvent(e);
            }
        }
    }

    /**
     * This class implements {@link EventConfDao} but every call returns null.
     */
    private static class EmptyEventConfDao implements EventConfDao {
        public void addEvent(org.opennms.netmgt.xml.eventconf.Event event) {}

        public void addEventToProgrammaticStore(org.opennms.netmgt.xml.eventconf.Event event) {}

        public org.opennms.netmgt.xml.eventconf.Event findByEvent(
                Event matchingEvent) {
            return null;
        }

        public org.opennms.netmgt.xml.eventconf.Event findByUei(String uei) {
            return null;
        }

        public String getEventLabel(String uei) {
            return null;
        }

        public Map<String, String> getEventLabels() {
            return null;
        }

        public List<String> getEventUEIs() {
            return null;
        }

        public List<org.opennms.netmgt.xml.eventconf.Event> getEvents(String uei) {
            return null;
        }

        public List<org.opennms.netmgt.xml.eventconf.Event> getEventsByLabel() {
            return null;
        }

        public boolean isSecureTag(String tag) {
            return false;
        }

        public void reload() throws DataAccessException {}

        public boolean removeEventFromProgrammaticStore(org.opennms.netmgt.xml.eventconf.Event event) {
            return false;
        }

        public void saveCurrent() {}
    }

    private EventAnticipator m_anticipator;
    
    private EventWriter m_eventWriter = new EventWriter() {
        public void writeEvent(Event e) {
            
        }
    };

    private List<ListenerKeeper> m_listeners = new ArrayList<ListenerKeeper>();

    private int m_pendingEvents;

    private int m_eventDelay = 20;

    private boolean m_synchronous = true;
    
    private ScheduledExecutorService m_scheduler = null;

    private EventIpcManagerProxy m_proxy;

    public MockEventIpcManager() {
        m_anticipator = new EventAnticipator();
    }
    
    public void addEventListener(EventListener listener) {
        m_listeners.add(new ListenerKeeper(listener, null));
    }

    public void addEventListener(EventListener listener, Collection<String> ueis) {
        m_listeners.add(new ListenerKeeper(listener, new HashSet<String>(ueis)));
    }

    public void addEventListener(EventListener listener, String uei) {
        m_listeners.add(new ListenerKeeper(listener, Collections.singleton(uei)));
    }

    public void broadcastNow(Event event) {
        MockUtil.println("Sending: " + new EventWrapper(event));
        List<ListenerKeeper> listeners = new ArrayList<ListenerKeeper>(m_listeners);
        for (ListenerKeeper k : listeners) {
            k.sendEventIfAppropriate(event);
        }
    }
    
    public void setEventWriter(EventWriter eventWriter) {
        m_eventWriter = eventWriter;
    }

    public EventAnticipator getEventAnticipator() {
        return m_anticipator;
    }
    
    public void setEventAnticipator(EventAnticipator anticipator) {
        m_anticipator = anticipator;
    }

    public void removeEventListener(EventListener listener) {
        m_listeners.remove(new ListenerKeeper(listener, null));
    }

    public void removeEventListener(EventListener listener, Collection<String> ueis) {
        m_listeners.remove(new ListenerKeeper(listener, new HashSet<String>(ueis)));
    }

    public void removeEventListener(EventListener listener, String uei) {
        m_listeners.remove(new ListenerKeeper(listener, Collections.singleton(uei)));
    }
    
    public void setEventDelay(int millis) {
        m_eventDelay  = millis;
    }

    /**
     * @param event
     */
    public void sendEventToListeners(final Event event) {
        m_eventWriter.writeEvent(event);
        broadcastNow(event);
    }

    public void setSynchronous(boolean syncState) {
        m_synchronous = syncState;
    }
    
    public boolean isSynchronous() {
        return m_synchronous;
    }
    
    public synchronized void sendNow(final Event event) {
        // Expand the event parms
        EventExpander expander = new EventExpander();
        expander.setEventConfDao(new EmptyEventConfDao());
        expander.expandEvent(event);
        m_pendingEvents++;
        MockUtil.println("StartEvent processing: m_pendingEvents = "+m_pendingEvents);
        MockUtil.println("Received: "+ new EventWrapper(event));
        m_anticipator.eventReceived(event);

        Runnable r = new Runnable() {
            public void run() {
                try {
                    m_eventWriter.writeEvent(event);
                    broadcastNow(event);
                    m_anticipator.eventProcessed(event);
                } finally {
                    synchronized(MockEventIpcManager.this) {
                        m_pendingEvents--;
                        MockUtil.println("Finished processing event m_pendingEvents = "+m_pendingEvents);
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
            m_scheduler = Executors.newSingleThreadScheduledExecutor();
        }
        return m_scheduler;
    }

    public void sendNow(Log eventLog) {
        for (Event event : eventLog.getEvents().getEventCollection()) {
            sendNow(event);
        }
    }

    /**
     * 
     */
    public synchronized void finishProcessingEvents() {
        while (m_pendingEvents > 0) {
            MockUtil.println("Waiting for event processing: m_pendingEvents = "+m_pendingEvents);
            try {
                wait();
            } catch (InterruptedException e) {
                // Do nothing
            }
        }
    }

    public EventdConfigManager getEventdConfigMgr() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setEventdConfigMgr(EventdConfigManager eventdConfigMgr) {
        // TODO Auto-generated method stub
        
    }

    public void setDataSource(DataSource instance) {
        // TODO Auto-generated method stub
        
    }
    
    
    

    public void reset() {
        m_listeners = new ArrayList<ListenerKeeper>();
        m_anticipator.reset();
    }

    public void setEventIpcManagerProxy(EventIpcManagerProxy proxy) {
        m_proxy = proxy;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_proxy, "expected to have proxy set");
        m_proxy.setDelegate(this);
    }

    public void send(Event event) throws EventProxyException {
        sendNow(event);
    }

    public void send(Log eventLog) throws EventProxyException {
        sendNow(eventLog);
    }

}
