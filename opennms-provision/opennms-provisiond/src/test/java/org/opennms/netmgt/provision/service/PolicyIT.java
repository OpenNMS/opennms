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

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
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
        "classpath:/META-INF/opennms/applicationContext-snmp-profile-mapper.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Ignore("This test flaps because it doesn't account for provision ordering correctly")
public class PolicyIT {

    public static interface BackgroundTask {
        public void await() throws InterruptedException;
    }

    @Autowired
    private JdbcTemplate m_jdbcTemplate;

    @Autowired
    private Provisioner m_provisioner;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private MockEventIpcManager m_eventSubscriber;

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

        System.err.println(policy1.toString());
        System.err.println(policy2.toString());

        fs.addPolicy(policy1);

        fs.addPolicy(policy2);

        mfsr.putDefaultForeignSource(fs);
        m_provisioner.getProvisionService().setForeignSourceRepository(mfsr);
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host="10.7.15.240", port=161, resource="classpath:/snmpwalk-NMS-5414.properties"),
            @JUnitSnmpAgent(host="10.7.15.241", port=161, resource="classpath:/snmpwalk-NMS-5414.properties"),
            @JUnitSnmpAgent(host="10.102.251.200", port=161, resource="classpath:/snmpwalk-NMS-5414.properties"),
            @JUnitSnmpAgent(host="10.211.140.149", port=161, resource="classpath:/snmpwalk-NMS-5414.properties")
    })
    //@Repeat()
    //@Transactional Do not use transactional because it freezes the database and makes it impossible to check for 
    // values created in other transactions (unless you are lucky - which sometimes we are not)
    public void testSnmpPollPolicy() throws Exception {
        try {
            // Create a BackgroundTask to wait for the provisioning group import to complete
            final BackgroundTask eventRecieved = anticipateEvents(EventConstants.PROVISION_SCAN_COMPLETE_UEI, EventConstants.PROVISION_SCAN_ABORTED_UEI );

            // Import the provisioning group
            m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/NMS-5414.xml"), Boolean.TRUE.toString());
            int nodeId = getNodeId();
            eventRecieved.await();

            final NodeScan scan = m_provisioner.createNodeScan(nodeId, getForeignSource(nodeId), getForeignId(nodeId), new OnmsMonitoringLocation(), null);
            runScan(scan);


            Integer snmpIfId = findMatchingSnmpIf("ifDescr", "Trunk 1");

            assertEquals(1, getNodeId(snmpIfId));
            assertEquals(8193, getIfIndex(snmpIfId));
            assertEquals(0, getIpInterfaceCount(snmpIfId));
            assertEquals("P", getPollSetting(snmpIfId));

            snmpIfId = findMatchingSnmpIf("ifDescr", "VLAN 600 L3");

            assertEquals(1, getNodeId(snmpIfId));
            assertEquals(10600, getIfIndex(snmpIfId));
            assertEquals(1, getIpInterfaceCount(snmpIfId));
            assertEquals("P", getPollSetting(snmpIfId));

            snmpIfId = findMatchingSnmpIf("ifName", "ifc3 (Slot: 1 Port: 3)");

            assertEquals(1, getNodeId(snmpIfId));
            assertEquals(3, getIfIndex(snmpIfId));
            assertEquals(1, getIpInterfaceCount(snmpIfId));
            assertEquals("P", getPollSetting(snmpIfId));

            assertEquals(3, countSnmpIfsWithPollSetting("P"));
            System.err.println("Completed Successfully");
        } catch (AssertionError e) {
            throw e;
        }

    }

    private int countSnmpIfsWithPollSetting(String pollSetting) {
        return m_jdbcTemplate.queryForObject("select count(*) from snmpinterface where snmppoll = ?", Integer.class, pollSetting).intValue();
    }

    private Integer findMatchingSnmpIf(String property, String value) {
        String columnName = "snmp"+property.toLowerCase();
        return m_jdbcTemplate.queryForObject("select id from snmpinterface where "+columnName+" ilike ?", Integer.class, "%"+value+"%");
    }

    private String getPollSetting(Integer snmpIfId) {
        return m_jdbcTemplate.queryForObject("select snmppoll from snmpinterface where id = ?", String.class, snmpIfId);
    }

    private int getIpInterfaceCount(Integer snmpIfId) {
        return m_jdbcTemplate.queryForObject("select count(*) from ipinterface where snmpinterfaceid = ?", Integer.class, snmpIfId);
    }

    private int getIfIndex(Integer snmpIfId) {
        return m_jdbcTemplate.queryForObject("select snmpifindex from snmpinterface where id = ?", Integer.class, snmpIfId);
    }

    private int getNodeId(Integer snmpIfId) {
        return m_jdbcTemplate.queryForObject("select nodeId from snmpinterface where id = ?", Integer.class, snmpIfId);
    }

    private int getNodeId() {
        return m_jdbcTemplate.queryForObject("select nodeId from node order by nodelabel limit 1", Integer.class);
    }

    private String getForeignId(Integer nodeId) {
        return m_jdbcTemplate.queryForObject("select foreignId from node where nodeid = ?", String.class, nodeId);
    }

    private String getForeignSource(Integer nodeId) {
        return m_jdbcTemplate.queryForObject("select foreignSource from node where nodeid = ?", String.class, nodeId);
    }

    public void runScan(final NodeScan scan) throws InterruptedException, ExecutionException {
        final Task t = scan.createTask();
        t.schedule();
        t.waitFor();
    }

    private BackgroundTask anticipateEvents(String... ueis) {
        final CountDownLatch eventRecieved = new CountDownLatch(1);
        m_eventSubscriber.addEventListener(new EventListener() {

            @Override
            public void onEvent(IEvent e) {
                eventRecieved.countDown();
            }

            @Override
            public String getName() {
                return "Test Initial Setup";
            }
        }, Arrays.asList(ueis));

        return new BackgroundTask() {

            @Override
            public void await() throws InterruptedException {
                eventRecieved.await();
            }
        };

    }

}
