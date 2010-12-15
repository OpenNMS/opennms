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
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.ImapDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class ImapDetectorTest implements ApplicationContextAware {
    private ImapDetector m_detector = null;
    private SimpleServer m_server = null;
    private ApplicationContext m_applicationContext = null;
    
    @Before
    public void setUp() throws Exception{
        MockLogAppender.setupLogging();
        
        m_detector = getDetector(ImapDetector.class);
        m_detector.setServiceName("Imap");
        m_detector.setTimeout(1000);
        m_detector.init();
    }
    
    @After
    public void tearDown() throws Exception{
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }
    
    @Test
    public void testServerSuccess() throws Exception{
        m_server  = new SimpleServer() {
            
            public void onInit() {
                setBanner("* OK THIS IS A BANNER FOR IMAP");
                addResponseHandler(contains("LOGOUT"), shutdownServer("* BYE\r\nONMSCAPSD OK"));
            }
        };
        
        m_server.init();
        m_server.startServer();
        
        try {
            m_detector.setPort(m_server.getLocalPort());
            m_detector.setIdleTime(100);
            
            //assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
            DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor());
            assertNotNull(future);
            
            future.awaitUninterruptibly();
            
            assertTrue(future.isServiceDetected());
        } finally {
            m_server.stopServer();
        }
    }
    
    @Test
    public void testDetectorFailUnexpectedBanner() throws Exception{
        m_server  = new SimpleServer() {
            
            public void onInit() {
                setBanner("* NOT OK THIS IS A BANNER FOR IMAP");
            }
        };
        
        m_server.init();
        m_server.startServer();
        
        try {
            m_detector.setPort(m_server.getLocalPort());
            
            //assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
            
            DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor());
            assertNotNull(future);
            
            future.awaitUninterruptibly();
            
            assertFalse(future.isServiceDetected());
        } finally {
            m_server.stopServer();
        }
    }
    
    @Test
    public void testDetectorFailUnexpectedLogoutResponse() throws Exception{
        m_server  = new SimpleServer() {
            
            public void onInit() {
                setBanner("* NOT OK THIS IS A BANNER FOR IMAP");
                addResponseHandler(contains("LOGOUT"), singleLineRequest("* NOT OK"));
            }
        };
        
        m_server.init();
        m_server.startServer();
        
        try {
            m_detector.setPort(m_server.getLocalPort());
            
            //assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
            
            DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor());
            assertNotNull(future);
            
            future.awaitUninterruptibly();
            
            assertFalse(future.isServiceDetected());
        } finally {
            m_server.stopServer();
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
    
    private ImapDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (ImapDetector)bean;
    }
    
}
