package org.opennms.netmgt.provision.support;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
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
 * <p>
 * There will be one ConnectionFactory for each discrete connection timeout
 * value.
 * </p>
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
	
    
    // Semaphores for number of connectors (small, probably ~ 20) and 
    // connections (large, in the hundreds).
    private static Semaphore s_availableConnectors;
    private static Semaphore s_availableConnections;
    private static int s_connectionExecutionRetries = 3;
    
    static{
        if(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnectors") != null){
            
            if(Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnectors")) == 0){
                s_availableConnectors = null;
            }else{
                s_availableConnectors = new Semaphore(Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnectors", "20")));
            }
        }
        
        if(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnections") != null){
            
            if(Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnections")) == 0){
                s_availableConnectors = null;
            }else{
                s_availableConnectors = new Semaphore(Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnections", "2000")));
            }
        }
        
        s_connectionExecutionRetries = Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnectors", "3"));
    }
    
    /**
     * Count the number of references to this Factory so we can dispose it
     * when there are no active references
     */
    private int m_references;
    /**
     * The actual connector
     */
    private NioSocketConnector m_connector;
    
    private final long m_timeout;
    
    /**
     * Create a new factory. Private because one should use {@link #getFactory(int)}
     */
    private ConnectionFactory(int timeoutInMillis) {
        m_timeout = timeoutInMillis;
        m_connector = getSocketConnector();
    }
    
    private final NioSocketConnector getSocketConnector() {
        NioSocketConnector connector = new NioSocketConnector();
        connector.setHandler(new SessionDelegateIoHandler());
        connector.setConnectTimeoutMillis(m_timeout);
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
        if (s_availableConnectors != null) {
            s_availableConnectors.acquireUninterruptibly();
        }
        ConnectionFactory factory = s_connectorPool.get(timeoutInMillis);
        if (factory == null) {
            LogUtils.debugf(ConnectionFactory.class, "Creating a ConnectionFactory for timeout %d, there are already %d factories", timeoutInMillis, s_connectorPool.size());
            ConnectionFactory newFactory = new ConnectionFactory(timeoutInMillis);
            factory = s_connectorPool.putIfAbsent(timeoutInMillis, newFactory);
            // If there was no previous value for the factory in the map...
            if (factory == null) {
                // ...then use the new value.
                factory = newFactory;
            } else {
                LogUtils.debugf(ConnectionFactory.class, "ConnectionFactory for timeout %d was already created in another thread!", timeoutInMillis);
            }
        }
        factory.m_references++;
        return factory;
    }

    /**
     * Connect to a remote socket. If org.opennms.netmgt.provision.maxConcurrentConnections
     * is set, this may block until a connection slot is available.
     * <p>
     * You must dispose both the ConnectionFactory and ConncetFuture when done
     * by calling {@link #dispose(ConnectionFactory, ConnectFuture)}.
     * 
     * @param destination
     * 		Destination address
     * @param init
     * 		Initialiser for the IoSession
     * @return
     * 		ConnectFuture from a Mina connect call
     * @throws IOException 
     */
    public ConnectFuture connect(SocketAddress destination, IoSessionInitializer<? extends ConnectFuture> init) throws IOException {
        if (s_availableConnections != null) {
            s_availableConnections.acquireUninterruptibly();
        }
        for (int retries = 0; retries < s_connectionExecutionRetries; retries++) { 
            try {
                synchronized (m_connector) {
                    return m_connector.connect(destination, init);
                }
            } catch (RejectedExecutionException e) {
                LogUtils.debugf(this, "Caught exception, retrying: %s", e);
                synchronized (m_connector) {
                    m_connector.dispose();
                    /*
                    while (!m_connector.isDisposed()) {
                        try { Thread.sleep(10); } catch (InterruptedException ex) {}
                    }
                    */
                    m_connector = getSocketConnector();
                    try { Thread.sleep(10); } catch (InterruptedException ex) {}
                }
            } catch (IllegalStateException e) {
                LogUtils.debugf(this, "Caught exception, retrying: %s", e);
                synchronized (m_connector) {
                    m_connector.dispose();
                    /*
                    while (!m_connector.isDisposed()) {
                        try { Thread.sleep(10); } catch (InterruptedException ex) {}
                    }
                    */
                    m_connector = getSocketConnector();
                    try { Thread.sleep(10); } catch (InterruptedException ex) {}
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
     * @param destination
     * @param init
     * @return
     */
    public ConnectFuture reConnect(SocketAddress destination, IoSessionInitializer<? extends ConnectFuture> init) {
        synchronized (m_connector) {
            m_connector.dispose();
            m_connector = getSocketConnector();
            return m_connector.connect(destination, init);
        }
    }
    
    /**
     * Free up the resources used by a connection and connection factory.
     * @param factory
     * @param connection
     */
    public static void dispose(ConnectionFactory factory, ConnectFuture connection) {
        if (s_availableConnections != null) {
            s_availableConnections.release();
        }

        if (--factory.m_references <= 0) {
            if (s_availableConnectors != null) {
                s_availableConnectors.release();
            }

            Iterator<Entry<Integer, ConnectionFactory>> i = s_connectorPool.entrySet().iterator();
            while(i.hasNext()) {
                if(i.next().getValue() == factory) {
                    i.remove();
                }
            }

            synchronized (factory.m_connector) {
                factory.m_connector.dispose();
            }
        }
    }

}
