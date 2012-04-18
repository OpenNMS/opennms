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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.mina.core.future.IoFutureListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.ConnectionFactory;
import org.opennms.netmgt.provision.support.DefaultDetectFuture;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class AsyncDetectorFileDescriptorLeakTest {

    private SimpleServer m_server;
    private TcpDetector m_detector;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();

        m_detector = new TcpDetector();
        m_detector.setServiceName("TCP");
        m_detector.setTimeout(10000);
        m_detector.setBanner(".*");
        m_detector.init();
    }
    
    @BeforeClass
    public static void beforeTest(){
        System.setProperty("org.opennms.netmgt.provision.maxConcurrentConnectors", "2000");
        // Make sure that the ConnectionFactory reloads the system properties
        ConnectionFactory.init();
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

        // No timeout
        m_server.setTimeout(0);
        m_server.init();
        m_server.startServer();
    }
    
    @Test
    public void testSuccessServer() throws Throwable {
        setUpServer();
        final int port = m_server.getLocalPort();
        final InetAddress address = m_server.getInetAddress();

        final double connectionRate = 0.2;
        
        final long startTime = System.currentTimeMillis();

        int i = 0;
        while (i < 10000) {
            long now = Math.max(System.currentTimeMillis(), 1);
            double actualRate = ((double)i) / ((double)(now - startTime));
            LogUtils.debugf(this, "Expected Rate: %f Actual Rate: %f Events Sent: %d", connectionRate, actualRate, i);
            if (actualRate < connectionRate) {
                setUp();
                LogUtils.debugf(this, "current loop: %d", i);
                assertNotNull(m_detector);

                m_detector.setPort(port);

                final DefaultDetectFuture future = (DefaultDetectFuture)m_detector.isServiceDetected(address, new NullDetectorMonitor());
                /*
                future.addListener(new IoFutureListener<DetectFuture>() {
                    public void operationComplete(final DetectFuture future) {
                        m_detector.dispose();
                    }
                });
                */

                future.awaitUninterruptibly();
                assertNotNull(future);
                if (future.getException() != null) {
                    LogUtils.debugf(this, future.getException(), "got future exception");
                    throw future.getException();
                }
                LogUtils.debugf(this, "got value: %s", future.getObjectValue());
                assertTrue(future.isServiceDetected());

                i++;
            } else {
                Thread.sleep(5);
            }
        }
    }

    @Test
    @Repeat(10000)
    public void testNoServerPresent() throws Exception {
        m_detector.setPort(1999);
        System.err.printf("Starting testNoServerPresent with detector: %s\n", m_detector);
        
        final DetectFuture future = m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress(), new NullDetectorMonitor());
        /*
        future.addListener(new IoFutureListener<DetectFuture>() {

            public void operationComplete(final DetectFuture future) {
                m_detector.dispose();
            }
            
        });
        */
        assertNotNull(future);
        future.awaitUninterruptibly();
        assertFalse(future.isServiceDetected());
        assertNull(future.getException());
        
        System.err.printf("Finished testNoServerPresent with detector: %s\n", m_detector);
    }
}
