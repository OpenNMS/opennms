/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.policies.InterfaceMetadataSettingPolicy;
import org.opennms.netmgt.provision.persist.policies.NodeMetadataSettingPolicy;
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
        "classpath:/META-INF/opennms/applicationContext-snmp-profile-mapper.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-dns.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
public class NMS12990_IT extends ProvisioningITCase {

    @Autowired
    private Provisioner m_provisioner;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DatabasePopulator m_populator;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        final MockForeignSourceRepository mockForeignSourceRepository = new MockForeignSourceRepository();
        final ForeignSource fs = new ForeignSource();
        fs.setName("default");
        fs.setScanInterval(Duration.standardDays(1));
        fs.addDetector(new PluginConfig("ICMP", "org.opennms.netmgt.provision.service.MockServiceDetector"));
        fs.addDetector(new PluginConfig("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector"));

        final PluginConfig policy1 = new PluginConfig("setNodeMetaData1", NodeMetadataSettingPolicy.class.getName());
        policy1.addParameter("metadataKey", "nodeKey1");
        policy1.addParameter("metadataValue", "nodeValue1");
        policy1.addParameter("metadataContext", "nodeCustomContext1");
        policy1.addParameter("label", "~.*");
        fs.addPolicy(policy1);

        final PluginConfig policy2 = new PluginConfig("setNodeMetaData2", NodeMetadataSettingPolicy.class.getName());
        policy2.addParameter("metadataKey", "nodeKey2");
        policy2.addParameter("metadataValue", "nodeValue2");
        policy2.addParameter("metadataContext", "nodeCustomContext2");
        policy2.addParameter("label", "~.*");
        fs.addPolicy(policy2);

        final PluginConfig policy3 = new PluginConfig("setInterfaceMetaData1", InterfaceMetadataSettingPolicy.class.getName());
        policy3.addParameter("metadataKey", "interfaceKey3");
        policy3.addParameter("metadataValue", "interfaceValue3");
        policy3.addParameter("metadataContext", "interfaceCustomContext3");
        policy3.addParameter("ipAddress", "~192\\.168\\.3\\..*");
        policy3.addParameter("matchBehavior", "ALL_PARAMETER");
        fs.addPolicy(policy3);

        mockForeignSourceRepository.putDefaultForeignSource(fs);
        m_provisioner.getProvisionService().setForeignSourceRepository(mockForeignSourceRepository);
        m_provisioner.start();
    }

    @After
    public void tearDown() throws InterruptedException {
        m_populator.resetDatabase();
        waitForEverything();
    }


    @Test
    @JUnitSnmpAgents({
            @JUnitSnmpAgent(host="192.168.3.1", port=161, resource="classpath:/snmpwalk-space.properties"),
            @JUnitSnmpAgent(host="10.0.0.4", port=161, resource="classpath:/snmpwalk-space.properties")
    })
    public void testScanSpaceDevice() throws Exception {
        final String[] ueis = { EventConstants.PROVISION_SCAN_COMPLETE_UEI, EventConstants.PROVISION_SCAN_ABORTED_UEI, EventConstants.IMPORT_SUCCESSFUL_UEI };
        final CountDownLatch eventReceived = anticipateEvents(1, ueis);

        // import the requisition
        m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/NMS-12990.xml"), Boolean.TRUE.toString());
        waitForEverything();
        eventReceived.await(5, TimeUnit.MINUTES);

        List<OnmsNode> nodes = m_nodeDao.findAll();

        // check that only one node exists
        assertEquals(1, nodes.size());

        // get the node
        OnmsNode node = nodes.get(0);

        // run a node scan, so policies are applied for node and interfaces
        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), "foobar", "1", new OnmsMonitoringLocation());
        final Task t = scan.createTask();
        t.schedule();
        t.waitFor();

        // check that all metadata is correctly set
        assertThat(node.getMetaData().size(), is(2));
        assertThat(node.getMetaData(), containsInAnyOrder(new OnmsMetaData("nodeCustomContext1", "nodeKey1", "nodeValue1"),new OnmsMetaData("nodeCustomContext2", "nodeKey2", "nodeValue2")));
        assertThat(node.getIpInterfaceByIpAddress("10.0.0.4").getMetaData().size(), is(0));
        assertThat(node.getIpInterfaceByIpAddress("192.168.3.1").getMetaData().size(), is(1));
        assertThat(node.getIpInterfaceByIpAddress("192.168.3.1").getMetaData(), containsInAnyOrder(new OnmsMetaData("interfaceCustomContext3","interfaceKey3","interfaceValue3")));

        // import again without scanning the node
        m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/NMS-12990.xml"), "dbonly");
        waitForEverything();
        eventReceived.await(5, TimeUnit.MINUTES);

        // check that still only one node exists
        nodes = m_nodeDao.findAll();
        assertEquals(1, nodes.size());

        // get the node
        node = nodes.get(0);

        // check that the metadata still exists
        assertThat(node.getMetaData().size(), is(2));
        assertThat(node.getMetaData(), containsInAnyOrder(new OnmsMetaData("nodeCustomContext1", "nodeKey1", "nodeValue1"),new OnmsMetaData("nodeCustomContext2", "nodeKey2", "nodeValue2")));
        assertThat(node.getIpInterfaceByIpAddress("10.0.0.4").getMetaData().size(), is(0));
        assertThat(node.getIpInterfaceByIpAddress("192.168.3.1").getMetaData().size(), is(1));
        assertThat(node.getIpInterfaceByIpAddress("192.168.3.1").getMetaData(), containsInAnyOrder(new OnmsMetaData("interfaceCustomContext3","interfaceKey3","interfaceValue3")));
    }
}
