/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

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

        m_detector = m_detectorFactory.createDetector(new HashMap<>());
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
