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

package org.opennms.netmgt.eventd.adaptors.tcp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.eventd.adaptors.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implement the server features necessary to receive events from
 * incoming connections.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http;//www.opennms.org">OpenNMS </a>
 * 
 */
final class TcpServer implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(TcpServer.class);
    
    /**
     * The default TCP/IP port where the server listens for connections. Each
     * connection to the server will be processed by its own thread.
     */
    static final int TCP_PORT = 5817;

    /**
     * The default IP address where the server listens for connections.
     */
    static final String DEFAULT_IP_ADDRESS = "127.0.0.1";

    /**
     * The TCP/IP Port for the server socket's binding. By default this should
     * be equal to {@link #TCP_PORT TCP_PORT}but it can be overridden in the
     * constructor.
     */
    private int m_tcpPort;

    /**
     * The TCP/IP Port for the server socket's binding. By default this should
     * be equal to {@link #TCP_PORT TCP_PORT}but it can be overridden in the
     * constructor.
     */
    private ServerSocket m_tcpSock;

    /**
     * When set true the server thread will exit.
     */
    private volatile boolean m_stop;

    /**
     * <p>
     * The list of receivers that are currently being processed. Each instance
     * in this list is of type {@link java.lang.Thread Thread}and will remain
     * in the list so long as it's alive.
     * </p>
     * 
     * <p>
     * This list is periodically cleaned by the main server thread.
     * </p>
     */
    private List<TcpStreamHandler> m_receivers;

    /**
     * The thread which is executing the server context
     */
    private Thread m_context;

    /**
     * The parent fiber.
     */
    private Fiber m_parent;

    /**
     * The list of event handlers
     */
    private List<EventHandler> m_handlers;

    /**
     * The logging context
     */
    private String m_logPrefix;

    /**
     * the events per connection
     */
    private int m_recsPerConn;

    private InetAddress m_ipAddress;

    /**
     * Constructs a new instance of an server to handle incoming tcp
     * connections.
     *
     * @param parent
     *            The parent fiber
     * @param handlers a {@link java.util.List} object.
     * @throws java.io.IOException if any.
     */
    public TcpServer(Fiber parent, List<EventHandler> handlers) throws IOException {
        this(parent, handlers, TCP_PORT, InetAddressUtils.addr(DEFAULT_IP_ADDRESS));
    }

    /**
     * Constructs a new instance of an server to handle incoming TCP
     * connections.
     *
     * @param parent
     *            The parent fiber
     * @param port
     *            The port to listen on.
     * @param address TODO
     * @param handlers a {@link java.util.List} object.
     * @throws java.io.IOException if any.
     */
    public TcpServer(Fiber parent, List<EventHandler> handlers, int port, InetAddress address) throws IOException {
        m_parent = parent;
        m_tcpPort = port;
        m_ipAddress = address;
        m_receivers = new LinkedList<TcpStreamHandler>();
        m_stop = false;
        m_context = null;
        m_handlers = handlers;
        m_logPrefix = org.opennms.netmgt.eventd.Eventd.LOG4J_CATEGORY;
        m_recsPerConn = TcpEventReceiver.UNLIMITED_EVENTS;

        try {
            m_tcpSock = new ServerSocket(m_tcpPort, 0, m_ipAddress);

            // Set SO_REUSEADDR so that we don't run into problems in
            // unit tests trying to rebind to an address where other tests
            // also bound. This shouldn't have any effect at runtime.
            try {
                LOG.debug("Setting socket SO_REUSEADDR to true");
                m_tcpSock.setReuseAddress(true);
            } catch (SocketException e) {
                LOG.warn("An I/O error occured while trying to set SO_REUSEADDR", e);
            }

        } catch (IOException e) {
            IOException n = new IOException("Could not create listening TCP socket on " + m_ipAddress + ":" + m_tcpPort + ": " + e);
            n.initCause(e);
            throw n;
        }
    }

    /**
     * This is called inform the current execution of this object is stopped.
     * Once called the object cannot be reused in another thread.
     *
     * @throws java.lang.InterruptedException if any.
     */
    public void stop() throws InterruptedException {
        LOG.debug("stop method invoked");

        // Stop this context
        m_stop = true;
        if (m_context != null) {
            LOG.debug("Interrupting and joining context thread {}", m_context.getName());

            m_context.interrupt();
            m_context.join();

            LOG.debug("Thread context stopped and joined {}", m_context.getName());

            m_context = null;
        }

        LOG.debug("Attempting to stop and join all stream handlers");
        LOG.debug("There are {} receivers", m_receivers.size());

        // stop all the receivers
        int ndx = 0; // for tracing!
        Iterator<TcpStreamHandler> i = m_receivers.iterator();
        while (i.hasNext()) {
            TcpStreamHandler t = i.next();
            if (t.isAlive()) {
                LOG.debug("Calling stop on handler index {}", ndx);

                t.stop();

                LOG.debug("Stopped handler index {}", ndx);
            }
            ndx++;
            i.remove();
        }

        LOG.debug("All TCP Handlers are stopped and removed");
    }

    /**
     * Returns true if this runnable is executing.
     *
     * @return a boolean.
     */
    public boolean isAlive() {
        boolean rc = false;
        if (m_context != null) {
            rc = m_context.isAlive();
        }

        return rc;
    }

    /**
     * The logic execution context to accept and process incoming connection
     * requests. When a new connection is received a new thread of control is
     * created to process the connection. This method encapsulates that control
     * logic so that it can be executed in it's own java thread.
     */
    @Override
    public void run() {
        // get the thread context for the ability to stop the process
        m_context = Thread.currentThread();
        synchronized (m_context) {
            m_context.notifyAll();
        }

        // get the log information
        Logging.putPrefix(m_logPrefix);
        
        // check to see if the thread has already been stopped.
        if (m_stop) {
            LOG.debug("Stop flag set on thread startup");

            try {
                if (m_tcpSock != null) {
                    m_tcpSock.close();
                }

                LOG.debug("The socket has been closed");
            } catch (Throwable e) {
                LOG.warn("An exception occured closing the socket", e);
            }

            LOG.debug("Thread exiting");

            return;
        }

        LOG.debug("Server connection processor started on {}:{}", m_ipAddress, m_tcpPort);

        /*
         *
         * Set the initial timeout on the socket. This allows
         * the thread to wake up every 1/2 second and check the
         * shutdown status.
         */
        try {
            m_tcpSock.setSoTimeout(500);
        } catch (SocketException e) {
            if (!m_stop) {
                LOG.warn("An I/O exception occured setting the socket timeout", e);
            }

            LOG.debug("Thread exiting due to socket error", e);

            return;
        }
        
        // used to avoid seeing the trace message repeatedly
        boolean ioInterrupted = false;

        /*
         * Check the status of the fiber and respond
         * correctly. When the fiber enters a STOPPED or
         * STOP PENDING state then shutdown occurs by exiting
         * the while loop
         */
        while (m_parent.getStatus() != Fiber.STOPPED && m_parent.getStatus() != Fiber.STOP_PENDING && !m_stop) {
            try {
                if (!ioInterrupted) {
                    LOG.debug("Waiting for new connection");
                }

                /*
                 * Get the newbie socket connection from the client.
                 * After accepting the connection start up a thread
                 * to process the request
                 */
                Socket newbie = m_tcpSock.accept();
                ioInterrupted = false; // reset the flag

                // build a connection string for the thread identifier
                StringBuffer connection = new StringBuffer(InetAddressUtils.str(newbie.getInetAddress()));
                connection.append(":").append(newbie.getPort());

                LOG.debug("New connection accepted from {}", connection);

                // start a new handler
                TcpStreamHandler handler = new TcpStreamHandler(m_parent, newbie, m_handlers, m_recsPerConn);
                Thread processor = new Thread(handler, m_parent.getName() + "[" + connection + "]");
                synchronized (processor) {
                    processor.start();
                    try {
                        processor.wait();
                    } catch (InterruptedException e) {
                        LOG.warn("The thread was interrupted", e);
                    }
                }

                LOG.debug("A new stream handler thread has been started");

                // add the handler to the list
                m_receivers.add(handler);
            } catch (InterruptedIOException e) {
                /*
                 * do nothing on interrupted I/O
                 * DON'T Continue, the end of the loop
                 * checks and removes terminated threads
                 */
                ioInterrupted = true;
            } catch (IOException e) {
                LOG.error("Server Socket I/O Error", e);
                break;
            }

            /*
             * Go through the threads in the list of
             * receivers and find the dead ones. When
             * they are no longer alive just remove them
             * from the list.
             */
            Iterator<TcpStreamHandler> i = m_receivers.iterator();
            while (i.hasNext()) {
                TcpStreamHandler t = i.next();
                if (!t.isAlive()) {
                    i.remove();
                }
            }
        }

        // Either a fatal I/O error has occured or the service has been stopped.
        try {
            LOG.debug("closing the server socket connection");

            m_tcpSock.close();
        } catch (Throwable t) {
            LOG.error("An I/O Error Occcured Closing the Server Socket", t);
        }

        // Log the termination of this runnable
        LOG.debug("TCP Server Shutdown");
    }

    /**
     * <p>setLogPrefix</p>
     *
     * @param prefix a {@link java.lang.String} object.
     */
    public void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }

    /**
     * <p>setEventsPerConnection</p>
     *
     * @param number a int.
     */
    public void setEventsPerConnection(int number) {
        m_recsPerConn = number;
    }
}
