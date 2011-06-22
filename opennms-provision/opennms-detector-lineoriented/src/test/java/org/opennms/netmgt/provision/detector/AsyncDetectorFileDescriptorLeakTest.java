/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import java.util.concurrent.atomic.AtomicReference;

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
import org.opennms.netmgt.provision.support.DefaultDetectFuture;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
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
    private AtomicReference<TcpDetector> m_detector = new AtomicReference<TcpDetector>();
    private ApplicationContext m_applicationContext;
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_detector.set(getDetector(TcpDetector.class));
        m_detector.get().setServiceName("TCP");
        m_detector.get().setTimeout(10000);
        m_detector.get().setBanner(".*");
        m_detector.get().init();
    }
    
    @BeforeClass
    public static void beforeTest(){
        System.setProperty("org.opennms.netmgt.provision.maxConcurrentConnectors", "2000");
    }
    
    @After
    public void tearDown() throws IOException {
        if(m_server != null){
            m_server.stopServer();
            m_server = null;
        }
        
    }

    private void setUpServer() throws Exception {
        m_server = new SimpleServer() {
            
            public void onInit() {
               setBanner("Winner");
            }
            
        };

        m_server.setTimeout(10000);
        m_server.init();
        m_server.startServer();
    }
    
    @Test
    public void testSucessServer() throws Throwable {
        setUpServer();
        final int port = m_server.getLocalPort();
        final InetAddress address = m_server.getInetAddress();
        for (int i = 0; i < 10000; i++) {
            setUp();
            System.err.println("current loop: " + i);
            assertNotNull(m_detector);
            
            final TcpDetector detector = m_detector.get();
            
            detector.setPort(port);
            
            final DefaultDetectFuture future = (DefaultDetectFuture)detector.isServiceDetected(address, new NullDetectorMonitor());
            future.addListener(new IoFutureListener<DetectFuture>() {
    
                public void operationComplete(final DetectFuture future) {
                    detector.dispose();
                }
                
            });
            
            future.awaitUninterruptibly();
            assertNotNull(future);
            if (future.getException() != null) {
                System.err.println("got future exception: " + future.getException());
                throw future.getException();
            }
            System.err.println("got value: " + future.getObjectValue());
            assertTrue(future.isServiceDetected());

            m_detector.set(null);
        }
    }
    
    @Test
    @Repeat(10000)
    public void testNoServerPresent() throws Exception {
        
        final TcpDetector detector = m_detector.get();
        detector.setPort(1999);
        
        final DetectFuture future = detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor());
        future.addListener(new IoFutureListener<DetectFuture>() {

            public void operationComplete(final DetectFuture future) {
                detector.dispose();
            }
            
        });
        assertNotNull(future);
        future.awaitUninterruptibly();
        assertFalse(future.isServiceDetected());
        
        
        m_detector.set(null);
        System.err.println("Finish test");
    }
    
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
    
    private TcpDetector getDetector(final Class<? extends ServiceDetector> detectorClass) {
        final Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (TcpDetector)bean;
    }
}
