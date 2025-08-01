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
package org.opennms.netmgt.provision.scan.snmp;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.provision.ScanContext;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(host=SnmpNodeScannerTest.TEST_IP_ADDRESS, resource="classpath:/org/opennms/netmgt/provision/scan/snmp/snmpTestData1.properties")
public class SnmpNodeScannerTest implements InitializingBean {
	static final String TEST_IP_ADDRESS = "192.0.2.205";
	
	@Autowired
	private SnmpPeerFactory m_snmpPeerFactory;
    
    /**
     * @author brozow
     *
     */
    private static class MockScanContext implements ScanContext {
        String m_sysObjectId;
        String m_sysContact;
        String m_sysDescription;
        String m_sysLocation;
        String m_sysName;
        InetAddress m_agentAddress;

        public MockScanContext(InetAddress agentAddress) {
            m_agentAddress = agentAddress;
        }

        @Override
        public InetAddress getAgentAddress(String agentType) {
            return m_agentAddress;
        }

        @Override
        public void updateSysObjectId(String sysObjectId) {
            m_sysObjectId = sysObjectId;
        }

        public String getSysObjectId() {
            return m_sysObjectId;
        }

        public String getSysContact() {
            return m_sysContact;
        }

        @Override
        public void updateSysContact(String sysContact) {
            m_sysContact = sysContact;
        }

        public String getSysDescription() {
            return m_sysDescription;
        }

        @Override
        public void updateSysDescription(String sysDescription) {
            m_sysDescription = sysDescription;
        }

        public String getSysLocation() {
            return m_sysLocation;
        }

        @Override
        public void updateSysLocation(String sysLocation) {
            m_sysLocation = sysLocation;
        }

        public String getSysName() {
            return m_sysName;
        }

        @Override
        public void updateSysName(String sysName) {
            m_sysName = sysName;
        }

    }

    private InetAddress m_agentAddress;
    private MockScanContext m_scanContext;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_agentAddress = InetAddressUtils.addr(TEST_IP_ADDRESS);
        m_scanContext = new MockScanContext(m_agentAddress);

    }

    @Test
    public void testScan() throws Exception {

    	final SnmpNodeScanner scanner = new SnmpNodeScanner();
        scanner.setSnmpAgentConfigFactory(m_snmpPeerFactory);
        scanner.init();
        scanner.scan(m_scanContext);
        
        assertEquals(".1.3.6.1.4.1.8072.3.2.255", m_scanContext.getSysObjectId());
        assertEquals("brozow.local", m_scanContext.getSysName());
        assertEquals("Darwin brozow.local 7.9.0 Darwin Kernel Version 7.9.0: Wed Mar 30 20:11:17 PST 2005; root:xnu/xnu-517.12.7.obj~1/RELEASE_PPC  Power Macintosh", m_scanContext.getSysDescription());
        assertEquals("Unknown", m_scanContext.getSysLocation());
        assertEquals("root@@no.where", m_scanContext.getSysContact());
    }
}
