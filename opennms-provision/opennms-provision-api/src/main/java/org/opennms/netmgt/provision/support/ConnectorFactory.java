/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.support;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.opennms.core.utils.ThreadCategory;

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
    
    public SocketConnector getConnector() throws InterruptedException {
        if(s_available != null){
            s_available.acquire();
        }
        return createConnector(); 
    }

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
