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

package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetectorFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/detectors.xml"
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(host = SnmpDetectorTest.TEST_IP_ADDRESS, resource = "classpath:/org/opennms/netmgt/provision/detector/snmpDetectorTestData.properties")
public class SnmpDetectorWithProfilesTest {

    static final String TEST_IP_ADDRESS = "192.0.2.205";

    @Autowired
    private SnmpDetectorFactory m_detectorFactory;

    private SnmpDetector m_detector;

    private DetectRequest m_request;

    @Before
    public void setUp() throws InterruptedException, UnknownHostException {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setRetries(2);
        m_detector.setUseSnmpProfiles("true");
        m_detector.setTimeout(500);
    }

    @Test(timeout = 30000)
    public void DetectorShouldSucceedWithSnmpProfiles() throws IOException {

        URL url = getClass().getResource("/org/opennms/netmgt/provision/detector/snmp-config-with-profiles.xml");
        try (InputStream configStream = url.openStream()) {
            SnmpPeerFactory snmpPeerFactory = new ProxySnmpAgentConfigFactoryExtension(configStream);
            // This is to not override snmp-config from etc
            SnmpPeerFactory.setFile(new File(url.getFile()));
            m_detectorFactory.setAgentConfigFactory(snmpPeerFactory);
            m_request = m_detectorFactory.buildRequest(null, InetAddressUtils.addr(TEST_IP_ADDRESS), null, Collections.emptyMap());
            assertTrue(m_detector.detect(m_request).isServiceDetected());
        }

    }

    @Test(timeout = 30000)
    public void detectorShouldFailWithInvalidSnmpProfiles() throws IOException {

        URL url = getClass().getResource("/org/opennms/netmgt/provision/detector/snmp-config-with-invalid-profiles.xml");
        try (InputStream configStream = url.openStream()) {
            SnmpPeerFactory snmpPeerFactory = new ProxySnmpAgentConfigFactoryExtension(configStream);
            // This is to not override snmp-config from etc
            SnmpPeerFactory.setFile(new File(url.getFile()));
            m_detectorFactory.setAgentConfigFactory(snmpPeerFactory);
            m_request = m_detectorFactory.buildRequest(null, InetAddressUtils.addr(TEST_IP_ADDRESS), null, Collections.emptyMap());
            assertFalse(m_detector.detect(m_request).isServiceDetected());
        }

    }

    /**
     * This returns wrong read-community so that default snmp config is changed
     **/
    static class ProxySnmpAgentConfigFactoryExtension extends ProxySnmpAgentConfigFactory {

        ProxySnmpAgentConfigFactoryExtension(InputStream config) throws FileNotFoundException {
            super(config);
        }

        @Override
        public SnmpAgentConfig getAgentConfig(final InetAddress address, String location) {
            SnmpAgentConfig config = super.getAgentConfig(address, location);
            config.setReadCommunity("horizon");
            return config;
        }
    }
}
