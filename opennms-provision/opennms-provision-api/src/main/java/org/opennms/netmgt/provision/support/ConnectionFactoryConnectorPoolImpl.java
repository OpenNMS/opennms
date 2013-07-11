/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.support;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Factory for encapsulating a {@link NioSocketConnector} in such a way as to allow us
 * to reuse the connector for each {@link #connect(SocketAddress, SocketAddress, IoSessionInitializer, IoHandler)}
 * call.
 * </p>
 * 
 * <p>
 * There will be one ConnectionFactory for each discrete connection timeout
 * value.
 * </p>
 * 
 * @author Seth
 * @author ranger
 * @author Duncan Mackintosh
 */
public class ConnectionFactoryConnectorPoolImpl extends ConnectionFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionFactoryConnectorPoolImpl.class);
    
    /**
     * The connector that will be reused for each incoming connection.
     */
    private NioSocketConnector m_connector;

    /**
     * A mutex that protects the connector instance since we must dispose() and 
     * recreate it if it encounters errors.
     */
    private final Object m_connectorMutex = new Object();

    private Integer m_port = null;
    private final Object m_portMutex = new Object();

    /**
     * Create a new factory. Private because one should use {@link #getFactory(int)}
     */
    protected ConnectionFactoryConnectorPoolImpl(int timeoutInMillis) {
        super(timeoutInMillis);
    }

    private static final NioSocketConnector getSocketConnector(long timeout, IoHandler handler) {
        NioSocketConnector connector = new NioSocketConnector();
        connector.setHandler(handler);
        connector.setConnectTimeoutMillis(timeout);
        return connector;
    }

    /**
     * <p>Connect to a remote socket. If org.opennms.netmgt.provision.maxConcurrentConnections
     * is set, this may block until a connection slot is available.</p>
     * 
     * <p>You must dispose both the {@link ConnectionFactoryConnectorPoolImpl} and {@link ConnectFuture} when done
     * by calling {@link #dispose(ConnectionFactoryConnectorPoolImpl, ConnectFuture)}.</p>
     * 
     * @param remoteAddress
     * 		Destination address
     * @param init
     * 		Initialiser for the IoSession
     * @return
     * 		ConnectFuture from a Mina connect call
     */
    @Override
    public ConnectFuture connect(SocketAddress remoteAddress, IoSessionInitializer<? extends ConnectFuture> init, IoHandler handler) {
        for (int retries = 0; retries < 3; retries++) { 
            synchronized (m_connectorMutex) {
                if (m_connector == null) {
                    // Sanity check for null connector instance
                    LOG.debug("Found a null NioSocketConnector, creating a new one with timeout {}", getTimeout());
                    m_connector = getSocketConnector(getTimeout(), handler);
                }

                try {
                    /*
                     * Set the handler each time since we are reusing this connector for every incoming
                     * connect() call.
                     */
                    m_connector.setHandler(handler);

                    InetSocketAddress localAddress = null;
                    synchronized (m_portMutex) {
                        if (m_port == null) {
                            // Fetch a new ephemeral port
                            localAddress = new InetSocketAddress(InetAddressUtils.getLocalHostAddress(), 0);
                            m_port = localAddress.getPort();
                        } else {
                            localAddress = new InetSocketAddress(InetAddressUtils.getLocalHostAddress(), m_port);
                        }
                    }

                    /*
                     * Use the 3-argument call to connect(). If you use the 2-argument version without
                     * the localhost port, the call will end up doing a name lookup which seems to fail
                     * intermittently in unit tests.
                     *
                     * @see http://issues.opennms.org/browse/NMS-5309
                     */
                    ConnectFuture cf = m_connector.connect(remoteAddress, localAddress, init);
                    cf.addListener(portSwitcher(m_connector, remoteAddress, init, handler));
                    return cf;
                } catch (Throwable e) {
                    LOG.debug("Caught exception on factory {}, retrying: {}", this, e);
                    m_connector.dispose();
                    m_connector = getSocketConnector(getTimeout(), handler);
                    continue;
                }
            }
        }
        throw new IllegalStateException("Could not connect to socket because of excessive RejectedExecutionExceptions");
    }

    private IoFutureListener<ConnectFuture> portSwitcher(final SocketConnector connector, final SocketAddress remoteAddress, final IoSessionInitializer<? extends ConnectFuture> init, final IoHandler handler) {
        return new IoFutureListener<ConnectFuture>() {

            @Override
            public void operationComplete(ConnectFuture future) {
                try {
                    Throwable e = future.getException();
                    // If we failed to bind to the outgoing port...
                    if (e != null && e instanceof BindException) {
                        synchronized(m_portMutex) {
                            // ... then reset the port
                            m_port = null;
                        }
                        // and reattempt the connection
                        connect(remoteAddress, init, handler);
                    }
                } catch (RuntimeIoException e) {
                    LOG.debug("Exception of type {} caught, disposing of connector: {}", e.getClass().getName(), Thread.currentThread().getName(), e);
                    // This will be thrown in the event of a ConnectException for example
                    connector.dispose();
                }
            }
        };
    }

    /**
     * Delegates completely to {@link #connect(SocketAddress, SocketAddress, IoSessionInitializer, IoHandler)}
     * since we are reusing the same connector for all invocations.
     * 
     * @param remoteAddress
     * @param localAddress
     * @param init
     * @param handler
     */
    @Override
    public ConnectFuture reConnect(SocketAddress remoteAddress, IoSessionInitializer<? extends ConnectFuture> init, IoHandler handler) {
        return connect(remoteAddress, init, handler);
    }

    @Override
    protected void dispose() {
        synchronized (m_connectorMutex) {
            m_connector.dispose();
        }
    }
}
