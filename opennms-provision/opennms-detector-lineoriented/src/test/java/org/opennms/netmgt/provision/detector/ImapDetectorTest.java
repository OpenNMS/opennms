/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import org.opennms.netmgt.provision.detector.simple.ImapDetector;
import org.opennms.netmgt.provision.detector.simple.ImapDetectorFactory;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class ImapDetectorTest {

    @Autowired
    private ImapDetectorFactory m_detectorFactory;
    private ImapDetector m_detector = null;
    private SimpleServer m_server = null;

    @Before
    public void setUp() throws Exception{
        MockLogAppender.setupLogging();

        m_detector = m_detectorFactory.createDetector();
        m_detector.setServiceName("Imap");
        m_detector.setPort(143);
        m_detector.setTimeout(500);
        m_detector.init();
    }

    @After
    public void tearDown() throws Exception{
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }

    @Test(timeout=20000)
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

    @Test(timeout=20000)
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

    @Test(timeout=20000)
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
}
