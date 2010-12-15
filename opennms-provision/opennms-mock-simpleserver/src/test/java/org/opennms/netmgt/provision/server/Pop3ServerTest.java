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
package org.opennms.netmgt.provision.server;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.test.mock.MockLogAppender;


public class Pop3ServerTest {
    private Pop3Server m_pop3Server;
    private Socket m_socket;
    private BufferedReader m_in;
    
    @Before
    public void setUp() throws Exception{
        MockLogAppender.setupLogging();
        m_pop3Server = new Pop3Server();
        try{
            m_pop3Server.init();
            m_pop3Server.startServer();
            m_socket = createSocketConnection(m_pop3Server.getInetAddress(), m_pop3Server.getLocalPort(), 1000);
            m_in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
        }catch(Exception e){
           throw new Exception(e); 
        }
    }
    
    @After
    public void tearDown() throws IOException{
        m_socket.close();
        m_pop3Server.stopServer();
    }
    
    @Test
    public void testServerBanner() throws Exception{
       String line = m_in.readLine();
       assertEquals("+OK", line);
    }
    
    @Test
    public void testServerBannerAndResponse() throws Exception{
       
        
        String line = m_in.readLine();
        System.out.println("banner: " + line);
        assertEquals("+OK", line);
        
        m_socket.getOutputStream().write("QUIT\r\n".getBytes());
        System.out.println("writing output QUIT");
        
        line = m_in.readLine();
        System.out.println("request response: " + line);
        assertEquals("+OK", line);
        
    }

    protected Socket createSocketConnection(InetAddress host, int port, int timeout) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeout);
        socket.setSoTimeout(timeout);
        return socket;
    }
    
}
