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

import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.dns.JUnitDNSServerExecutionListener;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.datagram.DnsDetector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Donald Desloge
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/empty-context.xml"})
@TestExecutionListeners(JUnitDNSServerExecutionListener.class)
@JUnitDNSServer(port=9153, zones={
    @DNSZone(name = "google.com.", entries = {
        @DNSEntry(hostname = "www", data = "72.14.204.99")
    })
})
public class DnsDetectorTest {

    private DnsDetector m_detector;

    @Before
    public void setUp() throws SocketException {
        MockLogAppender.setupLogging();

        m_detector = new DnsDetector();
        m_detector.setTimeout(500);

        //m_socket = new DatagramSocket(4445);
        //m_serverThread = createThread();
        //m_serverThread.start();
    }

    @After
    public void tearDown() {
        //m_serverThread.stop();
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws UnknownHostException {
        m_detector.setPort(9153);
        m_detector.setLookup("www.google.com");
        m_detector.init();

        assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("localhost")));
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws UnknownHostException {
        m_detector.setPort(5000);
        m_detector.setLookup("www.google.com");
        m_detector.init();

        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("localhost")));

    }
}
