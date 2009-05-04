/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.snmp.Win32ServiceDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/detectors.xml",
        "classpath:/META-INF/opennms/test/snmpConfigFactoryContext.xml"})
public class Win32ServiceDetectorTest implements ApplicationContextAware{
    
    private static int TEST_PORT = 1691;
    private static String TEST_IP_ADDRESS = "127.0.0.1";
    
    private MockSnmpAgent m_snmpAgent;
    private ApplicationContext m_applicationContext;
    private Win32ServiceDetector m_detector;
    
    @Before
    public void setUp() throws InterruptedException {
        if(m_detector == null) {
            m_detector = getDetector(Win32ServiceDetector.class);
            m_detector.setRetries(2);
            m_detector.setTimeout(500);
            m_detector.setPort(TEST_PORT);
        }
        
        if(m_snmpAgent == null) {
            m_snmpAgent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/provision/detector/snmpTestData1.properties"), String.format("%s/%s", TEST_IP_ADDRESS, TEST_PORT));
        }
        
    }
    
    @Test
    public void testDetectorSuccessful() throws UnknownHostException{
        assertTrue(m_detector.isServiceDetected(InetAddress.getByName(TEST_IP_ADDRESS), new NullDetectorMonitor()));
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext; 
    }
    
    private Win32ServiceDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (Win32ServiceDetector) bean;
    }

}
