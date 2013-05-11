/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.loop.LoopDetector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/detectors.xml"})
public class LoopDetectorTest implements ApplicationContextAware {
    
    private ApplicationContext m_applicationContext;
    private LoopDetector m_detector;
    
    @Before
    public void setUp(){
        MockLogAppender.setupLogging();
        m_detector = getDetector(LoopDetector.class);
        m_detector.setSupported(true);
    }
    
    @Test(timeout=90000)
    public void testDetectorSuccess() throws UnknownHostException{
        m_detector.setIpMatch(InetAddressUtils.str(InetAddress.getLocalHost()));
        m_detector.init();
        assertTrue("Service detection for loopDetector failed.", m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }
    
    @Test(timeout=90000)
    public void testDetectorFail() throws UnknownHostException{
        m_detector.init();
        assertFalse("Service detection was supposed to be false but was true:", m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }
    
    private LoopDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (LoopDetector) bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
        
    }
}
