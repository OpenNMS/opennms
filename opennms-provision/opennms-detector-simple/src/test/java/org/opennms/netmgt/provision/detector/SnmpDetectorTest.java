/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
		"classpath:/META-INF/opennms/detectors.xml"
})
@JUnitSnmpAgent(host=SnmpDetectorTest.TEST_IP_ADDRESS, resource="classpath:org/opennms/netmgt/provision/detector/snmpTestData1.properties")
public class SnmpDetectorTest implements ApplicationContextAware {
    
    static final String TEST_IP_ADDRESS = "172.20.1.205";
	private SnmpDetector m_detector;
    private ApplicationContext m_applicationContext;
    private InetAddress m_testIpAddress;
    
    @Before
    public void setUp() throws InterruptedException, UnknownHostException {
        MockLogAppender.setupLogging();

        m_testIpAddress = InetAddressUtils.addr(TEST_IP_ADDRESS);

        if(m_detector == null) {
            m_detector = getDetector(SnmpDetector.class);
            m_detector.setRetries(2);
            m_detector.setTimeout(500);
        }
    }
    
    @Test(timeout=90000)
    public void testIsForcedV1ProtocolSupported() throws UnknownHostException {
        m_detector.setVbvalue("\\.1\\.3\\.6\\.1\\.4\\.1.*");
        m_detector.setForceVersion("snmpv1");
        assertTrue(m_detector.isServiceDetected(m_testIpAddress));
    }
    
    @Test(timeout=90000)
    public void testIsExpectedValue() throws UnknownHostException {
        m_detector.setVbvalue("\\.1\\.3\\.6\\.1\\.4\\.1.*");
        assertTrue("protocol is not supported", m_detector.isServiceDetected(m_testIpAddress));
    }
    
    @Test(timeout=90000)
    public void testIsExpectedValueNoVbValue() throws UnknownHostException {
        assertTrue("protocol is not supported", m_detector.isServiceDetected(m_testIpAddress));
    }
    
    @Test(timeout=90000)
     public void testIsProtocolSupportedInetAddress() throws UnknownHostException {
         assertTrue("protocol is not supported", m_detector.isServiceDetected(m_testIpAddress));
         
     }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
    
    private SnmpDetector getDetector(final Class<? extends ServiceDetector> detectorClass) {
        final Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (SnmpDetector) bean;
    }
}
