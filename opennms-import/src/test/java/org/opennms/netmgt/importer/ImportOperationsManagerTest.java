/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.importer.operations.ImportOperationsManager;
import org.opennms.netmgt.importer.specification.AbstractImportVisitor;
import org.opennms.netmgt.importer.specification.SpecFile;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ImportOperationsManagerTest implements InitializingBean {
    private MockSnmpAgent m_agent;

    @Autowired
    DatabasePopulator m_populator;
    @Autowired
    TransactionTemplate m_transTemplate;
    @Autowired
    DistPollerDao m_distPollerDao;
    @Autowired
    NodeDao m_nodeDao;
    @Autowired
    ServiceTypeDao m_serviceTypeDao;
    @Autowired
    CategoryDao m_categoryDao;
    @Autowired
    IpInterfaceDao m_ipInterfaceDao;
    @Autowired
    SnmpInterfaceDao m_snmpInterfaceDao;

    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_populator);
        assertNotNull(m_transTemplate);
        assertNotNull(m_distPollerDao);
        assertNotNull(m_nodeDao);
        assertNotNull(m_serviceTypeDao);
        assertNotNull(m_categoryDao);
        assertNotNull(m_ipInterfaceDao);
        assertNotNull(m_snmpInterfaceDao);
    }

    @Before
    public void onSetUpInTransactionIfEnabled() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.snmp4j.Snmp", "FATAL");
        p.setProperty("log4j.logger.org.opennms.netmgt.snmp.SnmpWalker", "FATAL");
        MockLogAppender.setupLogging(p);

        m_populator.populateDatabase();

        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties"), "127.0.0.1/1691");

        SnmpPeerFactory spf = new SnmpPeerFactory(new ByteArrayInputStream(("<?xml version=\"1.0\"?>\n" + 
                "<snmp-config port=\"1691\" retry=\"3\" timeout=\"800\"\n" + 
                "             read-community=\"public\" \n" + 
                "             version=\"v1\" \n" + 
                "             max-vars-per-pdu=\"10\" proxy-host=\"127.0.0.1\">\n" + 
                "\n" + 
                "</snmp-config>\n" + 
                "\n" + 
        "").getBytes()));
        SnmpPeerFactory.setInstance(spf);

        m_categoryDao.save(new OnmsCategory("AC"));
        m_categoryDao.save(new OnmsCategory("UK"));
        m_categoryDao.save(new OnmsCategory("low"));
        m_categoryDao.flush();
    }


    @After
    public void onTearDownInTransactionIfEnabled() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        m_agent.shutDownAndWait();
    }

    protected ModelImporter getModelImporter() {
        ModelImporter mi = new ModelImporter();
        mi.setDistPollerDao(m_distPollerDao);
        mi.setNodeDao(m_nodeDao); 
        mi.setServiceTypeDao(m_serviceTypeDao);
        mi.setCategoryDao(m_categoryDao);
        return mi; 
    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
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

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testSaveThenUpdate() throws Exception {


        m_transTemplate.execute(new TransactionCallback<OnmsNode>() {
            public OnmsNode doInTransaction(TransactionStatus status) {
                OnmsServiceType icmp = m_serviceTypeDao.findByName("ICMP");
                OnmsServiceType snmp = m_serviceTypeDao.findByName("SNMP");
                OnmsDistPoller distPoller = m_distPollerDao.get("localhost");
                NetworkBuilder builder = new NetworkBuilder(distPoller);
                builder.addNode("node7").setForeignSource("imported:").setForeignId("7");
                builder.getCurrentNode().getAssetRecord().setDisplayCategory("cat7");
                builder.addInterface("192.168.7.1").setIsManaged("M").setIsSnmpPrimary("P");
                builder.addService(icmp);
                builder.addService(snmp);
                m_nodeDao.save(builder.getCurrentNode());
                return builder.getCurrentNode();
            }
        });

        //m_distPollerDao.clear();

        m_transTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                OnmsNode node = m_nodeDao.findByForeignId("imported:", "7");
                assertNotNull(node);
                assertEquals("node7", node.getLabel());
                assertEquals("cat7", node.getAssetRecord().getDisplayCategory());
                assertEquals(1, node.getIpInterfaces().size());
                OnmsIpInterface iface = node.getIpInterfaces().iterator().next();
                assertEquals("192.168.7.1", iface.getIpAddressAsString());
                assertEquals("M", iface.getIsManaged());
                assertEquals(2, iface.getMonitoredServices().size());

                System.err.println("###################3 UPDATE ####################");
                m_nodeDao.update(node);
                return null;
            }
        });

    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testChangeIpAddr() throws Exception {
        testImportFromSpecFile(new ClassPathResource("/tec_dump.xml"), 1, 1);

        assertEquals(1, m_ipInterfaceDao.findByIpAddress("172.20.1.204").size());

        testImportFromSpecFile(new ClassPathResource("/tec_dumpIpAddrChanged.xml"), 1, 1);

        assertEquals("Failed to add new interface 172.20.1.202", 1, m_ipInterfaceDao.findByIpAddress("172.20.1.202").size());
        assertEquals("Failed to delete removed interface 172.20.1.204", 0, m_ipInterfaceDao.findByIpAddress("172.20.1.204").size());


    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testImportToOperationsMgr() throws Exception {
        testDoubleImport(new ClassPathResource("/tec_dump.xml"));

        Collection<OnmsIpInterface> c = m_ipInterfaceDao.findByIpAddress("172.20.1.201");
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

        m_transTemplate.execute(new TransactionCallback<Object>() {

            public Object doInTransaction(TransactionStatus status) {
                AbstractImportVisitor accountant = new ImportAccountant(opsMgr);
                specFile.visitImport(accountant);
                return null;
            }

        });

        opsMgr.persistOperations(m_transTemplate, m_nodeDao);

    }


    private Map<String, Integer> getAssetNumberMapInTransaction(final SpecFile specFile) {
        Map<String, Integer> assetNumbers = m_transTemplate.execute(new TransactionCallback<Map<String, Integer>>() {
            public Map<String, Integer> doInTransaction(TransactionStatus status) {
                return getAssetNumberMap(specFile.getForeignSource());
            }
        });
        return assetNumbers;
    }

    protected Map<String, Integer> getAssetNumberMap(String foreignSource) {
        return m_nodeDao.getForeignIdToNodeIdMap(foreignSource);
    }

    protected void expectServiceTypeCreate(String string) {
        // TODO Auto-generated method stub
    }
}
