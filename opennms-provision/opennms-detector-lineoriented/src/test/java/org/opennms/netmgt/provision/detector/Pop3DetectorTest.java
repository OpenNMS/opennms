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
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.Pop3Detector;
import org.opennms.netmgt.provision.detector.simple.Pop3DetectorFactory;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class Pop3DetectorTest {
    private SimpleServer m_server;

    @Autowired
    private Pop3DetectorFactory m_detectorFactory;
    private Pop3Detector m_detector;
 
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_server = new SimpleServer() {

            @Override
            public void onInit() {
                setBanner("+OK");
                addResponseHandler(startsWith("QUIT"), shutdownServer("+OK"));
                //setExpectedClose("QUIT", "+OK");
            }
        };
        m_server.init();
        m_server.startServer();

        m_detector = createDetector(m_server.getLocalPort());
    }

    @After
    public void tearDown() throws Exception {
        if(m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }

    @Test(timeout=20000)
    public void testSuccess() throws Exception {
        m_detector.setIdleTime(1000);
        assertTrue( doCheck( m_detector.isServiceDetected(m_server.getInetAddress())));
    }

    @Test(timeout=20000)
    public void testFailureWithBogusResponse() throws Exception {
        m_server.setBanner("Oh Henry");
        assertFalse( doCheck( m_detector.isServiceDetected( m_server.getInetAddress())));

    }

    @Test(timeout=20000)
    public void testMonitorFailureWithNoResponse() throws Exception {
        m_server.setBanner(null);
        assertFalse( doCheck( m_detector.isServiceDetected( m_server.getInetAddress())));

    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws Exception{
        m_detector = createDetector(9000);
        assertFalse( doCheck( m_detector.isServiceDetected( m_server.getInetAddress())));
    }

    private Pop3Detector createDetector(int port) {
        Pop3Detector detector = m_detectorFactory.createDetector();
        detector.setServiceName("POP3");
        detector.setTimeout(500);
        detector.setPort(port);
        detector.init();
        return detector;
    }

    private boolean  doCheck(DetectFuture future) throws Exception {
        future.awaitFor();
        return future.isServiceDetected();
    }
}
