/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

import java.net.UnknownHostException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector.MatchType;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetectorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
		"classpath:/META-INF/opennms/detectors.xml"
})
@JUnitSnmpAgent(host=SnmpDetectorTest.TEST_IP_ADDRESS, resource="classpath:/org/opennms/netmgt/provision/detector/snmpDetectorTestData.properties")
public class SnmpDetectorTest {
    
    static final String TEST_IP_ADDRESS = "192.0.2.205";
    
    @Autowired
    private SnmpDetectorFactory m_detectorFactory;
    
	private SnmpDetector m_detector;

    private DetectRequest m_request;

    @Before
    public void setUp() throws InterruptedException, UnknownHostException {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector();
        m_detector.setRetries(2);
        m_detector.setTimeout(500);
        m_request = m_detectorFactory.buildRequest(null, InetAddressUtils.addr(TEST_IP_ADDRESS), null, Collections.emptyMap());
    }
    
    @Test(timeout=20000)
    public void testIsForcedV1ProtocolSupported() throws UnknownHostException {
        m_detector.setVbvalue("\\.1\\.3\\.6\\.1\\.4\\.1.*");
        m_detector.setForceVersion("snmpv1");
        assertTrue(m_detector.detect(m_request).isServiceDetected());
    }
    
    @Test(timeout=20000)
    public void testIsExpectedValue() throws UnknownHostException {
        m_detector.setVbvalue("\\.1\\.3\\.6\\.1\\.4\\.1.*");
        assertTrue("protocol is not supported", m_detector.detect(m_request).isServiceDetected());
    }
    
    @Test(timeout=20000)
    public void testIsExpectedValueNoVbValue() throws UnknownHostException {
        assertTrue("protocol is not supported", m_detector.detect(m_request).isServiceDetected());
    }
    
    @Test(timeout=20000)
     public void testIsProtocolSupportedInetAddress() throws UnknownHostException {
         assertTrue("protocol is not supported", m_detector.detect(m_request).isServiceDetected());
     }

    @Test(timeout=20000)
    public void testDetectTableExist() throws UnknownHostException {
        // Set to existing table, detect service
        m_detector.setOid(".1.3.6.1.2.1.2.2.1.7");
        m_detector.setIsTable("true");
        m_detector.setMatchType(MatchType.Exist.name());
        assertTrue(m_detector.detect(m_request).isServiceDetected());

        // Set to non existing table, do not detect service
        m_detector.setOid(".9.9.9.9.9.9.9.9.999");
        assertFalse(m_detector.detect(m_request).isServiceDetected());
    }

    @Test(timeout=20000)
    public void testDetectTableAll() throws UnknownHostException {
        // Set to table with all 1
        m_detector.setOid(".1.3.6.1.2.1.2.2.1.8");
        m_detector.setIsTable("true");
        m_detector.setMatchType(MatchType.All.name());

        // Detect service if all values are 1
        m_detector.setVbvalue("1");
        assertTrue(m_detector.detect(m_request).isServiceDetected());

        // Do not detect service if not all values are 2
        m_detector.setVbvalue("2");
        assertFalse(m_detector.detect(m_request).isServiceDetected());
    }

    @Test(timeout=20000)
    public void testDetectTableNone() throws UnknownHostException {
        // Set to table with 3 and 2 mixed
        m_detector.setOid(".1.3.6.1.2.1.2.2.1.7");
        m_detector.setIsTable("true");

        // Detect service if 1 is not in the table
        m_detector.setVbvalue("1");
        m_detector.setMatchType(MatchType.None.name());
        assertTrue(m_detector.detect(m_request).isServiceDetected());

        // Do not detect service if 2 is somewhere in the table
        m_detector.setVbvalue("2");
        m_detector.setMatchType(MatchType.None.name());
        assertFalse(m_detector.detect(m_request).isServiceDetected());
    }

    @Test(timeout=20000)
    public void testDetectTableAny() throws UnknownHostException {
        // Set to table with 3 and 2 mixed
        m_detector.setOid(".1.3.6.1.2.1.2.2.1.7");
        m_detector.setIsTable("true");
        m_detector.setMatchType(MatchType.Any.name());

        // Detect service if 2 is somewhere in the table
        m_detector.setVbvalue("2");
        assertTrue(m_detector.detect(m_request).isServiceDetected());

        // Do not detect service if 1 is not in the table
        m_detector.setVbvalue("1");
        assertFalse(m_detector.detect(m_request).isServiceDetected());
    }

    @Test(timeout=20000)
    public void testDetectScalarBackwardCompatibility() throws UnknownHostException {
        // Match type was introduced with SNMP table detection, make sure it does not break configs without MatchType set
        m_detector.setOid(".1.3.6.1.2.1.1.2.0");
        m_detector.setIsTable("false");
        m_detector.setVbvalue("\\.1\\.3\\.6\\.1\\.4\\.1.*");
        assertTrue(m_detector.detect(m_request).isServiceDetected());
    }

    @Test(timeout=20000)
    public void testDetectScalarHexString() throws UnknownHostException {
        m_detector.setOid(".1.3.6.1.2.1.3.1.1.2.4.1.192.0.2.1");
        m_detector.setIsTable("false");

        // Validate against data type Hex-STRING for a MAC address
        m_detector.setHex("true");
        m_detector.setVbvalue("000f662002fd");
        assertTrue(m_detector.detect(m_request).isServiceDetected());

        // Should not detect, input not interpreted as Hex-STRING
        m_detector.setHex("false");
        m_detector.setVbvalue("000f662002fd");
        assertFalse(m_detector.detect(m_request).isServiceDetected());
    }

    @Test(timeout=20000)
    public void testDetectTableHexString() throws UnknownHostException {
        m_detector.setOid(".1.3.6.1.2.1.3.1.1.2.4.1");
        m_detector.setIsTable("true");

        // Validate against data type Hex-STRING for a MAC address
        m_detector.setHex("true");
        m_detector.setMatchType(MatchType.Any.name());
        m_detector.setVbvalue("000f662002fd");
        assertTrue(m_detector.detect(m_request).isServiceDetected());

        // Should not detect, input not interpreted as Hex-STRING
        m_detector.setHex("false");
        m_detector.setVbvalue("000f662002fd");
        assertFalse(m_detector.detect(m_request).isServiceDetected());
    }
}
