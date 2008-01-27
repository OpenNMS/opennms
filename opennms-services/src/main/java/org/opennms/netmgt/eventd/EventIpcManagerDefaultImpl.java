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
// Tab Size = 8
//

package org.opennms.netmgt.eventd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.EventdConfigManager;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * An implementation of the EventIpcManager interface that can be used to
 * communicate between services in the same JVM
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class EventIpcManagerDefaultImpl implements EventIpcManager, InitializingBean {
    
    private EventdConfigManager m_eventdConfigMgr;
    
    /**
     * Hash table of list of event listeners keyed by event UEI
     */
    private Map<String, List<EventListener>> m_ueiListeners;

    /**
     * The list of event listeners interested in all events
     */
    private List<EventListener> m_listeners;

    /**
     * Hash table of event listener threads keyed by the listener's id
     */
    private Map<String, ListenerThread> m_listenerThreads;

    /**
     * The thread pool handling the events
     */
    private RunnableConsumerThreadPool m_eventHandlerPool;

    /**
     * The query string to get the next event id from the database sequence
     */
    private String m_getNextEventIdStr;

    private String m_getNextAlarmIdStr;

    private EventExpander m_eventExpander;
    
    private EventdServiceManager m_eventdServiceManager;

    private DataSource m_dataSource;

    /**
     * A thread dedicated to each listener. The events meant for each listener
     * is added to a dedicated queue when the 'sendNow()' is called. The
     * ListenerThread reads events off of this queue and sends it to the
     * appropriate listener
     */
    private class ListenerThread extends Object implements Runnable {
        /**
         * Listener to which this thread is dedicated
         */
        private EventListener m_listener;

        /**
         * Queue from which events for the listener are to be read
         */
        private FifoQueue<Event> m_queue;

        /**
         * The thread that is running this runnable.
         */
        private Thread m_delegateThread;

        /**
         * if set true then the thread should exist as soon as possible.
         */
        private volatile boolean m_shutdown;

        /**
         * Constructor
         */
        ListenerThread(EventListener listener, FifoQueue<Event> lq) {
            m_shutdown = false;
            m_listener = listener;
            ;
            m_queue = lq;
            m_delegateThread = new Thread(this, listener.getName());
        }

        public FifoQueue<Event> getQueue() {
            return m_queue;
        }

        /**
         * The run method preforms the actual work for the runnable. It loops
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
                Event event = null;
                try {
                    event = m_queue.remove(500);
                    if (event == null) {
                        continue;
                    }
                } catch (InterruptedException ie) {
                    m_shutdown = true;
                    break;
                } catch (FifoQueueException fqE) {
                    m_shutdown = true;
                    break;
                }

                try {
                    if (event != null) {

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
                    }
                } catch (Throwable t) {
                    log().warn("run: an unexpected error occured during ListenerThread " + m_listener.getName() + " run", t);

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

    public EventIpcManagerDefaultImpl() {
    }
    
    public synchronized void afterPropertiesSet() {
        Assert.state(m_eventdConfigMgr != null, "eventdConfigMgr not set");
        Assert.state(m_eventExpander != null, "eventExpander not set");
        Assert.state(m_dataSource != null, "dataSource not set");
        Assert.state(m_eventdServiceManager != null, "eventdServiceManager not set");
        
        m_ueiListeners = new HashMap<String, List<EventListener>>();
        m_listeners = new ArrayList<EventListener>();
        m_listenerThreads = new HashMap<String, ListenerThread>();

        m_eventHandlerPool = new RunnableConsumerThreadPool("EventHandlerPool", 0.6f, 1.0f, m_eventdConfigMgr.getReceivers());
        m_eventHandlerPool.start();
        
        m_getNextEventIdStr = m_eventdConfigMgr.getGetNextEventID();
        m_getNextAlarmIdStr = m_eventdConfigMgr.getGetNextAlarmID();
    }

    /**
     * Called by a service to send an event to other listeners.
     */
    public synchronized void sendNow(Event event) {
        Events events = new Events();
        events.addEvent(event);

        Log eventLog = new Log();
        eventLog.setEvents(events);

        sendNow(eventLog);
    }

    /**
     * Called by a service to send a set of events to other listeners.
     */
    public synchronized void sendNow(Log eventLog) {

        // create a new event handler for the events and queue it to the
        // event handler thread pool
        try {
            m_eventHandlerPool.getRunQueue().add(new EventHandler(eventLog, m_getNextEventIdStr, m_getNextAlarmIdStr, m_eventExpander, m_eventdServiceManager, m_dataSource));
        } catch (InterruptedException e) {
            log().warn("Unable to queue event log to the event handler pool queue: " + e, e);

            throw new UndeclaredEventException(e);
        } catch (FifoQueueException e) {
            log().warn("Unable to queue event log to the event handler pool queue: " + e, e);

            throw new UndeclaredEventException(e);
        }
    }

    /**
     * Called by eventd to send an event to all interested listeners
     */
    public synchronized void broadcastNow(Event event) {
        if (log().isDebugEnabled()) {
            log().debug("Event to be broadcasted: " + event.getUei());
        }

        if (m_listeners.size() <= 0) {
            if (log().isDebugEnabled()) {
                log().debug("No listeners interested in all events");
            }

        }

        // send to listeners interested in receiving all events
        for (EventListener listener : m_listeners) {
            ListenerThread listenerThr = m_listenerThreads.get(listener.getName());
            try {
                listenerThr.getQueue().add(event);
                if (log().isDebugEnabled()) {
                    log().debug("Queued event to listener: " + listener.getName());
                }
            } catch (FifoQueueException e) {
                log().error("Error queueing event " + event.getUei() + " to listener thread " + listener.getName() + ": " + e, e);
            } catch (InterruptedException e) {
                log().error("Error queueing event " + event.getUei() + " to listener thread " + listener.getName() + ": " + e, e);
            }
        }

        // get event UEI
        String uei = event.getUei();
        if (uei == null) {
            return;
        }

        // send to listeners who are interested in this event uei
        //Loop to get partial wild card "directory" matches
        while (uei.length() > 0) {
            List<EventListener> listenerList = m_ueiListeners.get(uei);
            if (listenerList != null) {
                for (EventListener listener : listenerList) {
                    ListenerThread listenerThread = m_listenerThreads.get(listener.getName());
                    try {
                        listenerThread.getQueue().add(event);
                        if (log().isDebugEnabled()) {
                            log().debug("Queued event "+uei+" to listener: " + listener.getName());
                        }
                    } catch (FifoQueueException e) {
                        log().error("Error queueing event " + event.getUei() + " to listener thread " + listener.getName() + ": " + e, e);
                    } catch (InterruptedException e) {
                        log().error("Error queueing event " + event.getUei() + " to listener thread " + listener.getName() + ": " + e, e);
                    }
                }
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("No listener interested in event: " + uei);
                }
            }
            
            //Try wild cards: Find / before last character
            int i = uei.lastIndexOf("/",uei.length()-2);
            if (i > 0) {
                //Split at "/", including the /
                uei = uei.substring (0, i+1);
            } else {
                //No more wild cards to match
                uei="";
                break;
            }
        }
    }

    /**
     * Register an event listener that is interested in all events
     */
    public synchronized void addEventListener(EventListener listener) {
        // create a new queue and listener thread for this listener
        ListenerThread listenerThread = m_listenerThreads.get(listener.getName());
        if (listenerThread == null) {
            FifoQueue<Event> lq = new FifoQueueImpl<Event>();
            listenerThread = new ListenerThread(listener, lq);
            listenerThread.start();

            m_listenerThreads.put(listener.getName(), listenerThread);
        }

        // add listener to list of listeners listening for all events
        m_listeners.add(listener);

        // remove listener from uei specific listeners
        for (String key : m_ueiListeners.keySet()) {
            List<EventListener> listenersList = m_ueiListeners.get(key);
            if (listenersList != null) {
                listenersList.remove(listener);
            }
        }
    }

    /**
     * Register an event listener interested in the UEIs in the passed list
     */
    public synchronized void addEventListener(EventListener listener, List<String> ueilist) {
        if (log().isDebugEnabled()) {
            log().debug("Adding event listener " + listener.getName() + " for " + ueilist);
        }

        if (ueilist == null || ueilist.size() == 0) {
            // nothing to do
            return;
        }

        // create a new queue and listener thread for this listener
        ListenerThread listenerThread = m_listenerThreads.get(listener.getName());
        if (listenerThread == null) {
            FifoQueue<Event> lq = new FifoQueueImpl<Event>();
            listenerThread = new ListenerThread(listener, lq);
            listenerThread.start();

            m_listenerThreads.put(listener.getName(), listenerThread);
        }

        // add to uei listeners
        for (String uei : ueilist) {
            // check if there are other listeners already, else create an entry
            List<EventListener> listenersList = m_ueiListeners.get(uei);
            if (listenersList == null) {
                listenersList = new ArrayList<EventListener>();
                listenersList.add(listener);

                m_ueiListeners.put(uei, listenersList);
            } else {
                if (!listenersList.contains(listener)) {
                    listenersList.add(listener);
                }
            }
        }

        // remove from list of listeners listening for all events
        m_listeners.remove(listener);

    }

    /**
     * Register an event listener interested in the passed UEI
     */
    public synchronized void addEventListener(EventListener listener, String uei) {
        if (uei == null) {
            // nothing to do
            return;
        }

        // create a new queue and listener thread for this listener
        ListenerThread listenerThread = m_listenerThreads.get(listener.getName());
        if (listenerThread == null) {
            FifoQueue<Event> lq = new FifoQueueImpl<Event>();
            listenerThread = new ListenerThread(listener, lq);
            listenerThread.start();

            m_listenerThreads.put(listener.getName(), listenerThread);
        }

        // check if there are other listeners already, else create an entry
        List<EventListener> listenersList = m_ueiListeners.get(uei);
        if (listenersList == null) {
            listenersList = new ArrayList<EventListener>();
            listenersList.add(listener);

            m_ueiListeners.put(uei, listenersList);
        } else {
            if (!listenersList.contains(listener)) {
                listenersList.add(listener);
            }
        }

        // remove from list of listeners listening for all events
        m_listeners.remove(listener);
    }

    /**
     * Removes a registered event listener. The UEI list indicates the list of
     * events the listener is no more interested in.
     * 
     * <strong>Note: </strong>The listener thread for this listener is not
     * stopped until the 'removeListener(EventListener listener)' method is
     * called.
     */
    public synchronized void removeEventListener(EventListener listener, List<String> ueilist) {
        if (ueilist == null || ueilist.size() == 0) {
            // nothing to do
            return;
        }

        // Iterate through the ueis and remove the listener
        for (String uei : ueilist) {
            List<EventListener> listenersList = m_ueiListeners.get(uei);
            if (listenersList != null) {
                listenersList.remove(listener);
            }
        }

    }

    /**
     * Removes a registered event listener. The UEI indicates one the listener
     * is no more interested in.
     * 
     * <strong>Note: </strong>The listener thread for this listener is not
     * stopped until the 'removeListener(EventListener listener)' method is
     * called.
     */
    public synchronized void removeEventListener(EventListener listener, String uei) {
        if (uei == null) {
            // nothing to do
            return;
        }

        List<EventListener> listenersList = m_ueiListeners.get(uei);
        if (listenersList != null) {
            listenersList.remove(listener);
        }
    }

    /**
     * Removes a registered event listener.
     * 
     * <strong>Note: </strong>Only this method stops the listener thread for the
     * listener passed.
     */
    public synchronized void removeEventListener(EventListener listener) {
        // remove from list of events that listen for all events
        m_listeners.remove(listener);

        // remove listener from uei specific listeners
        for (String key : m_ueiListeners.keySet()) {
            List<EventListener> listenersList = m_ueiListeners.get(key);
            if (listenersList != null) {
                listenersList.remove(listener);
            }
        }

        // stop the listener thread for this listener
        ListenerThread listenerThread = m_listenerThreads.get(listener.getName());
        if (listenerThread != null) {
            listenerThread.stop();

            m_listenerThreads.remove(listener.getName());
        }

    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * @return Returns the eventdConfigMgr.
     */
    public EventdConfigManager getEventdConfigMgr() {
        return m_eventdConfigMgr;
    }
    
    /**
     * @param eventdConfigMgr The eventdConfigMgr to set.
     */
    public void setEventdConfigMgr(EventdConfigManager eventdConfigMgr) {
        m_eventdConfigMgr = eventdConfigMgr;
    }

    public EventExpander getEventExpander() {
        return m_eventExpander;
    }
    
    public void setEventExpander(EventExpander eventExpander) {
        m_eventExpander = eventExpander;
    }

    public EventdServiceManager getEventdServiceManager() {
        return m_eventdServiceManager;
    }

    public void setEventdServiceManager(EventdServiceManager eventdServiceManager) {
        m_eventdServiceManager = eventdServiceManager;
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }
}
