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
import org.opennms.test.mock.MockLogAppender;


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
