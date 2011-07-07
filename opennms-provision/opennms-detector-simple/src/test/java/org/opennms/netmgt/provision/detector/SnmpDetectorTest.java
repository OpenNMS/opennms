/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/detectors.xml",
        "classpath:/META-INF/opennms/test/snmpConfigFactoryContext.xml"})
public class SnmpDetectorTest implements ApplicationContextAware {
    
    private static final int TEST_PORT = 1691;
    private static final String TEST_IP_ADDRESS = "127.0.0.1";
    
    private SnmpDetector m_detector;
    private ApplicationContext m_applicationContext;
    private InetAddress m_testIpAddress;
    private DetectorMonitor m_detectorMonitor;
    
    @Before
    public void setUp() throws InterruptedException, UnknownHostException {
        MockLogAppender.setupLogging();

        m_testIpAddress = InetAddressUtils.addr(TEST_IP_ADDRESS);
        m_detectorMonitor = new NullDetectorMonitor();

        if(m_detector == null) {
            m_detector = getDetector(SnmpDetector.class);
            m_detector.setRetries(2);
            m_detector.setTimeout(500);
            m_detector.setPort(TEST_PORT);
        }
    }
    
    @Test
    @JUnitSnmpAgent(resource="classpath:org/opennms/netmgt/provision/detector/snmpTestData1.properties", host=TEST_IP_ADDRESS, port=TEST_PORT)
    public void testIsForcedV1ProtocolSupported() throws UnknownHostException {
        m_detector.setVbvalue("\\.1\\.3\\.6\\.1\\.4\\.1.*");
        m_detector.setForceVersion("snmpv1");
        m_detector.setAgentConfigFactory(new AnAgentConfigFactory());
        
        assertTrue(m_detector.isServiceDetected(m_testIpAddress, m_detectorMonitor));
    }
    
    @Test
    @JUnitSnmpAgent(resource="classpath:org/opennms/netmgt/provision/detector/snmpTestData1.properties", host=TEST_IP_ADDRESS, port=TEST_PORT)
    public void testIsExpectedValue() throws UnknownHostException {
        m_detector.setVbvalue("\\.1\\.3\\.6\\.1\\.4\\.1.*");
        assertTrue("protocol is not supported", m_detector.isServiceDetected(m_testIpAddress, m_detectorMonitor));
    }
    
    @Test
    @JUnitSnmpAgent(resource="classpath:org/opennms/netmgt/provision/detector/snmpTestData1.properties", host=TEST_IP_ADDRESS, port=TEST_PORT)
    public void testIsExpectedValueNoVbValue() throws UnknownHostException {
        assertTrue("protocol is not supported", m_detector.isServiceDetected(m_testIpAddress, m_detectorMonitor));
    }
    
    @Test
    @JUnitSnmpAgent(resource="classpath:org/opennms/netmgt/provision/detector/snmpTestData1.properties", host=TEST_IP_ADDRESS, port=TEST_PORT)
     public void testIsProtocolSupportedInetAddress() throws UnknownHostException {
         assertTrue("protocol is not supported", m_detector.isServiceDetected(m_testIpAddress, m_detectorMonitor));
         
     }

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
