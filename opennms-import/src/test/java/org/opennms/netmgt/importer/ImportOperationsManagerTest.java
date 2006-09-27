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
import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.AbstractDaoTestCase;
import org.opennms.netmgt.importer.operations.ImportOperationsManager;
import org.opennms.netmgt.importer.specification.AbstractImportVisitor;
import org.opennms.netmgt.importer.specification.SpecFile;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.snmp.mock.MockSnmpAgent;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;


//public class ImportOperationsManagerTest extends AbstractMockTestCase {
public class ImportOperationsManagerTest extends AbstractDaoTestCase {
    
    MockSnmpAgent m_agent;
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("opennms.home", "src/test/opennms-home");

        MockLogAppender.setupLogging();
        setRunTestsInTransaction(false);
        
        
        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties"), "127.0.0.1/1691");

        SnmpPeerFactory.init();

        super.setUp();
    }
    

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
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
        
    
//    private final class CountingInvocationHandler implements InvocationHandler {
//        HashMap counts = new HashMap();
//        
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            int currentCount = 0;
//            if (counts.get(method.getName()) != null)
//                currentCount = ((Integer)counts.get(method.getName())).intValue();
//            counts.put(method.getName(), new Integer(currentCount+1));
//            
//            return null;
//        }
//        
//        public int getCount(String methodName) {
//            if (counts.get(methodName) == null) return 0;
//            return ((Integer)counts.get(methodName)).intValue();
//        }
//    }

//    public void testGetAssetNumber() {
//        assertEquals("imported:"+"1", ImportOperationsManager.getAssetNumber("1"));
//        
//    }
//
//    public void testGetForeignId() {
//        assertEquals("1", ImportOperationsManager.getForeignId("imported:"+"1"));
//    }
    
//    class TestOperationFactory implements OperationFactory {
//    }
//    
    
    public void testGetOperations() {
        Map assetNumberMap = getAssetNumberMap("imported:");
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
                builder.addNode("node7").getAssetRecord().setAssetNumber("imported:"+"7");
                builder.getCurrentNode().getAssetRecord().setDisplayCategory("cat7");
                builder.addInterface("192.168.7.1").setIsManaged("M").setIsSnmpPrimary("P").setIpStatus(1);
                builder.addService(icmp);
                builder.addService(snmp);
                getNodeDao().save(builder.getCurrentNode());
                return builder.getCurrentNode();
            }
        });
        
        //getDistPollerDao().clear();

        m_transTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
            	OnmsNode node = getNodeDao().findByAssetNumber("imported:"+"7");
            	assertNotNull(node);
            	assertEquals("node7", node.getLabel());
            	assertEquals("cat7", node.getAssetRecord().getDisplayCategory());
            	assertEquals(1, node.getIpInterfaces().size());
            	OnmsIpInterface iface = (OnmsIpInterface)node.getIpInterfaces().iterator().next();
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
        
        testImportFromSpecFile(new ClassPathResource("/tec_dump.xml"), 1, 1);
        assertEquals(1, getIpInterfaceDao().findByIpAddress("172.20.1.204").size());
        
        testImportFromSpecFile(new ClassPathResource("/tec_dumpIpAddrChanged.xml"), 1, 1);
        
        assertEquals("Failed to add new interface 172.20.1.202", 1, getIpInterfaceDao().findByIpAddress("172.20.1.202").size());
        assertEquals("Failed to delete removed interface 172.20.1.204", 0, getIpInterfaceDao().findByIpAddress("172.20.1.204").size());
        

    }

    public void testImportToOperationsMgr() throws Exception {
        
        testDoubleImport(new ClassPathResource("/tec_dump.xml"));
        
        Collection c = getIpInterfaceDao().findByIpAddress("172.20.1.201");
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

        Map assetNumbers = (Map)m_transTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                return getAssetNumberMap(specFile.getForeignSource());
            }
        });
        
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
        
//        assertEquals(2, opsMgr.getDeleteCount());
//        assertEquals(2, opsMgr.getUpdateCount());
//        assertEquals(2, opsMgr.getInsertCount());
//        assertEquals(6, opsMgr.getOperationCount());
        
        opsMgr.persistOperations(m_transTemplate, getNodeDao());
        
    }
    
    

}
