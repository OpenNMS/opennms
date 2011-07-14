/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.provision.detector.snmp.PercDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/detectors.xml",
        "classpath:/META-INF/opennms/test/snmpConfigFactoryContext.xml"})
public class PercDetectorTest {

    @Autowired
    private PercDetector m_detector;

    private static final int TEST_PORT = 1691;
    private static final String TEST_IP_ADDRESS = "127.0.0.1";

    @Before
    public void setUp() throws InterruptedException {
        MockLogAppender.setupLogging();

        m_detector.setRetries(2);
        m_detector.setTimeout(500);
        m_detector.setPort(TEST_PORT);
    }

    @Test
    @JUnitSnmpAgent(resource="classpath:org/opennms/netmgt/provision/detector/percDetector.properties", host=TEST_IP_ADDRESS, port=TEST_PORT, useMockSnmpStrategy=true)
    public void testDetectorSuccessful() throws UnknownHostException{
        assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr(TEST_IP_ADDRESS), new NullDetectorMonitor()));
    }

    @Test
    @JUnitSnmpAgent(resource="classpath:org/opennms/netmgt/provision/detector/percDetector.properties", host=TEST_IP_ADDRESS, port=TEST_PORT, useMockSnmpStrategy=true)
    public void testDetectorFail() throws UnknownHostException{
        m_detector.setArrayNumber("0.1");
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr(TEST_IP_ADDRESS), new NullDetectorMonitor()));
    }
}
