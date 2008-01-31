/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */

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
