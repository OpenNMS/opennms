package org.opennms.netmgt.provision.support;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class ConnectorFactory {
    
    Executor m_executor = Executors.newSingleThreadExecutor();
    
    public SocketConnector getConnector() {
        return createConnector(); 
    }

    public void dispose(final SocketConnector connector) {
       Runnable r = new Runnable(){

        public void run() {
            System.err.println("Disposing the connector");
            connector.dispose();
        }
           
       };
       
       m_executor.execute(r);
    }
    
    private SocketConnector createConnector(){
        return new NioSocketConnector();
    }

}
