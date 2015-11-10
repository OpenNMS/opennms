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

import org.apache.camel.Service;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
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
public final class SyslogHandler implements Service {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogHandler.class);

    private static enum SyslogReceiverImpl {
        JAVA_NET,
        NIO,
        NIO_DISRUPTOR,
        NETTY
    }

    // TODO: Make this configurable
    private static final SyslogReceiverImpl IMPLEMENTATION = SyslogReceiverImpl.JAVA_NET;

    /**
     * The UDP receiver thread.
     */
    private SyslogReceiver m_receiver;

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
     * <p>Constructor for SyslogHandler.</p>
     */
    public SyslogHandler(SyslogdConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        m_dgPort = config.getSyslogPort();
        m_dgIp = config.getListenAddress();
        m_config = config;

        m_receiver = null;
    }

    /**
     * <p>start</p>
     */
    @Override
    public synchronized void start() {
        try {
            switch(IMPLEMENTATION) {
            case NIO:
            case NIO_DISRUPTOR:
                // NIO SyslogReceiver implementation

                DatagramChannel channel = DatagramChannel.open();
                if (m_dgIp != null && m_dgIp.length() != 0) {
                    channel.socket().bind(new InetSocketAddress(InetAddressUtils.addr(m_dgIp), m_dgPort));
                } else {
                    channel.socket().bind(new InetSocketAddress(m_dgPort));
                }

                if (IMPLEMENTATION == SyslogReceiverImpl.NIO) {
                    m_receiver = new SyslogReceiverNioThreadPoolImpl(
                        channel,
                        m_config
                    );
                } else {
                    m_receiver = new SyslogReceiverNioDisruptorImpl(
                        channel,
                        m_config
                    );
                }
                break;
            case NETTY:
                // Camel Netty SyslogReceiver implementation

                m_receiver = new SyslogReceiverCamelNettyImpl(
                    (m_dgIp != null && m_dgIp.length() != 0) ? 
                        InetAddressUtils.addr(m_dgIp) :
                        InetAddressUtils.getLocalHostAddress(),
                    m_dgPort,
                    m_config
                );
                break;
            case JAVA_NET:
            default:
                // java.net SyslogReceiver implementation

                // The UDP socket for receipt of packets
                DatagramSocket dgSock;

                if (m_dgIp != null && m_dgIp.length() != 0) {
                    dgSock = new DatagramSocket(m_dgPort, InetAddressUtils.addr(m_dgIp));
                } else {
                    dgSock = new DatagramSocket(m_dgPort);
                }

                m_receiver = new SyslogReceiverJavaNetImpl(
                    dgSock,
                    m_config
                );
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
            throw e;
        }
    }

    /**
     * <p>stop</p>
     */
    @Override
    public synchronized void stop() {
        try {
            if (m_receiver != null) {
                m_receiver.stop();
            }
        } catch (InterruptedException e) {
            LOG.warn("The thread was interrupted while attempting to join sub-threads", e);
        }
    }

    /**
     * <p>setPort</p>
     *
     * @param port a {@link java.lang.Integer} object.
     */
    public void setPort(final Integer port) {
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
}
