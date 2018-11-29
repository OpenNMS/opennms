/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
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
import org.opennms.core.spring.BeanUtils;
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
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.NodeLabelChangedEventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.detector.snmp.SnmpDetector;
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
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import com.google.common.base.MoreObjects;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

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
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD) // XXX classMode right?
public class ProvisionerIT extends ProvisioningITCase implements InitializingBean, MockSnmpDataProviderAware {
    private static final Logger LOG = LoggerFactory.getLogger(ProvisionerIT.class);
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
    private MockNodeDao m_nodeDao;

    @Autowired
    private CategoryDao m_categoryDao;

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private AssetRecordDao m_assetRecordDao;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private ProvisionService m_provisionService;

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
        if (m_distPollerDao.findAll().size() == 0) {
            OnmsDistPoller distPoller = new OnmsDistPoller(DistPollerDao.DEFAULT_DIST_POLLER_ID);
            distPoller.setLabel("localhost");
            distPoller.setLocation(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
            distPoller.setType(OnmsMonitoringSystem.TYPE_OPENNMS);
            m_distPollerDao.save(distPoller);
        }

        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        assertTrue(m_snmpPeerFactory instanceof ProxySnmpAgentConfigFactory);

        // ensure this property is unset for tests and set it only in tests that need it
        System.getProperties().remove("org.opennms.provisiond.enableDeletionOfRequisitionedEntities");

        m_eventAnticipator = m_mockEventIpcManager.getEventAnticipator();

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

        final ForeignSource emptyForeignSource = new ForeignSource();
        emptyForeignSource.setName("empty");
        emptyForeignSource.setScanInterval(Duration.standardDays(1));
        m_foreignSourceRepository.save(emptyForeignSource);

        final ForeignSource snmpForeignSource = new ForeignSource();
        snmpForeignSource.setName("snmp");
        snmpForeignSource.setScanInterval(Duration.standardDays(1));
        final PluginConfig snmpDetector = new PluginConfig("SNMP", SnmpDetector.class.getName());
        snmpForeignSource.addDetector(snmpDetector);
        m_foreignSourceRepository.save(snmpForeignSource);

        m_foreignSourceRepository.flush();

        m_provisionService.setForeignSourceRepository(m_foreignSourceRepository);

        // make sure node scan scheduler is running initially
        getScanExecutor().resume();
        getScheduledExecutor().pause();
    }

    @After
    public void tearDown() {
        // remove property set during tests
        System.getProperties().remove("org.opennms.provisiond.enableDeletionOfRequisitionedEntities");
        m_eventAnticipator.reset();
    }

    @Test(timeout=300000)
    public void testVisit() throws Exception {
        final Requisition requisition = m_foreignSourceRepository.importResourceRequisition(new ClassPathResource("/NewFile2.xml"));
        final CountingVisitor visitor = new CountingVisitor();
        requisition.visit(visitor);
        verifyBasicImportCounts(visitor);
    }


    @Test(timeout=300000)
    // 192.0.2.0/24 reserved by IANA for testing purposes
    @JUnitSnmpAgent(host="192.0.2.123", resource="classpath:/no-ipaddrtable.properties")
    public void testNoIPAddrTable() throws Exception {
        importFromResource("classpath:/no-ipaddrtable.xml", Boolean.TRUE.toString());

        assertEquals(1, getNodeDao().countAll());

        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());

        //Verify ifservices count
        assertEquals(3, getMonitoredServiceDao().countAll());

        //Verify service count
        assertEquals(3, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(0, getSnmpInterfaceDao().countAll());

        runPendingScans();

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
    // 192.0.2.0/24 reserved by IANA for testing purposes
    @JUnitSnmpAgent(host="192.0.2.123", resource="classpath:/lameForce10.properties")
    public void testLameForce10Agent() throws Exception {
        importFromResource("classpath:/lameForce10.xml", Boolean.TRUE.toString());

        assertEquals(1, getNodeDao().countAll());

        //Verify ipinterface count
        assertEquals(1, getInterfaceDao().countAll());

        //Verify ifservices count
        assertEquals(3, getMonitoredServiceDao().countAll());

        //Verify service count
        assertEquals(3, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(0, getSnmpInterfaceDao().countAll());

        runPendingScans();

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
    @JUnitDNSServer(port=9153, zones={
        @DNSZone(name = "opennms.com.", v4address = "1.2.3.4", entries = {
            @DNSEntry(hostname = "www", data = "1.2.3.4")
            ,
            @DNSEntry(hostname = "www", data = "::1:2:3:4", type = "AAAA")
        })
    })
    public void testDnsVisit() throws ForeignSourceRepositoryException, MalformedURLException {
        final Requisition requisition = m_foreignSourceRepository.importResourceRequisition(new UrlResource("dns://localhost:9153/opennms.com"));
        final CountingVisitor visitor = new CountingVisitor() {
            @Override
            public void visitNode(final OnmsNodeRequisition req) {
                LOG.debug("visitNode: {}/{} {}", req.getForeignSource(), req.getForeignId(), req.getNodeLabel());
                m_nodes.add(req);
                m_nodeCount++;
            }
            @Override
            public void visitInterface(final OnmsIpInterfaceRequisition req) {
                LOG.debug("visitInterface: {}", req.getIpAddr());
                m_ifaces.add(req);
                m_ifaceCount++;
            }
        };
        requisition.visit(visitor);

        verifyDnsImportCounts(visitor);
    }

    @Test(timeout=300000)
    public void testSendEventsOnImport() throws Exception {

        final int nextNodeId = m_nodeDao.getNextNodeId();
        final String nodeLabel = "node1";

        final MockNetwork network = new MockNetwork();
        final MockNode node = network.addNode(nextNodeId, nodeLabel);
        network.addInterface("192.0.2.204");
        network.addService("ICMP");
        network.addService("HTTP");
        network.addInterface("192.0.2.201");
        network.addService("ICMP");
        network.addService("SNMP");

        anticipateCreationEvents(node);
        m_eventAnticipator.anticipateEvent(getNodeCategoryEvent(nextNodeId, nodeLabel));

        for (final Event e : m_eventAnticipator.getAnticipatedEvents()) {
            System.err.println("anticipated: " + e);
        }

        importFromResource("classpath:/tec_dump.xml", Boolean.TRUE.toString());

        for (final Event e : m_eventAnticipator.getAnticipatedEventsReceived()) {
            System.err.println("received anticipated: " + e);
        }
        for (final Event e : m_eventAnticipator.getUnanticipatedEvents()) {
            System.err.println("received unanticipated: " + e);
        }

        m_eventAnticipator.verifyAnticipated();

    }

    private void importFromResource(final String path, final String rescanExisting) throws Exception {
        m_provisioner.importModelFromResource(m_resourceLoader.getResource(path), rescanExisting);
        waitForImport();
    }

    private void anticipateCreationEvents(final MockElement element) {
        element.visit(new MockVisitorAdapter() {
            @Override
            public void visitElement(final MockElement e) {
                final Event newEvent = e.createNewEvent();
                LOG.debug("Anticipate Event: {}", newEvent.getUei());
                m_eventAnticipator.anticipateEvent(newEvent);
            }

        });
    }

    @Test(timeout=300000)
    public void testNonSnmpImportAndScan() throws Exception {
        importFromResource("classpath:/import_localhost.xml", Boolean.TRUE.toString());

        runPendingScans();

        final OnmsNode scannedNode = getNodeDao().findAll().get(0);
        assertEquals("TestCategory", scannedNode.getCategories().iterator().next().getName());

    }

    @Test(timeout=300000)
    public void testFindQuery() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", Boolean.TRUE.toString());

        for (final OnmsAssetRecord assetRecord : getAssetRecordDao().findAll()) {
            LOG.debug("Building = {}", assetRecord.getBuilding());
        }
    }

    @Test(timeout=300000)
    public void testBigImport() throws Exception {
        final File file = new File("/tmp/tec_dump.xml.large");
        if (file.exists()) {
            m_eventAnticipator.reset();
            m_eventAnticipator.setDiscardUnanticipated(true);
            final String path = file.toURI().toURL().toExternalForm();
            LOG.debug("Importing: {}", path);
            importFromResource(path, Boolean.TRUE.toString());
        }

    }

    @Test(timeout=300000)
    @JUnitSnmpAgent(host="192.0.2.201", resource="classpath:/snmpTestData1.properties")
    public void testPopulateWithSnmp() throws Exception {

        importFromResource("classpath:/tec_dump.xml", Boolean.TRUE.toString());

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
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="198.51.100.201", resource="classpath:/snmpTestData3.properties"),
        // for discovering the "SNMP" service on the second interface
        @JUnitSnmpAgent(host="198.51.100.204", resource="classpath:/snmpTestData3.properties")
    })
    public void testPopulateWithSnmpAndNodeScan() throws Exception {
        importFromResource("classpath:/requisition_then_scan2.xml", Boolean.TRUE.toString());

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

        runPendingScans();

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
        importFromResource("classpath:/nonodes-snmp.xml", Boolean.TRUE.toString());

        //Verify node count
        assertEquals(0, getNodeDao().countAll());
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="198.51.100.201", resource="classpath:/snmpTestData3.properties"),
        // for discovering the "SNMP" service on the second interface
        @JUnitSnmpAgent(host="198.51.100.204", resource="classpath:/snmpTestData3.properties")
    })
    public void testPopulateWithoutSnmpAndNodeScan() throws Exception {
        importFromResource("classpath:/requisition_then_scan_no_snmp_svc.xml", Boolean.TRUE.toString());

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

        runPendingScans();

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
        importFromResource("classpath:/nonodes-snmp.xml", Boolean.TRUE.toString());

        //Verify node count
        assertEquals(0, getNodeDao().countAll());
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="10.1.15.245", resource="classpath:/snmpwalk-demo.properties"),
        @JUnitSnmpAgent(host="10.3.20.23", resource="classpath:/snmpwalk-demo.properties"),
        @JUnitSnmpAgent(host="2001:0470:e2f1:cafe:16c1:7cff:12d6:7bb9", resource="classpath:/snmpwalk-demo.properties")
    })
    public void testPopulateWithIpv6SnmpAndNodeScan() throws Exception {
        final ForeignSource fs = new ForeignSource();
        fs.setName("matt:");
        fs.addDetector(new PluginConfig("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector"));
        m_foreignSourceRepository.putDefaultForeignSource(fs);

        importFromResource("classpath:/requisition_then_scanv6.xml", Boolean.TRUE.toString());

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

        runPendingScans();

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
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="10.1.15.245", resource="classpath:/snmpwalk-demo.properties"),
        @JUnitSnmpAgent(host="10.3.20.23", resource="classpath:/snmpwalk-demo.properties"),
        @JUnitSnmpAgent(host="2001:0470:e2f1:cafe:16c1:7cff:12d6:7bb9", resource="classpath:/snmpwalk-demo.properties")
    })
    public void testPopulateWithIpv6OnlySnmpAndNodeScan() throws Exception {
        importFromResource("classpath:/requisition_then_scanv6only.xml", Boolean.TRUE.toString());

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

        runPendingScans();

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
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="198.51.100.201", port=161, resource="classpath:/snmpTestData3.properties"),
        @JUnitSnmpAgent(host="198.51.100.202", port=161, resource="classpath:/snmpTestData4.properties"),
        @JUnitSnmpAgent(host="198.51.100.204", port=161, resource="classpath:/snmpTestData4.properties")
    })
    public void testImportAddrThenChangeAddr() throws Exception {
        // Node has 198.51.100.201 as a primary IP address
        importFromResource("classpath:/requisition_then_scan2.xml", Boolean.TRUE.toString());

        runPendingScans();

        m_nodeDao.flush();

        assertEquals(2, getInterfaceDao().countAll());

        System.err.println("-------------------------------------------------------------------------");

        // Wait long enough for the previously discovered interfaces services
        // to become obsolete (the milliseconds are trimmed when comparing the dates)
        Thread.sleep(1000);

        // Node has 198.51.100.202 as a primary IP address
        importFromResource("classpath:/requisition_primary_addr_changed.xml", Boolean.TRUE.toString());

        runPendingScans();

        m_nodeDao.flush();

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(1, getNodeDao().countAll());

        //Verify ipinterface count
        assertEquals("Unexpected number of interfaces found: "+getInterfaceDao().findAll(), 2, getInterfaceDao().countAll());

        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 2, getMonitoredServiceDao().countAll());

        //Verify service count
        assertEquals("Unexpected number of service types found: " + getServiceTypeDao().findAll(), 1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(6, getSnmpInterfaceDao().countAll());

        // Node Delete
        importFromResource("classpath:/nonodes-snmp.xml", Boolean.TRUE.toString());

        //Verify node count
        assertEquals(0, getNodeDao().countAll());
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    @JUnitSnmpAgent(host="198.51.100.201", port=161, resource="classpath:/snmpTestData3.properties")
    public void testIfIndexChangeNms6567() throws Exception {
        importFromResource("classpath:/requisition_then_scan2.xml", Boolean.TRUE.toString());

        final List<OnmsNode> nodes = getNodeDao().findAll();
        final OnmsNode node = nodes.get(0);

        runPendingScans();

        m_nodeDao.flush();

        assertEquals(2, getInterfaceDao().countAll());

        // Verify the initial state of the ifIndex values
        assertEquals(5, getInterfaceDao().get(node, "198.51.100.201").getIfIndex().intValue());
        assertEquals(5, getInterfaceDao().get(node, "198.51.100.201").getSnmpInterface().getIfIndex().intValue());
        assertEquals(4, getInterfaceDao().get(node, "198.51.100.204").getIfIndex().intValue());
        assertEquals(4, getInterfaceDao().get(node, "198.51.100.204").getSnmpInterface().getIfIndex().intValue());

        // This is a pretty pedantic check :)
        assertEquals(4, getSnmpInterfaceDao().findByNodeIdAndIfIndex(node.getId(), 4).getIfIndex().intValue());
        assertEquals(5, getSnmpInterfaceDao().findByNodeIdAndIfIndex(node.getId(), 5).getIfIndex().intValue());

        // Swap the ifIndex values for the two interfaces
        m_mockSnmpDataProvider.updateIntValue(new SnmpAgentAddress(InetAddressUtils.addr("198.51.100.201"), 161), ".1.3.6.1.2.1.4.20.1.2.198.51.100.201", 4);
        m_mockSnmpDataProvider.updateIntValue(new SnmpAgentAddress(InetAddressUtils.addr("198.51.100.201"), 161), ".1.3.6.1.2.1.4.20.1.2.198.51.100.204", 5);

        // Rescan
        m_mockEventIpcManager.sendEventToListeners(nodeUpdated(node.getId()));
        runPendingScans();

        m_nodeDao.flush();

        assertEquals(2, getInterfaceDao().countAll());

        // Verify that the ifIndex fields on the interfaces have been updated
        assertEquals(4, getInterfaceDao().get(node, "198.51.100.201").getIfIndex().intValue());
        assertEquals(4, getInterfaceDao().get(node, "198.51.100.201").getSnmpInterface().getIfIndex().intValue());
        assertEquals(5, getInterfaceDao().get(node, "198.51.100.204").getIfIndex().intValue());
        assertEquals(5, getInterfaceDao().get(node, "198.51.100.204").getSnmpInterface().getIfIndex().intValue());
    }

    // fail if we take more than five minutes
    @Test(timeout=300000)
    // Start the test with an empty SNMP agent
    @JUnitSnmpAgent(host="198.51.100.201", port=161, resource="classpath:/snmpwalk-empty.properties")
    public void testProvisionerServiceRescanAfterAddingSnmpNms7838() throws Exception {
        importFromResource("classpath:/requisition_then_scan2.xml", Boolean.TRUE.toString());

        final List<OnmsNode> nodes = getNodeDao().findAll();
        final OnmsNode node = nodes.get(0);

        runPendingScans();

        m_nodeDao.flush();

        assertEquals(1, getInterfaceDao().countAll());

        // Make sure that the OnmsIpInterface doesn't have an ifIndex
        assertNull(getInterfaceDao().get(node, "198.51.100.201").getIfIndex());

        // Make sure that no OnmsSnmpInterface records exist for the node
        assertNull(getSnmpInterfaceDao().findByNodeIdAndIfIndex(node.getId(), 4));
        assertNull(getSnmpInterfaceDao().findByNodeIdAndIfIndex(node.getId(), 5));

        LOG.info("******************** ADDING SNMP DATA ********************");

        // Add some SNMP data to the agent
        m_mockSnmpDataProvider.setDataForAddress(new SnmpAgentAddress(addr("198.51.100.201"), 161), new DefaultResourceLoader().getResource("classpath:/snmpTestData3.properties"));

        // Rescan
        m_mockEventIpcManager.sendEventToListeners(nodeUpdated(node.getId()));
        runPendingScans();

        m_nodeDao.flush();

        // Make sure that a second interface was added from the SNMP agent data
        assertEquals(2, getInterfaceDao().countAll());

        // Verify the ifIndex entries
        assertEquals(5, getInterfaceDao().get(node, "198.51.100.201").getIfIndex().intValue());
        assertEquals(5, getInterfaceDao().get(node, "198.51.100.201").getSnmpInterface().getIfIndex().intValue());
        assertEquals(4, getInterfaceDao().get(node, "198.51.100.204").getIfIndex().intValue());
        assertEquals(4, getInterfaceDao().get(node, "198.51.100.204").getSnmpInterface().getIfIndex().intValue());
    }

    @Test
    public void testDeleteService() throws Exception {

        // This test assumes that discovery is disabled
        assertFalse(m_provisionService.isDiscoveryEnabled());

        importFromResource("classpath:/deleteService.xml", Boolean.TRUE.toString());

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
    public void testDeleteInterface() throws Exception {

        // This test assumes that discovery is disabled
        assertFalse(m_provisionService.isDiscoveryEnabled());

        importFromResource("classpath:/deleteService.xml", Boolean.TRUE.toString());

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
    public void testDeleteNode() throws Exception {

        // This test assumes that discovery is disabled
        assertFalse(m_provisionService.isDiscoveryEnabled());

        importFromResource("classpath:/deleteService.xml", Boolean.TRUE.toString());

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

    @Test
    public void testDowntimeModelDeleteServiceEventDiscoveryDisabledDeletionEnabled() throws Exception {
        System.setProperty("org.opennms.provisiond.enableDiscovery", "false");
        assertFalse(m_provisionService.isDiscoveryEnabled());

        importFromResource("classpath:/deleteService.xml", Boolean.TRUE.toString());
        final OnmsNode node = m_nodeDao.findByForeignId("deleteService", "4243");
        m_eventAnticipator.reset();

        // only the service deletion should be fired
        m_eventAnticipator.anticipateEvent(serviceDeleted(node.getId(), "10.201.136.163", "ICMP"));
        m_mockEventIpcManager.sendEventToListeners(deleteService(node.getId(), "10.201.136.163", "ICMP"));
        m_eventAnticipator.verifyAnticipated();
    }

    @Test
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="198.51.100.201", resource="classpath:/snmpTestData3.properties"),
        @JUnitSnmpAgent(host="198.51.100.204", resource="classpath:/snmpTestData3.properties")
    })
    public void testDowntimeModelDeleteServiceEventDiscoveryEnabledDeletionDisabledDiscoveredNode() throws Exception {
        System.setProperty("org.opennms.provisiond.enableDiscovery", "true");
        assertTrue(m_provisionService.isDiscoveryEnabled());

        final NewSuspectScan scan = m_provisioner.createNewSuspectScan(addr("198.51.100.201"), null, null);
        runScan(scan);

        assertEquals(2, m_ipInterfaceDao.findAll().size());
        LOG.debug("ifaces = {}", m_ipInterfaceDao.findAll());
        final List<OnmsIpInterface> ifaces = m_ipInterfaceDao.findByIpAddress("198.51.100.201");
        assertEquals(1, ifaces.size());
        final OnmsNode node = ifaces.iterator().next().getNode();
        assertEquals(2, node.getIpInterfaces().size());
        assertEquals(2, getMonitoredServiceDao().findAll().size()); // SNMP on each of the 2 interfaces
        m_eventAnticipator.reset();

        // the service and interface should be deleted
        // since there is another interface, the node remains
        m_eventAnticipator.anticipateEvent(serviceDeleted(node.getId(), "198.51.100.201", "SNMP"));
        m_eventAnticipator.anticipateEvent(interfaceDeleted(node.getId(), "198.51.100.201"));
        getScheduledExecutor().resume();
        m_mockEventIpcManager.sendEventToListeners(deleteService(node.getId(), "198.51.100.201", "SNMP"));
        m_eventAnticipator.waitForAnticipated(10000);
        m_eventAnticipator.verifyAnticipated();
    }

    @Test
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="198.51.100.201", resource="classpath:/snmpwalk-system.properties")
    })
    public void testDowntimeModelDeleteServiceEventDiscoveryEnabledDeletionDisabledDiscoveredNodeSingleInterface() throws Exception {
        System.setProperty("org.opennms.provisiond.enableDiscovery", "true");
        assertTrue(m_provisionService.isDiscoveryEnabled());

        final NewSuspectScan scan = m_provisioner.createNewSuspectScan(addr("198.51.100.201"), null, null);
        runScan(scan);

        assertEquals(1, m_ipInterfaceDao.findAll().size());
        LOG.debug("ifaces = {}", m_ipInterfaceDao.findAll());
        final List<OnmsIpInterface> ifaces = m_ipInterfaceDao.findByIpAddress("198.51.100.201");
        assertEquals(1, ifaces.size());
        final OnmsNode node = ifaces.iterator().next().getNode();
        assertEquals(1, node.getIpInterfaces().size());
        m_eventAnticipator.reset();

        // everything up to the node should be deleted, since there is only a single interface with a single service
        // since there is another interface, the node remains
        m_eventAnticipator.anticipateEvent(serviceDeleted(node.getId(), "198.51.100.201", "SNMP"));
        m_eventAnticipator.anticipateEvent(interfaceDeleted(node.getId(), "198.51.100.201"));
        m_eventAnticipator.anticipateEvent(nodeDeleted(node.getId()));
        getScheduledExecutor().resume();
        m_mockEventIpcManager.sendEventToListeners(deleteService(node.getId(), "198.51.100.201", "SNMP"));
        m_eventAnticipator.waitForAnticipated(10000);
        m_eventAnticipator.verifyAnticipated();
    }

    @Test(timeout=300000)
    public void testPopulate() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", Boolean.TRUE.toString());

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

    /**
     * NodeScans are automatically created and scheduled
     * in response to nodeCreated/nodeUpdated events, sent when
     * the provisioning requisitions are imported.
     *
     * Instead of creating an additional scan, we should opt
     * to use the one that was automatically created in order
     * to avoid running multiple scans for a given
     * node in parallel. Running multiple scans for a single node
     * in parallel can lead to undefined and intermittent behavior.
     *
     * If a new scan must be created, care must be taken to delete
     * the schedule for the scan that was automatically created.
     *
     */
    private void runPendingScans() throws InterruptedException, ExecutionException {
        final boolean paused = getScheduledExecutor().isPaused();
        if (paused) getScheduledExecutor().resume();
        waitForEverything();
        if (paused) getScheduledExecutor().pause();
    }

    private void runScan(final Scan scan) throws InterruptedException, ExecutionException {
        final boolean paused = getScheduledExecutor().isPaused();
        if (paused) getScheduledExecutor().resume();
        final Task t = scan.createTask();
        t.schedule();
        t.waitFor();
        waitForEverything();
        if (paused) getScheduledExecutor().pause();
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
    public void testImportUtf8() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        m_provisioner.importModelFromResource(new ClassPathResource("/utf-8.xml"), Boolean.TRUE.toString());

        assertEquals(1, getNodeDao().countAll());
        // \u00f1 is unicode for n~
        final OnmsNode onmsNode = getNodeDao().get(nextNodeId);
        LOG.debug("node = {}", onmsNode);
        assertEquals("\u00f1ode2", onmsNode.getLabel());

    }

    /**
     * This test first bulk imports 10 nodes then runs update with 1 node missing
     * from the import file.
     *
     * @throws ModelImportException
     */
    @Test(timeout=300000)
    public void testDelete() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", Boolean.TRUE.toString());
        assertEquals(10, getNodeDao().countAll());
        importFromResource("classpath:/tec_dump.xml.smalltest.delete", Boolean.TRUE.toString());
        assertEquals(9, getNodeDao().countAll());

        importFromResource("classpath:/tec_dump.xml.smalltest.nonodes", Boolean.TRUE.toString());
        assertEquals(0, getNodeDao().countAll());
    }

    /**
     * This test makes sure that asset information is getting imported properly.
     * @throws Exception
     */
    @Test(timeout=300000)
    public void testAssets() throws Exception {
        importFromResource("classpath:/tec_dump.xml", Boolean.TRUE.toString());
        final OnmsNode n = getNodeDao().findByForeignId("empty", "4243");
        assertEquals("Asset Record: Manufacturer",     "Dell",                   n.getAssetRecord().getManufacturer());
        assertEquals("Asset Record: Operating System", "Windows Pi",             n.getAssetRecord().getOperatingSystem());
        assertEquals("Asset Record: Description",      "Large and/or In Charge", n.getAssetRecord().getDescription());
    }

    //Scheduler tests
    @Test(timeout=300000)
    public void testProvisionServiceGetScheduleForNodesCount() throws Exception {
        getScanExecutor().pause();
        m_provisioner.scheduleRescanForExistingNodes();
        final List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
        final int nodeCount = getNodeDao().countAll();
        LOG.debug("NodeCount: {}", nodeCount);

        assertEquals(nodeCount, schedulesForNode.size());
        assertEquals(nodeCount, m_provisioner.getScheduleLength());
    }

    @Test(timeout=300000)
    public void testProvisionServiceGetScheduleForNodesUponDelete() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", Boolean.TRUE.toString());
        getScanExecutor().pause();

        m_provisioner.scheduleRescanForExistingNodes();
        List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();

        assertEquals(10, schedulesForNode.size());

        getScanExecutor().resume();
        importFromResource("classpath:/tec_dump.xml.smalltest.delete", Boolean.TRUE.toString());
        getScanExecutor().pause();

        m_provisioner.scheduleRescanForExistingNodes();
        schedulesForNode = m_provisionService.getScheduleForNodes();

        assertEquals(9, schedulesForNode.size());
        assertEquals(9, m_provisioner.getScheduleLength());
    }

    @Test(timeout=300000)
    public void testProvisionerAddNodeToSchedule() throws Exception{
        final int nextNodeId = m_nodeDao.getNextNodeId();

        m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(0, m_provisioner.getScheduleLength());

        final OnmsNode node = createNode("empty");
        assertEquals(nextNodeId, node.getId().intValue());

        assertNotNull(m_nodeDao.get(nextNodeId));

        final EventBuilder bldr = new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Tests");
        bldr.setNodeid(nextNodeId);

        m_mockEventIpcManager.broadcastNow(bldr.getEvent());

        assertEquals(1, m_provisioner.getScheduleLength());
    }

    @Test(timeout=300000)
    public void testProvisionerRescanWorking() throws Exception{
        importFromResource("classpath:/tec_dump.xml.smalltest", Boolean.TRUE.toString());
        getScanExecutor().pause();

        m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(10, m_provisioner.getScheduleLength());
    }

    @Test(timeout=300000)
    public void testProvisionerRescanWorkingWithDiscoveredNodesDiscoveryDisabled() throws Exception{
        System.setProperty("org.opennms.provisiond.enableDiscovery", "false");
        // populator creates 4 provisioned nodes and 2 discovered nodes
        try {
            m_populator.populateDatabase();

            m_provisioner.scheduleRescanForExistingNodes();

            // make sure just the provisioned nodes are scheduled
            assertEquals(4, m_provisioner.getScheduleLength());
        } finally {
            m_populator.resetDatabase();
        }
    }

    @Test(timeout=300000)
    public void testProvisionerRescanWorkingWithDiscoveredNodesDiscoveryEnabled() throws Exception{
        System.setProperty("org.opennms.provisiond.enableDiscovery", "true");
        // populator creates 4 provisioned nodes and 2 discovered nodes
        try {
            m_populator.populateDatabase();

            m_provisioner.scheduleRescanForExistingNodes();

            // make sure all the nodes are scheduled (even the discovered ones)
            assertEquals(6, m_provisioner.getScheduleLength());
        } finally {
            m_populator.resetDatabase();
        }
    }

    @Test(timeout=300000)
    public void testProvisionerRemoveNodeInSchedule() throws Exception{
        importFromResource("classpath:/tec_dump.xml.smalltest", Boolean.TRUE.toString());
        getScanExecutor().pause();

        m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(10, m_provisioner.getScheduleLength());

        final List<OnmsNode> nodes = m_nodeDao.findAll();
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_DELETED_EVENT_UEI, "Tests");
        bldr.setNodeid(nodes.get(nodes.size() - 1).getId());

        m_mockEventIpcManager.broadcastNow(bldr.getEvent());

        assertEquals(9, m_provisioner.getScheduleLength());
    }

    @Test
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
    public void testProvisionerNodeRescanSchedule() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", Boolean.TRUE.toString());
        getScanExecutor().pause();

        m_provisioner.scheduleRescanForExistingNodes();
        List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();

        assertEquals(10, schedulesForNode.size());
        m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(10, m_provisioner.getScheduleLength());
    }

    @Test(timeout=300000)
    public void testProvisionerUpdateScheduleAfterImport() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest", Boolean.TRUE.toString());
        getScanExecutor().pause();

        List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
        assertEquals(10, schedulesForNode.size());
        m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(10, m_provisioner.getScheduleLength());

        //reimport with one missing node
        getScanExecutor().resume();
        importFromResource("classpath:/tec_dump.xml.smalltest.delete", Boolean.TRUE.toString());
        getScanExecutor().pause();

        m_provisioner.scheduleRescanForExistingNodes();
        schedulesForNode = m_provisionService.getScheduleForNodes();

        m_provisioner.scheduleRescanForExistingNodes();

        //check the schedule to make sure that it deletes the node
        assertEquals(schedulesForNode.size(), m_provisioner.getScheduleLength());
        assertEquals(getNodeDao().countAll(), m_provisioner.getScheduleLength());

    }

    @Test(timeout=300000)
    public void testSaveCategoriesOnUpdateNodeAttributes() throws Exception {
        final EventAnticipator eventAnticipator = m_mockEventIpcManager.getEventAnticipator();

        final String TEST_CATEGORY = "TEST_CATEGORY";
        final String OLD_LABEL = "apknd";
        final String NEW_LABEL = "apknd-new";

        importFromResource("classpath:/tec_dump.xml.smalltest", Boolean.TRUE.toString());
        getScanExecutor().pause();

        m_provisioner.scheduleRescanForExistingNodes();

        final Collection<OnmsNode> nodes = m_nodeDao.findByLabel(OLD_LABEL);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());

        OnmsNode node = nodes.iterator().next();
        assertNotNull(node);

        OnmsNode nodeCopy = new OnmsNode(m_locationDao.getDefaultLocation(), OLD_LABEL);
        nodeCopy.setId(node.getId());
        nodeCopy.setLabelSource(NodeLabelSource.USER);

        assertNotSame(node, nodeCopy);
        assertEquals(OLD_LABEL, node.getLabel());
        assertFalse(node.hasCategory(TEST_CATEGORY));

        // Create a policy that will apply the category to the node
        final NodeCategorySettingPolicy policy = new NodeCategorySettingPolicy();
        policy.setCategory(TEST_CATEGORY);
        policy.setLabel(OLD_LABEL);

        // Apply the policy
        nodeCopy = policy.apply(nodeCopy);
        assertTrue(nodeCopy.getRequisitionedCategories().contains(TEST_CATEGORY));

        final NodeLabelChangedEventBuilder eb = new NodeLabelChangedEventBuilder("OnmsNode.mergeNodeAttributes");
        eb.setNodeid(node.getId());
        eb.setOldNodeLabel(OLD_LABEL);
        eb.setOldNodeLabelSource("U");
        eb.setNewNodeLabel(NEW_LABEL);
        eb.setNewNodeLabelSource("U");
        eventAnticipator.anticipateEvent(eb.getEvent());

        // Change the label of the node so that we can trigger a NODE_LABEL_CHANGED_EVENT_UEI event
        nodeCopy.setLabel(NEW_LABEL);
        nodeCopy.setLabelSource(NodeLabelSource.USER);

        assertFalse(node.getLabel().equals(nodeCopy.getLabel()));

        m_provisionService.updateNodeAttributes(nodeCopy);

        // Flush here to force a write so we are sure that the OnmsCategoryCollection are correctly created
        m_nodeDao.flush();

        // Query by the new node label
        final OnmsNode node2 = m_nodeDao.findByLabel(NEW_LABEL).iterator().next();
        assertTrue(node2.hasCategory(TEST_CATEGORY));

        eventAnticipator.resetUnanticipated();
        eventAnticipator.verifyAnticipated();
    }

    @Test(timeout=300000)
    public void testCreateUndiscoveredNode() throws Exception {
        m_provisionService.createUndiscoveredNode("127.0.0.1", "discovered", null);
    }

    /**
     * Test that the parent-foreign-source attribute in a requisition can add a parent to a
     * node that resides in a different provisioning group.
     *
     * @see http://issues.opennms.org/browse/NMS-4109
     */
    @Test(timeout=300000)
    public void testParentForeignSource() throws Exception {
        importFromResource("classpath:/parent_foreign_source_server.xml", Boolean.TRUE.toString());
        importFromResource("classpath:/parent_foreign_source_client.xml", Boolean.TRUE.toString());

        final List<OnmsNode> nodes = getNodeDao().findAll();
        assertEquals(2, nodes.size());
        OnmsNode node = getNodeDao().findByLabel("www").iterator().next();
        assertEquals("admin", node.getParent().getLabel());
        assertEquals("192.168.1.12", node.getPathElement().getIpAddress());
        assertEquals("ICMP", node.getPathElement().getServiceName());
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testImportWithNodeCategoryEvents() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        final MockNetwork network = new MockNetwork();
        final MockNode node = network.addNode(nextNodeId, "test");
        network.addInterface("172.16.1.1");
        network.addService("ICMP");
        anticipateCreationEvents(node);
        m_eventAnticipator.anticipateEvent(getNodeCategoryEvent(nextNodeId, "test"));
        m_eventAnticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_UPDATED_EVENT_UEI, "Test").setNodeid(nextNodeId).getEvent());

        // we should not get category update events on a re-import now, that happens during the scan phase
        //m_eventAnticipator.anticipateEvent(getNodeCategoryEvent(nextNodeId, "test"));
        importFromResource("classpath:/requisition_with_node_categories.xml", Boolean.TRUE.toString());
        importFromResource("classpath:/requisition_with_node_categories_changed.xml", Boolean.TRUE.toString());

        m_eventAnticipator.verifyAnticipated();
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase
    public void testImportWithGeoData() throws Exception {
        importFromResource("classpath:/tec_dump.xml", Boolean.TRUE.toString());
        final NodeDao nodeDao = getNodeDao();

        OnmsNode node = nodeDao.findByForeignId("empty", "4243");
        nodeDao.initialize(node.getAssetRecord());
        nodeDao.initialize(node.getAssetRecord().getGeolocation());

        OnmsGeolocation geolocation = new OnmsGeolocation();
        geolocation.setAddress1("220 Chatham Business Dr.");
        geolocation.setCity("Pittsboro");
        geolocation.setState("NC");
        geolocation.setZip("27312");
        geolocation.setLatitude(35.715723);
        geolocation.setLongitude(-79.162261);
        node.getAssetRecord().setGeolocation(geolocation);
        nodeDao.saveOrUpdate(node);
        nodeDao.flush();

        node = nodeDao.findByForeignId("empty", "4243");
        geolocation = node.getAssetRecord().getGeolocation();

        assertNotNull(geolocation.getLatitude());
        assertNotNull(geolocation.getLongitude());
        assertEquals(Float.valueOf(35.715723f).doubleValue(),  geolocation.getLatitude().doubleValue(),  0.1d);
        assertEquals(Float.valueOf(-79.162261f).doubleValue(), geolocation.getLongitude().doubleValue(), 0.1d);

        System.err.println("=================================================================BLEARGH");
        importFromResource("classpath:/tec_dump.xml", Boolean.TRUE.toString());
        node = nodeDao.findByForeignId("empty", "4243");
        geolocation = node.getAssetRecord().getGeolocation();

        // Ensure it is reset
        assertNull(geolocation.asAddressString());
        assertNull(geolocation.getLatitude());
        assertNull(geolocation.getLongitude());
    }

    @Test(timeout=300000)
    public void testRequisitionedCategoriesNoPolicies() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        final MockNetwork network = new MockNetwork();
        final MockNode node = network.addNode(nextNodeId, "test");
        network.addInterface("172.16.1.1");
        network.addService("ICMP");
        anticipateCreationEvents(node);
        m_eventAnticipator.anticipateEvent(getNodeCategoryEvent(nextNodeId, "test"));

        // we should not get new update events on a re-import now, that happens during the scan phase
        //m_eventAnticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_UPDATED_EVENT_UEI, "Test").setNodeid(nextNodeId).getEvent());
        //m_eventAnticipator.anticipateEvent(getNodeCategoryEvent(nextNodeId, "test"));
        importFromResource("classpath:/provisioner-testCategories-oneCategory.xml", Boolean.TRUE.toString());

        m_eventAnticipator.verifyAnticipated();
        assertEquals(0, m_eventAnticipator.getUnanticipatedEvents().size());
        m_eventAnticipator.reset();

        m_eventAnticipator.anticipateEvent(nodeScanCompleted(nextNodeId));
        m_eventAnticipator.setDiscardUnanticipated(true);

        runPendingScans();

        m_eventAnticipator.verifyAnticipated();
        m_eventAnticipator.reset();

        OnmsNode n = getNodeDao().get(nextNodeId);
        assertEquals(1, n.getCategories().size());
        assertEquals("TotallyMadeUpCategoryName", n.getCategories().iterator().next().getName());

        // import again, should be the same
        importFromResource("classpath:/provisioner-testCategories-oneCategory.xml", Boolean.TRUE.toString());
        n = getNodeDao().get(nextNodeId);
        assertEquals(1, n.getCategories().size());
        assertEquals("TotallyMadeUpCategoryName", n.getCategories().iterator().next().getName());

        runPendingScans();

        n = getNodeDao().get(nextNodeId);
        assertEquals(1, n.getCategories().size());
        assertEquals("TotallyMadeUpCategoryName", n.getCategories().iterator().next().getName());
    }

    @Test(timeout=300000)
    public void testRequisitionedCategoriesWithPolicies() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        final ForeignSource fs = m_foreignSourceRepository.getForeignSource("empty");
        final PluginConfig policy = new PluginConfig("addDumbCategory", NodeCategorySettingPolicy.class.getName());
        policy.addParameter("category", "Dumb");
        policy.addParameter("label", "test");
        fs.addPolicy(policy);
        m_foreignSourceRepository.save(fs);

        importFromResource("classpath:/provisioner-testCategories-oneCategory.xml", Boolean.TRUE.toString());

        // after import, we should have 1 category, because policies haven't been applied yet
        OnmsNode n = getNodeDao().get(nextNodeId);
        assertEquals(1, n.getCategories().size());
        assertEquals("TotallyMadeUpCategoryName", n.getCategories().iterator().next().getName());
        assertEquals(0, n.getRequisitionedCategories().size());

        runPendingScans();

        // when the scan has completed, both categories should have been applied
        n = getNodeDao().get(nextNodeId);

        assertEquals(2, n.getCategories().size());
        assertTrue(n.hasCategory("TotallyMadeUpCategoryName"));
        assertTrue(n.hasCategory("Dumb"));
    }

    @Test(timeout=300000)
    public void testRequisitionedCategoriesWithUserAddedCategory() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        importFromResource("classpath:/provisioner-testCategories-oneCategory.xml", Boolean.TRUE.toString());

        runPendingScans();

        // make sure we have the 1 category we expect
        OnmsNode n = getNodeDao().get(nextNodeId);
        assertEquals(1, n.getCategories().size());
        assertTrue(n.hasCategory("TotallyMadeUpCategoryName"));

        OnmsCategory cat = new OnmsCategory("ThisIsAlsoMadeUp");
        m_categoryDao.save(cat);
        m_categoryDao.flush();

        n.addCategory(m_categoryDao.findByName("ThisIsAlsoMadeUp"));
        getNodeDao().save(n);

        importFromResource("classpath:/provisioner-testCategories-oneCategory.xml", Boolean.TRUE.toString());

        runPendingScans();

        // when the scan has completed, both categories should have been applied
        n = getNodeDao().get(nextNodeId);

        assertEquals(2, n.getCategories().size());
        assertTrue(n.hasCategory("TotallyMadeUpCategoryName"));
        assertTrue(n.hasCategory("ThisIsAlsoMadeUp"));
    }

    @Test(timeout=300000)
    public void testRequisitionedCategoriesThenUpdateRequisitionToRemoveCategory() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        importFromResource("classpath:/provisioner-testCategories-oneCategory.xml", Boolean.TRUE.toString());

        runPendingScans();

        // make sure we have the 1 category we expect
        OnmsNode n = getNodeDao().get(nextNodeId);
        assertEquals(1, n.getCategories().size());
        assertTrue(n.hasCategory("TotallyMadeUpCategoryName"));

        importFromResource("classpath:/provisioner-testCategories-noCategories.xml", Boolean.TRUE.toString());

        runPendingScans();

        // when the scan has completed, the category should be removed
        n = getNodeDao().get(nextNodeId);

        assertEquals(0, n.getCategories().size());
    }

    @Test(timeout=300000)
    public void testRequisitionedCategoriesWithUserCategoryThenUpdateRequisitionToRemoveRequisitionedCategory() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        importFromResource("classpath:/provisioner-testCategories-oneCategory.xml", Boolean.TRUE.toString());
        runPendingScans();

        // make sure we have the 1 category we expect
        OnmsNode n = getNodeDao().get(nextNodeId);
        assertEquals(1, n.getCategories().size());
        assertTrue(n.hasCategory("TotallyMadeUpCategoryName"));

        OnmsCategory cat = new OnmsCategory("ThisIsAlsoMadeUp");
        m_categoryDao.save(cat);
        m_categoryDao.flush();

        n.addCategory(m_categoryDao.findByName("ThisIsAlsoMadeUp"));
        getNodeDao().save(n);

        importFromResource("classpath:/provisioner-testCategories-noCategories.xml", Boolean.TRUE.toString());
        runPendingScans();

        // when the scan has completed, the requisitioned category should be removed, but the user-added one should remain
        n = getNodeDao().get(nextNodeId);

        assertEquals(1, n.getCategories().size());
        assertTrue(n.hasCategory("ThisIsAlsoMadeUp"));
    }

    /**
     * Test for NMS-7636
     */
    @Test(timeout=300000)
    public void resourcesAreNotDeletedWhenNodeScanIsAborted() throws InterruptedException, ExecutionException, UnknownHostException {
        // Setup a node with a single interface and service
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("node1");

        builder.addInterface("192.168.0.1")
            .getInterface()
            // Pretend this was discovered an hour ago
            .setIpLastCapsdPoll(new Date(new Date().getTime() - 60*60*1000));

        builder.addService(new OnmsServiceType());

        OnmsNode node = builder.getCurrentNode();
        getNodeDao().save(node);

        // Preliminary check
        assertEquals(1, node.getIpInterfaces().size());

        // Issue a scan without setting up the requisition
        // Expect a nodeScanAborted event
        m_eventAnticipator.anticipateEvent(nodeScanAborted(node.getId()));
        m_eventAnticipator.setDiscardUnanticipated(true);

        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), "should_not_exist", "1", node.getLocation());
        runScan(scan);

        m_eventAnticipator.verifyAnticipated();
        m_eventAnticipator.reset();

        // The interface should remain
        assertEquals(1, node.getIpInterfaces().size());
    }

    private void testLocationChanges(String path1, String location1, String path2, String location2) throws Exception {
        importFromResource(path1, Boolean.TRUE.toString());
        List<OnmsNode> nodes1 = m_nodeDao.findAll();
        assertEquals(1, nodes1.size());
        assertNotNull(nodes1.get(0));
        assertNotNull(nodes1.get(0).getLocation());
        assertEquals(location1, nodes1.get(0).getLocation().getLocationName());

        importFromResource(path2, Boolean.TRUE.toString());
        List<OnmsNode> nodes2 = m_nodeDao.findAll();
        assertEquals(1, nodes2.size());
        assertNotNull(nodes2.get(0));
        assertNotNull(nodes2.get(0).getLocation());
        assertEquals(location2, nodes2.get(0).getLocation().getLocationName());
    }

    @Test(timeout = 300000)
    public void testLocationChangeFromFoobarToEmpty() throws Exception {
        testLocationChanges("classpath:/import_dummy-foobar.xml", "foobar", "classpath:/import_dummy-empty.xml", "Default");
        List<OnmsMonitoringLocation> locations = m_locationDao.findAll();
        assertEquals(2, locations.size());
    }

    @Test(timeout = 300000)
    public void testLocationChangeFromNullToFoobar() throws Exception {
        testLocationChanges("classpath:/import_dummy-null.xml", "Default", "classpath:/import_dummy-foobar.xml", "foobar");
        List<OnmsMonitoringLocation> locations = m_locationDao.findAll();
        assertEquals(2, locations.size());
    }

    @Test(timeout = 300000)
    public void testLocationChangeFromFoobarToNull() throws Exception {
        testLocationChanges("classpath:/import_dummy-foobar.xml", "foobar", "classpath:/import_dummy-null.xml", "Default");
        List<OnmsMonitoringLocation> locations = m_locationDao.findAll();
        assertEquals(2, locations.size());
    }

    @Test(timeout = 300000)
    public void testLocationChangeFromEmptyToFoobar() throws Exception {
        testLocationChanges("classpath:/import_dummy-empty.xml", "Default", "classpath:/import_dummy-foobar.xml", "foobar");
        List<OnmsMonitoringLocation> locations = m_locationDao.findAll();
        assertEquals(2, locations.size());
    }

    private Event nodeScanAborted(final int nodeId) {
        final EventBuilder eb = new EventBuilder(EventConstants.PROVISION_SCAN_ABORTED_UEI, "Test");
        eb.setNodeid(nodeId);
        final Event event = eb.getEvent();
        return event;
    }

    private Event nodeScanCompleted(final int nodeId) {
        final EventBuilder eb = new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Test");
        eb.setNodeid(nodeId);
        final Event event = eb.getEvent();
        return event;
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

    private static Event nodeUpdated(int nodeid) {
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_UPDATED_EVENT_UEI, "Test");
        bldr.setNodeid(nodeid);
        return bldr.getEvent();
    }

    private Event getNodeCategoryEvent(final int nodeId, final String nodeLabel) {
        return new EventBuilder(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI, "Test").setNodeid(nodeId).setParam(EventConstants.PARM_NODE_LABEL, nodeLabel).getEvent();
    }

    private OnmsNode createNode(final String foreignSource) {
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "default");
        //node.setId(nodeId);
        node.setLastCapsdPoll(new Date());
        node.setForeignSource(foreignSource);

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
        protected List<OnmsNodeRequisition> m_nodes = new ArrayList<>();
        protected List<OnmsIpInterfaceRequisition> m_ifaces = new ArrayList<>();

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
            return MoreObjects.toStringHelper(this)
            .add("modelImportCount", getModelImportCount())
            .add("modelImportCompletedCount", getModelImportCompletedCount())
            .add("nodeCount", getNodeCount())
            .add("nodeCompletedCount", getNodeCompletedCount())
            .add("nodeCategoryCount", getNodeCategoryCount())
            .add("nodeCategoryCompletedCount", getNodeCategoryCompletedCount())
            .add("interfaceCount", getInterfaceCount())
            .add("interfaceCompletedCount", getInterfaceCompletedCount())
            .add("monitoredServiceCount", getMonitoredServiceCount())
            .add("monitoredServiceCompletedCount", getMonitoredServiceCompletedCount())
            .add("serviceCategoryCount", getServiceCategoryCount())
            .add("serviceCategoryCompletedCount", getServiceCategoryCompletedCount())
            .add("assetCount", getAssetCount())
            .add("assetCompletedCount", getAssetCompletedCount())
            .toString();
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
