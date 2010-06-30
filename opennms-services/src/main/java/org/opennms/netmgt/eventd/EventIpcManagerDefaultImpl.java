//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 10: Make it easy to set this class as the delegate for an
//              EventIpcManagerProxy object when afterPropertiesSet() is called. - dj@opennms.org
// 2008 Feb 02: Refactor out common code and pull code closer to the objects
//              it manipulates.  Fix bug where the same event could be sent
//              to the same listener multiple times. - dj@opennms.org
// 2008 Jan 27: Dependency inject (now-threadsafe) EventHandler. - dj@opennms.org
// 2008 Jan 26: Some code and comment formatting. - dj@opennms.org
// 2008 Jan 26: Dependency injection for DataSource and EventdServiceManager. - dj@opennms.org
// 2008 Jan 08: Dependency inject EventExpander, pass EventExpander to 
//              EventHandler, and create log() method. - dj@opennms.org
// 2008 Jan 07: Indent and format code a bit, implement log(). - dj@opennms.org
// 2007 Aug 25: Save and restore the log4j logging prefix when we
//              call onEvent(Event). - dj@opennms.org
// 2006 May 29: Throw IllegalStateException in init() if m_eventdConfigMgr is null - dj@gregor.com
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 24: Changed all references to HashTable to HashMap
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.eventd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * An implementation of the EventIpcManager interface that can be used to
 * communicate between services in the same JVM
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public class EventIpcManagerDefaultImpl implements EventIpcManager, EventIpcBroadcaster, InitializingBean {
    /**
     * Hash table of list of event listeners keyed by event UEI
     */
    private Map<String, List<EventListener>> m_ueiListeners = new HashMap<String, List<EventListener>>();

    /**
     * The list of event listeners interested in all events
     */
    private List<EventListener> m_listeners = new ArrayList<EventListener>();

    /**
     * Hash table of event listener threads keyed by the listener's id
     */
    private Map<String, ListenerThread> m_listenerThreads = new HashMap<String, ListenerThread>();

    /**
     * The thread pool handling the events
     */
    private RunnableConsumerThreadPool m_eventHandlerPool;

    private EventHandler m_eventHandler;

    private Integer m_handlerPoolSize;
    
    private EventIpcManagerProxy m_eventIpcManagerProxy;

    /**
     * A thread dedicated to each listener. The events meant for each listener
     * is added to a dedicated queue when the 'sendNow()' is called. The
     * ListenerThread reads events off of this queue and sends it to the
     * appropriate listener
     */
    private class ListenerThread implements Runnable {
        /**
         * Listener to which this thread is dedicated
         */
        private EventListener m_listener;

        /**
         * Queue from which events for the listener are to be read
         */
        private FifoQueue<Event> m_queue = new FifoQueueImpl<Event>();

        /**
         * The thread that is running this runnable.
         */
        private Thread m_delegateThread;

        /**
         * If set true then the thread should stop processing as soon as possible.
         */
        private volatile boolean m_shutdown = true;

        /**
         * Constructor
         */
        ListenerThread(EventListener listener) {
            m_listener = listener;
            m_delegateThread = new Thread(this, m_listener.getName());
        }
        
        public void addEvent(Event event) {
            try {
                m_queue.add(event);
                if (log().isDebugEnabled()) {
                    log().debug("Queued event ID " + event.getDbid() + " to listener thread: " + m_listener.getName());
                }
            } catch (FifoQueueException e) {
                log().error("Error queueing event " + event.getUei() + " to listener thread " + m_listener.getName() + ": " + e, e);
            } catch (InterruptedException e) {
                log().error("Error queueing event " + event.getUei() + " to listener thread " + m_listener.getName() + ": " + e, e);
            }
        }

        /**
         * The run method performs the actual work for the runnable. It loops
         * infinitely until the shutdown flag is set, during which time it
         * processes queue elements. Each element in the queue should be a
         * instance of {@link org.opennms.netmgt.xml.event.Event}. After each
         * event is read, the 'onEvent' method of the listener is invoked.
         * 
         */
        public void run() {
            if (log().isDebugEnabled()) {
                log().debug("In ListenerThread " + m_listener.getName() + " run");
            }

            while (!m_shutdown) {
                Event event;
                try {
                    event = m_queue.remove(500);
                    if (event == null) {
                        continue;
                    }
                } catch (InterruptedException e) {
                    m_shutdown = true;
                    break;
                } catch (FifoQueueException e) {
                    m_shutdown = true;
                    break;
                }

                try {
                    if (log().isInfoEnabled()) {
                        log().info("run: calling onEvent on " + m_listener.getName() + " for event " + event.getUei() + " dbid " + event.getDbid() + " with time " + event.getTime());
                    }

                    // Make sure we restore our log4j logging prefix after onEvent is called
                    String log4jPrefix = ThreadCategory.getPrefix(); 
                    try {
                        m_listener.onEvent(event);
                    } finally {
                        ThreadCategory.setPrefix(log4jPrefix);
                    }
                } catch (Throwable t) {
                    log().warn("run: an unexpected error occured during ListenerThread " + m_listener.getName() + " run: " + t, t);
                }
            }
        }

        /**
         * Starts up the thread.
         */
        public void start() {
            m_shutdown = false;
            m_delegateThread.start();
        }

        /**
         * Sets the stop flag in the thread.
         */
        public void stop() {
            m_shutdown = true;
        }

    }

    /**
     * <p>Constructor for EventIpcManagerDefaultImpl.</p>
     */
    public EventIpcManagerDefaultImpl() {
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

    /**
     * {@inheritDoc}
     *
     * Called by a service to send an event to other listeners.
     */
    public synchronized void sendNow(Event event) {
        Assert.notNull(event, "event argument cannot be null");

        Events events = new Events();
        events.addEvent(event);

        Log eventLog = new Log();
        eventLog.setEvents(events);

        sendNow(eventLog);
    }

    /**
     * Called by a service to send a set of events to other listeners.
     * Creates a new event handler for the event log and queues it to the
     * event handler thread pool.
     *
     * @param eventLog a {@link org.opennms.netmgt.xml.event.Log} object.
     */
    public synchronized void sendNow(Log eventLog) {
        Assert.notNull(eventLog, "eventLog argument cannot be null");

        try {
            m_eventHandlerPool.getRunQueue().add(m_eventHandler.createRunnable(eventLog));
        } catch (InterruptedException e) {
            log().warn("Unable to queue event log to the event handler pool queue: " + e, e);

            throw new UndeclaredEventException(e);
        } catch (FifoQueueException e) {
            log().warn("Unable to queue event log to the event handler pool queue: " + e, e);

            throw new UndeclaredEventException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventIpcBroadcaster#broadcastNow(org.opennms.netmgt.xml.event.Event)
     */
    /** {@inheritDoc} */
    public synchronized void broadcastNow(Event event) {
        if (log().isDebugEnabled()) {
            log().debug("Event ID " + event.getDbid() + " to be broadcasted: " + event.getUei());
        }

        if (m_listeners.isEmpty()) {
            log().debug("No listeners interested in all events");
        }

        // Send to listeners interested in receiving all events
        for (EventListener listener : m_listeners) {
            queueEventToListener(event, listener);
        }

        if (event.getUei() == null) {
            if (log().isDebugEnabled()) {
                log().debug("Event ID " + event.getDbid() + " does not have a UEI, so skipping UEI matching");
            }
            return;
        }

        /*
         * Send to listeners who are interested in this event UEI.
         * Loop to attempt partial wild card "directory" matches.
         */
        Set<EventListener> sentToListeners = new HashSet<EventListener>();
        for (String uei = event.getUei(); uei.length() > 0; ) {
            if (m_ueiListeners.containsKey(uei)) {
                for (EventListener listener : m_ueiListeners.get(uei)) {
                    if (!sentToListeners.contains(listener)) {
                        queueEventToListener(event, listener);
                        sentToListeners.add(listener);
                    }
                }
            }
            
            // Try wild cards: Find / before last character
            int i = uei.lastIndexOf("/", uei.length() - 2);
            if (i > 0) {
                // Split at "/", including the /
                uei = uei.substring (0, i + 1);
            } else {
                // No more wild cards to match
                break;
            }
        }
        
        if (sentToListeners.isEmpty()) {
            if (log().isDebugEnabled()) {
                log().debug("No listener interested in event ID " + event.getDbid() + ": " + event.getUei());
            }
        }
    }

    private void queueEventToListener(Event event, EventListener listener) {
        m_listenerThreads.get(listener.getName()).addEvent(event);
    }

    /**
     * {@inheritDoc}
     *
     * Register an event listener that is interested in all events.
     * Removes this listener from any UEI-specific matches.
     */
    public synchronized void addEventListener(EventListener listener) {
        Assert.notNull(listener, "listener argument cannot be null");

        createListenerThread(listener);

        addMatchAllForListener(listener);

        // Since we have a match-all listener, remove any specific UEIs
        for (String uei : m_ueiListeners.keySet()) {
            removeUeiForListener(uei, listener);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Register an event listener interested in the UEIs in the passed list.
     */
    public synchronized void addEventListener(EventListener listener, Collection<String> ueis) {
        Assert.notNull(listener, "listener argument cannot be null");
        Assert.notNull(ueis, "ueilist argument cannot be null");

        if (ueis.isEmpty()) {
            log().warn("Not adding event listener " + listener.getName() + " because the ueilist argument contains no entries");
            return;
        }

        if (log().isDebugEnabled()) {
            log().debug("Adding event listener " + listener.getName() + " for UEIs: " + StringUtils.collectionToCommaDelimitedString(ueis));
        }

        createListenerThread(listener);

        for (String uei : ueis) {
            addUeiForListener(uei, listener);
        }

        // Since we have a UEI-specific listener, remove the match-all listener
        removeMatchAllForListener(listener);
    }

    /**
     * Register an event listener interested in the passed UEI.
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public synchronized void addEventListener(EventListener listener, String uei) {
        Assert.notNull(listener, "listener argument cannot be null");
        Assert.notNull(uei, "uei argument cannot be null");
        
        addEventListener(listener, Collections.singletonList(uei));
    }

    /**
     * {@inheritDoc}
     *
     * Removes a registered event listener. The UEI list indicates the list of
     * events the listener is no more interested in.
     *
     * <strong>Note: </strong>The listener thread for this listener is not
     * stopped until the 'removeEventListener(EventListener listener)' method is
     * called.
     */
    public synchronized void removeEventListener(EventListener listener, Collection<String> ueis) {
        Assert.notNull(listener, "listener argument cannot be null");
        Assert.notNull(ueis, "ueilist argument cannot be null");

        for (String uei : ueis) {
            removeUeiForListener(uei, listener);
        }
    }

    /**
     * Removes a registered event listener. The UEI indicates one the listener
     * is no more interested in.
     *
     * <strong>Note: </strong>The listener thread for this listener is not
     * stopped until the 'removeEventListener(EventListener listener)' method is
     * called.
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public synchronized void removeEventListener(EventListener listener, String uei) {
        Assert.notNull(listener, "listener argument cannot be null");
        Assert.notNull(uei, "uei argument cannot be null");

        removeUeiForListener(uei, listener);
    }

    /**
     * {@inheritDoc}
     *
     * Removes a registered event listener.
     *
     * <strong>Note: </strong>Only this method stops the listener thread for the
     * listener passed.
     */
    public synchronized void removeEventListener(EventListener listener) {
        Assert.notNull(listener, "listener argument cannot be null");
        
        removeMatchAllForListener(listener);

        for (String uei : m_ueiListeners.keySet()) {
            removeUeiForListener(uei, listener);
        }

        // stop and remove the listener thread for this listener
        if (m_listenerThreads.containsKey(listener.getName())) {
            m_listenerThreads.get(listener.getName()).stop();

            m_listenerThreads.remove(listener.getName());
        }
    }

    /**
     * Create a new queue and listener thread for this listener if one does not
     * already exist.
     */
    private void createListenerThread(EventListener listener) {
        if (m_listenerThreads.containsKey(listener.getName())) {
            return;
        }
        
        ListenerThread listenerThread = new ListenerThread(listener);
        listenerThread.start();

        m_listenerThreads.put(listener.getName(), listenerThread);
    }

    /**
     * Add to uei listeners.
     */
    private void addUeiForListener(String uei, EventListener listener) {
        // Ensure there is a list for this UEI
        if (!m_ueiListeners.containsKey(uei)) {
            m_ueiListeners.put(uei, new ArrayList<EventListener>());
        }
        
        List<EventListener> listenersList = m_ueiListeners.get(uei);
        if (!listenersList.contains(listener)) {
            listenersList.add(listener);
        }
    }

    /**
     * Remove UEI for this listener.
     */
    private void removeUeiForListener(String uei, EventListener listener) {
        if (m_ueiListeners.containsKey(uei)) {
            m_ueiListeners.get(uei).remove(listener);
        }
    }

    /**
     * Add listener to list of listeners listening for all events.
     */
    private boolean addMatchAllForListener(EventListener listener) {
        return m_listeners.add(listener);
    }

    /**
     * Remove from list of listeners listening for all events.
     */
    private boolean removeMatchAllForListener(EventListener listener) {
        return m_listeners.remove(listener);
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    public synchronized void afterPropertiesSet() {
        Assert.state(m_eventHandlerPool == null, "afterPropertiesSet() has already been called");
        
        Assert.state(m_eventHandler != null, "eventHandler not set");
        Assert.state(m_handlerPoolSize != null, "handlerPoolSize not set");
        
        m_eventHandlerPool = new RunnableConsumerThreadPool("EventHandlerPool", 0.6f, 1.0f, m_handlerPoolSize);
        m_eventHandlerPool.start();
        
        // If the proxy is set, make this class its delegate.
        if (m_eventIpcManagerProxy != null) {
            m_eventIpcManagerProxy.setDelegate(this);
        }
    }

    /**
     * <p>getEventHandler</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.EventHandler} object.
     */
    public EventHandler getEventHandler() {
        return m_eventHandler;
    }

    /**
     * <p>setEventHandler</p>
     *
     * @param eventHandler a {@link org.opennms.netmgt.eventd.EventHandler} object.
     */
    public void setEventHandler(EventHandler eventHandler) {
        m_eventHandler = eventHandler;
    }

    /**
     * <p>getHandlerPoolSize</p>
     *
     * @return a int.
     */
    public int getHandlerPoolSize() {
        return m_handlerPoolSize;
    }

    /**
     * <p>setHandlerPoolSize</p>
     *
     * @param handlerPoolSize a int.
     */
    public void setHandlerPoolSize(int handlerPoolSize) {
        Assert.state(m_eventHandlerPool == null, "handlerPoolSize property cannot be set after afterPropertiesSet() is called");
        
        m_handlerPoolSize = handlerPoolSize;
    }

    /**
     * <p>getEventIpcManagerProxy</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.EventIpcManagerProxy} object.
     */
    public EventIpcManagerProxy getEventIpcManagerProxy() {
        return m_eventIpcManagerProxy;
    }

    /**
     * <p>setEventIpcManagerProxy</p>
     *
     * @param eventIpcManagerProxy a {@link org.opennms.netmgt.eventd.EventIpcManagerProxy} object.
     */
    public void setEventIpcManagerProxy(EventIpcManagerProxy eventIpcManagerProxy) {
        m_eventIpcManagerProxy = eventIpcManagerProxy;
    }
}
