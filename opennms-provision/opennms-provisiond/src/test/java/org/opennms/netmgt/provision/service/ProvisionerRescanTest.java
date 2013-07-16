/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import java.util.List;
import java.util.Properties;

import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.detector.icmp.IcmpDetector;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unit test for ModelImport application.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
@DirtiesContext
public class ProvisionerRescanTest implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ProvisionerRescanTest.class);

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
    private PausibleScheduledThreadPoolExecutor m_pausibleExecutor;

    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    private EventAnticipator m_eventAnticipator;

    private ForeignSourceRepository m_foreignSourceRepository;

    private ForeignSource m_foreignSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "ERROR");

        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        assertTrue(m_snmpPeerFactory instanceof ProxySnmpAgentConfigFactory);
        
        // ensure this property is unset for tests and set it only in tests that need it
        System.getProperties().remove("org.opennms.provisiond.enableDeletionOfRequisitionedEntities");

        m_eventAnticipator = m_mockEventIpcManager.getEventAnticipator();
        
        //((TransactionAwareEventForwarder)m_provisioner.getEventForwarder()).setEventForwarder(m_mockEventIpcManager);
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
        
        m_pausibleExecutor.pause();
    }
    
    @After
    public void tearDown() {
    	// remove property set during tests
        System.getProperties().remove("org.opennms.provisiond.enableDeletionOfRequisitionedEntities");
    }

    private void setupLogging(final String logLevel) {
        final Properties config = new Properties();
        config.setProperty("log4j.logger.org.hibernate", "ERROR");
        config.setProperty("log4j.logger.org.springframework", "ERROR");
        config.setProperty("log4j.logger.org.hibernate.SQL", "ERROR");

        MockLogAppender.setupLogging(true, logLevel, config);
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    @Transactional
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="172.20.1.201", port=161, resource="classpath:testNoRescanOnImport-part1.properties"),
        @JUnitSnmpAgent(host="172.20.1.204", port=161, resource="classpath:testNoRescanOnImport-part1.properties"),
        @JUnitSnmpAgent(host="10.1.15.245", port=161, resource="classpath:testNoRescanOnImport-part2.properties")
    })
    public void testNoRescanOnImport() throws Exception {
        setupLogging("INFO");

        System.err.println("-------------------------------------------------------------------------");
        System.err.println("Import Part 1");
        System.err.println("-------------------------------------------------------------------------");

        importFromResource("classpath:/testNoRescanOnImport-part1.xml", true);

        final List<OnmsNode> nodes = getNodeDao().findAll();
        assertEquals(1, nodes.size());
        
        final OnmsNode node = nodes.get(0);
        assertEquals(1, node.getIpInterfaces().size());

        System.err.println("-------------------------------------------------------------------------");
        System.err.println("Import Part 2");
        System.err.println("-------------------------------------------------------------------------");

        setupLogging("DEBUG");
        m_eventAnticipator.reset();
        anticipateNoRescanSecondNodeEvents();
        importFromResource("classpath:/testNoRescanOnImport-part2.xml", false);
        m_eventAnticipator.verifyAnticipated();
        setupLogging("INFO");

        //Verify node count
        assertEquals(2, getNodeDao().countAll());

        for (final OnmsNode n : getNodeDao().findAll()) {
        	LOG.info("found node {}", n);
        	for (final OnmsIpInterface iface : n.getIpInterfaces()) {
        		LOG.info("  interface: {}", iface);
        	}
        }
        
        setupLogging("ERROR");
    }

    private void anticipateNoRescanSecondNodeEvents() {
        final String name = this.getClass().getSimpleName();

        EventBuilder builder = new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, name);
        builder.setNodeid(2);
        m_eventAnticipator.anticipateEvent(builder.getEvent());

        builder = new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, name);
        builder.setNodeid(2);
        builder.setInterface(InetAddressUtils.addr("10.1.15.245"));
        m_eventAnticipator.anticipateEvent(builder.getEvent());
        
        for (final String service : new String[] { "ICMP", "SNMP" }) {
            builder = new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, name);
            builder.setNodeid(2);
            builder.setInterface(InetAddressUtils.addr("10.1.15.245"));
            builder.setService(service);
            m_eventAnticipator.anticipateEvent(builder.getEvent());
        }
	}

	private void importFromResource(final String path, final Boolean rescanExisting) throws Exception {
        m_provisioner.importModelFromResource(m_resourceLoader.getResource(path), rescanExisting);
    }
    
    private NodeDao getNodeDao() {
        return m_nodeDao;
    }
}
