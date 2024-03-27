/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.detector.bsf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

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
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
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
