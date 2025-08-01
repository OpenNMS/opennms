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
        Pop3Detector detector = m_detectorFactory.createDetector(new HashMap<>());
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
