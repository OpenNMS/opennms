/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.opennmsd;

public class TestDaemon implements Runnable {
    
    OpenNMSDaemon m_daemon;
    DefaultConfiguration m_config;
    DefaultEventForwarder m_forwarder;
    
    public TestDaemon() {
        m_daemon = new OpenNMSDaemon();
        m_config = new DefaultConfiguration();
        
        m_forwarder = new DefaultEventForwarder();
        m_forwarder.setOpenNmsHost("127.0.0.1");
      
        m_daemon.setConfiguration(m_config);
        m_daemon.setEventForwarder(m_forwarder);
        m_daemon.onInit();
        
    }

    private void addShutdownHook() {
        
        Runnable r = { 
                println "Stopping!"
                m_daemon.onStop();
        } as Runnable;
        
        Runtime.getRuntime().addShutdownHook(new Thread(r));
        
    }
    
    public void run() {
        for(i in 1..10) {
            NNMEvent e = TestNNMEvent.createEvent("category${i}", getSeverity(i), "event${i}", "192.168.1.${i}");
            m_daemon.onEvent(e);
            Thread.sleep(10000);
        }
        
    }
    
    public String getSeverity(int i) {
        if (i % 2) {
            return "Warning"
        } else {
            return "Normal"
        }
    }
    

    public static void main(String[] args) {
        
        TestDaemon daemon = new TestDaemon();
        daemon.addShutdownHook();
        
        daemon.run();
    }

}
