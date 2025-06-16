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

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Factory that controls creation of MINA {@link NioSocketConnector} connections.
 * This will allow us to reuse {@link NioSocketConnector} instances to improve
 * performance and avoid file handle leaks caused by using too many {@link NioSocketConnector}
 * instances simultaneously.
 * </p>
 * 
 * <p>
 * Because of the way that the MINA API works, there will be one {@link ConnectionFactory} 
 * for each discrete connection timeout value.
 * </p>
 *
 * @author Seth
 * @author ranger
 * @author Duncan Mackintosh
 */
public abstract class ConnectionFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionFactory.class);
	/** Map of timeoutInMillis to a ConnectionFactory with that timeout */
    private static final ConcurrentHashMap<Integer, ConnectionFactory> s_connectorPool = new ConcurrentHashMap<Integer, ConnectionFactory>();
	
    /**
     * Count the number of references to this factory so we can dispose it
     * when there are no active references.
     */
    private int m_references = 0;

    private final long m_timeout;

    /**
     * @return the timeout
     */
    public final long getTimeout() {
        return m_timeout;
    }

    /**
     * Create a new factory. Private because one should use {@link #getFactory(int)}
     */
    protected ConnectionFactory(int timeoutInMillis) {
        m_timeout = timeoutInMillis;
    }
    
    /**
     * <p>Get a new ConnectionFactory. If there is already a Factory with the
     * desired timeout, you will get that one; otherwise a new one is created.</p>
     * 
     * <p>If org.opennms.netmgt.provision.maxConcurrentConnectors is set, this may
     * block until a connector is available.</p>
     * 
     * @param timeoutInMillis
     * 		Connection timeout
     * @return
     * 		An appropriate Factory
     */
    public static final ConnectionFactory getFactory(int timeoutInMillis) {
        synchronized (s_connectorPool) {
            ConnectionFactory factory = s_connectorPool.get(timeoutInMillis);
            if (factory == null) {
                LOG.debug("Creating a ConnectionFactory for timeout {}, there are {} factories total", timeoutInMillis, s_connectorPool.size());
                ConnectionFactory newFactory = createConnectionFactory(timeoutInMillis);
                factory = s_connectorPool.putIfAbsent(timeoutInMillis, newFactory);
                // If there was no previous value for the factory in the map...
                if (factory == null) {
                    // ...then use the new value.
                    factory = newFactory;
                } else {
                    LOG.debug("ConnectionFactory for timeout {} was already created in another thread!", timeoutInMillis);
                    // Dispose of the new unused factory
                    dispose(newFactory);
                }
            }
            factory.m_references++;
            return factory;
        }
    }

    private static final ConnectionFactory createConnectionFactory(int timeout) {
        //return new ConnectionFactoryConnectorPoolImpl(timeout);
        return new ConnectionFactoryNewConnectorImpl(timeout);
    }

    /**
     * <p>Connect to a remote socket. If org.opennms.netmgt.provision.maxConcurrentConnections
     * is set, this may block until a connection slot is available.</p>
     * 
     * <p>You must dispose the {@link ConnectionFactory} when done
     * by calling {@link #dispose(ConnectionFactory)}.</p>
     * 
     * @param remoteAddress
     * 		Destination address
     * @param init
     * 		Initialiser for the IoSession
     * @return
     * 		ConnectFuture from a Mina connect call
     */
    public abstract ConnectFuture connect(SocketAddress remoteAddress, IoSessionInitializer<? extends ConnectFuture> init, IoHandler handler);

    /**
     * Retry a connection. This does not consume a connection slot, so will not
     * block or throw {@link InterruptedException}. Use only if you have already
     * acquired a connection slot using {@link #connect(SocketAddress, IoSessionInitializer)}.
     * 
     * @param remoteAddress
     * @param init
     * @return
     */
    public abstract ConnectFuture reConnect(SocketAddress remoteAddress, IoSessionInitializer<? extends ConnectFuture> init, IoHandler handler);

    /**
     * Dispose of any resources that are held by the connection.
     */
    protected abstract void dispose();

    /**
     * Free up the resources used by a connection and connection factory.
     * @param factory
     * @param connection
     */
    public static final void dispose(ConnectionFactory factory) {
        // If the reference count on the factory is zero...
        if (--factory.m_references <= 0) {
            // ... then remove it from the map of available connectors 
            synchronized (s_connectorPool) {
                LOG.debug("Disposing of factory {} for interval {}", factory, factory.m_timeout);
                Iterator<Entry<Integer, ConnectionFactory>> i = s_connectorPool.entrySet().iterator();
                while(i.hasNext()) {
                    if(i.next().getValue() == factory) {
                        i.remove();
                    }
                }
            }

            // Call dispose on the factory itself now that there are no references to it
            factory.dispose();
        }
    }

}
