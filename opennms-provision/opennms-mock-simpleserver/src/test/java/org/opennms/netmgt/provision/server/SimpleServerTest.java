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
package org.opennms.netmgt.provision.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;


public class SimpleServerTest {
    private Socket m_socket;
    private BufferedReader m_in;
    private OutputStream m_out;
    
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @After
    public void tearDown() throws IOException {
      m_socket.close();  
    }
    
    @Test
    public void testServerTimeout() throws Exception {
        SimpleServer server = new SimpleServer() {
            @Override
            public void onInit() {
                setTimeout(500);
                setBanner("+OK");
            }
        };
        server.init();
        server.startServer();        
        connectToServer(server);
        
        String line = m_in.readLine();
        assertEquals("+OK", line);
        
        // don't send a command and verify the socket gets closed eventually       
        assertNull(m_in.readLine());
    }
    
    @Test
    public void testServerWithCustomErrors() throws Exception {
        SimpleServer server = new SimpleServer() {
            @Override
            public void onInit() {
                setTimeout(1000);
                setBanner("+OK");
                addResponseHandler(matches("BING"), singleLineRequest("+GOT_BING"));
                addResponseHandler(matches("QUIT"), shutdownServer("+OK"));
                addResponseHandler(matches("APPLES"), singleLineRequest("+ORANGES"));
                addErrorHandler(errorString("GOT ERROR"));
            }
        };
        server.init();
        server.startServer();        
        connectToServer(server);
        String line = m_in.readLine();
        assertEquals("+OK", line);
        
        m_out.write("BING\r\n".getBytes());
        line = m_in.readLine();
        System.out.println("Line returned from Server: " + line);
        assertEquals("+GOT_BING",line);
        
        m_out.write("ORANGES\r\n".getBytes());
        line = m_in.readLine();
        System.out.println("Line returned from Server: " + line);
        assertEquals("GOT ERROR",line);
        
        m_out.write("APPLES\r\n".getBytes());
        line = m_in.readLine();
        System.out.println("Line returned from Server: " + line);
        assertEquals("+ORANGES",line);
        
        m_out.write("QUIT\r\n".getBytes());
        line = m_in.readLine();
        System.out.println("Line returned from Server: " + line);
        assertEquals("+OK", line);
        
        // don't send a command and verify the socket gets closed eventually
        
        assertNull(m_in.readLine());
    }
    
    @Test
    public void testMultipleRequestAndCloseServer() throws Exception {
        SimpleServer server = new SimpleServer() {
            @Override
            public void onInit() {
                setTimeout(1000);
                setBanner("+OK");
                addResponseHandler(matches("BING"), singleLineRequest("+GOT_BING"));
                addResponseHandler(matches("QUIT"), shutdownServer("+OK"));
                addResponseHandler(matches("APPLES"), singleLineRequest("+ORANGES"));
            }
        };
        server.init();
        server.startServer();        
        connectToServer(server);
        String line = m_in.readLine();
        assertEquals("+OK", line);
        
        m_out.write("BING\r\n".getBytes());
        line = m_in.readLine();
        assertEquals("+GOT_BING",line);
        
        m_out.write("APPLES\r\n".getBytes());
        line = m_in.readLine();
        assertEquals("+ORANGES",line);
        
        m_out.write("QUIT\r\n".getBytes());
        line = m_in.readLine();
        assertEquals("+OK", line);
        
        // don't send a command and verify the socket gets closed eventually
        
        assertNull(m_in.readLine());
    }
    
    @Test
    public void testServerQuitAndClose() throws Exception{
        //TODO
        SimpleServer server = new SimpleServer() {
            @Override
            public void onInit() {
                setTimeout(500);
                setBanner("+OK");
                addResponseHandler(matches("QUIT"), shutdownServer("+OK"));
            }
        };
        server.init();
        server.startServer();        
        connectToServer(server);
        String line = m_in.readLine();
        assertEquals("+OK", line);
        
        m_out.write("QUIT\r\n".getBytes());
        
        line = m_in.readLine();
        assertEquals("+OK", line);
        
        // don't send a command and verify the socket gets closed eventually
        
        assertNull(m_in.readLine());
    }
    
    @Test
    public void testServerNoBannerTimeout() throws Exception{
        SimpleServer server = new SimpleServer() {
            @Override
            public void onInit() {
                setTimeout(500);
            }
        };
        server.init();
        server.startServer();        
        connectToServer(server);
        
        // don't send a command and verify the socket gets closed eventually
        
        assertNull(m_in.readLine());
    
    }
    
    private void connectToServer(SimpleServer server) throws IOException {
        m_socket = createSocketConnection(server.getInetAddress(), server.getLocalPort(), 5000);
        m_in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
        m_out = m_socket.getOutputStream();
        
    }
    
    protected Socket createSocketConnection(InetAddress host, int port, int timeout) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeout);
        socket.setSoTimeout(timeout);
        return socket;
    }
}
