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
// 2008 Feb 02: Allow specific of the server IP address. - dj@opennms.org
// 2008 Jan 26: Rename m_handlers to m_eventHandlers and expose a
//              getter and setter. - dj@opennms.org
// 2008 Jan 23: Java 5 generics, log() method, format code. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
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

package org.opennms.netmgt.eventd.adaptors.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.adaptors.EventHandler;
import org.opennms.netmgt.eventd.adaptors.EventHandlerMBeanProxy;
import org.opennms.netmgt.eventd.adaptors.EventReceiver;
import org.springframework.util.Assert;

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
public final class UdpEventReceiver implements EventReceiver, UdpEventReceiverMBean {
    /**
     * The default User Datagram Port for the receipt and transmission of
     * events.
     */
    private static final int UDP_PORT = 5817;

    /**
     * The UDP receiver thread.
     */
    private UdpReceiver m_receiver;

    /**
     * The user datagram packet processor
     */
    private UdpProcessor m_processor;

    /**
     * The event receipt generator and sender thread.
     */
    private UdpUuidSender m_output;

    /**
     * The list of incomming events.
     */
    private List<UdpReceivedEvent> m_eventsIn;

    /**
     * The list of outgoing event-receipts by UUID.
     */
    private List<UdpReceivedEvent> m_eventUuidsOut;

    /**
     * The list of registered event handlers.
     */
    private List<EventHandler> m_eventHandlers;

    /**
     * The Fiber's status.
     */
    private volatile int m_status;

    /**
     * The UDP socket for receipt and transmission of packets from agents.
     */
    private DatagramSocket m_dgSock;

    /**
     * The IP address for the UDP socket to bind on.
     */
    private String m_ipAddress;

    /**
     * The UDP socket port binding.
     */
    private int m_dgPort;

    /**
     * The log prefix
     */
    private String m_logPrefix;

    public UdpEventReceiver() {
        this(UDP_PORT, null);
    }

    public UdpEventReceiver(int port, String ipAddress) {
        m_dgSock = null;
        m_ipAddress = ipAddress;
        m_dgPort = port;

        m_eventsIn = new LinkedList<UdpReceivedEvent>();
        m_eventUuidsOut = new LinkedList<UdpReceivedEvent>();

        m_eventHandlers = new ArrayList<EventHandler>(3);
        m_status = START_PENDING;

        m_dgSock = null;
        m_receiver = null;
        m_processor = null;
        m_output = null;
        m_logPrefix = null;
    }

    public synchronized void start() {
        assertNotRunning();

        m_status = STARTING;

        try {
            InetAddress address = "*".equals(m_ipAddress) ? null : InetAddress.getByName(m_ipAddress);
            m_dgSock = new DatagramSocket(m_dgPort, address);

            m_receiver = new UdpReceiver(m_dgSock, m_eventsIn);
            m_processor = new UdpProcessor(m_eventHandlers, m_eventsIn, m_eventUuidsOut);
            m_output = new UdpUuidSender(m_dgSock, m_eventUuidsOut, m_eventHandlers);

            if (m_logPrefix != null) {
                m_receiver.setLogPrefix(m_logPrefix);
                m_processor.setLogPrefix(m_logPrefix);
                m_output.setLogPrefix(m_logPrefix);
            }
        } catch (IOException e) {
            throw new java.lang.reflect.UndeclaredThrowableException(e);
        }

        Thread rThread = new Thread(m_receiver, "UDP Event Receiver[" + m_dgPort + "]");
        Thread pThread = new Thread(m_processor, "UDP Event Processor[" + m_dgPort + "]");
        Thread oThread = new Thread(m_output, "UDP UUID Sender[" + m_dgPort + "]");
        try {
            rThread.start();
            pThread.start();
            oThread.start();
        } catch (RuntimeException e) {
            rThread.interrupt();
            pThread.interrupt();
            oThread.interrupt();

            m_status = STOPPED;
            
            throw e;
        }

        m_status = RUNNING;
    }

    public synchronized void stop() {
        if (m_status == STOPPED) {
            return;
        }
        if (m_status == START_PENDING) {
            m_status = STOPPED;
            return;
        }

        m_status = STOP_PENDING;

        try {
            m_receiver.stop();
            m_processor.stop();
            m_output.stop();
        } catch (InterruptedException e) {
            log().warn("The thread was interrupted while attempting to join sub-threads: " + e, e);
        }

        m_dgSock.close();

        m_status = STOPPED;
    }

    public String getName() {
        return "Event UDP Receiver[" + m_dgPort + "]";
    }

    public int getStatus() {
        return m_status;
    }

    public String getStatusText() {
        return STATUS_NAMES[getStatus()];
    }

    public String status() {
        return getStatusText();
    }

    public void init() {
    }

    public void destroy() {
    }

    public String getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        assertNotRunning();
        
        m_ipAddress = ipAddress;
    }

    public void setPort(Integer port) {
        assertNotRunning();

        m_dgPort = port.intValue();
    }

    public Integer getPort() {
        return new Integer(m_dgPort);
    }

    /**
     * Adds a new event handler to receiver. When new events are received the
     * decoded event is passed to the handler.
     * 
     * @param handler
     *            A reference to an event handler
     * 
     */
    public void addEventHandler(EventHandler handler) {
        synchronized (m_eventHandlers) {
            if (!m_eventHandlers.contains(handler)) {
                m_eventHandlers.add(handler);
            }
        }
    }

    /**
     * Removes an event handler from the list of handler called when an event is
     * received. The handler is removed based upon the method
     * <code>equals()</code> inherieted from the <code>Object</code> class.
     * 
     * @param handler
     *            A reference to the event handler.
     * 
     */
    public void removeEventHandler(EventHandler handler) {
        synchronized (m_eventHandlers) {
            m_eventHandlers.remove(handler);
        }
    }

    public List<EventHandler> getEventHandlers() {
        return m_eventHandlers;
    }

    public void setEventHandlers(List<EventHandler> eventHandlers) {
        m_eventHandlers = eventHandlers;
    }

    public void addEventHandler(String name) throws MalformedObjectNameException, InstanceNotFoundException {
        addEventHandler(new EventHandlerMBeanProxy(new ObjectName(name)));
    }

    public void removeEventHandler(String name) throws MalformedObjectNameException, InstanceNotFoundException {
        removeEventHandler(new EventHandlerMBeanProxy(new ObjectName(name)));
    }

    public void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }
    
    private void assertNotRunning() {
        Assert.state(m_status == START_PENDING || m_status == STOPPED, "The fiber is already running and cannot be modified or started");
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
