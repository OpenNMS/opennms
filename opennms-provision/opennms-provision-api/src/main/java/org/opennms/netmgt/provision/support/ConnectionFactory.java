package org.opennms.netmgt.provision.support;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.opennms.core.utils.LogUtils;

/**
 * <p>
 * Factory for encapsulating a NioSocketConnector in such a way as to allow us
 * to have a Semaphore limiting the number of active Connections across all
 * Connectors.
 * </p>
 * 
 * <p>
 * There will be one ConnectionFactory for each discrete connection timeout
 * value.
 * </p>
 * 
 * <p>
 *  Adapted from original ConnectorFactory.
 * </p>
 *
 * @author ranger
 * @author Duncan Mackintosh
 * @version $Id: $
 */
public class ConnectionFactory {
    
	/** Map of timeoutInMillis to a ConnectionFactory with that timeout */
    private static ConcurrentHashMap<Integer, ConnectionFactory> s_connectorPool 
    	= new ConcurrentHashMap<Integer, ConnectionFactory>();
	
    
    // Semaphore for number of connections (large, in the hundreds).
    private static Semaphore s_availableConnections;
    private static int s_connectionExecutionRetries = 3;
    
    static {
        init();
    }

    public static void init() {
        if(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnections") != null){
            
            if(Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnections")) == 0){
                s_availableConnections = null;
            }else{
                s_availableConnections = new Semaphore(Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnections", "2000")));
            }
        }
        
        s_connectionExecutionRetries = Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnectors", "3"));
    }

    /**
     * Count the number of references to this Factory so we can dispose it
     * when there are no active references
     */
    private int m_references = 0;

    /**
     * The actual connector
     */
    private NioSocketConnector m_connector;
    
    /**
     * A mutex that protects the connector instance since we must dispose() and 
     * recreate it if it encounters errors.
     */
    private final Object m_connectorMutex = new Object();

    private final long m_timeout;
    
    /**
     * Create a new factory. Private because one should use {@link #getFactory(int)}
     */
    private ConnectionFactory(int timeoutInMillis) {
        m_timeout = timeoutInMillis;
        synchronized (m_connectorMutex) {
            m_connector = getSocketConnector(m_timeout);
        }
    }
    
    private static final NioSocketConnector getSocketConnector(long timeout) {
        NioSocketConnector connector = new NioSocketConnector();
        connector.setHandler(new SessionDelegateIoHandler());
        connector.setConnectTimeoutMillis(timeout);
        return connector;
    }

    /**
     * Get a new ConnectionFactory. If there is already a Factory with the
     * desired timeout, you will get that one; otherwise a new one is created.
     * <p>
     * If org.opennms.netmgt.provision.maxConcurrentConnectors is set, this may
     * block until a connector is available.
     * 
     * @param timeoutInMillis
     * 		Connection timeout
     * @return
     * 		An appropriate Factory
     */
    public static ConnectionFactory getFactory(int timeoutInMillis) {
        synchronized (s_connectorPool) {
            ConnectionFactory factory = s_connectorPool.get(timeoutInMillis);
            if (factory == null) {
                LogUtils.debugf(ConnectionFactory.class, "Creating a ConnectionFactory for timeout %d, there are %d factories total", timeoutInMillis, s_connectorPool.size());
                ConnectionFactory newFactory = new ConnectionFactory(timeoutInMillis);
                factory = s_connectorPool.putIfAbsent(timeoutInMillis, newFactory);
                // If there was no previous value for the factory in the map...
                if (factory == null) {
                    // ...then use the new value.
                    factory = newFactory;
                } else {
                    LogUtils.debugf(ConnectionFactory.class, "ConnectionFactory for timeout %d was already created in another thread!", timeoutInMillis);
                    // Dispose of the new unused factory
                    dispose(newFactory);
                }
            }
            factory.m_references++;
            return factory;
        }
    }

    /**
     * Connect to a remote socket. If org.opennms.netmgt.provision.maxConcurrentConnections
     * is set, this may block until a connection slot is available.
     * <p>
     * You must dispose both the ConnectionFactory and ConncetFuture when done
     * by calling {@link #dispose(ConnectionFactory, ConnectFuture)}.
     * 
     * @param remoteAddress
     * 		Destination address
     * @param init
     * 		Initialiser for the IoSession
     * @return
     * 		ConnectFuture from a Mina connect call
     * @throws IOException 
     */
    public ConnectFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> init) throws IOException {
        if (s_availableConnections != null) {
            s_availableConnections.acquireUninterruptibly();
        }
        for (int retries = 0; retries < s_connectionExecutionRetries; retries++) { 
            synchronized (m_connectorMutex) {
                if (m_connector == null) {
                    // Sanity check for null connector instance
                    LogUtils.warnf(this, "Found a null NioSocketConnector, creating a new one");
                    m_connector = getSocketConnector(m_timeout);
                    continue;
                } else if (m_connector.isDisposed() || m_connector.isDisposing()) {
                    /*
                     * There appears to be a bug in MINA that allows newly-created NioSocketConnectors
                     * to internally reference an executor that is already shutting down. We need to
                     * check for this state and recreate the connector if necessary.
                     * 
                     * @see http://issues.opennms.org/browse/NMS-4846
                     */
                    LogUtils.warnf(this, "Found a disposed NioSocketConnector, creating a new one");
                    m_connector = getSocketConnector(m_timeout);
                    continue;
                }
                try {
                    /*
                     * Use the 3-argument call to connect(). If you use the 2-argument version without
                     * the localhost port, the call will end up doing a name lookup which seems to fail
                     * intermittently in unit tests.
                     *
                     * @see http://issues.opennms.org/browse/NMS-5309
                     */
                    return m_connector.connect(remoteAddress, localAddress, init);
                } catch (Throwable e) {
                    LogUtils.debugf(this, e, "Caught exception on factory %s, retrying: %s", this, e);
                    m_connector.dispose(true);
                    m_connector = getSocketConnector(m_timeout);
                }
            }
        }
        throw new IOException("Could not connect to socket because of excessive RejectedExecutionExceptions");
    }

    /**
     * Retry a connection. This does not consume a connection slot, so will not
     * block or throw {@link InterruptedException}. Use only if you have already
     * acquired a connection slot using {@link #connect(SocketAddress, IoSessionInitializer)}.
     * 
     * @param remoteAddress
     * @param init
     * @return
     */
    public ConnectFuture reConnect(SocketAddress remoteAddress, SocketAddress localAddress, IoSessionInitializer<? extends ConnectFuture> init) {
        synchronized (m_connectorMutex) {
            m_connector.dispose(true);
            m_connector = getSocketConnector(m_timeout);
            /*
             * Use the 3-argument call to connect(). If you use the 2-argument version without
             * the localhost port, the call will end up doing a name lookup which seems to fail
             * intermittently in unit tests.
             *
             * @see http://issues.opennms.org/browse/NMS-5309
             */
            return m_connector.connect(remoteAddress, localAddress, init);
        }
    }
    
    /**
     * Free up the resources used by a connection and connection factory.
     * @param factory
     * @param connection
     */
    public static void dispose(ConnectionFactory factory) {
        if (s_availableConnections != null) {
            s_availableConnections.release();
        }

        if (--factory.m_references <= 0) {
            LogUtils.debugf(factory, "Disposing of factory %s for interval %d", factory, factory.m_timeout);
            synchronized (s_connectorPool) {
                Iterator<Entry<Integer, ConnectionFactory>> i = s_connectorPool.entrySet().iterator();
                while(i.hasNext()) {
                    if(i.next().getValue() == factory) {
                        i.remove();
                    }
                }
            }

            synchronized (factory.m_connectorMutex) {
                factory.m_connector.dispose(true);
            }
        }
    }

}
