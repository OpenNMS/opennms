/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 23, 2007
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * <p>TransactionAwareEventIpcManagerProxy class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class TransactionAwareEventIpcManagerProxy implements EventIpcManager, InitializingBean {
    
    public static class PendingEventsSynchronization extends TransactionSynchronizationAdapter {
        
        private PendingEventsHolder m_eventsHolder;
        private EventIpcManager m_eventIpcManager;

        public PendingEventsSynchronization(PendingEventsHolder eventsHolder, EventIpcManager eventIpcManager) {
            m_eventsHolder = eventsHolder;
            m_eventIpcManager = eventIpcManager;
        }

        @Override
        public void afterCommit() {
            if (!m_eventsHolder.hasPendingEvents()) {
                return;
            }
            
            List<Log> pendingEvents = m_eventsHolder.consumePendingEvents();
            for(Log events : pendingEvents) {
                m_eventIpcManager.sendNow(events);
            }
        }

        @Override
        public void afterCompletion(int status) {
            if (TransactionSynchronizationManager.hasResource(m_eventIpcManager)) {
                TransactionSynchronizationManager.unbindResource(m_eventIpcManager);
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

    private EventIpcManager m_eventIpcManager;
    
    
    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_eventIpcManager != null, "eventIpcManager property must be set");
    }

    /** {@inheritDoc} */
    public void addEventListener(EventListener listener, Collection<String> ueis) {
        m_eventIpcManager.addEventListener(listener, ueis);
    }

    /**
     * <p>addEventListener</p>
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void addEventListener(EventListener listener, String uei) {
        m_eventIpcManager.addEventListener(listener, uei);
    }

    /** {@inheritDoc} */
    public void addEventListener(EventListener listener) {
        m_eventIpcManager.addEventListener(listener);
    }

    /** {@inheritDoc} */
    public void removeEventListener(EventListener listener, Collection<String> ueis) {
        m_eventIpcManager.removeEventListener(listener, ueis);
    }

    /**
     * <p>removeEventListener</p>
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void removeEventListener(EventListener listener, String uei) {
        m_eventIpcManager.removeEventListener(listener, uei);
    }

    /** {@inheritDoc} */
    public void removeEventListener(EventListener listener) {
        m_eventIpcManager.removeEventListener(listener);
    }
    
    /** {@inheritDoc} */
    public void send(Event event) throws EventProxyException {
        sendNow(event);
    }

    /**
     * <p>send</p>
     *
     * @param eventLog a {@link org.opennms.netmgt.xml.event.Log} object.
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
     */
    public void send(Log eventLog) throws EventProxyException {
        sendNow(eventLog);
    }

    /** {@inheritDoc} */
    public void sendNow(Event event) {
        Log eventLog = new Log();
        Events events = new Events();
        eventLog.setEvents(events);
        events.addEvent(event);
        sendNow(eventLog);
    }

    /**
     * <p>sendNow</p>
     *
     * @param eventLog a {@link org.opennms.netmgt.xml.event.Log} object.
     */
    public void sendNow(Log eventLog) {
        List<Log> pendingEvents = requestPendingEventsList();
        
        pendingEvents.add(eventLog);
        
        releasePendingEventsList(pendingEvents);
    }
    
    /**
     * <p>requestPendingEventsList</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Log> requestPendingEventsList() {
        PendingEventsHolder eventsHolder = (PendingEventsHolder) TransactionSynchronizationManager.getResource(m_eventIpcManager);
        if (eventsHolder != null && (eventsHolder.hasPendingEvents() || eventsHolder.isSynchronizedWithTransaction())) {
            eventsHolder.requested();
            if (!eventsHolder.hasPendingEvents()) {
                eventsHolder.setPendingEventsList(new LinkedList<Log>());
            }
            return eventsHolder.getPendingEvents();
        }
        
        List<Log> pendingEvents = new LinkedList<Log>();
        
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            PendingEventsHolder holderToUse = eventsHolder;
            if (holderToUse == null) {
                holderToUse = new PendingEventsHolder(pendingEvents);
            }
            else {
                holderToUse.setPendingEventsList(pendingEvents);
            }
            holderToUse.requested();
            TransactionSynchronizationManager.registerSynchronization(
                    new PendingEventsSynchronization(holderToUse, m_eventIpcManager));
            holderToUse.setSynchronizedWithTransaction(true);
            if (holderToUse != eventsHolder) {
                TransactionSynchronizationManager.bindResource(m_eventIpcManager, holderToUse);
            }
        }

        return pendingEvents;
    }
    
    /**
     * <p>releasePendingEventsList</p>
     *
     * @param pendingEvents a {@link java.util.List} object.
     */
    public void releasePendingEventsList(List<Log> pendingEvents) {
        if (pendingEvents == null) {
            return;
            
        }

        PendingEventsHolder eventsHolder = (PendingEventsHolder) TransactionSynchronizationManager.getResource(m_eventIpcManager);
        if (eventsHolder != null && eventHolderHolds(eventsHolder, pendingEvents)) {
            // It's the transactional Connection: Don't close it.
            eventsHolder.released();
            return;
        }
       
    }

    private boolean eventHolderHolds(PendingEventsHolder eventsHolder, List<Log> passedInEvents) {
        if (!eventsHolder.hasPendingEvents()) {
            return false;
        }
        return (eventsHolder.getPendingEvents() == passedInEvents);
    }
}
