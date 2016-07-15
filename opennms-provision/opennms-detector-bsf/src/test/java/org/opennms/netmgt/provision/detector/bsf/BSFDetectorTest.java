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

package org.opennms.netmgt.provision.detector.bsf;

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

/**
 * <p>JUnit Test class for BSFDetector.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class BSFDetectorTest implements InitializingBean {

    @Autowired
    public BSFDetectorFactory m_detectorFactory;

    BSFDetector m_detector;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector();
        assertNotNull(m_detector);

        m_detector.setFileName(null);
        m_detector.setLangClass("groovy");
        m_detector.setBsfEngine("org.codehaus.groovy.bsf.GroovyEngine");
        m_detector.setFileExtensions("groovy,gy");
        m_detector.setRunType("exec");
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws UnknownHostException {
        m_detector.setFileName("src/test/resources/testa.groovy");
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    public void testDetectorWrongBanner() throws UnknownHostException {
        m_detector.setFileName("src/test/resources/testb.groovy");
        m_detector.onInit();
        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    public void testDetectorFileNotFound() throws UnknownHostException {
        m_detector.setFileName("src/test/resources/unknown.groovy");
        m_detector.onInit();
        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    public void testBadType() throws UnknownHostException {
        m_detector.setRunType("eval");
        m_detector.setFileName("src/test/resources/testa.groovy");
        m_detector.onInit();
        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Test(timeout=20000)
    public void testInvalidEngine() throws UnknownHostException {
        m_detector.setLangClass("jython");
        m_detector.setRunType("exec");
        m_detector.setFileExtensions("py");
        m_detector.setBsfEngine("org.apache.bsf.engines.jython.JythonEngine");
        m_detector.setFileName("src/test/resources/test.py");
        m_detector.onInit();
        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }

}
