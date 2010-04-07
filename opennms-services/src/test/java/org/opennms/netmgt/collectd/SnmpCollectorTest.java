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
// 2008 Aug 29: collect() can now throw CollectionException. - dj@opennms.org
// 2008 May 13: Change expectation on IpInterfaceDao from get to load. - dj@opennms.org
// 2008 Feb 09: Eliminate warnings. - dj@opennms.org
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
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.mock.snmp.MockSnmpAgentAware;
import org.opennms.netmgt.config.SnmpPeerFactory;
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
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    JUnitCollectorExecutionListener.class,
    JUnitSnmpAgentExecutionListener.class,
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
public class SnmpCollectorTest implements MockSnmpAgentAware {

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
    
    private final static String TEST_NODE_LABEL = "TestNode"; 

    private CollectionSpecification m_collectionSpecification;

    private CollectionAgent m_collectionAgent;

    private MockSnmpAgent m_agent;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        assertNotNull(m_mockEventIpcManager);
        assertNotNull(m_transactionManager);
        assertNotNull(m_nodeDao);
        assertNotNull(m_ipInterfaceDao);
        assertNotNull(m_serviceTypeDao);
        
        OnmsIpInterface iface = null;
        OnmsNode testNode = null;
        Collection<OnmsNode> testNodes = m_nodeDao.findByLabel(TEST_NODE_LABEL);
        if (testNodes == null || testNodes.size() < 1) {
            NetworkBuilder builder = new NetworkBuilder();
            builder.addNode(TEST_NODE_LABEL).setId(1).setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");

            builder.addSnmpInterface(m_testHostName, 1).setIfName("lo0").setPhysAddr("00:11:22:33:44");
            builder.addSnmpInterface(m_testHostName, 2).setIfName("gif0").setPhysAddr("00:11:22:33:45").setIfType(55);
            builder.addSnmpInterface(m_testHostName, 3).setIfName("stf0").setPhysAddr("00:11:22:33:46").setIfType(57);

            // InterfaceBuilder ifBldr = builder.addInterface(m_testHostName).setId(27).setIsSnmpPrimary("P");
            InterfaceBuilder ifBldr = builder.addInterface(m_testHostName).setIsSnmpPrimary("P");
            ifBldr.addSnmpInterface(m_testHostName, 6).setIfName("fw0").setPhysAddr("44:33:22:11:00").setIfType(144).setCollectionEnabled(true);

            testNode = builder.getCurrentNode();
            assertNotNull(testNode);
            m_nodeDao.save(testNode);
            m_nodeDao.flush();
        } else {
            testNode = testNodes.iterator().next();
        }

        Set<OnmsIpInterface> ifaces = testNode.getIpInterfaces();
        assertEquals(1, ifaces.size());
        iface = ifaces.iterator().next();

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(new StringReader(
                "<?xml version=\"1.0\"?>\n"
                + "<snmp-config port=\"9161\" retry=\"3\" timeout=\"800000\" read-community=\"public\" version=\"v2c\">\n"
                + "</snmp-config>"        )));

        SnmpCollector collector = new SnmpCollector();
        collector.initialize(null);

        m_collectionSpecification = CollectorTestUtils.createCollectionSpec("SNMP", collector, "default");
        m_collectionAgent = DefaultCollectionAgent.create(iface.getId(), m_ipInterfaceDao, m_transactionManager);
    }
    
    @After
    public void tearDown() throws Exception {
        MockUtil.println("------------ End Test --------------------------");
        // MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    @JUnitCollector(
            datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config.xml", 
            datacollectionType = "snmp",
            anticipateFiles = {
                    "1",
                    "1/fw0"
            },
            anticipateRrds = {
                    "1/tcpActiveOpens",
                    "1/tcpAttemptFails",
                    "1/tcpPassiveOpens",
                    "1/tcpRetransSegs",
                    "1/tcpCurrEstab",
                    "1/tcpEstabResets",
                    "1/tcpInErrors",
                    "1/tcpInSegs",
                    "1/tcpOutRsts",
                    "1/tcpOutSegs",
                    "1/fw0/ifInDiscards",
                    "1/fw0/ifInErrors",
                    "1/fw0/ifInNUcastpkts",
                    "1/fw0/ifInOctets",
                    "1/fw0/ifInUcastpkts",
                    "1/fw0/ifOutErrors",
                    "1/fw0/ifOutNUcastPkts",
                    "1/fw0/ifOutOctets",
                    "1/fw0/ifOutUcastPkts"
            }
    )
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/snmpTestData1.properties")
    public void testCollect() throws Exception {
        // initializeAgent("/org/opennms/netmgt/snmp/bigrouter-walk.properties");
        
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");

        /*
        File nodeDir = anticipatePath(getSnmpRrdDirectory(), "1");
        anticipateRrdFiles(nodeDir, "tcpActiveOpens", "tcpAttemptFails");
        anticipateRrdFiles(nodeDir, "tcpPassiveOpens", "tcpRetransSegs");
        anticipateRrdFiles(nodeDir, "tcpCurrEstab", "tcpEstabResets");
        anticipateRrdFiles(nodeDir, "tcpInErrors", "tcpInSegs");
        anticipateRrdFiles(nodeDir, "tcpOutRsts", "tcpOutSegs");
        
        File ifDir = anticipatePath(nodeDir, "fw0");
        anticipateRrdFiles(ifDir, "ifInDiscards", "ifInErrors", "ifInNUcastpkts",
                "ifInOctets", "ifInUcastpkts", "ifOutErrors", "ifOutNUcastPkts",
                "ifOutOctets", "ifOutUcastPkts");
        */

        // don't forget to initialize the agent
        m_collectionSpecification.initialize(m_collectionAgent);

        // now do the actual collection
        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status",
                     ServiceCollector.COLLECTION_SUCCEEDED,
                     collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_collectionSpecification, collectionSet);

        System.err.println("FIRST COLLECTION FINISHED");
        
        //need a one second time elapse to update the RRD
        Thread.sleep(1000);

        // try collecting again
        assertEquals("collection status",
                ServiceCollector.COLLECTION_SUCCEEDED,
                m_collectionSpecification.collect(m_collectionAgent).getStatus());

        System.err.println("SECOND COLLECTION FINISHED");

        // release the agent
        m_collectionSpecification.release(m_collectionAgent);
    }
    
    @Test
    @JUnitCollector(
            datacollectionConfig = "/org/opennms/netmgt/config/datacollection-persistTest-config.xml", 
            datacollectionType = "snmp",
            anticipateFiles = {
                    "1",
                    "1/fw0"
            },
            anticipateRrds = {
                    "1/tcpCurrEstab",
                    "1/fw0/ifInOctets"
            }
    )
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/snmpTestData1.properties")
    public void testPersist() throws Exception {
        File snmpRrdDirectory = (File)m_context.getAttribute("rrdDirectory");
        
        // node level collection
        File nodeDir = new File(snmpRrdDirectory, "1");
        File rrdFile = new File(nodeDir, rrd("tcpCurrEstab"));
        
        // interface level collection
        File ifDir = new File(nodeDir, "fw0");
        File ifRrdFile = new File(ifDir, rrd("ifInOctets"));
        
        int numUpdates = 2;
        int stepSizeInSecs = 1;
        
        int stepSizeInMillis = stepSizeInSecs*1000;
        
        // don't forget to initialize the agent
        m_collectionSpecification.initialize(m_collectionAgent);
        
        CollectorTestUtils.collectNTimes(m_collectionSpecification, m_collectionAgent, numUpdates);
        
        // This is the value from snmpTestData1.properties
        //.1.3.6.1.2.1.6.9.0 = Gauge32: 123
        assertEquals(new Double(123.0), RrdUtils.fetchLastValueInRange(rrdFile.getAbsolutePath(), "tcpCurrEstab", stepSizeInMillis, stepSizeInMillis));
        
        // This is the value from snmpTestData1.properties
        // .1.3.6.1.2.1.2.2.1.10.6 = Counter32: 1234567
        assertEquals(new Double(1234567.0), RrdUtils.fetchLastValueInRange(ifRrdFile.getAbsolutePath(), "ifInOctets", stepSizeInMillis, stepSizeInMillis));
        
        // now update the data in the agent
        m_agent.updateIntValue(".1.3.6.1.2.1.6.9.0", 456);
        m_agent.updateCounter32Value(".1.3.6.1.2.1.2.2.1.10.6", 7654321);
        
        CollectorTestUtils.collectNTimes(m_collectionSpecification, m_collectionAgent, numUpdates);

        // by now the values should be the new values
        assertEquals(new Double(456.0), RrdUtils.fetchLastValueInRange(rrdFile.getAbsolutePath(), "tcpCurrEstab", stepSizeInMillis, stepSizeInMillis));
        assertEquals(new Double(7654321.0), RrdUtils.fetchLastValueInRange(ifRrdFile.getAbsolutePath(), "ifInOctets", stepSizeInMillis, stepSizeInMillis));

        // release the agent
        m_collectionSpecification.release(m_collectionAgent);
    }

    @Test
    @JUnitCollector(
            datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config.xml", 
            datacollectionType = "snmp",
            anticipateRrds = {
                    "test"
            }
    )
    public void testUsingFetch() throws Exception {
        File snmpDir = (File)m_context.getAttribute("rrdDirectory");

        int stepSize = 1;
        int numUpdates = 2;

        long start = System.currentTimeMillis();

        File rrdFile = new File(snmpDir, rrd("test"));
        
        RrdStrategy m_rrdStrategy = RrdUtils.getStrategy();
        
        RrdDataSource rrdDataSource = new RrdDataSource("testAttr", "GAUGE", stepSize*2, "U", "U");
        Object def = m_rrdStrategy.createDefinition("test", snmpDir.getAbsolutePath(), "test", stepSize, Collections.singletonList(rrdDataSource), Collections.singletonList("RRA:AVERAGE:0.5:1:100"));
        m_rrdStrategy.createFile(def);
                
        Object rrdFileObject = m_rrdStrategy.openFile(rrdFile.getAbsolutePath());
        for (int i = 0; i < numUpdates; i++) {
            m_rrdStrategy.updateFile(rrdFileObject, "test", (start/1000 - stepSize*(numUpdates-i)) + ":1");
        }
        m_rrdStrategy.closeFile(rrdFileObject);

        assertEquals(new Double(1.0), m_rrdStrategy.fetchLastValueInRange(rrdFile.getAbsolutePath(), "testAttr", stepSize*1000, stepSize*1000));
    }

    @Test
    @JUnitCollector(
            datacollectionConfig="/org/opennms/netmgt/config/datacollection-brocade-config.xml", 
            datacollectionType="snmp",
            anticipateRrds={ 
                    "1/brocadeFCPortIndex/1/swFCPortTxWords",
                    "1/brocadeFCPortIndex/1/swFCPortRxWords",
                    "1/brocadeFCPortIndex/2/swFCPortTxWords",
                    "1/brocadeFCPortIndex/2/swFCPortRxWords",
                    "1/brocadeFCPortIndex/3/swFCPortTxWords",
                    "1/brocadeFCPortIndex/3/swFCPortRxWords",
                    "1/brocadeFCPortIndex/4/swFCPortTxWords",
                    "1/brocadeFCPortIndex/4/swFCPortRxWords",
                    "1/brocadeFCPortIndex/5/swFCPortTxWords",
                    "1/brocadeFCPortIndex/5/swFCPortRxWords",
                    "1/brocadeFCPortIndex/6/swFCPortTxWords",
                    "1/brocadeFCPortIndex/6/swFCPortRxWords",
                    "1/brocadeFCPortIndex/7/swFCPortTxWords",
                    "1/brocadeFCPortIndex/7/swFCPortRxWords",
                    "1/brocadeFCPortIndex/8/swFCPortTxWords",
                    "1/brocadeFCPortIndex/8/swFCPortRxWords"
            }, 
            anticipateFiles={ 
                    "1",
                    "1/brocadeFCPortIndex",
                    "1/brocadeFCPortIndex/1/strings.properties",
                    "1/brocadeFCPortIndex/1",
                    "1/brocadeFCPortIndex/2/strings.properties",
                    "1/brocadeFCPortIndex/2",
                    "1/brocadeFCPortIndex/3/strings.properties",
                    "1/brocadeFCPortIndex/3",
                    "1/brocadeFCPortIndex/4/strings.properties",
                    "1/brocadeFCPortIndex/4",
                    "1/brocadeFCPortIndex/5/strings.properties",
                    "1/brocadeFCPortIndex/5",
                    "1/brocadeFCPortIndex/6/strings.properties",
                    "1/brocadeFCPortIndex/6",
                    "1/brocadeFCPortIndex/7/strings.properties",
                    "1/brocadeFCPortIndex/7",
                    "1/brocadeFCPortIndex/8/strings.properties",
                    "1/brocadeFCPortIndex/8"
            }
    )
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/brocadeTestData1.properties")
    public void testBrocadeCollect() throws Exception {
        m_collectionSpecification.initialize(m_collectionAgent);

        // now do the actual collection
        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status",
                ServiceCollector.COLLECTION_SUCCEEDED,
                collectionSet.getStatus());
        
        CollectorTestUtils.persistCollectionSet(m_collectionSpecification, collectionSet);
        
        
        System.err.println("FIRST COLLECTION FINISHED");
        
        //need a one second time elapse to update the RRD
        Thread.sleep(1000);

        // try collecting again
        assertEquals("collection status",
                ServiceCollector.COLLECTION_SUCCEEDED,
                m_collectionSpecification.collect(m_collectionAgent).getStatus());

        System.err.println("SECOND COLLECTION FINISHED");

        // release the agent
        m_collectionSpecification.release(m_collectionAgent);
    }
    
    @Test
    @JUnitCollector(
            datacollectionConfig = "/org/opennms/netmgt/config/datacollection-brocade-no-ifaces-config.xml", 
            datacollectionType = "snmp",
            anticipateRrds={ 
                    "1/brocadeFCPortIndex/1/swFCPortTxWords",
                    "1/brocadeFCPortIndex/1/swFCPortRxWords",
                    "1/brocadeFCPortIndex/2/swFCPortTxWords",
                    "1/brocadeFCPortIndex/2/swFCPortRxWords",
                    "1/brocadeFCPortIndex/3/swFCPortTxWords",
                    "1/brocadeFCPortIndex/3/swFCPortRxWords",
                    "1/brocadeFCPortIndex/4/swFCPortTxWords",
                    "1/brocadeFCPortIndex/4/swFCPortRxWords",
                    "1/brocadeFCPortIndex/5/swFCPortTxWords",
                    "1/brocadeFCPortIndex/5/swFCPortRxWords",
                    "1/brocadeFCPortIndex/6/swFCPortTxWords",
                    "1/brocadeFCPortIndex/6/swFCPortRxWords",
                    "1/brocadeFCPortIndex/7/swFCPortTxWords",
                    "1/brocadeFCPortIndex/7/swFCPortRxWords",
                    "1/brocadeFCPortIndex/8/swFCPortTxWords",
                    "1/brocadeFCPortIndex/8/swFCPortRxWords"
            }, 
            anticipateFiles={ 
                    "1",
                    "1/brocadeFCPortIndex",
                    "1/brocadeFCPortIndex/1/strings.properties",
                    "1/brocadeFCPortIndex/1",
                    "1/brocadeFCPortIndex/2/strings.properties",
                    "1/brocadeFCPortIndex/2",
                    "1/brocadeFCPortIndex/3/strings.properties",
                    "1/brocadeFCPortIndex/3",
                    "1/brocadeFCPortIndex/4/strings.properties",
                    "1/brocadeFCPortIndex/4",
                    "1/brocadeFCPortIndex/5/strings.properties",
                    "1/brocadeFCPortIndex/5",
                    "1/brocadeFCPortIndex/6/strings.properties",
                    "1/brocadeFCPortIndex/6",
                    "1/brocadeFCPortIndex/7/strings.properties",
                    "1/brocadeFCPortIndex/7",
                    "1/brocadeFCPortIndex/8/strings.properties",
                    "1/brocadeFCPortIndex/8"
            }
    )
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/brocadeTestData1.properties")
    public void testBug2447_GenericIndexedOnlyCollect() throws Exception {
        // don't forget to initialize the agent
        m_collectionSpecification.initialize(m_collectionAgent);

        // now do the actual collection
        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status",
                ServiceCollector.COLLECTION_SUCCEEDED,
                collectionSet.getStatus());
        
        CollectorTestUtils.persistCollectionSet(m_collectionSpecification, collectionSet);
        
        
        System.err.println("FIRST COLLECTION FINISHED");
        
        //need a one second time elapse to update the RRD
        Thread.sleep(1000);

        // try collecting again
        assertEquals("collection status",
                ServiceCollector.COLLECTION_SUCCEEDED,
                m_collectionSpecification.collect(m_collectionAgent).getStatus());

        System.err.println("SECOND COLLECTION FINISHED");

        // release the agent
        m_collectionSpecification.release(m_collectionAgent);
    }

    private static String rrd(String file) {
        return file + RrdUtils.getExtension();
    }

    public void setMockSnmpAgent(MockSnmpAgent agent) {
        m_agent = agent;
    }

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

}
