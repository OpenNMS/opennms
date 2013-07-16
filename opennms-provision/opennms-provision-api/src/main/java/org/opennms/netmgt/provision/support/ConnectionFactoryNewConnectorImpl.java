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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This {@link ConnectionFactory} type will create a new {@link NioSocketConnector}
 * for every call to {@link #connect(SocketAddress, SocketAddress, IoSessionInitializer)}.
 * This is a naive implementation that does not really take advantage of the asynchronous
 * operations in MINA since it is creating new threads for each socket operation.</p>
 *
 * @author Seth
 * @author ranger
 * @author Duncan Mackintosh
 */
public class ConnectionFactoryNewConnectorImpl extends ConnectionFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionFactoryNewConnectorImpl.class);
    
    private static final Executor m_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final IoProcessor<NioSession> m_processor = new SimpleIoProcessorPool<NioSession>(NioProcessor.class, m_executor);
    private ThreadLocal<Integer> m_port = new ThreadLocal<Integer>();
    private final Object m_portMutex = new Object();

    /**
     * Create a new factory. Private because one should use {@link #getFactory(int)}
     */
    protected ConnectionFactoryNewConnectorImpl(int timeoutInMillis) {
        super(timeoutInMillis);
    }

    private static final NioSocketConnector getSocketConnector(long timeout, IoHandler handler) {
        // Create a new NioSocketConnector
        NioSocketConnector connector = new NioSocketConnector(m_executor, m_processor);

        // Enable SO_REUSEADDR on the socket so that TIMED_WAIT connections that are bound on the
        // same port do not block new outgoing connections. If the connections are blocked, then
        // the following exception will be thrown under heavy load:
        //
        // Caused by: java.net.SocketException: Invalid argument
        //   at sun.nio.ch.Net.connect(Native Method)
        //   at sun.nio.ch.SocketChannelImpl.connect(SocketChannelImpl.java:500)
        //   at org.apache.mina.transport.socket.nio.NioSocketConnector.connect(NioSocketConnector.java:188)
        //   ...
        //
        // @see http://issues.opennms.org/browse/NMS-5469
        //
        connector.getSessionConfig().setReuseAddress(true);

        // Setting SO_LINGER will prevent TIME_WAIT sockets altogether because TCP connections will be
        // forcefully terminated with RST packets. However, this doesn't seem to be necessary to maintain
        // performance of the outgoing connections. As long as SO_REUSEADDR is set, the operating system
        // and Java appear to recycle the ports effectively although some will remain in TIME_WAIT state.
        //
        // @see http://issues.opennms.org/browse/NMS-5469
        //
        //connector.getSessionConfig().setSoLinger(0);

        connector.setHandler(handler);
        connector.setConnectTimeoutMillis(timeout);
        return connector;
    }

    /**
     * <p>Connect to a remote socket. If org.opennms.netmgt.provision.maxConcurrentConnections
     * is set, this may block until a connection slot is available.</p>
     * 
     * <p>You must dispose both the {@link ConnectionFactoryNewConnectorImpl} and {@link ConnectFuture} when done
     * by calling {@link #dispose(ConnectionFactoryNewConnectorImpl, ConnectFuture)}.</p>
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
        SocketConnector connector = getSocketConnector(getTimeout(), handler);
        InetSocketAddress localAddress = null;
        synchronized (m_portMutex) {
            if (m_port.get() == null) {
                // Fetch a new ephemeral port
                localAddress = new InetSocketAddress(0);
                m_port.set(localAddress.getPort());
            } else {
                localAddress = new InetSocketAddress(m_port.get());
            }
        }
        final ConnectFuture cf = connector.connect(remoteAddress, localAddress, init);
        cf.addListener(portSwitcher(connector, remoteAddress, init, handler));
        cf.addListener(connectorDisposer(connector));
        return cf;
    }

    private static IoFutureListener<ConnectFuture> connectorDisposer(final SocketConnector connector) {
        return new IoFutureListener<ConnectFuture>() {

            @Override
            public void operationComplete(ConnectFuture future) {
                try {
                    // Add a listener to the CloseFuture that will dispose of the connector once the
                    // conversation is complete
                    future.getSession().getCloseFuture().addListener(new IoFutureListener<CloseFuture>() {
                        @Override
                        public void operationComplete(CloseFuture future) {
                            LOG.debug("Disposing of connector: {}", Thread.currentThread().getName());
                            connector.dispose();
                        }
                    });
                } catch (RuntimeIoException e) {
                    LOG.debug("Exception of type {} caught, disposing of connector: {}", e.getClass().getName(), Thread.currentThread().getName(), e);
                    // This will be thrown in the event of a ConnectException for example
                    connector.dispose();
                }
            }
        };
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
                            LOG.warn("Resetting outgoing TCP port, value was {}", m_port.get());
                            m_port.set(null);
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
     * since we are recreating connectors during each invocation.
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
        // Do nothing; we dispose of every NioSocketConnector as soon as a session is complete
    }
}
