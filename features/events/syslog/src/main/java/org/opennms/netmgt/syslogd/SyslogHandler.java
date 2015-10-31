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

package org.opennms.netmgt.syslogd;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the User Datagram Protocol (UDP) event receiver. When
 * the an agent sends an event via UDP/IP the receiver will process the event
 * and then add the UUIDs to the internal list. If the event is successfully
 * processed then an event-receipt is returned to the caller.
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 */
public final class SyslogHandler implements Fiber {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogHandler.class);

    private final boolean USE_NIO = false;
    private final boolean USE_NETTY = false;

    /**
     * The UDP receiver thread.
     */
    private SyslogReceiver m_receiver;

    /**
     * The Fiber's status.
     */
    private volatile int m_status;

    /**
     * The UDP socket for receipt and transmission of packets from agents.
     */
    private DatagramSocket m_dgSock;

    private SyslogdConfig m_config;

    /**
     * The UDP socket port binding.
     */
    private int m_dgPort;

    /**
     * The IP address to bind to.
     */
    private String m_dgIp;

    /**
     * The log prefix
     */
    private String m_logPrefix;

    /**
     * <p>Constructor for SyslogHandler.</p>
     */
    public SyslogHandler(SyslogdConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        m_dgSock = null;
        m_dgPort = config.getSyslogPort();
        m_dgIp = config.getListenAddress();

        m_config = config;

        m_status = START_PENDING;

        m_receiver = null;
        m_logPrefix = null;
    }

    /**
     * <p>start</p>
     */
    @Override
    public synchronized void start() {
        if (m_status != START_PENDING)
            throw new RuntimeException("The Fiber is in an incorrect state");

        m_status = STARTING;

        try {
            if (USE_NIO) {
                // NIO SyslogReceiver implementation

                DatagramChannel channel = DatagramChannel.open();
                if (m_dgIp != null && m_dgIp.length() != 0) {
                    channel.socket().bind(new InetSocketAddress(InetAddressUtils.addr(m_dgIp), m_dgPort));
                } else {
                    channel.socket().bind(new InetSocketAddress(m_dgPort));
                }

                m_receiver = new SyslogReceiverNioThreadPoolImpl(
                    channel,
                    m_config
                );
            } else if (USE_NETTY){
                // Camel Netty SyslogReceiver implementation

                m_receiver = new SyslogReceiverCamelNettyImpl(
                    (m_dgIp != null && m_dgIp.length() != 0) ? 
                        InetAddressUtils.addr(m_dgIp) :
                        InetAddressUtils.getLocalHostAddress(),
                    m_dgPort,
                    m_config
                );
            } else {
                // java.net SyslogReceiver implementation

                if (m_dgIp != null && m_dgIp.length() != 0) {
                    m_dgSock = new DatagramSocket(m_dgPort, InetAddressUtils.addr(m_dgIp));
                } else {
                    m_dgSock = new DatagramSocket(m_dgPort);
                }

                m_receiver = new SyslogReceiverJavaNetImpl(
                    m_dgSock,
                    m_config
                );
            }

            if (m_logPrefix != null) {
                m_receiver.setLogPrefix(m_logPrefix);
            }
        } catch (IOException e) {
            throw new java.lang.reflect.UndeclaredThrowableException(e);
        }

        Thread rThread = new Thread(m_receiver, "Syslog Event Receiver["
                + getIpAddress() + ":" + m_dgPort + "]");

        try {
            rThread.start();

        } catch (RuntimeException e) {
            rThread.interrupt();

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
        if (m_status == STOPPED)
            return;
        if (m_status == START_PENDING) {
            m_status = STOPPED;
            return;
        }

        m_status = STOP_PENDING;

        try {
            if (m_receiver != null) {
                m_receiver.stop();
            }
        } catch (InterruptedException e) {
            LOG.warn("The thread was interrupted while attempting to join sub-threads", e);
        }

        if (m_dgSock != null) {
            m_dgSock.close();
        }

        m_status = STOPPED;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "SyslogdHandler[" + getIpAddress() + ":" + m_dgPort + "]";
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
     * <p>init</p>
     */
    public void init() {
    }

    /**
     * <p>destroy</p>
     */
    public void destroy() {
    }

    /**
     * <p>setPort</p>
     *
     * @param port a {@link java.lang.Integer} object.
     */
    public void setPort(final Integer port) {
        if (m_status == STARTING || m_status == RUNNING
                || m_status == STOP_PENDING)
            throw new IllegalStateException("The process is already running");

        m_dgPort = port;
    }

    /**
     * <p>getPort</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getPort() {
        return m_dgPort;
    }

    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @since 1.8.1
     */
    public void setIpAddress(final String ipAddress) {
        m_dgIp = ipAddress;
    }
    
    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.8.1
     */
    public String getIpAddress() {
        if (m_dgIp == null || m_dgIp.length() == 0) {
            return "0.0.0.0";
        }
        return m_dgIp;
    }

    public void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }

    public interface EventHandler {
        public boolean processEvent(Event event);

        public void receiptSent(EventReceipt receipt);
    }
}
