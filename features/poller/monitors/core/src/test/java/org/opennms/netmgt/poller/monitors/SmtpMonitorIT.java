/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

/**
 * Implement a bare bones SMTP server that we can test our SmtpMonitor against.
 *
 * @author Ronald J. Roskens <ronald.roskens@gmail.com>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/emptyContext.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml"})
@JUnitConfigurationEnvironment
public class SmtpMonitorIT {

    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;

    @Before
    public void setUp() throws Exception {
        m_serverSocket = new ServerSocket();
        m_serverSocket.bind(null); // don't care what address, just gimme a port
        System.err.println("m_serverSocket.port: " + m_serverSocket.getLocalPort());
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
    public void testPoll() throws UnknownHostException, InterruptedException {
        m_serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    System.out.println("S: 220 localhost.localdomain ESMTP bogon");
                    s.getOutputStream().write("220 localhost.localdomain ESMTP bogon\r\n".getBytes());
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.startsWith("HELO ")) {
                        System.out.println("S: 250 Hello");
                        s.getOutputStream().write("250 Hello\r\n".getBytes());
                    }
                    command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.equals("QUIT")) {
                        System.out.println("S: 250 Hello");
                        s.getOutputStream().write("221 See ya\r\n".getBytes());
                    }
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        m_serverThread.start();

        ServiceMonitor sm = new SmtpMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Node One", InetAddressUtils.addr("127.0.0.1"), "SMTP");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port", m_serverSocket.getLocalPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(ps.isUp());
        assertFalse(ps.isDown());
    }

    @Test
    public void testPollCase1() throws UnknownHostException, InterruptedException {
        m_serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    System.out.println("S: 220-localhost.localdomain ESMTP bogon");
                    s.getOutputStream().write("220-localhost.localdomain ESMTP bogon\r\n".getBytes());
                    System.out.println("S: 220-send me mail soon!");
                    s.getOutputStream().write("220-send me mail soon!\r\n".getBytes());
                    System.out.println("S: 220 send me mail now!");
                    s.getOutputStream().write("220 send me mail now!\r\n".getBytes());
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.startsWith("HELO ")) {
                        System.out.println("S: 250 Hello");
                        s.getOutputStream().write("250 Hello\r\n".getBytes());
                    }
                    command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.equals("QUIT")) {
                        System.out.println("S: 250 Hello");
                        s.getOutputStream().write("221 See ya\r\n".getBytes());
                    }
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        m_serverThread.start();

        ServiceMonitor sm = new SmtpMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Node One", InetAddressUtils.addr("127.0.0.1"), "SMTP");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port", m_serverSocket.getLocalPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(ps.isUp());
        assertFalse(ps.isDown());
    }

    @Test
    public void testPollCase2() throws UnknownHostException, InterruptedException {
        m_serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    System.out.println("S: 220 localhost.localdomain ESMTP bogon");
                    s.getOutputStream().write("220 localhost.localdomain ESMTP bogon\r\n".getBytes());
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.startsWith("HELO ")) {
                        System.out.println("S: 250-Hello");
                        s.getOutputStream().write("250-Hello\r\n".getBytes());
                        System.out.println("S: 250 send me mail now!");
                        s.getOutputStream().write("250 send me mail now!\r\n".getBytes());
                    }
                    command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.equals("QUIT")) {
                        System.out.println("S: 250 Hello");
                        s.getOutputStream().write("221 See ya\r\n".getBytes());
                    }
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        m_serverThread.start();

        ServiceMonitor sm = new SmtpMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Node One", InetAddressUtils.addr("127.0.0.1"), "SMTP");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port", m_serverSocket.getLocalPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(ps.isUp());
        assertFalse(ps.isDown());
    }

    @Test
    public void testPollCase3() throws UnknownHostException, InterruptedException {
        m_serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    System.out.println("S: 220 localhost.localdomain ESMTP bogon");
                    s.getOutputStream().write("220 localhost.localdomain ESMTP bogon\r\n".getBytes());
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.startsWith("HELO ")) {
                        System.out.println("S: 250 Hello");
                        s.getOutputStream().write("250 Hello\r\n".getBytes());
                    }
                    command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.equals("QUIT")) {
                        System.out.println("S: 221-Goodbye, friend.");
                        s.getOutputStream().write("221-Goodbye, friend.\r\n".getBytes());
                        System.out.println("S: 221 See ya");
                        s.getOutputStream().write("221 See ya\r\n".getBytes());
                    }
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        m_serverThread.start();

        ServiceMonitor sm = new SmtpMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Node One", InetAddressUtils.addr("127.0.0.1"), "SMTP");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port", m_serverSocket.getLocalPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(ps.isUp());
        assertFalse(ps.isDown());
    }

    @Test
    public void testPollSvrStatus554() throws UnknownHostException, InterruptedException {
        m_serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    System.out.println("S: 554 localhost.localdomain ESMTP bogon");
                    s.getOutputStream().write("554 localhost.localdomain ESMTP bogon\r\n".getBytes());
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.equals("QUIT")) {
                        System.out.println("S: 221-Goodbye, friend.");
                        s.getOutputStream().write("221-Goodbye, friend.\r\n".getBytes());
                        System.out.println("S: 221 See ya");
                        s.getOutputStream().write("221 See ya\r\n".getBytes());
                    }
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        m_serverThread.start();

        ServiceMonitor sm = new SmtpMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Node One", InetAddressUtils.addr("127.0.0.1"), "SMTP");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port", m_serverSocket.getLocalPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(ps.isUnavailable());
        assertFalse(ps.isUp());
    }

    @Test
    public void testPollCaseForCRLFCheck() throws UnknownHostException, InterruptedException {
        m_serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    System.out.println("S: 220 localhost.localdomain ESMTP bogon");
                    // Having \r should fail in cheking for end of the line as the requirement is for CRLF
                    s.getOutputStream().write("220 localhost.localdomain ESMTP bogon\r".getBytes());
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.startsWith("HELO ")) {
                        System.out.println("S: 250 Hello");
                        s.getOutputStream().write("250 Hello\r\n".getBytes());
                    }
                    command = r.readLine();
                    System.out.println("C: " + command);
                    if (command.equals("QUIT")) {
                        System.out.println("S: 221-Goodbye, friend.");
                        s.getOutputStream().write("221-Goodbye, friend.\r\n".getBytes());
                        System.out.println("S: 221 See ya");
                        s.getOutputStream().write("221 See ya\r\n".getBytes());
                    }
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        m_serverThread.start();

        ServiceMonitor sm = new SmtpMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Node One", InetAddressUtils.addr("127.0.0.1"), "SMTP");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port", m_serverSocket.getLocalPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(ps.isUnavailable());
        assertFalse(ps.isUp());
    }

}
