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
 * Modifications;
 * Created 10/16/2008
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


public class Pop3DetectorTest {
    private AbstractDetector m_detector;
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private static int TIMEOUT = 2000;
    
    @Before
    public void setUp() throws Exception {
                
        m_serverSocket = new ServerSocket();
        m_serverSocket.bind(null); // don't care what address, just gimme a port

        m_detector = new Pop3Detector();
        m_detector.setServiceName("POP3");
        m_detector.setPort(m_serverSocket.getLocalPort());
        m_detector.setTimeout(1000);
        m_detector.init();
    }

    @After
    public void tearDown() throws Exception {
        if (m_serverSocket != null && !m_serverSocket.isClosed()) {
            m_serverSocket.close();
        }
        
        if (m_serverThread != null) {
            m_serverThread.join(1500);
        }
        
    }
    
    @Test
    public void testSuccess() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    OutputStream out = s.getOutputStream();
                    out.write("+OK\r\n".getBytes());
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = r.readLine();
                    System.out.println(command);
                    if (command != null && command.equals("QUIT")) {
                        out.write("+OK\r\n".getBytes());
                    }
                    s.close();
                } catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
        
        m_serverThread.start();
        
        assertTrue("Test for protocol Pop3 should have passed", doCheck());
    }
    
    @Test
    public void testFailureWithBogusResponse() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    s.getOutputStream().write("Go away!".getBytes());
                    s.close();
                } catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
        
        m_serverThread.start();
        
        assertFalse("Test for protocol FTP should have failed", doCheck());
    }
    
    @Test
    public void testMonitorFailureWithNoResponse() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    m_serverSocket.accept();
                    Thread.sleep(TIMEOUT);
                    m_serverSocket.close();
                } catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
        
        m_serverThread.start();
        
        assertFalse("Test for protocol FTP should have failed", doCheck());
    }
    
    @Test
    public void testMonitorFailureWithClosedPort() throws Exception {
        m_serverSocket.close();
        
        assertFalse("Test for protocol FTP should have failed", doCheck());
    }

    private boolean  doCheck() {
        return m_detector.isServiceDetected(m_serverSocket.getInetAddress(), new NullDetectorMonitor());
    }
}
