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
package org.opennms.netmgt.provision.detector.jms;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.URI;
import org.junit.After;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/detectors.xml"})
public class ActiveMQDetectorTest implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQDetectorTest.class);
    private static final String DEFAULT_BROKER_BROKERURL = "tcp://localhost:61616?transport.threadName&transport.trace=false&transport.soTimeout=20000";
    private static final String DEFAULT_CLIENT_BROKERURL = "tcp://localhost:61616?trace=false&soTimeout=20000";

    @Autowired
    public ActiveMQDetectorFactory m_detectorFactory;
    public ActiveMQDetector m_detector;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Rule
    public TestName m_test = new TestName();

    @ClassRule
    public static ActiveMQBroker broker = new ActiveMQBroker(DEFAULT_BROKER_BROKERURL);

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        LOG.info("setUp(): starting test {}\n", m_test.getMethodName());
        m_detector = m_detectorFactory.createDetector(null);
        assertNotNull(m_detector);
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("tearDown(): finished test {}\n\n", m_test.getMethodName());
    }

    @Test(timeout = 20000)
    public void testDetectorWired() {
    }

    @Test(timeout = 20000)
    public void testDetectorSuccess() throws Exception {
        URI uri = new URI(DEFAULT_CLIENT_BROKERURL);
        m_detector.setBrokerURL(DEFAULT_CLIENT_BROKERURL);
        m_detector.setPort(uri.getPort());
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(InetAddress.getLocalHost(), uri));
        m_detector.dispose();
    }
}
