package org.opennms.netmgt.provision.support;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.opennms.core.utils.ThreadCategory;

/**
 * <p>ConnectorFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ConnectorFactory {
    
    
    private static Semaphore s_available;
    
    static{
        if(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnectors") != null){
            
            if(Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnectors")) == 0){
                s_available = null;
            }else{
                s_available = new Semaphore(Integer.parseInt(System.getProperty("org.opennms.netmgt.provision.maxConcurrentConnectors", "2000")));
            }
        }
    }
    
    private static Executor s_executor = Executors.newSingleThreadExecutor();
    
    /**
     * <p>getConnector</p>
     *
     * @return a {@link org.apache.mina.transport.socket.SocketConnector} object.
     * @throws java.lang.InterruptedException if any.
     */
    public SocketConnector getConnector() throws InterruptedException {
        if(s_available != null){
            s_available.acquire();
        }
        return createConnector(); 
    }

    /**
     * <p>dispose</p>
     *
     * @param connector a {@link org.apache.mina.transport.socket.SocketConnector} object.
     */
    public void dispose(final SocketConnector connector) {
       Runnable r = new Runnable(){

        public void run() {
            ThreadCategory.getInstance(ConnectorFactory.class).debug("Disposing the connector");
            try{
                connector.dispose();
            }finally{
                if(s_available != null){
                    s_available.release();
                } 
            }
            
        }
           
       };
       
       s_executor.execute(r);
    }
    
    private SocketConnector createConnector() throws InterruptedException{
        return new NioSocketConnector(); //m_socketPool.getItem();
    }

}
