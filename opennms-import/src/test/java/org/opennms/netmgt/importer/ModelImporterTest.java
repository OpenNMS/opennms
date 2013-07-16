/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.importer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.importer.config.Asset;
import org.opennms.netmgt.importer.config.Category;
import org.opennms.netmgt.importer.config.Interface;
import org.opennms.netmgt.importer.config.ModelImport;
import org.opennms.netmgt.importer.config.MonitoredService;
import org.opennms.netmgt.importer.config.Node;
import org.opennms.netmgt.importer.specification.ImportVisitor;
import org.opennms.netmgt.importer.specification.SpecFile;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.test.context.ContextConfiguration;

/**
 * Unit test for ModelImport application.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/modelImporterTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@JUnitSnmpAgents({
    @JUnitSnmpAgent(host="172.20.1.201", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="192.168.2.1", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.99.99.99", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.128.2.1", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.128.7.1", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.131.177.1", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.131.180.1", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.131.182.1", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.131.185.1", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.132.80.1", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.132.78.1", resource="classpath:/snmpTestData1.properties"),
    @JUnitSnmpAgent(host="10.136.160.1", resource="classpath:/snmpTestData1.properties")
})
public class ModelImporterTest implements InitializingBean {
    @Autowired
    private DatabasePopulator m_populator;
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    @Autowired
    private CategoryDao m_categoryDao;
    @Autowired
    private ModelImporter m_importer;
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
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
        
        public int getCategoryCount() {
            return m_categoryCount;
        }

        public int getCategoryCompletedCount() {
            return m_categoryCompleted;
        }

        private int getAssetCount() {
            return m_assetCount;
        }

        private int getAssetCompletedCount() {
            return m_assetCompleted;
        }

        @Override
        public void visitModelImport(ModelImport mi) {
            m_modelImportCount++;
        }

        @Override
        public void visitNode(Node node) {
            m_nodeCount++;
            assertEquals("apknd", node.getNodeLabel());
            assertEquals("4243", node.getForeignId());
        }

        @Override
        public void visitInterface(Interface iface) {
            m_ifaceCount++;
        }

        @Override
        public void visitMonitoredService(MonitoredService svc) {
            m_svcCount++;
        }

        @Override
        public void visitCategory(Category category) {
            m_categoryCount++;
        }
        
        @Override
        public void visitAsset(Asset asset) {
            m_assetCount++;
        }
        
        @Override
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
                .append("assetCount", getAssetCount())
                .append("assetCompletedCount", getAssetCompletedCount())
                .toString());
        }

        @Override
        public void completeModelImport(ModelImport modelImport) {
            m_modelImportCompleted++;
        }

        @Override
        public void completeNode(Node node) {
            m_nodeCompleted++;
        }

        @Override
        public void completeInterface(Interface iface) {
            m_ifaceCompleted++;
        }

        @Override
        public void completeMonitoredService(MonitoredService svc) {
            m_svcCompleted++;
        }

        @Override
        public void completeCategory(Category category) {
            m_categoryCompleted++;
        }
        
        @Override
        public void completeAsset(Asset asset) {
            m_assetCompleted++;
        }
        
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
    public void testFindQuery() throws Exception {
        ModelImporter mi = m_importer;        
        String specFile = "/tec_dump.xml.smalltest";
        mi.importModelFromResource(new ClassPathResource(specFile));
        for (OnmsAssetRecord assetRecord : m_importer.getAssetRecordDao().findAll()) {
            System.err.println(assetRecord.getAssetNumber());
        }
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testPopulate() throws Exception {
        createAndFlushServiceTypes();
        createAndFlushCategories();
        
        ModelImporter mi = m_importer;        
        String specFile = "/tec_dump.xml.smalltest";
        mi.importModelFromResource(new ClassPathResource(specFile));

        //Verify distpoller count
        assertEquals(1, mi.getDistPollerDao().countAll());
        
        //Verify node count
        assertEquals(10, mi.getNodeDao().countAll());
        
        //Verify ipinterface count
        assertEquals(30, mi.getIpInterfaceDao().countAll());
        
        //Verify ifservices count
        assertEquals(50, mi.getMonitoredServiceDao().countAll());
        
        //Verify service count
        assertEquals(3, mi.getServiceTypeDao().countAll());
    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testAddSnmpInterfaces() throws Exception {
        createAndFlushServiceTypes();
        createAndFlushCategories();

        ModelImporter mi = m_importer;
        String specFile = "/tec_dump.xml";
        mi.importModelFromResource(new ClassPathResource(specFile));

        assertEquals(1, mi.getIpInterfaceDao().findByIpAddress("172.20.1.204").size());

        assertEquals(2, mi.getIpInterfaceDao().countAll());

        assertEquals(6, m_snmpInterfaceDao.countAll());
    }


    
    /**
     * This test first bulk imports 10 nodes then runs update with 1 node missing
     * from the import file.
     * 
     * @throws ModelImportException
     */
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testImportUtf8() throws Exception {
        createAndFlushServiceTypes();
        createAndFlushCategories();
        
        //Initialize the database
        ModelImporter mi = m_importer;
        String specFile = "/utf-8.xml";
        mi.importModelFromResource(new ClassPathResource(specFile));
        
        assertEquals(1, mi.getNodeDao().countAll());
        // \u00f1 is unicode for n~ 
        assertEquals("\u00f1ode2", mi.getNodeDao().get(1).getLabel());
    }
    
    /**
     * This test first bulk imports 10 nodes then runs update with 1 node missing
     * from the import file.
     * 
     * @throws ModelImportException
     */
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testDelete() throws Exception {
        createAndFlushServiceTypes();
        createAndFlushCategories();
        
        //Initialize the database
        ModelImporter mi = m_importer;
        String specFile = "/tec_dump.xml.smalltest";
        mi.importModelFromResource(new ClassPathResource(specFile));
        
        assertEquals(10, mi.getNodeDao().countAll());
    }

    private static void verifyCounts(CountingVisitor visitor) {
        System.err.println(visitor);
        assertEquals(1, visitor.getModelImportCount());
        assertEquals(1, visitor.getNodeCount());
        assertEquals(3, visitor.getCategoryCount());
        assertEquals(4, visitor.getInterfaceCount());
        assertEquals(6, visitor.getMonitoredServiceCount());
        assertEquals(3, visitor.getAssetCount());
        assertEquals(visitor.getModelImportCount(), visitor.getModelImportCompletedCount());
        assertEquals(visitor.getNodeCount(), visitor.getNodeCompletedCount());
        assertEquals(visitor.getCategoryCount(), visitor.getCategoryCompletedCount());
        assertEquals(visitor.getInterfaceCount(), visitor.getInterfaceCompletedCount());
        assertEquals(visitor.getMonitoredServiceCount(), visitor.getMonitoredServiceCompletedCount());
        assertEquals(visitor.getAssetCount(), visitor.getAssetCompletedCount());
    }

    private void createAndFlushServiceTypes() {
        m_serviceTypeDao.save(new OnmsServiceType("ICMP"));
        m_serviceTypeDao.save(new OnmsServiceType("SNMP"));
        m_serviceTypeDao.save(new OnmsServiceType("HTTP"));
        m_serviceTypeDao.flush();
    }
    
    private void createAndFlushCategories() {
        m_categoryDao.save(new OnmsCategory("AC"));
        m_categoryDao.save(new OnmsCategory("AP"));
        m_categoryDao.save(new OnmsCategory("UK"));
        m_categoryDao.save(new OnmsCategory("BE"));
        m_categoryDao.save(new OnmsCategory("high"));
        m_categoryDao.save(new OnmsCategory("low"));
        m_categoryDao.save(new OnmsCategory("Park Plaza"));
        m_categoryDao.save(new OnmsCategory("Golden Tulip"));
        m_categoryDao.save(new OnmsCategory("Hilton"));
        m_categoryDao.save(new OnmsCategory("Scandic"));
        m_categoryDao.save(new OnmsCategory("Best Western"));
        m_categoryDao.flush();
    }
}
