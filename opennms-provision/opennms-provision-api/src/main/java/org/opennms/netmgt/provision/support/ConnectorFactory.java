package org.opennms.netmgt.provision.support;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class ConnectorFactory {
    
    Semaphore m_available = new Semaphore(Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnectors", "2000")));
    
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
