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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.snmp.mock.MockSnmpAgent;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import junit.framework.TestCase;

public class SnmpCollectorTest extends TestCase {
    private static final String s_rrdConfig ="org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy";

    private SnmpCollector m_snmpCollector;

    private MockSnmpAgent m_agent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.println("------------ Begin Test " + getName()
                + " --------------------------");
        MockLogAppender.setupLogging();

        MockNetwork m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("127.0.0.1");
        m_network.addService("ICMP");

        MockDatabase m_db = new MockDatabase();
        m_db.populate(m_network);

        DataSourceFactory.setInstance(m_db);
    }

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Override
    protected void tearDown() throws Exception {
        if (m_agent != null) {
            m_agent.shutDownAndWait();
        }
        MockUtil.println("------------ End Test " + getName()
                + " --------------------------");
        super.tearDown();
    }

    public void testInstantiate() throws Exception {
        new SnmpCollector();
    }

    public void testInitialize() throws Exception {
        initialize();
    }

    public void testCollect() throws Exception {
        String svcName = "SNMP";

        String m_snmpConfig = "<?xml version=\"1.0\"?>\n"
                + "<snmp-config port=\"1691\" retry=\"3\" timeout=\"800\"\n"
                + "               read-community=\"public\"\n"
                + "               version=\"v1\">\n" + "</snmp-config>\n";

        initializeAgent();

        Reader dataCollectionConfig = ConfigurationTestUtils.getReaderForResource(
                                                                                  this,
                                                                                  "/org/opennms/netmgt/config/datacollection-config.xml");
        initialize(new StringReader(m_snmpConfig), dataCollectionConfig);
        dataCollectionConfig.close();

        OnmsNode node = new OnmsNode();
        node.setId(new Integer(1));
        node.setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");
        OnmsIpInterface iface = new OnmsIpInterface("127.0.0.1", node);

        Collection outageCalendars = new LinkedList();

        Package pkg = new Package();
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName(svcName);
        pkg.addService(service);

        CollectdPackage wpkg = new CollectdPackage(pkg, "foo", false);
        CollectionSpecification spec = new CollectionSpecification(
                                                                   wpkg,
                                                                   svcName,
                                                                   outageCalendars,
                                                                   m_snmpCollector);

        CollectionAgent agent = new CollectionAgent(iface);

        assertEquals("collection status",
                     ServiceCollector.COLLECTION_SUCCEEDED,
                     spec.collect(agent));
    }

    public void FIXMEtestBrocadeCollect() throws Exception {
        String svcName = "SNMP";

        String m_snmpConfig = "<?xml version=\"1.0\"?>\n"
                + "<snmp-config port=\"1691\" retry=\"3\" timeout=\"800\"\n"
                + "               read-community=\"public\"\n"
                + "               version=\"v1\">\n" + "</snmp-config>\n";

        initializeAgent("target/test-classes/org/opennms/netmgt/snmp/brocadeTestData1.properties");

        Reader dataCollectionConfig = ConfigurationTestUtils.getReaderForResource(
                                                                                  this,
                                                                                  "/org/opennms/netmgt/config/datacollection-brocade-config.xml");
        initialize(new StringReader(m_snmpConfig), dataCollectionConfig);
        dataCollectionConfig.close();

        OnmsNode node = new OnmsNode();
        node.setId(new Integer(1));
        node.setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");
        OnmsIpInterface iface = new OnmsIpInterface("127.0.0.1", node);

        Collection outageCalendars = new LinkedList();

        Package pkg = new Package();
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName(svcName);
        pkg.addService(service);

        CollectdPackage wpkg = new CollectdPackage(pkg, "foo", false);
        CollectionSpecification spec = new CollectionSpecification(
                                                                   wpkg,
                                                                   svcName,
                                                                   outageCalendars,
                                                                   m_snmpCollector);

        CollectionAgent agent = new CollectionAgent(iface);

        assertEquals("collection status",
                     ServiceCollector.COLLECTION_SUCCEEDED,
                     spec.collect(agent));
    }

    public void initialize(Reader snmpConfig, Reader dataCollectionConfig)
            throws MarshalException, ValidationException, IOException {
        RrdConfig.loadProperties(new ByteArrayInputStream(
                                                          s_rrdConfig.getBytes()));

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfig));
        DataCollectionConfigFactory.setInstance(new DataCollectionConfigFactory(
                                                                                dataCollectionConfig));

        Reader rdr = ConfigurationTestUtils.getReaderForResource(this,
                                                                 "/org/opennms/netmgt/config/test-database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(
                                                                                rdr));
        rdr.close();

        m_snmpCollector = new SnmpCollector();
        m_snmpCollector.initialize(null); // no properties are passed
    }

    public void initialize() throws IOException, MarshalException,
            ValidationException {
        Reader snmpConfig = ConfigurationTestUtils.getReaderForResource(this,
                                                                        "/org/opennms/netmgt/config/snmp-config.xml");
        Reader dataCollectionConfig = ConfigurationTestUtils.getReaderForResource(
                                                                                  this,
                                                                                  "/org/opennms/netmgt/config/datacollection-config.xml");

        initialize(snmpConfig, dataCollectionConfig);

        snmpConfig.close();
        dataCollectionConfig.close();
    }

    private void initializeAgent(String testData) throws InterruptedException {
        m_agent = MockSnmpAgent.createAgentAndRun(new FileSystemResource(testData),
                                                  "127.0.0.1/1691");
    }

    private void initializeAgent() throws InterruptedException {
        initializeAgent("target/test-classes/org/opennms/netmgt/snmp/mock/loadSnmpDataTest.properties");
    }
}
