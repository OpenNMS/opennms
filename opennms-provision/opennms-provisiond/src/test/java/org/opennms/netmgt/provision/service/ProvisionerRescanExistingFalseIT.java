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

import java.util.List;
import java.util.Properties;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@DirtiesContext
public class ProvisionerRescanExistingFalseIT implements InitializingBean {
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

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "ERROR");

        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        assertTrue(m_snmpPeerFactory instanceof ProxySnmpAgentConfigFactory);
        
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
        
        m_scheduledExecutor.pause();
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
        @JUnitSnmpAgent(host="192.0.2.201", port=161, resource="classpath:/testNoRescanOnImport-part1.properties"),
        @JUnitSnmpAgent(host="192.0.2.204", port=161, resource="classpath:/testNoRescanOnImport-part1.properties"),
        @JUnitSnmpAgent(host="10.1.15.245", port=161, resource="classpath:/testNoRescanOnImport-part2.properties")
    })
    public void testNoRescanOnImport() throws Exception {
        executeTest(Boolean.FALSE.toString());
    }
    
    protected void executeTest(String rescanExistingFlag) throws Exception {
        setupLogging("INFO");

        System.err.println("-------------------------------------------------------------------------");
        System.err.println("Import Part 1");
        System.err.println("-------------------------------------------------------------------------");

        importFromResource("classpath:/testNoRescanOnImport-part1.xml", Boolean.TRUE.toString());

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
        importFromResource("classpath:/testNoRescanOnImport-part2.xml", rescanExistingFlag);
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

    protected void anticipateNoRescanSecondNodeEvents() {
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

	private void importFromResource(final String path, final String rescanExisting) throws Exception {
        m_provisioner.importModelFromResource(m_resourceLoader.getResource(path), rescanExisting);
    }
    
    private NodeDao getNodeDao() {
        return m_nodeDao;
    }
}
