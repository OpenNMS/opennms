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
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.loop.LoopDetector;
import org.opennms.netmgt.provision.detector.loop.LoopDetectorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/detectors.xml"})
public class LoopDetectorTest implements InitializingBean {
    
    @Autowired
    private LoopDetectorFactory m_detectorFactory;
    private LoopDetector m_detector;
    
    @Before
    public void setUp(){
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setSupported(true);
    }
    
    @Test(timeout=20000)
    public void testDetectorSuccess() throws UnknownHostException{
        m_detector.setIpMatch(InetAddressUtils.str(InetAddress.getLocalHost()));
        m_detector.init();
        assertTrue("Service detection for loopDetector failed.", m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }
    
    @Test(timeout=20000)
    public void testDetectorFail() throws UnknownHostException{
        m_detector.init();
        assertFalse("Service detection was supposed to be false but was true:", m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        
    }
}
