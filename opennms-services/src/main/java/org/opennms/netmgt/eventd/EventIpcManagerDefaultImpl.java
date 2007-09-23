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
import java.util.Iterator;
import java.util.List;

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

/**
 * An implementation of the EventIpcManager interface that can be used to
 * communicate between services in the same JVM
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class EventIpcManagerDefaultImpl implements EventIpcManager {
    
    private static EventdConfigManager m_eventdConfigMgr;
    
    /**
     * Hashtable of list of event listeners keyed by event UEI
     */
    private HashMap m_ueiListeners;

    /**
     * The list of event listeners interested in all events
     */
    private List m_listeners;

    /**
     * Hashtable of event listener threads keyed by the listener's id
     */
    private HashMap m_listenerThreads;

    /**
     * The thread pool handling the events
     */
    private RunnableConsumerThreadPool m_eventHandlerPool;

    /**
     * The query string to get the next event id from the database sequence
     */
    private String m_getNextEventIdStr;

    private String m_getNextAlarmIdStr;

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
        private FifoQueue m_queue;

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
        ListenerThread(EventListener listener, FifoQueue lq) {
            m_shutdown = false;
            m_listener = listener;
            ;
            m_queue = lq;
            m_delegateThread = new Thread(this, listener.getName());
        }

        public FifoQueue getQueue() {
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
            Category log = ThreadCategory.getInstance(this.getClass());
            if (log.isDebugEnabled())
                log.debug("In ListenerThread " + m_listener.getName() + " run");

            while (!m_shutdown) {
                Object obj = null;
                try {
                    obj = m_queue.remove(500);
                    if (obj == null)
                        continue;
                } catch (InterruptedException ie) {
                    m_shutdown = true;
                    break;
                } catch (FifoQueueException fqE) {
                    m_shutdown = true;
                    break;
                }

                try {
                    if (obj != null && obj instanceof Event) {
                        Event event = (Event) obj;

                        if (log.isInfoEnabled()) {
                            log.info("run: calling onEvent on " + m_listener.getName() + " for event " + event.getUei() + " dbid " + event.getDbid() + " with time " + event.getTime());
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
                    log.warn("run: an unexpected error occured during ListenerThread " + m_listener.getName() + " run", t);

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
     * Constructor
     */
    public EventIpcManagerDefaultImpl() {
        init();
    }

    public EventIpcManagerDefaultImpl(EventdConfigManager configMgr) {
        setEventdConfigMgr(configMgr);
        init();
    }

    public void init() {
        if (m_eventdConfigMgr == null) {
            throw new IllegalStateException("eventd configuration manager not set");
        }
        
        m_ueiListeners = new HashMap();
        m_listeners = new ArrayList();
        m_listenerThreads = new HashMap();

        // get number of threads
        int numReceivers = m_eventdConfigMgr.getReceivers();
        // create handler pool
        m_eventHandlerPool = new RunnableConsumerThreadPool("EventHandlerPool", 0.6f, 1.0f, numReceivers);
        // start pool
        m_eventHandlerPool.start();
        // database sequence query string
        m_getNextEventIdStr = m_eventdConfigMgr.getGetNextEventID();
        m_getNextAlarmIdStr = m_eventdConfigMgr.getGetNextAlarmID();
    }

    /**
     * Called by a service to send an event to other listeners.
     */
    public synchronized void sendNow(Event event) {
        // create a new event handler for the event and queue it to the
        // eventhandler thread pool
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
        // eventhandler thread pool
        try {
            m_eventHandlerPool.getRunQueue().add(new EventHandler(eventLog, m_getNextEventIdStr, m_getNextAlarmIdStr));
        } catch (InterruptedException iE) {
            Category log = ThreadCategory.getInstance(this.getClass());
            log.warn("Unable to queue event log to the event handler pool queue", iE);

            throw new UndeclaredEventException(iE);
        } catch (FifoQueueException qE) {
            Category log = ThreadCategory.getInstance(this.getClass());
            log.warn("Unable to queue event log to the event handler pool queue", qE);

            throw new UndeclaredEventException(qE);
        }
    }

    /**
     * Called by eventd to send an event to all interested listeners
     */
    public synchronized void broadcastNow(Event event) {
        Category log = ThreadCategory.getInstance(this.getClass());
        if (log.isDebugEnabled())
            log.debug("Event to be broadcasted: " + event.getUei());

        if (m_listeners.size() <= 0) {
            if (log.isDebugEnabled())
                log.debug("No listeners interested in all events");

        }

        // send to listeners interested in receiving all events
        Iterator listenerIter = m_listeners.iterator();
        while (listenerIter.hasNext()) {
            EventListener listener = (EventListener) listenerIter.next();

            ListenerThread listenerThr = (ListenerThread) m_listenerThreads.get(listener.getName());
            try {
                listenerThr.getQueue().add(event);
                if (log.isDebugEnabled())
                    log.debug("Queued event to listener: " + listener.getName());
            } catch (FifoQueueException fe) {
                log.error("Error queueing event " + event.getUei() + " to listener thread " + listener.getName(), fe);
            } catch (InterruptedException ie) {
                log.error("Error queueing event " + event.getUei() + " to listener thread " + listener.getName(), ie);
            }
        }

        // get event UEI
        String uei = event.getUei();
        if (uei == null) {
            return;
        }

        // send to listeners who are interested in this event uei
        //Loop to get partial wildcard "directory" matches
        while (uei.length() > 0){
        List listenerList = (List) m_ueiListeners.get(uei);
        if (listenerList != null) {
            listenerIter = listenerList.iterator();
            while (listenerIter.hasNext()) {
                EventListener listener = (EventListener) listenerIter.next();

                ListenerThread listenerThread = (ListenerThread) m_listenerThreads.get(listener.getName());
                try {
                    listenerThread.getQueue().add(event);
                    if (log.isDebugEnabled())
                        log.debug("Queued event "+uei+" to listener: " + listener.getName());
                } catch (FifoQueueException fe) {
                    log.error("Error queueing event " + event.getUei() + " to listener thread " + listener.getName(), fe);
                } catch (InterruptedException ie) {
                    log.error("Error queueing event " + event.getUei() + " to listener thread " + listener.getName(), ie);
                }
            }
        } else {
            if (log.isDebugEnabled())
                log.debug("No listener interested in event: " + uei);
        }
        //Try wildcards: Find / before last character
        int i = uei.lastIndexOf("/",uei.length()-2);
        if (i > 0){
           //Split at "/", including the /
           uei = uei.substring (0, i+1);
        } else {
           //No more wildcards to match
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
        ListenerThread listenerThread = (ListenerThread) m_listenerThreads.get(listener.getName());
        if (listenerThread == null) {
            FifoQueue lq = new FifoQueueImpl();
            listenerThread = new ListenerThread(listener, lq);
            listenerThread.start();

            m_listenerThreads.put(listener.getName(), listenerThread);
        }

        // add listener to list of listeners listening for all events
        m_listeners.add(listener);

        // remove listener from uei specific listeners
        Iterator keysetIter = m_ueiListeners.keySet().iterator();
        while (keysetIter.hasNext()) {
            String key = (String) keysetIter.next();

            List listenersList = (List) m_ueiListeners.get(key);
            if (listenersList != null) {
                listenersList.remove(listener);
            }
        }
    }

    /**
     * Register an event listener interested in the UEIs in the passed list
     */
    public synchronized void addEventListener(EventListener listener, List ueilist) {
        Category log = ThreadCategory.getInstance(this.getClass());
        if (log.isDebugEnabled())
            log.debug("Adding event listener " + listener.getName() + " for " + ueilist);

        if (ueilist == null || ueilist.size() == 0) {
            // nothing to do
            return;
        }

        // create a new queue and listener thread for this listener
        ListenerThread listenerThread = (ListenerThread) m_listenerThreads.get(listener.getName());
        if (listenerThread == null) {
            FifoQueue lq = new FifoQueueImpl();
            listenerThread = new ListenerThread(listener, lq);
            listenerThread.start();

            m_listenerThreads.put(listener.getName(), listenerThread);
        }

        // add to uei listeners
        Iterator ueiIter = ueilist.iterator();
        while (ueiIter.hasNext()) {
            String uei = (String) ueiIter.next();

            // check if there are other listeners already, else create
            // an entry
            List listenersList = (List) m_ueiListeners.get(uei);
            if (listenersList == null) {
                listenersList = new ArrayList();
                listenersList.add(listener);

                m_ueiListeners.put(uei, listenersList);
            } else {
                if (!listenersList.contains(listener))
                    listenersList.add(listener);
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
        ListenerThread listenerThread = (ListenerThread) m_listenerThreads.get(listener.getName());
        if (listenerThread == null) {
            FifoQueue lq = new FifoQueueImpl();
            listenerThread = new ListenerThread(listener, lq);
            listenerThread.start();

            m_listenerThreads.put(listener.getName(), listenerThread);
        }

        // check if there are other listeners already, else create
        // an entry
        List listenersList = (List) m_ueiListeners.get(uei);
        if (listenersList == null) {
            listenersList = new ArrayList();
            listenersList.add(listener);

            m_ueiListeners.put(uei, listenersList);
        } else {
            if (!listenersList.contains(listener))
                listenersList.add(listener);
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
    public synchronized void removeEventListener(EventListener listener, List ueilist) {
        if (ueilist == null || ueilist.size() == 0) {
            // nothing to do
            return;
        }

        // Iterate through the ueis and remove the listener
        Iterator ueiIter = ueilist.iterator();
        while (ueiIter.hasNext()) {
            String uei = (String) ueiIter.next();

            List listenersList = (List) m_ueiListeners.get(uei);
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

        List listenersList = (List) m_ueiListeners.get(uei);
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
        Iterator keysetIter = m_ueiListeners.keySet().iterator();
        while (keysetIter.hasNext()) {
            String key = (String) keysetIter.next();

            List listenersList = (List) m_ueiListeners.get(key);
            if (listenersList != null) {
                listenersList.remove(listener);
            }
        }

        // stop the listener thread for this listener
        ListenerThread listenerThread = (ListenerThread) m_listenerThreads.get(listener.getName());
        if (listenerThread != null) {
            listenerThread.stop();

            m_listenerThreads.remove(listener.getName());
        }

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
    
}
