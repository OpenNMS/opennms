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
// 2007 Aug 23: Use new RrdTestUtils.initializeNullStratgegy. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.JUnitHttpServerExecutionListener;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    JUnitCollectorExecutionListener.class,
    JUnitHttpServerExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitHttpServer(port=10342, vhosts={"127.0.0.1"})
public class HttpCollectorTest {

    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;
    
    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    
    private TestContext m_context;

    private final OnmsDistPoller m_distPoller = new OnmsDistPoller("localhost", "127.0.0.1");

    private final String m_testHostName = "127.0.0.1";

    private CollectionSpecification m_collectionSpecification;

    private CollectionAgent m_collectionAgent;

    public void setTestContext(TestContext t) {
        m_context = t;
    }

    private OnmsServiceType getServiceType(String name) {
        OnmsServiceType serviceType = m_serviceTypeDao.findByName(name);
        if (serviceType == null) {
            serviceType = new OnmsServiceType(name);
            m_serviceTypeDao.save(serviceType);
            m_serviceTypeDao.flush();
        }
        return serviceType;
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        assertNotNull(m_mockEventIpcManager);
        assertNotNull(m_ipInterfaceDao);
        assertNotNull(m_nodeDao);

        if (m_nodeDao.findByLabel("testnode").size() == 0) {
            NetworkBuilder builder = new NetworkBuilder(m_distPoller);
            builder.addNode("testnode");
            builder.addInterface(InetAddress.getByName(m_testHostName).getHostAddress()).setIsManaged("M").setIsSnmpPrimary("P");
            builder.addService(getServiceType("ICMP"));
            builder.addService(getServiceType("HTTP"));
            if (m_nodeDao == null) {
                throw new Exception("node DAO does not exist!");
            }
            OnmsNode n = builder.getCurrentNode();
            assertNotNull(n);
            m_nodeDao.save(n);
            m_nodeDao.flush();
        }

        HttpCollector collector = new HttpCollector();

        Collection<OnmsIpInterface> ifaces = m_ipInterfaceDao.findByIpAddress(m_testHostName);
        assertEquals(1, ifaces.size());
        OnmsIpInterface iface = ifaces.iterator().next();
        
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("collection", "default");
        collector.initialize(parameters);

        m_collectionSpecification = CollectorTestUtils.createCollectionSpec("HTTP", collector, "default");
        m_collectionAgent = DefaultCollectionAgent.create(iface.getId(), m_ipInterfaceDao, m_transactionManager);
    }

    /**
     * Test method for {@link org.opennms.netmgt.collectd.HttpCollector#collect(
     *   org.opennms.netmgt.collectd.CollectionAgent, org.opennms.netmgt.model.events.EventProxy, java.util.Map)}.
     */
    @Test
    @JUnitCollector(datacollectionConfig="/org/opennms/netmgt/config/http-datacollection-config.xml", datacollectionType="http",
                    anticipateRrds={ "1/documentCount", "1/greatAnswer", "1/someNumber" }, anticipateFiles={ "1/strings.properties" })
    public final void testCollect() throws Exception {
        m_collectionSpecification.initialize(m_collectionAgent);

        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_collectionSpecification, collectionSet);

        m_collectionSpecification.release(m_collectionAgent);
    }

    @Test
    @JUnitCollector(datacollectionConfig="/org/opennms/netmgt/config/http-datacollection-persist-test-config.xml", datacollectionType="http",
                    anticipateRrds={ "1/documentCount", "1/greatAnswer", "1/someNumber" }, anticipateFiles={ "1/strings.properties" })
    public final void testPersist() throws Exception {
        File snmpRrdDirectory = (File)m_context.getAttribute("rrdDirectory");
        FileAnticipator anticipator = (FileAnticipator)m_context.getAttribute("fileAnticipator");

        int numUpdates = 2;
        int stepSizeInSecs = 1;
        
        int stepSizeInMillis = stepSizeInSecs*1000;

        m_collectionSpecification.initialize(m_collectionAgent);
        
        CollectorTestUtils.collectNTimes(m_collectionSpecification, m_collectionAgent, numUpdates);
        
        // node level collection
        File nodeDir = CollectorTestUtils.anticipatePath(anticipator, snmpRrdDirectory, "1");
        File documentCountRrdFile = new File(nodeDir, CollectorTestUtils.rrd("documentCount"));
        File someNumberRrdFile = new File(nodeDir, CollectorTestUtils.rrd("someNumber"));
        File greatAnswerRrdFile = new File(nodeDir, CollectorTestUtils.rrd("greatAnswer"));

       // This is the value of documentCount from the first test page
        // documentCount = Gauge32: 5
        assertEquals("documentCount", Double.valueOf(5.0), RrdUtils.fetchLastValueInRange(documentCountRrdFile.getAbsolutePath(), "documentCount", stepSizeInMillis, stepSizeInMillis));

        // This is the value of documentType from the first test page
        // someNumber = Gauge32: 17
        assertEquals("documentType", Double.valueOf(17.0), RrdUtils.fetchLastValueInRange(someNumberRrdFile.getAbsolutePath(), "someNumber", stepSizeInMillis, stepSizeInMillis));

        // This is the value of greatAnswer from the second test page
        //someNumber = Gauge32: 42
        assertEquals("greatAnswer", Double.valueOf(42.0), RrdUtils.fetchLastValueInRange(greatAnswerRrdFile.getAbsolutePath(), "greatAnswer", stepSizeInMillis, stepSizeInMillis));
        
        m_collectionSpecification.release(m_collectionAgent);
    }

}
