/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Unit test for ModelImport application.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class NewSuspectScanTest implements InitializingBean {
    
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
    private NodeDao m_nodeDao;

    @Autowired
    private DistPollerDao m_distPollerDao;
    
    @Autowired
    private ProvisionService m_provisionService;
    
    @Autowired
    private MockEventIpcManager m_eventSubscriber;
    
    private ForeignSourceRepository m_foreignSourceRepository;
    
    private ForeignSource m_foreignSource;

    static private String s_initialDiscoveryEnabledValue;
    
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

    @BeforeClass
    public static void setEnableDiscovery() {
        s_initialDiscoveryEnabledValue = System.getProperty("org.opennms.provisiond.enableDiscovery");
        System.setProperty("org.opennms.provisiond.enableDiscovery", "true");
        
    }
    
    @AfterClass
    public static void resetEnableDiscovery() {
        if (s_initialDiscoveryEnabledValue == null) {
            System.getProperties().remove("org.opennms.provisiond.enableDiscovery");
        } else {
            System.setProperty("org.opennms.provisiond.enableDiscovery", s_initialDiscoveryEnabledValue);
        }
    }
    
    @Before
    public void setUp() throws Exception {
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

        m_provisioner.setEventForwarder(m_eventSubscriber);
        m_provisioner.start();
    }

    @Test(timeout=300000)
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="172.20.2.201", resource="classpath:snmpTestData3.properties"),
        @JUnitSnmpAgent(host="172.20.2.204", resource="classpath:snmpTestData3.properties")
    })
    public void testScanNewSuspect() throws Exception {
        
        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());

        final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(1).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(InetAddressUtils.addr("172.20.2.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(InetAddressUtils.addr("172.20.2.204")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(InetAddressUtils.addr("172.20.2.201")).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(InetAddressUtils.addr("172.20.2.204")).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(InetAddressUtils.addr("172.20.2.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(1).getEvent());

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
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testScanNewSuspectNoSnmp() throws Exception {

        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());
        
        final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(1).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(InetAddressUtils.addr("172.20.2.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(1).getEvent());

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
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    // 192.0.2.0/24 reserved by IANA for testing purposes
    @JUnitSnmpAgent(host="192.0.2.123", resource="classpath:no-ipaddrtable.properties")
    public void testScanNewSuspectNoIpAddrTable() throws Exception {

        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());
        
        InetAddress ip = InetAddressUtils.addr("192.0.2.123");

        final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(1).getEvent());
		anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(ip).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(ip).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(1).setInterface(ip).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(1).getEvent());

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
