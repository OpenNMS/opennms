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
// 29 Aug 2008: Code formatting, use InetSocketAddress instead of InetAddress
//              and port, add a TCP connection timeout defaulting to two
//              seconds, implement log(), improve exception and log messages,
//              don't needlessly pass around fields that inner classes can
//              directly access, switch the Reader inside Connection to the
//              InputStream straight from Socket, and switch a while() loop with
//              variable initialization to a for() loop. - dj@opennms.org
// 31 Jan 2003: Cleaned up some unused imports.
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
package org.opennms.netmgt.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.Marshaller;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

/**
 * This is the interface used to send events into the event subsystem - It is
 * typically used by the poller framework plugins that perform service
 * monitoring to send out aprropriate events. Can also be used by capsd,
 * discovery etc.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public final class TcpEventProxy implements EventProxy {
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
        this(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), DEFAULT_PORT));
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
     * @throws org.opennms.netmgt.model.events.EventProxyException if any.
     */
    public void send(Log eventLog) throws EventProxyException {
        Connection connection = null;
        try {
            connection = new Connection();
            Writer writer = connection.getWriter();
            Marshaller.marshal(eventLog, writer);
            writer.flush();
        } catch (ConnectException e) {
            throw new EventProxyException("Could not connect to event daemon " + m_address + " to send event: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new EventProxyException("Unknown exception while sending event: " + e, e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private class Connection {
        private Socket m_sock;

        private Writer m_writer;
        
        private InputStream m_input;

        private Thread m_rdrThread;

        public Connection() throws IOException {
            m_sock = new Socket();
            m_sock.connect(m_address, m_timeout);
            m_sock.setSoTimeout(500);
            log().debug("Default Charset:" + Charset.defaultCharset().displayName());
            log().debug("Setting Charset: UTF-8");
            m_writer = new OutputStreamWriter(new BufferedOutputStream(m_sock.getOutputStream()), Charset.forName("UTF-8"));
            m_input = m_sock.getInputStream();
            m_rdrThread = new Thread("TcpEventProxy Input Discarder") {
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

        public void close() {
            if (m_sock != null) {
                try {
                    m_sock.close();
                } catch (IOException e) {
                    log().warn("Error closing socket " + m_sock + ": " + e, e);
                }
            }

            m_sock = null;

            if (m_rdrThread.isAlive()) {
                m_rdrThread.interrupt();
            }
        }
        
        protected void finalize() throws Throwable {
            close();
        }
    }
}
