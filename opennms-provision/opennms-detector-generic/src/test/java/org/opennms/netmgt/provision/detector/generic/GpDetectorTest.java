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
package org.opennms.netmgt.provision.detector.generic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
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
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        System.setProperty("opennms.home", System.getProperty("user.dir"));
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

    boolean isDescendantOf(final Path root, final Path child) {
        if (root == null || child == null) {
            return false;
        }

        final Path absoluteRoot = root
                .toAbsolutePath()
                .normalize();

        final Path absoluteChild = child
                .toAbsolutePath()
                .normalize();

        if (absoluteRoot.getNameCount() >= absoluteChild.getNameCount()) {
            return false;
        }

        final Path nextChild = absoluteChild
                .getParent();

        return nextChild.equals(absoluteRoot) || isDescendantOf(absoluteRoot, nextChild);
    }

    @Test(expected = IOException.class)
    public void testScriptOutsideOpenNMSHome() throws Exception {
        m_detector.setScript("/var/tmp/foo.sh");
        m_detector.onInit();
        m_detector.getClient().connect(InetAddress.getLocalHost(), 1234, 1000);
    }
}
