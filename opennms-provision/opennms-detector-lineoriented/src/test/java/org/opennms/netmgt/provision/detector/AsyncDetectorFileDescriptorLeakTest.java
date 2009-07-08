/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.mina.core.future.IoFutureListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class AsyncDetectorFileDescriptorLeakTest implements ApplicationContextAware {
    
    private SimpleServer m_server;
    private TcpDetector m_detector;
    private ApplicationContext m_applicationContext;
    
    @Before
    public void setUp() throws Exception {
        m_detector  = getDetector(TcpDetector.class);
        m_detector.setServiceName("TCP");
        m_detector.setTimeout(1000);
        m_detector.init();
    }
    
    @BeforeClass
    public static void beforeTest(){
        System.setProperty("org.opennms.netmgt.provision.maxConcurrentConnectors", "2000");
    }
    
    @After
    public void tearDown() throws IOException {
        if(m_server != null){
            m_server.stopServer();
        }
        
    }
    
    @Test
    @Repeat(10000)
    public void testSucessServer() throws Exception {
        m_server = new SimpleServer() {
            
            public void onInit() {
               setBanner("Winner");
            }
            
        };
        m_server.init();
        m_server.startServer();
        
        m_detector.setPort(m_server.getLocalPort());
        
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor());
        future.addListener(new IoFutureListener<DetectFuture>() {

            public void operationComplete(DetectFuture future) {
                TcpDetector detector = m_detector;
                m_detector = null;
                detector.dispose();
            }
            
        });
        
        future.awaitUninterruptibly();
        assertNotNull(future);
        assertTrue(future.isServiceDetected());
       
    }
    
    @Test
    @Repeat(10000)
    public void testNoServerPresent() throws Exception {
        
        m_detector.setPort(1999);
        
        DetectFuture future = m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor());
        future.addListener(new IoFutureListener<DetectFuture>() {

            public void operationComplete(DetectFuture future) {
                TcpDetector detector = m_detector;
                m_detector = null;
                detector.dispose();
            }
            
        });
        assertNotNull(future);
        future.awaitUninterruptibly();
        assertFalse(future.isServiceDetected());
        
        
        
        System.err.println("Finish test");
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
    
    private TcpDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (TcpDetector)bean;
    }
}
