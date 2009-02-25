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

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;

import junit.framework.TestCase;

import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ClassPathResource;

public class SnmpCollectorTest extends AbstractCollectorTest {
    private SnmpCollector m_snmpCollector;

    private MockSnmpAgent m_agent;

    final String SNMP_CONFIG = "<?xml version=\"1.0\"?>\n"
        + "<snmp-config port=\"1691\" retry=\"3\" timeout=\"800000\"\n"
        + "               read-community=\"public\"\n"
        + "               version=\"v2c\">\n" + "</snmp-config>\n";




    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");
        MockLogAppender.setupLogging();

        MockNetwork m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("127.0.0.1");
        m_network.addService("ICMP");

        MockDatabase m_db = new MockDatabase();
        m_db.populate(m_network);

        DataSourceFactory.setInstance(m_db);
        
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        
        EventIpcManagerFactory.setIpcManager(eventIpcManager);
        
        //RrdConfig.loadProperties(new ByteArrayInputStream(s_rrdConfig.getBytes()));
        RrdTestUtils.initialize();

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(new StringReader(SNMP_CONFIG)));
        
        initializeDatabaseSchemaConfig("/org/opennms/netmgt/config/test-database-schema.xml");
        
        setTransMgr();
        setFileAnticipator();
    }
    
    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
        m_fileAnticipator.deleteExpected();
    }

    @Override
    protected void tearDown() throws Exception {
        if (m_agent != null) {
            m_agent.shutDownAndWait();
        }
        m_fileAnticipator.deleteExpected(true);
        m_fileAnticipator.tearDown();
        MockUtil.println("------------ End Test " + getName() + " --------------------------");
        super.tearDown();
    }

    public void testCollect() throws Exception {
//        initializeAgent("/org/opennms/netmgt/snmp/bigrouter-walk.properties");
        initializeAgent("/org/opennms/netmgt/snmp/snmpTestData1.properties");

        initializeDataCollectionConfig("/org/opennms/netmgt/config/datacollection-config.xml");

        createSnmpCollector();

        OnmsIpInterface iface = createInterface();

        CollectionSpecification spec = createCollectionSpec("SNMP", m_snmpCollector, "default");

        CollectionAgent agent = createCollectionAgent(iface);
        
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
        
        // don't for get to initialize the agent
        spec.initialize(agent);

        // now do the actual collection
        CollectionSet collectionSet=spec.collect(agent);
        assertEquals("collection status",
                     ServiceCollector.COLLECTION_SUCCEEDED,
                     collectionSet.getStatus());
        persistCollectionSet(spec, collectionSet);

        System.err.println("FIRST COLLECTION FINISHED");
        
        //need a one second time elapse to update the RRD
        Thread.sleep(1000);

        // try collecting again
        assertEquals("collection status",
                ServiceCollector.COLLECTION_SUCCEEDED,
                spec.collect(agent).getStatus());

        System.err.println("SECOND COLLECTION FINISHED");

        // release the agent
        spec.release(agent);
        
        // Wait for any RRD writes to finish up
        Thread.sleep(1000);
    }
    
    public void testPersist() throws Exception {
        initializeAgent("/org/opennms/netmgt/snmp/snmpTestData1.properties");

        initializeDataCollectionConfig("/org/opennms/netmgt/config/datacollection-persistTest-config.xml");

        createSnmpCollector();

        OnmsIpInterface iface = createInterface();

        CollectionSpecification spec = createCollectionSpec("SNMP", m_snmpCollector, "default");

        CollectionAgent agent = createCollectionAgent(iface);
        
        // node level collection
        File nodeDir = anticipatePath(getSnmpRrdDirectory(), "1");
        anticipateRrdFiles(nodeDir, "tcpCurrEstab");
        
        // interface level collection
        File ifDir = anticipatePath(nodeDir, "fw0");
        anticipateRrdFiles(ifDir, "ifInOctets");
        
        File rrdFile = new File(nodeDir, rrd("tcpCurrEstab"));
        File ifRrdFile = new File(ifDir, rrd("ifInOctets"));
       
        
        int numUpdates = 2;
        int stepSizeInSecs = 1;
        
        int stepSizeInMillis = stepSizeInSecs*1000;
        
        // don't for get to initialize the agent
        spec.initialize(agent);
        
        collectNTimes(spec, agent, numUpdates);
        
        // This is the value from snmpTestData1.properties
        //.1.3.6.1.2.1.6.9.0 = Gauge32: 123
        assertEquals(123.0, RrdUtils.fetchLastValueInRange(rrdFile.getAbsolutePath(), "tcpCurrEstab", stepSizeInMillis, stepSizeInMillis));
        
        // This is the value from snmpTestData1.properties
        // .1.3.6.1.2.1.2.2.1.10.6 = Counter32: 1234567
        assertEquals(1234567.0, RrdUtils.fetchLastValueInRange(ifRrdFile.getAbsolutePath(), "ifInOctets", stepSizeInMillis, stepSizeInMillis));
        
        // now update the data in the agent
        m_agent.updateIntValue(".1.3.6.1.2.1.6.9.0", 456);
        m_agent.updateCounter32Value(".1.3.6.1.2.1.2.2.1.10.6", 7654321);
        
        collectNTimes(spec, agent, numUpdates);

        // by now the values should be the new values
        assertEquals(456.0, RrdUtils.fetchLastValueInRange(rrdFile.getAbsolutePath(), "tcpCurrEstab", stepSizeInMillis, stepSizeInMillis));
        assertEquals(7654321.0, RrdUtils.fetchLastValueInRange(ifRrdFile.getAbsolutePath(), "ifInOctets", stepSizeInMillis, stepSizeInMillis));
        

        // release the agent
        spec.release(agent);
        
        // Wait for any RRD writes to finish up
        Thread.sleep(1000);
        
    }

    public void testUsingFetch() throws Exception {

        int stepSize = 1;
        int numUpdates = 2;

        long start = System.currentTimeMillis();

        File snmpDir = getSnmpRrdDirectory();
        anticipateRrdFiles(snmpDir, "test");
        
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
        

        assertEquals(1.0, m_rrdStrategy.fetchLastValueInRange(rrdFile.getAbsolutePath(), "testAttr", stepSize*1000, stepSize*1000));
       
    }

    public void testBrocadeCollect() throws Exception {
        initializeAgent("/org/opennms/netmgt/snmp/brocadeTestData1.properties");

        initializeDataCollectionConfig("/org/opennms/netmgt/config/datacollection-brocade-config.xml");

        createSnmpCollector();

        OnmsIpInterface iface = createInterface();

        CollectionSpecification spec = createCollectionSpec("SNMP", m_snmpCollector, "default");

        CollectionAgent agent = createCollectionAgent(iface);

        File brocadeDir = anticipatePath(getSnmpRrdDirectory(), "1", "brocadeFCPortIndex"); 
        for (int i = 1; i <= 8; i++) {
            File brocadeIndexDir = anticipatePath(brocadeDir, Integer.toString(i));
            anticipateFiles(brocadeIndexDir, "strings.properties");
            anticipateRrdFiles(brocadeIndexDir, "swFCPortTxWords", "swFCPortRxWords");
        }

        // don't for get to initialize the agent
        spec.initialize(agent);

        // now do the actual collection
        CollectionSet collectionSet=spec.collect(agent);
        assertEquals("collection status",
                ServiceCollector.COLLECTION_SUCCEEDED,
                collectionSet.getStatus());
        
        persistCollectionSet(spec, collectionSet);
        
        
        System.err.println("FIRST COLLECTION FINISHED");
        
        //need a one second time elapse to update the RRD
        Thread.sleep(1000);

        // try collecting again
        assertEquals("collection status",
                ServiceCollector.COLLECTION_SUCCEEDED,
                spec.collect(agent).getStatus());

        System.err.println("SECOND COLLECTION FINISHED");

        // release the agent
        spec.release(agent);
        
        // Wait for any RRD writes to finish up
        Thread.sleep(1000);
        
    }

    private void createSnmpCollector() {
        m_snmpCollector = new SnmpCollector();
        m_snmpCollector.initialize(null); // no properties are passed
    }

    private OnmsIpInterface createInterface() {
        OnmsNode node = new OnmsNode();
        node.setId(new Integer(1));
        node.setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");
        OnmsSnmpInterface snmpIface = new OnmsSnmpInterface("127.0.0.1", 1, node);
        snmpIface.setIfName("lo0");
        snmpIface.setPhysAddr("00:11:22:33:44");
        OnmsSnmpInterface snmpIfaceA = new OnmsSnmpInterface("127.0.0.1", 2, node);
        snmpIfaceA.setIfName("gif0");
        snmpIfaceA.setPhysAddr("00:11:22:33:45");
        snmpIfaceA.setIfType(55);
        OnmsSnmpInterface snmpIfaceB = new OnmsSnmpInterface("127.0.0.1", 3, node);
        snmpIfaceB.setIfName("stf0");
        snmpIfaceB.setPhysAddr("00:11:22:33:46");
        snmpIfaceB.setIfType(57);
        OnmsSnmpInterface snmpIface2 = new OnmsSnmpInterface("127.0.0.1", 6, node);
        snmpIface2.setIfName("fw0");
        snmpIface2.setPhysAddr("44:33:22:11:00");
        snmpIface2.setIfType(144);
        OnmsIpInterface iface = new OnmsIpInterface("127.0.0.1", node);
        iface.setId(27);
        iface.setIsSnmpPrimary(PrimaryType.PRIMARY);
        iface.setSnmpInterface(snmpIface2);
        
        snmpIface2.addIpInterface(iface);
        return iface;
    }

    private void initializeAgent(String testData) throws InterruptedException {
        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource(testData),
                                                  "127.0.0.1/1691");
    }

}
