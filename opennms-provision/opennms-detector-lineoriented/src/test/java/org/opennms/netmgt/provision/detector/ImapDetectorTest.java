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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.ImapDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
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
    
    @Test(timeout=90000)
    public void testServerSuccess() throws Exception{
        m_server  = new SimpleServer() {
            
            @Override
            public void onInit() {
                setBanner("* OK THIS IS A BANNER FOR IMAP");
                addResponseHandler(contains("LOGOUT"), shutdownServer("* BYE\r\nONMSCAPSD OK"));
            }
        };
        
        m_server.init();
        m_server.startServer();
        
        Thread.sleep(100); // make sure the server is really started
        
        try {
            m_detector.setPort(m_server.getLocalPort());
            m_detector.setIdleTime(1000);
            
            //assertTrue(m_detector.isServiceDetected(m_server.getInetAddress()));
            DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
            assertNotNull(future);
            
            future.awaitForUninterruptibly();
            
            
            assertTrue(future.isServiceDetected());
        } finally {
            m_server.stopServer();
        }
    }
    
    @Test(timeout=90000)
    public void testDetectorFailUnexpectedBanner() throws Exception{
        m_server  = new SimpleServer() {
            
            @Override
            public void onInit() {
                setBanner("* NOT OK THIS IS A BANNER FOR IMAP");
            }
        };
        
        m_server.init();
        m_server.startServer();
        
        try {
            m_detector.setPort(m_server.getLocalPort());
            
            //assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
            
            DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
            assertNotNull(future);
            
            future.awaitForUninterruptibly();
            
            assertFalse(future.isServiceDetected());
        } finally {
            m_server.stopServer();
        }
    }
    
    @Test(timeout=90000)
    public void testDetectorFailUnexpectedLogoutResponse() throws Exception{
        m_server  = new SimpleServer() {
            
            @Override
            public void onInit() {
                setBanner("* NOT OK THIS IS A BANNER FOR IMAP");
                addResponseHandler(contains("LOGOUT"), singleLineRequest("* NOT OK"));
            }
        };
        
        m_server.init();
        m_server.startServer();
        
        try {
            m_detector.setPort(m_server.getLocalPort());
            
            //assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
            
            DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
            assertNotNull(future);
            
            future.awaitForUninterruptibly();
            
            assertFalse(future.isServiceDetected());
        } finally {
            m_server.stopServer();
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
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
