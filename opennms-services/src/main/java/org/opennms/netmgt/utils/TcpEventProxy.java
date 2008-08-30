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
// Tab Size = 8
//

package org.opennms.netmgt.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
 * 
 */
public final class TcpEventProxy implements EventProxy {
    private static final int s_default_port = 5817;

    private static final InetAddress s_default_host;

    private InetAddress m_target;

    private int m_port;

    static {
        try {
            s_default_host = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public TcpEventProxy() {
        this(s_default_host, s_default_port);
    }

    public TcpEventProxy(int port) {
        this(s_default_host, port);
    }

    public TcpEventProxy(InetAddress target) {
        this(target, s_default_port);
    }

    public TcpEventProxy(String address, int port) throws UnknownHostException {
    	m_port = port;
    	m_target = InetAddress.getByName(address);
    }
    
    public TcpEventProxy(InetAddress target, int port) {
        m_port = port;
        m_target = target;
    }

    /**
     * This method is called to send the event out
     * 
     * @param event
     *            the event to be sent out
     * 
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
     * 
     * @exception UndeclaredThrowableException
     *                thrown if the send fails for any reason
     */
    public void send(Log eventLog) throws EventProxyException {
        Connection connection = null;
        try {
            connection = new Connection(m_target, m_port);
            Writer writer = connection.getWriter();
            Marshaller.marshal(eventLog, writer);
            writer.flush();
        } catch (Exception e) {
            throw new EventProxyException("Exception while sending event: " + e.getMessage(), e);
        } finally {
            if (connection != null) connection.close();
        }
    }

    public class Connection {
        private Socket m_sock;

        private Writer m_writer;

        private Reader m_reader;

        private Thread m_rdrThread;

        public Connection(InetAddress target, int port) throws IOException {
            // get a socket and set the timeout
            //
            try {
                m_sock = new Socket(target, port);
            } catch (ConnectException e) {
                ConnectException n = new ConnectException("Could not connect to event daemon at " + target + " on port " + Integer.toString(port) + ": " + e.getMessage());
                n.initCause(e);
                throw n;
            }
            m_sock.setSoTimeout(500);

            m_writer = new OutputStreamWriter(new BufferedOutputStream(m_sock.getOutputStream()));
            m_reader = new InputStreamReader(m_sock.getInputStream());
            m_rdrThread = new Thread("TcpEventProxy Input Discarder") {
                public void run() {
                    int ch = 0;
                    while (ch != -1) {
                        try {
                            ch = m_reader.read();
                        } catch (InterruptedIOException e) {
                            ch = 0;
                        } catch (IOException e) {
                            ch = -1;
                        }
                    }
                } // end run()
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
                    ThreadCategory.getInstance(getClass()).warn("Error closing socket", e);
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
