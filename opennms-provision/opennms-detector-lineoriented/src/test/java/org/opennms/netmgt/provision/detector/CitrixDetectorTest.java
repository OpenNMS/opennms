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

import java.io.IOException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.CitrixDetector;
import org.opennms.netmgt.provision.detector.simple.CitrixDetectorFactory;
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
public class CitrixDetectorTest {
    
    @Autowired
    private CitrixDetectorFactory m_detectorFactory;
    
    private CitrixDetector m_detector;
    private SimpleServer m_server;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setTimeout(500);

        m_server = getServer();
        m_server.init();
        m_server.startServer();


    }

    @After
    public void tearDown() throws IOException {
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws Exception {
        m_detector.setPort(65535);
        m_detector.setIdleTime(10000);
        m_detector.init();

        //assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse(future.isServiceDetected());
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws Exception {
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setIdleTime(10000);
        m_detector.init();

        //assertTrue(m_detector.isServiceDetected(m_server.getInetAddress()));
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertTrue(future.isServiceDetected());
    }

    private SimpleServer getServer() {
        return new SimpleServer() {

            @Override
            public void onInit() {
                setBanner("ICAICAICAICA");
            }
        };
    }
}
