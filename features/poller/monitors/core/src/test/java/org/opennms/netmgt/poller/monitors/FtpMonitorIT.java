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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Ignore;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
    "classpath:/META-INF/opennms/emptyContext.xml"
})
public class FtpMonitorIT {
    private FtpMonitor m_monitor = new FtpMonitor();
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private static int TIMEOUT = 2000;

    @Before
    public void setUp() throws Exception {
        m_serverSocket = new ServerSocket();
        m_serverSocket.bind(null); // don't care what address, just gimme a port
        MockLogAppender.setupLogging(true);
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
    public void testMonitorSuccess() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            @Override
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
    
    @Test
    public void testParamSubstitution() throws Exception {
        FtpMonitor mon = new FtpMonitor();
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("port", m_serverSocket.getLocalPort());
        m.put("retries", 0);
        m.put("timeout", TIMEOUT);
        m.put("userid", "{ipAddr}");
        m.put("password", "{nodeLabel}");
        MockMonitoredService svc = new MockMonitoredService(1, "Node One", InetAddress.getByName("127.0.0.1"), "FTP");
        Map<String, Object> subbedParams = mon.getRuntimeAttributes(svc, m);
        assertTrue(subbedParams.get("subbed-userid").equals("127.0.0.1"));
        assertTrue(subbedParams.get("subbed-password").equals("Node One"));
    }

    @Test
    public void testMonitorFailureWithBogusResponse() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            @Override
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
    
    @Test
    public void testMonitorFailureWithNoResponse() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            @Override
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
    
    @Test
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
