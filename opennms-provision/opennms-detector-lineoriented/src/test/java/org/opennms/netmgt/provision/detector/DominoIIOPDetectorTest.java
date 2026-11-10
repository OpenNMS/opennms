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

import java.net.UnknownHostException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.simple.DominoIIOPDetector;
import org.opennms.netmgt.provision.detector.simple.DominoIIOPDetectorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Donald Desloge
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class DominoIIOPDetectorTest {

    @Autowired
    private  DominoIIOPDetectorFactory m_detectorFactory;
    private DominoIIOPDetector m_detector;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setTimeout(500);
    }

    /*
     * Testing against an open port that will connect to the socket. 
     * The DominoIIOPDetector simple connects to the socket to test
     * that it can connect. The default port is 63148. In this test I
     * used a pre-existing http server on a local machine that would
     * allow the connection. 
     */
    @Test(timeout=20000)
    @Ignore("manual test against a Domino server")
    public void testDetectorSuccessTokenPort() throws UnknownHostException {
        //m_detector.setPort(8080);
        //m_detector.init();
        //assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.103")));
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws UnknownHostException {
        m_detector.setPort(65535);
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }

    @Test(timeout=20000)
    public void testDetectorFailNoHost() throws UnknownHostException {
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("1.1.1.1")));
    }
}
