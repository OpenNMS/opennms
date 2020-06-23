/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;


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
        }catch(Throwable e){
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
