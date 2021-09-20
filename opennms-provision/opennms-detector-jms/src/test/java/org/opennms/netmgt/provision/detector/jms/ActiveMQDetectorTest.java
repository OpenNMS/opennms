/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
        BeanUtils.assertAutowiring(this);
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
