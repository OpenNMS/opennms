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
package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-dns.xml",
        "classpath:/META-INF/opennms/applicationContext-snmp-profile-mapper.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
public class PolicyIT extends ProvisioningITCase {

    @Autowired
    private Provisioner m_provisioner;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private MockNodeDao m_nodeDao;

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        final MockForeignSourceRepository mfsr = new MockForeignSourceRepository();
        final ForeignSource fs = new ForeignSource();
        fs.setName("default");
        fs.addDetector(new PluginConfig("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector"));

        PluginConfig policy1 = new PluginConfig("poll-trunk-1", "org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy");
        policy1.addParameter("ifDescr", "~^.*Trunk 1.*$");
        policy1.addParameter("action", "ENABLE_POLLING");
        policy1.addParameter("matchBehavior", "ANY_PARAMETER");

        PluginConfig policy2 = new PluginConfig("poll-vlan-600", "org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy");
        policy2.addParameter("ipAddress", "~^10\\.102\\..*$");
        policy2.addParameter("action", "ENABLE_SNMP_POLL");
        policy2.addParameter("matchBehavior", "ANY_PARAMETER");

        fs.addPolicy(policy1);
        fs.addPolicy(policy2);

        mfsr.putDefaultForeignSource(fs);
        m_provisioner.getProvisionService().setForeignSourceRepository(mfsr);
        m_provisioner.getProvisionService().setHostnameResolver(new HostnameResolver() {
            @Override public CompletableFuture<String> getHostnameAsync(final InetAddress addr, final String location) {
                return CompletableFuture.completedFuture("opennms-com");
            }
        });
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host="10.7.15.240", port=161, resource="classpath:/snmpwalk-NMS-5414.properties"),
            @JUnitSnmpAgent(host="10.7.15.241", port=161, resource="classpath:/snmpwalk-NMS-5414.properties"),
            @JUnitSnmpAgent(host="10.102.251.200", port=161, resource="classpath:/snmpwalk-NMS-5414.properties"),
            @JUnitSnmpAgent(host="10.211.140.149", port=161, resource="classpath:/snmpwalk-NMS-5414.properties")
    })
    public void testSnmpPollPolicy() throws Exception {
        final int nodeId = m_nodeDao.getNextNodeId();
        final CountDownLatch scanFinished = anticipateEvents(1, EventConstants.PROVISION_SCAN_COMPLETE_UEI, EventConstants.PROVISION_SCAN_ABORTED_UEI);

        m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/NMS-5414.xml"), Boolean.TRUE.toString());
        waitForEverything();
        assertTrue("import scan did not finish", scanFinished.await(5, TimeUnit.MINUTES));

        final OnmsNode node = m_nodeDao.get(nodeId);
        final Task scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId(), node.getLocation(), null).createTask();
        scan.schedule();
        scan.waitFor();
        waitForEverything();

        // MatchingSnmpInterfacePolicy on ifDescr "Trunk 1" polls an interface that has no IP address
        assertPolled(nodeId, 8193, "P");
        assertEquals(0, ipAddressesOn(8193).size());
        // MatchingIpInterfacePolicy on 10.102.* polls the SNMP interface behind 10.102.251.200
        assertPolled(nodeId, 10600, "P");
        assertTrue(ipAddressesOn(10600).contains("10.102.251.200"));
        // Neither policy matches Trunk 2
        assertPolled(nodeId, 8194, "N");

        assertEquals(2, m_snmpInterfaceDao.findAll().stream().filter(snmpIf -> "P".equals(snmpIf.getPoll())).count());
    }

    private void assertPolled(final int nodeId, final int ifIndex, final String expected) {
        assertEquals("snmpPoll of ifIndex " + ifIndex, expected, m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, ifIndex).getPoll());
    }

    private List<String> ipAddressesOn(final int ifIndex) {
        return m_ipInterfaceDao.findAll().stream()
                .filter(ipIf -> ipIf.getSnmpInterface() != null && Integer.valueOf(ifIndex).equals(ipIf.getSnmpInterface().getIfIndex()))
                .map(ipIf -> InetAddressUtils.str(ipIf.getIpAddress()))
                .collect(Collectors.toList());
    }
}
