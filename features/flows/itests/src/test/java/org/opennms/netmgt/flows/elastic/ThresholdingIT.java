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
package org.opennms.netmgt.flows.elastic;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collectd.DefaultResourceTypeMapper;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.core.DefaultCollectionAgentFactory;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThresholdingDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfo;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.exception.InvalidFilterException;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.processing.ProcessingOptions;
import org.opennms.netmgt.flows.processing.impl.FlowThresholdingImpl;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import com.google.common.collect.Lists;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-shared.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/applicationContext-testPostgresBlobStore.xml",
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-utils.xml",
        "classpath:/META-INF/opennms/applicationContext-jceks-scv.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase()
public class ThresholdingIT {
    @Autowired
    private ThresholdingService thresholdingService;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OverrideableThresholdingDao thresholdingDao;

    @Autowired
    private OverrideableThreshdDao threshdDao;

    @Autowired
    private MockEventIpcManager mockEventIpcManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PersisterFactory persisterFactory;

    private List<org.opennms.netmgt.flows.classification.persistence.api.Rule> rules;

    @Autowired
    private FilterDao filterDao;

    private FlowThresholdingImpl thresholding;

    @Before
    public void before() throws Exception {
        this.rules = Lists.newArrayList(
                new RuleBuilder().withName("APP1").withDstPort("1").withPosition(1).build(),
                new RuleBuilder().withName("APP2").withDstPort("2").withPosition(1).build()
        );

        this.applicationContext.getAutowireCapableBeanFactory().createBean(DefaultResourceTypeMapper.class);

        BeanUtils.assertAutowiring(this);

        this.databasePopulator.populateDatabase();

        this.thresholdingDao.overrideConfig(getClass().getResourceAsStream("/thresholds.xml"));
        this.threshdDao.overrideConfig(getClass().getResourceAsStream("/threshd-configuration.xml"));

        this.threshdDao.rebuildPackageIpListMap();

        final ClassificationRuleProvider classificationRuleProvider = () -> rules;
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(classificationRuleProvider, FilterService.NOOP);

        final var collectionAgentFactory = new DefaultCollectionAgentFactory();
        collectionAgentFactory.setNodeDao(this.databasePopulator.getNodeDao());
        collectionAgentFactory.setIpInterfaceDao(this.databasePopulator.getIpInterfaceDao());
        collectionAgentFactory.setPlatformTransactionManager(this.transactionTemplate.getTransactionManager());

        this.thresholdingService.getThresholdingSetPersister().reinitializeThresholdingSets();

        final FilterService filterService = new FilterService() {
            @Override
            public void validate(String filterExpression) throws InvalidFilterException {
            }

            @Override
            public boolean matches(String address, String filterExpression) {
                return true;
            }
        };

        this.thresholding = new FlowThresholdingImpl(this.thresholdingService,
                                                 collectionAgentFactory,
                                                 this.persisterFactory,
                                                 this.databasePopulator.getIpInterfaceDao(),
                                                 this.databasePopulator.getDistPollerDao(),
                                                 this.databasePopulator.getSnmpInterfaceDao(),
                                                 Mockito.mock(FilterDao.class),
                                                 new MockSessionUtils(),
                                                 filterService,
                                                 classificationRuleProvider,
                                                 classificationEngine);

        this.thresholding.setStepSizeMs(1000);
    }

    @After
    public void after() throws Exception {
        this.thresholding.close();
        this.thresholding = null;

        this.databasePopulator.resetDatabase();
    }

    private List<EnrichedFlow> createMockedFlows(final int count) {
        final var now = Instant.now();

        final List<EnrichedFlow> flows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final EnrichedFlow flow = new EnrichedFlow();
            flow.setTimestamp(now.plus(i * 1000L, ChronoUnit.MILLIS));
            flow.setIpProtocolVersion(4);
            flow.setInputSnmp(1);
            flow.setOutputSnmp(2);
            flow.setSrcAddr(String.format("192.168.%d.%d", i % 256, 255 - (i % 256)));
            flow.setDstAddr(String.format("192.168.%d.%d", 255 - (i % 256), i % 256));
            flow.setSrcPort(1);
            flow.setDstPort(2);
            flow.setProtocol(6);
            flow.setBytes(1024L * (count - i));
            flow.setDirection(Flow.Direction.INGRESS);

            flow.setApplication("APP2");
            flow.setExporterNodeInfo(new NodeInfo() {{
                this.setNodeId(databasePopulator.getNode1().getId());
                this.setForeignSource(databasePopulator.getNode1().getForeignSource());
                this.setForeignId(databasePopulator.getNode1().getForeignId());
                this.setInterfaceId(databasePopulator.getNode1().getPrimaryInterface().getId());
            }});

            flows.add(flow);
        }
        return flows;
    }

    @Test
    public void testThresholding() throws Exception {
        final var eventAnticipator = this.mockEventIpcManager.getEventAnticipator();
        eventAnticipator.anticipateEvent(new EventBuilder(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "Test")
                                                 .setNodeid(this.databasePopulator.getNode1().getId())
                                                 .setInterface(addr("192.168.1.1"))
                                                 .setService(FlowThresholdingImpl.SERVICE_NAME)
                                                 .getEvent());
        eventAnticipator.anticipateEvent(new EventBuilder(EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, "Test")
                                                 .setNodeid(this.databasePopulator.getNode1().getId())
                                                 .setInterface(addr("192.168.1.1"))
                                                 .setService(FlowThresholdingImpl.SERVICE_NAME)
                                                 .getEvent());

        final var source = new FlowSource(this.databasePopulator.getNode1().getLocation().getLocationName(),
                                          InetAddressUtils.str(this.databasePopulator.getNode1().getPrimaryInterface().getIpAddress()),
                                          null);

        assertEquals(0, eventAnticipator.getUnanticipatedEvents().size());

        // Sending just one flow, so that counters are initialized before starting the run
        this.transactionTemplate.execute((tx) -> {
            try {
                this.thresholding.threshold(createMockedFlows(1),
                                            ProcessingOptions.builder()
                                                             .setApplicationThresholding(true)
                                                             .build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        // Sleep roundabout a second, so that at least one thresholding run has finished
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Now, send the all the flows for triggering the threshold
        this.transactionTemplate.execute((tx) -> {
            try {
                this.thresholding.threshold(createMockedFlows(1000),
                                            ProcessingOptions.builder()
                                                             .setApplicationThresholding(true)
                                                             .build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        await().atMost(60, TimeUnit.SECONDS).until(eventAnticipator::getAnticipatedEvents, hasSize(0));

        eventAnticipator.verifyAnticipated();
    }

    @Test
    public void testApplications() throws Exception {
        for(FlowThresholdingImpl.Session session : this.thresholding.getSessions()) {
            for(Map.Entry<FlowThresholdingImpl.IndexKey, Map<String, AtomicLong>> entry : session.indexKeyMap.entrySet()) {
                assertEquals(2, entry.getValue().size());
                assertTrue(entry.getValue().containsKey("APP1"));
                assertTrue(entry.getValue().containsKey("APP2"));
            }
        }

        this.rules = Lists.newArrayList(
                new RuleBuilder().withName("APP1").withDstPort("1").withPosition(1).build(),
                new RuleBuilder().withName("APP2").withDstPort("2").withPosition(1).build(),
                new RuleBuilder().withName("APP3").withDstPort("3").withPosition(1).build()
        );

        this.thresholding.classificationRulesReloaded(this.rules);
        this.thresholding.runTimerTask();

        for(FlowThresholdingImpl.Session session : this.thresholding.getSessions()) {
            for(Map.Entry<FlowThresholdingImpl.IndexKey, Map<String, AtomicLong>> entry : session.indexKeyMap.entrySet()) {
                assertEquals(3, entry.getValue().size());
                assertTrue(entry.getValue().containsKey("APP1"));
                assertTrue(entry.getValue().containsKey("APP2"));
                assertTrue(entry.getValue().containsKey("APP3"));
            }
        }

        this.rules = Lists.newArrayList(
                new RuleBuilder().withName("APP1").withDstPort("1").withPosition(1).build()
        );

        this.thresholding.classificationRulesReloaded(this.rules);
        this.thresholding.runTimerTask();

        for(FlowThresholdingImpl.Session session : this.thresholding.getSessions()) {
            for(Map.Entry<FlowThresholdingImpl.IndexKey, Map<String, AtomicLong>> entry : session.indexKeyMap.entrySet()) {
                assertEquals(1, entry.getValue().size());
                assertTrue(entry.getValue().containsKey("APP1"));
            }
        }
    }

    @Test
    public void testHousekeeping() throws Exception {
        this.thresholding.setIdleTimeoutMs(2000);

        final var source = new FlowSource(this.databasePopulator.getNode1().getLocation().getLocationName(),
                                          InetAddressUtils.str(this.databasePopulator.getNode1().getPrimaryInterface().getIpAddress()),
                                          null);

        this.transactionTemplate.execute((tx) -> {
            try {
                this.thresholding.threshold(createMockedFlows(1),
                                            ProcessingOptions.builder()
                                                             .setApplicationThresholding(true)
                                                             .build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        assertThat(this.thresholding.getExporterKeys(), hasSize(1));

        await().atMost(60, TimeUnit.SECONDS).until(this.thresholding::getExporterKeys, hasSize(0));
    }
}
