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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
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
    public void setUp() throws InterruptedException, IOException {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setRetries(2);
        m_detector.setUseSnmpProfiles("true");
        m_detector.setTimeout(500);

        final TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        final File keystoreFile = new File(temporaryFolder.getRoot(), "scv.jce");
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        secureCredentialsVault.setCredentials("communityv1", new Credentials("username", "public"));
        SnmpPeerFactory.setSecureCredentialsVaultScope(new SecureCredentialsVaultScope(secureCredentialsVault));
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
