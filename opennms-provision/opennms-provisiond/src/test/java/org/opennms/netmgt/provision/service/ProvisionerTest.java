//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 10: Use default configs. - dj@opennms.org
// 2007 Aug 25: Use AbstractTransactionalTemporaryDatabaseSpringContextTests
//              and new Spring context files. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.OnmsAssetRequisition;
import org.opennms.netmgt.provision.persist.OnmsIpInterfaceRequisition;
import org.opennms.netmgt.provision.persist.OnmsMonitoredServiceRequisition;
import org.opennms.netmgt.provision.persist.OnmsNodeCategoryRequisition;
import org.opennms.netmgt.provision.persist.OnmsNodeRequisition;
import org.opennms.netmgt.provision.persist.OnmsServiceCategoryRequisition;
import org.opennms.netmgt.provision.persist.RequisitionVisitor;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.policies.NodeCategorySettingPolicy;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.style.ToStringCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unit test for ModelImport application.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    JUnitSnmpAgentExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitTemporaryDatabase()
public class ProvisionerTest {
    
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
    
    private EventAnticipator m_eventAnticipator;

    private ForeignSourceRepository m_foreignSourceRepository;
    
    private ForeignSource m_foreignSource;
    
    @BeforeClass
    public static void setUpSnmpConfig() {
        SnmpPeerFactory.setFile(new File("src/test/proxy-snmp-config.xml"));
    }
    
    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

        MockLogAppender.setupLogging(props);
        
        //System.setProperty("mock.debug", "false");
        
        m_eventAnticipator = m_mockEventIpcManager.getEventAnticipator();
        
        m_provisioner.start();
        
        m_foreignSource = new ForeignSource();
        m_foreignSource.setName("imported:");
        m_foreignSource.setScanInterval(Duration.standardDays(1));
        
        m_foreignSourceRepository = new MockForeignSourceRepository();
        m_foreignSourceRepository.save(m_foreignSource);
        
        m_provisionService.setForeignSourceRepository(m_foreignSourceRepository);
        
        m_pausibleExecutor.pause();

    }


    @Test
    @Transactional
    public void testVisit() throws Exception {

        Requisition requisition = m_foreignSourceRepository.importRequisition(new ClassPathResource("/NewFile2.xml"));
        CountingVisitor visitor = new CountingVisitor();
        requisition.visit(visitor);
        verifyCounts(visitor);
    }

    @Test
    @Transactional
    public void testSendEventsOnImport() throws Exception {
        
        MockNetwork network = new MockNetwork();
        MockNode node = network.addNode(1, "node1");
        network.addInterface("172.20.1.204");
        network.addService("ICMP");
        network.addService("HTTP");
        network.addInterface("172.20.1.201");
        network.addService("ICMP");
        network.addService("SNMP");
        
        anticpateCreationEvents(node);
        
        importFromResource("classpath:/tec_dump.xml");
        
        m_eventAnticipator.verifyAnticipated();
        
    }

    private void importFromResource(String path) throws Exception {
        m_provisioner.importModelFromResource(m_resourceLoader.getResource(path));
    }
    
    private void anticpateCreationEvents(MockElement element) {
        element.visit(new MockVisitorAdapter() {

            @Override
            public void visitElement(MockElement e) {
                Event newEvent = e.createNewEvent();
                System.out.println("Anticipate Event "+newEvent.getUei());
                m_eventAnticipator.anticipateEvent(newEvent);
            }
            
        });
    }
    
    @Test
    @Transactional
    public void testFindQuery() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest");
        
        for (OnmsAssetRecord assetRecord : getAssetRecordDao().findAll()) {
            System.err.println(assetRecord.getBuilding());
        }
    }
    
    @Test
    @Transactional
    public void testBigImport() throws Exception {
        File file = new File("/tmp/tec_dump.xml.large");
        if (file.exists()) {
            m_eventAnticipator.reset();
            m_eventAnticipator.setDiscardUnanticipated(true);
            String path = file.toURI().toURL().toExternalForm();
            System.err.println("Importing: "+path);
            importFromResource(path);
        }
        
    }
    
    @Test
    @Transactional
    @JUnitSnmpAgent(host="127.0.0.1", port=9161, resource="classpath:snmpTestData1.properties")
    public void testPopulateWithSnmp() throws Exception {
        
        importFromResource("classpath:/tec_dump.xml");

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
    @Transactional
    @JUnitSnmpAgent(host="127.0.0.1", port=9161, resource="classpath:snmpTestData1.properties")
    public void testPopulateWithSnmpAndNodeScan() throws Exception {
        
        importFromResource("classpath:/requisition_then_scan.xml");

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
        
        
        List<OnmsNode> nodes = getNodeDao().findAll();
        OnmsNode node = nodes.get(0);

        NodeScan scan = m_provisioner.createNodeScan(node.getForeignSource(), node.getForeignId());
        
        scan.run();
        
        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(1, getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(2, getInterfaceDao().countAll());
        
        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 3, getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(2, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(6, getSnmpInterfaceDao().countAll());
        
        
        // Node Delete
        importFromResource("classpath:/nonodes.xml");

        //Verify node count
        assertEquals(0, getNodeDao().countAll());
        
        
    }

    
    
    
    @Test
    @Transactional
    public void testPopulate() throws Exception {
        
        importFromResource("classpath:/tec_dump.xml.smalltest");

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
    @Test
    @Transactional
    public void testImportUtf8() throws Exception {

        m_provisioner.importModelFromResource(new ClassPathResource("/utf-8.xml"));
        
        assertEquals(1, getNodeDao().countAll());
        // \u00f1 is unicode for n~ 
        assertEquals("\u00f1ode2", getNodeDao().get(1).getLabel());
        
    }
    
    /**
     * This test first bulk imports 10 nodes then runs update with 1 node missing
     * from the import file.
     * 
     * @throws ModelImportException
     */
    @Test
    @Transactional
    public void testDelete() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest");
        assertEquals(10, getNodeDao().countAll());
        importFromResource("classpath:/tec_dump.xml.smalltest.delete");
        assertEquals(9, getNodeDao().countAll());
    
        importFromResource("classpath:/tec_dump.xml.smalltest.nonodes");
        assertEquals(0, getNodeDao().countAll());
    }

    /**
     * This test makes sure that asset information is getting imported properly.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testAssets() throws Exception {
        importFromResource("classpath:/tec_dump.xml");
        assertEquals(1, getNodeDao().countAll());
        OnmsNode n = getNodeDao().get(1);
        assertEquals("Asset Record: Manufacturer",     "Dell",                   n.getAssetRecord().getManufacturer());
        assertEquals("Asset Record: Operating System", "Windows Pi",             n.getAssetRecord().getOperatingSystem());
        assertEquals("Asset Record: Description",      "Large and/or In Charge", n.getAssetRecord().getDescription());
    }
    
    //Scheduler tests
    @Test
    @Transactional
    public void testProvisionServiceGetScheduleForNodesCount() throws Exception {
       //importFromResource("classpath:/tec_dump.xml.smalltest");
       
       List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
       
       int nodeCount = getNodeDao().countAll();
       System.err.println("NodeCount: "+nodeCount);

       
       assertEquals(nodeCount, schedulesForNode.size());
       assertEquals(nodeCount, m_provisioner.getScheduleLength());
    }
    
    @Test
    @Transactional
    public void testProvisionServiceGetScheduleForNodesUponDelete() throws Exception {
       importFromResource("classpath:/tec_dump.xml.smalltest");
       
       List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
       
       assertEquals(10, schedulesForNode.size());
       
       importFromResource("classpath:/tec_dump.xml.smalltest.delete");
       
       schedulesForNode = m_provisionService.getScheduleForNodes();
       
       assertEquals(9, schedulesForNode.size());
       assertEquals(9, m_provisioner.getScheduleLength());
    }
    
    @Test
    @Transactional
    public void testProvisionerAddNodeToSchedule() throws Exception{
        
        
        m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(0, m_provisioner.getScheduleLength());
        
       
        
        OnmsNode node = createNode();
        assertEquals(1, node.getId().intValue());
        
        assertNotNull(m_nodeDao.get(1));
        
        Event addedEvent = new Event();
        addedEvent.setUei(EventConstants.NODE_ADDED_EVENT_UEI);
        addedEvent.setNodeid(1);
        
        m_mockEventIpcManager.broadcastNow(addedEvent);
        
        assertEquals(1, m_provisioner.getScheduleLength());
        
    }
    
    @Test
    @Transactional
    public void testProvisionerRescanWorking() throws Exception{
        importFromResource("classpath:/tec_dump.xml.smalltest");
        
        m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(10, m_provisioner.getScheduleLength());
    }

    @Test
    @Transactional
    public void testProvisionerRemoveNodeInSchedule() throws Exception{
        importFromResource("classpath:/tec_dump.xml.smalltest");
        
        //m_provisioner.scheduleRescanForExistingNodes();
        assertEquals(10, m_provisioner.getScheduleLength());
        
        Event addedEvent = new Event();
        addedEvent.setUei(EventConstants.NODE_DELETED_EVENT_UEI);
        addedEvent.setNodeid(2);
        
        m_mockEventIpcManager.broadcastNow(addedEvent);
        
        assertEquals(9, m_provisioner.getScheduleLength());
    }
    
    @Test
    @Transactional
    public void testProvisionServiceScanIntervalCalcWorks() {
        long now = System.currentTimeMillis();
        
        Date date = new Date();
        date.setTime(System.currentTimeMillis() - 43200000);
        long lastPoll = date.getTime();
        long nextPoll = lastPoll + 86400000;
        long initialDelay = Math.max(0, nextPoll - now);
        
        assertEquals(43200000, initialDelay);
        
    }
    
    @Test
    @Transactional
    public void testProvisionerNodeRescanSchedule() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest");
        
        List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
        
        assertEquals(10, schedulesForNode.size());
        
        //m_provisioner.scheduleRescanForExistingNodes();
        
        assertEquals(10, m_provisioner.getScheduleLength());
    }
    
    @Test
    @Transactional
    public void testProvisionerUpdateScheduleAfterImport() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest");
        
        List<NodeScanSchedule> schedulesForNode = m_provisionService.getScheduleForNodes();
        
        assertEquals(10, schedulesForNode.size());
        
        //m_provisioner.scheduleRescanForExistingNodes();
        
        assertEquals(10, m_provisioner.getScheduleLength());
        
        //reimport with one missing node
        importFromResource("classpath:/tec_dump.xml.smalltest.delete");
        
        //m_provisioner.scheduleRescanForExistingNodes();
        schedulesForNode = m_provisionService.getScheduleForNodes();
        
        //check the schedule to make sure that it deletes the node
        assertEquals(schedulesForNode.size(), m_provisioner.getScheduleLength());
        assertEquals(getNodeDao().countAll(), m_provisioner.getScheduleLength());
        
    }
    
    @Test
    @Transactional
    public void testSaveCategoriesOnUpdateNodeAttributes() throws Exception {
        
        final String TEST_CATEGORY = "TEST_CATEGORY";
        
        final String LABEL = "apknd";
        
        importFromResource("classpath:/tec_dump.xml.smalltest");
        
        Collection<OnmsNode> nodes = m_nodeDao.findByLabel(LABEL);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        
        OnmsNode node = nodes.iterator().next();
        assertNotNull(node);
        assertEquals(LABEL, node.getLabel());
        assertFalse(node.hasCategory(TEST_CATEGORY));
        
        NodeCategorySettingPolicy policy = new NodeCategorySettingPolicy();
        policy.setCategory(TEST_CATEGORY);
        policy.setLabel(LABEL);
        
        node = policy.apply(node);
        
        assertTrue(node.hasCategory(TEST_CATEGORY));
        
        m_provisionService.updateNodeAttributes(node);
        
        // flush here to force a write so we are sure that the OnmsCategories are correctly created
        m_nodeDao.flush();
        
        OnmsNode node2 = m_nodeDao.findByLabel(LABEL).iterator().next();

        assertTrue(node2.hasCategory(TEST_CATEGORY));
        

    }
    
    
    
    
    private OnmsNode createNode() {
        OnmsNode node = new OnmsNode();
        //node.setId(nodeId);
        node.setLastCapsdPoll(new Date());
        node.setForeignSource("imported:");
        
        m_nodeDao.save(node);
        m_nodeDao.flush();
        return node;
    }
    
    private void verifyCounts(CountingVisitor visitor) {
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
        private int m_modelImportCount;
        private int m_modelImportCompleted;
        private int m_nodeCount;
        private int m_nodeCompleted;
        private int m_nodeCategoryCount;
        private int m_nodeCategoryCompleted;
        private int m_ifaceCount;
        private int m_ifaceCompleted;
        private int m_svcCount;
        private int m_svcCompleted;
        private int m_svcCategoryCount;
        private int m_svcCategoryCompleted;
        private int m_assetCount;
        private int m_assetCompleted;
        
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
        
        public void visitModelImport(Requisition req) {
            m_modelImportCount++;
        }

        public void visitNode(OnmsNodeRequisition nodeReq) {
            m_nodeCount++;
            assertEquals("apknd", nodeReq.getNodeLabel());
            assertEquals("4243", nodeReq.getForeignId());
        }

        public void visitInterface(OnmsIpInterfaceRequisition ifaceReq) {
            m_ifaceCount++;
        }

        public void visitMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
            m_svcCount++;
        }

        public void visitNodeCategory(OnmsNodeCategoryRequisition catReq) {
            m_nodeCategoryCount++;
        }
        
        public void visitServiceCategory(OnmsServiceCategoryRequisition catReq) {
            m_svcCategoryCount++;
        }
        
        public void visitAsset(OnmsAssetRequisition assetReq) {
            m_assetCount++;
        }
        
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

        public void completeModelImport(Requisition req) {
            m_modelImportCompleted++;
        }

        public void completeNode(OnmsNodeRequisition nodeReq) {
            m_nodeCompleted++;
        }

        public void completeInterface(OnmsIpInterfaceRequisition ifaceReq) {
            m_ifaceCompleted++;
        }

        public void completeMonitoredService(OnmsMonitoredServiceRequisition monSvcReq) {
            m_svcCompleted++;
        }

        public void completeNodeCategory(OnmsNodeCategoryRequisition catReq) {
            m_nodeCategoryCompleted++;
        }
        
        public void completeServiceCategory(OnmsServiceCategoryRequisition catReq) {
            m_nodeCategoryCompleted++;
        }
        
        public void completeAsset(OnmsAssetRequisition assetReq) {
            m_assetCompleted++;
        }
        
    }
    

}
