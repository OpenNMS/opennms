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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.eventd.adaptors.EventHandler;
import org.opennms.netmgt.eventd.adaptors.EventHandlerMBeanProxy;
import org.opennms.netmgt.eventd.adaptors.EventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * This class implements the User Datagram Protocol (UDP) event receiver. When
 * the an agent sends an event via UDP/IP the receiver will process the event
 * and then add the UUIDs to the internal list. If the event is successfully
 * processed then an event-receipt is returned to the caller.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 */
public final class UdpEventReceiver implements EventReceiver, UdpEventReceiverMBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(UdpEventReceiver.class);
    
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

    /**
     * <p>Constructor for UdpEventReceiver.</p>
     */
    public UdpEventReceiver() {
        this(UDP_PORT, null);
    }

    /**
     * <p>Constructor for UdpEventReceiver.</p>
     *
     * @param port a int.
     * @param ipAddress a {@link java.lang.String} object.
     */
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

    /**
     * <p>start</p>
     */
    @Override
    public synchronized void start() {
        assertNotRunning();

        m_status = STARTING;

        try {
            InetAddress address = "*".equals(m_ipAddress) ? null : InetAddressUtils.addr(m_ipAddress);
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

    /**
     * <p>stop</p>
     */
    @Override
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
            LOG.warn("The thread was interrupted while attempting to join sub-threads", e);
        }

        m_dgSock.close();

        m_status = STOPPED;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "Event UDP Receiver[" + m_dgPort + "]";
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return m_status;
    }

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStatusText() {
        return STATUS_NAMES[getStatus()];
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String status() {
        return getStatusText();
    }

    /**
     * <p>init</p>
     */
    @Override
    public void init() {
    }

    /**
     * <p>destroy</p>
     */
    @Override
    public void destroy() {
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public void setIpAddress(String ipAddress) {
        assertNotRunning();
        
        m_ipAddress = ipAddress;
    }

    /** {@inheritDoc} */
    @Override
    public void setPort(Integer port) {
        assertNotRunning();

        m_dgPort = port.intValue();
    }

    /**
     * <p>getPort</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getPort() {
        return m_dgPort;
    }

    /**
     * {@inheritDoc}
     *
     * Adds a new event handler to receiver. When new events are received the
     * decoded event is passed to the handler.
     */
    @Override
    public void addEventHandler(EventHandler handler) {
        synchronized (m_eventHandlers) {
            if (!m_eventHandlers.contains(handler)) {
                m_eventHandlers.add(handler);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Removes an event handler from the list of handler called when an event is
     * received. The handler is removed based upon the method
     * <code>equals()</code> inherieted from the <code>Object</code> class.
     */
    @Override
    public void removeEventHandler(EventHandler handler) {
        synchronized (m_eventHandlers) {
            m_eventHandlers.remove(handler);
        }
    }

    /**
     * <p>getEventHandlers</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<EventHandler> getEventHandlers() {
        return m_eventHandlers;
    }

    /**
     * <p>setEventHandlers</p>
     *
     * @param eventHandlers a {@link java.util.List} object.
     */
    public void setEventHandlers(List<EventHandler> eventHandlers) {
        m_eventHandlers = eventHandlers;
    }

    /** {@inheritDoc} */
    @Override
    public void addEventHandler(String name) throws MalformedObjectNameException, InstanceNotFoundException {
        addEventHandler(new EventHandlerMBeanProxy(new ObjectName(name)));
    }

    /** {@inheritDoc} */
    @Override
    public void removeEventHandler(String name) throws MalformedObjectNameException, InstanceNotFoundException {
        removeEventHandler(new EventHandlerMBeanProxy(new ObjectName(name)));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setLogPrefix(final String prefix) {
        m_logPrefix = prefix;
    }
    
    private void assertNotRunning() {
        Assert.state(m_status == START_PENDING || m_status == STOPPED, "The fiber is already running and cannot be modified or started");
    }
}
