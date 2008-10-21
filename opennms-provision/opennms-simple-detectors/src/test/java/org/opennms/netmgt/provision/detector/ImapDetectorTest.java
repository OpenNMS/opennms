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
package org.opennms.netmgt.provision.detector;

import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ImapDetectorTest {
    private AbstractDetector m_detector;
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private static int TIMEOUT = 2000; 
    
    
    @Before
    public void setUp() throws Exception{
        m_serverSocket = new ServerSocket();
        m_serverSocket.bind(null);
        
        m_detector = new ImapDetector();
        m_detector.setServiceName("Imap");
        m_detector.setPort(m_serverSocket.getLocalPort());
        m_detector.setTimeout(1000);
        m_detector.init();
    }
    
    @After
    public void tearDown() throws Exception{
        if (m_serverSocket != null && !m_serverSocket.isClosed()) {
            m_serverSocket.close();
        }
        
        if (m_serverThread != null) {
            m_serverThread.join(1500);
        }
    }
    
    @Test
    public void testSuccess(){
        
    }
    
}
