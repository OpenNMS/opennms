/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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
