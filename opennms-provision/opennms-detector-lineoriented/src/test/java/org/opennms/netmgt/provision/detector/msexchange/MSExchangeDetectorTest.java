/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.provision.detector.msexchange;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class MSExchangeDetectorTest {
    
    private static String TEST_BANNER = "Microsoft Exchange";
    
    @Autowired
    MSExchangeDetector m_detector;
    
    SimpleServer m_pop3Server;
    SimpleServer m_imapServer;
    
    
    @Before
    public void setUp() throws Exception{
        MockLogAppender.setupLogging();

        m_pop3Server = new SimpleServer(){
            
            @Override
            public void onInit(){
                setBanner(TEST_BANNER);
            }
        };
        m_pop3Server.init();
        m_pop3Server.startServer();
        
        
        m_imapServer = new SimpleServer(){
            
            @Override
            public void onInit(){
                setBanner(TEST_BANNER);
            }
        };
        m_imapServer.init();
        m_imapServer.startServer();
        
    }
    
    @After
    public void tearDown() throws IOException{
        if(m_imapServer != null){
            m_imapServer.stopServer();
            m_imapServer = null;
        }
        
        if(m_pop3Server != null){
            m_pop3Server.stopServer();
            m_pop3Server = null;
        }
    }
    
    @Test
    public void testDetectorWired(){
        assertNotNull(m_detector);
    }
    
    @Test
    public void testDetectorSuccess() throws UnknownHostException{
        m_detector.setImapPort(m_imapServer.getLocalPort());
        m_detector.setPop3Port(m_pop3Server.getLocalPort());
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(m_pop3Server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorSuccessPop3FailImap() throws IOException{
        m_imapServer.stopServer();
        m_detector.setPop3Port(m_pop3Server.getLocalPort());
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(m_pop3Server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorSuccessImapFailPop3() throws IOException{
        m_pop3Server.stopServer();
        m_detector.setImapPort(m_imapServer.getLocalPort());
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(m_pop3Server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailWrongPort(){
        m_detector.setImapPort(9000);
        m_detector.setPop3Port(9001);
        m_detector.onInit();
        assertFalse(m_detector.isServiceDetected(m_pop3Server.getInetAddress(), new NullDetectorMonitor()));
    }
}
