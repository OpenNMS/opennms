/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/detectors.xml",
        "classpath:/META-INF/opennms/test/snmpConfigFactoryContext.xml"})
public class SnmpDetectorTest implements ApplicationContextAware {
    
    private static int TEST_PORT = 1691;
    private static String TEST_IP_ADDRESS = "127.0.0.1";
    
    private MockSnmpAgent m_snmpAgent;
    private SnmpDetector m_detector;
    private ApplicationContext m_applicationContext;
    private InetAddress m_testIpAddress;
    private DetectorMonitor m_detectorMonitor;
    
    @Before
    public void setUp() throws InterruptedException, UnknownHostException {
        MockLogAppender.setupLogging();

        m_testIpAddress = InetAddress.getByName(TEST_IP_ADDRESS);
        m_detectorMonitor = new NullDetectorMonitor();

        if(m_detector == null) {
            m_detector = getDetector(SnmpDetector.class);
            m_detector.setRetries(2);
            m_detector.setTimeout(500);
            m_detector.setPort(TEST_PORT);
        }
        
        if(m_snmpAgent == null) {
            m_snmpAgent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/provision/detector/snmpTestData1.properties"), String.format("%s/%s", TEST_IP_ADDRESS, TEST_PORT));
        }
        
    }
    
    @After
    public void tearDown() throws InterruptedException {
        m_snmpAgent.shutDownAndWait();
    }
    
    @Test
    public void testIsForcedV1ProtocolSupported() throws UnknownHostException {
        m_detector.setForceVersion("snmpv1");
        m_detector.setAgentConfigFactory(new AnAgentConfigFactory());
        
        assertTrue(m_detector.isServiceDetected(m_testIpAddress, m_detectorMonitor));
    }
    
    @Test
    public void testIsExpectedValue() throws UnknownHostException {      
        m_detector.setVbvalue("\\.1\\.3\\.6\\.1\\.4\\.1.*");
        assertTrue("protocol is not supported", m_detector.isServiceDetected(m_testIpAddress, m_detectorMonitor));
    }
    
    @Test
     public void testIsProtocolSupportedInetAddress() throws UnknownHostException {
         
         assertTrue("protocol is not supported", m_detector.isServiceDetected(m_testIpAddress, m_detectorMonitor));
         
     }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
    
    private SnmpDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (SnmpDetector) bean;
    }
}
