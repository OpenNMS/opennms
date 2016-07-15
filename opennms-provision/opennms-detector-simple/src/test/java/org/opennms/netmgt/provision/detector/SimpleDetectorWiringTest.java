/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.ServiceDetectorFactory;
import org.opennms.netmgt.provision.detector.icmp.IcmpDetector;
import org.opennms.netmgt.provision.detector.icmp.IcmpDetectorFactory;
import org.opennms.netmgt.provision.detector.smb.SmbDetector;
import org.opennms.netmgt.provision.detector.smb.SmbDetectorFactory;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetectorFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Donald Desloge
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/detectors.xml",
        "classpath:/META-INF/opennms/test/snmpConfigFactoryContext.xml"})
public class SimpleDetectorWiringTest implements ApplicationContextAware {
    
    private ApplicationContext m_applicationContext;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @SuppressWarnings("rawtypes")
    private void testWiredDetector(Class<? extends ServiceDetectorFactory> detectorFactoryClass) {
        Object bean = m_applicationContext.getBean(detectorFactoryClass.getName());
        assertNotNull(bean);
        assertTrue(detectorFactoryClass.isInstance(bean));
    }
    
    @Test
    public void testIcmpDetectorWiring(){
        testWiredDetector(IcmpDetectorFactory.class);
    }
    
    @Test
    public void testSmbDetectorWiring() {
        testWiredDetector(SmbDetectorFactory.class);
    }
    
    @Test
    public void testSnmpDetectorWiring() {
        testWiredDetector(SnmpDetectorFactory.class);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }

}
