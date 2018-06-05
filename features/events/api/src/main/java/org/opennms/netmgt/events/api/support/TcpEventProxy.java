/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api.support;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the interface used to send events into the event subsystem - It is
 * typically used by the poller framework plugins that perform service
 * monitoring to send out appropriate events. Can also be used by capsd,
 * discovery etc.
 * 
 * @deprecated Use ActiveMQ JMS instead of a custom TCP protocol.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public final class TcpEventProxy implements EventProxy {
	
    private static final Logger LOG = LoggerFactory.getLogger(TcpEventProxy.class);
	
    /** Constant <code>DEFAULT_PORT=5817</code> */
    public static final int DEFAULT_PORT = 5817;

    /** Constant <code>DEFAULT_TIMEOUT=2000</code> */
    public static final int DEFAULT_TIMEOUT = 2000;

    private InetSocketAddress m_address;
    
    private int m_timeout = DEFAULT_TIMEOUT;

    /**
     * <p>Constructor for TcpEventProxy.</p>
     *
     * @throws java.net.UnknownHostException if any.
     */
    public TcpEventProxy() throws UnknownHostException {
        this(new InetSocketAddress(InetAddressUtils.addr("127.0.0.1"), DEFAULT_PORT));
    }

    /**
     * <p>Constructor for TcpEventProxy.</p>
     *
     * @param address a {@link java.net.InetSocketAddress} object.
     */
    public TcpEventProxy(InetSocketAddress address) {
        m_address = address;
    }

    /**
     * <p>Constructor for TcpEventProxy.</p>
     *
     * @param address a {@link java.net.InetSocketAddress} object.
     * @param timeout a int.
     */
    public TcpEventProxy(InetSocketAddress address, int timeout) {
        this(address);
        m_timeout = timeout;
    }

    /**
     * {@inheritDoc}
     *
     * This method is called to send the event out
     * @exception UndeclaredThrowableException
     *                thrown if the send fails for any reason
     */
    @Override
    public void send(Event event) throws EventProxyException {
        Log elog = new Log();
        Events events = new Events();
        events.addEvent(event);
        elog.setEvents(events);

        send(elog);
    }

    /**
     * This method is called to send an event log containing multiple events
     * out.
     *
     * @param eventLog
     *            the events to be sent out
     * @exception UndeclaredThrowableException
     *                thrown if the send fails for any reason
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    @Override
    public void send(Log eventLog) throws EventProxyException {
        try (Connection connection = new Connection(m_address, m_timeout)) {
            final Writer writer = connection.getWriter();
            JaxbUtils.marshal(eventLog, writer);
            writer.flush();
        } catch (ConnectException e) {
            throw new EventProxyException("Could not connect to event daemon " + m_address + " to send event: " + e.getMessage(), e);
        } catch (Throwable e) {
            throw new EventProxyException("Unknown exception while sending event: " + e, e);
        }
    }

    private static class Connection implements AutoCloseable {
        private Socket m_sock;

        private Writer m_writer;
        
        private InputStream m_input;

        private Thread m_rdrThread;

        public Connection(InetSocketAddress address, int timeout) throws IOException {
            m_sock = new Socket();
            m_sock.connect(address, timeout);
            m_sock.setSoTimeout(500);
            LOG.debug("Default Charset: {}", Charset.defaultCharset().displayName());
            LOG.debug("Setting Charset: UTF-8");
            m_writer = new OutputStreamWriter(new BufferedOutputStream(m_sock.getOutputStream()), StandardCharsets.UTF_8);
            m_input = m_sock.getInputStream();
            m_rdrThread = new Thread("TcpEventProxy Input Discarder") {
                @Override
                public void run() {
                    for (int ch = 0; ch != -1; ) {
                        try {
                            ch = m_input.read();
                        } catch (InterruptedIOException e) {
                            ch = 0;
                        } catch (IOException e) {
                            ch = -1;
                        }
                    }
                }
            };

            m_rdrThread.setDaemon(true);
            m_rdrThread.start();
        }

        public Writer getWriter() {
            return m_writer;
        }

        @Override
        public void close() {
            if (m_sock != null) {
                try {
                    LOG.debug("Closing socket {}", m_sock);
                    m_sock.close();
                } catch (IOException e) {
                    LOG.warn("Error closing socket {}", m_sock, e);
                }
            }

            m_sock = null;

            if (m_rdrThread.isAlive()) {
                m_rdrThread.interrupt();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }
    }
}
