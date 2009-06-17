package org.opennms.netmgt.provision.support;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class ConnectorFactory {
    
    private static final int MAX_AVAILABLE_CONNECTORS = 2000;
    Semaphore m_available = new Semaphore(MAX_AVAILABLE_CONNECTORS);
    
    Executor m_executor = Executors.newSingleThreadExecutor();
    
    public SocketConnector getConnector() throws InterruptedException {
        m_available.acquire();
        return createConnector(); 
    }

    public void dispose(final SocketConnector connector) {
       Runnable r = new Runnable(){

        public void run() {
            System.err.println("Disposing the connector");
            
            connector.dispose();
            m_available.release();
        }
           
       };
       
       m_executor.execute(r);
    }
    
    private SocketConnector createConnector() throws InterruptedException{
        return new NioSocketConnector(); //m_socketPool.getItem();
    }

}
