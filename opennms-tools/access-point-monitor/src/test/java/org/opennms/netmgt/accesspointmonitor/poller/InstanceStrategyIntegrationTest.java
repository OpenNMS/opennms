/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.accesspointmonitor.poller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.responder.Sleeper;
import org.opennms.netmgt.accesspointmonitor.AccessPointMonitord;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfigFactory;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.AccessPointStatus;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-daemon.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml",
    "classpath:META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:META-INF/opennms/applicationContext-proxy-snmp.xml",
    "classpath:META-INF/opennms/applicationContext-accesspointmonitord.xml",
    "classpath:META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@DirtiesContext
public class InstanceStrategyIntegrationTest implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(InstanceStrategyIntegrationTest.class);

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    @Autowired
    private AccessPointDao m_accessPointDao;

    @Autowired
    private AccessPointMonitord m_apm;

    AnnotationBasedEventListenerAdapter m_adapter;
    AccessPointMonitorConfigFactory m_apmdConfigFactory;
    private MockEventIpcManager m_eventMgr;
    private EventAnticipator m_anticipator;

    private final static String AP1_MAC = "00:01:02:03:04:05";
    private final static String AP2_MAC = "07:08:09:0A:0B:0C";
    private final static String AP3_MAC = "F0:05:BA:11:00:FF";

    private final static int AGENT_TIMEOUT = 1000;
    private final static int POLLING_INTERVAL = 5000;
    private final static int POLLING_INTERVAL_DELTA = POLLING_INTERVAL + 2000;

    private static final String PASSIVE_STATUS_UEI = "uei.opennms.org/services/passiveServiceStatus";
    private static final String SNMP_DATA_PATH = "/org/opennms/netmgt/accesspointmonitor/poller/instancestrategy/";

    @Override
    public void afterPropertiesSet() {
        assertNotNull(m_nodeDao);
        assertNotNull(m_ipInterfaceDao);
        assertNotNull(m_serviceTypeDao);
        assertNotNull(m_snmpPeerFactory);
        assertNotNull(m_apm);
    }

    @Before
    public void setUp() throws Exception {
        // Initialise the SNMP peer
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);

        // Create our event manager and anticipator
        m_anticipator = new EventAnticipator();

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.setSynchronous(true);

        // Ensure our annotations are called
        m_adapter = new AnnotationBasedEventListenerAdapter(m_apm, m_eventMgr);
    }

    @After
    public void tearDown() throws Exception {
        Sleeper.getInstance().setSleepTime(0);
        if (m_apm.getStatus() == AccessPointMonitord.RUNNING) {
            m_apm.stop();
            LOG.debug("AccessPointMonitor stopped");
        }
    }

    private void initApmdWithConfig(String config) throws Exception {
        m_apm.setEventManager(m_eventMgr);

        // Convert the string to an input stream
        InputStream is = new ByteArrayInputStream(config.getBytes("UTF-8"));
        m_apmdConfigFactory = new AccessPointMonitorConfigFactory(0, is);
        m_apm.setPollerConfig(m_apmdConfigFactory.getConfig());

        // Initialize, but do not start
        m_apm.init();
    }

    private void updateConfigAndReloadDaemon(String config, Boolean anticipateEvents) throws Exception {
        // Build an input stream from the string
        InputStream is = new ByteArrayInputStream(config.getBytes("UTF-8"));

        // Get the latest time-stamp on the configuration file so that the
        // factory will use our string and not the file
        File cfgFile = ConfigFileConstants.getConfigFileByName(AccessPointMonitorConfigFactory.getDefaultConfigFilename());

        // Update the configuration factory
        AccessPointMonitorConfigFactory.setInstance(new AccessPointMonitorConfigFactory(cfgFile.lastModified(), is));

        // Anticipate the reload successful event
        if (anticipateEvents) {
            EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, m_apm.getName());
            bldr.setParam(EventConstants.PARM_DAEMON_NAME, "AccessPointMonitor");
            m_anticipator.anticipateEvent(bldr.getEvent());
        }

        // Anticipate the reload event
        EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test");
        bldr.setParam(EventConstants.PARM_DAEMON_NAME, "AccessPointMonitor");
        if (anticipateEvents) {
            m_anticipator.anticipateEvent(bldr.getEvent());
        }

        // Send the reload event
        m_eventMgr.send(bldr.getEvent());
    }

    public void anticipateApStatusEvent(String physAddr, String status) {
        EventBuilder newSuspectBuilder = new EventBuilder(PASSIVE_STATUS_UEI, "accesspointmonitord");
        newSuspectBuilder.setParam("physAddr", physAddr);
        newSuspectBuilder.setParam(EventConstants.PARM_PASSIVE_SERVICE_STATUS, status);
        m_anticipator.anticipateEvent(newSuspectBuilder.getEvent());
    }

    private void addNewAccessPoint(String name, String mac, String pkg) {
        NetworkBuilder nb = new NetworkBuilder();

        nb.addNode(name).setForeignSource("apmd").setForeignId(name);
        nb.addInterface("169.254.0.1");
        m_nodeDao.save(nb.getCurrentNode());

        final OnmsAccessPoint ap1 = new OnmsAccessPoint(mac, nb.getCurrentNode().getId(), pkg);
        ap1.setStatus(AccessPointStatus.UNKNOWN);
        m_accessPointDao.save(ap1);

        m_nodeDao.flush();
        m_accessPointDao.flush();
    }

    private void addNewController(String nodeName, String ipAddress, String pollerCategory) {
        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode(nodeName).setForeignSource("apmd").setForeignId(nodeName).setType(NodeType.ACTIVE);
        nb.setAssetAttribute("pollerCategory", pollerCategory);
        nb.addInterface(ipAddress).setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();
    }

    private void setOidValueForAccessPoint(String ipAddress, String apMac, Integer value) {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.getInetAddress(ipAddress));
        SnmpObjId instance = InstanceStrategy.getInstanceFromPhysAddr(apMac);
        SnmpObjId oid = SnmpObjId.get(".1.3.6.1.4.1.14823.2.2.1.5.2.1.4.1.19").append(instance);
        SnmpUtils.set(agentConfig, oid, SnmpUtils.getValueFactory().getInt32(value));
    }

    /*
     * Run a series of tests with a single controller and 3 access points.
     * Verify: That the proper events are sent by the daemon. The AP state in
     * the database after the poll.
     */
    @Test
    @JUnitSnmpAgent(host = "10.1.0.2", resource = SNMP_DATA_PATH + "10.1.0.2-walk.txt")
    public void testApUpDown() throws Exception {
        // Add AP1 and AP2 to the default package
        addNewAccessPoint("ap1", AP1_MAC, "default");
        addNewAccessPoint("ap2", AP2_MAC, "default");

        // Add AP3 to a separate package
        addNewAccessPoint("ap3", AP3_MAC, "not-default");

        // Add a controller to the default package
        addNewController("amc1", "10.1.0.2", "default");

        // Set AP1 as UP and AP2 as DOWN
        setOidValueForAccessPoint("10.1.0.2", AP1_MAC, 1);
        setOidValueForAccessPoint("10.1.0.2", AP2_MAC, 0);

        // Anticipate the events
        anticipateApStatusEvent(AP1_MAC, "UP");
        anticipateApStatusEvent(AP2_MAC, "DOWN");

        // Initialize and start the daemon
        initApmdWithConfig(getStandardConfig());
        m_apm.start();

        // Verify the events
        verifyAnticipated(POLLING_INTERVAL_DELTA);

        // Verify the state of the APs in the database
        OnmsAccessPoint ap1 = m_accessPointDao.get(AP1_MAC);
        assertTrue(ap1.getStatus() == AccessPointStatus.ONLINE);

        OnmsAccessPoint ap2 = m_accessPointDao.get(AP2_MAC);
        assertTrue(ap2.getStatus() == AccessPointStatus.OFFLINE);

        OnmsAccessPoint ap3 = m_accessPointDao.get(AP3_MAC);
        assertTrue(ap3.getStatus() == AccessPointStatus.UNKNOWN);

        // Change AP3's package, the next poll should send an additional DOWN
        // event
        anticipateApStatusEvent(AP1_MAC, "UP");
        anticipateApStatusEvent(AP2_MAC, "DOWN");
        anticipateApStatusEvent(AP3_MAC, "DOWN");

        ap3.setPollingPackage("default");
        m_accessPointDao.update(ap3);
        m_accessPointDao.flush();

        // Verify the events
        verifyAnticipated(POLLING_INTERVAL_DELTA);

        // Update the data in the SNMP agent to show AP1 as DOWN
        anticipateApStatusEvent(AP1_MAC, "DOWN");
        anticipateApStatusEvent(AP2_MAC, "DOWN");
        anticipateApStatusEvent(AP3_MAC, "DOWN");

        setOidValueForAccessPoint("10.1.0.2", AP1_MAC, 2);

        // Verify the events
        verifyAnticipated(POLLING_INTERVAL_DELTA);

        // Verify the DB again, all APs should be DOWN now
        ap1 = m_accessPointDao.get(AP1_MAC);
        assertTrue(ap1.getStatus() == AccessPointStatus.OFFLINE);

        ap2 = m_accessPointDao.get(AP2_MAC);
        assertTrue(ap2.getStatus() == AccessPointStatus.OFFLINE);

        ap3 = m_accessPointDao.get(AP3_MAC);
        assertTrue(ap3.getStatus() == AccessPointStatus.OFFLINE);

        // Bring AP1 back UP
        anticipateApStatusEvent(AP1_MAC, "UP");
        anticipateApStatusEvent(AP2_MAC, "DOWN");
        anticipateApStatusEvent(AP3_MAC, "DOWN");

        setOidValueForAccessPoint("10.1.0.2", AP1_MAC, 1);

        // Verify the events
        verifyAnticipated(POLLING_INTERVAL_DELTA);
    }

    /*
     * Test the poller's behaviour when the SNMP agent times outs. Verify:
     * That the proper events are sent by the daemon. The AP state in the
     * database after the poll.
     */
    @Test
    @JUnitSnmpAgent(host = "10.1.0.2", resource = SNMP_DATA_PATH + "10.1.0.2-walk.txt")
    public void testAgentTimeout() throws Exception {
        // Add AP1 to the default package
        addNewAccessPoint("ap1", AP1_MAC, "default");

        // Add a controller to the default package
        addNewController("amc1", "10.1.0.2", "default");

        // Set AP1 as UP
        setOidValueForAccessPoint("10.1.0.2", AP1_MAC, 1);

        // Make the SNMP agent timeout
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.getInetAddress("10.1.0.2"));
        Sleeper.getInstance().setSleepTime(agentConfig.getTimeout() + 1000);

        try {
            // Anticipate the event
            anticipateApStatusEvent(AP1_MAC, "DOWN");

            // Initialize and start the daemon
            initApmdWithConfig(getStandardConfig());
            m_apm.start();

            // Verify the events
            verifyAnticipated(POLLING_INTERVAL_DELTA + 2000);

            // Verify the state of the AP in the database
            OnmsAccessPoint ap1 = m_accessPointDao.get(AP1_MAC);
            assertTrue(ap1.getStatus() == AccessPointStatus.OFFLINE);
        } finally {
            // Clear the timeout
            Sleeper.getInstance().setSleepTime(0);
        }
    }

    /*
     * Test the behaviour when the configuration is modified and the daemon is
     * reloaded. Verify: That the proper events are sent by the daemon. The AP
     * state in the database after the poll.
     */
    @Test
    @JUnitSnmpAgent(host = "10.1.0.2", resource = SNMP_DATA_PATH + "10.1.0.2-walk.txt")
    public void testReloadDaemon() throws Exception {
        // Add AP1 and AP2 to the default package
        addNewAccessPoint("ap1", AP1_MAC, "default");
        addNewAccessPoint("ap2", AP2_MAC, "default");

        // Add AP3 to a separate package
        addNewAccessPoint("ap3", AP3_MAC, "not-default");

        // Add a controller to the default package
        addNewController("amc1", "10.1.0.2", "default");

        // Set AP1 as UP
        setOidValueForAccessPoint("10.1.0.2", AP1_MAC, 1);

        // Initialize and start the daemon
        initApmdWithConfig(getEmptyConfig());
        m_apm.start();

        // Sleep for a polling cycle
        sleep(POLLING_INTERVAL_DELTA);

        // Verify the state of the APs in the database
        OnmsAccessPoint ap1 = m_accessPointDao.get(AP1_MAC);
        LOG.debug(ap1.getStatus().getLabel());
        assertTrue(ap1.getStatus() == AccessPointStatus.UNKNOWN);

        OnmsAccessPoint ap2 = m_accessPointDao.get(AP2_MAC);
        assertTrue(ap2.getStatus() == AccessPointStatus.UNKNOWN);

        OnmsAccessPoint ap3 = m_accessPointDao.get(AP3_MAC);
        assertTrue(ap3.getStatus() == AccessPointStatus.UNKNOWN);

        // Anticipate the events
        anticipateApStatusEvent(AP1_MAC, "UP");
        anticipateApStatusEvent(AP2_MAC, "DOWN");

        // Update the configuration and send a reload event to the daemon
        updateConfigAndReloadDaemon(getStandardConfig(), true);

        // Verify the events
        verifyAnticipated(POLLING_INTERVAL_DELTA);

        // Verify the state of the APs in the database
        ap1 = m_accessPointDao.get(AP1_MAC);
        assertTrue(ap1.getStatus() == AccessPointStatus.ONLINE);

        ap2 = m_accessPointDao.get(AP2_MAC);
        assertTrue(ap2.getStatus() == AccessPointStatus.OFFLINE);

        ap3 = m_accessPointDao.get(AP3_MAC);
        assertTrue(ap3.getStatus() == AccessPointStatus.UNKNOWN);
    }

    /*
     * Test the behaviour when multiple controllers are configured in a single
     * package. Verify: That the proper events are sent by the daemon. The AP
     * state and the controller's address in the database after the poll.
     */
    @Test
    @JUnitSnmpAgents(value = {
        @JUnitSnmpAgent(host = "10.1.0.2", port = 161, resource = SNMP_DATA_PATH + "10.1.0.2-walk.txt"),
        @JUnitSnmpAgent(host = "10.1.1.2", port = 161, resource = SNMP_DATA_PATH + "10.1.1.2-walk.txt"),
        @JUnitSnmpAgent(host = "10.1.2.2", port = 161, resource = SNMP_DATA_PATH + "10.1.2.2-walk.txt")
    })
    public void testManyControllers() throws Exception {
        // Add AP1 and AP2 to the default package
        addNewAccessPoint("ap1", AP1_MAC, "default");
        addNewAccessPoint("ap2", AP2_MAC, "default");

        // Add AP3 to a separate package
        addNewAccessPoint("ap3", AP3_MAC, "not-default");

        // Add 3 controllers to the default package
        addNewController("amc0", "10.1.0.2", "default");
        addNewController("amc1", "10.1.1.2", "default");
        addNewController("amc2", "10.1.2.2", "default");

        // Set the access point state on all 3 controllers
        setOidValueForAccessPoint("10.1.0.2", AP1_MAC, 0);
        setOidValueForAccessPoint("10.1.0.2", AP2_MAC, 0);

        setOidValueForAccessPoint("10.1.1.2", AP1_MAC, 1);
        setOidValueForAccessPoint("10.1.1.2", AP2_MAC, 0);

        setOidValueForAccessPoint("10.1.2.2", AP1_MAC, 0);
        setOidValueForAccessPoint("10.1.2.2", AP2_MAC, 1);

        // Anticipate the events
        anticipateApStatusEvent(AP1_MAC, "UP");
        anticipateApStatusEvent(AP2_MAC, "UP");

        // Initialize and start the daemon
        initApmdWithConfig(getMultiControllerConfig());
        m_apm.start();

        // Verify the events
        verifyAnticipated(POLLING_INTERVAL_DELTA);

        OnmsAccessPoint ap1 = m_accessPointDao.get(AP1_MAC);
        assertTrue(ap1.getStatus() == AccessPointStatus.ONLINE);
        assertEquals(InetAddressUtils.getInetAddress("10.1.1.2"), ap1.getControllerIpAddress());

        OnmsAccessPoint ap2 = m_accessPointDao.get(AP2_MAC);
        assertTrue(ap2.getStatus() == AccessPointStatus.ONLINE);
        assertEquals(InetAddressUtils.getInetAddress("10.1.2.2"), ap2.getControllerIpAddress());

        // Anticipate the events
        anticipateApStatusEvent(AP1_MAC, "UP");
        anticipateApStatusEvent(AP2_MAC, "UP");

        // Move AP1 to amc2
        setOidValueForAccessPoint("10.1.1.2", AP1_MAC, 0);
        setOidValueForAccessPoint("10.1.2.2", AP1_MAC, 1);

        // Move AP2 to amc1
        setOidValueForAccessPoint("10.1.1.2", AP2_MAC, 1);
        setOidValueForAccessPoint("10.1.2.2", AP2_MAC, 0);

        // Verify the events
        verifyAnticipated(POLLING_INTERVAL_DELTA);

        // Verify the controller address in the database
        ap1 = m_accessPointDao.get(AP1_MAC);
        assertTrue(ap1.getStatus() == AccessPointStatus.ONLINE);
        assertEquals(InetAddressUtils.getInetAddress("10.1.2.2"), ap1.getControllerIpAddress());

        ap2 = m_accessPointDao.get(AP2_MAC);
        assertTrue(ap2.getStatus() == AccessPointStatus.ONLINE);
        assertEquals(InetAddressUtils.getInetAddress("10.1.1.2"), ap2.getControllerIpAddress());
    }

    /*
     * Test the behaviour when a new controller is added after the daemon has
     * started. Verify: That the proper events are sent by the daemon.
     */
    @Test
    @JUnitSnmpAgents(value = {
        @JUnitSnmpAgent(host = "10.1.0.2", port = 161, resource = SNMP_DATA_PATH + "10.1.0.2-walk.txt"),
        @JUnitSnmpAgent(host = "10.1.1.2", port = 161, resource = SNMP_DATA_PATH + "10.1.1.2-walk.txt")
    })
    public void testAddControllerToPackage() throws Exception {
        // Add AP1 and AP2 to the default package
        addNewAccessPoint("ap1", AP1_MAC, "default");
        addNewAccessPoint("ap2", AP2_MAC, "default");

        // Add a single controller to the default package
        addNewController("amc1", "10.1.0.2", "default");

        // Set the access point state
        setOidValueForAccessPoint("10.1.0.2", AP1_MAC, 1);
        setOidValueForAccessPoint("10.1.0.2", AP2_MAC, 0);

        setOidValueForAccessPoint("10.1.1.2", AP1_MAC, 0);
        setOidValueForAccessPoint("10.1.1.2", AP2_MAC, 1);

        // Anticipate the events
        anticipateApStatusEvent(AP1_MAC, "UP");
        anticipateApStatusEvent(AP2_MAC, "DOWN");

        // Initialize and start the daemon
        initApmdWithConfig(getMultiControllerClusteredConfig());
        m_apm.start();

        // Verify the events
        verifyAnticipated(POLLING_INTERVAL_DELTA);

        // Anticipate the events
        anticipateApStatusEvent(AP1_MAC, "UP");
        anticipateApStatusEvent(AP2_MAC, "UP");

        // Add the second controller
        addNewController("amc2", "10.1.1.2", "default");

        // Verify the events
        verifyAnticipated(POLLING_INTERVAL_DELTA);
    }

    /*
     * Test the behaviour when packages are not explicitely defined in the
     * configuration file (i.e. dynamic packages) Verify: That the proper
     * events are sent by the daemon.
     */
    @Test
    @JUnitSnmpAgent(host = "10.1.0.2", resource = SNMP_DATA_PATH + "10.1.0.2-walk.txt")
    public void testDynamicPackageConfig() throws Exception {
        // Add AP1 to a new package
        addNewAccessPoint("ap1", AP1_MAC, "aruba-default-pkg");

        // Add the controller in the same package
        addNewController("amc1", "10.1.0.2", "aruba-default-pkg");

        // Set the access point state
        setOidValueForAccessPoint("10.1.0.2", AP1_MAC, 1);

        // Anticipate the events
        anticipateApStatusEvent(AP1_MAC, "UP");

        // Initialize and start the daemon
        initApmdWithConfig(getDynamicPackageConfig());
        m_apm.start();

        // The AP should be reported as UP even though the package is not
        // explicitly defined
        verifyAnticipated(POLLING_INTERVAL_DELTA);

        // Delete AP1
        OnmsAccessPoint ap1 = m_accessPointDao.get(AP1_MAC);
        m_accessPointDao.delete(ap1);
        m_accessPointDao.flush();

        // Wait for the package scan to kick in
        sleep(2000);

        // Take the AP off-line
        anticipateApStatusEvent(AP1_MAC, "DOWN");

        // No event should be generated
        sleep(POLLING_INTERVAL_DELTA);
        assertEquals("Received unexpected events", 0, m_anticipator.unanticipatedEvents().size());
        m_anticipator.reset();
    }

    private void verifyAnticipated(long millis) {
        // Verify the AP UP/DOWN events
        LOG.debug("Events we're still waiting for: {}", m_anticipator.waitForAnticipated(millis));
        LOG.debug("Unanticipated: {}", m_anticipator.unanticipatedEvents());

        assertTrue("Expected events not forthcoming", m_anticipator.waitForAnticipated(0).isEmpty());
        sleep(200);
        assertEquals("Received unexpected events", 0, m_anticipator.unanticipatedEvents().size());
        m_anticipator.reset();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Do nothing
        }
    }
		
    private String getEmptyConfig() {
    	return "<?xml version=\"1.0\"?>\n" +
    	"<access-point-monitor-configuration threads=\"30\" package-scan-interval=\"1000\">\n" +
    	"        <package name=\"default-empty\">\n" +
    	"                <filter>IPADDR = '1.1.1.1'</filter>\n" +
    	"                <service name=\"Aruba-AP-IsAdoptedOnController\" interval=\"" + POLLING_INTERVAL + "\" user-defined=\"false\" status=\"on\">\n" +
    	"                        <parameter key=\"retry\" value=\"3\"/>\n" +
    	"                        <parameter key=\"oid\" value=\".1.3.6.1.4.1.14823.2.2.1.5.2.1.4.1.19\"/>\n" +
    	"                        <parameter key=\"operator\" value=\"=\"/>\n" +
    	"                        <parameter key=\"operand\" value=\"1\"/>\n" +
    	"                        <parameter key=\"match\" value=\"true\"/>\n" +
    	"                </service>\n" +
    	"        </package>\n" +
    	"        <monitor service=\"Aruba-AP-IsAdoptedOnController\" class-name=\"org.opennms.netmgt.accesspointmonitor.poller.InstanceStrategy\" />\n" +
    	"</access-point-monitor-configuration>\n" +
    	"";
    }
    
    private String getStandardConfig() {
    	return "<?xml version=\"1.0\"?>\n" +
    	"<access-point-monitor-configuration threads=\"30\" package-scan-interval=\"1000\">\n" +
		"   	<service-template name=\"Aruba-AP-IsAdoptedOnController\" interval=\"" + POLLING_INTERVAL + "\" status=\"off\">\n" +
    	"                        <parameter key=\"timeout\" value=\"" + AGENT_TIMEOUT + "\"/>\n" +
		"                        <parameter key=\"retry\" value=\"0\"/>\n" +
    	"                        <parameter key=\"oid\" value=\".1.3.6.1.4.1.14823.2.2.1.5.2.1.4.1.19\"/>\n" +
    	"                        <parameter key=\"operator\" value=\"=\"/>\n" +
    	"                        <parameter key=\"operand\" value=\"1\"/>\n" +
    	"                        <parameter key=\"match\" value=\"true\"/>\n" +
		"		</service-template>" +
    	"       <package name=\"default\">\n" +
    	"               <filter>(IPADDR != '0.0.0.0' &amp; (IPADDR IPLIKE 10.1.0.*))</filter>\n" +
    	"               <service name=\"Aruba-AP-IsAdoptedOnController\" status=\"on\"/>" +
    	"       </package>\n" +
    	"       <monitor service=\"Aruba-AP-IsAdoptedOnController\" class-name=\"org.opennms.netmgt.accesspointmonitor.poller.InstanceStrategy\" />\n" +
    	"</access-point-monitor-configuration>\n" +
    	"";
    }

    private String getMultiControllerConfig() {
        return "<?xml version=\"1.0\"?>\n" +
    	"<access-point-monitor-configuration threads=\"30\" package-scan-interval=\"1000\">\n" +
    	"        <package name=\"default\">\n" +
    	"                <filter>(IPADDR != '0.0.0.0' &amp; (IPADDR IPLIKE 10.1.*.*))</filter>\n" +
    	"                <service name=\"Aruba-AP-IsAdoptedOnController\" interval=\"" + POLLING_INTERVAL + "\" user-defined=\"false\" status=\"on\">\n" +
    	"                        <parameter key=\"retry\" value=\"3\"/>\n" +
    	"                        <parameter key=\"oid\" value=\".1.3.6.1.4.1.14823.2.2.1.5.2.1.4.1.19\"/>\n" +
    	"                        <parameter key=\"operator\" value=\"=\"/>\n" +
    	"                        <parameter key=\"operand\" value=\"1\"/>\n" +
    	"                        <parameter key=\"match\" value=\"true\"/>\n" +
    	"                </service>\n" +
    	"        </package>\n" +
    	"        <monitor service=\"Aruba-AP-IsAdoptedOnController\" class-name=\"org.opennms.netmgt.accesspointmonitor.poller.InstanceStrategy\" />\n" +
    	"</access-point-monitor-configuration>\n" +
    	"";
    }
    
    private String getMultiControllerClusteredConfig() {
    	return "<?xml version=\"1.0\"?>\n" +
    	"<access-point-monitor-configuration threads=\"30\" package-scan-interval=\"1000\">\n" +
    	"        <package name=\"default\">\n" +
    	"                <filter>(IPADDR != '0.0.0.0' &amp; (IPADDR IPLIKE 10.1.4.*))</filter>\n" +
    	"                <service name=\"Aruba-AP-IsAdoptedOnController\" interval=\"" + POLLING_INTERVAL + "\" user-defined=\"false\" status=\"on\">\n" +
    	"                        <parameter key=\"retry\" value=\"3\"/>\n" +
    	"                        <parameter key=\"oid\" value=\".1.3.6.1.4.1.14823.2.2.1.5.2.1.4.1.19\"/>\n" +
    	"                        <parameter key=\"operator\" value=\"=\"/>\n" +
    	"                        <parameter key=\"operand\" value=\"1\"/>\n" +
    	"                        <parameter key=\"match\" value=\"true\"/>\n" +
    	"                </service>\n" +
    	"        </package>\n" +
    	"        <monitor service=\"Aruba-AP-IsAdoptedOnController\" class-name=\"org.opennms.netmgt.accesspointmonitor.poller.InstanceStrategy\" />\n" +
    	"</access-point-monitor-configuration>\n" +
    	"";
    }

    private String getDynamicPackageConfig() {
        return "<?xml version=\"1.0\"?>" +
        "<access-point-monitor-configuration threads=\"30\" package-scan-interval=\"1000\">" +
        "	<service-template name=\"Aruba-IsAPAdoptedOnController\" interval=\"" + POLLING_INTERVAL + "\" status=\"off\">" +
        "		<parameter key=\"retry\" value=\"3\"/>" +
        "		<parameter key=\"oid\" value=\".1.3.6.1.4.1.14823.2.2.1.5.2.1.4.1.19\"/>" +
        "		<parameter key=\"operator\" value=\"=\"/>" +
        "		<parameter key=\"operand\" value=\"1\"/>" +
        "		<parameter key=\"match\" value=\"true\"/>" +
        "	</service-template>" +
        "	<package name=\"aruba-%\">" +
        "		<filter>(IPADDR != '0.0.0.0' &amp; (pollerCategory == '%packageName%'))</filter>" +
        "		<service name=\"Aruba-IsAPAdoptedOnController\" status=\"on\"/>" +
        "	</package>" +
        "	<monitor service=\"Aruba-IsAPAdoptedOnController\" class-name=\"org.opennms.netmgt.accesspointmonitor.poller.InstanceStrategy\" />" +
        "</access-point-monitor-configuration>";
    }
}
