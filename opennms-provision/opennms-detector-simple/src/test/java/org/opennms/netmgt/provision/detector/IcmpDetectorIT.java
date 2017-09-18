/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.net.InetAddress;

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
        m_icmpDetector = m_detectorFactory.createDetector();
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccessJniDscp() throws Exception {
        getPingerFactory().setInstance(0x24, true, new JniPinger());
        m_icmpDetector = m_detectorFactory.createDetector();
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorFailJni() throws Exception {
        getPingerFactory().setInstance(0, true, new JniPinger());
        m_icmpDetector = m_detectorFactory.createDetector();
        assertFalse("ICMP was incorrectly identified on " + InetAddressUtils.UNPINGABLE_ADDRESS.getHostAddress(), m_icmpDetector.isServiceDetected(InetAddressUtils.UNPINGABLE_ADDRESS));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccess() throws Exception {
        getPingerFactory().setInstance(0, true, new JnaPinger());
        m_icmpDetector = m_detectorFactory.createDetector();
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccessDscp() throws Exception {
        getPingerFactory().setInstance(0x24, true, new JnaPinger());
        m_icmpDetector = m_detectorFactory.createDetector();
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorFail() throws Exception {
        getPingerFactory().setInstance(0, true, new JnaPinger());
        m_icmpDetector = m_detectorFactory.createDetector();
        assertFalse("ICMP was incorrectly identified on " + InetAddressUtils.UNPINGABLE_ADDRESS.getHostAddress(), m_icmpDetector.isServiceDetected(InetAddressUtils.UNPINGABLE_ADDRESS));
    }

    private AbstractPingerFactory getPingerFactory() {
        if (m_pingerFactory instanceof AbstractPingerFactory) {
            return (AbstractPingerFactory) m_pingerFactory;
        }
        throw new IllegalStateException("Pinger factory for testing is not a normal AbstractPingerFactory!");
    }
}
