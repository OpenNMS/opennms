/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.adaptors.udp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 * 
 */
class UdpReceiver implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(UdpReceiver.class);
    
    /**
     * The list of incoming events.
     */
    private List<UdpReceivedEvent> m_eventsIn;

    /**
     * The Fiber's status.
     */
    private volatile boolean m_stop;

    /**
     * The UDP socket for receipt and transmission of packets from agents.
     */
    private DatagramSocket m_dgSock;

    /**
     * The context thread
     */
    private Thread m_context;

    /**
     * The log prefix
     */
    private String m_logPrefix;

    /**
     * construct a new receiver
     */
    UdpReceiver(DatagramSocket sock, List<UdpReceivedEvent> xchange) {
        m_eventsIn = xchange;
        m_stop = false;
        m_dgSock = sock;
        m_logPrefix = org.opennms.netmgt.eventd.Eventd.LOG4J_CATEGORY;
    }

    /**
     * stop the current receiver
     */
    void stop() throws InterruptedException {
        m_stop = true;
        if (m_context != null) {
            LOG.debug("Stopping and joining thread context {}", m_context.getName());

            m_context.interrupt();
            m_context.join();

            LOG.debug("Thread context stopped and joined");
        }
    }

    /**
     * Return true if this receiver is alive
     */
    boolean isAlive() {
        return (m_context == null ? false : m_context.isAlive());
    }

    /**
     * The execution context.
     */
    @Override
    public void run() {
        // get the context
        m_context = Thread.currentThread();

        // Get a log instance
        Logging.putPrefix(m_logPrefix);
        
        if (m_stop) {
            LOG.debug("Stop flag set before thread started, exiting");
            return;
        } else {
            LOG.debug("Thread context started");
        }

        // allocate a buffer
        final int length = 0xffff;
        final byte[] buffer = new byte[length];
        DatagramPacket pkt = new DatagramPacket(buffer, length);

        // Set an SO timout to make sure we don't block forever if a socket is closed.
        try {
            LOG.debug("Setting socket timeout to 500ms");

            m_dgSock.setSoTimeout(500);
        } catch (SocketException e) {
            LOG.warn("An I/O error occured while trying to set the socket timeout", e);
        }

        // Increase the receive buffer for the socket
        try {
            LOG.debug("Setting receive buffer size to {}", length);

            m_dgSock.setReceiveBufferSize(length);
        } catch (SocketException e) {
            LOG.info("Failed to set the receive buffer to {}", length, e);
        }

        // set to avoid numerious tracing message
        boolean ioInterrupted = false;

        // now start processing incoming request
        while (!m_stop) {
            if (m_context.isInterrupted()) {
                LOG.debug("Thread context interrupted");

                break;
            }

            try {
                if (!ioInterrupted) {
                    LOG.debug("Wating on a datagram to arrive");
                }

                m_dgSock.receive(pkt);
                ioInterrupted = false; // reset the flag
            } catch (InterruptedIOException e) {
                ioInterrupted = true;
                continue;
            } catch (IOException e) {
                LOG.error("An I/O exception occured on the datagram receipt port, exiting", e);
                break;
            }

            LOG.debug("Sending received packet to processor");

            UdpReceivedEvent re = UdpReceivedEvent.make(pkt);
            synchronized (m_eventsIn) {
                m_eventsIn.add(re);
                m_eventsIn.notify();
            }

            pkt = new DatagramPacket(buffer, length);

        }

        LOG.debug("Thread context exiting");

    }

    void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }
}
