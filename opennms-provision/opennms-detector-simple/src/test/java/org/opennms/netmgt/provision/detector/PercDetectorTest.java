/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.detector.snmp.PercDetector;
import org.opennms.netmgt.provision.detector.snmp.PercDetectorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/detectors.xml"
})
@JUnitSnmpAgent(host=PercDetectorTest.TEST_IP_ADDRESS, resource="classpath:org/opennms/netmgt/provision/detector/percDetector.properties")
public class PercDetectorTest implements InitializingBean {
    static final String TEST_IP_ADDRESS = "192.168.0.1";

    @Autowired
    private PercDetectorFactory m_detectorFactory;
    
    private PercDetector m_detector;

    private DetectRequest m_request;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws InterruptedException {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector();
        m_detector.setArrayNumber("0.0"); // the default
        m_detector.setRetries(2);
        m_detector.setTimeout(500);
        m_request = m_detectorFactory.buildRequest(null, InetAddressUtils.addr(TEST_IP_ADDRESS), null);
    }

    @Test(timeout=20000)
    public void testDetectorSuccessful() throws UnknownHostException{
        assertTrue(m_detector.detect(m_request).isServiceDetected());
    }

    @Test(timeout=20000)
    public void testDetectorFail() throws UnknownHostException{
        m_detector.setArrayNumber("0.1");
        assertFalse(m_detector.detect(m_request).isServiceDetected());
    }
}
