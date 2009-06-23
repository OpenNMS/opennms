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
package org.opennms.netmgt.importer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.modelimport.Asset;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.importer.specification.ImportVisitor;
import org.opennms.netmgt.importer.specification.SpecFile;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.style.ToStringCreator;

/**
 * Unit test for ModelImport application.
 */
public class ModelImporterTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private DatabasePopulator m_populator;
    private ServiceTypeDao m_serviceTypeDao;
    private CategoryDao m_categoryDao;

    private ModelImporter m_importer;
    private SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Override
    protected void setUpConfiguration() {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
                "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
                "classpath:/modelImporterTest.xml"
        };
    }

    public void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();
        
        initSnmpPeerFactory();
    }

    @SuppressWarnings("deprecation")
    private void initSnmpPeerFactory() throws IOException, MarshalException, ValidationException {
        Reader rdr = new StringReader("<?xml version=\"1.0\"?>" +
        		"<snmp-config port=\"9161\" retry=\"3\" timeout=\"800\" " +
        			"read-community=\"public\" " +
        			"version=\"v1\" " +
        			"max-vars-per-pdu=\"10\" proxy-host=\"127.0.0.1\">" +
        		"</snmp-config>");
        
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
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
        
        public void visitAsset(Asset asset) {
            m_assetCount++;
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
                .append("assetCount", getAssetCount())
                .append("assetCompletedCount", getAssetCompletedCount())
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
        
        public void completeAsset(Asset asset) {
            m_assetCompleted++;
        }
        
    }
    
    public void testVisit() throws Exception {

        SpecFile specFile = new SpecFile();
        specFile.loadResource(new ClassPathResource("/NewFile2.xml"));
        CountingVisitor visitor = new CountingVisitor();
        specFile.visitImport(visitor);
        verifyCounts(visitor);
    }
    
    public void testFindQuery() throws Exception {
        ModelImporter mi = m_importer;        
        String specFile = "/tec_dump.xml.smalltest";
        mi.importModelFromResource(new ClassPathResource(specFile));
        for (OnmsAssetRecord assetRecord : m_importer.getAssetRecordDao().findAll()) {
            System.err.println(assetRecord.getAssetNumber());
        }
    }
    
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
    
    public void testAddSnmpInterfaces() throws Exception {
        
        ClassPathResource agentConfig = new ClassPathResource("/snmpTestData1.properties");
        
        MockSnmpAgent agent = MockSnmpAgent.createAgentAndRun(agentConfig, "127.0.0.1/9161");

        try {
            createAndFlushServiceTypes();
            createAndFlushCategories();

            ModelImporter mi = m_importer;        
            String specFile = "/tec_dump.xml";
            mi.importModelFromResource(new ClassPathResource(specFile));

            assertEquals(1, mi.getIpInterfaceDao().findByIpAddress("172.20.1.204").size());

            assertEquals(2, mi.getIpInterfaceDao().countAll());

            assertEquals(6, getSnmpInterfaceDao().countAll());

        } finally {
            agent.shutDownAndWait();
        }

    }


    
    /**
     * This test first bulk imports 10 nodes then runs update with 1 node missing
     * from the import file.
     * 
     * @throws ModelImportException
     */
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
    public void testDelete() throws Exception {
        createAndFlushServiceTypes();
        createAndFlushCategories();
        
        //Initialize the database
        ModelImporter mi = m_importer;
        String specFile = "/tec_dump.xml.smalltest";
        mi.importModelFromResource(new ClassPathResource(specFile));
        
        assertEquals(10, mi.getNodeDao().countAll());
    }
    private void verifyCounts(CountingVisitor visitor) {
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
        getServiceTypeDao().save(new OnmsServiceType("ICMP"));
        getServiceTypeDao().save(new OnmsServiceType("SNMP"));
        getServiceTypeDao().save(new OnmsServiceType("HTTP"));
        getServiceTypeDao().flush();

        setComplete();
        endTransaction();
        startNewTransaction();
    }
    
    private void createAndFlushCategories() {
        getCategoryDao().save(new OnmsCategory("AC"));
        getCategoryDao().save(new OnmsCategory("AP"));
        getCategoryDao().save(new OnmsCategory("UK"));
        getCategoryDao().save(new OnmsCategory("BE"));
        getCategoryDao().save(new OnmsCategory("high"));
        getCategoryDao().save(new OnmsCategory("low"));
        getCategoryDao().save(new OnmsCategory("Park Plaza"));
        getCategoryDao().save(new OnmsCategory("Golden Tulip"));
        getCategoryDao().save(new OnmsCategory("Hilton"));
        getCategoryDao().save(new OnmsCategory("Scandic"));
        getCategoryDao().save(new OnmsCategory("Best Western"));
        getCategoryDao().flush();

        setComplete();
        endTransaction();
        startNewTransaction();
    }

    public ModelImporter getImporter() {
        return m_importer;
    }


    public void setImporter(ModelImporter importer) {
        m_importer = importer;
    }

    public DatabasePopulator getPopulator() {
        return m_populator;
    }

    public void setPopulator(DatabasePopulator populator) {
        m_populator = populator;
    }

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }

    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }
    
    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao; 
    }
    
    
    
    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }
    

}
