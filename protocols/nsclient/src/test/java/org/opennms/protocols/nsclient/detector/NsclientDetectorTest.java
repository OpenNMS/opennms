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
package org.opennms.protocols.nsclient.detector;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.server.exchange.RequestHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <p>JUnit Test Class for NsclientDetector.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/META-INF/opennms/detectors.xml"})
public class NsclientDetectorTest implements InitializingBean {

    @Autowired
    private NsclientDetectorFactory m_detectorFactory;
    
    private NsclientDetector m_detector;

    private SimpleServer m_server = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @After
    public void tearDown() throws Exception{
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }

    @Before
    public void setUp() throws Exception{
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        // Initialize Mock NSClient Server
        m_server  = new SimpleServer() {
            @Override
            public void onInit() {
                addResponseHandler(startsWith("None&1"), new RequestHandler() {
                    @Override
                    public void doRequest(OutputStream out) throws IOException {
                        out.write(String.format("%s\r\n", "NSClient++ 0.3.8.75 2010-05-27").getBytes());
                    }
                });
            }
        };
        m_server.init();
        m_server.startServer();
        Thread.sleep(100); // make sure the server is really started
        // Initialize Detector
        m_detector.setServiceName("NSclient++");
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setTimeout(2000);
        m_detector.setRetries(3);
    }

    @Test(timeout=90000)
    public void testServerSuccess() throws Exception{
        m_detector.setCommand("CLIENTVERSION");
        m_detector.init();
        Assert.assertTrue(m_detector.isServiceDetected(m_server.getInetAddress()));
    }

    @Test(timeout=90000)
    public void testBadCommand() throws Exception{
        m_detector.setCommand("UNKNOWN");
        m_detector.init();
        Assert.assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
    }

    @Test(timeout=90000)
    public void testNoCommand() throws Exception{
        m_detector.init(); // Assumes CLIENTVERSION
        Assert.assertTrue(m_detector.isServiceDetected(m_server.getInetAddress()));
    }

}
