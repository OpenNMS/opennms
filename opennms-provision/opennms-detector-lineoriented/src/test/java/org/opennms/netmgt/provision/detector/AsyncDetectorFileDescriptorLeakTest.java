/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
@Ignore
public class AsyncDetectorFileDescriptorLeakTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(AsyncDetectorFileDescriptorLeakTest.class);
    private SimpleServer m_server;
    private ServerSocket m_socket;

    @Before
    public void setUp() {
        // Set the logging to INFO so that it doesn't OutOfMemory Eclipse with logs :)
        MockLogAppender.setupLogging(true, "INFO");
    }

    private static AsyncServiceDetector getNewDetector(int port, String bannerRegex) {
        TcpDetector detector = new TcpDetector();
        detector.setServiceName("TCP");
        detector.setPort(port);
        // Three seconds
        detector.setTimeout(3000);
        // Three seconds
        detector.setIdleTime(3000);
        detector.setBanner(bannerRegex);
        detector.setRetries(3);
        detector.init();
        return detector;
    }
    
    @After
    public void tearDown() throws IOException {
        if(m_server != null){
            m_server.stopServer();
            m_server = null;
        }
        
    }

    private void setUpSocket() throws Exception {
        m_socket = new ServerSocket();
        m_socket.bind(null);
    }

    private void setUpServer(final String banner) throws Exception {
        setUpServer(banner, 0);
    }

    private void setUpServer(final String banner, final int bannerDelay) throws Exception {
        m_server = new SimpleServer() {
            
            @Override
            public void onInit() {
                if (banner != null) {
                    setBanner(banner);
                    setBannerDelay(bannerDelay);
                }
            }
            
        };

        // No timeout
        m_server.setTimeout(0);
        //m_server.setThreadSleepLength(0);
        m_server.init();
        m_server.startServer();
    }
    
    @Test
    public void testDetectorTimeoutWaitingForBanner() throws Throwable {
        // Start a socket that doesn't have a thread servicing it
        setUpSocket();
        final int port = m_socket.getLocalPort();
        final InetAddress address = m_socket.getInetAddress();

        AsyncServiceDetector detector = getNewDetector(port, "Hello");

        assertNotNull(detector);

        final DetectFuture future = (DetectFuture)detector.isServiceDetected(address);

        assertNotNull(future);
        future.awaitFor();
        if (future.getException() != null) {
            LOG.debug("got future exception", future.getException());
            throw future.getException();
        }
        assertFalse("False positive during detection!!", future.isServiceDetected());
        assertNull(future.getException());
    }
    
    /**
     * TODO: This test will fail if there are more than a few milliseconds of delay 
     * between the characters of the banner. We need to fix this behavior.
     */
    @Test
    public void testDetectorBannerTimeout() throws Throwable {
        // Add 50 milliseconds of delay in between sending bytes of the banner
        setUpServer("Banner", 50);
        final int port = m_server.getLocalPort();
        final InetAddress address = m_server.getInetAddress();

        AsyncServiceDetector detector = getNewDetector(port, "Banner");

        assertNotNull(detector);

        final DetectFuture future = (DetectFuture)detector.isServiceDetected(address);

        assertNotNull(future);
        future.awaitFor();
        if (future.getException() != null) {
            LOG.debug("got future exception", future.getException());
            throw future.getException();
        }
        assertTrue("False negative during detection!!", future.isServiceDetected());
        assertNull(future.getException());
    }
    
    @Test
    public void testSuccessServer() throws Throwable {
        setUpServer("Winner");
        final int port = m_server.getLocalPort();
        final InetAddress address = m_server.getInetAddress();

        int i = 0;
        while (i < 30000) {
            LOG.info("current loop: {}", i);

            AsyncServiceDetector detector = getNewDetector(port, ".*");

            assertNotNull(detector);

            final DetectFuture future = (DetectFuture)detector.isServiceDetected(address);

            assertNotNull(future);
            future.awaitFor();
            if (future.getException() != null) {
                LOG.debug("got future exception", future.getException());
                throw future.getException();
            }
            assertTrue("False negative during detection!!", future.isServiceDetected());
            assertNull(future.getException());

            i++;
        }
    }

    @Test
    public void testBannerlessServer() throws Throwable {
        // No banner
        setUpServer(null);
        final int port = m_server.getLocalPort();
        final InetAddress address = m_server.getInetAddress();

        int i = 0;
        while (i < 30000) {
            LOG.info("current loop: {}", i);

            AsyncServiceDetector detector = getNewDetector(port, null);

            assertNotNull(detector);

            final DetectFuture future = (DetectFuture)detector.isServiceDetected(address);

            assertNotNull(future);
            future.awaitFor();
            if (future.getException() != null) {
                LOG.debug("got future exception", future.getException());
                throw future.getException();
            }
            assertTrue("False negative during detection!!", future.isServiceDetected());
            assertNull(future.getException());

            i++;
        }
    }

    @Test
    @Repeat(10000)
    public void testNoServerPresent() throws Exception {
        AsyncServiceDetector detector = getNewDetector(1999, ".*");
        LOG.info("Starting testNoServerPresent with detector: {}\n", detector);
        
        final DetectFuture future = detector.isServiceDetected(InetAddressUtils.getLocalHostAddress());
        assertNotNull(future);
        future.awaitFor();
        assertFalse("False positive during detection!!", future.isServiceDetected());
        assertNull(future.getException());
        
        LOG.info("Finished testNoServerPresent with detector: {}\n", detector);
    }
}
