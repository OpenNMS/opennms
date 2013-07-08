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

package org.opennms.netmgt.syslogd;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.concurrent.WaterfallExecutor;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.UeiList;

/**
 * @deprecated This class should be combined with {@link SyslogHandler}
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.oculan.com">Oculan Corporation</a>
 * @fiddler joed
 */
class SyslogReceiver implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogReceiver.class);

    private static final int SOCKET_TIMEOUT = 500;

    /**
     * The Fiber's status.
     */
    private volatile boolean m_stop;

    /**
     * The UDP socket for receipt and transmission of packets from agents.
     */
    private final DatagramSocket m_dgSock;

    /**
     * The context thread
     */
    private Thread m_context;

    private final String m_matchPattern;

    private final int m_hostGroup;

    private final int m_messageGroup;
    
    private final String m_discardUei;

    private final UeiList m_UeiList;

    private final HideMessage m_HideMessages;

    private final List<ExecutorService> m_executors = new ArrayList<ExecutorService>();

    /**
     * construct a new receiver
     *
     * @param sock
     * @param matchPattern
     * @param hostGroup
     * @param messageGroup
     */
    SyslogReceiver(DatagramSocket sock, String matchPattern, int hostGroup, int messageGroup,
                   UeiList ueiList, HideMessage hideMessages, String discardUei) {
        m_stop = false;
        m_dgSock = sock;
        m_matchPattern = matchPattern;
        m_hostGroup = hostGroup;
        m_messageGroup = messageGroup;
        m_discardUei = discardUei;
        m_UeiList = ueiList;
        m_HideMessages = hideMessages;

        m_executors.add(new ThreadPoolExecutor(
            1,
            Integer.MAX_VALUE,
            100L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new LogPreservingThreadFactory(getClass().getSimpleName(), Integer.MAX_VALUE, false)
        ));

        m_executors.add(new ThreadPoolExecutor(
            1,
            Integer.MAX_VALUE,
            100L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new LogPreservingThreadFactory(getClass().getSimpleName(), Integer.MAX_VALUE, false)
        ));
}

    /*
     * stop the current receiver
     * @throws InterruptedException
     * 
     */
    void stop() throws InterruptedException {
        m_stop = true;

        // Shut down the thread pools that are executing SyslogConnection and SyslogProcessor tasks
        for (ExecutorService service : m_executors) {
            service.shutdown();
        }

        if (m_context != null) {
            LOG.debug("Stopping and joining thread context {}", m_context.getName());
            m_context.interrupt();
            m_context.join();
            LOG.debug("Thread context stopped and joined");
        }
    }

    /**
     * The execution context.
     */
    @Override
    public void run() {
        // get the context
        m_context = Thread.currentThread();

        // Get a log instance
        Logging.putPrefix(Syslogd.LOG4J_CATEGORY);

        if (m_stop) {
            LOG.debug("Stop flag set before thread started, exiting");
            return;
        } else
            LOG.debug("Thread context started");

        // allocate a buffer
        final int length = 0xffff;
        final byte[] buffer = new byte[length];

        // set an SO timeout to make sure we don't block forever
        // if a socket is closed.
        try {
            LOG.debug("Setting socket timeout to {}ms", SOCKET_TIMEOUT);
            m_dgSock.setSoTimeout(SOCKET_TIMEOUT);
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
        // set to avoid numerous tracing message
        boolean ioInterrupted = false;
        // now start processing incoming requests
        while (!m_stop) {
            if (m_context.isInterrupted()) {
                LOG.debug("Thread context interrupted");
                break;
            }

            try {
                if (!ioInterrupted) {
                    LOG.debug("Waiting on a datagram to arrive");
                }

                DatagramPacket pkt = new DatagramPacket(buffer, length);
                m_dgSock.receive(pkt);

                //SyslogConnection *Must* copy packet data and InetAddress as DatagramPacket is a mutable type
                WaterfallExecutor.waterfall(m_executors, new SyslogConnection(pkt, m_matchPattern, m_hostGroup, m_messageGroup, m_UeiList, m_HideMessages, m_discardUei));
                ioInterrupted = false; // reset the flag
            } catch (SocketTimeoutException e) {
                ioInterrupted = true;
                continue;
            } catch (InterruptedIOException e) {
                ioInterrupted = true;
                continue;
            } catch (ExecutionException e) {
                LOG.error("Task execution failed in {}", this.getClass().getSimpleName(), e);
                break;
            } catch (InterruptedException e) {
                LOG.error("Task interrupted in {}", this.getClass().getSimpleName(), e);
                break;
            } catch (IOException e) {
                LOG.error("An I/O exception occured on the datagram receipt port, exiting", e);
                break;
            }

        } // end while status OK

        LOG.debug("Thread context exiting");

    }

    /**
     * <p>setLogPrefix</p>
     *
     * @param prefix a {@link java.lang.String} object.
     */
    protected void setLogPrefix(String prefix) {
    }
}
