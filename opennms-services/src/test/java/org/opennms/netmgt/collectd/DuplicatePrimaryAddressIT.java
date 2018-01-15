/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.Level;
import org.opennms.core.test.LoggingEvent;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.InsufficientInformationException;
import org.opennms.netmgt.collection.support.DefaultServiceCollectorRegistry;
import org.opennms.netmgt.collection.test.api.CollectorTestUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockPersisterFactory;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.mock.EasyMockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Test class for <a href="http://issues.opennms.org/browse/NMS-6226">NMS-6226</a>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class DuplicatePrimaryAddressIT {
    private static final Logger LOG = LoggerFactory.getLogger(DuplicatePrimaryAddressIT.class);

    /** The event IPC manager instance. */
    private EventIpcManager m_eventIpcManager;

    /** The Collectd instance. */
    private Collectd m_collectd;

    /** The Mock Utils instance. */
    private EasyMockUtils m_mockUtils;

    /** The Collectd configuration factory instance. */
    private CollectdConfigFactory m_collectdConfigFactory;

    /** The Collectd configuration instance. */
    private CollectdConfiguration m_collectdConfiguration;

    /** The IP Interface DAO instance. */
    private IpInterfaceDao m_ifaceDao;

    /** The Node DAO instance. */
    private NodeDao m_nodeDao;

    /** The Filter DAO instance. */
    private FilterDao m_filterDao;

    @Before
    public void setUp() {
        MockServiceCollector.setDelegate(null);
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
        m_collectd.onEvent(nodeGained.getEvent());
        m_collectd.onEvent(reinitialize.getEvent());

        // Emulate Provisiond events for node3
        nodeGained.setNodeid(3);
        reinitialize.setNodeid(3);
        m_collectd.onEvent(nodeGained.getEvent());
        m_collectd.onEvent(reinitialize.getEvent());

        verify();
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

        m_mockUtils = new EasyMockUtils();

        m_filterDao = m_mockUtils.createMock(FilterDao.class);
        FilterDaoFactory.setInstance(m_filterDao);

        Resource resource = new ClassPathResource("etc/poll-outages.xml");
        PollOutagesConfigFactory factory = new PollOutagesConfigFactory(resource);
        factory.afterPropertiesSet();
        PollOutagesConfigFactory.setInstance(factory);

        File homeDir = resource.getFile().getParentFile().getParentFile();
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        Collector collector = new Collector();
        collector.setService("SNMP");
        collector.setClassName(MockServiceCollector.class.getName());

        m_collectdConfigFactory = m_mockUtils.createMock(CollectdConfigFactory.class);
        m_collectdConfiguration = m_mockUtils.createMock(CollectdConfiguration.class);
        EasyMock.expect(m_collectdConfigFactory.getCollectdConfig()).andReturn(m_collectdConfiguration).anyTimes();
        EasyMock.expect(m_collectdConfiguration.getCollectors()).andReturn(Collections.singletonList(collector)).anyTimes();
        EasyMock.expect(m_collectdConfiguration.getThreads()).andReturn(2).anyTimes();

        m_ifaceDao = m_mockUtils.createMock(IpInterfaceDao.class);
        m_nodeDao = m_mockUtils.createMock(NodeDao.class);

        m_collectd = new Collectd() {
            @Override
            protected void handleInsufficientInfo(InsufficientInformationException e) {
                Assert.fail("Invalid event received: "+ e.getMessage());
            }
        };

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
        EasyMock.expect(m_ifaceDao.findByServiceType(snmp.getName())).andReturn(initialIfs).anyTimes();

        m_filterDao.flushActiveIpAddressListCache();
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(m_nodeDao.load(1)).andReturn(n1).anyTimes();
        EasyMock.expect(m_nodeDao.load(3)).andReturn(n2).anyTimes();

        createGetPackagesExpectation();

        EasyMock.expect(m_ifaceDao.load(2)).andReturn(ip1).anyTimes();
        EasyMock.expect(m_ifaceDao.load(4)).andReturn(ip2).anyTimes();

        m_mockUtils.replayAll();

        final MockTransactionTemplate transTemplate = new MockTransactionTemplate();
        transTemplate.afterPropertiesSet();

        m_collectd.setCollectdConfigFactory(m_collectdConfigFactory);
        m_collectd.setEventIpcManager(m_eventIpcManager);
        m_collectd.setTransactionTemplate(transTemplate);
        m_collectd.setIpInterfaceDao(m_ifaceDao);
        m_collectd.setNodeDao(m_nodeDao);
        m_collectd.setFilterDao(m_filterDao);
        m_collectd.setPersisterFactory(new MockPersisterFactory());
        m_collectd.setServiceCollectorRegistry(new DefaultServiceCollectorRegistry());
        m_collectd.setLocationAwareCollectorClient(CollectorTestUtils.createLocationAwareCollectorClient());

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
        Thread.sleep(10000);
        int successfulCollections = 5; // At least 5 collections must be performed for the above wait time.
        m_collectd.stop();
        m_mockUtils.verifyAll();
        MockLogAppender.assertNoWarningsOrGreater();

        Assert.assertTrue(successfulCollections <= countMessages("collector.collect: begin:testPackage/1/192.168.1.1/SNMP"));
        Assert.assertTrue(successfulCollections <= countMessages("collector.collect: end:testPackage/1/192.168.1.1/SNMP"));
        Assert.assertTrue(successfulCollections <= countMessages("collector.collect: begin:testPackage/3/192.168.1.1/SNMP"));
        Assert.assertTrue(successfulCollections <= countMessages("collector.collect: end:testPackage/3/192.168.1.1/SNMP"));
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

        EasyMock.expect(m_collectdConfiguration.getPackages()).andReturn(Collections.singletonList(pkg)).anyTimes();
        EasyMock.expect(m_collectdConfigFactory.interfaceInPackage(anyObject(OnmsIpInterface.class), eq(pkg))).andReturn(true).anyTimes();
    }

}
