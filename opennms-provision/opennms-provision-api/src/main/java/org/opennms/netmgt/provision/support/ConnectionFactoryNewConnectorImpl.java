/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import org.opennms.core.utils.LogUtils;

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

    private static final Executor m_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final IoProcessor<NioSession> m_processor = new SimpleIoProcessorPool<NioSession>(NioProcessor.class, m_executor);

    /**
     * Create a new factory. Private because one should use {@link #getFactory(int)}
     */
    protected ConnectionFactoryNewConnectorImpl(int timeoutInMillis) {
        super(timeoutInMillis);
    }

    private static final NioSocketConnector getSocketConnector(long timeout, IoHandler handler) {
        // Create a new NioSocketConnector
        NioSocketConnector connector = new NioSocketConnector(m_executor, m_processor);
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
    public ConnectFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> init, IoHandler handler) {
        SocketConnector connector = getSocketConnector(getTimeout(), handler);
        final ConnectFuture cf = connector.connect(remoteAddress, localAddress, init);
        cf.addListener(connectorDisposer(connector));
        return cf;
    }

    private static IoFutureListener<ConnectFuture> connectorDisposer(final SocketConnector connector) {
        return new IoFutureListener<ConnectFuture>() {

            public void operationComplete(ConnectFuture future) {
                try {
                    // Add a listener to the CloseFuture that will dispose of the connector once the
                    // conversation is complete
                    future.getSession().getCloseFuture().addListener(new IoFutureListener<CloseFuture>() {
                        @Override
                        public void operationComplete(CloseFuture future) {
                            LogUtils.debugf(this, "Disposing of connector: %s", Thread.currentThread().getName());
                            connector.dispose();
                        }
                    });
                } catch (RuntimeIoException e) {
                    LogUtils.debugf(this, e, "Exception of type %s caught, disposing of connector: %s", e.getClass().getName(), Thread.currentThread().getName());
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
    public ConnectFuture reConnect(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> init, IoHandler handler) {
        return connect(remoteAddress, localAddress, init, handler);
    }

    @Override
    protected void dispose() {
        // Do nothing; we dispose of every NioSocketConnector as soon as a session is complete
    }
}
