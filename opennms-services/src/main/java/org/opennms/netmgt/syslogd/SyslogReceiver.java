//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 2009 Mar 23: Add support for discarding messages. - jeffg@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.syslogd;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.UeiList;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.oculan.com">Oculan Corporation</a>
 * @fiddler joed
 */
class SyslogReceiver implements Runnable {

    private static final int SOCKET_TIMEOUT = 500;
    
    private static final String LOG4J_CATEGORY = "OpenNMS.Syslogd";

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

    private String m_matchPattern;

    private int m_hostGroup;

    private int m_messageGroup;
    
    private String m_discardUei;

    private UeiList m_UeiList;

    private HideMessage m_HideMessages;

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
        m_logPrefix = LOG4J_CATEGORY;

    }

    /*
     * stop the current receiver
     * @throws InterruptedException
     * 
     */
    void stop() throws InterruptedException {
        m_stop = true;
        if (m_context != null) {
            Category log = ThreadCategory.getInstance(getClass());
            log.debug("Stopping and joining thread context " + m_context.getName());
            m_context.interrupt();
            m_context.join();
            log.debug("Thread context stopped and joined");
        }
    }

    /**
     * Return true if this receiver is alive
     *
     * @return boolean
     */
    boolean isAlive() {
        return (m_context != null && m_context.isAlive());
    }

    /**
     * The execution context.
     */
    public void run() {
        // get the context
        m_context = Thread.currentThread();

        // Get a log instance
        ThreadCategory.setPrefix(m_logPrefix);
        Category log = ThreadCategory.getInstance(getClass());

        if (m_stop) {
            log.debug("Stop flag set before thread started, exiting");
            return;
        } else
            log.debug("Thread context started");

        // allocate a buffer
        final int length = 0xffff;
        final byte[] buffer = new byte[length];

        // set an SO timeout to make sure we don't block forever
        // if a socket is closed.
        try {
            log.debug("Setting socket timeout to " + SOCKET_TIMEOUT + "ms");
            m_dgSock.setSoTimeout(SOCKET_TIMEOUT);
        } catch (SocketException e) {
            log.warn("An I/O error occured while trying to set the socket timeout", e);
        }

        // Increase the receive buffer for the socket
        try {
            log.debug("Setting receive buffer size to " + length);
            m_dgSock.setReceiveBufferSize(length);
        } catch (SocketException e) {
            log.info("Failed to set the receive buffer to " + length, e);
        }
        // set to avoid numerous tracing message
        boolean ioInterrupted = false;
        // now start processing incoming requests
        while (!m_stop) {
            if (m_context.isInterrupted()) {
                log.debug("Thread context interrupted");
                break;
            }

            try {
                if (!ioInterrupted) {
                    log.debug("Wating on a datagram to arrive");
                }

                DatagramPacket pkt = new DatagramPacket(buffer, length);
                m_dgSock.receive(pkt);

                //SyslogConnection *Must* copy packet data and InetAddress as DatagramPacket is a mutable type
                Thread worker = new Thread(new SyslogConnection(pkt, m_matchPattern, m_hostGroup, m_messageGroup, m_UeiList, m_HideMessages, m_discardUei));
                worker.start();
                ioInterrupted = false; // reset the flag
            } catch (SocketTimeoutException e) {
                ioInterrupted = true;
                continue;
            } catch (InterruptedIOException e) {
                ioInterrupted = true;
                continue;
            } catch (IOException e) {
                log.error("An I/O exception occured on the datagram receipt port, exiting", e);
                break;
            }

        } // end while status OK

        log.debug("Thread context exiting");

    }

    protected void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }
}
