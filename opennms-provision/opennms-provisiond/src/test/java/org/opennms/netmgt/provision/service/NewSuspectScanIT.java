/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.NodeLabelChangedEventBuilder;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
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
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
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
public class NewSuspectScanIT extends ProvisioningITCase implements InitializingBean {

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

    @Autowired
    private LocationAwareDnsLookupClient m_locationAwareDnsLookupClient;

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
        m_eventSubscriber.getEventAnticipator().reset();

        if (m_distPollerDao.findAll().size() == 0) {
            OnmsDistPoller distPoller = new OnmsDistPoller(DistPollerDao.DEFAULT_DIST_POLLER_ID);
            distPoller.setLabel("localhost");
            distPoller.setLocation(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
            distPoller.setType(OnmsMonitoringSystem.TYPE_OPENNMS);
            m_distPollerDao.save(distPoller);
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
        @JUnitSnmpAgent(host="198.51.100.201", resource="classpath:/snmpTestData3.properties"),
        @JUnitSnmpAgent(host="198.51.100.204", resource="classpath:/snmpTestData3.properties")
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
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.204")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.204")).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new NodeLabelChangedEventBuilder("Provisiond").setOldNodeLabel("oldNodeLabel").setNewNodeLabel("newNodeLabel").setNodeid(nextNodeId).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        final NewSuspectScan scan = m_provisioner.createNewSuspectScan(addr("198.51.100.201"), null, "my-custom-location");
        runScan(scan);

        anticipator.verifyAnticipated(20000, 0, 0, 0, 0);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(1, getNodeDao().countAll());

        OnmsNode onmsNode = getNodeDao().get(nextNodeId);
        assertEquals(null, onmsNode.getForeignSource());
        assertEquals(null, onmsNode.getForeignId());
        assertEquals("my-custom-location", onmsNode.getLocation().getLocationName());

        final StringBuilder errorMsg = new StringBuilder();
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
    @JUnitSnmpAgents({
        @JUnitSnmpAgent(host="198.51.100.201", resource="classpath:/snmpTestData3.properties"),
        @JUnitSnmpAgent(host="198.51.100.204", resource="classpath:/snmpTestData3.properties")
    })
    public void testScanNewSuspectWithForeignSource() throws Exception {
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
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.204")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.204")).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).getEvent());
        anticipator.anticipateEvent(new NodeLabelChangedEventBuilder("Provisiond").setOldNodeLabel("oldNodeLabel").setNewNodeLabel("newNodeLabel").setNodeid(nextNodeId).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        final String foreignSource = "Testie";
        NewSuspectScan scan = m_provisioner.createNewSuspectScan(addr("198.51.100.201"), foreignSource, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
        runScan(scan);

        anticipator.verifyAnticipated(20000, 0, 0, 0, 0);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(1, getNodeDao().countAll());

        List<OnmsNode> onmsNodes = getNodeDao().findByLabel("brozow.local");
        OnmsNode onmsNode = onmsNodes.get(0);
        assertEquals("Testie", onmsNode.getForeignSource());
        assertEquals(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, onmsNode.getLocation().getLocationName());

        final StringBuilder errorMsg = new StringBuilder();
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

        // NMS-8835: Now send another new suspect event for the same IP address and foreignSource
        scan = m_provisioner.createNewSuspectScan(addr("198.51.100.201"), foreignSource, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
        runScan(scan);

        // The node count should not increase
        assertEquals(1, getNodeDao().countAll());
    }

    @Test(timeout=300000)
    public void testScanNewSuspectWithForeignSourceAndLocation() throws Exception {
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
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        final String foreignSource = "Testie";
        final String locationName = "!" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
        final NewSuspectScan scan = m_provisioner.createNewSuspectScan(addr("198.51.100.201"), foreignSource, locationName);
        runScan(scan);

        anticipator.verifyAnticipated(20000, 0, 0, 0, 0);

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

        // HZN-960: Verify that the location name was properly set on the node in the requisition
        final Requisition requisition = m_foreignSourceRepository.getRequisition(foreignSource);
        final List<RequisitionNode> requisitionNodes = requisition.getNodes();
        assertEquals(1, requisitionNodes.size());

        final RequisitionNode requisitionNode = requisitionNodes.get(0);
        assertEquals(locationName, requisitionNode.getLocation());
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
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        final NewSuspectScan scan = m_provisioner.createNewSuspectScan(addr("198.51.100.201"), null, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
        runScan(scan);

        anticipator.verifyAnticipated(20000, 0, 0, 0, 0);

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
    public void testRescanWithChangingDns() throws Exception {
        final HostnameResolver originalHostnameResolver = m_provisionService.getHostnameResolver();
        try {
            final int nextNodeId = m_nodeDao.getNextNodeId();

            //Verify empty database
            assertEquals(1, getDistPollerDao().countAll());
            assertEquals(0, getNodeDao().countAll());
            assertEquals(0, getInterfaceDao().countAll());
            assertEquals(0, getMonitoredServiceDao().countAll());
            assertEquals(0, getServiceTypeDao().countAll());
            assertEquals(0, getSnmpInterfaceDao().countAll());

            m_provisionService.setHostnameResolver(new HostnameResolver() {
                @Override public String getHostname(final InetAddress addr, final String location) {
                    return "oldNodeLabel";
                }
            });

            final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
            anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());
            anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).getEvent());
            anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

            final NewSuspectScan scan = m_provisioner.createNewSuspectScan(addr("198.51.100.201"), null, null);
            runScan(scan);

            anticipator.verifyAnticipated(20000, 0, 0, 0, 0);

            //Verify distpoller count
            assertEquals(1, getDistPollerDao().countAll());

            //Verify node count
            assertEquals(1, getNodeDao().countAll());

            //Verify node info
            final OnmsNode beforeNode = getNodeDao().findAll().iterator().next();
            assertEquals(Integer.valueOf(nextNodeId), beforeNode.getId());
            assertEquals("oldNodeLabel", beforeNode.getLabel());
            assertEquals(NodeLabelSource.HOSTNAME, beforeNode.getLabelSource());
            assertEquals(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, beforeNode.getLocation().getLocationName());
            assertEquals("oldNodeLabel", beforeNode.getIpInterfaces().iterator().next().getIpHostName());

            //Verify ipinterface count
            assertEquals("Unexpected number of interfaces found: " + getInterfaceDao().findAll(), 1, getInterfaceDao().countAll());
            
            //Verify ifservices count - discover snmp service on other if
            assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 0, getMonitoredServiceDao().countAll());

            //Verify service count
            assertEquals(0, getServiceTypeDao().countAll());

            //Verify snmpInterface count
            assertEquals(0, getSnmpInterfaceDao().countAll());

            m_provisionService.setHostnameResolver(new HostnameResolver() {
                @Override public String getHostname(final InetAddress addr, final String location) {
                    return "newNodeLabel";
                }
            });

            final ForceRescanScan rescan = m_provisioner.createForceRescanScan(nextNodeId);
            runScan(rescan);

            final OnmsNode afterNode = getNodeDao().findAll().iterator().next();
            assertEquals(Integer.valueOf(nextNodeId), afterNode.getId());
            assertEquals("newNodeLabel", afterNode.getLabel());
            assertEquals(NodeLabelSource.HOSTNAME, afterNode.getLabelSource());
            assertEquals("newNodeLabel", afterNode.getIpInterfaces().iterator().next().getIpHostName());
        } finally {
            m_provisionService.setHostnameResolver(originalHostnameResolver);
        }
    }

    @Test(timeout=300000)
    public void testWithNoDnsOnRescan() throws Exception {
        try {
            final int nextNodeId = m_nodeDao.getNextNodeId();

            //Verify empty database
            assertEquals(1, getDistPollerDao().countAll());
            assertEquals(0, getNodeDao().countAll());
            assertEquals(0, getInterfaceDao().countAll());
            assertEquals(0, getMonitoredServiceDao().countAll());
            assertEquals(0, getServiceTypeDao().countAll());
            assertEquals(0, getSnmpInterfaceDao().countAll());

            m_provisionService.setHostnameResolver(new HostnameResolver() {
                @Override public String getHostname(final InetAddress addr, final String location) {
                    return "oldNodeLabel";
                }
            });

            final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
            anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());
            anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(addr("198.51.100.201")).getEvent());
            anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

            final NewSuspectScan scan = m_provisioner.createNewSuspectScan(addr("198.51.100.201"), null, null);
            runScan(scan);

            anticipator.verifyAnticipated(20000, 0, 0, 0, 0);

            //Verify distpoller count
            assertEquals(1, getDistPollerDao().countAll());

            //Verify node count
            assertEquals(1, getNodeDao().countAll());

            //Verify node info
            final OnmsNode beforeNode = getNodeDao().findAll().iterator().next();
            assertEquals(Integer.valueOf(nextNodeId), beforeNode.getId());
            assertEquals("oldNodeLabel", beforeNode.getLabel());
            assertEquals(NodeLabelSource.HOSTNAME, beforeNode.getLabelSource());
            assertEquals("oldNodeLabel", beforeNode.getIpInterfaces().iterator().next().getIpHostName());

            //Verify ipinterface count
            assertEquals("Unexpected number of interfaces found: " + getInterfaceDao().findAll(), 1, getInterfaceDao().countAll());

            //Verify ifservices count - discover snmp service on other if
            assertEquals("Unexpected number of services found: "+getMonitoredServiceDao().findAll(), 0, getMonitoredServiceDao().countAll());

            //Verify service count
            assertEquals(0, getServiceTypeDao().countAll());

            //Verify snmpInterface count
            assertEquals(0, getSnmpInterfaceDao().countAll());

            m_provisionService.setHostnameResolver(new HostnameResolver() {
                @Override public String getHostname(final InetAddress addr, final String location) {
                    return null;
                }
            });

            final ForceRescanScan rescan = m_provisioner.createForceRescanScan(nextNodeId);
            runScan(rescan);

            final OnmsNode afterNode = getNodeDao().findAll().iterator().next();
            assertEquals(Integer.valueOf(nextNodeId), afterNode.getId());
            assertEquals("oldNodeLabel", afterNode.getLabel());
            assertEquals(NodeLabelSource.HOSTNAME, afterNode.getLabelSource());
            assertEquals("oldNodeLabel", afterNode.getIpInterfaces().iterator().next().getIpHostName());
        } finally {
            m_provisionService.setHostnameResolver(new DefaultHostnameResolver(m_locationAwareDnsLookupClient));
        }
    }

    @Test(timeout=300000)
    // 192.0.2.0/24 reserved by IANA for testing purposes
    @JUnitSnmpAgent(host="192.0.2.123", resource="classpath:/no-ipaddrtable.properties")
    public void testScanNewSuspectNoIpAddrTable() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());

        InetAddress ip = addr("192.0.2.123");

        final EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(ip).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(ip).setService("SNMP").getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(ip).getEvent());
        anticipator.anticipateEvent(new NodeLabelChangedEventBuilder("Provisiond").setOldNodeLabel("oldNodeLabel").setNewNodeLabel("newNodeLabel").setNodeid(nextNodeId).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        final NewSuspectScan scan = m_provisioner.createNewSuspectScan(ip, null, null);
        runScan(scan);

        anticipator.verifyAnticipated(20000, 0, 2000, 0, 0);

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

    @Test(timeout=300000)
    public void testDuplicateIpsInSeparateLocations() throws Exception {
        runDuplicateIpScan(null, null, "RDU", "ORD");
    }

    @Test(timeout=300000)
    public void testDuplicateIpsInSeparateLocationsWithDifferentForeignSource() throws Exception {
        runDuplicateIpScan("one", "two", "RDU", "ORD");
    }

    @Test(timeout=300000)
    public void testDuplicateIpsInSeparateLocationsWithSameForeignSource() throws Exception {
        runDuplicateIpScan("one", "one", "RDU", "ORD");
    }

    @Test(timeout=300000)
    public void testDuplicateIpsInSameLocationWithDifferentForeignSource() throws Exception {
        runDuplicateIpScan("one", "two", "RDU", "RDU");
    }

    @Test(timeout=300000)
    public void testDuplicateIpsInNullAndDefaultLocationWithDifferentForeignSource() throws Exception {
        runDuplicateIpScan("one", "two", null, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
    }

    @Test(timeout=300000)
    public void testDuplicateIpsInDefaultAndNullLocationWithDifferentForeignSource() throws Exception {
        runDuplicateIpScan("one", "two", MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, null);
    }

    private void runDuplicateIpScan(final String firstForeignSource, final String secondForeignSource, final String firstLocation, final String secondLocation) throws InterruptedException, ExecutionException {
        int nextNodeId = m_nodeDao.getNextNodeId();

        //Verify empty database
        assertEquals(1, getDistPollerDao().countAll());
        assertEquals(0, getNodeDao().countAll());
        assertEquals(0, getInterfaceDao().countAll());
        assertEquals(0, getMonitoredServiceDao().countAll());
        assertEquals(0, getServiceTypeDao().countAll());
        assertEquals(0, getSnmpInterfaceDao().countAll());

        InetAddress ip = addr("192.0.2.123");

        EventAnticipator anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(ip).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        NewSuspectScan scan = m_provisioner.createNewSuspectScan(ip, firstForeignSource, firstLocation);
        runScan(scan);

        anticipator.verifyAnticipated(20000, 0, 2000, 0, 0);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(1, getNodeDao().countAll());

        //Verify ipinterface count
        assertEquals("Unexpected number of interfaces found: " + getInterfaceDao().findAll(), 1, getInterfaceDao().countAll());

        // Now do it again, with a different location
        nextNodeId = m_nodeDao.getNextNodeId();

        anticipator = m_eventSubscriber.getEventAnticipator();
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond").setNodeid(nextNodeId).setInterface(ip).getEvent());
        anticipator.anticipateEvent(new EventBuilder(EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond").setNodeid(nextNodeId).getEvent());

        scan = m_provisioner.createNewSuspectScan(ip, secondForeignSource, secondLocation);
        runScan(scan);

        anticipator.verifyAnticipated(20000, 0, 2000, 0, 0);

        //Verify distpoller count
        assertEquals(1, getDistPollerDao().countAll());

        //Verify node count
        assertEquals(2, getNodeDao().countAll());

        //Verify ipinterface count
        assertEquals("Unexpected number of interfaces found: " + getInterfaceDao().findAll(), 2, getInterfaceDao().countAll());
    }

    public void runScan(final Scan scan) throws InterruptedException, ExecutionException {
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
