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
package org.opennms.netmgt.collectd;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.rpc.mock.MockEntityScopeProvider;
import org.opennms.core.test.Level;
import org.opennms.core.test.LoggingEvent;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.InsufficientInformationException;
import org.opennms.netmgt.collection.support.DefaultServiceCollectorRegistry;
import org.opennms.netmgt.collection.test.api.CollectorTestUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.dao.outages.api.OverrideablePollOutagesDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockPersisterFactory;
import org.opennms.netmgt.mock.MockThresholdingService;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test class for <a href="http://issues.opennms.org/browse/NMS-6226">NMS-6226</a>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml"
})
@JUnitConfigurationEnvironment
public class DuplicatePrimaryAddressIT {
    private static final Logger LOG = LoggerFactory.getLogger(DuplicatePrimaryAddressIT.class);

    /** The event IPC manager instance. */
    private EventIpcManager m_eventIpcManager;

    /** The Collectd instance. */
    private Collectd m_collectd;

    /** The Collectd configuration factory instance. */
    private CollectdConfigFactory m_collectdConfigFactory;

    /** The IP Interface DAO instance. */
    private IpInterfaceDao m_ifaceDao;

    /** The Node DAO instance. */
    private NodeDao m_nodeDao;

    /** The Filter DAO instance. */
    private FilterDao m_filterDao;

    private ThresholdingService m_thresholdingService;

    @Autowired
    private OverrideablePollOutagesDao m_pollOutagesDao;

    @Before
    public void setUp() {
        MockServiceCollector.setDelegate(null);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(m_filterDao);
        verifyNoMoreInteractions(m_collectdConfigFactory);
        verifyNoMoreInteractions(m_nodeDao);
        verifyNoMoreInteractions(m_ifaceDao);
    }

    /**
     * Test existing.
     * <p>This test assumes that there are two nodes on the database sharing the same primary address.</p>
     *
     * @throws Exception the exception
     */
    @Test
    public void testExisting() throws Exception {
        initialize(true);
        verify();

        Mockito.verify(m_filterDao, atLeastOnce()).flushActiveIpAddressListCache();
        Mockito.verify(m_collectdConfigFactory, atLeastOnce()).getCollectors();
        Mockito.verify(m_collectdConfigFactory, atLeastOnce()).getPackages();
        Mockito.verify(m_collectdConfigFactory, atLeastOnce()).interfaceInPackage(any(OnmsIpInterface.class), any(Package.class));
        Mockito.verify(m_collectdConfigFactory, atLeastOnce()).getThreads();
        Mockito.verify(m_ifaceDao, atLeastOnce()).findByServiceType(anyString());
        Mockito.verify(m_ifaceDao, atLeastOnce()).load(anyInt());
    }

    /**
     * Test on demand.
     * <p>This test assumes that the database is empty. The data collection will be triggered through events.<p>
     *
     * @throws Exception the exception
     */
    @Test
    public void testOnDemand() throws Exception {
        initialize(false);

        EventBuilder nodeGained = new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Test");
        nodeGained.setInterface(InetAddressUtils.addr("192.168.1.1"));
        nodeGained.setService("SNMP");

        EventBuilder reinitialize = new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Test");
        reinitialize.setInterface(InetAddressUtils.addr("192.168.1.1"));

        // Emulate Provisiond events for node1
        nodeGained.setNodeid(1);
        reinitialize.setNodeid(1);
        m_collectd.onEvent(ImmutableMapper.fromMutableEvent(nodeGained.getEvent()));
        m_collectd.onEvent(ImmutableMapper.fromMutableEvent(reinitialize.getEvent()));

        // Emulate Provisiond events for node3
        nodeGained.setNodeid(3);
        reinitialize.setNodeid(3);
        m_collectd.onEvent(ImmutableMapper.fromMutableEvent(nodeGained.getEvent()));
        m_collectd.onEvent(ImmutableMapper.fromMutableEvent(reinitialize.getEvent()));

        verify();

        Mockito.verify(m_filterDao, times(3)).flushActiveIpAddressListCache();
        Mockito.verify(m_collectdConfigFactory, atLeastOnce()).getCollectors();
        Mockito.verify(m_collectdConfigFactory, atLeastOnce()).getPackages();
        Mockito.verify(m_collectdConfigFactory, atLeastOnce()).interfaceInPackage(any(OnmsIpInterface.class), any(Package.class));
        Mockito.verify(m_collectdConfigFactory, atLeastOnce()).getThreads();
        Mockito.verify(m_ifaceDao, atLeastOnce()).findByServiceType(anyString());
        Mockito.verify(m_ifaceDao, atLeastOnce()).load(anyInt());
        Mockito.verify(m_nodeDao, atLeastOnce()).load(anyInt());
    }

    /**
     * Initialize.
     *
     * @param scheduleExistingNodes true, to emulate having data on the database
     * @throws Exception the exception
     */
    private void initialize(boolean scheduleExistingNodes) throws Exception {
        MockLogAppender.setupLogging();

        m_eventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);

        List<InetAddress> m_addrs = new ArrayList<>();
        m_addrs.add(InetAddressUtils.getInetAddress("192.168.1.1"));
        m_filterDao = mock(FilterDao.class);
        when(m_filterDao.getActiveIPAddressList("IPADDR IPLIKE *.*.*.*")).thenReturn(m_addrs);
        FilterDaoFactory.setInstance(m_filterDao);

        Resource resource = new ClassPathResource("etc/poll-outages.xml");
        m_pollOutagesDao.overrideConfig(resource.getInputStream());

        File homeDir = resource.getFile().getParentFile().getParentFile();
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        Collector collector = new Collector();
        collector.setService("SNMP");
        collector.setClassName(MockServiceCollector.class.getName());

        m_collectdConfigFactory = mock(CollectdConfigFactory.class);
        when(m_collectdConfigFactory.getCollectors()).thenReturn(Collections.singletonList(collector));
        when(m_collectdConfigFactory.getThreads()).thenReturn(2);

        m_ifaceDao = mock(IpInterfaceDao.class);
        m_nodeDao = mock(NodeDao.class);
        m_thresholdingService = new MockThresholdingService();

        m_collectd = new Collectd() {
            @Override
            protected void handleInsufficientInfo(InsufficientInformationException e) {
                Assert.fail("Invalid event received: "+ e.getMessage());
            }
        };

        m_collectd.setEntityScopeProvider(new MockEntityScopeProvider());

        OnmsServiceType snmp = new OnmsServiceType("SNMP");
        NetworkBuilder netBuilder = new NetworkBuilder();

        OnmsNode n1 = netBuilder.addNode("node1").setId(1).setForeignSource("TestA").setForeignId("node1").getNode();
        OnmsIpInterface ip1 = netBuilder.addSnmpInterface(1).addIpInterface("192.168.1.1").setId(2).setIsSnmpPrimary("P").getInterface();
        netBuilder.addService(snmp);

        OnmsNode n2 = netBuilder.addNode("node2").setId(3).setForeignSource("TestB").setForeignId("node2").getNode();
        OnmsIpInterface ip2 = netBuilder.addSnmpInterface(1).addIpInterface("192.168.1.1").setId(4).setIsSnmpPrimary("P").getInterface();
        netBuilder.addService(snmp);

        Assert.assertNotSame(ip1.getNode().getId(), ip2.getNode().getId());

        List<OnmsIpInterface> initialIfs = new ArrayList<>();
        if (scheduleExistingNodes) {
            initialIfs.add(ip1);
            initialIfs.add(ip2);
        }
        when(m_ifaceDao.findByServiceType(snmp.getName())).thenReturn(initialIfs);

        m_filterDao.flushActiveIpAddressListCache();

        when(m_nodeDao.load(1)).thenReturn(n1);
        when(m_nodeDao.load(3)).thenReturn(n2);

        createGetPackagesExpectation();

        when(m_ifaceDao.load(2)).thenReturn(ip1);
        when(m_ifaceDao.load(4)).thenReturn(ip2);

        when(m_filterDao.getActiveIPAddressList(anyString())).thenReturn(Arrays.asList(InetAddressUtils.addr("192.168.1.1")));

        final MockTransactionTemplate transTemplate = new MockTransactionTemplate();
        transTemplate.afterPropertiesSet();

        m_collectd.setCollectdConfigFactory(m_collectdConfigFactory);
        m_collectd.setEventIpcManager(m_eventIpcManager);
        m_collectd.setTransactionTemplate(transTemplate);
        m_collectd.setIpInterfaceDao(m_ifaceDao);
        m_collectd.setNodeDao(m_nodeDao);
        m_collectd.setFilterDao(m_filterDao);
        m_collectd.setThresholdingService(m_thresholdingService);
        m_collectd.setPersisterFactory(new MockPersisterFactory());
        m_collectd.setServiceCollectorRegistry(new DefaultServiceCollectorRegistry());
        m_collectd.setLocationAwareCollectorClient(CollectorTestUtils.createLocationAwareCollectorClient());
        m_collectd.setPollOutagesDao(m_pollOutagesDao);

        m_collectd.afterPropertiesSet();
        m_collectd.start();
    }

    /**
     * Verify.
     * <p>The data collection should be performed for each node, no matter if they are using the same primary address.</p>
     *
     * @throws Exception the exception
     */
    private void verify() throws Exception {
        int successfulCollections = 5; // At least 5 collections must be performed for the above wait time.

        await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(100)).untilAsserted(() -> {
                Assert.assertTrue(successfulCollections <= countMessages("collector.collect: begin:testPackage/1/192.168.1.1/SNMP"));
                Assert.assertTrue(successfulCollections <= countMessages("collector.collect: end:testPackage/1/192.168.1.1/SNMP"));
                Assert.assertTrue(successfulCollections <= countMessages("collector.collect: begin:testPackage/3/192.168.1.1/SNMP"));
                Assert.assertTrue(successfulCollections <= countMessages("collector.collect: end:testPackage/3/192.168.1.1/SNMP"));                
        });

        m_collectd.stop();
        MockLogAppender.assertNoWarningsOrGreater();

    }

    private int countMessages(String message) {
        int c = 0;
        for (LoggingEvent event : MockLogAppender.getEventsAtLevel(Level.INFO)) {
            if (event.getLevel().eq(Level.INFO) && event.getMessage().contains(message)) {
                c++;;
            }
        }
        LOG.info("Message {} found {} times.", message, c);
        return c;
    }

    /**
     * Creates the get packages expectation.
     */
    private void createGetPackagesExpectation() {
        final Package pkg = new Package();
        pkg.setName("testPackage");
        Filter filter = new Filter();
        filter.setContent("ipaddr != '0.0.0.0'");
        pkg.setFilter(filter);

        final Service collector = new Service();
        collector.setName("SNMP");
        collector.setStatus("on");
        collector.setInterval(1000l);
        collector.addParameter("thresholding-enabled", "false");
        pkg.addService(collector);

        when(m_collectdConfigFactory.getPackages()).thenReturn(Collections.singletonList(pkg));
        when(m_collectdConfigFactory.interfaceInPackage(any(OnmsIpInterface.class), eq(pkg))).thenReturn(true);
    }

}
