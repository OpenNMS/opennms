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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class SnmpCollectorTest extends TestCase {
    private SnmpCollector m_snmpCollector;

    private MockSnmpAgent m_agent;

    private FileAnticipator m_fileAnticipator;

    private File m_snmpRrdDirectory;

    private PlatformTransactionManager m_transMgr;

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
        
        m_transMgr = new DataSourceTransactionManager(m_db);
        
        m_fileAnticipator = new FileAnticipator();
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

        Reader dataCollectionConfig = getDataCollectionConfigReader("/org/opennms/netmgt/config/datacollection-config.xml");
        initialize(new StringReader(m_snmpConfig), dataCollectionConfig);
        dataCollectionConfig.close();

        OnmsNode node = new OnmsNode();
        node.setId(new Integer(1));
        node.setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");
        OnmsIpInterface iface = new OnmsIpInterface("127.0.0.1", node);
        iface.setId(27);

        Collection outageCalendars = new LinkedList();

        Package pkg = new Package();
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName(svcName);
        pkg.addService(service);

        CollectdPackage wpkg = new CollectdPackage(pkg, "foo", false);
        CollectionSpecification spec = new CollectionSpecification(wpkg,
                                                                   svcName,
                                                                   outageCalendars,
                                                                   m_snmpCollector);

        CollectionAgent agent = getCollectionAgent(iface);
        
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), "1");
        for (String file : new String[] { "tcpActiveOpens", "tcpAttemptFails", "tcpCurrEstab",
                "tcpEstabResets", "tcpInErrors", "tcpInSegs", "tcpOutRsts", "tcpOutSegs",
                "tcpPassiveOpens", "tcpRetransSegs" }) {
            m_fileAnticipator.expecting(nodeDir, file + RrdUtils.getExtension());
        }

        assertEquals("collection status",
                     ServiceCollector.COLLECTION_SUCCEEDED,
                     spec.collect(agent));
        
        // Wait for any RRD writes to finish up
        Thread.sleep(1000);
    }

    private CollectionAgent getCollectionAgent(OnmsIpInterface iface) {
        IpInterfaceDao ifDao = EasyMock.createMock(IpInterfaceDao.class);
        EasyMock.expect(ifDao.get(iface.getId())).andReturn(iface).anyTimes();
        EasyMock.replay(ifDao);
        CollectionAgent agent = DefaultCollectionAgent.create(iface.getId(), ifDao, m_transMgr);
        return agent;
    }

    public void testBrocadeCollect() throws Exception {
        String svcName = "SNMP";

        String m_snmpConfig = "<?xml version=\"1.0\"?>\n"
                + "<snmp-config port=\"1691\" retry=\"3\" timeout=\"800\"\n"
                + "               read-community=\"public\"\n"
                + "               version=\"v1\">\n" + "</snmp-config>\n";

        initializeAgent("/org/opennms/netmgt/snmp/brocadeTestData1.properties");

        Reader dataCollectionConfig = getDataCollectionConfigReader("/org/opennms/netmgt/config/datacollection-brocade-config.xml");

        initialize(new StringReader(m_snmpConfig), dataCollectionConfig);
        dataCollectionConfig.close();

        OnmsNode node = new OnmsNode();
        node.setId(new Integer(1));
        node.setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");
        OnmsIpInterface iface = new OnmsIpInterface("127.0.0.1", node);
        iface.setId(27);

        Collection outageCalendars = new LinkedList();

        Package pkg = new Package();
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName(svcName);
        pkg.addService(service);

        CollectdPackage wpkg = new CollectdPackage(pkg, "foo", false);
        CollectionSpecification spec = new CollectionSpecification(wpkg,
                                                                   svcName,
                                                                   outageCalendars,
                                                                   m_snmpCollector);

        CollectionAgent agent = getCollectionAgent(iface);
        
        File nodeDir = m_fileAnticipator.expecting(getSnmpRrdDirectory(), "1");
        File brocadeDir = m_fileAnticipator.expecting(nodeDir, "brocadeFCPortIndex");
        for (int i = 1; i <= 8; i++) {
            File brocadeIndexDir = m_fileAnticipator.expecting(brocadeDir, Integer.toString(i));
            m_fileAnticipator.expecting(brocadeIndexDir, "strings.properties");
            for (String file : new String[] { "swFCPortTxWords", "swFCPortRxWords" }) {
                m_fileAnticipator.expecting(brocadeIndexDir, file + RrdUtils.getExtension());
            }
        }

        assertEquals("collection status",
                     ServiceCollector.COLLECTION_SUCCEEDED,
                     spec.collect(agent));

        // Wait for any RRD writes to finish up
        Thread.sleep(1000);
    }

    public void initialize(Reader snmpConfig, Reader dataCollectionConfig)
            throws MarshalException, ValidationException, IOException, RrdException {
        //RrdConfig.loadProperties(new ByteArrayInputStream(s_rrdConfig.getBytes()));
        RrdTestUtils.initialize();

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfig));
        DataCollectionConfigFactory.setInstance(new DataCollectionConfigFactory(dataCollectionConfig));

        Reader rdr = ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/test-database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(rdr));
        rdr.close();

        m_snmpCollector = new SnmpCollector();
        m_snmpCollector.initialize(null); // no properties are passed
    }

    public void initialize() throws IOException, MarshalException, ValidationException, RrdException {
        Reader snmpConfig = ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/snmp-config.xml");
        Reader dataCollectionConfig = getDataCollectionConfigReader("/org/opennms/netmgt/config/datacollection-config.xml");

        initialize(snmpConfig, dataCollectionConfig);

        snmpConfig.close();
        dataCollectionConfig.close();
    }

    private Reader getDataCollectionConfigReader(String classPathLocation) throws IOException {
        return ConfigurationTestUtils.getReaderForResourceWithReplacements(this, classPathLocation, new String[] { "%rrdRepository%", getSnmpRrdDirectory().getAbsolutePath() });
    }

    private File getSnmpRrdDirectory() throws IOException {
        if (m_snmpRrdDirectory == null) {
            m_snmpRrdDirectory = m_fileAnticipator.tempDir("snmp");
        }
        return m_snmpRrdDirectory;
    }

    private void initializeAgent(String testData) throws InterruptedException {
        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource(testData),
                                                  "127.0.0.1/1691");
    }

    private void initializeAgent() throws InterruptedException {
        initializeAgent("/org/opennms/netmgt/snmp/snmpTestData1.properties");
    }
}
