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
import java.io.OutputStream;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.MemcachedDetector;
import org.opennms.netmgt.provision.detector.simple.MemcachedDetectorFactory;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.server.exchange.RequestHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class MemcachedDetectorTest implements InitializingBean {

    @Autowired
    private MemcachedDetectorFactory m_detectorFactory;
    
    private MemcachedDetector m_detector;

    private SimpleServer m_server = null;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Before
    public void setUp() throws Exception{
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setServiceName("Memcached");
        m_detector.setPort(1000);
        m_detector.setIdleTime(3000);
        m_detector.setTimeout(1000);
        m_detector.init();
    }

    @After
    public void tearDown() throws Exception{
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }

    @Test(timeout=20000)
    public void testServerSuccess() throws Exception{
        m_server  = new SimpleServer() {
            @Override
            public void onInit() {
                addResponseHandler(contains("version"), new RequestHandler() {
                    @Override
                    public void doRequest(OutputStream out) throws IOException {
                        out.write(String.format("%s\r\n", "VERSION 1.2.3").getBytes());
                    }
                });
            }
        };
        m_server.init();
        m_server.startServer();
        Thread.sleep(100); // make sure the server is really started
        try {
            m_detector.setPort(m_server.getLocalPort());
            m_detector.setIdleTime(1000);
            DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
            assertNotNull(future);
            future.awaitForUninterruptibly();
            assertTrue(future.isServiceDetected());
        } finally {
            m_server.stopServer();
        }
    }

    @Test(timeout=20000)
    public void testServerFail() throws Exception{
        m_server  = new SimpleServer() {
            @Override
            public void onInit() {
                addResponseHandler(contains("version"), new RequestHandler() {
                    @Override
                    public void doRequest(OutputStream out) throws IOException {
                        out.write(String.format("%s\r\n", "I don't know what version means...").getBytes());
                    }
                });
            }
        };
        m_server.init();
        m_server.startServer();
        Thread.sleep(100); // make sure the server is really started
        try {
            m_detector.setPort(m_server.getLocalPort());
            m_detector.setIdleTime(1000);
            DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
            assertNotNull(future);
            future.awaitForUninterruptibly();
            assertFalse(future.isServiceDetected());
        } finally {
            m_server.stopServer();
        }
    }
}
