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

import java.io.IOException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.SmtpDetector;
import org.opennms.netmgt.provision.detector.simple.SmtpDetectorFactory;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Donald Desloge
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class SmtpDetectorTest {

    @Autowired
    private SmtpDetectorFactory m_detectorFactory;
    
    private SmtpDetector m_detector;
    private SimpleServer m_server;


    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_server = getServer();
        m_server.init();
        m_server.startServer();

        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setTimeout(500);
        m_detector.init();
        m_detector.setPort(m_server.getLocalPort());
    }

    @After
    public void tearDown() throws IOException {
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongCodeExpectedMultilineRequest() throws Exception {
        SimpleServer tempServer = new SimpleServer() {

            @Override
            public void onInit() {
                String[] multiLine = {"600 First line"};

                setBanner("220 ewhserver279.edgewebhosting.net");
                addResponseHandler(matches("HELO LOCALHOST"), multilineLineRequest(multiLine));
                addResponseHandler(matches("QUIT"), shutdownServer("221 Service closing transmission channel"));
            }
        };

        tempServer.init();
        tempServer.startServer();
        m_detector.setPort(tempServer.getLocalPort());

        assertFalse(doCheck(m_detector.isServiceDetected(tempServer.getInetAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailIncompleteMultilineResponseFromServer() throws Exception {
        SimpleServer tempServer = new SimpleServer() {

            @Override
            public void onInit() {
                String[] multiLine = {"250-First line", "400-Bogus second line"};

                setBanner("220 ewhserver279.edgewebhosting.net");
                addResponseHandler(matches("HELO LOCALHOST"), multilineLineRequest(multiLine));
                addResponseHandler(matches("QUIT"), shutdownServer("221 Service closing transmission channel"));
            }
        };

        tempServer.init();
        tempServer.startServer();
        m_detector.setPort(tempServer.getLocalPort());

        assertFalse(doCheck(m_detector.isServiceDetected(tempServer.getInetAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailBogusSecondLine() throws Exception {
        SimpleServer tempServer = new SimpleServer() {

            @Override
            public void onInit() {
                String[] multiLine = {"250-First line", "400-Bogus second line", "250 Requested mail action completed"};

                setBanner("220 ewhserver279.edgewebhosting.net");
                addResponseHandler(matches("HELO LOCALHOST"), multilineLineRequest(multiLine));
                addResponseHandler(matches("QUIT"), shutdownServer("221 Service closing transmission channel"));
            }
        };

        tempServer.init();
        tempServer.startServer();
        m_detector.setPort(tempServer.getLocalPort());
        m_detector.setIdleTime(1000);

        assertFalse(doCheck(m_detector.isServiceDetected(tempServer.getInetAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongTypeOfBanner() throws Exception {
        m_server.setBanner("bogus");

        assertFalse(doCheck(m_detector.isServiceDetected(m_server.getInetAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailServerStopped() throws Exception {
        m_server.stopServer();
        assertFalse(doCheck(m_detector.isServiceDetected(m_server.getInetAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws Exception {
        m_detector.setPort(1);
        assertFalse(doCheck(m_detector.isServiceDetected(m_server.getInetAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorSucess() throws Exception {
        assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress())));
    }

    private boolean doCheck(DetectFuture future) throws InterruptedException {
        future.awaitFor();
        return future.isServiceDetected();
    }

    private SimpleServer getServer() {
        return new SimpleServer() {

            @Override
            public void onInit() {
                String[] multiLine = {"250-First line", "250-Second line", "250 Requested mail action completed"};

                setBanner("220 ewhserver279.edgewebhosting.net");
                addResponseHandler(matches("HELO LOCALHOST"), multilineLineRequest(multiLine));
                addResponseHandler(matches("QUIT"), shutdownServer("221 Service closing transmission channel"));
            }
        };
    }
}
