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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.eventd.adaptors.EventHandler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.springframework.dao.DataAccessException;

/**
 * This class implements the User Datagram Protocol (UDP) event receiver. When
 * the an agent sends an event via UDP/IP the receiver will process the event
 * and then add the UUIDs to the internal list. If the event is successfully
 * processed then an event-receipt is returned to the caller.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 * 
 */
final class UdpUuidSender implements Runnable {
    /**
     * The list of outgoing event-receipts by UUID.
     */
    private List<UdpReceivedEvent> m_eventUuidsOut;

    /**
     * The stop flag
     */
    private volatile boolean m_stop;

    /**
     * The UDP socket for receipt and transmission of packets from agents.
     */
    private DatagramSocket m_dgSock;

    /**
     * The thread context
     */
    private Thread m_context;

    /**
     * The list of handlers
     */
    private List<EventHandler> m_handlers;

    /**
     * The log prefix
     */
    private String m_logPrefix;

    /**
     * Constructs a new instance of this runnable.
     */
    UdpUuidSender(DatagramSocket sock, List<UdpReceivedEvent> uuidsOut, List<EventHandler> handlers) {
        m_context = null;
        m_dgSock = sock;
        m_stop = false;
        m_eventUuidsOut = uuidsOut;
        m_handlers = handlers;
        m_logPrefix = org.opennms.netmgt.eventd.Eventd.LOG4J_CATEGORY;
    }

    /**
     * Stops the current context.
     */
    void stop() throws InterruptedException {
        m_stop = true;
        if (m_context != null) {
            if (log().isDebugEnabled()) {
                log().debug("Stopping and joining thread context " + m_context.getName());
            }

            m_context.interrupt();
            m_context.join();

            log().debug("Thread context stopped and joined");
        }
    }

    /**
     * Returns true if the runnable is still running in its context
     */
    boolean isAlive() {
        return (m_context == null ? false : m_context.isAlive());
    }

    /**
     * <p>run</p>
     */
    @Override
    public void run() {
        // get the context
        m_context = Thread.currentThread();

        // get a logger
        ThreadCategory.setPrefix(m_logPrefix);
        boolean isTracing = log().isDebugEnabled();

        if (m_stop) {
            log().debug("Stop flag set before thread started, exiting");
            return;
        } else {
            log().debug("Thread context started");
        }

        /*
         * This loop is labeled so that it can be
         * exited quickly when the thread is interrupted.
         */
        List<UdpReceivedEvent> eventHold = new ArrayList<UdpReceivedEvent>(30);
        Map<UdpReceivedEvent, EventReceipt> receipts = new HashMap<UdpReceivedEvent, EventReceipt>();

        RunLoop: while (!m_stop) {
            log().debug("Waiting on event receipts to be generated");

            synchronized (m_eventUuidsOut) {
                // wait for an event to show up.  wait in 1 second intervals
                while (m_eventUuidsOut.isEmpty()) {
                    try {
                        // use wait instead of sleep to release the lock!
                        m_eventUuidsOut.wait(1000);
                    } catch (InterruptedException ie) {
                        log().debug("Thread context interrupted");
                        break RunLoop;
                    }
                }

                eventHold.addAll(m_eventUuidsOut);
                m_eventUuidsOut.clear();
            }

            if (isTracing) {
                log().debug("Received " + eventHold.size() + " event receipts to process");
                log().debug("Processing receipts");
            }

            // build an event-receipt
            for (UdpReceivedEvent re : eventHold) {
                for (Event e : re.getAckedEvents()) {
                    if (e.getUuid() != null) {
                        EventReceipt receipt = receipts.get(re);
                        if (receipt == null) {
                            receipt = new EventReceipt();
                            receipts.put(re, receipt);
                        }
                        receipt.addUuid(e.getUuid());
                    }
                }
            }
            eventHold.clear();

            log().debug("Event receipts sorted, transmitting receipts");

            // turn them into XML and send it out the socket
            for (Map.Entry<UdpReceivedEvent, EventReceipt> entry : receipts.entrySet()) {
                UdpReceivedEvent re = entry.getKey();
                EventReceipt receipt = entry.getValue();

                StringWriter writer = new StringWriter();
                try {
                    JaxbUtils.marshal(receipt, writer);
                } catch (DataAccessException e) {
                    log().warn("Failed to build event receipt for agent " + InetAddressUtils.str(re.getSender()) + ":" + re.getPort() + ": " + e, e);
                }

                String xml = writer.getBuffer().toString();
                try {
                    byte[] xml_bytes = xml.getBytes("US-ASCII");
                    DatagramPacket pkt = new DatagramPacket(xml_bytes, xml_bytes.length, re.getSender(), re.getPort());

                    if (isTracing) {
                        log().debug("Transmitting receipt to destination " + InetAddressUtils.str(re.getSender()) + ":" + re.getPort());
                    }

                    m_dgSock.send(pkt);
                    
                    synchronized (m_handlers) {
                        for (EventHandler handler : m_handlers) {
                            try {
                                handler.receiptSent(receipt);
                            } catch (Throwable t) {
                                log().warn("Error processing event receipt: "+ t, t);
                            }
                        }
                    }

                    if (isTracing) {
                        log().debug("Receipt transmitted OK {");
                        log().debug(xml);
                        log().debug("}");
                    }
                } catch (UnsupportedEncodingException e) {
                    log().warn("Failed to convert XML to byte array: " + e, e);
                } catch (IOException e) {
                    log().warn("Failed to send packet to host" + InetAddressUtils.str(re.getSender()) + ":" + re.getPort() + ": " + e, e);
                }
            }
            
            receipts.clear();
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

