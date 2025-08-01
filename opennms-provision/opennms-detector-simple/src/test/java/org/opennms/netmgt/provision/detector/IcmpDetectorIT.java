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

import java.net.InetAddress;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.icmp.AbstractPingerFactory;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.icmp.jna.JnaPinger;
import org.opennms.netmgt.icmp.jni.JniPinger;
import org.opennms.netmgt.provision.detector.icmp.IcmpDetector;
import org.opennms.netmgt.provision.detector.icmp.IcmpDetectorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/test/detectors.xml",
        "classpath:/META-INF/opennms/detectors.xml"
})
public class IcmpDetectorIT {

    @Autowired
    private IcmpDetectorFactory m_detectorFactory;

    @Autowired
    private PingerFactory m_pingerFactory;

    private IcmpDetector m_icmpDetector;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        getPingerFactory().reset();
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccessJni() throws Exception {
        getPingerFactory().setInstance(0, true, new JniPinger());
        m_icmpDetector = m_detectorFactory.createDetector(new HashMap<>());
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccessJniDscp() throws Exception {
        getPingerFactory().setInstance(0x24, true, new JniPinger());
        m_icmpDetector = m_detectorFactory.createDetector(new HashMap<>());
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorFailJni() throws Exception {
        getPingerFactory().setInstance(0, true, new JniPinger());
        m_icmpDetector = m_detectorFactory.createDetector(new HashMap<>());
        assertFalse("ICMP was incorrectly identified on " + InetAddressUtils.UNPINGABLE_ADDRESS.getHostAddress(), m_icmpDetector.isServiceDetected(InetAddressUtils.UNPINGABLE_ADDRESS));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccess() throws Exception {
        getPingerFactory().setInstance(0, true, new JnaPinger());
        m_icmpDetector = m_detectorFactory.createDetector(new HashMap<>());
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccessDscp() throws Exception {
        getPingerFactory().setInstance(0x24, true, new JnaPinger());
        m_icmpDetector = m_detectorFactory.createDetector(new HashMap<>());
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorFail() throws Exception {
        getPingerFactory().setInstance(0, true, new JnaPinger());
        m_icmpDetector = m_detectorFactory.createDetector(new HashMap<>());
        assertFalse("ICMP was incorrectly identified on " + InetAddressUtils.UNPINGABLE_ADDRESS.getHostAddress(), m_icmpDetector.isServiceDetected(InetAddressUtils.UNPINGABLE_ADDRESS));
    }

    private AbstractPingerFactory getPingerFactory() {
        if (m_pingerFactory instanceof AbstractPingerFactory) {
            return (AbstractPingerFactory) m_pingerFactory;
        }
        throw new IllegalStateException("Pinger factory for testing is not a normal AbstractPingerFactory!");
    }
}
