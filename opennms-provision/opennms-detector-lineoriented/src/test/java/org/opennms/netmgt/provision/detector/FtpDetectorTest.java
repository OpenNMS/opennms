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
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.FtpDetector;
import org.opennms.netmgt.provision.detector.simple.FtpDetectorFactory;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class FtpDetectorTest {

    @Autowired
    private FtpDetectorFactory m_detectorFactory;
    
    private FtpDetector m_detector;

    private SimpleServer m_server;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setTimeout(500);
        m_detector.init();

        m_server = new SimpleServer() {

            @Override
            public void onInit() {
                setBanner("220 ProFTPD 1.3.0 Server (ProFTPD)");
                addResponseHandler(matches("quit"), shutdownServer("221 Goodbye."));
            }
        };

        m_server.init();
        m_server.startServer();
    }

    @After
    public void tearDown() throws Exception {
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
        m_detector.dispose();
    }


    @Test(timeout=20000)
    public void testDetectorSingleLineResponseSuccess() throws Exception {

        m_server.setBanner("220 ProFTPD 1.3.0 Server (ProFTPD)");
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setIdleTime(10000);

        assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress()))); 
    }

    @Test(timeout=20000)
    public void testDetectorMultilineSuccess() throws Exception {

        m_server.setBanner("220---------- Welcome to Pure-FTPd [TLS] ----------\r\n220-You are user number 1 of 50 allowed.\r\n220-Local time is now 07:47. Server port: 21.\r\n220 You will be disconnected after 15 minutes of inactivity.");
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setIdleTime(10000);

        assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress()))); 
    }

    @Test(timeout=20000)
    public void testFailureClosedPort() throws Exception {

        m_server.setBanner("WRONG BANNER");
        m_detector.setPort(65535);
        m_detector.setIdleTime(10000);

        DetectFuture df = m_detector.isServiceDetected(m_server.getInetAddress());
        assertFalse("Test should fail because the server closes before detection takes place", doCheck(df));

    }

    @Test(timeout=20000)
    public void testFailureNoBannerSent() throws Exception {
        m_server = new SimpleServer();
        m_server.init();
        m_server.startServer();

        m_detector.setPort(m_server.getLocalPort());
        m_detector.setIdleTime(10000);

        DetectFuture df = m_detector.isServiceDetected(m_server.getInetAddress());
        assertFalse("Test should fail because the banner doesn't even get sent", doCheck(df));

    }

    private boolean doCheck(DetectFuture future) throws InterruptedException {
        future.awaitFor();
        return future.isServiceDetected();
    }
}
