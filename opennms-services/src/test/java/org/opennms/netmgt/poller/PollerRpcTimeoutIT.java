/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockPersisterFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.poller.pollables.PollableNetwork;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-jms.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-icmp.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-poller.xml",
        "classpath:/META-INF/opennms/applicationContext-pollerd.xml",
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-test-deviceConfig.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.netmgt.icmp.pingerClass=org.opennms.netmgt.icmp.jna.JnaPinger",
        // Add immediate timeouts
        "org.opennms.jms.timeout=1",
        // Store the ActiveMQ kahadb in the target directory
        // which we delete before the test executes
        "activemq.data=" + PollerRpcTimeoutIT.ACTIVEMQ_STORAGE_DIRECTORY
})
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class PollerRpcTimeoutIT implements TemporaryDatabaseAware<MockDatabase> {

    public static final String ACTIVEMQ_STORAGE_DIRECTORY = "target/activemq";
    public static final String NONEXISTENT_LOCATION = "DOESNT_EXIST";

    private Poller m_poller;

    private MockNetwork m_network;

    private MockDatabase m_db;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Autowired
    private QueryManager m_queryManager;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private MonitoringLocationDao m_monitoringLocationDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private TransactionTemplate m_transactionTemplate;

    @Autowired
    private LocationAwarePollerClient m_locationAwarePollerClient;

    @Autowired
    private LocationAwarePingClient m_locationAwarePingClient;
    
    @Autowired
    private ReadablePollOutagesDao m_pollOutagesDao;

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_db = database;
    }

    private void startDaemons() {
        m_poller.init();
        m_poller.start();
    }

    private void stopDaemons() {
        if (m_poller != null) {
            m_poller.stop();
        }
    }

    /**
     * Clean up the activemq persistence directory before and after
     * the test.
     * 
     * @throws Exception
     */
    @BeforeClass
    @AfterClass
    public static void deleteActiveMqStorage() throws Exception {
        FileUtils.deleteDirectory(new File(ACTIVEMQ_STORAGE_DIRECTORY));
    }

    @Before
    @Transactional
    public void setUp() throws Exception {

        MockUtil.println("------------ Begin Test  --------------------------");
        MockLogAppender.setupLogging();

        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface(str(InetAddressUtils.UNPINGABLE_ADDRESS));
        m_network.addService("ICMP");
        m_network.addService("HTTP");

        m_db.populate(m_network);
        DataSourceFactory.setInstance(m_db);

        // Add a location that no systems are monitoring
        OnmsMonitoringLocation location = new OnmsMonitoringLocation(NONEXISTENT_LOCATION, "Nullsville");
        m_monitoringLocationDao.save(location);

        // Update all of the nodes to have the nonexistent location
        for (OnmsNode node : m_nodeDao.findAll()) {
            node.setLocation(location);
            m_nodeDao.saveOrUpdate(node);
        }
        m_nodeDao.flush();

        InputStream is = new FileInputStream(new File("src/test/resources/etc/rpctimeout-poller-configuration.xml"));
        PollerConfigFactory factory = new PollerConfigFactory(0, is);
        PollerConfigFactory.setInstance(factory);
        IOUtils.closeQuietly(is);

        // Sanity check the config
        ServiceMonitor monitor = PollerConfigFactory.getInstance().getServiceMonitorLocator("HTTP").orElseThrow()
                .getServiceMonitor(PollerConfigFactory.getInstance().getServiceMonitorRegistry());
        Assert.assertNotNull(monitor);
        Package pkg = PollerConfigFactory.getInstance().getPackage("PollerRpcTimeoutIT");
        Assert.assertNotNull(pkg);
        Service svc = pkg.getServices().iterator().next();
        Assert.assertEquals("HTTP", svc.getName());

        DefaultPollContext pollContext = new DefaultPollContext();
        pollContext.setEventManager(m_eventMgr);
        pollContext.setLocalHostName("localhost");
        pollContext.setName("Test.DefaultPollContext");
        pollContext.setPollerConfig(factory);
        pollContext.setQueryManager(m_queryManager);
        pollContext.setLocationAwarePingClient(m_locationAwarePingClient);

        PollableNetwork network = new PollableNetwork(pollContext);

        m_poller = new Poller();
        m_poller.setMonitoredServiceDao(m_monitoredServiceDao);
        m_poller.setOutageDao(m_outageDao);
        m_poller.setTransactionTemplate(m_transactionTemplate);
        m_poller.setEventIpcManager(m_eventMgr);
        m_poller.setNetwork(network);
        m_poller.setQueryManager(m_queryManager);
        m_poller.setPollerConfig(factory);
        m_poller.setLocationAwarePollerClient(m_locationAwarePollerClient);
        m_poller.setPollOutagesDao(m_pollOutagesDao);
        m_poller.setServiceMonitorAdaptor((service, parameters, status) -> status);
        m_poller.setPersisterFactory(new MockPersisterFactory());
    }

    @After
    public void tearDown() throws Exception {
        m_eventMgr.finishProcessingEvents();
        MockUtil.println("------------ End Test  --------------------------");
    }

    @Test
    public void testPolling() throws Exception {

        String queueName = new JmsQueueNameFactory("RPC", "Poller", NONEXISTENT_LOCATION).getName();

        try {
            ManagementFactory.getPlatformMBeanServer().getObjectInstance(
                ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=ActiveMQ.DLQ")
            );
            fail("DLQ was unexpected found in the PlatformMBeanServer");
        } catch (InstanceNotFoundException e) {
            // Expected: the queue hasn't been created yet
        }

        try {
            ManagementFactory.getPlatformMBeanServer().getObjectInstance(
                ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=" + queueName)
            );
            fail(queueName + " queue was unexpected found in the PlatformMBeanServer");
        } catch (InstanceNotFoundException e) {
            // Expected: the queue hasn't been created yet
        }

        // Start the poller: polls will timeout because there is no consumer for the location
        startDaemons();

        // Sleep long enough so that messages would be flushed to the DLQ
        Thread.sleep(60000);

        // Stop the poller daemon
        stopDaemons();

        try {
            ManagementFactory.getPlatformMBeanServer().getObjectInstance(
                ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=ActiveMQ.DLQ") 
            );
            fail("DLQ was unexpected found in the PlatformMBeanServer");
        } catch (InstanceNotFoundException e) {
            // Expected: the DLQ still should be absent
        }

        Long dequeueCount = (Long)ManagementFactory.getPlatformMBeanServer().getAttribute(
            ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=" + queueName), 
            "DequeueCount"
        );
        Long expiredCount = (Long)ManagementFactory.getPlatformMBeanServer().getAttribute(
            ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=" + queueName), 
            "ExpiredCount"
        );
        Long dispatchCount = (Long)ManagementFactory.getPlatformMBeanServer().getAttribute(
            ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=" + queueName), 
            "DispatchCount"
        );

        assertTrue("No expired messages were present", expiredCount > 0L);
        assertEquals("Dequeued messages do not equal expired messages", expiredCount, dequeueCount);
        assertEquals("Dispatched message count was not zero", 0, dispatchCount.intValue());
    }
}
