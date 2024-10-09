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

package org.opennms.netmgt.flows.elastic;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.script.ScriptEngineManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.flows.processing.TestFlow;
import org.opennms.netmgt.flows.processing.impl.DocumentEnricherImpl;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.flows.processing.impl.DocumentMangler;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfo;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfoCache;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfoCacheImpl;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class NodeIdentificationIT {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    private int nodeAId;
    private int nodeBId;
    private int nodeCId;
    private int nodeDId;
    private int nodeEId;

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        this.databasePopulator.populateDatabase();

        nodeAId = this.databasePopulator.getNodeDao().save(buildNodeA());
        nodeBId = this.databasePopulator.getNodeDao().save(buildNodeB());
        nodeCId = this.databasePopulator.getNodeDao().save(buildNodeC());
        nodeDId = this.databasePopulator.getNodeDao().save(buildNodeD());
        nodeEId = this.databasePopulator.getNodeDao().save(buildNodeE());

        this.interfaceToNodeCache.dataSourceSync();
    }

    @Test
    public void testSomething() throws InterruptedException {
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(() -> Collections.emptyList(), FilterService.NOOP);
        final NodeInfoCache nodeInfoCache = new NodeInfoCacheImpl(
                new CacheConfigBuilder()
                        .withName("nodeInfoCache")
                        .withMaximumSize(1000)
                        .withExpireAfterWrite(300)
                        .withExpireAfterRead(300)
                        .build(),
                true,
                new MetricRegistry(),
                databasePopulator.getNodeDao(),
                databasePopulator.getIpInterfaceDao(),
                interfaceToNodeCache,
                new MockSessionUtils()
        );

        final DocumentEnricherImpl documentEnricher = new DocumentEnricherImpl(
                sessionUtils,
                classificationEngine,
                 0,
                new DocumentMangler(new ScriptEngineManager()),
                nodeInfoCache);

        final TestFlow testFlow = new TestFlow();
        testFlow.setSrcAddr("1.1.1.1");
        testFlow.setSrcPort(1);
        testFlow.setDstAddr("2.2.2.2");
        testFlow.setDstPort(2);
        testFlow.setProtocol(6);
        testFlow.setDirection(Flow.Direction.INGRESS);

        List<EnrichedFlow> flowDocuments;

        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.99.99", null));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo().getNodeId(), is(nodeAId));

        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.11.11", null));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo().getNodeId(), is(nodeBId));

        testFlow.setNodeIdentifier("99099");
        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.22.22", new ContextKey("testContext","testKey")));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo().getNodeId(), is(nodeAId));

        testFlow.setNodeIdentifier("11011");
        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.22.22", new ContextKey("testContext","testKey")));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo().getNodeId(), is(nodeBId));

        testFlow.setNodeIdentifier("22022");
        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.22.22", new ContextKey("testContext","testKey")));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo(), nullValue());
    }

    private OnmsNode buildNodeA() {
        final NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("nodeA").setForeignSource("nodeIdTest:").setForeignId("A").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.99.99").setIsManaged("M").setIsSnmpPrimary("P");
        OnmsNode node = builder.getCurrentNode();
        node.getMetaData().add(new OnmsMetaData("testContext", "testKey", "99099"));
        return node;
    }

    private OnmsNode buildNodeB() {
        final NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("nodeB").setForeignSource("nodeIdTest:").setForeignId("B").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.11.11").setIsManaged("M").setIsSnmpPrimary("P");
        OnmsNode node = builder.getCurrentNode();
        node.getMetaData().add(new OnmsMetaData("testContext", "testKey", "11011"));
        return node;
    }

    private OnmsNode buildNodeC() {
        final NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("nodeC").setForeignSource("nodeIdTest:").setForeignId("C").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.33.33").setIsManaged("M").setIsSnmpPrimary("P");
        OnmsNode node = builder.getCurrentNode();
        node.getMetaData().add(new OnmsMetaData("testContext", "testKey", "55055,33033 , 44044"));
        return node;
    }

    private OnmsNode buildNodeD() {
        final NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("nodeD").setForeignSource("nodeIdTest:").setForeignId("D").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.88.88").setIsManaged("M").setIsSnmpPrimary("P");
        OnmsNode node = builder.getCurrentNode();
        node.getPrimaryInterface().getMetaData().add(new OnmsMetaData("testContext", "testKey", "88088, 77077"));
        return node;
    }

    private OnmsNode buildNodeE() {
        final NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("nodeE").setForeignSource("nodeIdTest:").setForeignId("E").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.77.77").setIsManaged("M").setIsSnmpPrimary("P");
        OnmsNode node = builder.getCurrentNode();
        node.getMetaData().add(new OnmsMetaData("testContext", "testKey", "55055,.\\*{}(?) , 44044"));
        return node;
    }

    @Test
    public void testNodeCache() throws InterruptedException {
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(() -> Collections.emptyList(), FilterService.NOOP);
        final NodeInfoCache nodeInfoCache = new NodeInfoCacheImpl(
                new CacheConfigBuilder()
                        .withName("nodeInfoCache")
                        .withMaximumSize(1000)
                        .withExpireAfterWrite(300)
                        .withExpireAfterRead(300)
                        .build(),
                true,
                new MetricRegistry(),
                databasePopulator.getNodeDao(),
                databasePopulator.getIpInterfaceDao(),
                interfaceToNodeCache,
                new MockSessionUtils()
        );

        final DocumentEnricherImpl documentEnricher = new DocumentEnricherImpl(
                sessionUtils,
                classificationEngine,
                0,
                new DocumentMangler(new ScriptEngineManager()),
                nodeInfoCache);

        final TestFlow testFlow = new TestFlow();
        testFlow.setSrcAddr("1.1.1.1");
        testFlow.setSrcPort(1);
        testFlow.setDstAddr("2.2.2.2");
        testFlow.setDstPort(2);
        testFlow.setProtocol(6);
        testFlow.setDirection(Flow.Direction.INGRESS);
        sessionUtils.withTransaction(new Runnable() {
            @Override
            public void run() {
                final Optional<NodeInfo> nodeInfoA = nodeInfoCache.getNodeInfoFromCache(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "3.3.3.3", new ContextKey("testContext", "testKey"), "99099");
                Assert.assertThat(nodeInfoA.isPresent(), is(true));
                Assert.assertThat(nodeInfoA.get().getNodeId(), is(nodeAId));
                final Optional<NodeInfo> nodeInfoB = nodeInfoCache.getNodeInfoFromCache(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "3.3.3.3", new ContextKey("testContext", "testKey"), "11011");
                Assert.assertThat(nodeInfoB.isPresent(), is(true));

                Assert.assertThat(nodeInfoB.get().getNodeId(), is(nodeBId));
                final Optional<NodeInfo> nodeInfoN = nodeInfoCache.getNodeInfoFromCache(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "3.3.3.3", new ContextKey("testContext", "testKey"), "12345");
                Assert.assertThat(nodeInfoN.isPresent(), is(false));

                final Optional<NodeInfo> nodeInfoC1 = nodeInfoCache.getNodeInfoFromCache(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "3.3.3.3", new ContextKey("testContext", "testKey"), "33033");
                Assert.assertThat(nodeInfoC1.isPresent(), is(true));
                Assert.assertThat(nodeInfoC1.get().getNodeId(), is(nodeCId));
                final Optional<NodeInfo> nodeInfoC2 = nodeInfoCache.getNodeInfoFromCache(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "3.3.3.3", new ContextKey("testContext", "testKey"), "55055");
                Assert.assertThat(nodeInfoC2.isPresent(), is(true));
                Assert.assertThat(nodeInfoC2.get().getNodeId(), is(nodeCId));
                final Optional<NodeInfo> nodeInfoC3 = nodeInfoCache.getNodeInfoFromCache(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "3.3.3.3", new ContextKey("testContext", "testKey"), "44044");
                Assert.assertThat(nodeInfoC3.isPresent(), is(true));
                Assert.assertThat(nodeInfoC3.get().getNodeId(), is(nodeCId));

                final Optional<NodeInfo> nodeInfoD1 = nodeInfoCache.getNodeInfoFromCache(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "3.3.3.3", new ContextKey("testContext", "testKey"), "88088");
                Assert.assertThat(nodeInfoD1.isPresent(), is(true));
                Assert.assertThat(nodeInfoD1.get().getNodeId(), is(nodeDId));
                final Optional<NodeInfo> nodeInfoD2 = nodeInfoCache.getNodeInfoFromCache(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "3.3.3.3", new ContextKey("testContext", "testKey"), "77077");
                Assert.assertThat(nodeInfoD2.isPresent(), is(true));
                Assert.assertThat(nodeInfoD2.get().getNodeId(), is(nodeDId));

                final Optional<NodeInfo> nodeInfoE = nodeInfoCache.getNodeInfoFromCache(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "3.3.3.3", new ContextKey("testContext", "testKey"), ".\\*{}(?)");
                Assert.assertThat(nodeInfoE.isPresent(), is(true));
                Assert.assertThat(nodeInfoE.get().getNodeId(), is(nodeEId));
            }
        });
    }
}
