/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.generic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class GpDetectorTest implements InitializingBean {

    @Autowired
    public GpDetectorFactory m_detectorFactory;
    public GpDetector m_detector;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector();
    }

    @Test(timeout=20000)
    public void testDetectorWired(){
        assertNotNull(m_detector);
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws UnknownHostException{
        m_detector.setScript(System.getProperty("user.dir") + "/src/test/resources/TestBashScript.sh");
        m_detector.setBanner("hello\n");
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000) 
    public void testDetectorWrongBanner() throws UnknownHostException{
        m_detector.setScript(System.getProperty("user.dir") + "/src/test/resources/TestBashScript.sh");
        m_detector.setBanner("world");
        m_detector.onInit();
        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }
}
