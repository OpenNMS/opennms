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
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.jmx.JmxConfig;
import org.opennms.netmgt.provision.detector.jmx.Jsr160Detector;
import org.opennms.netmgt.provision.detector.jmx.Jsr160DetectorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Donald Desloge
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml",
                                 "classpath:/test-spring-jmxconfig.xml"})
public class Jsr160DetectorTest implements InitializingBean {

    @Autowired
    public Jsr160DetectorFactory m_detectorFactory;
    
    public Jsr160Detector m_detector;

    public static MBeanServer m_beanServer;
    private JMXConnectorServer m_connectorServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        this.m_detectorFactory.setJmxConfigDao(() -> new JmxConfig());
    }

    @BeforeClass
    public static void beforeTest() throws RemoteException{
        LocateRegistry.createRegistry(9123);
        m_beanServer = ManagementFactory.getPlatformMBeanServer();
    }

    @Before
    public void setUp() throws IOException {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        assertNotNull(m_detector);

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9123/server");

        m_connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, m_beanServer);
        m_connectorServer.start();
    }

    @After
    public void tearDown() throws IOException{
        m_connectorServer.stop();
        MockLogAppender.assertNoErrorOrGreater();
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws IOException, MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {

        m_detector.setPort(9123);
        m_detector.setUrlPath("/server");
        m_detector.init();

        assertTrue(m_detector.isServiceDetected(InetAddress.getLocalHost()));

    }

    @Test(timeout=20000)
    public void testDetectorWrongPort() throws IOException, MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {

        m_detector.setPort(9000);
        m_detector.setUrlPath("/server");
        m_detector.init();

        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost()));

    }

    @Test(timeout=20000)
    public void testDetectorWrongUrlPath() throws IOException, MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {

        m_detector.setPort(9123);
        m_detector.setUrlPath("/wrongurlpath");
        m_detector.init();

        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost()));

    }

    /**
     * If we try to connect to localhost on the default OpenNMS JMX port, the detector
     * should connect to the in-JVM {@link MBeanServer} and return that the service has
     * been detected.
     */
    @Test(timeout=20000)
    public void testDetectorLocalJvm() throws IOException, MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {

        m_detector.setPort(18980);
        m_detector.init();

        assertTrue(m_detector.isServiceDetected(InetAddressUtils.ONE_TWENTY_SEVEN));
    }

}
