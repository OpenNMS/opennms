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

import java.io.File;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.provision.service.specification.ImportVisitor;
import org.opennms.netmgt.provision.service.specification.SpecFile;
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

/**
 * Unit test for ModelImport application.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/modelImporterTest.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})

public class BaseProvisionerTest {

    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;
    
    @Autowired
    private BaseProvisioner m_provisioner;
    
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DistPollerDao m_distPollerDao;
    
    @Autowired
    private AssetRecordDao m_assetRecordDao;
    
    @Autowired
    private ResourceLoader m_resourceLoader;
    
    private EventAnticipator m_eventAnticipator;
    
    
//    public void onSetUpInTransactionIfEnabled() throws Exception {
//        super.onSetUpInTransactionIfEnabled();
//        
//        initSnmpPeerFactory();
//    }
//
//    private void initSnmpPeerFactory() throws IOException, MarshalException, ValidationException {
//        Reader rdr = new StringReader("<?xml version=\"1.0\"?>\n" + 
//                "<snmp-config port=\"161\" retry=\"0\" timeout=\"2000\"\n" + 
//                "             read-community=\"public\" \n" + 
//                "                 version=\"v1\">\n" + 
//                "\n" + 
//                "</snmp-config>");
//        
//        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
//    }
    
    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

       // MockLogAppender.setupLogging(props);
        
        m_eventAnticipator = m_mockEventIpcManager.getEventAnticipator();
    }


    @Test
    public void testVisit() throws Exception {

        SpecFile specFile = new SpecFile();
        specFile.loadResource(new ClassPathResource("/NewFile2.xml"));
        CountingVisitor visitor = new CountingVisitor();
        specFile.visitImport(visitor);
        verifyCounts(visitor);
    }

    @Test
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
                m_eventAnticipator.anticipateEvent(e.createNewEvent());
            }
            
        });
    }
    
    @Test
    public void testFindQuery() throws Exception {
        importFromResource("classpath:/tec_dump.xml.smalltest");
        
        for (OnmsAssetRecord assetRecord : getAssetRecordDao().findAll()) {
            System.err.println(assetRecord.getBuilding());
        }
    }
    
    @Test
    public void testBigImport() throws Exception {
        File file = new File("/Users/brozow/tec_dump.xml.large");
        if (file.exists()) {
            m_eventAnticipator.reset();
            m_eventAnticipator.setDiscardUnanticipated(true);
            String path = file.toURI().toURL().toExternalForm();
            System.err.println("Importing: "+path);
            importFromResource(path);
        }
        
    }
    
    @Test
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
    public void testDelete() throws Exception {
        
        importFromResource("classpath:/tec_dump.xml.smalltest");
        
        assertEquals(10, getNodeDao().countAll());
        
        importFromResource("classpath:/tec_dump.xml.smalltest.delete");
        
        assertEquals(9, getNodeDao().countAll());
        

    }
    
    private void verifyCounts(CountingVisitor visitor) {
        //System.err.println(visitor);
        assertEquals(1, visitor.getModelImportCount());
        assertEquals(1, visitor.getNodeCount());
        assertEquals(3, visitor.getCategoryCount());
        assertEquals(4, visitor.getInterfaceCount());
        assertEquals(6, visitor.getMonitoredServiceCount());
        assertEquals(visitor.getModelImportCount(), visitor.getModelImportCompletedCount());
        assertEquals(visitor.getNodeCount(), visitor.getNodeCompletedCount());
        assertEquals(visitor.getCategoryCount(), visitor.getCategoryCompletedCount());
        assertEquals(visitor.getInterfaceCount(), visitor.getInterfaceCompletedCount());
        assertEquals(visitor.getMonitoredServiceCount(), visitor.getMonitoredServiceCompletedCount());
    }

    class CountingVisitor implements ImportVisitor {
        

        private int m_modelImportCount;
        private int m_modelImportCompleted;
        private int m_nodeCount;
        private int m_nodeCompleted;
        private int m_ifaceCount;
        private int m_ifaceCompleted;
        private int m_svcCount;
        private int m_svcCompleted;
        private int m_categoryCount;
        private int m_categoryCompleted;
        
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
        
        public int getCategoryCount() {
            return m_categoryCount;
        }

        public int getCategoryCompletedCount() {
            return m_categoryCompleted;
        }

        public void visitModelImport(ModelImport mi) {
            m_modelImportCount++;
        }

        public void visitNode(Node node) {
            m_nodeCount++;
            assertEquals("apknd", node.getNodeLabel());
            assertEquals("4243", node.getForeignId());
        }

        public void visitInterface(Interface iface) {
            m_ifaceCount++;
        }

        public void visitMonitoredService(MonitoredService svc) {
            m_svcCount++;
        }

        public void visitCategory(Category category) {
            m_categoryCount++;
        }
        
        public String toString() {
            return (new ToStringCreator(this)
                .append("modelImportCount", getModelImportCount())
                .append("modelImportCompletedCount", getModelImportCompletedCount())
                .append("nodeCount", getNodeCount())
                .append("nodeCompletedCount", getNodeCompletedCount())
                .append("interfaceCount", getInterfaceCount())
                .append("interfaceCompletedCount", getInterfaceCompletedCount())
                .append("monitoredServiceCount", getMonitoredServiceCount())
                .append("monitoredServiceCompletedCount", getMonitoredServiceCompletedCount())
                .append("categoryCount", getCategoryCount())
                .append("categoryCompletedCount", getCategoryCompletedCount())
                .toString());
        }

        public void completeModelImport(ModelImport modelImport) {
            m_modelImportCompleted++;
        }

        public void completeNode(Node node) {
            m_nodeCompleted++;
        }

        public void completeInterface(Interface iface) {
            m_ifaceCompleted++;
        }

        public void completeMonitoredService(MonitoredService svc) {
            m_svcCompleted++;
        }

        public void completeCategory(Category category) {
            m_categoryCompleted++;
        }
        
    }
    

}
