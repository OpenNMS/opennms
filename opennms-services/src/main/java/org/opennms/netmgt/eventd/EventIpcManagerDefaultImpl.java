/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.events.EventIpcBroadcaster;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventIpcManagerProxy;
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
 */
public class EventIpcManagerDefaultImpl implements EventIpcManager, EventIpcBroadcaster, InitializingBean {

    public static class DiscardTrapsAndSyslogEvents implements RejectedExecutionHandler {
        /**
         * Creates a <tt>DiscardOldestPolicy</tt> for the given executor.
         */
        public DiscardTrapsAndSyslogEvents() { }

        /**
         * Obtains and ignores the next task that the executor
         * would otherwise execute, if one is immediately available,
         * and then retries execution of task r, unless the executor
         * is shut down, in which case task r is instead discarded.
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }

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
    private Map<String, EventListenerExecutor> m_listenerThreads = new HashMap<String, EventListenerExecutor>();

    /**
     * The thread pool handling the events
     */
    private ExecutorService m_eventHandlerPool;

    private EventHandler m_eventHandler;

    private Integer m_handlerPoolSize;
    
    private Integer m_handlerQueueLength;

    private EventIpcManagerProxy m_eventIpcManagerProxy;

    /**
     * A thread dedicated to each listener. The events meant for each listener
     * is added to an execution queue when the 'sendNow()' is called. The
     * ListenerThread reads events off of this queue and sends them to the
     * appropriate listener.
     */
    private static class EventListenerExecutor {
        /**
         * Listener to which this thread is dedicated
         */
        private final EventListener m_listener;

        /**
         * The thread that is running this runnable.
         */
        private final ExecutorService m_delegateThread;

        /**
         * Constructor
         */
        EventListenerExecutor(EventListener listener, Integer handlerQueueLength) {
            m_listener = listener;
            // You could also do Executors.newSingleThreadExecutor() here
            m_delegateThread = new ThreadPoolExecutor(
                    1,
                    1,
                    0L,
                    TimeUnit.MILLISECONDS,
                    handlerQueueLength == null ? new LinkedBlockingQueue<Runnable>() : new LinkedBlockingQueue<Runnable>(handlerQueueLength),
                    // This ThreadFactory will ensure that the log prefix of the calling thread
                    // is used for all events that this listener handles. Therefore, if Notifd
                    // registers for an event then all logs for handling that event will end up
                    // inside notifd.log.
                    new LogPreservingThreadFactory(m_listener.getName(), 1, true),
                    new RejectedExecutionHandler() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                            log().warn("Listener " + m_listener.getName() + "'s event queue is full, discarding event");
                        }
                    }
            );
        }

        public void addEvent(final Event event) {
            m_delegateThread.execute(new Runnable() {
                public void run() {
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
            });
        }

        /**
         * Stops the execution of this listener.
         */
        public void stop() {
            m_delegateThread.shutdown();
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
    public void sendNow(Event event) {
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
    public void sendNow(Log eventLog) {
        Assert.notNull(eventLog, "eventLog argument cannot be null");

        try {
            m_eventHandlerPool.execute(m_eventHandler.createRunnable(eventLog));
        } catch (RejectedExecutionException e) {
            log().warn("Unable to queue event log to the event handler pool queue: " + e, e);
            throw new UndeclaredEventException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventIpcBroadcaster#broadcastNow(org.opennms.netmgt.xml.event.Event)
     */
    /** {@inheritDoc} */
    public void broadcastNow(Event event) {
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
        
        EventListenerExecutor listenerThread = new EventListenerExecutor(listener, m_handlerQueueLength);
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

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(EventIpcManagerDefaultImpl.class);
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_eventHandlerPool == null, "afterPropertiesSet() has already been called");

        Assert.state(m_eventHandler != null, "eventHandler not set");
        Assert.state(m_handlerPoolSize != null, "handlerPoolSize not set");

        final String prefix = ThreadCategory.getPrefix();
        try {
            
            ThreadCategory.setPrefix(Eventd.LOG4J_CATEGORY);

            /**
             * Create a fixed-size thread pool. The number of threads can be configured by using
             * the "receivers" attribute in the config. The queue length for the pool can be configured
             * with the "queueLength" attribute in the config.
             */
            m_eventHandlerPool = new ThreadPoolExecutor(
                m_handlerPoolSize,
                m_handlerPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                m_handlerQueueLength == null ? new LinkedBlockingQueue<Runnable>() : new LinkedBlockingQueue<Runnable>(m_handlerQueueLength),
                new LogPreservingThreadFactory(EventIpcManagerDefaultImpl.class.getSimpleName(), m_handlerPoolSize, true)
            );
        } finally {
            ThreadCategory.setPrefix(prefix);
        }

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
     * <p>getHandlerQueueLength</p>
     *
     * @return a int.
     */
    public int getHandlerQueueLength() {
        return m_handlerQueueLength;
    }

    /**
     * <p>setHandlerQueueLength</p>
     *
     * @param size a int.
     */
    public void setHandlerQueueLength(int size) {
        Assert.state(m_eventHandlerPool == null, "handlerQueueLength property cannot be set after afterPropertiesSet() is called");
        m_handlerQueueLength = size;
    }

    /**
     * <p>getEventIpcManagerProxy</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcManagerProxy} object.
     */
    public EventIpcManagerProxy getEventIpcManagerProxy() {
        return m_eventIpcManagerProxy;
    }

    /**
     * <p>setEventIpcManagerProxy</p>
     *
     * @param eventIpcManagerProxy a {@link org.opennms.netmgt.model.events.EventIpcManagerProxy} object.
     */
    public void setEventIpcManagerProxy(EventIpcManagerProxy eventIpcManagerProxy) {
        m_eventIpcManagerProxy = eventIpcManagerProxy;
    }
}
