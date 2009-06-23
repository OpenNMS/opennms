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
// 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.importer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.importer.operations.ImportOperationsManager;
import org.opennms.netmgt.importer.specification.AbstractImportVisitor;
import org.opennms.netmgt.importer.specification.SpecFile;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class ImportOperationsManagerTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private MockSnmpAgent m_agent;
    
    private DatabasePopulator m_populator;
    
    private TransactionTemplate m_transTemplate;
    private DistPollerDao m_distPollerDao;
    private NodeDao m_nodeDao;
    private ServiceTypeDao m_serviceTypeDao;
    private CategoryDao m_categoryDao;
    private IpInterfaceDao m_ipInterfaceDao;
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
                "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
        };
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.snmp4j.Snmp", "FATAL");
        p.setProperty("log4j.logger.org.opennms.netmgt.snmp.SnmpWalker", "FATAL");
        MockLogAppender.setupLogging(p);

        m_populator.populateDatabase();
        setComplete();
        endTransaction();
        startNewTransaction();
        
        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties"), "127.0.0.1/1691");

        SnmpPeerFactory spf = new SnmpPeerFactory(new StringReader("<?xml version=\"1.0\"?>\n" + 
        		"<snmp-config port=\"1691\" retry=\"3\" timeout=\"800\"\n" + 
        		"             read-community=\"public\" \n" + 
        		"             version=\"v1\" \n" + 
        		"             max-vars-per-pdu=\"10\" proxy-host=\"127.0.0.1\">\n" + 
        		"\n" + 
        		"</snmp-config>\n" + 
        		"\n" + 
        		""));
        SnmpPeerFactory.setInstance(spf);

        super.onSetUpInTransactionIfEnabled();
    }
    

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Override
    protected void onTearDownInTransactionIfEnabled() throws Exception {
        try {
            super.onTearDownInTransactionIfEnabled();
        } finally {
            m_agent.shutDownAndWait();
        }
    }
    
    protected ModelImporter getModelImporter() {
        ModelImporter mi = new ModelImporter();
        mi.setDistPollerDao(getDistPollerDao());
        mi.setNodeDao(getNodeDao()); 
        mi.setServiceTypeDao(getServiceTypeDao());
        mi.setCategoryDao(getCategoryDao());
        return mi; 
    }
        
    public void testGetOperations() {
        Map<String, Integer> assetNumberMap = getAssetNumberMap("imported:");
        ImportOperationsManager opsMgr = new ImportOperationsManager(assetNumberMap, getModelImporter());
        opsMgr.setForeignSource("imported:");
        opsMgr.foundNode("1", "node1", "myhouse", "durham");
        opsMgr.foundNode("3", "node3", "myhouse", "durham");
        opsMgr.foundNode("5", "node5", "theoffice", "pittsboro");
        opsMgr.foundNode("6", "node6", "theoffice", "pittsboro");
        assertEquals(2, opsMgr.getUpdateCount());
        assertEquals(2, opsMgr.getInsertCount());
        assertEquals(2, opsMgr.getDeleteCount());
        assertEquals(6, opsMgr.getOperationCount());
    }
    
    public void testSaveThenUpdate() throws Exception {
    	
    	
        m_transTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
            	OnmsServiceType icmp = getServiceTypeDao().findByName("ICMP");
            	OnmsServiceType snmp = getServiceTypeDao().findByName("SNMP");
                OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
                NetworkBuilder builder = new NetworkBuilder(distPoller);
                builder.addNode("node7").setForeignSource("imported:").setForeignId("7");
                builder.getCurrentNode().getAssetRecord().setDisplayCategory("cat7");
                builder.addInterface("192.168.7.1").setIsManaged("M").setIsSnmpPrimary("P");
                builder.addService(icmp);
                builder.addService(snmp);
                getNodeDao().save(builder.getCurrentNode());
                return builder.getCurrentNode();
            }
        });
        
        //getDistPollerDao().clear();

        m_transTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                OnmsNode node = getNodeDao().findByForeignId("imported:", "7");
            	assertNotNull(node);
            	assertEquals("node7", node.getLabel());
            	assertEquals("cat7", node.getAssetRecord().getDisplayCategory());
            	assertEquals(1, node.getIpInterfaces().size());
            	OnmsIpInterface iface = node.getIpInterfaces().iterator().next();
            	assertEquals("192.168.7.1", iface.getIpAddress());
            	assertEquals("M", iface.getIsManaged());
            	assertEquals(2, iface.getMonitoredServices().size());
            	
            	System.err.println("###################3 UPDATE ####################");
            	getNodeDao().update(node);
            	return null;
            }
        });

    }
    
    public void testChangeIpAddr() throws Exception {
        createAndFlushCategories();

        testImportFromSpecFile(new ClassPathResource("/tec_dump.xml"), 1, 1);
        
        assertEquals(1, getIpInterfaceDao().findByIpAddress("172.20.1.204").size());
        
        testImportFromSpecFile(new ClassPathResource("/tec_dumpIpAddrChanged.xml"), 1, 1);
        
        assertEquals("Failed to add new interface 172.20.1.202", 1, getIpInterfaceDao().findByIpAddress("172.20.1.202").size());
        assertEquals("Failed to delete removed interface 172.20.1.204", 0, getIpInterfaceDao().findByIpAddress("172.20.1.204").size());
        

    }

    private void createAndFlushCategories() {
        getCategoryDao().save(new OnmsCategory("AC"));
        getCategoryDao().save(new OnmsCategory("UK"));
        getCategoryDao().save(new OnmsCategory("low"));
        getCategoryDao().flush();

        setComplete();
        endTransaction();
        startNewTransaction();
    }

    public void testImportToOperationsMgr() throws Exception {
        createAndFlushCategories();
        
        testDoubleImport(new ClassPathResource("/tec_dump.xml"));
        
        Collection<OnmsIpInterface> c = getIpInterfaceDao().findByIpAddress("172.20.1.201");
        assertEquals(1, c.size());
        

    }


	private void testDoubleImport(Resource specFileResource) throws ModelImportException, IOException {
        
        long pass1 = System.currentTimeMillis();
		testImportFromSpecFile(specFileResource);
        
        System.err.println("##################################################################################");
        long pass2 = System.currentTimeMillis();
        testImportFromSpecFile(specFileResource);
        long end = System.currentTimeMillis();
        System.err.println("Pass1 Duration: "+(pass2-pass1)/1000+" s. Pass2 Duration: "+(end-pass2)/1000+" s.");
	}

    private void testImportFromSpecFile(Resource specFileResource) throws IOException, ModelImportException {
        testImportFromSpecFile(specFileResource, 4, 50);
    }
    
    private void testImportFromSpecFile(Resource specFileResource, int writeThreads, int scanThreads) throws IOException, ModelImportException {
        expectServiceTypeCreate("HTTP");
        final SpecFile specFile = new SpecFile();
        specFile.loadResource(specFileResource);

        Map<String, Integer> assetNumbers = getAssetNumberMapInTransaction(specFile);
        
        final ImportOperationsManager opsMgr = new ImportOperationsManager(assetNumbers, getModelImporter());
        opsMgr.setWriteThreads(writeThreads);
        opsMgr.setScanThreads(scanThreads);
        opsMgr.setForeignSource(specFile.getForeignSource());
        
        m_transTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                AbstractImportVisitor accountant = new ImportAccountant(opsMgr);
                specFile.visitImport(accountant);
                return null;
            }
            
        });
        
        opsMgr.persistOperations(m_transTemplate, getNodeDao());
        
    }


    @SuppressWarnings("unchecked")
    private Map<String, Integer> getAssetNumberMapInTransaction(final SpecFile specFile) {
        Map<String, Integer> assetNumbers = (Map<String, Integer>) m_transTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                return getAssetNumberMap(specFile.getForeignSource());
            }
        });
        return assetNumbers;
    }

    protected Map<String, Integer> getAssetNumberMap(String foreignSource) {
        return getNodeDao().getForeignIdToNodeIdMap(foreignSource);
    }

    protected void expectServiceTypeCreate(String string) {
        // TODO Auto-generated method stub
    }

    public TransactionTemplate getTransactionTemplate() {
        return m_transTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transTemplate) {
        m_transTemplate = transTemplate;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }
    
    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }

    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

    public DatabasePopulator getPopulator() {
        return m_populator;
    }

    public void setPopulator(DatabasePopulator populator) {
        m_populator = populator;
    }
}
