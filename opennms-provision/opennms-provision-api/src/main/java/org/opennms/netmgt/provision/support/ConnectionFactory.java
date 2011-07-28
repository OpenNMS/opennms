package org.opennms.netmgt.provision.support;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.opennms.core.utils.LogUtils;

/**
 * Factory for encapsulating a NioSocketConnector in such a way as to allow us
 * to have a Semaphore limiting the number of active Connections across all
 * Connectors.
 * <p>
 * There will be one ConnectionFactory for each discrete connection timeout
 * value.
 * <p>
 *  Adapted from original ConnectorFactory.
 *
 * @author ranger, Duncan Mackintosh
 * @version $Id: $
 */
public class ConnectionFactory {
    
	/** Map of timeoutInMillis to a ConnectionFactory with that timeout */
    private static Map<Integer, ConnectionFactory> s_connectorPool 
    	= new HashMap<Integer, ConnectionFactory>();
	
    
    // Semaphores for number of connectors (small, probably ~ 20) and 
    // connections (large, in the hundreds).
    private static Semaphore s_availableConnectors;
    private static Semaphore s_availableConnections;
    
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
    
    /**
     * Create a new factory. Private because one should use {@link #getFactory(int)}
     */
    private ConnectionFactory(int timeoutInMillis) {
    	m_connector = new NioSocketConnector();
    	m_connector.setHandler(new SessionDelegateIoHandler());
    	m_connector.setConnectTimeoutMillis(timeoutInMillis);
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
    	LogUtils.debugf(ConnectionFactory.class, "Creating a ConnectionFactory for timeout %d, there are already %d factories", timeoutInMillis, s_connectorPool.size());
    	if (s_availableConnectors != null) {
    		s_availableConnectors.acquireUninterruptibly();
    	}
    	synchronized (s_connectorPool) {
        	ConnectionFactory w = s_connectorPool.get(timeoutInMillis);
        	if (w == null) {
        		w = new ConnectionFactory(timeoutInMillis);
        		s_connectorPool.put(timeoutInMillis, w);
        	}
        	w.m_references++;
        	return w;
		}
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
     */
    public ConnectFuture connect(SocketAddress destination, IoSessionInitializer<? extends ConnectFuture> init) {
    	if (s_availableConnections != null) {
    		s_availableConnections.acquireUninterruptibly();
    	}
    	return m_connector.connect(destination, init);
    }
    
    /**
     * Retry a connection. This does not consume a connection slot, so will not
     * block or throw IntrruptedExceptions. Use only if you have already
     * acquired a connection slot using {@link #connect(SocketAddress, IoSessionInitializer)}.
     * 
     * @param destination
     * @param init
     * @return
     */
    public ConnectFuture reConnect(SocketAddress destination, IoSessionInitializer<? extends ConnectFuture> init) {
    	return m_connector.connect(destination, init);
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
    		
    		factory.m_connector.dispose();
    	}
    }

}
