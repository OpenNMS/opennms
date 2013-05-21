/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.adaptors.udp;

import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.adaptors.EventHandler;
import org.opennms.netmgt.xml.event.Event;

/**
 * This class encapsulates the execution context for processing events received
 * via UDP from remote agents. This is a separate event context to allow the
 * event receiver to do minimum work to avoid dropping packets from the agents.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 * 
 */
final class UdpProcessor implements Runnable {
    /**
     * The UDP receiver thread.
     */
    private Thread m_context;

    /**
     * The list of incoming events.
     */
    private List<UdpReceivedEvent> m_eventsIn;

    /**
     * The list of outgoing event-receipts by UUID.
     */
    private List<UdpReceivedEvent> m_eventUuidsOut;

    /**
     * The list of registered event handlers.
     */
    private List<EventHandler> m_handlers;

    /**
     * The stop flag
     */
    private volatile boolean m_stop;

    /**
     * The log prefix
     */
    private String m_logPrefix;

    UdpProcessor(List<EventHandler> handlers, List<UdpReceivedEvent> in, List<UdpReceivedEvent> out) {
        m_context = null;
        m_stop = false;
        m_eventsIn = in;
        m_eventUuidsOut = out;
        m_handlers = handlers;
        m_logPrefix = org.opennms.netmgt.eventd.Eventd.LOG4J_CATEGORY;
    }

    /**
     * Returns true if the thread is still alive
     */
    boolean isAlive() {
        return (m_context == null ? false : m_context.isAlive());
    }

    /**
     * Stops the current context
     */
    void stop() throws InterruptedException {
        m_stop = true;
        if (m_context != null) {
            ThreadCategory log = log();
            if (log.isDebugEnabled())
                log.debug("Stopping and joining thread context " + m_context.getName());

            m_context.interrupt();
            m_context.join();

            if (log.isDebugEnabled())
                log.debug("Thread context stopped and joined");
        }
    }

    /**
     * The event processing execution context.
     */
    @Override
    public void run() {
        // The runnable context
        m_context = Thread.currentThread();

        // get a logger
        ThreadCategory.setPrefix(m_logPrefix);
        if (m_stop) {
            log().debug("Stop flag set before thread started, exiting");
            return;
        } else {
            log().debug("Thread context started");
        }

        /*
         * This loop is labeled so that it can be
         * exited quickly when the thread is interrupted
         */
        RunLoop: while (!m_stop) {
            log().debug("Waiting on a new datagram to arrive");

            UdpReceivedEvent re = null;
            synchronized (m_eventsIn) {
                // wait for an event to show up.  wait in 1/2 second intervals
                while (m_eventsIn.isEmpty()) {
                    try {
                        m_eventsIn.wait(500);
                    } catch (InterruptedException ie) {
                        log().debug("Thread interrupted");
                        break RunLoop;
                    }

                    if (m_stop) {
                        log().debug("Stop flag is set");
                        break RunLoop;
                    }
                }
                re = m_eventsIn.remove(0);
            }

            log().debug("A new request has arrived");

            // Convert the Event
            Event[] events = null;
            try {
                if (log().isDebugEnabled()) {
                    log().debug("Event from " + InetAddressUtils.str(re.getSender()) + ":" + re.getPort());
                    log().debug("Unmarshalling Event text {" + System.getProperty("line.separator") + re.getXmlData() + System.getProperty("line.separator") + "}");
                }
                events = re.unmarshal().getEvents().getEvent();
            } catch (MarshalException e) {
                log().warn("Failed to unmarshal the event from " + InetAddressUtils.str(re.getSender()) + ":" + re.getPort() + ": " + e, e);
                continue;
            } catch (ValidationException e) {
                log().warn("Failed to validate the event from " + InetAddressUtils.str(re.getSender()) + ":" + re.getPort() + ": " + e, e);
                continue;
            }

            if (events == null || events.length == 0) {
                log().debug("The event log record contained no events");
                continue;
            } else if (log().isDebugEnabled()) {
                log().debug("Processing " + events.length + " events");
            }

            // process the event
            synchronized (m_handlers) {
                /*
                 * Get the list of events from the event log.
                 * Also, get an iterator to walk over the set
                 * of event handlers.
                 */
                for (EventHandler handler : m_handlers) {
                    // iterate over the list of the events for the received events
                    for (int ndx = 0; ndx < events.length; ndx++) {
                        try {
                            /*
                             * shortcut and, both sides of the and statment WILL
                             * execute regardless of the other side's value
                             */
                            if (handler.processEvent(events[ndx])) {
                                re.ackEvent(events[ndx]);
                            }
                        } catch (Throwable t) {
                            log().warn("Failed to process received UDP event, exception follows", t);
                        }
                    }
                }
            }

            log().debug("event processing complete, forwarding to receipt generator");

            synchronized (m_eventUuidsOut) {
                m_eventUuidsOut.add(re);
                // Don't notify, let them batch up!
            }
        }

        log().debug("Context finished, returning");
    }

    void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}

