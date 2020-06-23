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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.core.rpc.utils.mate.ContextKey;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
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
public class NodeIdentificationTest {

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

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        this.databasePopulator.populateDatabase();

        nodeAId = this.databasePopulator.getNodeDao().save(buildNodeA());
        nodeBId = this.databasePopulator.getNodeDao().save(buildNodeB());

        this.interfaceToNodeCache.dataSourceSync();
    }

    @Test
    public void testSomething() {
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(() -> Collections.emptyList(), FilterService.NOOP);
        final DocumentEnricher documentEnricher = new DocumentEnricher(
                new MetricRegistry(), databasePopulator.getNodeDao(), interfaceToNodeCache, sessionUtils, classificationEngine,
                new CacheConfigBuilder()
                        .withName("flows.node")
                        .withMaximumSize(1000)
                        .withExpireAfterWrite(300)
                        .build());

        final FlowDocument flowDocument = new FlowDocument();
        flowDocument.setSrcAddr("1.1.1.1");
        flowDocument.setSrcPort(1);
        flowDocument.setDstAddr("2.2.2.2");
        flowDocument.setDstPort(2);
        flowDocument.setProtocol(6);
        flowDocument.setDirection(Direction.INGRESS);
        TestFlow testFlow = new TestFlow(flowDocument);

        List<FlowDocument> flowDocuments;

        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.99.99", null));
        Assert.assertThat(flowDocuments.get(0).getNodeExporter().getNodeId(), is(nodeAId));

        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.11.11", null));
        Assert.assertThat(flowDocuments.get(0).getNodeExporter().getNodeId(), is(nodeBId));

        testFlow.setNodeIdentifier("99099");
        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.22.22", new ContextKey("testContext","testKey")));
        Assert.assertThat(flowDocuments.get(0).getNodeExporter().getNodeId(), is(nodeAId));

        testFlow.setNodeIdentifier("11011");
        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.22.22", new ContextKey("testContext","testKey")));
        Assert.assertThat(flowDocuments.get(0).getNodeExporter().getNodeId(), is(nodeBId));

        testFlow.setNodeIdentifier("22022");
        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.22.22", new ContextKey("testContext","testKey")));
        Assert.assertThat(flowDocuments.get(0).getNodeExporter(), nullValue());
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
}
