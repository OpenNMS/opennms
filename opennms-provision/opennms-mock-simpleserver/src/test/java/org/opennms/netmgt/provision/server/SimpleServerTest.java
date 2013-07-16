/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
