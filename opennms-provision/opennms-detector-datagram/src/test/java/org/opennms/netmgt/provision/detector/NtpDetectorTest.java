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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.datagram.NtpDetector;
import org.opennms.netmgt.provision.detector.datagram.NtpDetectorFactory;
import org.opennms.netmgt.provision.server.SimpleUDPServer;
import org.opennms.netmgt.provision.support.ntp.NtpMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class NtpDetectorTest implements InitializingBean {
    
    @Autowired
    public NtpDetectorFactory m_detectorFactory;
    public NtpDetector m_detector;
    private SimpleUDPServer m_server;
    
    @Before
    public void setUp(){
        MockLogAppender.setupLogging();

        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setRetries(0);
        assertNotNull(m_detector);
        
        m_server = new SimpleUDPServer(){
          
            @Override
            public void onInit(){
                NtpMessage message = new NtpMessage();
                message.version = 3;
                message.mode = 4;
                message.stratum = 3;
                message.precision = 24;
                message.rootDelay = 24.17;
                message.rootDispersion = 56.82;
                message.referenceTimestamp = message.transmitTimestamp;
                message.originateTimestamp = message.transmitTimestamp;
                message.receiveTimestamp = message.transmitTimestamp;
                message.transmitTimestamp = message.transmitTimestamp;
                byte[] response = message.toByteArray();
                
                addRequestResponse(null, response);
            }
            
        };
        m_server.setPort(1800);
        m_server.setInetAddress(InetAddressUtils.getLocalHostAddress());
    }
    
    @After
    public void tearDown() throws IOException {
        m_server.stopServer();
        m_server = null;
    }
     
    @Test(timeout=20000)
    public void testDetectorSuccess() throws Exception{
        m_server.onInit();
        m_server.startServer();
        
        m_detector.setPort(m_server.getPort());
        m_detector.setIpToValidate(InetAddressUtils.str(m_server.getInetAddress()));
        m_detector.init();
        assertTrue("Testing for NTP service, got false when true is supposed to be returned", m_detector.isServiceDetected(m_server.getInetAddress()));
    }
    
    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws Exception{
        m_server.onInit();
        m_server.startServer();
        
        m_detector.setPort(2000);
        m_detector.setIpToValidate(InetAddressUtils.str(m_server.getInetAddress()));
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        
    }
    
}
