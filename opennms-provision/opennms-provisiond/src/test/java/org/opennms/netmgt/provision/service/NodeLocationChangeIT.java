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

package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.List;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.detector.icmp.IcmpDetector;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
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
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
@DirtiesContext
public class NodeLocationChangeIT {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionerRescanExistingFalseIT.class);

    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;

    @Autowired
    private Provisioner m_provisioner;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private ProvisionService m_provisionService;

    @Autowired
    @Qualifier("scheduledExecutor")
    private PausibleScheduledThreadPoolExecutor m_scheduledExecutor;

    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    protected EventAnticipator m_eventAnticipator;

    private ForeignSourceRepository m_foreignSourceRepository;

    private ForeignSource m_foreignSource;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "ERROR");

        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        assertTrue(m_snmpPeerFactory instanceof ProxySnmpAgentConfigFactory);

        m_eventAnticipator = m_mockEventIpcManager.getEventAnticipator();

        m_provisioner.start();

        m_foreignSource = new ForeignSource();
        m_foreignSource.setName("noRescanOnImport");
        m_foreignSource.setScanInterval(Duration.standardDays(1));

        final PluginConfig icmpDetector = new PluginConfig("ICMP", IcmpDetector.class.getName());
        icmpDetector.addParameter("timeout", "500");
        icmpDetector.addParameter("retries", "0");
        m_foreignSource.addDetector(icmpDetector);

        final PluginConfig snmpDetector = new PluginConfig("SNMP", SnmpDetector.class.getName());
        snmpDetector.addParameter("timeout", "500");
        snmpDetector.addParameter("retries", "0");
        m_foreignSource.addDetector(snmpDetector);

        m_foreignSourceRepository = new MockForeignSourceRepository();
        m_foreignSourceRepository.save(m_foreignSource);
        m_foreignSourceRepository.flush();

        m_provisionService.setForeignSourceRepository(m_foreignSourceRepository);
        m_provisionService.setHostnameResolver(new HostnameResolver() {
            @Override
            public String getHostname(InetAddress addr, String location) {
                return "opennms-com";
            }
        });

        m_scheduledExecutor.pause();
    }

    @Test(timeout=30000)
    @JUnitSnmpAgents({
            @JUnitSnmpAgent(host="192.0.2.201", port=161, resource="classpath:/testNoRescanOnImport-part1.properties"),
    })
    public void testNodeLocationChange() throws Exception {

        m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/testLocationChange-location1.xml"), Boolean.TRUE.toString());

        final List<OnmsNode> nodes = getNodeDao().findAll();
        assertEquals(1, nodes.size());
        // Verify node and it's location.
        OnmsNode node = nodes.get(0);
        assertEquals("Hyderabad", node.getLocation().getLocationName());
        assertEquals(1, node.getIpInterfaces().size());
        assertEquals(1, node.getSnmpInterfaces().size());
        // Verify that persisted Ip interface will have resolved hostname.
        assertEquals("opennms-com", node.getIpInterfaces().iterator().next().getIpHostName());
        m_eventAnticipator.reset();
        // Anticipate node location change and node updated events
        anticipateNodeLocationChangeEvent();
        m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/testLocationChange-location2.xml"), "dbonly");
        m_eventAnticipator.verifyAnticipated();
        node = nodes.get(0);
        // verify  node location change.
        assertEquals("Bangalore", node.getLocation().getLocationName());
        assertEquals(1, node.getIpInterfaces().size());

    }

    private void anticipateNodeLocationChangeEvent() {
        EventBuilder builder = new EventBuilder(EventConstants.NODE_LOCATION_CHANGED_EVENT_UEI, DefaultProvisionService.PROVISIOND);
        builder.setNodeid(1);
        builder.addParam(EventConstants.PARM_NODE_PREV_LOCATION, "Hyderabad");
        builder.addParam(EventConstants.PARM_NODE_CURRENT_LOCATION, "Bangalore");
        m_eventAnticipator.anticipateEvent(builder.getEvent());
        builder = new EventBuilder(EventConstants.NODE_UPDATED_EVENT_UEI, DefaultProvisionService.PROVISIOND);
        builder.setNodeid(1);
        m_eventAnticipator.anticipateEvent(builder.getEvent());


    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
}
