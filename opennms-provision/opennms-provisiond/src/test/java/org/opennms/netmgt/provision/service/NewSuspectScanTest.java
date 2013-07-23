/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * Unit test for ModelImport application.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.provisiond.enableDiscovery=true",
        "importer.foreign-source.dir=target/foreign-sources",
        "importer.requisition.dir=target/imports"
})
@DirtiesContext
public class NewSuspectScanTest extends ProvisioningTestCase implements InitializingBean {

    @Autowired
    private Provisioner m_provisioner;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private MockNodeDao m_nodeDao;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private ProvisionService m_provisionService;

    @Autowired
    private MockEventIpcManager m_eventSubscriber;

    @Autowired
    private DatabasePopulator m_populator;

    private ForeignSourceRepository m_foreignSourceRepository;

    private ForeignSource m_foreignSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @BeforeClass
    public static void setUpLogging() {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

        MockLogAppender.setupLogging(props);
    }

    @Before
    public void setUp() throws Exception {
        if (m_distPollerDao.findAll().size() == 0) {
            m_distPollerDao.save(new OnmsDistPoller("localhost", "127.0.0.1"));
        }

        m_foreignSource = new ForeignSource();
        m_foreignSource.setName("imported:");
        m_foreignSource.setScanInterval(Duration.standardDays(1));
        final PluginConfig detector = new PluginConfig("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector");
        detector.addParameter("timeout", "1000");
        detector.addParameter("retries", "0");
        m_foreignSource.addDetector(detector);

        m_foreignSourceRepository = new MockForeignSourceRepository();
        m_foreignSourceRepository.putDefaultForeignSource(m_foreignSource);

        m_provisionService.setForeignSourceRepository(m_foreignSourceRepository);

        m_provisioner.start();
    }

    @After
    public void tearDown() throws InterruptedException {
        m_populator.resetDatabase();
        waitForEverything();
    }

    @Test(timeout=300000)
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="172.20.2.201", resource="classpath:snmpTestData3.properties"),
        @JUnitSnmpAgent(host="172.20.2.204", resource="classpath:snmpTestData3.properties")
    })
    public void testScanNewSuspect() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());

        final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(InetAddressUtils.addr("172.20.2.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(InetAddressUtils.addr("172.20.2.204")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(InetAddressUtils.addr("172.20.2.201")).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(InetAddressUtils.addr("172.20.2.204")).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(InetAddressUtils.addr("172.20.2.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        final NewSuspectScan scan = m_provisioner.createNewSuspectScan(InetAddressUtils.addr("172.20.2.201"));
        runScan(scan);

        anticipator.verifyAnticipated(200000, 0, 0, 0, 0);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(1, getNodeDao().countAll());

        final StringBuffer errorMsg = new StringBuffer();
        //Verify ipinterface count
        for (final OnmsIpInterface iface : getInterfaceDao().findAll()) {
            errorMsg.append(iface.toString());
        }
        assertEquals(errorMsg.toString(), 2, getInterfaceDao().countAll());

        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found.", 2, getMonitoredServiceDao().countAll());

        //Verify service count
        assertEquals(1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(6, getSnmpInterfaceDao().countAll());

    }

    @Test(timeout=300000)
    public void testScanNewSuspectNoSnmp() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());

        final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(InetAddressUtils.addr("172.20.2.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        final NewSuspectScan scan = m_provisioner.createNewSuspectScan(InetAddressUtils.addr("172.20.2.201"));
        runScan(scan);

        anticipator.verifyAnticipated(200000, 0, 0, 0, 0);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(1, getNodeDao().countAll());

        //Verify ipinterface count
        assertEquals("Unexpected number of interfaces found: " + getInterfaceDao().findAll(), 1, getInterfaceDao().countAll());

        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 0, getMonitoredServiceDao().countAll());

        //Verify service count
        assertEquals(0, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(0, getSnmpInterfaceDao().countAll());

    }

    @Test(timeout=300000)
    // 192.0.2.0/24 reserved by IANA for testing purposes
    @JUnitSnmpAgent(host="192.0.2.123", resource="classpath:no-ipaddrtable.properties")
    public void testScanNewSuspectNoIpAddrTable() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());

        InetAddress ip = InetAddressUtils.addr("192.0.2.123");

        final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(ip).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(ip).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(ip).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        final NewSuspectScan scan = m_provisioner.createNewSuspectScan(ip);
        runScan(scan);

        anticipator.verifyAnticipated(200000, 0, 2000, 0, 0);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(1, getNodeDao().countAll());

        //Verify ipinterface count
        assertEquals("Unexpected number of interfaces found: " + getInterfaceDao().findAll(), 1, getInterfaceDao().countAll());

        //Verify ifservices count - discover snmp service on other if
        assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 1, getMonitoredServiceDao().countAll());

        //Verify service count
        assertEquals(1, getServiceTypeDao().countAll());

        //Verify snmpInterface count
        assertEquals(0, getSnmpInterfaceDao().countAll());

    }

    public void runScan(final NewSuspectScan scan) throws InterruptedException, ExecutionException {
        final Task t = scan.createTask();
        t.schedule();
        t.waitFor();
        waitForEverything();
    }


    private DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }


    private NodeDao getNodeDao() {
        return m_nodeDao;
    }


    private IpInterfaceDao getInterfaceDao() {
        return m_ipInterfaceDao;
    }


    private SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }


    private MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }


    private ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }
}
