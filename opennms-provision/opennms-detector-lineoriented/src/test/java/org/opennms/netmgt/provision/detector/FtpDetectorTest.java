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

package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.FtpDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class FtpDetectorTest {
    
    @Autowired
    private FtpDetector m_detector;

    private SimpleServer m_server;
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_detector.init();
       
        m_server = new SimpleServer() {
           
            @Override
            public void onInit() {
                setBanner("220 ProFTPD 1.3.0 Server (ProFTPD)");
                addResponseHandler(matches("quit"), shutdownServer("221 Goodbye."));
            }
        };
       
        m_server.init();
        m_server.startServer();
    }
    
    @After
    public void tearDown() throws Exception {
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
        m_detector.dispose();
    }
    
    
    @Test(timeout=90000)
    public void testDetectorSingleLineResponseSuccess() throws Exception {
        
        m_server.setBanner("220 ProFTPD 1.3.0 Server (ProFTPD)");
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setIdleTime(10000);
       assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress()))); 
    }
    
    @Test(timeout=90000)
    public void testDetectorMultilineSuccess() throws Exception {
       
        m_server.setBanner("220---------- Welcome to Pure-FTPd [TLS] ----------\r\n220-You are user number 1 of 50 allowed.\r\n220-Local time is now 07:47. Server port: 21.\r\n220 You will be disconnected after 15 minutes of inactivity.");
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setIdleTime(10000);

       assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress()))); 
    }
    
    @Test(timeout=90000)
    public void testFailureClosedPort() throws Exception {
        
        m_server.setBanner("WRONG BANNER");
        m_detector.setPort(1000); //m_server.getLocalPort()
        m_detector.setIdleTime(10000);
        
        DetectFuture df = m_detector.isServiceDetected(m_server.getInetAddress());
        assertFalse("Test should fail because the server closes before detection takes place", doCheck(df));
    
    }
    
    @Test(timeout=90000)
    public void testFailureNoBannerSent() throws Exception {
       m_server = new SimpleServer();
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setIdleTime(10000);
        
        DetectFuture df = m_detector.isServiceDetected(m_server.getInetAddress());
        assertFalse("Test should fail because the banner doesn't even get sent", doCheck(df));
    
    }
    
    private boolean doCheck(DetectFuture future) throws InterruptedException {
        
        future.awaitFor();
        
        return future.isServiceDetected();
    }
}
