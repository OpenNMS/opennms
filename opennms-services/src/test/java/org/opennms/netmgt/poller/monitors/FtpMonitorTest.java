/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;

public class FtpMonitorTest extends TestCase {
    private FtpMonitor m_monitor = new FtpMonitor();
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private static int TIMEOUT = 2000;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_serverSocket = new ServerSocket();
        m_serverSocket.bind(null); // don't care what address, just gimme a port
    }

    @Override
    protected void tearDown() throws Exception {
        if (m_serverSocket != null && !m_serverSocket.isClosed()) {
            m_serverSocket.close();
        }
        
        if (m_serverThread != null) {
            m_serverThread.join(1500);
        }
        
        super.tearDown();
    }

    // Let's not depend on external systems if we don't have to
    public void SKIPtestMonitorOnOpennmsOrgFtpSuccess() throws Exception {
        PollStatus status = m_monitor.poll(new MockMonitoredService(1, "Node One", InetAddressUtils.addr("ftp.opennms.org"), "FTP"), new HashMap<String,Object>());
        assertTrue("status should be available (Up), but is: " + status, status.isAvailable());
    }

    // Let's not depend on external systems if we don't have to
    public void SKIPtestMonitorFailureOnRandomFtp() throws Exception {
        PollStatus status = m_monitor.poll(new MockMonitoredService(1, "Node One", InetAddressUtils.addr("1.1.1.1"), "FTP"), new HashMap<String,Object>());
        assertTrue("status should be unavailable (Down), but is: " + status, status.isUnavailable());
    }

    public void testMonitorSuccess() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    s.getOutputStream().write("220 Hello!!!\r\n".getBytes());
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = r.readLine();
                    if (command.equals("QUIT")) {
                        s.getOutputStream().write("221 See ya\r\n".getBytes());
                    }
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
        
        m_serverThread.start();
        
        PollStatus status = doPoll();
        assertTrue("status should be available (Up), but is: " + status, status.isAvailable());
    }
    
    public void testMonitorFailureWithBogusResponse() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    s.getOutputStream().write("Go away!".getBytes());
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
        
        m_serverThread.start();
        
        PollStatus status = doPoll();
        assertTrue("status should be unavailable (Down), but is: " + status, status.isUnavailable());
    }
    
    public void testMonitorFailureWithNoResponse() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    m_serverSocket.accept();
                    Thread.sleep(3000);
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
        
        m_serverThread.start();
        
        PollStatus status = doPoll();
        assertTrue("status should be unavailable (Down), but is: " + status, status.isUnavailable());
    }
    
    public void testMonitorFailureWithClosedPort() throws Exception {
        m_serverSocket.close();
        
        PollStatus status = doPoll();
        assertTrue("status should be unavailable (Down), but is: " + status, status.isUnavailable());
    }

    private PollStatus doPoll() throws UnknownHostException {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("port", m_serverSocket.getLocalPort());
        m.put("retries", 0);
        m.put("timeout", TIMEOUT);
        PollStatus status = m_monitor.poll(new MockMonitoredService(1, "Node One", m_serverSocket.getInetAddress(), "FTP"), m);
        return status;
    }
}
