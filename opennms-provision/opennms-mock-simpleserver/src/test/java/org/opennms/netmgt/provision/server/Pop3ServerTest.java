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
