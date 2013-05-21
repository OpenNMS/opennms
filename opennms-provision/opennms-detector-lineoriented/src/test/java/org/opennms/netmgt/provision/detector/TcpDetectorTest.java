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

import java.io.IOException;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectFutureListener;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class TcpDetectorTest implements ApplicationContextAware {
    private SimpleServer m_server;
    private TcpDetector m_detector;
    private ApplicationContext m_applicationContext;
    private String m_serviceName;
    private int m_timeout;
    private String m_banner;
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    private void initializeDetector() {
        m_detector = getDetector(TcpDetector.class);
        m_detector.setServiceName(getServiceName());
        m_detector.setTimeout(getTimeout());
        m_detector.setBanner(getBanner());
        m_detector.init();
    }
    
    private void initializeDefaultDetector() {
        setServiceName("TCP");
        setTimeout(1000);
        setBanner(".*");
        
        initializeDetector();
    }
    
    private void intializeNullBannerDetector() {
        setServiceName("TCP");
        setTimeout(1000);
        setBanner(null);
        
        initializeDetector();
    }
    
    @After
    public void tearDown() throws IOException {
        if(m_server != null){
            m_server.stopServer();
            m_server = null;
        }
    }
    
    
    @Test(timeout=90000)
    public void testSuccessServer() throws Exception {
        initializeDefaultDetector();
        
        m_server = new SimpleServer() {
            
            @Override
            public void onInit() {
               setBanner("Hello");
            }
            
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        future.addListener(new DetectFutureListener<DetectFuture>() {

            @Override
            public void operationComplete(DetectFuture future) {
                TcpDetector detector = m_detector;
                m_detector = null;
                detector.dispose();
            }
            
        });
        
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertTrue(future.isServiceDetected());
    }

    
    
    @Test(timeout=90000)
    public void testFailureNoBannerSentWhenExpectingABanner() throws Exception {
        initializeDefaultDetector();
        
        m_server = new SimpleServer() {
            
            @Override
            public void onInit() {
            	
            }
            
        };
        m_server.init();
        m_server.startServer();
        
        m_detector.setPort(m_server.getLocalPort());

        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse("Test should fail because no banner was sent when expecting a banner to be sent",future.isServiceDetected());
    
    }
    
    @Test(timeout=90000)
    public void testFailureConnectionTimesOutWhenExpectingABanner() throws Exception {
        initializeDefaultDetector();
        
        m_server = new SimpleServer() {
            
            @Override
            public void onInit() {
                setTimeout(3000);
            }
            
        };
        m_server.init();
        m_server.startServer();
        
        m_detector.setPort(m_server.getLocalPort());

        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse("Test should fail because no banner was sent when expecting a banner to be sent",future.isServiceDetected());
    
    }
    
    @Test(timeout=90000)
    public void testSuccessNotExpectingBannerNoBannerSent() throws Exception {
        intializeNullBannerDetector();
        
        m_server = new SimpleServer() {
            
            @Override
            public void onInit() {
                setTimeout(3000);
            }
            
        };
        m_server.init();
        m_server.startServer();

        m_detector.setBanner(null);
        m_detector.setPort(m_server.getLocalPort());

        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertTrue("Test should pass if we don't set a banner property and nothing responds", future.isServiceDetected());
    
    }

    
    
    @Test(timeout=90000)
    public void testFailureClosedPort() throws Exception {
        initializeDefaultDetector();
        
        m_server = new SimpleServer() {
            
            @Override
            public void onInit() {
               setBanner("BLIP");
            }
            
        };
        m_server.init();
        //m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
        //assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress()));
        
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse(future.isServiceDetected());
    
    }

    /**
     * I think that this test is redundant with {@link #testFailureClosedPort()} since neither
     * server is actually started. The detector just times out on both connections.
     */
    @Test(timeout=90000)
    public void testServerCloses() throws Exception{
        initializeDefaultDetector();
        
        m_server = new SimpleServer() {
            
            @Override
            public void onInit() {
               shutdownServer("Closing");
            }
            
        };
        m_server.init();
        //m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
        //assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress()));
        
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse(future.isServiceDetected());
    }
    
    @Test(timeout=90000)
    public void testNoServerPresent() throws Exception {
        initializeDefaultDetector();
        
        m_detector.setPort(1999);
        //assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress()));
        DetectFuture future = m_detector.isServiceDetected(InetAddress.getLocalHost());
        future.addListener(new DetectFutureListener<DetectFuture>() {

            @Override
            public void operationComplete(DetectFuture future) {
                TcpDetector detector = m_detector;
                m_detector = null;
                detector.dispose();
            }
            
        });
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse(future.isServiceDetected());
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
    
    private TcpDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (TcpDetector)bean;
    }

    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }

    public String getServiceName() {
        return m_serviceName;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setBanner(String banner) {
        m_banner = banner;
    }

    public String getBanner() {
        return m_banner;
    }
}
