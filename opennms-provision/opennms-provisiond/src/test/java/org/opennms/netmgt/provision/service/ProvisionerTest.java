/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.test.snmp.MockSnmpDataProvider;
import org.opennms.core.test.snmp.MockSnmpDataProviderAware;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.TransactionAwareEventForwarder;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.OnmsAssetRequisition;
import org.opennms.netmgt.provision.persist.OnmsIpInterfaceRequisition;
import org.opennms.netmgt.provision.persist.OnmsMonitoredServiceRequisition;
import org.opennms.netmgt.provision.persist.OnmsNodeCategoryRequisition;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.OnmsServiceCategoryRequisition;
import org.opennms.netmgt.provision.persist.RequisitionVisitor;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.policies.NodeCategorySettingPolicy;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unit test for ModelImport application.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@DirtiesContext
public class ProvisionerTest implements InitializingBean, MockSnmpDataProviderAware {
    
    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DistPollerDao m_distPollerDao;
    
    @Autowired
    private AssetRecordDao m_assetRecordDao;
    
    @Autowired
    private ResourceLoader m_resourceLoader;
    
    @Autowired
    private ProvisionService m_provisionService;
    
    @Autowired
    private PausibleScheduledThreadPoolExecutor m_pausibleExecutor;
    
    @Autowired
    private ImportScheduler m_importSchedule;
    
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;
    
    @Autowired
    private DatabasePopulator m_populator;
    
    private EventAnticipator m_eventAnticipator;

    private ForeignSourceRepository m_foreignSourceRepository;
    
    private ForeignSource m_foreignSource;

	private MockSnmpDataProvider m_mockSnmpDataProvider;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @BeforeClass
    public static void setUpSnmpConfig() {
        final Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

        MockLogAppender.setupLogging(props);
    }
    
    @Before
    public void setUp() throws Exception {
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        assertTrue(m_snmpPeerFactory instanceof ProxySnmpAgentConfigFactory);
        
        // ensure this property is unset for tests and set it only in tests that need it
        System.getProperties().remove("org.opennms.provisiond.enableDeletionOfRequisitionedEntities");

        m_eventAnticipator = m_mockEventIpcManager.getEventAnticipator();
        
        //((TransactionAwareEventForwarder)m_provisioner.getEventForwarder()).setEventForwarder(m_mockEventIpcManager);
        m_provisioner.start();
        
        m_foreignSource = new ForeignSource();
        m_foreignSource.setName("imported:");
        m_foreignSource.setScanInterval(Duration.standardDays(1));
        
        final PluginConfig policy = new PluginConfig("setCategory", NodeCategorySettingPolicy.class.getName());
        policy.addParameter("category", "TestCategory");
        policy.addParameter("label", "localhost");
        
        m_foreignSource.addPolicy(policy);
        
        m_foreignSourceRepository = new MockForeignSourceRepository();
        m_foreignSourceRepository.save(m_foreignSource);
        
        m_provisionService.setForeignSourceRepository(m_foreignSourceRepository);
        
        m_pausibleExecutor.pause();
    }
    
    @After
    public void tearDown() {
    	// remove property set during tests
        System.getProperties().remove("org.opennms.provisiond.enableDeletionOfRequisitionedEntities");
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testVisit() throws Exception {
        final Requisition requisition = m_foreignSourceRepository.importResourceRequisition(new ClassPathResource("/NewFile2.xml"));
        final CountingVisitor visitor = new CountingVisitor();
        requisition.visit(visitor);
        verifyBasicImportCounts(visitor);
    }
    
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase
    // 192.0.2.0/24 reserved by IANA for testing purposes
    @JUnitSnmpAgent(host="192.0.2.123", resource="classpath:no-ipaddrtable.properties")
    public void testNoIPAddrTable() throws Exception {
        importFromResource("classpath:/no-ipaddrtable.xml", true);

        OnmsNode node = getNodeDao().findByForeignId("no-ipaddrtable", "123");
        
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(3, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(3, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(0, getSnmpInterfaceDao().countAll());
        
        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        runScan(scan);
   
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(3, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(3, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(0, getSnmpInterfaceDao().countAll());
        
    }
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase
    // 192.0.2.0/24 reserved by IANA for testing purposes
    @JUnitSnmpAgent(host="192.0.2.123", resource="classpath:lameForce10.properties")
    public void testLameForce10Agent() throws Exception {
        importFromResource("classpath:/lameForce10.xml", true);

        OnmsNode node = getNodeDao().findByForeignId("walk", "123");
        
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(3, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(3, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(0, getSnmpInterfaceDao().countAll());
        
        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        runScan(scan);
   
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(4, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(3, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(3, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(6, getSnmpInterfaceDao().countAll());
        
    }
    /**
     * We have to ignore this test until there is a DNS service available in the test harness
     * 
     * @throws ForeignSourceRepositoryException
     * @throws MalformedURLException
     */
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="opennms.com.", v4address="1.2.3.4", entries={
                    @DNSEntry(hostname="www", address="1.2.3.4")
                    // V6 support is only in master right now
                    // @DNSEntry(hostname="www", address="::1:2:3:4", ipv6=true)
            })
    })
    public void testDnsVisit() throws ForeignSourceRepositoryException, MalformedURLException {
        final Requisition requisition = m_foreignSourceRepository.importResourceRequisition(new UrlResource("dns://localhost:9153/opennms.com"));
        final CountingVisitor visitor = new CountingVisitor() {
            @Override
            public void visitNode(final OnmsNodeRequisition req) {
                LogUtils.debugf(this, "visitNode: %s/%s %s", req.getForeignSource(), req.getForeignId(), req.getNodeLabel());
                m_nodes.add(req);
                m_nodeCount++;
            }
            @Override
            public void visitInterface(final OnmsIpInterfaceRequisition req) {
                LogUtils.debugf(this, "visitInterface: %s", req.getIpAddr());
                m_ifaces.add(req);
                m_ifaceCount++;
            }
        };
        requisition.visit(visitor);

        verifyDnsImportCounts(visitor);
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testSendEventsOnImport() throws Exception {
    	final MockNetwork network = new MockNetwork();
        final MockNode node = network.addNode(1, "node1");
        network.addInterface("172.20.1.204");
        network.addService("ICMP");
        network.addService("HTTP");
        network.addInterface("172.20.1.201");
        network.addService("ICMP");
        network.addService("SNMP");
        
        anticpateCreationEvents(node);
        
        importFromResource("classpath:/tec_dump.xml", true);
        
        m_eventAnticipator.verifyAnticipated();
        
    }

    private void importFromResource(final String path, final Boolean rescanExisting) throws Exception {
        m_provisioner.importModelFromResource(m_resourceLoader.getResource(path), rescanExisting);
    }
    
    private void anticpateCreationEvents(final MockElement element) {
        element.visit(new MockVisitorAdapter() {
            @Override
            public void visitElement(final MockElement e) {
            	final Event newEvent = e.createNewEvent();
                LogUtils.debugf(this, "Anticipate Event: %s", newEvent.getUei());
                m_eventAnticipator.anticipateEvent(newEvent);
            }
            
        });
    }
    
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @Transactional
    public void testNonSnmpImportAndScan() throws Exception {
        importFromResource("classpath:/import_localhost.xml", true);
        
        final List<OnmsNode> nodes = getNodeDao().findAll();
        final OnmsNode node = nodes.get(0);

        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        
        runScan(scan);
        
        final OnmsNode scannedNode = getNodeDao().findAll().get(0);
        assertEquals("TestCategory", scannedNode.getCategories().iterator().next().getName());
                
    }
    
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testFindQuery() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", true);
        
        for (final OnmsAssetRecord assetRecord : getAssetRecordDao().findAll()) {
            LogUtils.debugf(this, "Building = %s", assetRecord.getBuilding());
        }
    }
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testBigImport() throws Exception {
    	final File file = new File("/tmp/tec_dump.xml.large");
        if (file.exists()) {
            m_eventAnticipator.reset();
            m_eventAnticipator.setDiscardUnanticipated(true);
            final String path = file.toURI().toURL().toExternalForm();
            LogUtils.debugf(this, "Importing: %s", path);
            importFromResource(path, true);
        }
        
    }
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @JUnitSnmpAgent(host="172.20.1.201", resource="classpath:snmpTestData1.properties")
    public void testPopulateWithSnmp() throws Exception {
        importFromResource("classpath:/tec_dump.xml", true);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(2, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(4, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(3, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(2, getSnmpInterfaceDao().countAll());
        
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="172.20.2.201", resource="classpath:snmpTestData3.properties"),
        // for discovering the "SNMP" service on the second interface
        @JUnitSnmpAgent(host="172.20.2.204", resource="classpath:snmpTestData3.properties")
    })
    @Transactional
    public void testPopulateWithSnmpAndNodeScan() throws Exception {
        importFromResource("classpath:/requisition_then_scan2.xml", true);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(1, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(1, getSnmpInterfaceDao().countAll());
        
        final List<OnmsNode> nodes = getNodeDao().findAll();
        final OnmsNode node = nodes.get(0);

        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        runScan(scan);
        
        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(2, getInterfaceDao().countAll());
        
        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 2, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(6, getSnmpInterfaceDao().countAll());
        
        
        // Node Delete
        importFromResource("classpath:/nonodes.xml", true);

        //Verify node count
        assertEquals(0, getNodeDao().countAll());
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="172.20.2.201", resource="classpath:snmpTestData3.properties"),
        // for discovering the "SNMP" service on the second interface
        @JUnitSnmpAgent(host="172.20.2.204", resource="classpath:snmpTestData3.properties")
    })
    @Transactional
    public void testPopulateWithoutSnmpAndNodeScan() throws Exception {
        importFromResource("classpath:/requisition_then_scan_no_snmp_svc.xml", true);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());
        
        assertEquals(0, getSnmpInterfaceDao().countAll());

        // Expect there to be no services since we are not provisioning one
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());

        final List<OnmsNode> nodes = getNodeDao().findAll();
        final OnmsNode node = nodes.get(0);

        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        runScan(scan);
        
        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(2, getInterfaceDao().countAll());
        
        //Verify ifservices count - discover snmp service on both ifs
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 2, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(6, getSnmpInterfaceDao().countAll());
        
        // Node Delete
        importFromResource("classpath:/nonodes.xml", true);

        //Verify node count
        assertEquals(0, getNodeDao().countAll());
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="10.1.15.245", resource="classpath:snmpwalk-demo.properties"),
        @JUnitSnmpAgent(host="10.3.20.23", resource="classpath:snmpwalk-demo.properties"),
        @JUnitSnmpAgent(host="2001:0470:e2f1:cafe:16c1:7cff:12d6:7bb9", resource="classpath:snmpwalk-demo.properties")
    })
    @Transactional
    public void testPopulateWithIpv6SnmpAndNodeScan() throws Exception {
        final ForeignSource fs = new ForeignSource();
        fs.setName("matt:");
        fs.addDetector(new PluginConfig("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector"));
        m_foreignSourceRepository.putDefaultForeignSource(fs);

        importFromResource("classpath:/requisition_then_scanv6.xml", true);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(1, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(1, getSnmpInterfaceDao().countAll());
        
        
        final List<OnmsNode> nodes = getNodeDao().findAll();
        final OnmsNode node = nodes.get(0);

        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        runScan(scan);
        
        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals("Unexpected number of IP interfaces found: " + getInterfaceDao().findAll(), 3, getInterfaceDao().countAll());
        
        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 3, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals("Unexpected number of service types found: " + getServiceTypeDao().findAll(), 1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals("Unexpected number of SNMP interfaces found: " + getSnmpInterfaceDao().findAll(), 6, getSnmpInterfaceDao().countAll());
        
        // Ensure that collection is on for all ip interfaces
        for(OnmsIpInterface iface : getInterfaceDao().findAll()) {
        	OnmsSnmpInterface snmpIface = iface.getSnmpInterface();
			assertNotNull("Expected an snmp interface associated with "+iface.getIpAddress(), snmpIface);
        	assertTrue("Expected snmp interface associated with "+iface.getIpAddress()+" to have collection enabled.", snmpIface.isCollectionEnabled());
        	
        }
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="10.1.15.245", resource="classpath:snmpwalk-demo.properties"),
        @JUnitSnmpAgent(host="10.3.20.23", resource="classpath:snmpwalk-demo.properties"),
        @JUnitSnmpAgent(host="2001:0470:e2f1:cafe:16c1:7cff:12d6:7bb9", resource="classpath:snmpwalk-demo.properties")
    })
    @Transactional
    public void testPopulateWithIpv6OnlySnmpAndNodeScan() throws Exception {
        importFromResource("classpath:/requisition_then_scanv6only.xml", true);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(1, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(1, getSnmpInterfaceDao().countAll());

        final List<OnmsNode> nodes = getNodeDao().findAll();
        final OnmsNode node = nodes.get(0);

        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        runScan(scan);
        
        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals("Unexpected number of IP interfaces found: " + getInterfaceDao().findAll(), 3, getInterfaceDao().countAll());
        
        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 3, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals("Unexpected number of service types found: " + getServiceTypeDao().findAll(), 1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals("Unexpected number of SNMP interfaces found: " + getSnmpInterfaceDao().findAll(), 6, getSnmpInterfaceDao().countAll());
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @Transactional
    @JUnitSnmpAgents({
    	@JUnitSnmpAgent(host="172.20.2.201", port=161, resource="classpath:snmpTestData3.properties"),
    	@JUnitSnmpAgent(host="172.20.2.202", port=161, resource="classpath:snmpTestData4.properties"),
    	@JUnitSnmpAgent(host="172.20.2.204", port=161, resource="classpath:snmpTestData4.properties")
    })
    public void testImportAddrThenChangeAddr() throws Exception {
        importFromResource("classpath:/requisition_then_scan2.xml", true);

        final List<OnmsNode> nodes = getNodeDao().findAll();
        final OnmsNode node = nodes.get(0);

        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());
        
        runScan(scan);
        
        m_nodeDao.flush();
        
        assertEquals(2, getInterfaceDao().countAll());

        System.err.println("-------------------------------------------------------------------------");

        m_mockSnmpDataProvider.setDataForAddress(new SnmpAgentAddress(InetAddressUtils.addr("172.20.2.201"), 161), m_resourceLoader.getResource("classpath:snmpTestData4.properties"));
        
        importFromResource("classpath:/requisition_primary_addr_changed.xml", true);

        final NodeScan scan2 = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId());

        runScan(scan2);
        
        m_nodeDao.flush();

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        LogUtils.debugf(this, "found: %s", getInterfaceDao().findAll());
        
        //Verify ipinterface count
        assertEquals(2, getInterfaceDao().countAll());
        
        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 2, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals("Unexpected number of service types found: " + getServiceTypeDao().findAll(), 1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(6, getSnmpInterfaceDao().countAll());
        
        
        // Node Delete
        importFromResource("classpath:/nonodes.xml", true);

        //Verify node count
        assertEquals(0, getNodeDao().countAll());
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testDeleteService() throws Exception {

        System.setProperty("org.opennms.provisiond.enableDeletionOfRequisitionedEntities", "true");

        assertTrue(m_provisionService.isRequisitionedEntityDeletionEnabled());

        // This test assumes that discovery is disabled
        assertFalse(m_provisionService.isDiscoveryEnabled());

        importFromResource("classpath:/deleteService.xml", true);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(1, getNodeDao().countAll());

        //Verify ipinterface count
        assertEquals(4, getInterfaceDao().countAll());

        //Verify ifservices count
        assertEquals(6, getMonitoredServiceDao().countAll());

        //Verify service count
        assertEquals(2, getServiceTypeDao().countAll());

        // Locate the service to be deleted
        final OnmsNode node = m_nodeDao.findByForeignId("deleteService", "4243");
        assertNotNull(node);
        final int nodeid = node.getId();

        m_eventAnticipator.reset();
        m_eventAnticipator.anticipateEvent(serviceDeleted(nodeid, "10.201.136.163", "HTTP"));

        m_mockEventIpcManager.sendEventToListeners(deleteService(nodeid, "10.201.136.163", "HTTP"));

        // this only waits until all the anticipated events are received so it is fast unless there is a bug
        m_eventAnticipator.waitForAnticipated(10000);
        m_eventAnticipator.verifyAnticipated();
    }

    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testDontDeleteRequisitionedService() throws Exception {
    	
    	assertFalse(m_provisionService.isRequisitionedEntityDeletionEnabled());
    	
        // This test assumes that discovery is disabled
        assertFalse(m_provisionService.isDiscoveryEnabled());
        
        importFromResource("classpath:/deleteService.xml", true);
        
        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(4, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(6, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(2, getServiceTypeDao().countAll());

        // Locate the service to be deleted
        final OnmsNode node = m_nodeDao.findByForeignId("deleteService", "4243");
        assertNotNull(node);
        final int nodeid = node.getId();

        
        m_eventAnticipator.reset();

        m_mockEventIpcManager.sendEventToListeners(deleteService(nodeid, "10.201.136.163", "HTTP"));
        
        // there is no event to wait for so make sure we don't get anything..
        m_eventAnticipator.waitForAnticipated(5000);
        m_eventAnticipator.verifyAnticipated();
        
        // Make sure the service is still there
        assertEquals(6, getMonitoredServiceDao().countAll());

    }

    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testDeleteInterface() throws Exception {
    	
    	System.setProperty("org.opennms.provisiond.enableDeletionOfRequisitionedEntities", "true");
    	assertTrue(m_provisionService.isRequisitionedEntityDeletionEnabled());

    	
        // This test assumes that discovery is disabled
        assertFalse(m_provisionService.isDiscoveryEnabled());

        importFromResource("classpath:/deleteService.xml", true);
        
        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(4, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(6, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(2, getServiceTypeDao().countAll());

        // Locate the service to be deleted
        final OnmsNode node = m_nodeDao.findByForeignId("deleteService", "4243");
        assertNotNull(node);
        final int nodeid = node.getId();
        final String ipaddr = "10.201.136.163";

        m_eventAnticipator.reset();
        m_eventAnticipator.anticipateEvent(serviceDeleted(nodeid, ipaddr, "ICMP"));
        m_eventAnticipator.anticipateEvent(serviceDeleted(nodeid, ipaddr, "HTTP"));
        m_eventAnticipator.anticipateEvent(interfaceDeleted(nodeid, ipaddr));

        m_mockEventIpcManager.sendEventToListeners(deleteInterface(nodeid, ipaddr));
        
        // this only waits until all the anticipated events are received so it is fast unless there is a bug
        m_eventAnticipator.waitForAnticipated(10000);
        m_eventAnticipator.verifyAnticipated();
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testDeleteNode() throws Exception {

        System.setProperty("org.opennms.provisiond.enableDeletionOfRequisitionedEntities", "true");
        assertTrue(m_provisionService.isRequisitionedEntityDeletionEnabled());


        // This test assumes that discovery is disabled
        assertFalse(m_provisionService.isDiscoveryEnabled());

        importFromResource("classpath:/deleteService.xml", true);
        
        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(4, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(6, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(2, getServiceTypeDao().countAll());

        // Locate the service to be deleted
        final OnmsNode node = m_nodeDao.findByForeignId("deleteService", "4243");
        assertNotNull(node);
        final int nodeid = node.getId();
        m_eventAnticipator.reset();

        m_eventAnticipator.anticipateEvent(serviceDeleted(nodeid, "10.136.160.1", "ICMP"));
        m_eventAnticipator.anticipateEvent(serviceDeleted(nodeid, "10.136.160.1", "HTTP"));
        m_eventAnticipator.anticipateEvent(interfaceDeleted(nodeid, "10.136.160.1"));
        
        m_eventAnticipator.anticipateEvent(serviceDeleted(nodeid, "10.201.136.163", "ICMP"));
        m_eventAnticipator.anticipateEvent(serviceDeleted(nodeid, "10.201.136.163", "HTTP"));
        m_eventAnticipator.anticipateEvent(interfaceDeleted(nodeid, "10.201.136.163"));
        
        m_eventAnticipator.anticipateEvent(serviceDeleted(nodeid, "10.201.136.161", "ICMP"));
        m_eventAnticipator.anticipateEvent(interfaceDeleted(nodeid, "10.201.136.161"));
        
        m_eventAnticipator.anticipateEvent(serviceDeleted(nodeid, "10.201.136.167", "ICMP"));
        m_eventAnticipator.anticipateEvent(interfaceDeleted(nodeid, "10.201.136.167"));
       
        m_eventAnticipator.anticipateEvent(nodeDeleted(nodeid));

        m_mockEventIpcManager.sendEventToListeners(deleteNode(nodeid));
        
        // this only waits until all the anticipated events are received so it is fast unless there is a bug
        m_eventAnticipator.waitForAnticipated(10000);
        m_eventAnticipator.verifyAnticipated();
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testPopulate() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", true);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(10, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(30, getInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(50, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(3, getServiceTypeDao().countAll());
    }
    
    private DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }

    private void runScan(final NodeScan scan) throws InterruptedException, ExecutionException {
    	final Task t = scan.createTask();
        t.schedule();
        t.waitFor();
    }
    
    private NodeDao getNodeDao() {
        return m_nodeDao;
    }


    private IpInterfaceDao getInterfaceDao() {
        return m_ipInterfaceDao;
    }


    private SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }


    private MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }


    private ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }
    
    private AssetRecordDao getAssetRecordDao() {
        return m_assetRecordDao;
    }
    
    /**
     * This test first bulk imports 10 nodes then runs update with 1 node missing
     * from the import file.
     * 
     * @throws ModelImportException
     */
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @Transactional
    public void testImportUtf8() throws Exception {
        m_provisioner.importModelFromResource(new ClassPathResource("/utf-8.xml"), true);
        
        assertEquals(1, getNodeDao().countAll());
        // \u00f1 is unicode for n~ 
        final OnmsNode onmsNode = getNodeDao().get(1);
        LogUtils.debugf(this, "node = %s", onmsNode);
        assertEquals("\u00f1ode2", onmsNode.getLabel());
        
    }
    
    /**
     * This test first bulk imports 10 nodes then runs update with 1 node missing
     * from the import file.
     * 
     * @throws ModelImportException
     */
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testDelete() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", true);
        assertEquals(10, getNodeDao().countAll());
        importFromResource("classpath:/tec_dump.xml.smalltest.delete", true);
        assertEquals(9, getNodeDao().countAll());
    
        importFromResource("classpath:/tec_dump.xml.smalltest.nonodes", true);
        assertEquals(0, getNodeDao().countAll());
    }

    /**
     * This test makes sure that asset information is getting imported properly.
     * @throws Exception
     */
    @Test(timeout=300000)
    public void testAssets() throws Exception {
        importFromResource("classpath:/tec_dump.xml", true);
        final OnmsNode n = getNodeDao().findByForeignId("matt:", "4243");
        assertEquals("Asset Record: Manufacturer",     "Dell",                   n.getAssetRecord().getManufacturer());
        assertEquals("Asset Record: Operating System", "Windows Pi",             n.getAssetRecord().getOperatingSystem());
        assertEquals("Asset Record: Description",      "Large and/or In Charge", n.getAssetRecord().getDescription());
    }
    
    //Scheduler tests
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionServiceGetScheduleForNodesCount() throws Exception {
        final List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
        final int nodeCount = getNodeDao().countAll();
        LogUtils.debugf(this, "NodeCount: %d", nodeCount);

        assertEquals(nodeCount, schedulesForNode.size());
        assertEquals(nodeCount, m_provisioner.getScheduleLength());
    }
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionServiceGetScheduleForNodesUponDelete() throws Exception {
       importFromResource("classpath:/tec_dump.xml.smalltest", true);

       List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
       
       assertEquals(10, schedulesForNode.size());
       
       importFromResource("classpath:/tec_dump.xml.smalltest.delete", true);
       
       schedulesForNode = m_provisionService.getScheduleForNodes();
       
       assertEquals(9, schedulesForNode.size());
       assertEquals(9, m_provisioner.getScheduleLength());
    }
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionerAddNodeToSchedule() throws Exception{
        m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(0, m_provisioner.getScheduleLength());
        
        final OnmsNode node = createNode();
        assertEquals(1, node.getId().intValue());
        
        assertNotNull(m_nodeDao.get(1));
        
        final EventBuilder bldr = new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Tests");
        bldr.setNodeid(1);
        
        m_mockEventIpcManager.broadcastNow(bldr.getEvent());
        
        assertEquals(1, m_provisioner.getScheduleLength());
    }
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionerRescanWorking() throws Exception{
        importFromResource("classpath:/tec_dump.xml.smalltest", true);
        
        m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(10, m_provisioner.getScheduleLength());
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionerRescanWorkingWithDiscoveredNodesDiscoveryDisabled() throws Exception{
        System.setProperty("org.opennms.provisiond.enableDiscovery", "false");
        // populator creates 4 provisioned nodes and 2 discovered nodes
        m_populator.populateDatabase();

        m_provisioner.scheduleRescanForExistingNodes();
        
        // make sure just the provisioned nodes are scheduled
        assertEquals(4, m_provisioner.getScheduleLength());
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionerRescanWorkingWithDiscoveredNodesDiscoveryEnabled() throws Exception{
    	System.setProperty("org.opennms.provisiond.enableDiscovery", "true");
        // populator creates 4 provisioned nodes and 2 discovered nodes
        m_populator.populateDatabase();

        m_provisioner.scheduleRescanForExistingNodes();
        
        // make sure all the nodes are scheduled (even the discovered ones)
        assertEquals(6, m_provisioner.getScheduleLength());
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionerRemoveNodeInSchedule() throws Exception{
        importFromResource("classpath:/tec_dump.xml.smalltest", true);

        //m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(10, m_provisioner.getScheduleLength());
        
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_DELETED_EVENT_UEI, "Tests");
        bldr.setNodeid(2);
        
        m_mockEventIpcManager.broadcastNow(bldr.getEvent());
        
        assertEquals(9, m_provisioner.getScheduleLength());
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionServiceScanIntervalCalcWorks() {
        long now = System.currentTimeMillis();
        
        Date date = new Date();
        date.setTime(now - 43200000);
        long lastPoll = date.getTime();
        long nextPoll = lastPoll + 86400000;
        long initialDelay = Math.max(0, nextPoll - now);
        
        assertEquals(43200000, initialDelay);
        
    }
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionerNodeRescanSchedule() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", true);
        
        List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
        
        assertEquals(10, schedulesForNode.size());
        
        //m_provisioner.scheduleRescanForExistingNodes();
        
        assertEquals(10, m_provisioner.getScheduleLength());
    }
    
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testProvisionerUpdateScheduleAfterImport() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", true);
        
        List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
        
        assertEquals(10, schedulesForNode.size());
        
        //m_provisioner.scheduleRescanForExistingNodes();
        
        assertEquals(10, m_provisioner.getScheduleLength());
        
        //reimport with one missing node
        importFromResource("classpath:/tec_dump.xml.smalltest.delete", true);
        
        //m_provisioner.scheduleRescanForExistingNodes();
        schedulesForNode = m_provisionService.getScheduleForNodes();
        
        //check the schedule to make sure that it deletes the node
        assertEquals(schedulesForNode.size(), m_provisioner.getScheduleLength());
        assertEquals(getNodeDao().countAll(), m_provisioner.getScheduleLength());
        
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @Transactional
    public void testSaveCategoriesOnUpdateNodeAttributes() throws Exception {
        final String TEST_CATEGORY = "TEST_CATEGORY";
        final String OLD_LABEL = "apknd";
        final String NEW_LABEL = "apknd-new";

        importFromResource("classpath:/tec_dump.xml.smalltest", true);

        final Collection<OnmsNode> nodes = m_nodeDao.findByLabel(OLD_LABEL);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());

        OnmsNode node = nodes.iterator().next();
        assertNotNull(node);

        OnmsNode nodeCopy = new OnmsNode();
        nodeCopy.setId(node.getId());
        nodeCopy.setLabel(OLD_LABEL);
        // TODO: Replace with constant
        nodeCopy.setLabelSource("U");

        assertNotSame(node, nodeCopy);
        assertEquals(OLD_LABEL, node.getLabel());
        assertFalse(node.hasCategory(TEST_CATEGORY));

        // Create a policy that will apply the category to the node
        final NodeCategorySettingPolicy policy = new NodeCategorySettingPolicy();
        policy.setCategory(TEST_CATEGORY);
        policy.setLabel(OLD_LABEL);

        // Apply the policy
        nodeCopy = policy.apply(nodeCopy);
        assertTrue(nodeCopy.hasCategory(TEST_CATEGORY));

        // Change the label of the node so that we can trigger a NODE_LABEL_CHANGED_EVENT_UEI event
        nodeCopy.setLabel(NEW_LABEL);
        // TODO: Replace with constant
        nodeCopy.setLabelSource("U");

        assertFalse(node.getLabel().equals(nodeCopy.getLabel()));

        m_provisionService.updateNodeAttributes(nodeCopy);

        // Flush here to force a write so we are sure that the OnmsCategories are correctly created
        m_nodeDao.flush();

        // Query by the new node label
        final OnmsNode node2 = m_nodeDao.findByLabel(NEW_LABEL).iterator().next();
        assertTrue(node2.hasCategory(TEST_CATEGORY));

        // Iterate over the events in the TransactionAwareEventForwarder to make
        // sure that the NODE_LABEL_CHANGED_EVENT_UEI event was sent
        boolean foundEvent = false;
        for (Log eventLog : ((TransactionAwareEventForwarder)m_provisioner.getEventForwarder()).requestPendingEventsList()) {
            for (Event event : eventLog.getEvents().getEventCollection()) {
                if (EventConstants.NODE_LABEL_CHANGED_EVENT_UEI.equals(event.getUei())) {
                    foundEvent = true;
                }
            }
        }
        assertTrue(String.format("Did not find anticipated %s event", EventConstants.NODE_LABEL_CHANGED_EVENT_UEI), foundEvent);
    }

    /**
     * Test that the parent-foreign-source attribute in a requisition can add a parent to a
     * node that resides in a different provisioning group.
     * 
     * @see http://issues.opennms.org/browse/NMS-4109
     */
    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @Transactional
    public void testParentForeignSource() throws Exception {
        importFromResource("classpath:/parent_foreign_source_server.xml", true);
        importFromResource("classpath:/parent_foreign_source_client.xml", true);

        final List<OnmsNode> nodes = getNodeDao().findAll();
        assertEquals(2, nodes.size());
        OnmsNode node = getNodeDao().findByLabel("www").iterator().next();
        assertEquals("admin", node.getParent().getLabel());
        assertEquals("192.168.1.12", node.getPathElement().getIpAddress());
        assertEquals("ICMP", node.getPathElement().getServiceName());
    }

    private static Event nodeDeleted(int nodeid) {
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_DELETED_EVENT_UEI, "Test");
        bldr.setNodeid(nodeid);
        return bldr.getEvent();        
    }
    
    private static Event deleteNode(int nodeid) {
        EventBuilder bldr = new EventBuilder(EventConstants.DELETE_NODE_EVENT_UEI, "Test");
        bldr.setNodeid(nodeid);
        return bldr.getEvent();        
    }
    
    private static Event interfaceDeleted(int nodeid, String ipaddr) {
        EventBuilder bldr = new EventBuilder(EventConstants.INTERFACE_DELETED_EVENT_UEI, "Test");
        bldr.setNodeid(nodeid);
        bldr.setInterface(addr(ipaddr));
        return bldr.getEvent();
    }
    
    private static Event deleteInterface(int nodeid, String ipaddr) {
        EventBuilder bldr = new EventBuilder(EventConstants.DELETE_INTERFACE_EVENT_UEI, "Test");
        bldr.setNodeid(nodeid);
        bldr.setInterface(addr(ipaddr));
        return bldr.getEvent();
    }
    
    private static Event serviceDeleted(int nodeid, String ipaddr, String svc) {
        EventBuilder bldr = new EventBuilder(EventConstants.SERVICE_DELETED_EVENT_UEI, "Test");
        bldr.setNodeid(nodeid);
        bldr.setInterface(addr(ipaddr));
        bldr.setService(svc);
        return bldr.getEvent();
    }
    
    private static Event deleteService(int nodeid, String ipaddr, String svc) {
        EventBuilder bldr = new EventBuilder(EventConstants.DELETE_SERVICE_EVENT_UEI, "Test");
        bldr.setNodeid(nodeid);
        bldr.setInterface(addr(ipaddr));
        bldr.setService(svc);
        return bldr.getEvent();
    }


    
    
    private OnmsNode createNode() {
        OnmsNode node = new OnmsNode();
        //node.setId(nodeId);
        node.setLastCapsdPoll(new Date());
        node.setForeignSource("imported:");
        node.setLabel("default");
        
        m_nodeDao.save(node);
        m_nodeDao.flush();
        return node;
    }
    
    private static void verifyDnsImportCounts(CountingVisitor visitor) {
        assertEquals(1, visitor.getModelImportCount());
        // 1 for "opennms.com", 1 for "www.opennms.com"
        assertEquals(2, visitor.getNodeCount());
        assertEquals(0, visitor.getNodeCategoryCount());
        // 1 IPv4 address per hostname
        assertEquals(2, visitor.getInterfaceCount());
        // 2 services per interface (ICMP + SNMP)
        assertEquals(4, visitor.getMonitoredServiceCount());
        assertEquals(0, visitor.getServiceCategoryCount());
        assertEquals(visitor.getModelImportCount(), visitor.getModelImportCompletedCount());
        assertEquals(visitor.getNodeCount(), visitor.getNodeCompletedCount());
        assertEquals(visitor.getNodeCategoryCount(), visitor.getNodeCategoryCompletedCount());
        assertEquals(visitor.getInterfaceCount(), visitor.getInterfaceCompletedCount());
        assertEquals(visitor.getMonitoredServiceCount(), visitor.getMonitoredServiceCompletedCount());
        assertEquals(visitor.getServiceCategoryCount(), visitor.getServiceCategoryCompletedCount());
    }
    
    private static void verifyBasicImportCounts(CountingVisitor visitor) {
        assertEquals(1, visitor.getModelImportCount());
        assertEquals(1, visitor.getNodeCount());
        assertEquals(3, visitor.getNodeCategoryCount());
        assertEquals(4, visitor.getInterfaceCount());
        assertEquals(6, visitor.getMonitoredServiceCount());
        assertEquals(0, visitor.getServiceCategoryCount());
        assertEquals(visitor.getModelImportCount(), visitor.getModelImportCompletedCount());
        assertEquals(visitor.getNodeCount(), visitor.getNodeCompletedCount());
        assertEquals(visitor.getNodeCategoryCount(), visitor.getNodeCategoryCompletedCount());
        assertEquals(visitor.getInterfaceCount(), visitor.getInterfaceCompletedCount());
        assertEquals(visitor.getMonitoredServiceCount(), visitor.getMonitoredServiceCompletedCount());
        assertEquals(visitor.getServiceCategoryCount(), visitor.getServiceCategoryCompletedCount());
    }

    static class CountingVisitor implements RequisitionVisitor {
        protected int m_modelImportCount;
        protected int m_modelImportCompleted;
        protected int m_nodeCount;
        protected int m_nodeCompleted;
        protected int m_nodeCategoryCount;
        protected int m_nodeCategoryCompleted;
        protected int m_ifaceCount;
        protected int m_ifaceCompleted;
        protected int m_svcCount;
        protected int m_svcCompleted;
        protected int m_svcCategoryCount;
        protected int m_svcCategoryCompleted;
        protected int m_assetCount;
        protected int m_assetCompleted;
        protected List<OnmsNodeRequisition> m_nodes = new ArrayList<OnmsNodeRequisition>();
        protected List<OnmsIpInterfaceRequisition> m_ifaces = new ArrayList<OnmsIpInterfaceRequisition>();

        public List<OnmsNodeRequisition> getNodes() {
            return m_nodes;
        }

        public List<OnmsIpInterfaceRequisition> getInterfaces() {
            return m_ifaces;
        }

        public int getModelImportCount() {
            return m_modelImportCount;
        }
        
        public int getModelImportCompletedCount() {
            return m_modelImportCompleted;
        }
        
        public int getNodeCount() {
            return m_nodeCount;
        }
        
        public int getNodeCompletedCount() {
            return m_nodeCompleted;
        }
        
        public int getInterfaceCount() {
            return m_ifaceCount;
        }
        
        public int getInterfaceCompletedCount() {
            return m_ifaceCompleted;
        }
        
        public int getMonitoredServiceCount() {
            return m_svcCount;
        }
        
        public int getMonitoredServiceCompletedCount() {
            return m_svcCompleted;
        }
        
        public int getNodeCategoryCount() {
            return m_nodeCategoryCount;
        }

        public int getNodeCategoryCompletedCount() {
            return m_nodeCategoryCompleted;
        }

        public int getServiceCategoryCount() {
            return m_svcCategoryCount;
        }

        public int getServiceCategoryCompletedCount() {
            return m_svcCategoryCompleted;
        }

        public int getAssetCount() {
            return m_assetCount;
        }
        
        public int getAssetCompletedCount() {
            return m_assetCompleted;
        }
        
        @Override
        public void visitModelImport(final Requisition req) {
            m_modelImportCount++;
        }

        @Override
        public void visitNode(final OnmsNodeRequisition nodeReq) {
            m_nodeCount++;
            assertEquals("apknd", nodeReq.getNodeLabel());
            assertEquals("4243", nodeReq.getForeignId());
        }

        @Override
        public void visitInterface(final OnmsIpInterfaceRequisition ifaceReq) {
            m_ifaceCount++;
        }

        @Override
        public void visitMonitoredService(final OnmsMonitoredServiceRequisition monSvcReq) {
            m_svcCount++;
        }

        @Override
        public void visitNodeCategory(final OnmsNodeCategoryRequisition catReq) {
            m_nodeCategoryCount++;
        }
        
        @Override
        public void visitServiceCategory(final OnmsServiceCategoryRequisition catReq) {
            m_svcCategoryCount++;
        }
        
        @Override
        public void visitAsset(final OnmsAssetRequisition assetReq) {
            m_assetCount++;
        }
        
        @Override
        public String toString() {
            return (new ToStringCreator(this)
                .append("modelImportCount", getModelImportCount())
                .append("modelImportCompletedCount", getModelImportCompletedCount())
                .append("nodeCount", getNodeCount())
                .append("nodeCompletedCount", getNodeCompletedCount())
                .append("nodeCategoryCount", getNodeCategoryCount())
                .append("nodeCategoryCompletedCount", getNodeCategoryCompletedCount())
                .append("interfaceCount", getInterfaceCount())
                .append("interfaceCompletedCount", getInterfaceCompletedCount())
                .append("monitoredServiceCount", getMonitoredServiceCount())
                .append("monitoredServiceCompletedCount", getMonitoredServiceCompletedCount())
                .append("serviceCategoryCount", getServiceCategoryCount())
                .append("serviceCategoryCompletedCount", getServiceCategoryCompletedCount())
                .append("assetCount", getAssetCount())
                .append("assetCompletedCount", getAssetCompletedCount())
                .toString());
        }

        @Override
        public void completeModelImport(Requisition req) {
            m_modelImportCompleted++;
        }

        @Override
        public void completeNode(OnmsNodeRequisition nodeReq) {
            m_nodeCompleted++;
        }

        @Override
        public void completeInterface(OnmsIpInterfaceRequisition ifaceReq) {
            m_ifaceCompleted++;
        }

        @Override
        public void completeMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
            m_svcCompleted++;
        }

        @Override
        public void completeNodeCategory(OnmsNodeCategoryRequisition catReq) {
            m_nodeCategoryCompleted++;
        }
        
        @Override
        public void completeServiceCategory(OnmsServiceCategoryRequisition catReq) {
            m_nodeCategoryCompleted++;
        }
        
        @Override
        public void completeAsset(OnmsAssetRequisition assetReq) {
            m_assetCompleted++;
        }
    }

    @Override
    public void setMockSnmpDataProvider(final MockSnmpDataProvider provider) {
        m_mockSnmpDataProvider = provider;
    }
}
