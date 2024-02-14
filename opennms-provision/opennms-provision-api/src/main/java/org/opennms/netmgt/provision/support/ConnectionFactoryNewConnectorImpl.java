/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.support;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.socket.SocketConnector;
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
    
    //private static final Executor m_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    //private static final IoProcessor<NioSession> m_processor = new SimpleIoProcessorPool<NioSession>(NioProcessor.class, m_executor);
    private ThreadLocal<Integer> m_port = new ThreadLocal<>();
    private final Object m_portMutex = new Object();

    /**
     * Create a new factory. Private because one should use {@link #getFactory(int)}
     */
    protected ConnectionFactoryNewConnectorImpl(int timeoutInMillis) {
        super(timeoutInMillis);
    }

    private static final NioSocketConnector getSocketConnector(long timeout, IoHandler handler) {
        // Create a new NioSocketConnector
        //NioSocketConnector connector = new NioSocketConnector(m_executor, m_processor);

        // To address bug NMS-6412, I'm changing this to use the default constructor so that the
        // Executor pool and IoProcessor pools are created and destroyed for every connector.
        // This slows things down but might be an acceptable tradeoff for more reliable detection.
        //
        // @see http://issues.opennms.org/browse/NMS-6412
        //
        NioSocketConnector connector = new NioSocketConnector(Runtime.getRuntime().availableProcessors());

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
