/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.collection.api.AbstractServiceCollector;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.support.DefaultServiceCollectorRegistry;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.dao.outages.api.OverrideablePollOutagesDao;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThresholdingDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockPersisterFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.ThresholdingServiceImpl;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/applicationContext-testPostgresBlobStore.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-collector.xml",
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml"
})
@JUnitConfigurationEnvironment(systemProperties={// We don't need a real pinger here
        "org.opennms.netmgt.icmp.pingerClass=org.opennms.netmgt.icmp.NullPinger"})
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
@DirtiesContext
public class ThresholdIT implements TemporaryDatabaseAware<MockDatabase> {

    private CollectdConfigFactory collectdConfigFactory;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private IpInterfaceDao ifaceDao;

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private DefaultServiceCollectorRegistry serviceCollectorRegistry;

    @Autowired
    private LocationAwareCollectorClient locationAwareCollectorClient;

    @Autowired
    private TransactionTemplate transTemplate;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private MockEventIpcManager mockEventIpcManager;

    private PersisterFactory persisterFactory = new MockPersisterFactory();

    private MockDatabase mockDatabase;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Autowired
    private ThresholdingService thresholdingService;
    
    @Autowired
    private OverrideableThreshdDao threshdDao;
    
    @Autowired
    private OverrideableThresholdingDao thresholdingDao;
    
    @Autowired
    private OverrideablePollOutagesDao pollOutagesDao;

    @Test
    public void canTriggerThreshold() throws Exception {
        // Load our custom config
        try (InputStream in = ConfigurationTestUtils.getInputStreamForResource(this, "collectd-with-mock-collector.xml")) {
            collectdConfigFactory = new CollectdConfigFactory(in);
        }

        // Register our collector as the delegate for the mock
        MyServiceCollector collector = new MyServiceCollector(sessionUtils);
        MockServiceCollector.setDelegate(collector);

        // Load custom threshd configuration
        initThreshdFactories("threshd-configuration.xml","test-thresholds.xml");
        threshdDao.rebuildPackageIpListMap();
        mockEventIpcManager.addEventListener((EventListener) thresholdingService, ThresholdingServiceImpl.UEI_LIST);

        // Wire and initialize collectd
        Collectd collectd = new Collectd();
        collectd.setCollectdConfigFactory(collectdConfigFactory);
        collectd.setIpInterfaceDao(ifaceDao);
        collectd.setFilterDao(filterDao);
        collectd.setServiceCollectorRegistry(serviceCollectorRegistry);
        collectd.setLocationAwareCollectorClient(locationAwareCollectorClient);
        collectd.setTransactionTemplate(transTemplate);
        collectd.setNodeDao(nodeDao);
        collectd.setEventIpcManager(mockEventIpcManager);
        collectd.setPersisterFactory(persisterFactory);
        collectd.setThresholdingService(thresholdingService);
        collectd.setPollOutagesDao(pollOutagesDao);
        collectd.init();
        collectd.start();

        // Now let's add a node
        MockNetwork mockNetwork = new MockNetwork();
        mockNetwork.addNode(1, "Router");
        mockNetwork.addInterface("192.168.1.1");
        mockNetwork.addService("Mock");
        mockDatabase.populate(mockNetwork);

        // Set the sysObjectId
        transTemplate.execute(status -> {
            OnmsNode node = nodeDao.get(1);
            node.setSysObjectId(".1.3.6.1.4.1.8072.3.2.10");
            nodeDao.update(node);
            return node;
        });

        EventAnticipator eventAnticipator = mockEventIpcManager.getEventAnticipator();

        // Let's send a nodeGainedService event
        EventBuilder bldr = new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Test");
        bldr.setNodeid(1);
        bldr.setInterface(addr("192.168.1.1"));
        bldr.setService("Mock");
        Event nodeGainedServiceEvent = bldr.getEvent();
        eventAnticipator.anticipateEvent(nodeGainedServiceEvent);
        mockEventIpcManager.sendNow(nodeGainedServiceEvent);

        // Assert 2 collections are performed and Threshold is not triggered
        collector.resetLatch(2);
        if (!collector.getLatch().await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Collector was not called!");
        }
        assertEquals(0, eventAnticipator.getUnanticipatedEvents().size());

        // Anticipate the high threshold event
        eventAnticipator.reset();
        EventBuilder threshBldr = new EventBuilder(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "Test");
        threshBldr.setNodeid(1);
        threshBldr.setInterface(addr("192.168.1.1"));
        threshBldr.setService("Mock");
        eventAnticipator.anticipateEvent(threshBldr.getEvent());

        // Add the 'Development' category
        transTemplate.execute(status -> {
            OnmsNode node = nodeDao.get(1);
            node.addCategory(categoryDao.findByName("Development"));
            nodeDao.update(node);
            return node;
        });

        // Let's send a nodeCategoryChange event
        bldr = new EventBuilder(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI, "Test");
        bldr.setNodeid(1);
        bldr.setInterface(addr("192.168.1.1"));
        Event nodeCategoryChangeEvent = bldr.getEvent();
        eventAnticipator.anticipateEvent(nodeCategoryChangeEvent);
        mockEventIpcManager.sendNow(nodeCategoryChangeEvent);

        // Now wait until our collector was called
        collector.resetLatch(1);
        if (!collector.getLatch().await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Collector was not called!");
        }

        // Wait until our threshold was triggered - the anticipator will remove the event from the list once received
        await().atMost(30, TimeUnit.SECONDS).until(eventAnticipator::getAnticipatedEvents, hasSize(0));

        eventAnticipator.reset();

        // Remove the 'Development' category
        transTemplate.execute(status -> {
            OnmsNode node = nodeDao.get(1);
            node.removeCategory(categoryDao.findByName("Development"));
            nodeDao.update(node);
            return node;
        });

        // Let's send a nodeCategoryChange event
        bldr = new EventBuilder(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI, "Test");
        bldr.setNodeid(1);
        bldr.setInterface(addr("192.168.1.1"));
        nodeCategoryChangeEvent = bldr.getEvent();
        eventAnticipator.anticipateEvent(nodeCategoryChangeEvent);
        mockEventIpcManager.sendNow(nodeCategoryChangeEvent);

        eventAnticipator.reset();

        // Again, Assert 2 collections are performed and that Threshold is no longer triggered
        collector.resetLatch(2);
        if (!collector.getLatch().await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Collector was not called!");
        }
        assertEquals(0, eventAnticipator.getUnanticipatedEvents().size());

        // Stop collectd gracefully so we don't keep trying to collect during the tear down
        collectd.stop();
    }

    private void initThreshdFactories(String threshd, String thresholds) throws Exception {
        thresholdingDao.overrideConfig(getClass().getResourceAsStream(thresholds));
        threshdDao.overrideConfig(getClass().getResourceAsStream(threshd));
    }

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        mockDatabase = database;
    }

    private static class MyServiceCollector extends AbstractServiceCollector {

        private final SessionUtils sessionUtils;

        private CountDownLatch latch = new CountDownLatch(1);


        public MyServiceCollector(SessionUtils sessionUtils) {
            this.sessionUtils = Objects.requireNonNull(sessionUtils);
        }

        @Override
        public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
            final CollectionSet collectionSet = sessionUtils.withReadOnlyTransaction(() -> {
                final NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId());
                return new CollectionSetBuilder(agent)
                        .withNumericAttribute(nodeLevelResource, "systemMem", "freeMem", 10001d, AttributeType.GAUGE)
                        .build();
            });
            // Count down the latch after we build the collection set in case an error occurs while building
            latch.countDown();
            return collectionSet;
        }

        @Override
        public RrdRepository getRrdRepository(String collectionName) {
            final RrdRepository rrdRepository = new RrdRepository();
            rrdRepository.setStep(5000);
            rrdRepository.setHeartBeat(2 * rrdRepository.getStep());
            rrdRepository.setRraList(Lists.newArrayList("RRA:AVERAGE:0.5:1:2016"));
            rrdRepository.setRrdBaseDir(new File("snmp"));
            return rrdRepository;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        public void resetLatch(int count) {
            latch = new CountDownLatch(count);
        }
    }
}
