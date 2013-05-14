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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.CitrixDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Donald Desloge
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class CitrixDetectorTest implements ApplicationContextAware {
    
    private ApplicationContext m_applicationContext;
    private CitrixDetector m_detector;
    private SimpleServer m_server;
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_detector = getDetector(CitrixDetector.class);
        
        m_server = getServer();
        m_server.init();
        m_server.startServer();
        
        
    }
    
    @After
    public void tearDown() throws IOException {
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }
    
    @Test(timeout=90000)
    public void testMyDetector() throws Exception {
        m_detector.setPort(20000);
        m_detector.setIdleTime(10000);
        m_detector.init();
        
        //assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse(future.isServiceDetected());
    }
    
    @Test(timeout=90000)
    public void testDetectorFailWrongPort() throws Exception {
        m_detector.setPort(20000);
        m_detector.setIdleTime(10000);
        m_detector.init();
        
        //assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse(future.isServiceDetected());
    }
    
    @Test(timeout=90000)
    public void testDetectorSuccess() throws Exception {
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setIdleTime(10000);
        m_detector.init();
        
        //assertTrue(m_detector.isServiceDetected(m_server.getInetAddress()));
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertTrue(future.isServiceDetected());
    }
    
    private SimpleServer getServer() {
        return new SimpleServer() {
            
            @Override
            public void onInit() {
                setBanner("ICAICAICAICA");
            }
        };
    }
    
    private CitrixDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (CitrixDetector)bean;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
}
